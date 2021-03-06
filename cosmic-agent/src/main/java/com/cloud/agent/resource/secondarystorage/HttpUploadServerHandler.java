package com.cloud.agent.resource.secondarystorage;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.storage.UploadEntity;
import com.cloud.utils.imagestore.ImageStoreUtil;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUploadServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static final Logger logger = LoggerFactory.getLogger(HttpUploadServerHandler.class.getName());

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(true);
    private static final String HEADER_SIGNATURE = "X-signature";
    private static final String HEADER_METADATA = "X-metadata";
    private static final String HEADER_EXPIRES = "X-expires";
    private static final String HEADER_HOST = "X-Forwarded-Host";
    private final StringBuilder responseContent = new StringBuilder();
    private HttpRequest request;
    private HttpPostRequestDecoder decoder;
    private final NfsSecondaryStorageResource storageResource;
    private String uuid;
    private boolean requestProcessed = false;

    public HttpUploadServerHandler(final NfsSecondaryStorageResource storageResource) {
        this.storageResource = storageResource;
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        if (this.decoder != null) {
            this.decoder.cleanFiles();
        }
        this.requestProcessed = false;
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        if (!this.requestProcessed) {
            final String message = "file receive failed or connection closed prematurely.";
            logger.error(message);
            this.storageResource.updateStateMapWithError(this.uuid, message);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        logger.warn(this.responseContent.toString(), cause);
        this.responseContent.append("\r\nException occurred: ").append(cause.getMessage());
        writeResponse(ctx.channel(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
        ctx.channel().close();
    }

    private void writeResponse(final Channel channel, final HttpResponseStatus statusCode) {
        // Convert the response content to a ChannelBuffer.
        final ByteBuf buf = copiedBuffer(this.responseContent.toString(), CharsetUtil.UTF_8);
        this.responseContent.setLength(0);
        // Decide whether to close the connection or not.
        final boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(this.request.headers().get(CONNECTION)) ||
                this.request.getProtocolVersion().equals(HttpVersion.HTTP_1_0) && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(this.request.headers().get(CONNECTION));
        // Build the response object.
        final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, statusCode, buf);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        if (!close) {
            // There's no need to add 'Content-Length' header if this is the last response.
            response.headers().set(CONTENT_LENGTH, buf.readableBytes());
        }
        // Write the response.
        final ChannelFuture future = channel.writeAndFlush(response);
        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            final HttpRequest request = this.request = (HttpRequest) msg;
            this.responseContent.setLength(0);

            if (request.getMethod().equals(HttpMethod.POST)) {

                final URI uri = new URI(request.getUri());

                String signature = null;
                String expires = null;
                String metadata = null;
                String hostname = null;
                long contentLength = 0;

                for (final Entry<String, String> entry : request.headers()) {
                    switch (entry.getKey()) {
                        case HEADER_SIGNATURE:
                            signature = entry.getValue();
                            break;
                        case HEADER_METADATA:
                            metadata = entry.getValue();
                            break;
                        case HEADER_EXPIRES:
                            expires = entry.getValue();
                            break;
                        case HEADER_HOST:
                            hostname = entry.getValue();
                            break;
                        case HttpHeaders.Names.CONTENT_LENGTH:
                            contentLength = Long.parseLong(entry.getValue());
                            break;
                    }
                }
                logger.info("HEADER: signature=" + signature);
                logger.info("HEADER: metadata=" + metadata);
                logger.info("HEADER: expires=" + expires);
                logger.info("HEADER: hostname=" + hostname);
                logger.info("HEADER: Content-Length=" + contentLength);
                final QueryStringDecoder decoderQuery = new QueryStringDecoder(uri);
                final Map<String, List<String>> uriAttributes = decoderQuery.parameters();
                this.uuid = uriAttributes.get("uuid").get(0);
                logger.info("URI: uuid=" + this.uuid);

                UploadEntity uploadEntity = null;
                try {
                    // Validate the request here
                    this.storageResource.validatePostUploadRequest(signature, metadata, expires, hostname, contentLength, this.uuid);
                    //create an upload entity. This will fail if entity already exists.
                    uploadEntity = this.storageResource.createUploadEntity(this.uuid, metadata, contentLength);
                } catch (final InvalidParameterValueException ex) {
                    logger.error("post request validation failed", ex);
                    this.responseContent.append(ex.getMessage());
                    writeResponse(ctx.channel(), HttpResponseStatus.BAD_REQUEST);
                    this.requestProcessed = true;
                    return;
                }
                if (uploadEntity == null) {
                    logger.error("Unable to create upload entity. An exception occurred.");
                    this.responseContent.append("Internal Server Error");
                    writeResponse(ctx.channel(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    this.requestProcessed = true;
                    return;
                }
                //set the base directory to download the file
                DiskFileUpload.baseDirectory = uploadEntity.getInstallPathPrefix();
                logger.info("base directory: " + DiskFileUpload.baseDirectory);
                try {
                    //initialize the decoder
                    this.decoder = new HttpPostRequestDecoder(factory, request);
                } catch (final ErrorDataDecoderException e) {
                    logger.error("exception while initialising the decoder", e);
                    this.responseContent.append(e.getMessage());
                    writeResponse(ctx.channel(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    this.requestProcessed = true;
                    return;
                }
            } else {
                logger.warn("received a get request");
                this.responseContent.append("only post requests are allowed");
                writeResponse(ctx.channel(), HttpResponseStatus.BAD_REQUEST);
                this.requestProcessed = true;
                return;
            }
        }
        // check if the decoder was constructed before
        if (this.decoder != null) {
            if (msg instanceof HttpContent) {
                // New chunk is received
                final HttpContent chunk = (HttpContent) msg;
                try {
                    this.decoder.offer(chunk);
                } catch (final ErrorDataDecoderException e) {
                    logger.error("data decoding exception", e);
                    this.responseContent.append(e.getMessage());
                    writeResponse(ctx.channel(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    this.requestProcessed = true;
                    return;
                }
                if (chunk instanceof LastHttpContent) {
                    writeResponse(ctx.channel(), readFileUploadData());
                    reset();
                }
            }
        }
    }

    private HttpResponseStatus readFileUploadData() throws IOException {
        while (this.decoder.hasNext()) {
            final InterfaceHttpData data = this.decoder.next();
            if (data != null) {
                try {
                    logger.info("BODY FileUpload: " + data.getHttpDataType().name() + ": " + data);
                    if (data.getHttpDataType() == HttpDataType.FileUpload) {
                        final FileUpload fileUpload = (FileUpload) data;
                        if (fileUpload.isCompleted()) {
                            this.requestProcessed = true;
                            final String format = ImageStoreUtil.checkTemplateFormat(fileUpload.getFile().getAbsolutePath(), fileUpload.getFilename());
                            if (StringUtils.isNotBlank(format)) {
                                final String errorString = "File type mismatch between the sent file and the actual content. Received: " + format;
                                logger.error(errorString);
                                this.responseContent.append(errorString);
                                this.storageResource.updateStateMapWithError(this.uuid, errorString);
                                return HttpResponseStatus.BAD_REQUEST;
                            }
                            final String status = this.storageResource.postUpload(this.uuid, fileUpload.getFile().getName());
                            if (status != null) {
                                this.responseContent.append(status);
                                this.storageResource.updateStateMapWithError(this.uuid, status);
                                return HttpResponseStatus.INTERNAL_SERVER_ERROR;
                            } else {
                                this.responseContent.append("upload successful.");
                                return HttpResponseStatus.OK;
                            }
                        }
                    }
                } finally {
                    data.release();
                }
            }
        }
        this.responseContent.append("received entity is not a file");
        return HttpResponseStatus.UNPROCESSABLE_ENTITY;
    }

    private void reset() {
        this.request = null;
        // destroy the decoder to release all resources
        this.decoder.destroy();
        this.decoder = null;
    }
}

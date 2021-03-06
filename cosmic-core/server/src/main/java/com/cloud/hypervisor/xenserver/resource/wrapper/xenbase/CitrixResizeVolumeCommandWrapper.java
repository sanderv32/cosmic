package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.ResizeVolumeAnswer;
import com.cloud.legacymodel.communication.command.ResizeVolumeCommand;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.VDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = ResizeVolumeCommand.class)
public final class CitrixResizeVolumeCommandWrapper extends CommandWrapper<ResizeVolumeCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixResizeVolumeCommandWrapper.class);

    @Override
    public Answer execute(final ResizeVolumeCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        final String volid = command.getPath();
        final long newSize = command.getNewSize();

        try {
            final VDI vdi = citrixResourceBase.getVDIbyUuid(conn, volid);
            vdi.resize(conn, newSize);
            return new ResizeVolumeAnswer(command, true, "success", newSize);
        } catch (final Exception e) {
            s_logger.warn("Unable to resize volume", e);
            final String error = "failed to resize volume:" + e;
            return new ResizeVolumeAnswer(command, false, error);
        }
    }
}

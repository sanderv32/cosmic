package com.cloud.legacymodel.communication.command;

public class ShutdownCommand extends Command {
    public static final String Requested = "sig.kill";
    public static final String Update = "update";
    public static final String Unknown = "unknown";
    public static final String DeleteHost = "deleteHost";

    private String reason;
    private String detail;

    protected ShutdownCommand() {
        super();
    }

    public ShutdownCommand(final String reason, final String detail) {
        super();
        this.reason = reason;
        this.detail = detail;
    }

    public ShutdownCommand(final String reason) {
        super();
        this.reason = reason;
    }

    /**
     * @return return the reason the agent shutdown.  If Unknown, call getDetail() for any details.
     */
    public String getReason() {
        return reason;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}

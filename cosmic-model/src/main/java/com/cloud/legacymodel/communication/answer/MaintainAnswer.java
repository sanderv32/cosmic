package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.MaintainCommand;

public class MaintainAnswer extends Answer {
    boolean willMigrate;

    public MaintainAnswer() {
    }

    public MaintainAnswer(final MaintainCommand cmd) {
        this(cmd, true, null);
    }

    public MaintainAnswer(final MaintainCommand cmd, final boolean result, final String details) {
        super(cmd, result, details);
        this.willMigrate = true;
    }

    public MaintainAnswer(final MaintainCommand cmd, final boolean willMigrate) {
        this(cmd, true, null);
        this.willMigrate = willMigrate;
    }

    public MaintainAnswer(final MaintainCommand cmd, final String details) {
        this(cmd, true, details);
    }

    public boolean getMigrate() {
        return this.willMigrate;
    }
}

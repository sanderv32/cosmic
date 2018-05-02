package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;

public class MigrateAnswer extends Answer {
    Integer vncPort = null;

    protected MigrateAnswer() {
    }

    public MigrateAnswer(final MigrateCommand cmd, final boolean result, final String detail, final Integer vncPort) {
        super(cmd, result, detail);
        this.vncPort = vncPort;
    }

    public Integer getVncPort() {
        return vncPort;
    }
}

package com.cloud.engine.subsystem.api.storage;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.storage.command.CommandResult;

public class CreateCmdResult extends CommandResult {
    private final String path;
    private Answer answer;

    public CreateCmdResult(final String path, final Answer answer) {
        super();
        this.path = path;
        this.answer = answer;
    }

    public String getPath() {
        return this.path;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(final Answer answer) {
        this.answer = answer;
    }
}

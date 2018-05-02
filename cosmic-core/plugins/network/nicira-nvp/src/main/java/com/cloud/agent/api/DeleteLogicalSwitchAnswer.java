package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;

public class DeleteLogicalSwitchAnswer extends Answer {

    public DeleteLogicalSwitchAnswer(final Command command, final Exception e) {
        super(command, false, e.getMessage());
    }

    public DeleteLogicalSwitchAnswer(final Command command, final boolean success, final String details) {
        super(command, success, details);
    }
}

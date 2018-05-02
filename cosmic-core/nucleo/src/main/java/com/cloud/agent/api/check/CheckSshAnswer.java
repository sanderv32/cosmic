package com.cloud.agent.api.check;

import com.cloud.legacymodel.communication.answer.Answer;

public class CheckSshAnswer extends Answer {
    protected CheckSshAnswer() {

    }

    public CheckSshAnswer(final CheckSshCommand cmd) {
        super(cmd, true, null);
    }

    public CheckSshAnswer(final CheckSshCommand cmd, final String details) {
        super(cmd, false, details);
    }

    public CheckSshAnswer(final CheckSshCommand cmd, final Throwable th) {
        super(cmd, false, th.getMessage());
    }
}

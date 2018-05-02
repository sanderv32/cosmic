package com.cloud.agent.api;

import com.cloud.legacymodel.communication.command.Command;

public class RecurringNetworkUsageCommand extends Command implements CronCommand {
    int interval;

    public RecurringNetworkUsageCommand(final int interval) {
        this.interval = interval;
    }

    @Override
    public int getInterval() {
        return interval;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}

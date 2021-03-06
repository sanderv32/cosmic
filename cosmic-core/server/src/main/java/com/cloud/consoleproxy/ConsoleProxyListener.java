package com.cloud.consoleproxy;

import com.cloud.agent.Listener;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.agentcontrol.AgentControlCommand;
import com.cloud.legacymodel.communication.command.agentcontrol.ConsoleAccessAuthenticationCommand;
import com.cloud.legacymodel.communication.command.agentcontrol.ConsoleProxyLoadReportCommand;
import com.cloud.legacymodel.communication.command.startup.StartupCommand;
import com.cloud.legacymodel.communication.command.startup.StartupProxyCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;

public class ConsoleProxyListener implements Listener {
    AgentHook _proxyMgr = null;

    public ConsoleProxyListener(final AgentHook proxyMgr) {
        _proxyMgr = proxyMgr;
    }

    @Override
    public boolean processAnswers(final long agentId, final long seq, final Answer[] answers) {
        return true;
    }

    @Override
    public boolean processCommands(final long agentId, final long seq, final Command[] commands) {
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(final long agentId, final AgentControlCommand cmd) {
        if (cmd instanceof ConsoleProxyLoadReportCommand) {
            _proxyMgr.onLoadReport((ConsoleProxyLoadReportCommand) cmd);

            // return dummy answer
            return new AgentControlAnswer(cmd);
        } else if (cmd instanceof ConsoleAccessAuthenticationCommand) {
            return _proxyMgr.onConsoleAccessAuthentication((ConsoleAccessAuthenticationCommand) cmd);
        }
        return null;
    }

    @Override
    public void processConnect(final Host host, final StartupCommand[] startupCommands, final boolean forRebalance) {
        for (final StartupCommand startupCommand : startupCommands) {
            _proxyMgr.onAgentConnect(host, startupCommand);

            if (startupCommand instanceof StartupProxyCommand) {
                _proxyMgr.startAgentHttpHandlerInVM((StartupProxyCommand) startupCommand);
            }
        }
    }

    @Override
    public boolean processDisconnect(final long agentId, final HostStatus state) {
        _proxyMgr.onAgentDisconnect(agentId, state);
        return true;
    }

    @Override
    public boolean isRecurring() {
        return true;
    }

    @Override
    public int getTimeout() {
        return -1;
    }

    @Override
    public boolean processTimeout(final long agentId, final long seq) {
        return true;
    }
}

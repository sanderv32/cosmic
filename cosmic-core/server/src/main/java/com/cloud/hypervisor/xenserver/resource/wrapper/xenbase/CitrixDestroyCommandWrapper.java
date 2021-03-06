package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.DestroyCommand;
import com.cloud.legacymodel.to.VolumeTO;

import java.util.Set;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.VBD;
import com.xensource.xenapi.VDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = DestroyCommand.class)
public final class CitrixDestroyCommandWrapper extends CommandWrapper<DestroyCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixDestroyCommandWrapper.class);

    @Override
    public Answer execute(final DestroyCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        final VolumeTO vol = command.getVolume();
        // Look up the VDI
        final String volumeUUID = vol.getPath();
        VDI vdi = null;
        try {
            vdi = citrixResourceBase.getVDIbyUuid(conn, volumeUUID);
        } catch (final Exception e) {
            return new Answer(command, true, "Success");
        }
        Set<VBD> vbds = null;
        try {
            vbds = vdi.getVBDs(conn);
        } catch (final Exception e) {
            final String msg = "VDI getVBDS for " + volumeUUID + " failed due to " + e.toString();
            s_logger.warn(msg, e);
            return new Answer(command, false, msg);
        }
        for (final VBD vbd : vbds) {
            try {
                vbd.unplug(conn);
                vbd.destroy(conn);
            } catch (final Exception e) {
                final String msg = "VM destroy for " + volumeUUID + "  failed due to " + e.toString();
                s_logger.warn(msg, e);
                return new Answer(command, false, msg);
            }
        }
        try {
            final Set<VDI> snapshots = vdi.getSnapshots(conn);
            for (final VDI snapshot : snapshots) {
                snapshot.destroy(conn);
            }
            vdi.destroy(conn);
        } catch (final Exception e) {
            final String msg = "VDI destroy for " + volumeUUID + " failed due to " + e.toString();
            s_logger.warn(msg, e);
            return new Answer(command, false, msg);
        }

        return new Answer(command, true, "Success");
    }
}

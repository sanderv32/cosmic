package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.storage.to.VolumeObjectTO;

import java.util.List;

public class MigrateWithStorageAnswer extends Answer {

    List<VolumeObjectTO> volumeTos;

    public MigrateWithStorageAnswer(final MigrateWithStorageCommand cmd, final Exception ex) {
        super(cmd, false, ex.getMessage());
        volumeTos = null;
    }

    public MigrateWithStorageAnswer(final MigrateWithStorageCommand cmd, final List<VolumeObjectTO> volumeTos) {
        super(cmd, true, null);
        this.volumeTos = volumeTos;
    }

    public List<VolumeObjectTO> getVolumeTos() {
        return volumeTos;
    }
}

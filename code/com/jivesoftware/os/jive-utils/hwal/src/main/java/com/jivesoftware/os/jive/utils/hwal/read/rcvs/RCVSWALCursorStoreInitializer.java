package com.jivesoftware.os.jive.utils.hwal.read.rcvs;

import com.jivesoftware.os.jive.utils.hwal.read.WALCursorStore;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALService;
import com.jivesoftware.os.jive.utils.hwal.shared.rcvs.RCVSWALStorage;
import org.merlin.config.Config;

/**
 *I
 * @author jonathan.colt
 */
public class RCVSWALCursorStoreInitializer {

    static public interface RCVSWALCursorStoreConfig extends Config {

    }

    public WALService<WALCursorStore> initialize(RCVSWALCursorStoreConfig config, RCVSWALStorage storage) {

        final WALCursorStore cursorStore = new RCVSWALCursorStore(storage.getCursors());

        return new WALService<WALCursorStore>() {

            @Override
            public WALCursorStore getService() {
                return cursorStore;
            }

            @Override
            public void start() throws Exception {
            }

            @Override
            public void stop() throws Exception {
            }
        };
    }
}

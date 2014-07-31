package com.jivesoftware.os.jive.utils.hwal.write.hbase;

import java.io.IOException;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;

/**
 *
 * @author jonathan
 */
public class ColumnValueAsCurrentTimeCoprocessor extends BaseRegionObserver {

    @Override
    public void prePut(final ObserverContext<RegionCoprocessorEnvironment> e,
            final Put put, final WALEdit edit, final boolean writeToWAL) throws IOException {
        // TODO figure out how ti change the column value to be System.currentTimeMillis();
    }

    @Override
    public void postPut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, boolean writeToWAL) throws IOException {
        // TODO insert future magic here. Make is so consumer attach to region servers and get notified that there have been a changes instead of polling the
        // hell out of HBase.
    }

}

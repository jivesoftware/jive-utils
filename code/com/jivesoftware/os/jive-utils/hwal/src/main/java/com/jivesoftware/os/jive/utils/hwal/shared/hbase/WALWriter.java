package com.jivesoftware.os.jive.utils.hwal.shared.hbase;

import com.jivesoftware.os.jive.utils.id.TenantId;
import java.util.List;

/**
 * @author jonathan
 */
public interface WALWriter {

    void write(TenantId tenantId, List<WALEntry> entried) throws Exception;

}

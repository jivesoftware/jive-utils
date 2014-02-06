/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.chunk.store;

import com.jivesoftware.os.jive.utils.chunk.store.filers.ByteBufferBackedFiler;
import com.jivesoftware.os.jive.utils.chunk.store.filers.FileBackedMemMappedByteBufferFactory;
import com.jivesoftware.os.jive.utils.chunk.store.filers.Filer;
import com.jivesoftware.os.jive.utils.chunk.store.filers.FilerIO;
import com.jivesoftware.os.jive.utils.chunk.store.filers.SubsetableFiler;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan.colt
 */
public class ChunkStoreTest {

    @Test
    public void chunkStoreTest() throws IOException, Exception {
        int size = 1024 * 10;
        File tmp = File.createTempFile("chunk", "1");
        FileBackedMemMappedByteBufferFactory bufferFactory = new FileBackedMemMappedByteBufferFactory(tmp, size);
        ByteBuffer byteBuffer = bufferFactory.allocate();
        ByteBufferBackedFiler byteBufferBackedFiler = new ByteBufferBackedFiler(tmp, byteBuffer);

        ChunkStore chunkStore = new ChunkStore();
        chunkStore.open(new SubsetableFiler(byteBufferBackedFiler, 0, size, 0));

        long chunk10 = chunkStore.newChunk(10);
        System.out.println("chunkId:" + chunk10);
        Filer filer = chunkStore.getFiler(chunk10);
        synchronized (filer.lock()) {
            FilerIO.writeInt(filer, 10, "");
        }

        filer = chunkStore.getFiler(chunk10);
        synchronized (filer.lock()) {
            filer.seek(0);
            int ten = FilerIO.readInt(filer, "");
            System.out.println("ten:" + ten);
            Assert.assertEquals(ten, 10);
        }
    }
}

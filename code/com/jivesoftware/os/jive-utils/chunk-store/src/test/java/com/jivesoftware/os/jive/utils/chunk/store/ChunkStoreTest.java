/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.chunk.store;

import com.jivesoftware.os.jive.utils.io.Filer;
import com.jivesoftware.os.jive.utils.io.FilerIO;
import java.io.File;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author jonathan.colt
 */
public class ChunkStoreTest {

    @Test
    public void testNewChunkStore() throws Exception {
        int size = 1024 * 10;
        File chunkFile = File.createTempFile("testNewChunkStore", "1");
        ChunkStore chunkStore = new ChunkStoreInitializer().initialize(chunkFile.getAbsolutePath(), size, false);

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
            assertEquals(ten, 10);
        }
    }

    @Test
    public void testExistingChunkStore() throws Exception {
        int size = 1024 * 10;
        File chunkFile = File.createTempFile("testExistingChunkStore", "1");
        ChunkStore chunkStore = new ChunkStoreInitializer().initialize(chunkFile.getAbsolutePath(), size, false);

        long chunk10 = chunkStore.newChunk(10);
        System.out.println("chunkId:" + chunk10);
        Filer filer = chunkStore.getFiler(chunk10);
        synchronized (filer.lock()) {
            FilerIO.writeInt(filer, 10, "");
        }
        filer.close();

        long expectedReferenceNumber = chunkStore.getReferenceNumber();

        chunkStore = new ChunkStoreInitializer().initialize(chunkFile.getAbsolutePath(), size, false);
        assertEquals(chunkStore.getReferenceNumber(), expectedReferenceNumber);

        filer = chunkStore.getFiler(chunk10);
        synchronized (filer.lock()) {
            filer.seek(0);
            int ten = FilerIO.readInt(filer, "");
            System.out.println("ten:" + ten);
            assertEquals(ten, 10);
        }
    }

    @Test
    public void testResizingChunkStore() throws Exception {
        int size = 512;
        File chunkFile = File.createTempFile("testResizingChunkStore", "1");
        ChunkStore chunkStore = new ChunkStoreInitializer().initialize(chunkFile.getAbsolutePath(), size, true);

        long chunk10 = chunkStore.newChunk(size * 4);
        System.out.println("chunkId:" + chunk10);
        Filer filer = chunkStore.getFiler(chunk10);
        synchronized (filer.lock()) {
            byte[] bytes = new byte[size * 4];
            bytes[0] = 1;
            bytes[bytes.length - 1] = 1;
            FilerIO.write(filer, bytes);
        }

        filer = chunkStore.getFiler(chunk10);
        synchronized (filer.lock()) {
            filer.seek(0);
            byte[] bytes = new byte[size * 4];
            FilerIO.read(filer, bytes);
            assertEquals(bytes[0], 1);
            assertEquals(bytes[bytes.length - 1], 1);
        }
    }
}

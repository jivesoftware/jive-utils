/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.keyed.store;

import com.jivesoftware.os.jive.utils.chunk.store.ChunkStore;
import com.jivesoftware.os.jive.utils.chunk.store.ChunkStoreInitializer;
import com.jivesoftware.os.jive.utils.io.Filer;
import com.jivesoftware.os.jive.utils.io.FilerIO;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author jonathan.colt
 */
public class FileBackedKeyedStoreTest {

    @Test
    public void keyedStoreTest() throws Exception {
        File mapDir = Files.createTempDirectory("map").toFile();
        File swapDir = Files.createTempDirectory("swap").toFile();
        Path chunksDir = Files.createTempDirectory("chunks");
        File chunks = new File(chunksDir.toFile(), "chunks.data");

        ChunkStoreInitializer chunkStoreInitializer = new ChunkStoreInitializer();
        ChunkStore chunkStore = chunkStoreInitializer.initialize(chunks.getAbsolutePath(), 30 * 1024 * 1024);
        FileBackedKeyedStore fileBackedKeyedStore = new FileBackedKeyedStore(mapDir.getAbsolutePath(), swapDir.getAbsolutePath(), 4, 100, chunkStore, 512);

        byte[] key = FilerIO.intBytes(1010);
        Filer filer = fileBackedKeyedStore.get(key);
        synchronized (filer.lock()) {
            FilerIO.writeInt(filer, 10, "");
        }

        fileBackedKeyedStore = new FileBackedKeyedStore(mapDir.getAbsolutePath(), swapDir.getAbsolutePath(), 4, 100, chunkStore, 512);
        filer = fileBackedKeyedStore.get(key);
        synchronized (filer.lock()) {
            filer.seek(0);
            int ten = FilerIO.readInt(filer, "");
            System.out.println("ten:" + ten);
            Assert.assertEquals(ten, 10);
        }
    }

    @Test
    public void swapTest() throws Exception {
        File mapDir = Files.createTempDirectory("map").toFile();
        File swapDir = Files.createTempDirectory("swap").toFile();
        Path chunksDir = Files.createTempDirectory("chunks");
        File chunks = new File(chunksDir.toFile(), "chunks.data");

        ChunkStoreInitializer chunkStoreInitializer = new ChunkStoreInitializer();
        ChunkStore chunkStore = chunkStoreInitializer.initialize(chunks.getAbsolutePath(), 30 * 1024 * 1024);
        FileBackedKeyedStore fileBackedKeyedStore = new FileBackedKeyedStore(mapDir.getAbsolutePath(), swapDir.getAbsolutePath(), 4, 100, chunkStore, 512);

        byte[] key = FilerIO.intBytes(1020);
        SwappableFiler filer = fileBackedKeyedStore.get(key);
        synchronized (filer.lock()) {
            FilerIO.writeInt(filer, 10, "");

            SwappingFiler swappingFiler = filer.swap(4);
            FilerIO.writeInt(swappingFiler, 20, "");
            swappingFiler.commit();
        }

        fileBackedKeyedStore = new FileBackedKeyedStore(mapDir.getAbsolutePath(), swapDir.getAbsolutePath(), 4, 100, chunkStore, 512);
        filer = fileBackedKeyedStore.get(key);
        synchronized (filer.lock()) {
            filer.seek(0);
            int twenty = FilerIO.readInt(filer, "");
            System.out.println("twenty:" + twenty);
            Assert.assertEquals(twenty, 20);
        }
    }
}

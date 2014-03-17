/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.keyed.store;

import com.jivesoftware.os.jive.utils.io.Filer;
import com.jivesoftware.os.jive.utils.io.FilerIO;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan.colt
 */
public class FileBackedKeyedStoreTest {

    @Test
    public void keyedStoreTest() {
        try {
            File mapDir = Files.createTempDirectory("map").toFile();
            Path chunksDir = Files.createTempDirectory("chunks");
            File chunks = new File(chunksDir.toFile(), "chunks.data");

            FileBackedKeyedStore fileBackedKeyedStore = new FileBackedKeyedStore(mapDir.getAbsolutePath(), 4, 100,
                    chunks.getAbsolutePath(), 30 * 1024 * 1024, 512);

            Filer filer = fileBackedKeyedStore.get(FilerIO.intBytes(10));
            synchronized (filer.lock()) {
                FilerIO.writeInt(filer, 10, "");
            }

            fileBackedKeyedStore = new FileBackedKeyedStore(chunks.getParentFile().getAbsolutePath(), 4, 100,
                    chunks.getAbsolutePath(), 30 * 1024 * 1024, 512);
            filer = fileBackedKeyedStore.get(FilerIO.intBytes(10));
            synchronized (filer.lock()) {
                filer.seek(0);
                int ten = FilerIO.readInt(filer, "");
                System.out.println("ten:" + ten);
                Assert.assertEquals(ten, 10);
            }
        } catch (Exception x) {
            x.printStackTrace();
        }

    }
}

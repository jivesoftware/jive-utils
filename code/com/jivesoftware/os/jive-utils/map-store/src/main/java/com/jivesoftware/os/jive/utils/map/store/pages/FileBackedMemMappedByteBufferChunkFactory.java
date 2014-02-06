/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.map.store.pages;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author jonathan.colt
 */
public class FileBackedMemMappedByteBufferChunkFactory implements ChunkFactory {

    private final File file;

    public FileBackedMemMappedByteBufferChunkFactory(File file) {
        this.file = file;
    }

    public MappedByteBuffer open() {
        try {
            ensureDirectory(file);
            MappedByteBuffer buf;
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.seek(0);
                FileChannel channel = raf.getChannel();
                buf = channel.map(FileChannel.MapMode.READ_WRITE, 0, (int) channel.size());
            }
            return buf;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Chunk allocate(long _size) {
        try {
            ensureDirectory(file);
            MappedByteBuffer buf;
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.seek(_size);
                raf.write(0);
                raf.seek(0);
                FileChannel channel = raf.getChannel();
                buf = channel.map(FileChannel.MapMode.READ_WRITE, 0, (int) channel.size());
            }
            return new ByteBufferChunk(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureDirectory(File _file) {
        if (!_file.exists()) {
            File parent = _file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs()) {
                    throw new RuntimeException("Failed to create parent:" + parent);
                }
            }
        }
    }

}

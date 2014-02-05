package com.jivesoftware.os.jive.utils.chunk.store.filers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author jonathan.colt
 */
public class FileBackedMemMappedByteBufferFactory {

    private final File file;
    private final long size;

    public FileBackedMemMappedByteBufferFactory(File file, long size) {
        this.file = file;
        this.size = size;
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

    public ByteBuffer allocate() {
        try {
            ensureDirectory(file);
            MappedByteBuffer buf;
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.seek(size);
                raf.write(0);
                raf.seek(0);
                FileChannel channel = raf.getChannel();
                buf = channel.map(FileChannel.MapMode.READ_WRITE, 0, (int) channel.size());
            }
            return buf;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Exception ensureDirectory(File _file) {
        try {
            if (!_file.exists()) {
                File parent = _file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
            }
            return null;
        } catch (Exception x) {
            return x;
        }
    }

}

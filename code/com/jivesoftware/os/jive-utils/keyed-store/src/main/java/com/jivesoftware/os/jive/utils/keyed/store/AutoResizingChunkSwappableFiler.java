package com.jivesoftware.os.jive.utils.keyed.store;

import com.jivesoftware.os.jive.utils.chunk.store.ChunkStore;
import com.jivesoftware.os.jive.utils.io.Filer;
import com.jivesoftware.os.jive.utils.io.FilerIO;
import com.jivesoftware.os.jive.utils.map.store.FileBackMapStore;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 *
 */
public class AutoResizingChunkSwappableFiler implements SwappableFiler {

    private final AtomicReference<AutoResizingChunkFiler> filerReference;
    private final ChunkStore chunkStore;
    private final byte[] key;
    private final FileBackMapStore<byte[], byte[]> mapStore;
    private final FileBackMapStore<byte[], byte[]> swapStore;

    public AutoResizingChunkSwappableFiler(AutoResizingChunkFiler filer, ChunkStore chunkStore, byte[] key,
            FileBackMapStore<byte[], byte[]> mapStore, FileBackMapStore<byte[], byte[]> swapStore)
    {
        this.filerReference = new AtomicReference<>(filer);
        this.chunkStore = chunkStore;
        this.key = key;
        this.mapStore = mapStore;
        this.swapStore = swapStore;
    }

    public SwappingFiler swap(long initialChunkSize) throws Exception {
        AutoResizingChunkFiler filer = new AutoResizingChunkFiler(swapStore, key, chunkStore);
        filer.init(initialChunkSize);
        return new AutoResizingChunkSwappingFiler(filer);
    }

    @Override
    public Object lock() {
        return filerReference.get().lock();
    }

    @Override
    public void seek(long offset) throws IOException {
        filerReference.get().seek(offset);
    }

    @Override
    public long skip(long offset) throws IOException {
        return filerReference.get().skip(offset);
    }

    @Override
    public long length() throws IOException {
        return filerReference.get().length();
    }

    @Override
    public void setLength(long length) throws IOException {
        filerReference.get().setLength(length);
    }

    @Override
    public long getFilePointer() throws IOException {
        return filerReference.get().getFilePointer();
    }

    @Override
    public void eof() throws IOException {
        filerReference.get().eof();
    }

    @Override
    public void flush() throws IOException {
        filerReference.get().flush();
    }

    @Override
    public int read() throws IOException {
        return filerReference.get().read();
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        return filerReference.get().read(bytes);
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        return filerReference.get().read(bytes, offset, length);
    }

    @Override
    public void write(int i) throws IOException {
        filerReference.get().write(i);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        filerReference.get().write(bytes);
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        filerReference.get().write(bytes, offset, length);
    }

    @Override
    public void close() throws IOException {
        filerReference.get().close();
    }

    private class AutoResizingChunkSwappingFiler implements SwappingFiler {

        private final Filer filer;

        public AutoResizingChunkSwappingFiler(Filer filer) {
            this.filer = filer;
        }

        @Override
        public void commit() throws Exception {
            byte[] oldChunk = mapStore.get(key);
            byte[] newChunk = swapStore.get(key);
            mapStore.add(key, newChunk);
            swapStore.remove(key);
            chunkStore.remove(FilerIO.bytesLong(oldChunk));
        }

        @Override
        public Object lock() {
            return filer.lock();
        }

        @Override
        public void seek(long offset) throws IOException {
            filer.seek(offset);
        }

        @Override
        public long skip(long offset) throws IOException {
            return filer.skip(offset);
        }

        @Override
        public long length() throws IOException {
            return filer.length();
        }

        @Override
        public void setLength(long length) throws IOException {
            filer.setLength(length);
        }

        @Override
        public long getFilePointer() throws IOException {
            return filer.getFilePointer();
        }

        @Override
        public void eof() throws IOException {
            filer.eof();
        }

        @Override
        public void flush() throws IOException {
            filer.flush();
        }

        @Override
        public int read() throws IOException {
            return filer.read();
        }

        @Override
        public int read(byte[] bytes) throws IOException {
            return filer.read(bytes);
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            return filer.read(bytes, offset, length);
        }

        @Override
        public void write(int i) throws IOException {
            filer.write(i);
        }

        @Override
        public void write(byte[] bytes) throws IOException {
            filer.write(bytes);
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            filer.write(bytes, offset, length);
        }

        @Override
        public void close() throws IOException {
            filer.close();
        }
    }
}
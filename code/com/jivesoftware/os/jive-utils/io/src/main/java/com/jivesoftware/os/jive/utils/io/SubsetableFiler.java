package com.jivesoftware.os.jive.utils.io;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/*
 This class segments a single Filer into segment filers where
 each segment filer restates fp = 0. It only allows one segment filer
 at a time to be in control. It is the responsibility of the
 programmer to remove the segment filers as the become stale.

 @author jonathan.colt
 */
public class SubsetableFiler implements Filer {

    private final Filer filer;
    private final long startOfFP;
    private final long endOfFP;
    private final String key;
    private final ConcurrentHashMap<String, SubsetableFiler> subFilers = new ConcurrentHashMap<>();
    private final long count;

    public SubsetableFiler(Filer filer, long startOfFP, long endOfFP, long count) {
        this.filer = filer;
        this.startOfFP = startOfFP;
        this.endOfFP = endOfFP;
        this.key = startOfFP + ":" + endOfFP;
        this.count = count;
    }

    public SubsetableFiler get(long _startOfFP, long _endOfFP, long _count) {
        String subKey = _startOfFP + ":" + _endOfFP;
        SubsetableFiler subFiler = subFilers.get(subKey);
        if (subFiler == null) {
            subFiler = new SubsetableFiler(filer, _startOfFP, _endOfFP, _count);
            SubsetableFiler had = subFilers.putIfAbsent(subKey, subFiler);
            if (had != null) {
                subFiler = had;
            }
        }
        return subFiler;
    }

    @Override
    public Object lock() {
        return filer;
    }

    @Override
    public String toString() {
        return "SOF=" + startOfFP + " EOF:" + endOfFP + " Count=" + count;
    }

    final public long getSize() throws IOException {
        if (isFileBacked()) {
            return length();
        }
        return endOfFP - startOfFP;
    }

    final public boolean isFileBacked() {
        return (endOfFP == Long.MAX_VALUE);
    }

    final public long startOfFP() {
        return startOfFP;
    }

    final public long count() {
        return count;
    }

    final public long endOfFP() {
        return endOfFP;
    }

    @Override
    final public int read() throws IOException {
        return filer.read();
    }

    @Override
    final public int read(byte[] b) throws IOException {
        return filer.read(b);
    }

    @Override
    public synchronized int read(byte b[], int _offset, int _len) throws IOException {
        return filer.read(b, _offset, _len);
    }

    @Override
    final public void write(int b) throws IOException {
        filer.write(b);
    }

    @Override
    final public void write(byte[] b) throws IOException {
        filer.write(b);
    }

    @Override
    public synchronized void write(byte b[], int _offset, int _len) throws IOException {
        filer.write(b, _offset, _len);
    }

    @Override
    final public void seek(long position) throws IOException {
        synchronized (lock()) {
            if (position > endOfFP - startOfFP) {
                throw new IOException("seek overflow " + position + " " + this);
            }
            filer.seek(startOfFP + position);
        }
    }

    @Override
    final public long skip(long position) throws IOException {

        return filer.skip(position);
    }

    @Override
    final public long length() throws IOException {

        if (isFileBacked()) {
            return filer.length();
        }
        return endOfFP - startOfFP;
    }

    @Override
    final public void setLength(long len) throws IOException {

        if (isFileBacked()) {
            filer.setLength(len);
        } else {
            throw new IOException("try to modified a fixed length filer");
        }
    }

    @Override
    final public long getFilePointer() throws IOException {

        long fp = filer.getFilePointer();
        if (fp < startOfFP) {
            throw new IOException("seek misalignment " + fp + " < " + startOfFP);
        }
        if (fp > endOfFP) {
            throw new IOException("seek misalignment " + fp + " > " + endOfFP);
        }
        return fp - startOfFP;
    }

    @Override
    final public void eof() throws IOException {
        synchronized (lock()) {

            if (isFileBacked()) {
                filer.eof();
            } else {
                filer.seek(endOfFP - startOfFP);
            }
        }
    }

    @Override
    final public void close() throws IOException {

        if (isFileBacked()) {
            filer.close();
        }
    }

    @Override
    final public void flush() throws IOException {

        filer.flush();
    }

}

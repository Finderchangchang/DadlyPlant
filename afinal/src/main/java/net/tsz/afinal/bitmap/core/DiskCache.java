package net.tsz.afinal.bitmap.core;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.zip.Adler32;

public class DiskCache implements Closeable {
    private static final String TAG = DiskCache.class.getSimpleName();
    private static final int MAGIC_INDEX_FILE = -1289277392;
    private static final int MAGIC_DATA_FILE = -1121680112;
    private static final int IH_MAGIC = 0;
    private static final int IH_MAX_ENTRIES = 4;
    private static final int IH_MAX_BYTES = 8;
    private static final int IH_ACTIVE_REGION = 12;
    private static final int IH_ACTIVE_ENTRIES = 16;
    private static final int IH_ACTIVE_BYTES = 20;
    private static final int IH_VERSION = 24;
    private static final int IH_CHECKSUM = 28;
    private static final int INDEX_HEADER_SIZE = 32;
    private static final int DATA_HEADER_SIZE = 4;
    private static final int BH_KEY = 0;
    private static final int BH_CHECKSUM = 8;
    private static final int BH_OFFSET = 12;
    private static final int BH_LENGTH = 16;
    private static final int BLOB_HEADER_SIZE = 20;
    private RandomAccessFile mIndexFile;
    private RandomAccessFile mDataFile0;
    private RandomAccessFile mDataFile1;
    private FileChannel mIndexChannel;
    private MappedByteBuffer mIndexBuffer;
    private int mMaxEntries;
    private int mMaxBytes;
    private int mActiveRegion;
    private int mActiveEntries;
    private int mActiveBytes;
    private int mVersion;
    private RandomAccessFile mActiveDataFile;
    private RandomAccessFile mInactiveDataFile;
    private int mActiveHashStart;
    private int mInactiveHashStart;
    private byte[] mIndexHeader;
    private byte[] mBlobHeader;
    private Adler32 mAdler32;
    private String mPath;
    private DiskCache.LookupRequest mLookupRequest;
    private int mSlotOffset;
    private int mFileOffset;

    public DiskCache(String path, int maxEntries, int maxBytes, boolean reset) throws IOException {
        this(path, maxEntries, maxBytes, reset, 0);
    }

    public DiskCache(String path, int maxEntries, int maxBytes, boolean reset, int version) throws IOException {
        this.mIndexHeader = new byte[32];
        this.mBlobHeader = new byte[20];
        this.mAdler32 = new Adler32();
        this.mLookupRequest = new DiskCache.LookupRequest();
        File dir = new File(path);
        if(!dir.exists() && !dir.mkdirs()) {
            throw new IOException("unable to make dirs");
        } else {
            this.mPath = path;
            this.mIndexFile = new RandomAccessFile(path + ".idx", "rw");
            this.mDataFile0 = new RandomAccessFile(path + ".0", "rw");
            this.mDataFile1 = new RandomAccessFile(path + ".1", "rw");
            this.mVersion = version;
            if(reset || !this.loadIndex()) {
                this.resetCache(maxEntries, maxBytes);
                if(!this.loadIndex()) {
                    this.closeAll();
                    throw new IOException("unable to load index");
                }
            }
        }
    }

    public void delete() {
        deleteFileSilently(this.mPath + ".idx");
        deleteFileSilently(this.mPath + ".0");
        deleteFileSilently(this.mPath + ".1");
    }

    private static void deleteFileSilently(String path) {
        try {
            (new File(path)).delete();
        } catch (Throwable var2) {
            ;
        }

    }

    public void close() {
        this.syncAll();
        this.closeAll();
    }

    private void closeAll() {
        closeSilently(this.mIndexChannel);
        closeSilently(this.mIndexFile);
        closeSilently(this.mDataFile0);
        closeSilently(this.mDataFile1);
    }

    private boolean loadIndex() {
        try {
            this.mIndexFile.seek(0L);
            this.mDataFile0.seek(0L);
            this.mDataFile1.seek(0L);
            byte[] ex = this.mIndexHeader;
            if(this.mIndexFile.read(ex) != 32) {
                Log.w(TAG, "cannot read header");
                return false;
            } else if(readInt(ex, 0) != -1289277392) {
                Log.w(TAG, "cannot read header magic");
                return false;
            } else if(readInt(ex, 24) != this.mVersion) {
                Log.w(TAG, "version mismatch");
                return false;
            } else {
                this.mMaxEntries = readInt(ex, 4);
                this.mMaxBytes = readInt(ex, 8);
                this.mActiveRegion = readInt(ex, 12);
                this.mActiveEntries = readInt(ex, 16);
                this.mActiveBytes = readInt(ex, 20);
                int sum = readInt(ex, 28);
                if(this.checkSum(ex, 0, 28) != sum) {
                    Log.w(TAG, "header checksum does not match");
                    return false;
                } else if(this.mMaxEntries <= 0) {
                    Log.w(TAG, "invalid max entries");
                    return false;
                } else if(this.mMaxBytes <= 0) {
                    Log.w(TAG, "invalid max bytes");
                    return false;
                } else if(this.mActiveRegion != 0 && this.mActiveRegion != 1) {
                    Log.w(TAG, "invalid active region");
                    return false;
                } else if(this.mActiveEntries >= 0 && this.mActiveEntries <= this.mMaxEntries) {
                    if(this.mActiveBytes >= 4 && this.mActiveBytes <= this.mMaxBytes) {
                        if(this.mIndexFile.length() != (long)(32 + this.mMaxEntries * 12 * 2)) {
                            Log.w(TAG, "invalid index file length");
                            return false;
                        } else {
                            byte[] magic = new byte[4];
                            if(this.mDataFile0.read(magic) != 4) {
                                Log.w(TAG, "cannot read data file magic");
                                return false;
                            } else if(readInt(magic, 0) != -1121680112) {
                                Log.w(TAG, "invalid data file magic");
                                return false;
                            } else if(this.mDataFile1.read(magic) != 4) {
                                Log.w(TAG, "cannot read data file magic");
                                return false;
                            } else if(readInt(magic, 0) != -1121680112) {
                                Log.w(TAG, "invalid data file magic");
                                return false;
                            } else {
                                this.mIndexChannel = this.mIndexFile.getChannel();
                                this.mIndexBuffer = this.mIndexChannel.map(MapMode.READ_WRITE, 0L, this.mIndexFile.length());
                                this.mIndexBuffer.order(ByteOrder.LITTLE_ENDIAN);
                                this.setActiveVariables();
                                return true;
                            }
                        }
                    } else {
                        Log.w(TAG, "invalid active bytes");
                        return false;
                    }
                } else {
                    Log.w(TAG, "invalid active entries");
                    return false;
                }
            }
        } catch (IOException var4) {
            Log.e(TAG, "loadIndex failed.", var4);
            return false;
        }
    }

    private void setActiveVariables() throws IOException {
        this.mActiveDataFile = this.mActiveRegion == 0?this.mDataFile0:this.mDataFile1;
        this.mInactiveDataFile = this.mActiveRegion == 1?this.mDataFile0:this.mDataFile1;
        this.mActiveDataFile.setLength((long)this.mActiveBytes);
        this.mActiveDataFile.seek((long)this.mActiveBytes);
        this.mActiveHashStart = 32;
        this.mInactiveHashStart = 32;
        if(this.mActiveRegion == 0) {
            this.mInactiveHashStart += this.mMaxEntries * 12;
        } else {
            this.mActiveHashStart += this.mMaxEntries * 12;
        }

    }

    private void resetCache(int maxEntries, int maxBytes) throws IOException {
        this.mIndexFile.setLength(0L);
        this.mIndexFile.setLength((long)(32 + maxEntries * 12 * 2));
        this.mIndexFile.seek(0L);
        byte[] buf = this.mIndexHeader;
        writeInt(buf, 0, -1289277392);
        writeInt(buf, 4, maxEntries);
        writeInt(buf, 8, maxBytes);
        writeInt(buf, 12, 0);
        writeInt(buf, 16, 0);
        writeInt(buf, 20, 4);
        writeInt(buf, 24, this.mVersion);
        writeInt(buf, 28, this.checkSum(buf, 0, 28));
        this.mIndexFile.write(buf);
        this.mDataFile0.setLength(0L);
        this.mDataFile1.setLength(0L);
        this.mDataFile0.seek(0L);
        this.mDataFile1.seek(0L);
        writeInt(buf, 0, -1121680112);
        this.mDataFile0.write(buf, 0, 4);
        this.mDataFile1.write(buf, 0, 4);
    }

    private void flipRegion() throws IOException {
        this.mActiveRegion = 1 - this.mActiveRegion;
        this.mActiveEntries = 0;
        this.mActiveBytes = 4;
        writeInt(this.mIndexHeader, 12, this.mActiveRegion);
        writeInt(this.mIndexHeader, 16, this.mActiveEntries);
        writeInt(this.mIndexHeader, 20, this.mActiveBytes);
        this.updateIndexHeader();
        this.setActiveVariables();
        this.clearHash(this.mActiveHashStart);
        this.syncIndex();
    }

    private void updateIndexHeader() {
        writeInt(this.mIndexHeader, 28, this.checkSum(this.mIndexHeader, 0, 28));
        this.mIndexBuffer.position(0);
        this.mIndexBuffer.put(this.mIndexHeader);
    }

    private void clearHash(int hashStart) {
        byte[] zero = new byte[1024];
        this.mIndexBuffer.position(hashStart);

        int todo;
        for(int count = this.mMaxEntries * 12; count > 0; count -= todo) {
            todo = Math.min(count, 1024);
            this.mIndexBuffer.put(zero, 0, todo);
        }

    }

    public void insert(long key, byte[] data) throws IOException {
        if(24 + data.length > this.mMaxBytes) {
            throw new RuntimeException("blob is too large!");
        } else {
            if(this.mActiveBytes + 20 + data.length > this.mMaxBytes || this.mActiveEntries * 2 >= this.mMaxEntries) {
                this.flipRegion();
            }

            if(!this.lookupInternal(key, this.mActiveHashStart)) {
                ++this.mActiveEntries;
                writeInt(this.mIndexHeader, 16, this.mActiveEntries);
            }

            this.insertInternal(key, data, data.length);
            this.updateIndexHeader();
        }
    }

    private void insertInternal(long key, byte[] data, int length) throws IOException {
        byte[] header = this.mBlobHeader;
        int sum = this.checkSum(data);
        writeLong(header, 0, key);
        writeInt(header, 8, sum);
        writeInt(header, 12, this.mActiveBytes);
        writeInt(header, 16, length);
        this.mActiveDataFile.write(header);
        this.mActiveDataFile.write(data, 0, length);
        this.mIndexBuffer.putLong(this.mSlotOffset, key);
        this.mIndexBuffer.putInt(this.mSlotOffset + 8, this.mActiveBytes);
        this.mActiveBytes += 20 + length;
        writeInt(this.mIndexHeader, 20, this.mActiveBytes);
    }

    public byte[] lookup(long key) throws IOException {
        this.mLookupRequest.key = key;
        this.mLookupRequest.buffer = null;
        return this.lookup(this.mLookupRequest)?this.mLookupRequest.buffer:null;
    }

    public boolean lookup(DiskCache.LookupRequest req) throws IOException {
        if(this.lookupInternal(req.key, this.mActiveHashStart) && this.getBlob(this.mActiveDataFile, this.mFileOffset, req)) {
            return true;
        } else {
            int insertOffset = this.mSlotOffset;
            if(this.lookupInternal(req.key, this.mInactiveHashStart) && this.getBlob(this.mInactiveDataFile, this.mFileOffset, req)) {
                if(this.mActiveBytes + 20 + req.length <= this.mMaxBytes && this.mActiveEntries * 2 < this.mMaxEntries) {
                    this.mSlotOffset = insertOffset;

                    try {
                        this.insertInternal(req.key, req.buffer, req.length);
                        ++this.mActiveEntries;
                        writeInt(this.mIndexHeader, 16, this.mActiveEntries);
                        this.updateIndexHeader();
                    } catch (Throwable var4) {
                        Log.e(TAG, "cannot copy over");
                    }

                    return true;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    private boolean getBlob(RandomAccessFile file, int offset, DiskCache.LookupRequest req) throws IOException {
        byte[] header = this.mBlobHeader;
        long oldPosition = file.getFilePointer();

        try {
            file.seek((long)offset);
            if(file.read(header) != 20) {
                Log.w(TAG, "cannot read blob header");
                return false;
            } else {
                long t = readLong(header, 0);
                if(t != req.key) {
                    Log.w(TAG, "blob key does not match: " + t);
                    return false;
                } else {
                    int sum = readInt(header, 8);
                    int blobOffset = readInt(header, 12);
                    if(blobOffset != offset) {
                        Log.w(TAG, "blob offset does not match: " + blobOffset);
                        return false;
                    } else {
                        int length = readInt(header, 16);
                        if(length >= 0 && length <= this.mMaxBytes - offset - 20) {
                            if(req.buffer == null || req.buffer.length < length) {
                                req.buffer = new byte[length];
                            }

                            byte[] blob = req.buffer;
                            req.length = length;
                            if(file.read(blob, 0, length) != length) {
                                Log.w(TAG, "cannot read blob data");
                                return false;
                            } else if(this.checkSum(blob, 0, length) == sum) {
                                return true;
                            } else {
                                Log.w(TAG, "blob checksum does not match: " + sum);
                                return false;
                            }
                        } else {
                            Log.w(TAG, "invalid blob length: " + length);
                            return false;
                        }
                    }
                }
            }
        } catch (Throwable var16) {
            Log.e(TAG, "getBlob failed.", var16);
            return false;
        } finally {
            file.seek(oldPosition);
        }
    }

    private boolean lookupInternal(long key, int hashStart) {
        int slot = (int)(key % (long)this.mMaxEntries);
        if(slot < 0) {
            slot += this.mMaxEntries;
        }

        int slotBegin = slot;

        while(true) {
            int offset = hashStart + slot * 12;
            long candidateKey = this.mIndexBuffer.getLong(offset);
            int candidateOffset = this.mIndexBuffer.getInt(offset + 8);
            if(candidateOffset == 0) {
                this.mSlotOffset = offset;
                return false;
            }

            if(candidateKey == key) {
                this.mSlotOffset = offset;
                this.mFileOffset = candidateOffset;
                return true;
            }

            ++slot;
            if(slot >= this.mMaxEntries) {
                slot = 0;
            }

            if(slot == slotBegin) {
                Log.w(TAG, "corrupted index: clear the slot.");
                this.mIndexBuffer.putInt(hashStart + slot * 12 + 8, 0);
            }
        }
    }

    public void syncIndex() {
        try {
            this.mIndexBuffer.force();
        } catch (Throwable var2) {
            Log.w(TAG, "sync index failed", var2);
        }

    }

    public void syncAll() {
        this.syncIndex();

        try {
            this.mDataFile0.getFD().sync();
        } catch (Throwable var3) {
            Log.w(TAG, "sync data file 0 failed", var3);
        }

        try {
            this.mDataFile1.getFD().sync();
        } catch (Throwable var2) {
            Log.w(TAG, "sync data file 1 failed", var2);
        }

    }

    int getActiveCount() {
        int count = 0;

        for(int i = 0; i < this.mMaxEntries; ++i) {
            int offset = this.mActiveHashStart + i * 12;
            int candidateOffset = this.mIndexBuffer.getInt(offset + 8);
            if(candidateOffset != 0) {
                ++count;
            }
        }

        if(count == this.mActiveEntries) {
            return count;
        } else {
            Log.e(TAG, "wrong active count: " + this.mActiveEntries + " vs " + count);
            return -1;
        }
    }

    int checkSum(byte[] data) {
        this.mAdler32.reset();
        this.mAdler32.update(data);
        return (int)this.mAdler32.getValue();
    }

    int checkSum(byte[] data, int offset, int nbytes) {
        this.mAdler32.reset();
        this.mAdler32.update(data, offset, nbytes);
        return (int)this.mAdler32.getValue();
    }

    static void closeSilently(Closeable c) {
        if(c != null) {
            try {
                c.close();
            } catch (Throwable var2) {
                ;
            }

        }
    }

    static int readInt(byte[] buf, int offset) {
        return buf[offset] & 255 | (buf[offset + 1] & 255) << 8 | (buf[offset + 2] & 255) << 16 | (buf[offset + 3] & 255) << 24;
    }

    static long readLong(byte[] buf, int offset) {
        long result = (long)(buf[offset + 7] & 255);

        for(int i = 6; i >= 0; --i) {
            result = result << 8 | (long)(buf[offset + i] & 255);
        }

        return result;
    }

    static void writeInt(byte[] buf, int offset, int value) {
        for(int i = 0; i < 4; ++i) {
            buf[offset + i] = (byte)(value & 255);
            value >>= 8;
        }

    }

    static void writeLong(byte[] buf, int offset, long value) {
        for(int i = 0; i < 8; ++i) {
            buf[offset + i] = (byte)((int)(value & 255L));
            value >>= 8;
        }

    }

    public static class LookupRequest {
        public long key;
        public byte[] buffer;
        public int length;

        public LookupRequest() {
        }
    }
}


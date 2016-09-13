package net.tsz.afinal.bitmap.core;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import java.util.ArrayList;

public class BytesBufferPool {
    private final int mPoolSize;
    private final int mBufferSize;
    private final ArrayList<BytesBufferPool.BytesBuffer> mList;

    public BytesBufferPool(int poolSize, int bufferSize) {
        this.mList = new ArrayList(poolSize);
        this.mPoolSize = poolSize;
        this.mBufferSize = bufferSize;
    }

    public synchronized BytesBufferPool.BytesBuffer get() {
        int n = this.mList.size();
        return n > 0?(BytesBufferPool.BytesBuffer)this.mList.remove(n - 1):new BytesBufferPool.BytesBuffer(this.mBufferSize, (BytesBufferPool.BytesBuffer)null);
    }

    public synchronized void recycle(BytesBufferPool.BytesBuffer buffer) {
        if(buffer.data.length == this.mBufferSize) {
            if(this.mList.size() < this.mPoolSize) {
                buffer.offset = 0;
                buffer.length = 0;
                this.mList.add(buffer);
            }

        }
    }

    public synchronized void clear() {
        this.mList.clear();
    }

    public static class BytesBuffer {
        public byte[] data;
        public int offset;
        public int length;

        private BytesBuffer(int capacity, BytesBuffer bytesBuffer) {
            this.data = new byte[capacity];
        }
    }
}


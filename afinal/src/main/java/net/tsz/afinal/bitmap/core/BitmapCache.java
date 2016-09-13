package net.tsz.afinal.bitmap.core;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.tsz.afinal.bitmap.core.BytesBufferPool.BytesBuffer;
import net.tsz.afinal.bitmap.core.DiskCache.LookupRequest;
import net.tsz.afinal.utils.BitmapUtils;

public class BitmapCache {
    private static final int DEFAULT_MEM_CACHE_SIZE = 8388608;
    private static final int DEFAULT_DISK_CACHE_SIZE = 52428800;
    private static final int DEFAULT_DISK_CACHE_COUNT = 10000;
    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
    private DiskCache mDiskCache;
    private IMemoryCache mMemoryCache;
    private BitmapCache.ImageCacheParams mCacheParams;

    public BitmapCache(BitmapCache.ImageCacheParams cacheParams) {
        this.init(cacheParams);
    }

    private void init(BitmapCache.ImageCacheParams cacheParams) {
        this.mCacheParams = cacheParams;
        if(this.mCacheParams.memoryCacheEnabled) {
            if(this.mCacheParams.recycleImmediately) {
                this.mMemoryCache = new SoftMemoryCacheImpl(this.mCacheParams.memCacheSize);
            } else {
                this.mMemoryCache = new BaseMemoryCacheImpl(this.mCacheParams.memCacheSize);
            }
        }

        if(cacheParams.diskCacheEnabled) {
            try {
                String path = this.mCacheParams.diskCacheDir.getAbsolutePath();
                this.mDiskCache = new DiskCache(path, this.mCacheParams.diskCacheCount, this.mCacheParams.diskCacheSize, false);
            } catch (IOException var3) {
                ;
            }
        }

    }

    public void addToMemoryCache(String url, Bitmap bitmap) {
        if(url != null && bitmap != null) {
            this.mMemoryCache.put(url, bitmap);
        }
    }

    public void addToDiskCache(String url, byte[] data) {
        if(this.mDiskCache != null && url != null && data != null) {
            byte[] key = BitmapUtils.makeKey(url);
            long cacheKey = BitmapUtils.crc64Long(key);
            ByteBuffer buffer = ByteBuffer.allocate(key.length + data.length);
            buffer.put(key);
            buffer.put(data);
            DiskCache var7 = this.mDiskCache;
            synchronized(this.mDiskCache) {
                try {
                    this.mDiskCache.insert(cacheKey, buffer.array());
                } catch (IOException var9) {
                    ;
                }

            }
        }
    }

    public boolean getImageData(String url, BytesBuffer buffer) {
        if(this.mDiskCache == null) {
            return false;
        } else {
            byte[] key = BitmapUtils.makeKey(url);
            long cacheKey = BitmapUtils.crc64Long(key);

            try {
                LookupRequest request = new LookupRequest();
                request.key = cacheKey;
                request.buffer = buffer.data;
                DiskCache var7 = this.mDiskCache;
                synchronized(this.mDiskCache) {
                    if(!this.mDiskCache.lookup(request)) {
                        return false;
                    }
                }

                if(BitmapUtils.isSameKey(key, request.buffer)) {
                    buffer.data = request.buffer;
                    buffer.offset = key.length;
                    buffer.length = request.length - buffer.offset;
                    return true;
                }
            } catch (IOException var9) {
                ;
            }

            return false;
        }
    }

    public Bitmap getBitmapFromMemoryCache(String data) {
        return this.mMemoryCache != null?this.mMemoryCache.get(data):null;
    }

    public void clearCache() {
        this.clearMemoryCache();
        this.clearDiskCache();
    }

    public void clearDiskCache() {
        if(this.mDiskCache != null) {
            this.mDiskCache.delete();
        }

    }

    public void clearMemoryCache() {
        if(this.mMemoryCache != null) {
            this.mMemoryCache.evictAll();
        }

    }

    public void clearCache(String key) {
        this.clearMemoryCache(key);
        this.clearDiskCache(key);
    }

    public void clearDiskCache(String url) {
        this.addToDiskCache(url, new byte[0]);
    }

    public void clearMemoryCache(String key) {
        if(this.mMemoryCache != null) {
            this.mMemoryCache.remove(key);
        }

    }

    public void close() {
        if(this.mDiskCache != null) {
            this.mDiskCache.close();
        }

    }

    public static class ImageCacheParams {
        public int memCacheSize = 8388608;
        public int diskCacheSize = 52428800;
        public int diskCacheCount = 10000;
        public File diskCacheDir;
        public boolean memoryCacheEnabled = true;
        public boolean diskCacheEnabled = true;
        public boolean recycleImmediately = true;

        public ImageCacheParams(File diskCacheDir) {
            this.diskCacheDir = diskCacheDir;
        }

        public ImageCacheParams(String diskCacheDir) {
            this.diskCacheDir = new File(diskCacheDir);
        }

        public void setMemCacheSizePercent(Context context, float percent) {
            if(percent >= 0.05F && percent <= 0.8F) {
                this.memCacheSize = Math.round(percent * (float)getMemoryClass(context) * 1024.0F * 1024.0F);
            } else {
                throw new IllegalArgumentException("setMemCacheSizePercent - percent must be between 0.05 and 0.8 (inclusive)");
            }
        }

        public void setMemCacheSize(int memCacheSize) {
            this.memCacheSize = memCacheSize;
        }

        public void setDiskCacheSize(int diskCacheSize) {
            this.diskCacheSize = diskCacheSize;
        }

        private static int getMemoryClass(Context context) {
            return ((ActivityManager)context.getSystemService("activity")).getMemoryClass();
        }

        public void setDiskCacheCount(int diskCacheCount) {
            this.diskCacheCount = diskCacheCount;
        }

        public void setRecycleImmediately(boolean recycleImmediately) {
            this.recycleImmediately = recycleImmediately;
        }
    }
}


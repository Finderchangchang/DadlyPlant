package net.tsz.afinal.bitmap.core;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.graphics.Bitmap;

import net.tsz.afinal.utils.BitmapUtils;

public class BaseMemoryCacheImpl implements IMemoryCache {
    private final LruMemoryCache<String, Bitmap> mMemoryCache;

    public BaseMemoryCacheImpl(final int size) {
        this.mMemoryCache = new LruMemoryCache(size) {
            protected int sizeOf(String key, Bitmap bitmap) {
                return BitmapUtils.getBitmapSize(bitmap);
            }
        };
    }

    public void put(String key, Bitmap bitmap) {
        this.mMemoryCache.put(key, bitmap);
    }

    public Bitmap get(String key) {
        return (Bitmap)this.mMemoryCache.get(key);
    }

    public void evictAll() {
        this.mMemoryCache.evictAll();
    }

    public void remove(String key) {
        this.mMemoryCache.remove(key);
    }
}

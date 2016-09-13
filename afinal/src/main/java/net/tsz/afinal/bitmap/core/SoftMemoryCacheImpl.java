package net.tsz.afinal.bitmap.core;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.graphics.Bitmap;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import net.tsz.afinal.bitmap.core.IMemoryCache;

public class SoftMemoryCacheImpl implements IMemoryCache {
    private final HashMap<String, SoftReference<Bitmap>> mMemoryCache = new HashMap();

    public SoftMemoryCacheImpl(int size) {
    }

    public void put(String key, Bitmap bitmap) {
        this.mMemoryCache.put(key, new SoftReference(bitmap));
    }

    public Bitmap get(String key) {
        SoftReference memBitmap = (SoftReference)this.mMemoryCache.get(key);
        return memBitmap != null?(Bitmap)memBitmap.get():null;
    }

    public void evictAll() {
        this.mMemoryCache.clear();
    }

    public void remove(String key) {
        this.mMemoryCache.remove(key);
    }
}


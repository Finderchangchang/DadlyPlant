package net.tsz.afinal.bitmap.core;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.graphics.Bitmap;

public interface IMemoryCache {
    void put(String var1, Bitmap var2);

    Bitmap get(String var1);

    void evictAll();

    void remove(String var1);
}

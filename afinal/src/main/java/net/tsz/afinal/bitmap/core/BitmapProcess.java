package net.tsz.afinal.bitmap.core;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import net.tsz.afinal.bitmap.core.BitmapCache;
import net.tsz.afinal.bitmap.core.BitmapDecoder;
import net.tsz.afinal.bitmap.core.BitmapDisplayConfig;
import net.tsz.afinal.bitmap.core.BytesBufferPool;
import net.tsz.afinal.bitmap.core.BytesBufferPool.BytesBuffer;
import net.tsz.afinal.bitmap.download.Downloader;

public class BitmapProcess {
    private Downloader mDownloader;
    private BitmapCache mCache;
    private static final int BYTESBUFFE_POOL_SIZE = 4;
    private static final int BYTESBUFFER_SIZE = 204800;
    private static final BytesBufferPool sMicroThumbBufferPool = new BytesBufferPool(4, 204800);

    public BitmapProcess(Downloader downloader, BitmapCache cache) {
        this.mDownloader = downloader;
        this.mCache = cache;
    }

    public Bitmap getBitmap(String url, BitmapDisplayConfig config) {
        Bitmap bitmap = this.getFromDisk(url, config);
        if(bitmap == null) {
            byte[] data = this.mDownloader.download(url);
            if(data != null && data.length > 0) {
                if(config == null) {
                    return BitmapFactory.decodeByteArray(data, 0, data.length);
                }

                bitmap = BitmapDecoder.decodeSampledBitmapFromByteArray(data, 0, data.length, config.getBitmapWidth(), config.getBitmapHeight());
                this.mCache.addToDiskCache(url, data);
            }
        }

        return bitmap;
    }

    public Bitmap getFromDisk(String key, BitmapDisplayConfig config) {
        BytesBuffer buffer = sMicroThumbBufferPool.get();
        Bitmap b = null;

        try {
            boolean found = this.mCache.getImageData(key, buffer);
            if(found && buffer.length - buffer.offset > 0) {
                if(config != null) {
                    b = BitmapDecoder.decodeSampledBitmapFromByteArray(buffer.data, buffer.offset, buffer.length, config.getBitmapWidth(), config.getBitmapHeight());
                } else {
                    b = BitmapFactory.decodeByteArray(buffer.data, buffer.offset, buffer.length);
                }
            }
        } finally {
            sMicroThumbBufferPool.recycle(buffer);
        }

        return b;
    }
}


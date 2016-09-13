package net.tsz.afinal;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import net.tsz.afinal.bitmap.core.BitmapCache;
import net.tsz.afinal.bitmap.core.BitmapDisplayConfig;
import net.tsz.afinal.bitmap.core.BitmapProcess;
import net.tsz.afinal.bitmap.core.BitmapCache.ImageCacheParams;
import net.tsz.afinal.bitmap.display.Displayer;
import net.tsz.afinal.bitmap.display.SimpleDisplayer;
import net.tsz.afinal.bitmap.download.Downloader;
import net.tsz.afinal.bitmap.download.SimpleDownloader;
import net.tsz.afinal.core.AsyncTask;
import net.tsz.afinal.utils.BitmapUtils;

public class FinalBitmap {
    private FinalBitmap.FinalBitmapConfig mConfig;
    private BitmapCache mImageCache;
    private BitmapProcess mBitmapProcess;
    private boolean mExitTasksEarly = false;
    private boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();
    private Context mContext;
    private boolean mInit = false;
    private ExecutorService bitmapLoadAndDisplayExecutor;
    private static FinalBitmap mFinalBitmap;
    private HashMap<String, BitmapDisplayConfig> configMap = new HashMap();

    private FinalBitmap(Context context) {
        this.mContext = context;
        this.mConfig = new FinalBitmap.FinalBitmapConfig(context);
        this.configDiskCachePath(BitmapUtils.getDiskCacheDir(context, "afinalCache").getAbsolutePath());
        this.configDisplayer(new SimpleDisplayer());
        this.configDownlader(new SimpleDownloader());
    }

    public static synchronized FinalBitmap create(Context ctx) {
        if(mFinalBitmap == null) {
            mFinalBitmap = new FinalBitmap(ctx.getApplicationContext());
        }

        return mFinalBitmap;
    }

    public FinalBitmap configLoadingImage(Bitmap bitmap) {
        this.mConfig.defaultDisplayConfig.setLoadingBitmap(bitmap);
        return this;
    }

    public FinalBitmap configLoadingImage(int resId) {
        this.mConfig.defaultDisplayConfig.setLoadingBitmap(BitmapFactory.decodeResource(this.mContext.getResources(), resId));
        return this;
    }

    public FinalBitmap configLoadfailImage(Bitmap bitmap) {
        this.mConfig.defaultDisplayConfig.setLoadfailBitmap(bitmap);
        return this;
    }

    public FinalBitmap configLoadfailImage(int resId) {
        this.mConfig.defaultDisplayConfig.setLoadfailBitmap(BitmapFactory.decodeResource(this.mContext.getResources(), resId));
        return this;
    }

    public FinalBitmap configBitmapMaxHeight(int bitmapHeight) {
        this.mConfig.defaultDisplayConfig.setBitmapHeight(bitmapHeight);
        return this;
    }

    public FinalBitmap configBitmapMaxWidth(int bitmapWidth) {
        this.mConfig.defaultDisplayConfig.setBitmapWidth(bitmapWidth);
        return this;
    }

    public FinalBitmap configDownlader(Downloader downlader) {
        this.mConfig.downloader = downlader;
        return this;
    }

    public FinalBitmap configDisplayer(Displayer displayer) {
        this.mConfig.displayer = displayer;
        return this;
    }

    public FinalBitmap configDiskCachePath(String strPath) {
        if(!TextUtils.isEmpty(strPath)) {
            this.mConfig.cachePath = strPath;
        }

        return this;
    }

    public FinalBitmap configMemoryCacheSize(int size) {
        this.mConfig.memCacheSize = size;
        return this;
    }

    public FinalBitmap configMemoryCachePercent(float percent) {
        this.mConfig.memCacheSizePercent = percent;
        return this;
    }

    public FinalBitmap configDiskCacheSize(int size) {
        this.mConfig.diskCacheSize = size;
        return this;
    }

    public FinalBitmap configBitmapLoadThreadSize(int size) {
        if(size >= 1) {
            this.mConfig.poolSize = size;
        }

        return this;
    }

    public FinalBitmap configRecycleImmediately(boolean recycleImmediately) {
        this.mConfig.recycleImmediately = recycleImmediately;
        return this;
    }

    private FinalBitmap init() {
        if(!this.mInit) {
            ImageCacheParams imageCacheParams = new ImageCacheParams(this.mConfig.cachePath);
            if((double)this.mConfig.memCacheSizePercent > 0.05D && (double)this.mConfig.memCacheSizePercent < 0.8D) {
                imageCacheParams.setMemCacheSizePercent(this.mContext, this.mConfig.memCacheSizePercent);
            } else if(this.mConfig.memCacheSize > 2097152) {
                imageCacheParams.setMemCacheSize(this.mConfig.memCacheSize);
            } else {
                imageCacheParams.setMemCacheSizePercent(this.mContext, 0.3F);
            }

            if(this.mConfig.diskCacheSize > 5242880) {
                imageCacheParams.setDiskCacheSize(this.mConfig.diskCacheSize);
            }

            imageCacheParams.setRecycleImmediately(this.mConfig.recycleImmediately);
            this.mImageCache = new BitmapCache(imageCacheParams);
            this.bitmapLoadAndDisplayExecutor = Executors.newFixedThreadPool(this.mConfig.poolSize, new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setPriority(4);
                    return t;
                }
            });
            this.mBitmapProcess = new BitmapProcess(this.mConfig.downloader, this.mImageCache);
            this.mInit = true;
        }

        return this;
    }

    public void display(View imageView, String uri) {
        this.doDisplay(imageView, uri, (BitmapDisplayConfig)null);
    }

    public void display(View imageView, String uri, int imageWidth, int imageHeight) {
        BitmapDisplayConfig displayConfig = (BitmapDisplayConfig)this.configMap.get(imageWidth + "_" + imageHeight);
        if(displayConfig == null) {
            displayConfig = this.getDisplayConfig();
            displayConfig.setBitmapHeight(imageHeight);
            displayConfig.setBitmapWidth(imageWidth);
            this.configMap.put(imageWidth + "_" + imageHeight, displayConfig);
        }

        this.doDisplay(imageView, uri, displayConfig);
    }

    public void display(View imageView, String uri, Bitmap loadingBitmap) {
        BitmapDisplayConfig displayConfig = (BitmapDisplayConfig)this.configMap.get(String.valueOf(loadingBitmap));
        if(displayConfig == null) {
            displayConfig = this.getDisplayConfig();
            displayConfig.setLoadingBitmap(loadingBitmap);
            this.configMap.put(String.valueOf(loadingBitmap), displayConfig);
        }

        this.doDisplay(imageView, uri, displayConfig);
    }

    public void display(View imageView, String uri, Bitmap loadingBitmap, Bitmap laodfailBitmap) {
        BitmapDisplayConfig displayConfig = (BitmapDisplayConfig)this.configMap.get(String.valueOf(loadingBitmap) + "_" + laodfailBitmap);
        if(displayConfig == null) {
            displayConfig = this.getDisplayConfig();
            displayConfig.setLoadingBitmap(loadingBitmap);
            displayConfig.setLoadfailBitmap(laodfailBitmap);
            this.configMap.put(String.valueOf(loadingBitmap) + "_" + laodfailBitmap, displayConfig);
        }

        this.doDisplay(imageView, uri, displayConfig);
    }

    public void display(View imageView, String uri, int imageWidth, int imageHeight, Bitmap loadingBitmap, Bitmap laodfailBitmap) {
        BitmapDisplayConfig displayConfig = (BitmapDisplayConfig)this.configMap.get(imageWidth + "_" + imageHeight + "_" + loadingBitmap + "_" + laodfailBitmap);
        if(displayConfig == null) {
            displayConfig = this.getDisplayConfig();
            displayConfig.setBitmapHeight(imageHeight);
            displayConfig.setBitmapWidth(imageWidth);
            displayConfig.setLoadingBitmap(loadingBitmap);
            displayConfig.setLoadfailBitmap(laodfailBitmap);
            this.configMap.put(imageWidth + "_" + imageHeight + "_" + loadingBitmap + "_" + laodfailBitmap, displayConfig);
        }

        this.doDisplay(imageView, uri, displayConfig);
    }

    public void display(View imageView, String uri, BitmapDisplayConfig config) {
        this.doDisplay(imageView, uri, config);
    }

    private void doDisplay(View imageView, String uri, BitmapDisplayConfig displayConfig) {
        if(!this.mInit) {
            this.init();
        }

        if(!TextUtils.isEmpty(uri) && imageView != null) {
            if(displayConfig == null) {
                displayConfig = this.mConfig.defaultDisplayConfig;
            }

            Bitmap bitmap = null;
            if(this.mImageCache != null) {
                bitmap = this.mImageCache.getBitmapFromMemoryCache(uri);
            }

            if(bitmap != null) {
                if(imageView instanceof ImageView) {
                    ((ImageView)imageView).setImageBitmap(bitmap);
                } else {
                    imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
                }
            } else if(checkImageTask(uri, imageView)) {
                FinalBitmap.BitmapLoadAndDisplayTask task = new FinalBitmap.BitmapLoadAndDisplayTask(imageView, displayConfig);
                FinalBitmap.AsyncDrawable asyncDrawable = new FinalBitmap.AsyncDrawable(this.mContext.getResources(), displayConfig.getLoadingBitmap(), task);
                if(imageView instanceof ImageView) {
                    ((ImageView)imageView).setImageDrawable(asyncDrawable);
                } else {
                    imageView.setBackgroundDrawable(asyncDrawable);
                }

                task.executeOnExecutor(this.bitmapLoadAndDisplayExecutor, new Object[]{uri});
            }

        }
    }

    private BitmapDisplayConfig getDisplayConfig() {
        BitmapDisplayConfig config = new BitmapDisplayConfig();
        config.setAnimation(this.mConfig.defaultDisplayConfig.getAnimation());
        config.setAnimationType(this.mConfig.defaultDisplayConfig.getAnimationType());
        config.setBitmapHeight(this.mConfig.defaultDisplayConfig.getBitmapHeight());
        config.setBitmapWidth(this.mConfig.defaultDisplayConfig.getBitmapWidth());
        config.setLoadfailBitmap(this.mConfig.defaultDisplayConfig.getLoadfailBitmap());
        config.setLoadingBitmap(this.mConfig.defaultDisplayConfig.getLoadingBitmap());
        return config;
    }

    private void clearCacheInternalInBackgroud() {
        if(this.mImageCache != null) {
            this.mImageCache.clearCache();
        }

    }

    private void clearDiskCacheInBackgroud() {
        if(this.mImageCache != null) {
            this.mImageCache.clearDiskCache();
        }

    }

    private void clearCacheInBackgroud(String key) {
        if(this.mImageCache != null) {
            this.mImageCache.clearCache(key);
        }

    }

    private void clearDiskCacheInBackgroud(String key) {
        if(this.mImageCache != null) {
            this.mImageCache.clearDiskCache(key);
        }

    }

    private void closeCacheInternalInBackgroud() {
        if(this.mImageCache != null) {
            this.mImageCache.close();
            this.mImageCache = null;
            mFinalBitmap = null;
        }

    }

    private Bitmap processBitmap(String uri, BitmapDisplayConfig config) {
        return this.mBitmapProcess != null?this.mBitmapProcess.getBitmap(uri, config):null;
    }

    public Bitmap getBitmapFromCache(String key) {
        Bitmap bitmap = this.getBitmapFromMemoryCache(key);
        if(bitmap == null) {
            bitmap = this.getBitmapFromDiskCache(key);
        }

        return bitmap;
    }

    public Bitmap getBitmapFromMemoryCache(String key) {
        return this.mImageCache.getBitmapFromMemoryCache(key);
    }

    public Bitmap getBitmapFromDiskCache(String key) {
        return this.getBitmapFromDiskCache(key, (BitmapDisplayConfig)null);
    }

    public Bitmap getBitmapFromDiskCache(String key, BitmapDisplayConfig config) {
        return this.mBitmapProcess.getFromDisk(key, config);
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        this.mExitTasksEarly = exitTasksEarly;
    }

    public void onResume() {
        this.setExitTasksEarly(false);
    }

    public void onPause() {
        this.setExitTasksEarly(true);
    }

    public void onDestroy() {
        this.closeCache();
    }

    public void clearCache() {
        (new FinalBitmap.CacheExecutecTask((FinalBitmap.CacheExecutecTask)null)).execute(new Object[]{Integer.valueOf(1)});
    }

    public void clearCache(String key) {
        (new FinalBitmap.CacheExecutecTask((FinalBitmap.CacheExecutecTask)null)).execute(new Object[]{Integer.valueOf(4), key});
    }

    public void clearMemoryCache() {
        if(this.mImageCache != null) {
            this.mImageCache.clearMemoryCache();
        }

    }

    public void clearMemoryCache(String key) {
        if(this.mImageCache != null) {
            this.mImageCache.clearMemoryCache(key);
        }

    }

    public void clearDiskCache() {
        (new FinalBitmap.CacheExecutecTask((FinalBitmap.CacheExecutecTask)null)).execute(new Object[]{Integer.valueOf(3)});
    }

    public void clearDiskCache(String key) {
        (new FinalBitmap.CacheExecutecTask((FinalBitmap.CacheExecutecTask)null)).execute(new Object[]{Integer.valueOf(5), key});
    }

    public void closeCache() {
        (new FinalBitmap.CacheExecutecTask((FinalBitmap.CacheExecutecTask)null)).execute(new Object[]{Integer.valueOf(2)});
    }

    public void exitTasksEarly(boolean exitTasksEarly) {
        this.mExitTasksEarly = exitTasksEarly;
        if(exitTasksEarly) {
            this.pauseWork(false);
        }

    }

    public void pauseWork(boolean pauseWork) {
        Object var2 = this.mPauseWorkLock;
        synchronized(this.mPauseWorkLock) {
            this.mPauseWork = pauseWork;
            if(!this.mPauseWork) {
                this.mPauseWorkLock.notifyAll();
            }

        }
    }

    private static FinalBitmap.BitmapLoadAndDisplayTask getBitmapTaskFromImageView(View imageView) {
        if(imageView != null) {
            Drawable drawable = null;
            if(imageView instanceof ImageView) {
                drawable = ((ImageView)imageView).getDrawable();
            } else {
                drawable = imageView.getBackground();
            }

            if(drawable instanceof FinalBitmap.AsyncDrawable) {
                FinalBitmap.AsyncDrawable asyncDrawable = (FinalBitmap.AsyncDrawable)drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }

        return null;
    }

    public static boolean checkImageTask(Object data, View imageView) {
        FinalBitmap.BitmapLoadAndDisplayTask bitmapWorkerTask = getBitmapTaskFromImageView(imageView);
        if(bitmapWorkerTask != null) {
            Object bitmapData = bitmapWorkerTask.data;
            if(bitmapData != null && bitmapData.equals(data)) {
                return false;
            }

            bitmapWorkerTask.cancel(true);
        }

        return true;
    }

    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<FinalBitmap.BitmapLoadAndDisplayTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, FinalBitmap.BitmapLoadAndDisplayTask bitmapWorkerTask) {
            super(res, bitmap);
            this.bitmapWorkerTaskReference = new WeakReference(bitmapWorkerTask);
        }

        public FinalBitmap.BitmapLoadAndDisplayTask getBitmapWorkerTask() {
            return (FinalBitmap.BitmapLoadAndDisplayTask)this.bitmapWorkerTaskReference.get();
        }
    }

    private class BitmapLoadAndDisplayTask extends AsyncTask<Object, Void, Bitmap> {
        private Object data;
        private final WeakReference<View> imageViewReference;
        private final BitmapDisplayConfig displayConfig;

        public BitmapLoadAndDisplayTask(View imageView, BitmapDisplayConfig config) {
            this.imageViewReference = new WeakReference(imageView);
            this.displayConfig = config;
        }

        protected Bitmap doInBackground(Object... params) {
            this.data = params[0];
            String dataString = String.valueOf(this.data);
            Bitmap bitmap = null;
            synchronized(FinalBitmap.this.mPauseWorkLock) {
                while(FinalBitmap.this.mPauseWork && !this.isCancelled()) {
                    try {
                        FinalBitmap.this.mPauseWorkLock.wait();
                    } catch (InterruptedException var6) {
                        ;
                    }
                }
            }

            if(bitmap == null && !this.isCancelled() && this.getAttachedImageView() != null && !FinalBitmap.this.mExitTasksEarly) {
                bitmap = FinalBitmap.this.processBitmap(dataString, this.displayConfig);
            }

            if(bitmap != null) {
                FinalBitmap.this.mImageCache.addToMemoryCache(dataString, bitmap);
            }

            return bitmap;
        }

        protected void onPostExecute(Bitmap bitmap) {
            if(this.isCancelled() || FinalBitmap.this.mExitTasksEarly) {
                bitmap = null;
            }

            View imageView = this.getAttachedImageView();
            if(bitmap != null && imageView != null) {
                FinalBitmap.this.mConfig.displayer.loadCompletedisplay(imageView, bitmap, this.displayConfig);
            } else if(bitmap == null && imageView != null) {
                FinalBitmap.this.mConfig.displayer.loadFailDisplay(imageView, this.displayConfig.getLoadfailBitmap());
            }

        }

        protected void onCancelled(Bitmap bitmap) {
            super.onCancelled(bitmap);
            synchronized(FinalBitmap.this.mPauseWorkLock) {
                FinalBitmap.this.mPauseWorkLock.notifyAll();
            }
        }

        private View getAttachedImageView() {
            View imageView = (View)this.imageViewReference.get();
            FinalBitmap.BitmapLoadAndDisplayTask bitmapWorkerTask = FinalBitmap.getBitmapTaskFromImageView(imageView);
            return this == bitmapWorkerTask?imageView:null;
        }
    }

    private class CacheExecutecTask extends AsyncTask<Object, Void, Void> {
        public static final int MESSAGE_CLEAR = 1;
        public static final int MESSAGE_CLOSE = 2;
        public static final int MESSAGE_CLEAR_DISK = 3;
        public static final int MESSAGE_CLEAR_KEY = 4;
        public static final int MESSAGE_CLEAR_KEY_IN_DISK = 5;

        private CacheExecutecTask(CacheExecutecTask cacheExecutecTask) {
        }

        protected Void doInBackground(Object... params) {
            switch(((Integer)params[0]).intValue()) {
                case 1:
                    FinalBitmap.this.clearCacheInternalInBackgroud();
                    break;
                case 2:
                    FinalBitmap.this.closeCacheInternalInBackgroud();
                    break;
                case 3:
                    FinalBitmap.this.clearDiskCacheInBackgroud();
                    break;
                case 4:
                    FinalBitmap.this.clearCacheInBackgroud(String.valueOf(params[1]));
                    break;
                case 5:
                    FinalBitmap.this.clearDiskCacheInBackgroud(String.valueOf(params[1]));
            }

            return null;
        }
    }

    private class FinalBitmapConfig {
        public String cachePath;
        public Displayer displayer;
        public Downloader downloader;
        public BitmapDisplayConfig defaultDisplayConfig = new BitmapDisplayConfig();
        public float memCacheSizePercent;
        public int memCacheSize;
        public int diskCacheSize;
        public int poolSize = 3;
        public boolean recycleImmediately = true;

        public FinalBitmapConfig(Context context) {
            this.defaultDisplayConfig.setAnimation((Animation)null);
            this.defaultDisplayConfig.setAnimationType(1);
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int defaultWidth = (int)Math.floor((double)(displayMetrics.widthPixels / 2));
            this.defaultDisplayConfig.setBitmapHeight(defaultWidth);
            this.defaultDisplayConfig.setBitmapWidth(defaultWidth);
        }
    }
}

package net.tsz.afinal.bitmap.core;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.graphics.Bitmap;
import android.view.animation.Animation;

public class BitmapDisplayConfig {
    private int bitmapWidth;
    private int bitmapHeight;
    private Animation animation;
    private int animationType;
    private Bitmap loadingBitmap;
    private Bitmap loadfailBitmap;

    public BitmapDisplayConfig() {
    }

    public int getBitmapWidth() {
        return this.bitmapWidth;
    }

    public void setBitmapWidth(int bitmapWidth) {
        this.bitmapWidth = bitmapWidth;
    }

    public int getBitmapHeight() {
        return this.bitmapHeight;
    }

    public void setBitmapHeight(int bitmapHeight) {
        this.bitmapHeight = bitmapHeight;
    }

    public Animation getAnimation() {
        return this.animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    public int getAnimationType() {
        return this.animationType;
    }

    public void setAnimationType(int animationType) {
        this.animationType = animationType;
    }

    public Bitmap getLoadingBitmap() {
        return this.loadingBitmap;
    }

    public void setLoadingBitmap(Bitmap loadingBitmap) {
        this.loadingBitmap = loadingBitmap;
    }

    public Bitmap getLoadfailBitmap() {
        return this.loadfailBitmap;
    }

    public void setLoadfailBitmap(Bitmap loadfailBitmap) {
        this.loadfailBitmap = loadfailBitmap;
    }

    public class AnimationType {
        public static final int userDefined = 0;
        public static final int fadeIn = 1;

        public AnimationType() {
        }
    }
}

package net.tsz.afinal.bitmap.display;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import net.tsz.afinal.bitmap.core.BitmapDisplayConfig;
import net.tsz.afinal.bitmap.display.Displayer;

public class SimpleDisplayer implements Displayer {
    public SimpleDisplayer() {
    }

    public void loadCompletedisplay(View imageView, Bitmap bitmap, BitmapDisplayConfig config) {
        switch(config.getAnimationType()) {
            case 0:
                this.animationDisplay(imageView, bitmap, config.getAnimation());
                break;
            case 1:
                this.fadeInDisplay(imageView, bitmap);
        }

    }

    public void loadFailDisplay(View imageView, Bitmap bitmap) {
        if(imageView instanceof ImageView) {
            ((ImageView)imageView).setImageBitmap(bitmap);
        } else {
            imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
        }

    }

    private void fadeInDisplay(View imageView, Bitmap bitmap) {
        TransitionDrawable td = new TransitionDrawable(new Drawable[]{new ColorDrawable(17170445), new BitmapDrawable(imageView.getResources(), bitmap)});
        if(imageView instanceof ImageView) {
            ((ImageView)imageView).setImageDrawable(td);
        } else {
            imageView.setBackgroundDrawable(td);
        }

        td.startTransition(300);
    }

    private void animationDisplay(View imageView, Bitmap bitmap, Animation animation) {
        animation.setStartTime(AnimationUtils.currentAnimationTimeMillis());
        if(imageView instanceof ImageView) {
            ((ImageView)imageView).setImageBitmap(bitmap);
        } else {
            imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
        }

        imageView.startAnimation(animation);
    }
}


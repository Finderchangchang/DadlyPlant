package net.tsz.afinal.bitmap.display;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.graphics.Bitmap;
import android.view.View;
import net.tsz.afinal.bitmap.core.BitmapDisplayConfig;

public interface Displayer {
    void loadCompletedisplay(View var1, Bitmap var2, BitmapDisplayConfig var3);

    void loadFailDisplay(View var1, Bitmap var2);
}

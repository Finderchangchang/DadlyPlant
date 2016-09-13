package net.tsz.afinal.bitmap.core;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.BitmapFactory.Options;
import java.io.FileDescriptor;

public class BitmapDecoder {
    private BitmapDecoder() {
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        try {
            return BitmapFactory.decodeResource(res, resId, options);
        } catch (OutOfMemoryError var6) {
            var6.printStackTrace();
            return null;
        }
    }

    public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, (Rect)null, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        try {
            return BitmapFactory.decodeFileDescriptor(fileDescriptor, (Rect)null, options);
        } catch (OutOfMemoryError var5) {
            var5.printStackTrace();
            return null;
        }
    }

    public static Bitmap decodeSampledBitmapFromByteArray(byte[] data, int offset, int length, int reqWidth, int reqHeight) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;
        BitmapFactory.decodeByteArray(data, offset, length, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, offset, length, options);
    }

    private static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if(height > reqHeight || width > reqWidth) {
            if(width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }

            float totalPixels = (float)(width * height);

            for(float totalReqPixelsCap = (float)(reqWidth * reqHeight * 2); totalPixels / (float)(inSampleSize * inSampleSize) > totalReqPixelsCap; ++inSampleSize) {
                ;
            }
        }

        return inSampleSize;
    }
}


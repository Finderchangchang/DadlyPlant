package net.tsz.afinal.http.entityhandler;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.text.TextUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import net.tsz.afinal.http.entityhandler.EntityCallBack;
import org.apache.http.HttpEntity;

public class FileEntityHandler {
    private boolean mStop = false;

    public FileEntityHandler() {
    }

    public boolean isStop() {
        return this.mStop;
    }

    public void setStop(boolean stop) {
        this.mStop = stop;
    }

    public Object handleEntity(HttpEntity entity, EntityCallBack callback, String target, boolean isResume) throws IOException {
        if(!TextUtils.isEmpty(target) && target.trim().length() != 0) {
            File targetFile = new File(target);
            if(!targetFile.exists()) {
                targetFile.createNewFile();
            }

            if(this.mStop) {
                return targetFile;
            } else {
                long current = 0L;
                FileOutputStream os = null;
                if(isResume) {
                    current = targetFile.length();
                    os = new FileOutputStream(target, true);
                } else {
                    os = new FileOutputStream(target);
                }

                if(this.mStop) {
                    return targetFile;
                } else {
                    InputStream input = entity.getContent();
                    long count = entity.getContentLength() + current;
                    if(current < count && !this.mStop) {
                        boolean readLen = false;
                        byte[] buffer = new byte[1024];

                        int readLen1;
                        while(!this.mStop && current < count && (readLen1 = input.read(buffer, 0, 1024)) > 0) {
                            os.write(buffer, 0, readLen1);
                            current += (long)readLen1;
                            callback.callBack(count, current, false);
                        }

                        callback.callBack(count, current, true);
                        if(this.mStop && current < count) {
                            throw new IOException("user stop download thread");
                        } else {
                            return targetFile;
                        }
                    } else {
                        return targetFile;
                    }
                }
            }
        } else {
            return null;
        }
    }
}

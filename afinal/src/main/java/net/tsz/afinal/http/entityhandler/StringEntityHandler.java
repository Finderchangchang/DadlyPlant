package net.tsz.afinal.http.entityhandler;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import net.tsz.afinal.http.entityhandler.EntityCallBack;
import org.apache.http.HttpEntity;

public class StringEntityHandler {
    public StringEntityHandler() {
    }

    public Object handleEntity(HttpEntity entity, EntityCallBack callback, String charset) throws IOException {
        if(entity == null) {
            return null;
        } else {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            long count = entity.getContentLength();
            long curCount = 0L;
            boolean len = true;
            InputStream is = entity.getContent();

            int len1;
            while((len1 = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, len1);
                curCount += (long)len1;
                if(callback != null) {
                    callback.callBack(count, curCount, false);
                }
            }

            if(callback != null) {
                callback.callBack(count, curCount, true);
            }

            byte[] data = outStream.toByteArray();
            outStream.close();
            is.close();
            return new String(data, charset);
        }
    }
}


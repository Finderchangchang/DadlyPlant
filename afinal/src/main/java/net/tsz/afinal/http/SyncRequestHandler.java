package net.tsz.afinal.http;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import java.io.IOException;
import java.net.UnknownHostException;
import net.tsz.afinal.http.entityhandler.EntityCallBack;
import net.tsz.afinal.http.entityhandler.StringEntityHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

public class SyncRequestHandler {
    private final AbstractHttpClient client;
    private final HttpContext context;
    private final StringEntityHandler entityHandler = new StringEntityHandler();
    private int executionCount = 0;
    private String charset;

    public SyncRequestHandler(AbstractHttpClient client, HttpContext context, String charset) {
        this.client = client;
        this.context = context;
        this.charset = charset;
    }

    private Object makeRequestWithRetries(HttpUriRequest request) throws Throwable {
        boolean retry = true;
        Object cause = null;
        HttpRequestRetryHandler retryHandler = this.client.getHttpRequestRetryHandler();

        while(retry) {
            try {
                HttpResponse e = this.client.execute(request, this.context);
                return this.entityHandler.handleEntity(e.getEntity(), (EntityCallBack)null, this.charset);
            } catch (UnknownHostException var6) {
                cause = var6;
                retry = retryHandler.retryRequest(var6, ++this.executionCount, this.context);
            } catch (IOException var7) {
                cause = var7;
                retry = retryHandler.retryRequest(var7, ++this.executionCount, this.context);
            } catch (NullPointerException var8) {
                cause = new IOException("NPE in HttpClient" + var8.getMessage());
                retry = retryHandler.retryRequest((IOException)cause, ++this.executionCount, this.context);
            } catch (Exception var9) {
                cause = new IOException("Exception" + var9.getMessage());
                retry = retryHandler.retryRequest((IOException)cause, ++this.executionCount, this.context);
            }
        }

        if(cause != null) {
            throw (Throwable) cause;
        } else {
            throw new IOException("未知网络错误");
        }
    }

    public Object sendRequest(HttpUriRequest... params) {
        try {
            return this.makeRequestWithRetries(params[0]);
        } catch (IOException var3) {
            var3.printStackTrace();
            return null;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }
}


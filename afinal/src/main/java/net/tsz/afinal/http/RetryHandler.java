package net.tsz.afinal.http;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.os.SystemClock;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import javax.net.ssl.SSLHandshakeException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

public class RetryHandler implements HttpRequestRetryHandler {
    private static final int RETRY_SLEEP_TIME_MILLIS = 1000;
    private static HashSet<Class<?>> exceptionWhitelist = new HashSet();
    private static HashSet<Class<?>> exceptionBlacklist = new HashSet();
    private final int maxRetries;

    static {
        exceptionWhitelist.add(NoHttpResponseException.class);
        exceptionWhitelist.add(UnknownHostException.class);
        exceptionWhitelist.add(SocketException.class);
        exceptionBlacklist.add(InterruptedIOException.class);
        exceptionBlacklist.add(SSLHandshakeException.class);
    }

    public RetryHandler(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        boolean retry = true;
        Boolean b = (Boolean)context.getAttribute("http.request_sent");
        boolean sent = b != null && b.booleanValue();
        if(executionCount > this.maxRetries) {
            retry = false;
        } else if(exceptionBlacklist.contains(exception.getClass())) {
            retry = false;
        } else if(exceptionWhitelist.contains(exception.getClass())) {
            retry = true;
        } else if(!sent) {
            retry = true;
        }

        if(retry) {
            HttpUriRequest currentReq = (HttpUriRequest)context.getAttribute("http.request");
            retry = currentReq != null && !"POST".equals(currentReq.getMethod());
        }

        if(retry) {
            SystemClock.sleep(1000L);
        } else {
            exception.printStackTrace();
        }

        return retry;
    }
}


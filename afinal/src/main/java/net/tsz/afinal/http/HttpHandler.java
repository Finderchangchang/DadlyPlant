package net.tsz.afinal.http;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.os.SystemClock;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import net.tsz.afinal.core.AsyncTask;
import net.tsz.afinal.http.entityhandler.EntityCallBack;
import net.tsz.afinal.http.entityhandler.FileEntityHandler;
import net.tsz.afinal.http.entityhandler.StringEntityHandler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

public class HttpHandler<T> extends AsyncTask<Object, Object, Object> implements EntityCallBack {
    private final AbstractHttpClient client;
    private final HttpContext context;
    private final StringEntityHandler mStrEntityHandler = new StringEntityHandler();
    private final FileEntityHandler mFileEntityHandler = new FileEntityHandler();
    private final AjaxCallBack<T> callback;
    private int executionCount = 0;
    private String targetUrl = null;
    private boolean isResume = false;
    private String charset;
    private static final int UPDATE_START = 1;
    private static final int UPDATE_LOADING = 2;
    private static final int UPDATE_FAILURE = 3;
    private static final int UPDATE_SUCCESS = 4;
    private long time;

    public HttpHandler(AbstractHttpClient client, HttpContext context, AjaxCallBack<T> callback, String charset) {
        this.client = client;
        this.context = context;
        this.callback = callback;
        this.charset = charset;
    }

    private void makeRequestWithRetries(HttpUriRequest request) throws IOException {
        if(this.isResume && this.targetUrl != null) {
            File retry = new File(this.targetUrl);
            long cause = 0L;
            if(retry.isFile() && retry.exists()) {
                cause = retry.length();
            }

            if(cause > 0L) {
                request.setHeader("RANGE", "bytes=" + cause + "-");
            }
        }

        boolean var10 = true;
        IOException var11 = null;
        HttpRequestRetryHandler retryHandler = this.client.getHttpRequestRetryHandler();

        while(var10) {
            try {
                if(!this.isCancelled()) {
                    HttpResponse e = this.client.execute(request, this.context);
                    if(!this.isCancelled()) {
                        this.handleResponse(e);
                    }
                }

                return;
            } catch (UnknownHostException var6) {
                this.publishProgress(new Object[]{Integer.valueOf(3), var6, Integer.valueOf(0), "unknownHostException：can\'t resolve host"});
                return;
            } catch (IOException var7) {
                var11 = var7;
                var10 = retryHandler.retryRequest(var7, ++this.executionCount, this.context);
            } catch (NullPointerException var8) {
                var11 = new IOException("NPE in HttpClient" + var8.getMessage());
                var10 = retryHandler.retryRequest(var11, ++this.executionCount, this.context);
            } catch (Exception var9) {
                var11 = new IOException("Exception" + var9.getMessage());
                var10 = retryHandler.retryRequest(var11, ++this.executionCount, this.context);
            }
        }

        if(var11 != null) {
            throw var11;
        } else {
            throw new IOException("未知网络错误");
        }
    }

    protected Object doInBackground(Object... params) {
        if(params != null && params.length == 3) {
            this.targetUrl = String.valueOf(params[1]);
            this.isResume = ((Boolean)params[2]).booleanValue();
        }

        try {
            this.publishProgress(new Object[]{Integer.valueOf(1)});
            this.makeRequestWithRetries((HttpUriRequest)params[0]);
        } catch (IOException var3) {
            this.publishProgress(new Object[]{Integer.valueOf(3), var3, Integer.valueOf(0), var3.getMessage()});
        }

        return null;
    }

    protected void onProgressUpdate(Object... values) {
        int update = Integer.valueOf(String.valueOf(values[0])).intValue();
        switch(update) {
            case 1:
                if(this.callback != null) {
                    this.callback.onStart();
                }
                break;
            case 2:
                if(this.callback != null) {
                    this.callback.onLoading(Long.valueOf(String.valueOf(values[1])).longValue(), Long.valueOf(String.valueOf(values[2])).longValue());
                }
                break;
            case 3:
                if(this.callback != null) {
                    this.callback.onFailure((Throwable)values[1], ((Integer)values[2]).intValue(), (String)values[3]);
                }
                break;
            case 4:
                if(this.callback != null) {
                    this.callback.onSuccess((T) values[1]);
                }
        }

        super.onProgressUpdate(values);
    }

    public boolean isStop() {
        return this.mFileEntityHandler.isStop();
    }

    public void stop() {
        this.mFileEntityHandler.setStop(true);
    }

    private void handleResponse(HttpResponse response) {
        StatusLine status = response.getStatusLine();
        if(status.getStatusCode() >= 300) {
            String e = "response status error code:" + status.getStatusCode();
            if(status.getStatusCode() == 416 && this.isResume) {
                e = e + " \n maybe you have download complete.";
            }

            this.publishProgress(new Object[]{Integer.valueOf(3), new HttpResponseException(status.getStatusCode(), status.getReasonPhrase()), Integer.valueOf(status.getStatusCode()), e});
        } else {
            try {
                HttpEntity e1 = response.getEntity();
                Object responseBody = null;
                if(e1 != null) {
                    this.time = SystemClock.uptimeMillis();
                    if(this.targetUrl != null) {
                        responseBody = this.mFileEntityHandler.handleEntity(e1, this, this.targetUrl, this.isResume);
                    } else {
                        responseBody = this.mStrEntityHandler.handleEntity(e1, this, this.charset);
                    }
                }

                this.publishProgress(new Object[]{Integer.valueOf(4), responseBody});
            } catch (IOException var5) {
                this.publishProgress(new Object[]{Integer.valueOf(3), var5, Integer.valueOf(0), var5.getMessage()});
            }
        }

    }

    public void callBack(long count, long current, boolean mustNoticeUI) {
        if(this.callback != null && this.callback.isProgress()) {
            if(mustNoticeUI) {
                this.publishProgress(new Object[]{Integer.valueOf(2), Long.valueOf(count), Long.valueOf(current)});
            } else {
                long thisTime = SystemClock.uptimeMillis();
                if(thisTime - this.time >= (long)this.callback.getRate()) {
                    this.time = thisTime;
                    this.publishProgress(new Object[]{Integer.valueOf(2), Long.valueOf(count), Long.valueOf(current)});
                }
            }
        }

    }
}

package net.tsz.afinal;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import net.tsz.afinal.http.HttpHandler;
import net.tsz.afinal.http.RetryHandler;
import net.tsz.afinal.http.SyncRequestHandler;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

public class FinalHttp {
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";
    private static int maxConnections = 10;
    private static int socketTimeout = 10000;
    private static int maxRetries = 5;
    private static int httpThreadCount = 3;
    private final DefaultHttpClient httpClient;
    private final HttpContext httpContext;
    private String charset = "utf-8";
    private final Map<String, String> clientHeaderMap;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            Thread tread = new Thread(r, "FinalHttp #" + this.mCount.getAndIncrement());
            tread.setPriority(4);
            return tread;
        }
    };
    private static final Executor executor;

    static {
        executor = Executors.newFixedThreadPool(httpThreadCount, sThreadFactory);
    }

    public FinalHttp() {
        BasicHttpParams httpParams = new BasicHttpParams();
        ConnManagerParams.setTimeout(httpParams, (long)socketTimeout);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(maxConnections));
        ConnManagerParams.setMaxTotalConnections(httpParams, 10);
        HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, socketTimeout);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
        this.httpContext = new SyncBasicHttpContext(new BasicHttpContext());
        this.httpClient = new DefaultHttpClient(cm, httpParams);
        this.httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(HttpRequest request, HttpContext context) {
                if(!request.containsHeader("Accept-Encoding")) {
                    request.addHeader("Accept-Encoding", "gzip");
                }

                Iterator var4 = FinalHttp.this.clientHeaderMap.keySet().iterator();

                while(var4.hasNext()) {
                    String header = (String)var4.next();
                    request.addHeader(header, (String)FinalHttp.this.clientHeaderMap.get(header));
                }

            }
        });
        this.httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(HttpResponse response, HttpContext context) {
                HttpEntity entity = response.getEntity();
                if(entity != null) {
                    Header encoding = entity.getContentEncoding();
                    if(encoding != null) {
                        HeaderElement[] var8;
                        int var7 = (var8 = encoding.getElements()).length;

                        for(int var6 = 0; var6 < var7; ++var6) {
                            HeaderElement element = var8[var6];
                            if(element.getName().equalsIgnoreCase("gzip")) {
                                response.setEntity(new FinalHttp.InflatingEntity(response.getEntity()));
                                break;
                            }
                        }
                    }

                }
            }
        });
        this.httpClient.setHttpRequestRetryHandler(new RetryHandler(maxRetries));
        this.clientHeaderMap = new HashMap();
    }

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    public HttpContext getHttpContext() {
        return this.httpContext;
    }

    public void configCharset(String charSet) {
        if(charSet != null && charSet.trim().length() != 0) {
            this.charset = charSet;
        }

    }

    public void configCookieStore(CookieStore cookieStore) {
        this.httpContext.setAttribute("http.cookie-store", cookieStore);
    }

    public void configUserAgent(String userAgent) {
        HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
    }

    public void configTimeout(int timeout) {
        HttpParams httpParams = this.httpClient.getParams();
        ConnManagerParams.setTimeout(httpParams, (long)timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
    }

    public void configSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        Scheme scheme = new Scheme("https", sslSocketFactory, 443);
        this.httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
    }

    public void configRequestExecutionRetryCount(int count) {
        this.httpClient.setHttpRequestRetryHandler(new RetryHandler(count));
    }

    public void addHeader(String header, String value) {
        this.clientHeaderMap.put(header, value);
    }

    public void get(String url, AjaxCallBack<? extends Object> callBack) {
        this.get(url, (AjaxParams)null, callBack);
    }

    public void get(String url, AjaxParams params, AjaxCallBack<? extends Object> callBack) {
        this.sendRequest(this.httpClient, this.httpContext, new HttpGet(getUrlWithQueryString(url, params)), (String)null, callBack);
    }

    public void get(String url, Header[] headers, AjaxParams params, AjaxCallBack<? extends Object> callBack) {
        HttpGet request = new HttpGet(getUrlWithQueryString(url, params));
        if(headers != null) {
            request.setHeaders(headers);
        }

        this.sendRequest(this.httpClient, this.httpContext, request, (String)null, callBack);
    }

    public Object getSync(String url) {
        return this.getSync(url, (AjaxParams)null);
    }

    public Object getSync(String url, AjaxParams params) {
        HttpGet request = new HttpGet(getUrlWithQueryString(url, params));
        return this.sendSyncRequest(this.httpClient, this.httpContext, request, (String)null);
    }

    public Object getSync(String url, Header[] headers, AjaxParams params) {
        HttpGet request = new HttpGet(getUrlWithQueryString(url, params));
        if(headers != null) {
            request.setHeaders(headers);
        }

        return this.sendSyncRequest(this.httpClient, this.httpContext, request, (String)null);
    }

    public void post(String url, AjaxCallBack<? extends Object> callBack) {
        this.post(url, (AjaxParams)null, callBack);
    }

    public void post(String url, AjaxParams params, AjaxCallBack<? extends Object> callBack) {
        this.post(url, this.paramsToEntity(params), (String)null, callBack);
    }

    public void post(String url, HttpEntity entity, String contentType, AjaxCallBack<? extends Object> callBack) {
        this.sendRequest(this.httpClient, this.httpContext, this.addEntityToRequestBase(new HttpPost(url), entity), contentType, callBack);
    }

    public <T> void post(String url, Header[] headers, AjaxParams params, String contentType, AjaxCallBack<T> callBack) {
        HttpPost request = new HttpPost(url);
        if(params != null) {
            request.setEntity(this.paramsToEntity(params));
        }

        if(headers != null) {
            request.setHeaders(headers);
        }

        this.sendRequest(this.httpClient, this.httpContext, request, contentType, callBack);
    }

    public void post(String url, Header[] headers, HttpEntity entity, String contentType, AjaxCallBack<? extends Object> callBack) {
        HttpEntityEnclosingRequestBase request = this.addEntityToRequestBase(new HttpPost(url), entity);
        if(headers != null) {
            request.setHeaders(headers);
        }

        this.sendRequest(this.httpClient, this.httpContext, request, contentType, callBack);
    }

    public Object postSync(String url) {
        return this.postSync(url, (AjaxParams)null);
    }

    public Object postSync(String url, AjaxParams params) {
        return this.postSync(url, this.paramsToEntity(params), (String)null);
    }

    public Object postSync(String url, HttpEntity entity, String contentType) {
        return this.sendSyncRequest(this.httpClient, this.httpContext, this.addEntityToRequestBase(new HttpPost(url), entity), contentType);
    }

    public Object postSync(String url, Header[] headers, AjaxParams params, String contentType) {
        HttpPost request = new HttpPost(url);
        if(params != null) {
            request.setEntity(this.paramsToEntity(params));
        }

        if(headers != null) {
            request.setHeaders(headers);
        }

        return this.sendSyncRequest(this.httpClient, this.httpContext, request, contentType);
    }

    public Object postSync(String url, Header[] headers, HttpEntity entity, String contentType) {
        HttpEntityEnclosingRequestBase request = this.addEntityToRequestBase(new HttpPost(url), entity);
        if(headers != null) {
            request.setHeaders(headers);
        }

        return this.sendSyncRequest(this.httpClient, this.httpContext, request, contentType);
    }

    public void put(String url, AjaxCallBack<? extends Object> callBack) {
        this.put(url, (AjaxParams)null, callBack);
    }

    public void put(String url, AjaxParams params, AjaxCallBack<? extends Object> callBack) {
        this.put(url, this.paramsToEntity(params), (String)null, callBack);
    }

    public void put(String url, HttpEntity entity, String contentType, AjaxCallBack<? extends Object> callBack) {
        this.sendRequest(this.httpClient, this.httpContext, this.addEntityToRequestBase(new HttpPut(url), entity), contentType, callBack);
    }

    public void put(String url, Header[] headers, HttpEntity entity, String contentType, AjaxCallBack<? extends Object> callBack) {
        HttpEntityEnclosingRequestBase request = this.addEntityToRequestBase(new HttpPut(url), entity);
        if(headers != null) {
            request.setHeaders(headers);
        }

        this.sendRequest(this.httpClient, this.httpContext, request, contentType, callBack);
    }

    public Object putSync(String url) {
        return this.putSync(url, (AjaxParams)null);
    }

    public Object putSync(String url, AjaxParams params) {
        return this.putSync(url, this.paramsToEntity(params), (String)null);
    }

    public Object putSync(String url, HttpEntity entity, String contentType) {
        return this.putSync(url, (Header[])null, entity, contentType);
    }

    public Object putSync(String url, Header[] headers, HttpEntity entity, String contentType) {
        HttpEntityEnclosingRequestBase request = this.addEntityToRequestBase(new HttpPut(url), entity);
        if(headers != null) {
            request.setHeaders(headers);
        }

        return this.sendSyncRequest(this.httpClient, this.httpContext, request, contentType);
    }

    public void delete(String url, AjaxCallBack<? extends Object> callBack) {
        HttpDelete delete = new HttpDelete(url);
        this.sendRequest(this.httpClient, this.httpContext, delete, (String)null, callBack);
    }

    public void delete(String url, Header[] headers, AjaxCallBack<? extends Object> callBack) {
        HttpDelete delete = new HttpDelete(url);
        if(headers != null) {
            delete.setHeaders(headers);
        }

        this.sendRequest(this.httpClient, this.httpContext, delete, (String)null, callBack);
    }

    public Object deleteSync(String url) {
        return this.deleteSync(url, (Header[])null);
    }

    public Object deleteSync(String url, Header[] headers) {
        HttpDelete delete = new HttpDelete(url);
        if(headers != null) {
            delete.setHeaders(headers);
        }

        return this.sendSyncRequest(this.httpClient, this.httpContext, delete, (String)null);
    }

    public HttpHandler<File> download(String url, String target, AjaxCallBack<File> callback) {
        return this.download(url, (AjaxParams)null, target, false, callback);
    }

    public HttpHandler<File> download(String url, String target, boolean isResume, AjaxCallBack<File> callback) {
        return this.download(url, (AjaxParams)null, target, isResume, callback);
    }

    public HttpHandler<File> download(String url, AjaxParams params, String target, AjaxCallBack<File> callback) {
        return this.download(url, params, target, false, callback);
    }

    public HttpHandler<File> download(String url, AjaxParams params, String target, boolean isResume, AjaxCallBack<File> callback) {
        HttpGet get = new HttpGet(getUrlWithQueryString(url, params));
        HttpHandler handler = new HttpHandler(this.httpClient, this.httpContext, callback, this.charset);
        handler.executeOnExecutor(executor, new Object[]{get, target, Boolean.valueOf(isResume)});
        return handler;
    }

    protected <T> void sendRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, AjaxCallBack<T> ajaxCallBack) {
        if(contentType != null) {
            uriRequest.addHeader("Content-Type", contentType);
        }

        (new HttpHandler(client, httpContext, ajaxCallBack, this.charset)).executeOnExecutor(executor, new Object[]{uriRequest});
    }

    protected Object sendSyncRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType) {
        if(contentType != null) {
            uriRequest.addHeader("Content-Type", contentType);
        }

        return (new SyncRequestHandler(client, httpContext, this.charset)).sendRequest(new HttpUriRequest[]{uriRequest});
    }

    public static String getUrlWithQueryString(String url, AjaxParams params) {
        if(params != null) {
            String paramString = params.getParamString();
            url = url + "?" + paramString;
        }

        return url;
    }

    private HttpEntity paramsToEntity(AjaxParams params) {
        HttpEntity entity = null;
        if(params != null) {
            entity = params.getEntity();
        }

        return entity;
    }

    private HttpEntityEnclosingRequestBase addEntityToRequestBase(HttpEntityEnclosingRequestBase requestBase, HttpEntity entity) {
        if(entity != null) {
            requestBase.setEntity(entity);
        }

        return requestBase;
    }

    private static class InflatingEntity extends HttpEntityWrapper {
        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        public InputStream getContent() throws IOException {
            return new GZIPInputStream(this.wrappedEntity.getContent());
        }

        public long getContentLength() {
            return -1L;
        }
    }
}

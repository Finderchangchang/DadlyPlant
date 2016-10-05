package liuliu.dadlyplant.method;

import android.net.Uri;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

import liuliu.dadlyplant.base.BaseApplication;
import liuliu.dadlyplant.config.Config;

/**
 * 访问网络方法
 * Created by Administrator on 2016/10/5.
 */

public class HttpUtil {
    /**
     * 执行添加操作只能访问一次
     *
     * @param method
     * @param map
     * @param listener
     */
    public static void loadSave(String method, Map<String, String> map, final LoadJsonListener listener) {
        load(method, map, listener, true);
    }

    public static void loadJson(String method, Map<String, String> map, final LoadJsonListener listener) {
        load(method, map, listener, false);
    }

    public static void load(String method, Map<String, String> map, final LoadJsonListener listener, boolean isSave) {
        String url;
        if (method.equals("version")) {
            url = Config.DOWN_PATH + method + ".aspx?1=1";
        } else {
            if (method.contains("shop")) {
                url = Config.URL + method + ".aspx?1=1";
            } else {
                url = Config.PATH + method + ".aspx?1=1";
            }
        }
        if (map != null) {
            Iterator i = map.entrySet().iterator();
            while (i.hasNext()) {
                String link = i.next().toString();
                if (isSave) {
                    url = url + "&" + link.split("=")[0] + "=" + Uri.encode(link.split("=")[1]);
                } else {
                    url = url + "&" + link;
                }
            }
            url = url.replace(" ", "%20");
        }
        Log.e("XX", url);

        JsonObjectRequest json = new JsonObjectRequest(
                url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.load(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CrashReport.postCatchedException(error);
                listener.load(null);
            }
        });
        if (!isSave) {
            json.setRetryPolicy(
                    new DefaultRetryPolicy(
                            500000,//默认超时时间，应设置一个稍微大点儿的，例如本处的500000
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,//默认最大尝试次数
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                    )
            );
        }
//        if (BaseApplication.mQueue != null) {
//            BaseApplication.mQueue = Volley.newRequestQueue(BaseApplication.getContext());
//        }
//        BaseApplication.mQueue.add(json);
//        BaseApplication.mQueue.start();
    }

    public static void load(String method, Map<String, String> map, final LoadArrayListener listener) {
        String url = Config.PATH + method + ".aspx?1=1";

        if (map != null) {
            Iterator i = map.entrySet().iterator();
            while (i.hasNext()) {
                url = url + "&" + i.next().toString();
            }
            url = url.replace(" ", "%20");
        }
        Log.e("XX", url);
        JsonArrayRequest arrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                listener.load(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.load(null);
            }
        });
//        if (BaseApplication.mQueue != null) {
//            BaseApplication.mQueue = Volley.newRequestQueue(BaseApplication.getContext());
//        }
//        BaseApplication.mQueue.add(arrayRequest);
//        BaseApplication.mQueue.start();
    }


    public interface LoadArrayListener {
        void load(JSONArray obj);
    }

    public interface LoadJsonListener {
        void load(JSONObject obj);
    }
}

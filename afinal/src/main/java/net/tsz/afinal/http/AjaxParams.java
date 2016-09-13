package net.tsz.afinal.http;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

public class AjaxParams {
    private static String ENCODING = "UTF-8";
    protected ConcurrentHashMap<String, String> urlParams;
    protected ConcurrentHashMap<String, AjaxParams.FileWrapper> fileParams;

    public AjaxParams() {
        this.init();
    }

    public AjaxParams(Map<String, String> source) {
        this.init();
        Iterator var3 = source.entrySet().iterator();

        while(var3.hasNext()) {
            Entry entry = (Entry)var3.next();
            this.put((String)entry.getKey(), (String)entry.getValue());
        }

    }

    public AjaxParams(String key, String value) {
        this.init();
        this.put(key, value);
    }

    public AjaxParams(Object... keysAndValues) {
        this.init();
        int len = keysAndValues.length;
        if(len % 2 != 0) {
            throw new IllegalArgumentException("Supplied arguments must be even");
        } else {
            for(int i = 0; i < len; i += 2) {
                String key = String.valueOf(keysAndValues[i]);
                String val = String.valueOf(keysAndValues[i + 1]);
                this.put(key, val);
            }

        }
    }

    public void put(String key, String value) {
        if(key != null && value != null) {
            this.urlParams.put(key, value);
        }

    }

    public void put(String key, File file) throws FileNotFoundException {
        this.put(key, new FileInputStream(file), file.getName());
    }

    public void put(String key, InputStream stream) {
        this.put(key, stream, (String)null);
    }

    public void put(String key, InputStream stream, String fileName) {
        this.put(key, stream, fileName, (String)null);
    }

    public void put(String key, InputStream stream, String fileName, String contentType) {
        if(key != null && stream != null) {
            this.fileParams.put(key, new AjaxParams.FileWrapper(stream, fileName, contentType));
        }

    }

    public void remove(String key) {
        this.urlParams.remove(key);
        this.fileParams.remove(key);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        Iterator var3 = this.urlParams.entrySet().iterator();

        Entry entry;
        while(var3.hasNext()) {
            entry = (Entry)var3.next();
            if(result.length() > 0) {
                result.append("&");
            }

            result.append((String)entry.getKey());
            result.append("=");
            result.append((String)entry.getValue());
        }

        var3 = this.fileParams.entrySet().iterator();

        while(var3.hasNext()) {
            entry = (Entry)var3.next();
            if(result.length() > 0) {
                result.append("&");
            }

            result.append((String)entry.getKey());
            result.append("=");
            result.append("FILE");
        }

        return result.toString();
    }

    public HttpEntity getEntity() {
        Object entity = null;
        if(!this.fileParams.isEmpty()) {
            MultipartEntity e = new MultipartEntity();
            Iterator lastIndex = this.urlParams.entrySet().iterator();

            while(lastIndex.hasNext()) {
                Entry currentIndex = (Entry)lastIndex.next();
                e.addPart((String)currentIndex.getKey(), (String)currentIndex.getValue());
            }

            int var10 = 0;
            int var11 = this.fileParams.entrySet().size() - 1;

            for(Iterator var6 = this.fileParams.entrySet().iterator(); var6.hasNext(); ++var10) {
                Entry entry = (Entry)var6.next();
                AjaxParams.FileWrapper file = (AjaxParams.FileWrapper)entry.getValue();
                if(file.inputStream != null) {
                    boolean isLast = var10 == var11;
                    if(file.contentType != null) {
                        e.addPart((String)entry.getKey(), file.getFileName(), file.inputStream, file.contentType, isLast);
                    } else {
                        e.addPart((String)entry.getKey(), file.getFileName(), file.inputStream, isLast);
                    }
                }
            }

            entity = e;
        } else {
            try {
                entity = new UrlEncodedFormEntity(this.getParamsList(), ENCODING);
            } catch (UnsupportedEncodingException var9) {
                var9.printStackTrace();
            }
        }

        return (HttpEntity)entity;
    }

    private void init() {
        this.urlParams = new ConcurrentHashMap();
        this.fileParams = new ConcurrentHashMap();
    }

    protected List<BasicNameValuePair> getParamsList() {
        LinkedList lparams = new LinkedList();
        Iterator var3 = this.urlParams.entrySet().iterator();

        while(var3.hasNext()) {
            Entry entry = (Entry)var3.next();
            lparams.add(new BasicNameValuePair((String)entry.getKey(), (String)entry.getValue()));
        }

        return lparams;
    }

    public String getParamString() {
        return URLEncodedUtils.format(this.getParamsList(), ENCODING);
    }

    private static class FileWrapper {
        public InputStream inputStream;
        public String fileName;
        public String contentType;

        public FileWrapper(InputStream inputStream, String fileName, String contentType) {
            this.inputStream = inputStream;
            this.fileName = fileName;
            this.contentType = contentType;
        }

        public String getFileName() {
            return this.fileName != null?this.fileName:"nofilename";
        }
    }
}

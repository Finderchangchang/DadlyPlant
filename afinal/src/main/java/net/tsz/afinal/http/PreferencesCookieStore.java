package net.tsz.afinal.http;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

public class PreferencesCookieStore implements CookieStore {
    private static final String COOKIE_PREFS = "CookiePrefsFile";
    private static final String COOKIE_NAME_STORE = "names";
    private static final String COOKIE_NAME_PREFIX = "cookie_";
    private final ConcurrentHashMap<String, Cookie> cookies;
    private final SharedPreferences cookiePrefs;

    public PreferencesCookieStore(Context context) {
        this.cookiePrefs = context.getSharedPreferences("CookiePrefsFile", 0);
        this.cookies = new ConcurrentHashMap();
        String storedCookieNames = this.cookiePrefs.getString("names", (String)null);
        if(storedCookieNames != null) {
            String[] cookieNames = TextUtils.split(storedCookieNames, ",");
            String[] var7 = cookieNames;
            int var6 = cookieNames.length;

            for(int var5 = 0; var5 < var6; ++var5) {
                String name = var7[var5];
                String encodedCookie = this.cookiePrefs.getString("cookie_" + name, (String)null);
                if(encodedCookie != null) {
                    Cookie decodedCookie = this.decodeCookie(encodedCookie);
                    if(decodedCookie != null) {
                        this.cookies.put(name, decodedCookie);
                    }
                }
            }

            this.clearExpired(new Date());
        }

    }

    public void addCookie(Cookie cookie) {
        String name = cookie.getName();
        if(!cookie.isExpired(new Date())) {
            this.cookies.put(name, cookie);
        } else {
            this.cookies.remove(name);
        }

        Editor prefsWriter = this.cookiePrefs.edit();
        prefsWriter.putString("names", TextUtils.join(",", this.cookies.keySet()));
        prefsWriter.putString("cookie_" + name, this.encodeCookie(new PreferencesCookieStore.SerializableCookie(cookie)));
        prefsWriter.commit();
    }

    public void clear() {
        this.cookies.clear();
        Editor prefsWriter = this.cookiePrefs.edit();
        Iterator var3 = this.cookies.keySet().iterator();

        while(var3.hasNext()) {
            String name = (String)var3.next();
            prefsWriter.remove("cookie_" + name);
        }

        prefsWriter.remove("names");
        prefsWriter.commit();
    }

    public boolean clearExpired(Date date) {
        boolean clearedAny = false;
        Editor prefsWriter = this.cookiePrefs.edit();
        Iterator var5 = this.cookies.entrySet().iterator();

        while(var5.hasNext()) {
            Entry entry = (Entry)var5.next();
            String name = (String)entry.getKey();
            Cookie cookie = (Cookie)entry.getValue();
            if(cookie.isExpired(date)) {
                this.cookies.remove(name);
                prefsWriter.remove("cookie_" + name);
                clearedAny = true;
            }
        }

        if(clearedAny) {
            prefsWriter.putString("names", TextUtils.join(",", this.cookies.keySet()));
        }

        prefsWriter.commit();
        return clearedAny;
    }

    public List<Cookie> getCookies() {
        return new ArrayList(this.cookies.values());
    }

    protected String encodeCookie(PreferencesCookieStore.SerializableCookie cookie) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            ObjectOutputStream e = new ObjectOutputStream(os);
            e.writeObject(cookie);
        } catch (Exception var4) {
            return null;
        }

        return this.byteArrayToHexString(os.toByteArray());
    }

    protected Cookie decodeCookie(String cookieStr) {
        byte[] bytes = this.hexStringToByteArray(cookieStr);
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        Cookie cookie = null;

        try {
            ObjectInputStream e = new ObjectInputStream(is);
            cookie = ((PreferencesCookieStore.SerializableCookie)e.readObject()).getCookie();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return cookie;
    }

    protected String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        byte[] var6 = b;
        int var5 = b.length;

        for(int var4 = 0; var4 < var5; ++var4) {
            byte element = var6[var4];
            int v = element & 255;
            if(v < 16) {
                sb.append('0');
            }

            sb.append(Integer.toHexString(v));
        }

        return sb.toString().toUpperCase();
    }

    protected byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for(int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public class SerializableCookie implements Serializable {
        private static final long serialVersionUID = 6374381828722046732L;
        private final transient Cookie cookie;
        private transient BasicClientCookie clientCookie;

        public SerializableCookie(Cookie cookie) {
            this.cookie = cookie;
        }

        public Cookie getCookie() {
            Object bestCookie = this.cookie;
            if(this.clientCookie != null) {
                bestCookie = this.clientCookie;
            }

            return (Cookie)bestCookie;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(this.cookie.getName());
            out.writeObject(this.cookie.getValue());
            out.writeObject(this.cookie.getComment());
            out.writeObject(this.cookie.getDomain());
            out.writeObject(this.cookie.getExpiryDate());
            out.writeObject(this.cookie.getPath());
            out.writeInt(this.cookie.getVersion());
            out.writeBoolean(this.cookie.isSecure());
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            String name = (String)in.readObject();
            String value = (String)in.readObject();
            this.clientCookie = new BasicClientCookie(name, value);
            this.clientCookie.setComment((String)in.readObject());
            this.clientCookie.setDomain((String)in.readObject());
            this.clientCookie.setExpiryDate((Date)in.readObject());
            this.clientCookie.setPath((String)in.readObject());
            this.clientCookie.setVersion(in.readInt());
            this.clientCookie.setSecure(in.readBoolean());
        }
    }
}


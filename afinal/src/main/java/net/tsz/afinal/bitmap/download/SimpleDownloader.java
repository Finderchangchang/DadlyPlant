package net.tsz.afinal.bitmap.download;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import net.tsz.afinal.bitmap.download.Downloader;

public class SimpleDownloader implements Downloader {
    private static final String TAG = SimpleDownloader.class.getSimpleName();
    private static final int IO_BUFFER_SIZE = 8192;

    public SimpleDownloader() {
    }

    public byte[] download(String urlString) {
        if(urlString == null) {
            return null;
        } else if(urlString.trim().toLowerCase().startsWith("http")) {
            return this.getFromHttp(urlString);
        } else {
            File f;
            if(urlString.trim().toLowerCase().startsWith("file:")) {
                try {
                    f = new File(new URI(urlString));
                    if(f.exists() && f.canRead()) {
                        return this.getFromFile(f);
                    }
                } catch (URISyntaxException var3) {
                    Log.e(TAG, "Error in read from file - " + urlString + " : " + var3);
                }
            } else {
                f = new File(urlString);
                if(f.exists() && f.canRead()) {
                    return this.getFromFile(f);
                }
            }

            return null;
        }
    }

    private byte[] getFromFile(File file) {
        if(file == null) {
            return null;
        } else {
            FileInputStream fis = null;

            try {
                fis = new FileInputStream(file);
                ByteArrayOutputStream e = new ByteArrayOutputStream();
                boolean len = false;
                byte[] buffer = new byte[1024];

                int len1;
                while((len1 = fis.read(buffer)) != -1) {
                    e.write(buffer, 0, len1);
                }

                byte[] var7 = e.toByteArray();
                return var7;
            } catch (Exception var15) {
                Log.e(TAG, "Error in read from file - " + file + " : " + var15);
            } finally {
                if(fis != null) {
                    try {
                        fis.close();
                        fis = null;
                    } catch (IOException var14) {
                        ;
                    }
                }

            }

            return null;
        }
    }

    private byte[] getFromHttp(String urlString) {
        HttpURLConnection urlConnection = null;
        Object out = null;
        SimpleDownloader.FlushedInputStream in = null;

        try {
            URL e = new URL(urlString);
            urlConnection = (HttpURLConnection)e.openConnection();
            in = new SimpleDownloader.FlushedInputStream(new BufferedInputStream(urlConnection.getInputStream(), 8192));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int b;
            while((b = in.read()) != -1) {
                baos.write(b);
            }

            byte[] var9 = baos.toByteArray();
            return var9;
        } catch (IOException var17) {
            Log.e(TAG, "Error in downloadBitmap - " + urlString + " : " + var17);
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }

            try {
                if(out != null) {
                    ((BufferedOutputStream)out).close();
                }

                if(in != null) {
                    in.close();
                }
            } catch (IOException var16) {
                ;
            }

        }

        return null;
    }

    public class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        public long skip(long n) throws IOException {
            long totalBytesSkipped;
            long bytesSkipped;
            for(totalBytesSkipped = 0L; totalBytesSkipped < n; totalBytesSkipped += bytesSkipped) {
                bytesSkipped = this.in.skip(n - totalBytesSkipped);
                if(bytesSkipped == 0L) {
                    int by_te = this.read();
                    if(by_te < 0) {
                        break;
                    }

                    bytesSkipped = 1L;
                }
            }

            return totalBytesSkipped;
        }
    }
}


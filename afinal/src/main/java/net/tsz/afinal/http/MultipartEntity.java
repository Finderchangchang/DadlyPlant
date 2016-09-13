package net.tsz.afinal.http;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

class MultipartEntity implements HttpEntity {
    private static final char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private String boundary = null;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    boolean isSetLast = false;
    boolean isSetFirst = false;

    public MultipartEntity() {
        StringBuffer buf = new StringBuffer();
        Random rand = new Random();

        for(int i = 0; i < 30; ++i) {
            buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }

        this.boundary = buf.toString();
    }

    public void writeFirstBoundaryIfNeeds() {
        if(!this.isSetFirst) {
            try {
                this.out.write(("--" + this.boundary + "\r\n").getBytes());
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

        this.isSetFirst = true;
    }

    public void writeLastBoundaryIfNeeds() {
        if(!this.isSetLast) {
            try {
                this.out.write(("\r\n--" + this.boundary + "--\r\n").getBytes());
            } catch (IOException var2) {
                var2.printStackTrace();
            }

            this.isSetLast = true;
        }
    }

    public void addPart(String key, String value) {
        this.writeFirstBoundaryIfNeeds();

        try {
            this.out.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n").getBytes());
            this.out.write(value.getBytes());
            this.out.write(("\r\n--" + this.boundary + "\r\n").getBytes());
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }

    public void addPart(String key, String fileName, InputStream fin, boolean isLast) {
        this.addPart(key, fileName, fin, "application/octet-stream", isLast);
    }

    public void addPart(String key, String fileName, InputStream fin, String type, boolean isLast) {
        this.writeFirstBoundaryIfNeeds();

        try {
            type = "Content-Type: " + type + "\r\n";
            this.out.write(("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
            this.out.write(type.getBytes());
            this.out.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes());
            byte[] e = new byte[4096];
            boolean l = false;

            int l1;
            while((l1 = fin.read(e)) != -1) {
                this.out.write(e, 0, l1);
            }

            if(!isLast) {
                this.out.write(("\r\n--" + this.boundary + "\r\n").getBytes());
            }

            this.out.flush();
        } catch (IOException var16) {
            var16.printStackTrace();
        } finally {
            try {
                fin.close();
            } catch (IOException var15) {
                var15.printStackTrace();
            }

        }

    }

    public void addPart(String key, File value, boolean isLast) {
        try {
            this.addPart(key, value.getName(), new FileInputStream(value), isLast);
        } catch (FileNotFoundException var5) {
            var5.printStackTrace();
        }

    }

    public long getContentLength() {
        this.writeLastBoundaryIfNeeds();
        return (long)this.out.toByteArray().length;
    }

    public Header getContentType() {
        return new BasicHeader("Content-Type", "multipart/form-data; boundary=" + this.boundary);
    }

    public boolean isChunked() {
        return false;
    }

    public boolean isRepeatable() {
        return false;
    }

    public boolean isStreaming() {
        return false;
    }

    public void writeTo(OutputStream outstream) throws IOException {
        outstream.write(this.out.toByteArray());
    }

    public Header getContentEncoding() {
        return null;
    }

    public void consumeContent() throws IOException, UnsupportedOperationException {
        if(this.isStreaming()) {
            throw new UnsupportedOperationException("Streaming entity does not implement #consumeContent()");
        }
    }

    public InputStream getContent() throws IOException, UnsupportedOperationException {
        return new ByteArrayInputStream(this.out.toByteArray());
    }
}

package crawler.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.text.SimpleDateFormat;

// TODO: http version?
/**
 * abstract class, extended by HttpClient and HttpsClients
 * 
 * @author xiaofandou
 *
 */
public abstract class Client {

    protected static final SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    public static String USER_AGENT_HEADER = "User-Agent";
    public static String USER_AGENT = "cis455crawler";
    public static String HOST_HEADER = "Host";

    protected String method;
    protected String host;
    protected String filePath;
    protected int portNum;
    protected String httpVer;

    protected HashMap<String, String> headers;

    protected InputStream in;
    protected int statusCode;

    protected long lastModified;
    protected String contentType;
    protected long contentLength;
    protected boolean invalid;
    protected String charset;
    protected String location;

    /**
     * constructor: set header
     * 
     * @param url
     */
    protected Client(String url) {
        setUp(url);
        headers = new HashMap<>();
        addHeader(USER_AGENT_HEADER, USER_AGENT);
        addHeader(HOST_HEADER, host);
        addHeader("Connection", "close");
        lastModified = -1;
        contentLength = -1;
        charset = "ISO-8859-1";
    }

    public void addHeader(String name, String val) {
        headers.put(name, val);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * factory method
     * 
     * @param url
     * @return
     */
    public static Client getClient(String url) {
        if (url.startsWith("http://"))
            return new HttpClient(url);
        if (url.startsWith("https://"))
            return new HttpsClient(url);
        return null;
    }

    protected void setUp(String url) {
        URLInfo urlInfo = new URLInfo(url);
        filePath = urlInfo.getFilePath();
        host = urlInfo.getHostName();
        // host = "www." + host;
        // System.out.println(host);
        portNum = urlInfo.getPortNo();
        statusCode = -1;
        httpVer = "HTTP/1.1";
        setMethod("GET");
    }

    public static String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int c = in.read();
            if ((char) c == '\n' || c == -1)
                break;
            sb.append((char) c);
        }
        return sb.toString();
    }

    public long getResLastModified() {
        return lastModified;
    }

    public long getResContentLength() {
        return contentLength;
    }

    public String getResContentType() {
        return contentType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setContentType(String s) {
        if (s == null)
            return;
        String[] strs = s.split(";");
        if (strs.length == 1) {
            contentType = s.trim();
        } else {
            contentType = strs[0].trim();
            charset = strs[1].split("=")[1].trim();
        }
    }

    public BufferedReader getBufferedReader() {
        return new BufferedReader(new InputStreamReader(in));
    }

    public InputStream getInputStream() {
        return in;
    }

    public String getCharset() {
        return charset;
    }

    public abstract void close();

    public abstract boolean sendReq();

}

package crawler.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 * client for https protocol
 * 
 * @author xiaofandou
 *
 */
public class HttpsClient extends Client {

    private URL url;
    private HttpsURLConnection con;

    public HttpsClient(String url1) {
        super(url1);
        try {
            url = new URL("https", host, portNum, filePath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            invalid = true;
        }
    }

    /**
     * use URL to connect
     */
    @Override
    public boolean sendReq() {
        if (invalid)
            return false;
        try {
            con = (HttpsURLConnection) url.openConnection();
            con.setConnectTimeout(10000);
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod(method);
            for (String header : headers.keySet()) {
                con.setRequestProperty(header, headers.get(header));
            }
            con.connect();
            statusCode = con.getResponseCode();
            if (statusCode >= 400)
                in = con.getErrorStream();
            else
                in = con.getInputStream();
            lastModified = con.getLastModified();
            contentType = con.getContentType();
            setContentType(contentType);
            contentLength = con.getContentLengthLong();
            location = con.getHeaderField("Location");

            // String redirect = con.getHeaderField("Location");
            // System.out.println("Location: " + redirect);
            // if(redirect != null) {
            // Client c = Client.getClient(redirect);
            // c.setMethod("HEAD");
            // c.sendReq();
            // System.out.println(c.getStatusCode());
            // System.out.println(c.getResLastModified());
            // System.out.println(c.getResContentLength());
            // System.out.println(c.getResContentType());
            // System.out.println(c.getCharset());
            // }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        Client client = new HttpsClient("https://www.piazza.com/");
        client.setMethod("HEAD");
        client.sendReq();
        System.out.println(client.getStatusCode());
        System.out.println(client.getResLastModified());
        System.out.println(client.getResContentLength());
        System.out.println(client.getResContentType());
        System.out.println(client.getCharset());
        // InputStream in = client.getInputStream();
        // String line = null;
        // try {
        // while((line = Client.readLine(in)) != null) {
        // line = line.trim();
        // System.out.println(line);
        // }
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }

    @Override
    public void close() {
        if (con == null)
            return;
        con.disconnect();
    }
}

package crawler.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.util.Date;

import crawler.Crawler;

/**
 * client for http protocol
 * 
 * @author xiaofandou
 *
 */
public class HttpClient extends Client {

    public Socket soc;

    public HttpClient(String url) {
        super(url);
    }

    /**
     * use socket to connect
     */
    @Override
    public boolean sendReq() {
        soc = new Socket();
        try {
            // send request
        	soc.connect(new InetSocketAddress(host, portNum), 5000);
        	soc.setSoTimeout(5000);
            
            in = soc.getInputStream();
            PrintWriter out = new PrintWriter(soc.getOutputStream());
            out.print(this.toString());
            out.flush();

            // init line
            String line = Client.readLine(in);
            if (line == null || line.trim().length() == 0)
                return false;
            String[] strs = line.split("\\s+");
            if (strs.length != 2 && strs.length != 3)
                return false;
            statusCode = Integer.parseInt(strs[1]);
            while ((line = Client.readLine(in)) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    // System.out.println("empty line, ready to break");
                    break;
                }
                String[] header = line.split(":", 2);
                if (header.length != 2)
                    continue;
                switch (header[0].trim().toLowerCase()) {
                case "last-modified":
                    Date d = date.parse(header[1].trim());
                    lastModified = d.getTime();
                    break;
                case "content-length":
                    contentLength = Long.parseLong((header[1].trim()));
                    break;
                case "content-type":
                    setContentType(header[1]);
                    break;
                case "location":
                    location = header[1].trim();
                    break;
                }
            }
        } catch (Exception e) {
//             e.printStackTrace();
            return false;
        }
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(method + " http://" + host + ":" + portNum + filePath + " " + httpVer + "\r\n");
        for (String header : headers.keySet()) {
            sb.append(header + ": " + headers.get(header) + "\r\n");
        }
        sb.append("\r\n");
        return sb.toString();
    }

    @Override
    public void close() {
        try {
            soc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public static void main(String[] args) {
    	
    	String url = "http://www.audible.com";
        Client client = Client.getClient(url);
        client.setMethod("GET");
        client.sendReq();
        System.out.println(url + ": " + client.getStatusCode());
        
        url = "https://www.youtube.com/";
        client = Client.getClient(url);
        client.setMethod("HEAD");
        client.sendReq();
        System.out.println(url + ": " + client.getStatusCode());
        
        url = "http://www.upenn.edu/";
        client = Client.getClient(url);
        client.setMethod("HEAD");
        client.sendReq();
        System.out.println(url + ": " + client.getStatusCode());
        
        url = "https://www.reddit.com/";
        client = Client.getClient(url);
        client.setMethod("HEAD");
        client.sendReq();
        System.out.println(url + ": " + client.getStatusCode());
        
        url = "https://en.wikipedia.org/wiki/Main_Page/";
        client = Client.getClient(url);
        client.setMethod("HEAD");
        client.sendReq();
        System.out.println(url + ": " + client.getStatusCode());
        
        url = "https://www.google.com/";
        client = Client.getClient(url);
        client.setMethod("HEAD");
        client.sendReq();
        System.out.println(url + ": " + client.getStatusCode());
        
        url = "https://www.amazon.com/";
        client = Client.getClient(url);
        client.setMethod("HEAD");
        client.sendReq();
        System.out.println(url + ": " + client.getStatusCode());
        
        url = "http://www.ebay.com/";
        client = Client.getClient(url);
        client.setMethod("HEAD");
        client.sendReq();
        System.out.println(url + ": " + client.getStatusCode());
        
        url = "https://www.bloomberg.com/";
        client = Client.getClient(url);
        client.setMethod("HEAD");
        client.sendReq();
        System.out.println(url + ": " + client.getStatusCode());
        
        url = "http://www.cnn.com/";
        client = Client.getClient(url);
        client.setMethod("HEAD");
        client.sendReq();
        System.out.println(url + ": " + client.getStatusCode());
        
    }
}

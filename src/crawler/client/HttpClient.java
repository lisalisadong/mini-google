package crawler.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.util.Date;

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
            // System.out.println("host: " + host);
            soc.connect(new InetSocketAddress(host, portNum), 10000);
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
            // e.printStackTrace();
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
        Client client = new HttpClient("http://linkedin.com/");
        // System.out.println(client.toString());
        client.sendReq();
        InputStream in = client.getInputStream();
        String line = null;
        try {
            while ((line = Client.readLine(in)) != null) {
                line = line.trim();
                if (line.length() == 0)
                    break;
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

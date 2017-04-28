package crawler.robots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import crawler.client.Client;

public class RobotTxt_old {

    private HashSet<String> allowedLinks;
    private HashSet<String> disallowedLinks;
    private long crawlDelay;
    private boolean empty;
    private boolean self, flag;
    private boolean haveLinks;

    public RobotTxt_old(String url) {
        disallowedLinks = new HashSet<>();
        allowedLinks = new HashSet<>();
        Client client = Client.getClient(url);
        client.setMethod("GET");
        if (client == null || !client.sendReq() || client.getStatusCode() != 200) {
            empty = true;
            return;
        }
        crawlDelay = 0;
        BufferedReader bf = client.getBufferedReader();
        String line;
        try {
            while ((line = bf.readLine()) != null) {
                // System.out.println("line: " + line);
                if (line.length() == 0) {
                    if (self && haveLinks)
                        break;
                    continue;
                }
                String[] strs = line.split(":", 2);
                if (line.startsWith("#") || strs.length != 2) {
                    continue;
                }
                String val = strs[1].trim();
                switch (strs[0].trim().toLowerCase()) {
                case "user-agent":
                    if ("cis455crawler".equals(val)) {
                        self = true;
                        flag = true;
                        empty = false;
                        haveLinks = false;
                        disallowedLinks = new HashSet<>();
                        allowedLinks = new HashSet<>();
                    } else if ("*".equals(val)) {
                        empty = false;
                        if (!self) {
                            flag = true;
                            haveLinks = false;
                        } else {
                            flag = false;
                        }
                    } else {
                        flag = false;
                        self = false;
                    }
                    // System.out.println("user agent is: " + val);
                    break;
                case "disallow":
                    if (flag) {
                        haveLinks = true;
                        disallowedLinks.add(val);
                    }
                    break;
                case "allow":
                    if (flag) {
                        haveLinks = true;
                        allowedLinks.add(val);
                    }
                    break;
                case "crawl-delay":
                    if (flag) {
                        crawlDelay = 1000 * (int) Double.parseDouble(val);
                    }
                    break;
                }
            }

            // System.out.println("=====robot========");
            // System.out.println(toString());
            // System.out.println("==================");
        } catch (IOException e) {
            e.printStackTrace();
            empty = true;
            return;
        }
    }

    public long getCrawlDelay() {
        return crawlDelay;
    }

    public Set<String> getAllowedLinks() {
        return allowedLinks;
    }

    public Set<String> getDisallowedLinks() {
        return disallowedLinks;
    }
    
    public boolean match(String filePath) {
        for (String link : allowedLinks) {
            if (link.charAt(link.length() - 1) == '/' && link.substring(0, link.length() - 1).equals(filePath)) {
                return true;
            }
            if (filePath.startsWith(link))
                return true;
        }

        for (String link : disallowedLinks) {
            // System.out.println("disallowed link: " + link.length() + ", " +
            // link);
            if (link.charAt(link.length() - 1) == '/' && link.substring(0, link.length() - 1).equals(filePath)) {
                return false;
            }
            if (filePath.startsWith(link))
                return false;
        }
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("User-Agent: cis455crawler\n");
        System.out.println("Crawl delay: " + crawlDelay);
        for (String link : allowedLinks) {
            sb.append("Allow: " + link + "\n");
        }
        for (String link : disallowedLinks) {
            sb.append("Disallow: " + link + "\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // RobotTxt r = new RobotTxt("https://piazza.com/robots.txt");
        // System.out.println(r);
        // System.out.println("is allowed: " + r);

        // RobotInfoManager r = new RobotInfoManager();
        // r.getRobotTxt("http://crawltest.cis.upenn.edu/");
        // System.out.println("is allowed: " +
        // r.isAllowed("http://crawltest.cis.upenn.edu/marie/private"));
        // System.out.println("is allowed: " +
        // r.isAllowed("http://crawltest.cis.upenn.edu/foo"));

        // Client c = Client.getClient("http://nba.hupu.com/robots.txt");
        // c.setMethod("GET");
        // c.sendReq();
        // BufferedReader bf = c.getBufferedReader();
        //
        // String line = null;
        // try {
        // while((line = bf.readLine()) != null) {
        // System.out.println(line);
        // }
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }
}

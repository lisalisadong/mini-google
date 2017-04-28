package crawler.robots;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import crawler.client.URLInfo;

/**
 * robot manager class, taking care of the robot rules
 * 
 * @author xiaofandou
 *
 */
public class RobotInfoManager_old {

    // <hostName, *>
    private ConcurrentHashMap<String, RobotTxt> robotMap;
    private ConcurrentHashMap<String, Long> crawlTime;

    public RobotInfoManager_old() {
        robotMap = new ConcurrentHashMap<>();
        crawlTime = new ConcurrentHashMap<>();
    }

    private String getHostName(String url) {
        return new URLInfo(url).getHostName();
    }

    public RobotTxt getRobotTxt(String url) {
        String host = getHostName(url);
        if (robotMap.containsKey(host))
            return robotMap.get(host);

        String robotUrl = host + "/robots.txt";
        if (!robotUrl.startsWith("http://") && !robotUrl.startsWith("https://")) {
            robotUrl = "http://" + robotUrl;
        }

        RobotTxt robotTxt = new RobotTxt(robotUrl);
        // System.out.println(robotTxt);
        robotMap.put(host, robotTxt);
        crawlTime.put(host, -1L);
        return robotMap.get(host);
    }

    /**
     * if a link is allowed by robot rules
     * 
     * @param url
     * @return
     */
    public boolean isAllowed(String url) {
        String host = getHostName(url);
        if (host == null)
            return false;
        RobotTxt robotTxt = getRobotTxt(url);
        if (robotTxt == null)
            return true;

//        String filePath = new URLInfo(url).getFilePath();
//        Set<String> allowedLinks = robotTxt.getAllowedLinks();
//        for (String link : allowedLinks) {
//            if (link.charAt(link.length() - 1) == '/' && link.substring(0, link.length() - 1).equals(filePath)) {
//                return true;
//            }
//            if (filePath.startsWith(link))
//                return true;
//        }
//
//        Set<String> disallowedLinks = robotTxt.getDisallowedLinks();
//        for (String link : disallowedLinks) {
//            // System.out.println("disallowed link: " + link.length() + ", " +
//            // link);
//            if (link.charAt(link.length() - 1) == '/' && link.substring(0, link.length() - 1).equals(filePath)) {
//                return false;
//            }
//            if (filePath.startsWith(link))
//                return false;
//        }
        return true;
    }

    public boolean setHostLastAccessTime(String url) {
        String host = getHostName(url);
        if (robotMap.containsKey(host)) {
            crawlTime.put(host, System.currentTimeMillis());
            return true;
        }
        return false;
    }

    /**
     * get last accessed time
     * 
     * @param url
     * @return
     */
    public long getHostLastAccessTime(String url) {
        String host = getHostName(url);
        if (!crawlTime.containsKey(host))
            return -1;
        return crawlTime.get(host);
    }

    /**
     * is the link allowed to crawl in terms of time
     * 
     * @param url
     * @return
     */
    public boolean timeAllowed(String url) {
        String host = getHostName(url);
        RobotTxt r = robotMap.get(host);
        if (r == null || r.getCrawlDelay() == -1 || !crawlTime.containsKey(host))
            return true;
        return crawlTime.get(host) + r.getCrawlDelay() < System.currentTimeMillis();
    }

    public long getCrawlDelay(String url) {
        RobotTxt r = getRobotTxt(url);
        return r.getCrawlDelay();
    }

    /**
     * wait delay time of the url
     * 
     * @param url
     */
    public void waitUntilAvailable(String url) {
        long t = getCrawlDelay(url);
        while (!timeAllowed(url)) {
            try {
                Thread.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

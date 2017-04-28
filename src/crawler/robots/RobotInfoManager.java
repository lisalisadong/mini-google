package crawler.robots;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import crawler.Crawler;
import crawler.client.URLInfo;
import crawler.utils.LRU;

/**
 * robot manager class, taking care of the robot rules
 * 
 * @author xiaofandou
 *
 */
public class RobotInfoManager {

	public int crawledPageLimit = Crawler.crawledPageLimit;
	
    // <hostName, *>
    private LRU<String, RobotTxt> robotMap;
    private LRU<String, Long> crawlTime;
    private ConcurrentHashMap<String, Integer> crawledPageNum;	//<host, time>

    public RobotInfoManager() {
        robotMap = new LRU<>(10);
        crawlTime = new LRU<>(10);
        
        crawledPageNum = new ConcurrentHashMap<>();
    }

    private String getHostName(String url) {
        return new URLInfo(url).getHostName();
    }

    public RobotTxt getRobotTxt(String url) {
        String host = getHostName(url);
        RobotTxt robotTxt =  robotMap.getValue(host);
        if(robotTxt != null) return robotTxt;

        String robotUrl = host + "/robots.txt";
        if (!robotUrl.startsWith("http://") && !robotUrl.startsWith("https://")) {
            robotUrl = "http://" + robotUrl;
        }

        robotTxt = new RobotTxt(robotUrl);
        // System.out.println(robotTxt);
        robotMap.store(host, robotTxt);
        crawlTime.store(host, -1L);
        return robotTxt;
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
        
        /* return false if exceed the crawl limits */
        Integer num = crawledPageNum.get(host);
        if(num != null && num >= crawledPageLimit) return false;
        
        RobotTxt robotTxt = getRobotTxt(url);
        if (robotTxt == null)
            return true;

        String filePath = new URLInfo(url).getFilePath();
        
        return robotTxt.match(filePath);
        
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
//        return true;
    }

    public boolean setHostLastAccessTime(String url) {
        String host = getHostName(url);
        if (robotMap.contains(host)) {
            crawlTime.store(host, System.currentTimeMillis());
            return true;
        }
        return false;
    }
    
    /**
     * get called when the page is crawled
     * @param url
     */
    public void visit(String url) {
    	String host = getHostName(url);
    	crawledPageNum.put(host, crawledPageNum.getOrDefault(host, 0));
    }

    /**
     * get last accessed time
     * 
     * @param url
     * @return
     */
    public long getHostLastAccessTime(String url) {
        String host = getHostName(url);
        if (!crawlTime.contains(host))
            return -1;
        return crawlTime.getValue(host);
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
        
        Long t = crawlTime.getValue(host);
        
        if (r == null || r.getCrawlDelay() == -1 || t == null)
            return true;
        return t + r.getCrawlDelay() < System.currentTimeMillis();
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

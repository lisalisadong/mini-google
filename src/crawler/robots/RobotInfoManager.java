package crawler.robots;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import crawler.Crawler;
import crawler.client.URLInfo;
import crawler.utils.RobotCache;
import crawler.utils.LRUCache;
import crawler.worker.CrawlerWorker;

/**
 * robot manager class, taking care of the robot rules
 * 
 * @author xiaofandou
 *
 */
public class RobotInfoManager {

	public int crawledPageLimit = Crawler.crawledPageLimit;
	
    // <hostName, *>
    private RobotCache robotInfo;
//    private LRUCache<Long> crawlTime;
    private ConcurrentHashMap<String, Integer> crawledPageNum;	//<host, time>

    public RobotInfoManager() {
        robotInfo = new RobotCache(3000, "./tmp_robot_cache", 3000);
//        crawlTime = new LRUCache<>(65536, Crawler.ROBOT_CACHE_PATH);
        
        crawledPageNum = new ConcurrentHashMap<>();
    }

    private String getHostName(String url) {
        return new URLInfo(url).getHostName();
    }

    public RobotTxt getRobotTxt(String url) {
        String host = getHostName(url);
        RobotTxt robotTxt = robotInfo.get(host);
        if(robotTxt != null) return robotTxt;

        String robotUrl = host + "/robots.txt";
        if (!robotUrl.startsWith("http://") && !robotUrl.startsWith("https://")) {
            robotUrl = "http://" + robotUrl;
        }

        robotTxt = new RobotTxt(robotUrl);
        // System.out.println(robotTxt);
        robotInfo.put(host, robotTxt);
//        crawlTime.put(host, -1L);
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
        
        /* not crawl the host with delay */
        if(robotTxt.getCrawlDelay() > 0) return false;
        	
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
//        String host = getHostName(url);
//        if (robotInfo.get(host) != null) {
//            crawlTime.put(host, System.currentTimeMillis());
//            return true;
//        }
        return false;
    }
    
    /**
     * get called when the page is crawled
     * @param url
     */
    public void visit(String url) {
    	String host = getHostName(url);
    	crawledPageNum.put(host, crawledPageNum.getOrDefault(host, 0) + 1);
    }

    /**
     * get last accessed time
     * 
     * @param url
     * @return
     */
    public long getHostLastAccessTime(String url) {
//        String host = getHostName(url);
//        if (crawlTime.get(host) == null)
//            return -1;
//        return crawlTime.get(host);
    	
    	return -1;
    }

    /**
     * is the link allowed to crawl in terms of time
     * 
     * @param url
     * @return
     */
    public boolean timeAllowed(String url) {
//        String host = getHostName(url);
//        RobotTxt r = robotInfo.get(host);
//        
//        Long t = crawlTime.get(host);
//        
//        if (r == null || r.getCrawlDelay() == -1 || t == null)
//            return true;
//        return t + r.getCrawlDelay() < System.currentTimeMillis();
    	return true;
    }

    public long getCrawlDelay(String url) {
        RobotTxt r = getRobotTxt(url);
        return r.getCrawlDelay();
    }
    
    public long getAvailableTime(String url) {
    	return getHostLastAccessTime(url) + getCrawlDelay(url);
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
    
    public void writeSnapshot() {
    	robotInfo.writeSnapshot();
//    	crawlTime.writeSnapshot();
    }
    
    public static void main(String[] args) {
    	RobotInfoManager rm = new RobotInfoManager();
    	System.out.println("is allowed: " + rm.isAllowed("http://crawltest.cis.upenn.edu/marie/private/"));
    	System.out.println("is allowed: " + rm.isAllowed("http://crawltest.cis.upenn.edu/foo/"));
//    	System.out.println("is allowed: " + rm.isAllowed(url));
//        RobotTxt r = new RobotTxt("https://piazza.com/robots.txt");
//        System.out.println(r);
//        System.out.println("is allowed: " + r);

//        RobotInfoManager r = new RobotInfoManager();
//        r.getRobotTxt("http://crawltest.cis.upenn.edu/");
//        System.out.println("is allowed: " +
//        r.isAllowed("http://crawltest.cis.upenn.edu/marie/private/"));
//        System.out.println("is allowed: " +
//        r.isAllowed("http://crawltest.cis.upenn.edu/foo/"));

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

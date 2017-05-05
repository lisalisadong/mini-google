package crawler.robots;

import crawler.Crawler;
import crawler.client.URLInfo;
import crawler.storage.DBWrapper;

public class RobotManager {

	public DBWrapper db;
	
	public RobotManager(String DBPath) {
		db = new DBWrapper(DBPath);
		db.setup();
		System.out.println("Robot info cache: " + db.rIdx.map().size());
	}
	
	private String getHostName(String url) {
        return new URLInfo(url).getHostName();
    }
	
	public RobotTxt getRobotTxt(String url) {
        String host = getHostName(url);
        RobotTxt rt = db.getRobotTxt(host);
        if(rt != null) return rt;

        String robotUrl = host + "/robots.txt";
        if (!robotUrl.startsWith("http://") && !robotUrl.startsWith("https://")) {
            robotUrl = "http://" + robotUrl;
        }

        rt = new RobotTxt(robotUrl);
        db.saveRobotTxt(rt);
//        db.sync();
        return rt;
    }
	
	/**
     * if a link is allowed by robot rules
     * 
     * @param url
     * @return
     */
    public boolean isAllowed(String url) {
    	String host = getHostName(url);
        if (host == null || host.startsWith("mailto"))
            return false;
        
//        /* return false if exceed the crawl limits */
//        Integer num = crawledPageNum.get(host);
//        if(num != null && num >= crawledPageLimit) return false;
        
        RobotTxt robotTxt = getRobotTxt(url);
        if (robotTxt == null)
            return true;
        
        /* not crawl the host with delay */
        if(robotTxt.getCrawlDelay() > 0) return false;
        	
        String filePath = new URLInfo(url).getFilePath();
        
        return robotTxt.match(filePath);
    }
    
    public void writeSnapshot() {
    	db.sync();
    	synchronized(db.rIdx) {
    		System.out.println("Robot txt size: " + db.rIdx.map().size());
    	}
    }
}

package crawler.due;

import java.util.HashMap;
import java.util.Map;

import crawler.Crawler;
import crawler.robots.RobotInfoManager;
import crawler.robots.RobotTxt;
import crawler.storage.DBWrapper;
import crawler.storage.VisitedURL;
import crawler.utils.Node;
import utils.Logger;

public class URLSet {
	
	static Logger logger = new Logger(URLSet.class.getName());
	
	public DBWrapper db;
    
    public URLSet (int capacity, String DBPath, int numToWriteSnapshot) {
    	db = new DBWrapper(DBPath);
    	db.setup();
    	System.out.println("[URL Set] size of last crawl: " + db.vIdx.map().size());
    }
    
    public URLSet(int capacity, String DBPath) {
    	this(capacity, DBPath, 10000);
    }
    
    
    public synchronized boolean addURL(String url) {
    	if(db.getVisitedURL(url) != null) return false;
    	db.saveVisitedURL(new VisitedURL(url, 1L));
    	return true;
    }
    
    public synchronized void writeSnapshot() {
    	db.sync();
    }
    
    public static void main(String[] args) {
    	
//    	URLSet us = new URLSet(10, Crawler.URL_SET_CACHE_PATH, 1000);
//    	us.addURL("a");
//    	us.addURL("b");
//    	us.addURL("c");
//    	us.addURL("d");
//    	us.addURL("e");
//    	us.writeSnapshot();
    	
    }

}

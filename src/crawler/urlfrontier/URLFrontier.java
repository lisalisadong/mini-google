package crawler.urlfrontier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import crawler.Crawler;
import crawler.client.URLInfo;
import crawler.robots.RobotInfoManager;
import crawler.storage.DBWrapper;
import crawler.storage.URLQueue;
import utils.Logger;

/**
 * the url frontier, takes care of the synchronization
 * @author xiaofandou
 *
 */
public class URLFrontier {
	
	public static Logger logger = new Logger(URLFrontier.class.getName());
	
	public Long id = 0L;
	public int maxSize = 4096;
    
    public LinkedBlockingQueue<String> inQueue;
    public LinkedBlockingQueue<String> outQueue;
    
    private DBWrapper db;

    public URLFrontier(int maxSize, String DBPath) {
    	this.maxSize = maxSize;
    	
//    	db = new DBWrapper(DBPath);
    	db = new DBWrapper("./tmp");
    	db.setup();
    	
    	inQueue = new LinkedBlockingQueue<String>();
    	outQueue = new LinkedBlockingQueue<String>();
    	
    	config();
    }
    
    /* RESTORE FROM DB */
    public void config() {
    	
    	if(db.uwIdx.map().isEmpty()) {
    		System.out.println("start from fresh");
    		addURL("https://www.facebook.com/");
    		addURL("http://www.upenn.edu/");
    		addURL("https://en.wikipedia.org/wiki/Main_Page/");
    		addURL("https://www.amazon.com/");
    		addURL("http://www.cnn.com/");
    		return;
    	}
    	
    	logger.debug("restore from db");
    	System.out.println("restore from db");
    	
    	
    }

    /**
     * the url is guaranteed available to access
     * 
     * @return next available url, null if empty
     */
    public String getNextURL() {
    	
    	if(outQueue.isEmpty()) {
    		readFromDB();
    	}
    	return outQueue.poll();	
    }
    
    private synchronized void readFromDB() {
    	
    	if(!inQueue.isEmpty()) writeInQueueToDB();
    	
    	db.pullURL(2000, outQueue);
    }
    
    private synchronized void writeInQueueToDB() {
		db.saveURL(inQueue);
		db.sync();
	}

	/**
     * add an url to frontier
     * 
     * @param url
     */
    public void addURL(String url) {
    	inQueue.offer(url);
    	if(inQueue.size() > maxSize) {
    		writeInQueueToDB();
    	}
    }
    
    public synchronized void writeSnapshot() {
    	System.out.println("[URL Frontier]: write snapshot");
    	db.saveURL(inQueue);
    	db.saveURL(outQueue);
    	db.sync();
    }
    
    public static void main(String[] args) {
    	URLFrontier uf = new URLFrontier(5000, "./tmp");
    	String url = "";
    	while(url != null) {
    		System.out.println(url);
    		url = uf.getNextURL();
    	}
//    	uf.addURL("abc1");
//    	uf.addURL("abc2");
//    	uf.addURL("abc3");
//    	uf.addURL("abc4");
//    	uf.addURL("abc5");
//    	uf.writeSnapshot();
    }
}





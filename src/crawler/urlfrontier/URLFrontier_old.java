package crawler.urlfrontier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;

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
public class URLFrontier_old {
	
//	public static Logger logger = new Logger(URLFrontier.class.getName());
//	
//	public static int maxSize = 4096;
//	
//	public static String QUEUE_ID_1 = "QUEUE_ID_1";
//	public static String QUEUE_ID_2 = "QUEUE_ID_2";
//	public static String QUEUE_ID_3 = "QUEUE_ID_3";
//	
//	public static int LEVEL_ONE = 2000;
//	public static int LEVEL_TWO = 4000;
//    
//    private int size;
//    private Random rand;
//    
//    public HashMap<String, Integer> hostTimeMap;
//    
//    public LinkedList<String>[] urls;
//
//    public URLFrontier_old () {
//    	
//    	rand = new Random();
//    	urls = new LinkedList[3];
//    	for(int i = 0; i < 3; i++) {
//    		urls[i] = new LinkedList<>();
//    	}
//    	
//    	hostTimeMap = new HashMap<>();
//    }
//    
//    /* RESTORE FROM DB */
//    public void config(DBWrapper db) {
//    	
//    	URLQueue q1 = db.getURLQueue(QUEUE_ID_1);
//    	
//    	/* no state start from fresh */
//    	if(q1 == null) {
//    		logger.debug("start from fresh");
//    		System.out.println("start from fresh");
//    		addURL("http://crawltest.cis.upenn.edu/");
//        	addURL("https://piazza.com/");
//        	addURL("https://www.reddit.com/");
//        	addURL("http://www.cnn.com/");
//        	return;
//    	}
//    	
//    	logger.debug("restore from db");
//    	System.out.println("restore from db");
//    	urls[0] = q1.getQueue();
//    	urls[1] = db.getURLQueue(QUEUE_ID_2).getQueue();
//    	urls[2] = db.getURLQueue(QUEUE_ID_3).getQueue();
//    	
//    	for(int i = 0; i < urls.length; i++) {
//    		System.out.println("url " + i + ": " + urls[i]);
//    	}
//    }
//
//    /**
//     * the url is guaranteed available to access
//     * 
//     * @return next available url, null if empty
//     */
//    public synchronized String getNextURL() {
//    	int idx = getIdx();
//    	if(!urls[idx].isEmpty()) return urls[idx].poll();
//    	
//    	idx = (idx - 1 + 3) % 3;
//    	if(!urls[idx].isEmpty()) {
//    		return urls[idx].poll();
//    	}
//    	
//    	idx = (idx - 1 + 3) % 3;
//    	return urls[idx].poll();
//    }
//    
//    private int getIdx() {
//    	int num = rand.nextInt(16);
//    	if(6 < num) return 2;
//    	if(1 < num) return 1;
//    	return 0;
//    }
//
//    /**
//     * add an url to frontier
//     * 
//     * @param url
//     */
//    public synchronized void addURL(String url) {
//    	int priority = getPriority(url);
//    	urls[priority].offer(url);
//    }
//    
//    public int getPriority(String url) {
//    	String host = new URLInfo(url).getHostName();
//    	Integer time = hostTimeMap.get(host);
//    	if(time == null) {
//    		hostTimeMap.put(host, 1);
//    		return 2;
//    	}
//    	hostTimeMap.put(host, time + 1);
//    	if(time < LEVEL_ONE) return 2;
//    	if(time < LEVEL_TWO) return 1;
//    	return 0;
//    }
//    
//    public synchronized void writeSnapshot(DBWrapper db) {
//    	System.out.println("[URL Frontier] writing snapshot...");
////    	System.out.println("0: " + urls[0]);
////    	System.out.println("1: " + urls[1]);
////    	System.out.println("2: " + urls[2]);
////    	db.saveURLQueue(new URLQueue(QUEUE_ID_1, urls[0]));
////    	db.saveURLQueue(new URLQueue(QUEUE_ID_2, urls[1]));
////    	db.saveURLQueue(new URLQueue(QUEUE_ID_3, urls[2]));
//    	
//    	System.out.println("[URL Frontier] finished writing snapshot...");
//    }
//    
//    public synchronized int size() {
//    	return size;
//    }
//    
//    public static void main(String[] args) {
//    	DBWrapper db = new DBWrapper("./url_cache0");
//    	db.setup();
////    	System.out.println(db.getURLQueue(URLFrontier.QUEUE_ID_1).getQueue());
////    	System.out.println(db.getURLQueue(URLFrontier.QUEUE_ID_2).getQueue());
////    	System.out.println(db.getURLQueue(URLFrontier.QUEUE_ID_3).getQueue());
//    	for(String id: db.qIdx.map().keySet()) {
//    		System.out.println(id + ": ");
//    		System.out.println(db.getURLQueue(id).getQueue());
//    	}
//    	
////    	for(String id: db.cIdx.map().keySet()) {
////    		System.out.println(id + ": ");
//////    		System.out.println(db.getURLQueue(id).getQueue());
////    	}
//    	
//    }
}





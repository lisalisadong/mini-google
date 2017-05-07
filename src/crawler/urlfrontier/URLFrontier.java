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
import crawler.worker.CrawlerWorker;
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
    	
    	db = new DBWrapper(DBPath);
//    	db = new DBWrapper("./tmp");
    	db.setup();
    	
    	inQueue = new LinkedBlockingQueue<String>();
    	outQueue = new LinkedBlockingQueue<String>();
    	
    	config();
    }
    
    /* RESTORE FROM DB */
    public void config() {
    	
    	if(db.uwIdx.map().isEmpty()) {
    		System.out.println("[URL Frontier] new crawl");
    		
    		if(CrawlerWorker.WORKER_ID.equals("1")) {
    			addURL("https://www.facebook.com/");
    			addURL("https://www.youtube.com/");
    			
    			System.out.println("[seed]: https://www.facebook.com/");
    			System.out.println("[seed]: https://www.youtube.com/");
    		} else if(CrawlerWorker.WORKER_ID.equals("2")) {
    			addURL("http://www.upenn.edu/");
    			addURL("https://www.reddit.com/");
    			
    			System.out.println("[seed]: http://www.upenn.edu/");
    			System.out.println("[seed]: https://www.reddit.com/");
    		} else if(CrawlerWorker.WORKER_ID.equals("3")) {
    			addURL("https://en.wikipedia.org/wiki/Portal:Contents");
    			addURL("https://www.google.com/");
    			
    			System.out.println("[seed]: https://en.wikipedia.org/wiki/Portal:Contents");
    			System.out.println("[seed]: https://www.google.com/");
    		} else if(CrawlerWorker.WORKER_ID.equals("4")) {
    			addURL("https://www.amazon.com/");
    			addURL("http://www.ebay.com/");
    			
    			System.out.println("[seed]: https://www.amazon.com/");
    			System.out.println("[seed]: http://www.ebay.com/");
    		} else if(CrawlerWorker.WORKER_ID.equals("5")) {
    			addURL("https://www.bloomberg.com/");
    			addURL("http://www.cnn.com/");
    			
    			System.out.println("[seed]: https://www.bloomberg.com/");
    			System.out.println("[seed]: http://www.cnn.com/");
    		} else if(CrawlerWorker.WORKER_ID.equals("6")) {
    			addURL("http://stackoverflow.com/");
    			addURL("https://www.cloudera.com/");
    			
    			System.out.println("[seed]: http://stackoverflow.com/");
    			System.out.println("[seed]: https://www.cloudera.com/");
    		} else {
    			addURL("https://www.facebook.com/");
    		}
    		
    		return;
    	}

//    	addURL("http://www.upenn.edu/");
//		addURL("https://www.reddit.com/");
//		System.out.println("[seed]: http://www.upenn.edu/");
//		System.out.println("[seed]: https://www.reddit.com/");
//		addURL("https://en.wikipedia.org/wiki/Main_Page/");
//		addURL("https://www.google.com/");
//		System.out.println("[seed]: https://en.wikipedia.org/wiki/Main_Page/");
//		System.out.println("[seed]: https://www.google.com/");
//		addURL("https://www.amazon.com/");
//		addURL("http://www.ebay.com/");
//		System.out.println("[seed]: https://www.amazon.com/");
//		System.out.println("[seed]: http://www.ebay.com/");
//		addURL("https://www.bloomberg.com/");
//		addURL("http://www.cnn.com/");
//		System.out.println("[seed]: https://www.bloomberg.com/");
//		System.out.println("[seed]: http://www.cnn.com/");
		
    	logger.debug("restore from db");
    	System.out.println("[URL Frontier] restore from last crawl: " 
    			+ db.uwIdx.map().size());
    	
    	
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
    
    private void readFromDB() {
    	writeQueueToDB(inQueue);
    	
    	synchronized(outQueue) {
    		int i = 0;
    		for(String url: db.uwIdx.map().keySet()) {
    			db.deleteURL(url);
    			outQueue.add(url);
    			i++;
    			if(i == 1000) break;
    		}
    	}
    }
    
    private void writeQueueToDB(LinkedBlockingQueue<String> queue) {
    	synchronized(queue) {
    		String url = "";
    		while(!queue.isEmpty()) {
    			url = queue.poll();
    			db.saveURL(url);
    		}
        	db.sync();
    	}
	}

	/**
     * add an url to frontier
     * 
     * @param url
     */
    public void addURL(String url) {
    	inQueue.offer(url);
    	if(inQueue.size() > maxSize) {
    		writeQueueToDB(inQueue);
    	}
    }
    
    public void writeSnapshot() {
    	System.out.println("[URL Frontier]: write snapshot");
    	writeQueueToDB(inQueue);
    	writeQueueToDB(outQueue);
    	db.sync();
    	System.out.println(db.getPath() + ": " + db.uwIdx.map().size());
    }
    
    public static void main(String[] args) {
    	URLFrontier uf = new URLFrontier(1, "./tmp");
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





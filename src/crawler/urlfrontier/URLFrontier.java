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
    			addURL("https://www.quora.com/");
    			
    			System.out.println("[seed]: https://www.quora.com/");
    			System.out.println("[seed]: https://www.facebook.com/");
    			System.out.println("[seed]: https://www.youtube.com/");
    		} else if(CrawlerWorker.WORKER_ID.equals("2")) {
    			addURL("http://www.upenn.edu/");
    			addURL("http://www.cis.upenn.edu/~ahae/");
    			addURL("https://www.yahoo.com/");

    			System.out.println("[seed]: http://www.cis.upenn.edu/~ahae/");
    			System.out.println("[seed]: https://www.yahoo.com/");
    			System.out.println("[seed]: http://www.upenn.edu/");
    		} else if(CrawlerWorker.WORKER_ID.equals("3")) {
    			addURL("https://www.google.com/");
    			addURL("https://www.whatsapp.com/");
    			addURL("https://philadelphia.craigslist.org/");

    			System.out.println("[seed]: https://philadelphia.craigslist.org/");
    			System.out.println("[seed]: https://www.whatsapp.com/");
    			System.out.println("[seed]: https://www.google.com/");
    		} else if(CrawlerWorker.WORKER_ID.equals("4")) {
    			addURL("https://www.amazon.com/");
    			addURL("https://www.netflix.com/");
    			addURL("http://www.imdb.com/");
    			
    			System.out.println("[seed]: http://www.imdb.com/");
    			System.out.println("[seed]: https://www.netflix.com/");
    			System.out.println("[seed]: https://www.amazon.com/");
    		} else if(CrawlerWorker.WORKER_ID.equals("5")) {
    			addURL("https://www.bloomberg.com/");
    			addURL("http://www.cnn.com/");
    			addURL("https://www.walmart.com/");
    			
    			System.out.println("[seed]: https://www.walmart.com/");
    			System.out.println("[seed]: https://www.bloomberg.com/");
    			System.out.println("[seed]: http://www.cnn.com/");
    		} else if(CrawlerWorker.WORKER_ID.equals("6")) {
    			addURL("http://stackoverflow.com/");
    			addURL("https://www.cloudera.com/");
    			addURL("https://www.yelp.com/");
    			addURL("http://www.accuweather.com/");
    			
    			System.out.println("[seed]: http://www.accuweather.com/");
    			System.out.println("[seed]: https://www.yelp.com/");
    			System.out.println("[seed]: http://stackoverflow.com/");
    			System.out.println("[seed]: https://www.cloudera.com/");
    		} else if(CrawlerWorker.WORKER_ID.equals("7")) {
    			addURL("http://www.oracle.com/index.html");
    			addURL("http://www.espn.com/");
    			addURL("http://movielikers.net/");
    			
    			System.out.println("[seed]: http://movielikers.net/");
    			System.out.println("[seed]: http://www.espn.com/");
    			System.out.println("[seed]: http://www.oracle.com/index.html");
    		} else if(CrawlerWorker.WORKER_ID.equals("8")) {
    			addURL("https://www.reddit.com/");
    			addURL("https://www.pinterest.com/");
    			
    			System.out.println("[seed]: https://www.pinterest.com/");
    			System.out.println("[seed]: https://www.reddit.com/");
    		} else if(CrawlerWorker.WORKER_ID.equals("9")) {
    			addURL("http://www.ebay.com/");
    			addURL("https://www.apple.com/");

    			System.out.println("[seed]: https://www.apple.com/");
    			System.out.println("[seed]: http://www.ebay.com/");
    		} else if(CrawlerWorker.WORKER_ID.equals("10")) {
    			addURL("https://en.wikipedia.org/wiki/Portal:Contents");
    			addURL("https://www.nytimes.com/");
    			addURL("http://www.bing.com/");
    			
    			System.out.println("[seed]: http://www.bing.com/");
    			System.out.println("[seed]: https://www.nytimes.com/");
    			System.out.println("[seed]: https://en.wikipedia.org/wiki/Portal:Contents");
    		} else if(CrawlerWorker.WORKER_ID.equals("11")) {
    			addURL("https://www.aol.com/");
    			addURL("http://popurls.com/");
    			addURL("https://philadelphia.craigslist.org/");
    			
    			System.out.println("[seed]: https://www.aol.com/");
    			System.out.println("[seed]: http://popurls.com/");
    			System.out.println("[seed]: https://philadelphia.craigslist.org/");
    		} else if(CrawlerWorker.WORKER_ID.equals("12")) {
    			addURL("https://en.wikipedia.org/wiki/List_of_most_popular_websites");
    			addURL("https://en.wikipedia.org/wiki/Main_Page");
    			addURL("https://en.wikipedia.org/wiki/Tom_Cruise");
    			addURL("https://en.wikipedia.org/wiki/Apple_Inc.");
    			addURL("https://en.wikipedia.org/wiki/Alan_Turing");

    			System.out.println("[seed]: https://en.wikipedia.org/wiki/List_of_most_popular_websites");
    			System.out.println("[seed]: https://en.wikipedia.org/wiki/Tom_Cruise");
    			System.out.println("[seed]: https://en.wikipedia.org/wiki/Main_Page");
    			System.out.println("[seed]: https://en.wikipedia.org/wiki/Alan_Turing");
    			System.out.println("[seed]: https://en.wikipedia.org/wiki/Apple_Inc.");
    			
    		}  else {
    			addURL("https://www.facebook.com/");
    		}
    		
    		return;
    	}
		
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





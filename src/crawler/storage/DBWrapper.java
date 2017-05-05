package crawler.storage;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import java.util.LinkedList;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import crawler.Crawler;
import crawler.robots.RobotInfoManager;
import crawler.robots.RobotTxt;
import crawler.urlfrontier.URLFrontier;
import crawler.utils.CacheEntry;

@SuppressWarnings("rawtypes")
public class DBWrapper {

    public static String envDir = null;

    private static Environment myEnv;
    private static EntityStore store;

    public PrimaryIndex<String, CrawledPage> pIdx;
    public PrimaryIndex<String, RobotTxt> rIdx;
    
	public PrimaryIndex<String, URLQueue> qIdx;
    public PrimaryIndex<String, VisitedURL> vIdx;
    public PrimaryIndex<String, URLWrapper> uwIdx;
	
    public DBWrapper(String dir) {
        envDir = dir;
        File f = new File(envDir);
        if (!f.isDirectory())
            f.mkdir();
    }

    // The setup() method opens the environment and store for us.
    public void setup() throws DatabaseException {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setLockTimeout(1000, TimeUnit.MILLISECONDS);
        StoreConfig storeConfig = new StoreConfig();
        envConfig.setAllowCreate(true);
        storeConfig.setAllowCreate(true);
        // Open the environment and entity store
        myEnv = new Environment(new File(envDir), envConfig);
        store = new EntityStore(myEnv, "EntityStore", storeConfig);
        
        pIdx = store.getPrimaryIndex(String.class, CrawledPage.class);
        rIdx = store.getPrimaryIndex(String.class, RobotTxt.class);
        qIdx = store.getPrimaryIndex(String.class, URLQueue.class);
        vIdx = store.getPrimaryIndex(String.class, VisitedURL.class);
        uwIdx = store.getPrimaryIndex(String.class, URLWrapper.class);
    }

    public String getPath() {
        return envDir;
    }

    public void savePage(CrawledPage page) {
        pIdx.put(page);
    }

    public CrawledPage getPage(String url) {
        return pIdx.get(url);
    }
    
    public RobotTxt getRobotTxt(String id) {
    	return rIdx.get(id);
    }
    
    public void saveRobotTxt(RobotTxt r) {
    	rIdx.put(r);
//    	sync();
    }
    
    public VisitedURL getVisitedURL(String url) {
    	return vIdx.get(url);
    }
    
    public void saveVisitedURL(VisitedURL url) {
//    	System.out.println("[DB] save: " + url.getUrl());
    	vIdx.put(url);
    	sync();
    }
    
    public void deleteURL(String url) {
    	uwIdx.delete(url);
    }
    
    
    // for url frontier
    public void saveURL(Queue<String> queue) {
    	while(!queue.isEmpty()) {
    		String url = queue.poll();
    		uwIdx.put(new URLWrapper(url));
    		System.out.println("[DB] save url: " + url);
    	}
    }
    
    public void pullURL(int num, Queue<String> outQueue) {
		for(String url: uwIdx.map().keySet()) {
			outQueue.offer(url);
			uwIdx.delete(url);
		}
		sync();
	}
    
    public void sync() {
        if (myEnv != null)
            myEnv.sync();
        if (store != null)
            store.sync();
    }

    public void close() {
        if (myEnv != null)
            myEnv.close();
        if (store != null)
            store.close();
    }

    public static void main(String[] args) {
         DBWrapper db = new DBWrapper("./url_cache");
         db.setup();
         
         for(String s: db.vIdx.map().keySet()) {
        	 System.out.println(s);
         }
         
//         RobotInfoManager rm = new RobotInfoManager();
//         
//         RobotTxt r = rm.getRobotTxt("http://crawltest.cis.upenn.edu/");
//         db.saveRobotTxt(r);
         
         
        // List<Channel> channels = db.getAllChannels();
        // for(Channel c: channels) {
        // System.out.println("Channel: " + c.getName());
        // for(String url: c.getLinks()) {
        // System.out.println(" url: " + url);
        // CrawledPage page = db.getPage(url);
        // System.out.println(" content: " + page.getContent());
        //
        // }
        // }

        // db.saveUser(new User("user1", "123"));

        // Client c = Client.getClient("http://crawltest.cis.upenn.edu/bbc/");
        // c.setMethod("GET");
        // c.sendReq();
        // CrawledPage p = new CrawledPage(c,
        // "http://crawltest.cis.upenn.edu/bbc/");
        // db.savePage(p);

        // System.out.println(db.getPage("http://crawltest.cis.upenn.edu/bbc/")
        // == null);

        // System.out.println("get user1: " + db.getUser("user1"));
        // System.out.println("get user2: " + db.getUser("user2"));
    }

}

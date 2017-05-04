package crawler.storage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.LinkedList;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import crawler.robots.RobotTxt;
import crawler.urlfrontier.URLFrontier;
import crawler.utils.CacheEntry;

public class DBWrapper {

    public static String envDir = null;

    private static Environment myEnv;
    private static EntityStore store;

    public PrimaryIndex<String, CrawledPage> pIdx;
    
    @SuppressWarnings("rawtypes")
    public PrimaryIndex<String, CacheEntry> cIdx;

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
        cIdx = store.getPrimaryIndex(String.class, CacheEntry.class);
    }

    public String getPath() {
        return envDir;
    }

    public void savePage(CrawledPage page) {
        pIdx.put(page);
        sync();
    }

    public CrawledPage getPage(String url) {
        return pIdx.get(url);
    }
    
    @SuppressWarnings("rawtypes")
    public void saveCacheEntry(CacheEntry r) {
    	cIdx.put(r);
    	sync();
    }
    
    @SuppressWarnings("rawtypes")
	public CacheEntry getCacheEntry(String key) {
    	return cIdx.get(key);
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
    
    public void clear() {
    	List<CrawledPage> res = new LinkedList<>();
//		 EntityCursor<Channel> channelCursor = cIdx.entities();
//			Iterator<Channel> iterator = channelCursor.iterator();
//			while (iterator.hasNext())
//				res.add(iterator.next());
		 Map<String, CrawledPage> map = pIdx.map();
		 for(String key: map.keySet()) {
			 pIdx.delete(key);
		 }
		 sync();
    }

    public static void main(String[] args) {
        // DBWrapper db = new DBWrapper("./db");
        // db.setup();
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

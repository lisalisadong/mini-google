package crawler.storage;

import java.util.concurrent.TimeUnit;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import crawler.urlfrontier.URLFrontier;

public class StateDBWrapper {

    public static String envDir = null;

    private static Environment myEnv;
    private static EntityStore store;
    
    public PrimaryIndex<String, URLFrontier> uIdx;
//    public PrimaryIndex<String, CrawledPage> uIdx;

    public StateDBWrapper(String dir) {
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
        
        uIdx = store.getPrimaryIndex(String.class, URLFrontier.class);
    }

    public String getPath() {
        return envDir;
    }
    
    public void saveURLFrontier(URLFrontier urlFrontier) {
    	uIdx.put(urlFrontier);
    }
    
    public URLFrontier getURLFrontier() {
    	return uIdx.get(URLFrontier.ID);
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
    	
    }

    public static void main(String[] args) {
    	
    }
}

package crawler.stormlite.bolt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.LinkedList;
import java.util.Iterator;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.EntityCursor;

public class DBWrapper {
	
	public static String envDir = null;
	
	private static Environment myEnv;
	private static EntityStore store;
	
	public PrimaryIndex<String, Entry> eIdx;
	
	public DBWrapper(String dir) {
		envDir = dir;
		File f = new File(envDir);
		if(!f.isDirectory()) f.mkdir();
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
		 eIdx = store.getPrimaryIndex(String.class, Entry.class);
	 }
	 
	 public String getPath() {
		 return envDir;
	 }
	 
	 public void saveEntry(Entry e) {
		 eIdx.put(e);
		 sync();
	 }
	 
	 public Entry getEntry(String key) {
		 return eIdx.get(key);
	 }
	 
	 public void clearDB() {
		 Map<String, Entry> map = eIdx.map();
		 for(String key: map.keySet()) {
			 eIdx.delete(key);
		 }
		 sync();
	 }
	 
	 public void show() {
		 System.out.println("Database: ");
		 Map<String, Entry> map = eIdx.map();
		 for(String key: map.keySet()) {
			 System.out.println("key: " + key);
		 }
	 }
	 
	 public void sync() {
		 if(myEnv != null) myEnv.sync();
		 if(store != null) store.sync();
	 }
	 
	 public void close() {
		 if(myEnv != null) myEnv.close();
		 if(store != null) store.close();
	 }
	 
	
	public static void main(String[] args) {

	}
}

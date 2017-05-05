package crawler.utils;

import java.util.HashMap;

import crawler.robots.RobotInfoManager;
import crawler.robots.RobotTxt;
import crawler.storage.DBWrapper;
import utils.Logger;

/**
 * 
 * @author xiaofandou
 *
 * @param <T> T has to be marked as Persistent
 */
public class LRUCache<T> {
	
//	static Logger logger = new Logger(LRUCache.class.getName());
//	
//	public DBWrapper db;
//    
//	public HashMap<String, Node<T>> map;
//	public Node<T> head;
//	public Node<T> tail;
//	public int cap;
//	public int opNum;
//	public int numToWriteSnapshot;
//    
//    public LRUCache (int capacity, String DBPath, int numToWriteSnapshot) {
//    	
//    	opNum = 0;
//    	this.numToWriteSnapshot = numToWriteSnapshot;
//        cap = capacity;
//        head = new Node<>(null, null);
//        tail = new Node<>(null, null);
//        head.next = tail;
//        tail.prev = head;
//        map = new HashMap<>();
//        
//        db = new DBWrapper(DBPath);
//        db.setup();
//    }
//    
//    public LRUCache(int capacity, String DBPath) {
//    	this(capacity, DBPath, 1000);
//    }
//    
//    @SuppressWarnings("unchecked")
//	public synchronized T get(String key) {
//    	if(map.containsKey(key)) {
//    		Node<T> n = map.get(key);
//            detach(n);
//            moveToHead(n);
//            return n.entry.val;
//    	}
//    	
//    	// in DB
//    	CacheEntry<T> c = db.getCacheEntry(key);
//    	if(c != null) {
//    		Node<T> n = new Node<>(key, c);
//    		if(cap == map.size()) evict();
//    		map.put(key, n);
//    		moveToHead(n);
//    	}
//        return c == null? null: c.val;
//    }
//    
//    public synchronized void put(String key, T value) {
//        if(cap <= 0) return;
//        
//        if(get(key) != null) {
//            map.get(key).entry.val = value;
//            return;
//        }
//        
//        opNum++;
//        CacheEntry<T> e = new CacheEntry<>(key, value);
//        Node<T> n = new Node<>(key, e);
//        if(map.size() == cap) {
//            evict();
//        }
//        moveToHead(n);
//        map.put(key, n);
//        
//        if(opNum == numToWriteSnapshot) {
//        	opNum = 0;
//        	writeSnapshot();
//        }
//    }
//    
//    private void detach(Node<T> e) {
//        e.prev.next = e.next;
//        e.next.prev = e.prev;
//    }
//    
//    private void moveToHead(Node<T> e) {
//        e.prev = head;
//        e.next = head.next;
//        head.next.prev = e;
//        head.next = e;
//    }
//    
//    private void evict() {
//        if(tail.prev == head) return;
//        Node<T> e = tail.prev;
//        e.prev.next = tail;
//        tail.prev = e.prev;
//        map.remove(e.key);
//        db.saveCacheEntry(e.entry);
////        db.sync();
////        
//        System.out.println("cap reached, evict: " + e.key);
//    }
//    
//    public synchronized void writeSnapshot() {
////    	System.out.println("write snapshot");
//    	for(Node<T> n: map.values()) {
////			System.out.println("write: " + n.key);
//    		db.saveCacheEntry(n.entry);
//    	}
//    	db.sync();
//    }
//    
//    public static void main(String[] args) {
//    	
//    	DBWrapper db = new DBWrapper("./tmp");
//    	db.setup();
//    	
//    	RobotInfoManager rm = new RobotInfoManager();
//        
//    	String key = "key";
//        RobotTxt r = rm.getRobotTxt("http://crawltest.cis.upenn.edu/");
//        
//        CacheEntry<RobotTxt> ce = new CacheEntry<>(key, r);
//        db.saveCacheEntry(ce);
//    	
//        System.out.println(db.getCacheEntry(key).val);
//    }
}
















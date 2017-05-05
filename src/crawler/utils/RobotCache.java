package crawler.utils;

import java.util.HashMap;

import crawler.robots.RobotInfoManager;
import crawler.robots.RobotTxt;
import crawler.storage.DBWrapper;
import utils.Logger;

public class RobotCache {
	
	static Logger logger = new Logger(RobotCache.class.getName());
	
	public DBWrapper db;
    
	public HashMap<String, Node<RobotTxt>> map;
	public Node<RobotTxt> head;
	public Node<RobotTxt> tail;
	public int cap;
	public int opNum;
	public int numToWriteSnapshot;
    
    public RobotCache (int capacity, String DBPath, int numToWriteSnapshot) {
    	
    	opNum = 0;
    	this.numToWriteSnapshot = numToWriteSnapshot;
        cap = capacity;
        head = new Node<>(null, null);
        tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
        map = new HashMap<>();
        
        db = new DBWrapper(DBPath);
        db.setup();
        
        System.out.println("[robot cache] restore from " + DBPath);
        int loadSize = (int)(cap * 0.75);
        int num = 0;
        for(String id: db.rIdx.map().keySet()) {
        	System.out.println("[robot cache] got " + id);
        	num++;
        	put(id, db.getRobotTxt(id));
        	if(num == loadSize) break;
        }
        System.out.println("[robot cache] finish restore from " + DBPath);
    }
    
    public RobotCache(int capacity, String DBPath) {
    	this(capacity, DBPath, 1000);
    }
    
	public synchronized RobotTxt get(String key) {
    	if(map.containsKey(key)) {
    		Node<RobotTxt> n = map.get(key);
            detach(n);
            moveToHead(n);
            return n.val;
    	}
    	
    	// in DB
    	RobotTxt r = db.getRobotTxt(key);
    	if(r != null) {
    		if(cap == map.size()) evict();
    		Node<RobotTxt> n = new Node<>(key, r);
    		map.put(key, n);
    		moveToHead(n);
    	}
        return r;
    }
    
    public synchronized void put(String key, RobotTxt value) {
        if(cap <= 0) return;
        
        if(get(key) != null) {
            map.get(key).val = value;
            return;
        }
        
        opNum++;
        Node<RobotTxt> n = new Node<>(key, value);
        if(map.size() == cap) {
            evict();
        }
        moveToHead(n);
        map.put(key, n);
        
        if(opNum == numToWriteSnapshot) {
        	opNum = 0;
        	writeSnapshot();
        }
    }
    
    private void detach(Node<RobotTxt> e) {
        e.prev.next = e.next;
        e.next.prev = e.prev;
    }
    
    private void moveToHead(Node<RobotTxt> e) {
        e.prev = head;
        e.next = head.next;
        head.next.prev = e;
        head.next = e;
    }
    
    private void evict() {
        if(tail.prev == head) return;
        Node<RobotTxt> e = tail.prev;
        e.prev.next = tail;
        tail.prev = e.prev;
        map.remove(e.key);
        db.saveRobotTxt(e.val);
//        db.sync();
        
        System.out.println("evict: " + e.key);
    }
    
    public synchronized void writeSnapshot() {
    	System.out.println("[robot cache]write snapshot to " + db.getPath());
    	for(Node<RobotTxt> n: map.values()) {
//			System.out.println("[robot cache] write: " + n.key);
    		db.saveRobotTxt(n.val);
    	}
    	db.sync();
    }
    
    public static void main(String[] args) {
    	
    	RobotInfoManager rm = new RobotInfoManager();
    	rm.isAllowed("http://crawltest.cis.upenn.edu/bbc/science.xml");
    	rm.isAllowed("https://piazza.com/");
    	rm.isAllowed("https://www.reddit.com/");
    	rm.writeSnapshot();
    	
//    	RobotCache rc = new RobotCache(1, "./tmp", 100);
//    	rc.put("key1", r1);
//    	rc.put("key2", r2);
//    	rc.put("key3", r3);
//    	rc.writeSnapshot();
    	
    }

}

package crawler.due;

import java.util.HashMap;
import java.util.Map;

import crawler.Crawler;
import crawler.robots.RobotInfoManager;
import crawler.robots.RobotTxt;
import crawler.storage.DBWrapper;
import crawler.storage.VisitedURL;
import crawler.utils.Node;
import utils.Logger;

public class URLSetCopy {
	
	static Logger logger = new Logger(URLSet.class.getName());
	
	public DBWrapper db;
    
	public HashMap<String, Node<Long>> map;
	public Node<Long> head;
	public Node<Long> tail;
	public int cap;
	public int opNum;
	public int numToWriteSnapshot;
    
    public URLSetCopy (int capacity, String DBPath, int numToWriteSnapshot) {
    	
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
        
        if(!db.vIdx.map().isEmpty()) {
        	System.out.println("url set restores from db");
        } else  {
        	System.out.println("url set starts fresh");
        }
        int loadSize = (int)(cap * 0.75);
        int num = 0;
        for(String id: db.vIdx.map().keySet()) {
//        	System.out.println("[URL SET] got " + id);
        	num++;
        	VisitedURL vu = db.getVisitedURL(id);
        	put(vu.getUrl(), vu.getTime());
        	if(num == loadSize) break;
        }
    }
    
    public URLSetCopy(int capacity, String DBPath) {
    	this(capacity, DBPath, 10000);
    }
    
	public Long get(String key) {
    	if(map.containsKey(key)) {
    		Node<Long> n = map.get(key);
            detach(n);
            moveToHead(n);
            return n.val;
    	}
    	
    	// in DB
    	VisitedURL r = db.getVisitedURL(key);
    	if(r != null) {
    		if(cap == map.size()) evict();
    		Node<Long> n = new Node<>(key, 1L);
    		map.put(key, n);
    		moveToHead(n);
    	}
        return null;
    }
    
    public void put(String key, Long value) {
        if(cap <= 0) return;
        
        if(get(key) != null) {
            map.get(key).val = value;
            return;
        }
        
//        opNum++;
        Node<Long> n = new Node<>(key, value);
        
        if(map.size() == cap) {
            evict();
        }
        moveToHead(n);
        map.put(key, n);
        
//        if(opNum == numToWriteSnapshot) {
//        	opNum = 0;
//        	writeSnapshot();
//        }
    }
    
    private void detach(Node<Long> e) {
        e.prev.next = e.next;
        e.next.prev = e.prev;
    }
    
    private void moveToHead(Node<Long> e) {
        e.prev = head;
        e.next = head.next;
        head.next.prev = e;
        head.next = e;
    }
    
    private void evict() {
        if(tail.prev == head) return;
        Node<Long> e = tail.prev;
        e.prev.next = tail;
        tail.prev = e.prev;
        map.remove(e.key);
        db.saveVisitedURL(new VisitedURL(e.key, e.val));
//        db.sync();
//        System.out.println("evict: " + e.key);
    }
    
    public synchronized boolean addURL(String url) {
    	if(get(url) != null) return false;
    	put(url, 1L);
    	return true;
    }
    
    public synchronized void writeSnapshot() {
    	System.out.println("write snapshot");
    	synchronized(map) {
    		for(Node<Long> e: map.values()) {
//    			System.out.println("write: " + e.key);
        		db.saveVisitedURL(new VisitedURL(e.key, e.val));
        	}
        	db.sync();
    	}
    }
    
    public static void main(String[] args) {
    	
    	URLSet us = new URLSet(10, Crawler.URL_SET_CACHE_PATH, 1000);
//    	us.addURL("a");
//    	us.addURL("b");
//    	us.addURL("c");
//    	us.addURL("d");
//    	us.addURL("e");
//    	us.writeSnapshot();
    	
    }

}

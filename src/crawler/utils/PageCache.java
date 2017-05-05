package crawler.utils;

import java.util.HashMap;

import crawler.storage.CrawledPage;
import crawler.storage.DBWrapper;
import utils.Logger;

public class PageCache {
//
//	static Logger logger = new Logger(PageCache.class.getName());
//	
//	public DBWrapper db;
//    
//	public HashMap<String, Node<CrawledPage>> map;
//	public Node<CrawledPage> head;
//	public Node<CrawledPage> tail;
//	public int cap;
//	public int opNum;
//	public int numToWriteSnapshot;
//    
//    public PageCache (int capacity, String DBPath, int numToWriteSnapshot) {
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
//    public PageCache(int capacity, String DBPath) {
//    	this(capacity, DBPath, 1000);
//    }
//    
//	public synchronized CrawledPage get(String key) {
//    	if(map.containsKey(key)) {
//    		Node<CrawledPage> n = map.get(key);
//            detach(n);
//            moveToHead(n);
//            return n.value;
//    	}
//    	
//    	// in DB
//    	CrawledPage p = db.getPage(key);
//    	if(p != null) {
//    		if(cap == map.size()) evict();
//    		Node<CrawledPage> n = new Node<>(key, p);
//    		map.put(key, n);
//    		moveToHead(n);
//    	}
//        return p;
//    }
//    
//    public synchronized void put(String key, CrawledPage value) {
//        if(cap <= 0) return;
//        
//        if(get(key) != null) {
//            map.get(key).value = value;
//            return;
//        }
//        
//        opNum++;
//        Node<CrawledPage> n = new Node<>(key, value);
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
//    private void detach(Node<CrawledPage> e) {
//        e.prev.next = e.next;
//        e.next.prev = e.prev;
//    }
//    
//    private void moveToHead(Node<CrawledPage> e) {
//        e.prev = head;
//        e.next = head.next;
//        head.next.prev = e;
//        head.next = e;
//    }
//    
//    private void evict() {
//        if(tail.prev == head) return;
//        Node<CrawledPage> e = tail.prev;
//        e.prev.next = tail;
//        tail.prev = e.prev;
//        map.remove(e.key);
//        db.savePage(e.value);
//        db.sync();
//        
//        System.out.println("evict: " + e.key);
//    }
//    
//    public void writeSnapshot() {
//    	System.out.println("write snapshot");
//    	synchronized(map) {
//    		for(Node<CrawledPage> n: map.values()) {
////    			System.out.println("write: " + n.key);
//        		db.savePage(n.value);
//        	}
//        	db.sync();
//    	}
//    }
//    
//    public static void main(String[] args) {
//    	
//    	PageCache p = new PageCache(100, "./db0", 100);
//    	System.out.println(new String(p.get("http://crawltest.cis.upenn.edu/bbc/science.xml").getContent()));
//    	
//    	
////    	DBWrapper db = new DBWrapper("./db0");
////    	db.setup();
////    	for(String s: db.pIdx.map().keySet()) {
////    		System.out.println(s);
////    	}
//    }

}

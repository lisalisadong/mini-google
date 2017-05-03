package crawler.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import crawler.Crawler;
import crawler.storage.DBWrapper;

@SuppressWarnings("serial")
public class LRUCache<T> {
	
	DBWrapper db;
    
    HashMap<String, CacheEntry<T>> map;
    CacheEntry<T> head;
    CacheEntry<T> tail;
    int cap;
    
    public LRUCache (int capacity, String DBPath) {
        cap = capacity;
        head = new CacheEntry<>(null, null);
        tail = new CacheEntry<>(null, null);
        head.next = tail;
        tail.prev = head;
        map = new HashMap<>();
        
        db = new DBWrapper(DBPath);
        db.setup();
    }
    
    @SuppressWarnings("unchecked")
	public synchronized T get(String key) {
    	if(map.containsKey(key)) {
    		CacheEntry<T> c = map.get(key);
            detach(c);
            moveToHead(c);
            return c.val;
    	}
    	
    	// in DB
    	CacheEntry<T> c = db.getCacheEntry(key);
    	if(c != null) {
    		if(cap == map.size()) evict();
    		moveToHead(c);
    	}
        return c == null? null: c.val;
    }
    
    public synchronized void put(String key, T value) {
        if(cap <= 0) return;
        
        if(get(key) != null) {
            map.get(key).val = value;
            return;
        }
        
        CacheEntry<T> e = new CacheEntry<>(key, value);
        if(map.size() == cap) {
            evict();
        }
        moveToHead(e);
        map.put(key, e);
    }
    
    private void detach(CacheEntry<T> e) {
        e.prev.next = e.next;
        e.next.prev = e.prev;
    }
    
    private void moveToHead(CacheEntry<T> e) {
        e.prev = head;
        e.next = head.next;
        head.next.prev = e;
        head.next = e;
    }
    
    private void evict() {
        if(tail.prev == head) return;
        CacheEntry<T> e = tail.prev;
        e.prev.next = tail;
        tail.prev = e.prev;
        map.remove(e.key);
        db.saveCacheEntry(e);
    }
}

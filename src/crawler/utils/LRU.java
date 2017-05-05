package crawler.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import crawler.storage.DBWrapper;

public class LRU<K, V> extends LinkedHashMap<K, V> {
	
	private int capacity = 100;
	
	public LRU(int capacity) {
		super(100, 0.75f, true);
		this.capacity = capacity;
	}
	
	@Override  
    public boolean removeEldestEntry(Map.Entry<K, V> eldest){   
//        System.out.println(eldest.getKey() + "=" + eldest.getValue());    
        return size() > capacity;  
    } 
	
	public static void main(String[] args) {
		LRU<String, String> lru = new LRU<>(2);
		
		lru.put("1", "1");
		lru.put("2", "2");
		
		System.out.println("contains 1: " + lru.containsKey("1"));
		lru.get("2");
		lru.put("3", "3");
		
		System.out.println("contains 1: " + lru.containsKey("1"));
		
		
	}
	
}

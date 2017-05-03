package crawler.due;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

import crawler.Crawler;
import crawler.utils.LRU;
import crawler.utils.LRUCache;
import crawler.worker.CrawlerWorker;

public class URLSet {
	
	public LRUCache<Integer> urlSet;
	
	public URLSet() {
		urlSet = new LRUCache<>(65536, CrawlerWorker.cacheDir);
	}
	
	public boolean addURL(String url) {
		Integer i = urlSet.get(url);
		if(i != null) return false;
		urlSet.put(url, i == null? 1: i + 1);
		return true;
	}
	
	public synchronized boolean contains(String url) {
		return urlSet.get(url) != null;
	}
	
}

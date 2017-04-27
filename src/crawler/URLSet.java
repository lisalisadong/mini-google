package crawler;

import java.util.HashSet;

public class URLSet {
	
	HashSet<String> urlSet;
	
	public URLSet() {
		urlSet = new HashSet<>();
	}
	
	public synchronized boolean addURL(String url) {
		return urlSet.add(url);
	}
	
	public synchronized boolean contains(String url) {
		return urlSet.contains(url);
	}
	
}

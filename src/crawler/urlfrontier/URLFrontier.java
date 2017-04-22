package crawler.urlfrontier;

import java.util.LinkedList;

public class URLFrontier {
	
	private LinkedList<String> urls;
	
	public URLFrontier() {
		urls = new LinkedList<>();
	}
	
	/**
	 * the url is guaranteed available to access
	 * @return next available url, null if empty
	 */
	public String getNextURL() {
		
		if(urls.size() == 0) return null;
		
		String url;
		synchronized(urls) {
			url = urls.poll();
		}
		return url;
	}
	
	/**
	 * add an url to frontier
	 * @param url
	 */
	public void addURL(String url) {
		synchronized(urls) {
			urls.offer(url);
		}
	}
}

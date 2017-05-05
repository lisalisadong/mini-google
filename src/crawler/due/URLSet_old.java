package crawler.due;

import java.util.HashSet;

import utils.Logger;

public class URLSet_old {

	static Logger logger = new Logger(URLSet_old.class.getName());
	
	public HashSet<String> set;
	
	public URLSet_old(int maxSize, String DBPAath, int numToWriteSnapshot) {
		set = new HashSet<>();
		
	}
	
	public synchronized boolean addURL(String url) {
    	return set.add(url);
    }
}

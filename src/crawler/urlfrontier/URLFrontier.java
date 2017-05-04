package crawler.urlfrontier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import crawler.Crawler;
import crawler.client.URLInfo;
import crawler.robots.RobotInfoManager;
import crawler.storage.DBWrapper;
import crawler.storage.StateDBWrapper;

/**
 * the url frontier, takes care of the synchronization
 * @author xiaofandou
 *
 */
@Entity
public class URLFrontier {
	
	public static final String ID = "URL_FRONTIER";
	
    @PrimaryKey
    private String id = ID;
    
    private int size;
    
    public RobotInfoManager robotManager;
    
//    <HOST, url list>
    HashMap<String, LinkedList<String>> frontendQueues;
    HashMap<String, LinkedList<String>> backendQueues;
    PriorityQueue<HostWrapper> backendSelector;

    public URLFrontier() {
    	size = 0;
    	robotManager = Crawler.getRobotManager();
    	frontendQueues = new HashMap<>();
    	backendQueues = new HashMap<>();
    	backendSelector = new PriorityQueue<>(5000, (w1, w2) -> {
    		if(w1.availableTime - w2.availableTime > 0) return 1;
    		if(w1.availableTime - w2.availableTime < 0) return -1;
    		return 0;
    	});
    }

    /**
     * the url is guaranteed available to access
     * 
     * @return next available url, null if empty
     */
    public synchronized String getNextURL() {
    	if(backendSelector.isEmpty()) return null;
    	size--;
    	HostWrapper hw = backendSelector.poll();
    	
    	LinkedList<String> ll = backendQueues.get(hw.host);
    	String url = ll.poll();
    	
    	// no url left, delete the queue
    	if(ll.isEmpty()) {
    		backendQueues.remove(hw.host);
    		return url;
    	}
    	
    	// update available time for host;
    	hw.availableTime = robotManager.getAvailableTime(url);
    	backendSelector.offer(hw);
    	return url;
    }

    /**
     * add an url to frontier
     * 
     * @param url
     */
    public synchronized void addURL(String url) {
    	String host = new URLInfo(url).getHostName();
    	if(!backendQueues.containsKey(host)) {
    		backendQueues.put(host, new LinkedList<>());
    		backendSelector.offer(new HostWrapper(host, robotManager.getAvailableTime(url)));
    	}
    	backendQueues.get(host).offer(url);
    	size++;
    }
    
    public synchronized int size() {
    	return size;
    }
    
    public void writeSnapshot(StateDBWrapper db) {
    	
    }
}

class HostWrapper {
	String host;
	long availableTime;
	
	HostWrapper(String host, long availableTime) {
		this.host = host;
		this.availableTime = availableTime;
	}
}

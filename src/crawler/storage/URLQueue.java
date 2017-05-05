package crawler.storage;

import java.util.LinkedList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class URLQueue {
	
	@PrimaryKey
	private String id;
	private LinkedList<String> queue;
	
	public URLQueue() {}
	
	public URLQueue(String id, LinkedList<String> queue) {
		this.id = id;
		this.queue = queue;
	}
	
	public LinkedList<String> getQueue() {
		return queue;
	}
}

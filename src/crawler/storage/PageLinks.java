package crawler.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import java.util.LinkedList;

@Entity
public class PageLinks {
	
	@PrimaryKey
	private String url;
	private LinkedList<String> links;
	
	public PageLinks() {}
	
	public PageLinks (String url) {
		this.url = url;
		links = new LinkedList<>();
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void addLink(String link) {
		links.offer(link);
	}
	
	public LinkedList<String> getLinks() {
		return links;
	}
}

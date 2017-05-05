package crawler.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class VisitedURL {
	
	@PrimaryKey
	private String url;
	private Long time;
	
	public VisitedURL() {}
	
	public VisitedURL(String url, long time) {
		this.url = url;
		this.time = time;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public long getTime() {
		return time;
	}
}

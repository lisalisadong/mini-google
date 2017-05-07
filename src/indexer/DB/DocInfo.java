package indexer.DB;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class DocInfo {
	
	@PrimaryKey
	private String docID;
	private String url;
	private String title;
	private String description;
	
	public DocInfo() { }
	
	public DocInfo(String docID, String url, String title, String description) {
		this.docID = docID;
		this.url = url;
		this.title = title;
		this.description = description;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String[] getInfo() {
		return new String[]{url, title, description};
	}

}

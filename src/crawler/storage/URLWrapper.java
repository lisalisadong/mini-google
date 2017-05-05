package crawler.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class URLWrapper {

	@PrimaryKey
	public String id;
	
	URLWrapper(){}
	public URLWrapper(String id) { this.id = id; }
}

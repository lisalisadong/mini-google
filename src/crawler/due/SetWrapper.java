package crawler.due;

import java.util.HashSet;
import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class SetWrapper {

	@PrimaryKey
	public String id;
	public HashSet<String> set;
	
	public SetWrapper() {}
	
	public SetWrapper(String id, HashSet<String> set) {
		this.id = id;
		this.set = set;
	}
}

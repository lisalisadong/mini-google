package crawler.utils;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class CacheEntry<T> {
	
	@PrimaryKey
	public String key;
    public T val;
	
    public CacheEntry<T> prev;
    public CacheEntry<T> next;
    
    CacheEntry() { }
    
    CacheEntry (String key, T val) {
        this.key = key;
        this.val = val;
    }
}

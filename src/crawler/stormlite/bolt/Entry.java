package crawler.stormlite.bolt;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import static com.sleepycat.persist.model.Relationship.*;

import java.util.LinkedList;
import java.util.List;

import com.sleepycat.persist.model.SecondaryKey;

/**
 * wrapper class for user
 * 
 * @author xiaofandou
 *
 */
@Entity
public class Entry {

    @PrimaryKey
    private String key;
    public List<String> values;

    public Entry(String key) {
        this.key = key;
        this.values = new LinkedList<>();
    }

    public Entry() {
    }

}

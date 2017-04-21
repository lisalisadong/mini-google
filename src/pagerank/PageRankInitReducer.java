package pagerank;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * PageRank init reducer
 * 
 * @author yishang
 *
 */
public class PageRankInitReducer extends Reducer<Text, Text, Text, Text> {

    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        Iterator<Text> itr = values.iterator();
        if (itr.hasNext()) {
            context.write(key, itr.next());
        }
    }

}

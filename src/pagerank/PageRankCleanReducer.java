package pagerank;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * PageRank clean reducer
 * 
 * @author yishang
 *
 */
public class PageRankCleanReducer extends Reducer<Text, Text, Text, Text> {

    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

        Iterator<Text> itr = values.iterator();
        while (itr.hasNext()) {
            context.write(key, itr.next());
        }

    }

}

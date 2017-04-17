package pagerank;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * PageRank run reducer
 * 
 * @author yishang
 *
 */
public class PageRankRunReducer extends Reducer<Text, Text, Text, Text> {

    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

        Iterator<Text> itr = values.iterator();
        String data = "";
        boolean done = false;
        double pr = 0.0;

        while (itr.hasNext()) {
            String line = itr.next().toString();
            if (line.startsWith("#")) {
                done = true;
                data = line.substring(1);
            } else {
                pr += Double.valueOf(line);
            }
        }

        if (done) {
            pr = 0.85 * pr + 0.15;
            context.write(key, new Text(pr + "," + data));
        }
    }

}

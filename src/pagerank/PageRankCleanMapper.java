package pagerank;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * PageRank clean mapper
 * 
 * @author yishang
 *
 */
public class PageRankCleanMapper extends Mapper<LongWritable, Text, Text, Text> {

    /**
     * input line: [url,pagerank,#oflinks,link1;link2;link3...]
     */
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        Text url = new Text();
        Text rank = new Text();
        String data = "";

        String line = value.toString();
        if (line.indexOf(',') != -1) {
            int comma = line.indexOf(',');
            url.set(line.substring(0, comma));
            data = line.substring(comma + 1);
        } else {
            System.out.println("[Wrong format] missing delimiter ,");
        }

        String pr = data.split(",")[0];
        rank.set(pr);
        context.write(url, rank);
    }

}

package pagerank;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * PageRank init mapper
 * 
 * @author yishang
 *
 */
public class PageRankInitMapper extends Mapper<LongWritable, Text, Text, Text> {

    /**
     * Input line: [url,link1;link2;link3...]; [url,] if no internal links
     * Output line: [url,pagerank,#oflinks,link1;link2;link3...]
     */
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        Text url = new Text();
        Text links = new Text();
        String allLinks = "";

        String line = value.toString();
        if (line.indexOf(',') != -1) {
            int comma = line.indexOf(',');
            url.set(line.substring(0, comma));
            allLinks = line.substring(comma + 1);
        } else {
            System.out.println("[Wrong format] missing delimiter ,");
        }

        int numOfLinks = 0;
        if (!allLinks.isEmpty()) {
            numOfLinks = allLinks.split(";").length;
        }

        double initialPageRank = 1.0;
        links.set(initialPageRank + "," + numOfLinks + "," + allLinks);

        context.write(url, links);
    }

}

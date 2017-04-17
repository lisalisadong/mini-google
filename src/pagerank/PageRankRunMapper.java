package pagerank;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * PageRank run mapper
 * 
 * @author yishang
 *
 */
public class PageRankRunMapper extends Mapper<LongWritable, Text, Text, Text> {

    /**
     * input line: [url,pagerank,#oflinks,link1;link2;link3...]
     */
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        Text url = new Text();
        String data = "";

        String line = value.toString();
        if (line.indexOf(',') != -1) {
            int comma = line.indexOf(',');
            url.set(line.substring(0, comma));
            data = line.substring(comma + 1);
        } else {
            System.out.println("[Wrong format] missing delimiter ,");
        }

        String[] tokens = data.split(",");
        if (tokens.length != 3) {
            System.out.println("[Wrong format] should be: [pagerank,#oflinks,link1:link2:link3...]");
        }

        Double pr = Double.valueOf(tokens[0]);
        int numOfLinks = Integer.valueOf(tokens[1]);
        String allLinks = tokens[2];

        if (numOfLinks > 0) {
            for (String link : allLinks.split(";")) {
                Text l = new Text();
                l.set(link);
                context.write(l, new Text(String.valueOf(pr / numOfLinks)));
            }
        }

        context.write(url, new Text("#" + numOfLinks + "," + allLinks));
    }

}

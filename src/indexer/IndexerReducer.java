package indexer;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class IndexerReducer extends Reducer<Text, Text, Text, Text> { //TODO: intermediate format, output format

	public void reduce(Text key, Iterable<Text> value, Context context) throws IOException, InterruptedException {
		// TODO:
		context.write(key, new Text("temp"));
	}
}
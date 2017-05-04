package indexer;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.google.common.collect.Iterables;

import utils.Constants;

public class IndexerReducer extends Reducer<Text, InterValue, Text, OutputValue> {

	public void reduce(Text key, Iterable<InterValue> value, Context context) throws IOException, InterruptedException {
		int count = Iterables.size(value);
		double idf = Math.log((double) Constants.TF_FACTOR / count);
		
		for (InterValue v : value) {
			OutputValue outputValue = new OutputValue(v.getDocID(), v.getTf(), idf, v.getPositions());
			context.write(key, outputValue);
		}
	}
}

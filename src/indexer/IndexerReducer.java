package indexer;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.google.common.collect.Lists;

import utils.Constants;

public class IndexerReducer extends Reducer<Text, InterValue, Text, OutputValue> {

	public void reduce(Text key, Iterable<InterValue> values, Context context) throws IOException, InterruptedException {
		List<InterValue> valueList = Lists.newArrayList(values);
		int count = valueList.size();
		double idf = Math.log((double) Constants.TF_FACTOR / count);
		
		for (InterValue value : valueList) {
			System.out.println("word:" + key + "docID: " + value.getDocID());
			OutputValue outputValue = new OutputValue(value.getDocID(), value.getTf(), idf, value.getPositions());
			context.write(key, outputValue);
		}
	}
}

package indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;


import utils.Constants;

public class IndexerReducer extends Reducer<Text, InterValue, Text, OutputValue> {

	public void reduce(Text key, Iterable<InterValue> values, Context context) throws IOException, InterruptedException {
		List<InterValue> valueList = new ArrayList<InterValue>();
		for (InterValue value : values) {
			valueList.add(new InterValue(value.getDocID(), value.getTf(), value.getTitlePositions(), value.getContentPositions()));
		}
		int count = valueList.size();
		double idf = Math.log((double) Constants.TOTAL_DOC_NUM / count);
		
		for (InterValue value : valueList) {
//			System.out.println("word:" + key + "docID: " + value.getDocID());
			OutputValue outputValue = new OutputValue(value.getDocID(), value.getTf(), idf, value.getTitlePositions(), value.getContentPositions());
			context.write(key, outputValue);
		}
	}
}

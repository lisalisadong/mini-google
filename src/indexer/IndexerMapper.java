package indexer;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class IndexerMapper extends Mapper<Text, Text, Text, Text>{ // TODO: inputformate , intermediate formate

	public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
		// TODO:
		
		context.write(key, value);
	}

}

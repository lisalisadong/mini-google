package indexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import utils.Constants;

public class IndexerMapper extends Mapper<Text, BytesWritable, Text, InterValue>{ // TODO: inputformate

	public void map(Text key, BytesWritable value, Context context) throws IOException, InterruptedException {
		// TODO: retrieve input info
		String docID = "";
		String contentType = "html";
		
		IndexerMapWorker mapWorker = new IndexerMapWorker(docID, new ByteArrayInputStream(value.getBytes()), contentType);
		mapWorker.parse();
		Map<String, Integer> wordFreq = mapWorker.getWordFreq();
		Map<String, List<Integer>> wordPos = mapWorker.getWordPos();
		
		double tfFactor = Constants.TF_FACTOR;
		if (!wordFreq.isEmpty()) {
			int maxFreq = Collections.max(wordFreq.values());
			for (Entry<String, Integer> wf : wordFreq.entrySet()) {
				String word = wf.getKey();
				double tf = tfFactor + (1 - tfFactor) * wf.getValue() / maxFreq;
				InterValue interValue = new InterValue(docID, tf, wordPos.get(word));
				context.write(new Text(word), interValue);
			}
		}
	}

}

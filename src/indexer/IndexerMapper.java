package indexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import utils.Constants;

public class IndexerMapper extends Mapper<LongWritable, Text, Text, InterValue>{

	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		String[] info = value.toString().split("\t", 3);
		String docID = info[0];
		String contentType = info[1];
		
		IndexerMapWorker mapWorker = new IndexerMapWorker(new ByteArrayInputStream(info[2].getBytes()), contentType);
		mapWorker.parse();
		Map<String, Integer> wordFreq = mapWorker.getWordFreq();
		Map<String, List<Integer>> titlePos = mapWorker.getTitlePos();
		Map<String, List<Integer>> contentPos = mapWorker.getContentPos();
//		for (String k : wordFreq.keySet()) {
//		    System.out.println(k + ": " + wordFreq.get(k));
//		    System.out.println(wordPos.get(k));
//		}
		
		double tfFactor = Constants.TF_FACTOR;
		List<Integer> tPos;
		List<Integer> cPos;
		if (!wordFreq.isEmpty()) {
			int maxFreq = Collections.max(wordFreq.values());
			for (Entry<String, Integer> wf : wordFreq.entrySet()) {
				String word = wf.getKey();
				double tf = tfFactor + (1 - tfFactor) * wf.getValue() / maxFreq;
				tPos = titlePos.get(word);
				cPos = contentPos.get(word);
				if (tPos == null) {
					tPos = new ArrayList<Integer>();
				}
				if (cPos == null) {
					cPos = new ArrayList<Integer>();
				}
				InterValue interValue = new InterValue(docID, tf, tPos, cPos);
				context.write(new Text(word), interValue);
			}
		}
	}

}

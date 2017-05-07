package indexer;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.CombineFileRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;

public class CFInputFormat extends CombineFileInputFormat<Text, Text> {

	public CFInputFormat() {
		super();
		setMaxSplitSize(33554432); // 32 MB, smaller than default block size
	}
	
	@Override
    protected boolean isSplitable(JobContext context, Path file) {
        return false;
    }

	@Override
	public RecordReader<Text, Text> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException {
		return new CombineFileRecordReader<Text, Text>((CombineFileSplit)split, context, CFRecordReader.class);
	}
	

}

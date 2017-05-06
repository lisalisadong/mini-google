package indexer;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class WholeFileRecordReader extends RecordReader<LongWritable, Text>{

	private FileSplit fileSplit;
    private Configuration conf;
    private LongWritable key;
    private Text value;
    private boolean fileProcessed = false;
    
	@Override
	public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
		this.fileSplit = (FileSplit) split;
		this.conf = context.getConfiguration();
		this.key = new LongWritable();
		this.value = new Text();
	}

	@Override
	public LongWritable getCurrentKey() throws IOException, InterruptedException {
		return key;
	}

	@Override
	public Text getCurrentValue() throws IOException, InterruptedException {
		return value;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return fileProcessed ? 1 : 0;
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if (fileProcessed) {
            return false;
        }
		int length = (int) fileSplit.getLength();
		byte[] contents = new byte[length];
		Path path = fileSplit.getPath();
        FileSystem fs = FileSystem.get(path.toUri(), conf);
        FSDataInputStream in = null;
        try {
        	in = fs.open(path);
        	IOUtils.readFully(in, contents, 0, length);
        	value.set(contents, 0, length);
        } finally {
        	IOUtils.closeStream(in);
        }
		fileProcessed = true;
		return true;
	}
	
	@Override
	public void close() throws IOException {
	}

}

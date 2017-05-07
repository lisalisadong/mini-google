package indexer;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;

public class CFRecordReader extends RecordReader<Text, Text> {

	private int index;
	private CombineFileSplit split;
	private Configuration conf;
	private Text key;
    private Text value;
    private boolean fileProcessed = false;

	public CFRecordReader(CombineFileSplit split, TaskAttemptContext context, Integer index) throws IOException {
		this.index = index;
		this.split = split;
		this.conf = context.getConfiguration();
		String fileName = this.split.getPath(index).getName();
        this.key = new Text(fileName);
        this.value = new Text();
	}

	@Override
	public void close() throws IOException { }

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
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
	public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if (fileProcessed) {
            return false;
        }
		int length = (int) split.getLength(index);
		byte[] contents = new byte[length];
		Path path = split.getPath(index);
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

}

package indexer;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Driver {

	public static void main(String[] args) throws Exception {
		Job job = new Job();
		job.setJarByClass(Driver.class);

		// TODO:
		FileInputFormat.addInputPath(job, new Path("in"));
		FileOutputFormat.setOutputPath(job, new Path("out"));

		job.setMapperClass(IndexerMapper.class);
		job.setReducerClass(IndexerReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

}

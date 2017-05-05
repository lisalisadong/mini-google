package indexer;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Driver {
	

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Args Error: [input path] [output path]");
			System.exit(1);
		}
		String inputPath = args[0];
		String outputPath = args[1];
		
		Job job = new Job();
		job.setJarByClass(Driver.class);

		FileInputFormat.addInputPath(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));

		job.setMapperClass(IndexerMapper.class);
		job.setReducerClass(IndexerReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(InterValue.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(OutputValue.class);

		System.exit(job.waitForCompletion(true) ? 0 : 1);
		
		//
//		job.setInputFormatClass(TextInputFormat.class);
	}

}

package pagerank;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * PageRank task
 * 
 * @author yishang
 *
 */
public class PageRankTask {

    public final static String INIT = "init";
    public final static String RUN = "run";
    public final static String CLEAN = "clean";

    public static void task(String inputPath, String outputPath, String taskName) {
        try {
            Configuration config = new Configuration();
            config.set("mapred.textoutputformat.separator", "@");

            Job job = Job.getInstance(config, taskName);
            job.setJarByClass(PageRankDriver.class);

            if (taskName.equals(INIT)) {
                MultithreadedMapper.setMapperClass(job, PageRankInitMapper.class);
                MultithreadedMapper.setNumberOfThreads(job, 10);
                job.setMapperClass(MultithreadedMapper.class);
                // job.setMapperClass(PageRankInitMapper.class);
                job.setReducerClass(PageRankInitReducer.class);
            }

            else if (taskName.equals(RUN)) {
                MultithreadedMapper.setMapperClass(job, PageRankRunMapper.class);
                MultithreadedMapper.setNumberOfThreads(job, 10);
                job.setMapperClass(MultithreadedMapper.class);
                // job.setMapperClass(PageRankRunMapper.class);
                job.setReducerClass(PageRankRunReducer.class);
            }

            else if (taskName.equals(CLEAN)) {
                MultithreadedMapper.setMapperClass(job, PageRankCleanMapper.class);
                MultithreadedMapper.setNumberOfThreads(job, 10);
                job.setMapperClass(MultithreadedMapper.class);
                // job.setMapperClass(PageRankCleanMapper.class);
                job.setReducerClass(PageRankCleanReducer.class);
            }

            else {
                System.out.println("No such task!");
            }

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class); // changed
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);

            FileInputFormat.addInputPath(job, new Path(inputPath));
            FileOutputFormat.setOutputPath(job, new Path(outputPath));
            job.waitForCompletion(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

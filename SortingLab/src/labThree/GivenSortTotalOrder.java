package labThree;

import java.net.URI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;

import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler.RandomSampler;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;
import org.apache.hadoop.util.GenericOptionsParser;

public class GivenSortTotalOrder {
	
	public static void main(String[] args) throws Exception{ 
		
		// Set the number of reducer (No more than 10)
		int reduceNumber = 10;
		
		Configuration conf = new Configuration();
		
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		
		if (otherArgs.length != 2) {
			System.err.println("Usage: Patent <in> <out>");
			System.exit(2);
		}
		
		
		conf.setInt("Count", 0);
		
		// Use InputSampler.RandomSampler to create sample list
		// Although the efficiency of RandomSampler is the lowest in all three kinds of Sampler (interval, splitSampler) 
		// But randomSampler is the most accurate sampler class for this experiment 
		// The argument set as the following:
		// freq: 0.1 (probability with which a key will be chosen)
		// numSamplers: 5000000 (total number of samples to obtain from all splits)
		// maxSplitSampled: 9 (the maximum number of splits to examine)
		RandomSampler<Text, Text> sampler = new InputSampler.RandomSampler<Text, Text> (0.15, 5600000);
		
		// Set the path of partition file 
		TotalOrderPartitioner.setPartitionFile(conf, new Path("temp/_partitions"));
		
		
		Job job = Job.getInstance(conf, "Exp1");
		job.setJarByClass(GivenSortTotalOrder.class);
		job.setNumReduceTasks(reduceNumber);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		job.setMapperClass(mapOne.class);
		job.setReducerClass(reduceOne.class);
		
		// KeyValueTextInputFormat.class is the only one class we can use in this experiment
		// Since TotalOrderPartitioner.class requires input must be <Text, Text>
		job.setInputFormatClass(KeyValueTextInputFormat.class); 
		job.setOutputFormatClass(TextOutputFormat.class);
		
		// Use TotalOrderPartitioner class to sort input data
		job.setPartitionerClass(TotalOrderPartitioner.class);
		
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

		// Output path
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		
		// Write the partition file to the partition path set above
		InputSampler.writePartitionFile(job, sampler);
		
		URI partitionUri = new URI("temp/_partitions");
		job.addCacheFile(partitionUri);
		
		if (job.waitForCompletion(true)) {
			
			
			/*FileSystem fs = FileSystem.get(conf);

			// Create path object and check for its existence
			Path ParPath = new Path("/temp/_partitions");
			if (fs.exists(ParPath)) {
				// false indicates do not deletes recursively
				fs.delete(ParPath, false);
			   
			 }*/
	        
			System.exit(0);
		}
		//job.waitForCompletion(true);	
	}	
	
	
	
	public static class mapOne extends Mapper<LongWritable, Text, Text, Text> {
		
	}
	
	public static class reduceOne extends Reducer<Text, Text, Text, Text> {
		
		
		
	}
}
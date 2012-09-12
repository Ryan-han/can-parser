package com.nexr.hmc.mr;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.nexr.hmc.MessageFectory;

public class HmcMr extends Configured implements Tool {


    static MessageFectory mf = null;

    static class SshFileMapper extends MapReduceBase implements Mapper<Text, Text, NullWritable, Text> {

        private JobConf conf;
        long beginTime = 0;

        @Override
        public void configure(JobConf conf) {
            this.conf = conf;
            mf = new MessageFectory(conf.get("dbc"), conf);
        }

        public void map(Text key, Text value, OutputCollector<NullWritable, Text> output, Reporter reporter) throws IOException {
            String dataLine = value.toString();
            List<String> result = mf.messageBuilderList.get("BO_").dataParse(dataLine);
            for (String res : result) {
                output.collect(NullWritable.get(), new Text(res + "\n"));
            }
        }
    }

//    static class SshFileReducer extends MapReduceBase implements Reducer<Text, Text, NullWritable, Text> {
//
//        @Override
//        public void reduce(Text key, Iterator<Text> value, OutputCollector<NullWritable, Text> output, Reporter arg3) throws IOException {
//            while (value.hasNext()) {
//                output.collect(NullWritable.get(), value.next());
//            }
//        }
//
//    }

    public int run(String[] args) throws Exception {

        JobConf conf = new JobConf(getConf(), this.getClass());

        conf.set("dbc", args[1]);

        System.out.println("==> " + args[2]);
        conf.setInputFormat(HmcFileInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);
        FileOutputFormat.setOutputPath(conf, new Path(args[2]));
        conf.set("target", args[0]);
        conf.setOutputKeyClass(NullWritable.class);
        conf.setOutputValueClass(Text.class);
        conf.setMapperClass(SshFileMapper.class);
        conf.setNumReduceTasks(0);
//        conf.setReducerClass(SshFileReducer.class);
//        conf.setNumReduceTasks(2);

        JobClient.runJob(conf);
        return 0;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: hadoop jar scpmr.jar com.nexr.ryan.scp.SshFileToSequenceMr <source> <dbc> <output>");
            System.exit(2);
        }

        int exitCode = ToolRunner.run(new HmcMr(), args);
        System.out.println(exitCode);
    }

}

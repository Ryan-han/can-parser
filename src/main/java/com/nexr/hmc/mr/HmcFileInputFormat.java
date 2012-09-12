package com.nexr.hmc.mr;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.UTF8;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.log4j.Logger;

import com.nexr.hmc.ssh.JschRunner;
import com.nexr.hmc.ssh.SshExecCommand;
import com.nexr.hmc.ssh.util.IoUtil;

public class HmcFileInputFormat<Text, ByteWritable> implements InputFormat<Text, Text> {
    static Logger log = Logger.getLogger(HmcFileInputFormat.class);

    File f;

    protected boolean isSplitable(FileSystem fs, Path fileName) {
        return false;
    }

    protected TargetInfo[] listStatus(JobConf job) throws IOException {
        try {
            f = File.createTempFile("scpmr", "txt");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        List<TargetInfo> result = new ArrayList<TargetInfo>();

        BufferedReader reader = null;
        FileSystem fs = FileSystem.get(job);
        String[] inputs = job.get("target").split(" ");
        System.out.println("Input path size ==> " + inputs.length);

        String line;
        for (int i = 0; i < inputs.length; i++) {
            System.out.println("Input path  ==> " + inputs[i]);
            reader = new BufferedReader(new InputStreamReader(new DataInputStream(fs.open(new Path(inputs[i])))));

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                String[] info = line.split(",");
                String host = info[0];
                String id = info[1];
                String passwd = info[2];
                String path = info[3];

                JschRunner runner = new JschRunner(id, host);
                runner.setPassword(passwd);
                SshExecCommand command = new SshExecCommand("ls " + path, IoUtil.closeProtectedStream(new FileOutputStream(f)));
                try {
                    runner.run(command);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                BufferedReader resulReader = null;
                resulReader = new BufferedReader(new FileReader(f));

                String fileName;
                while ((fileName = resulReader.readLine()) != null) {
                    if (fileName.endsWith(".asc")) {
                        TargetInfo targetInfo = new TargetInfo();
                        targetInfo.setHost(host);
                        targetInfo.setPath(path + "/" + fileName);
                        targetInfo.setUserId(id);
                        targetInfo.setPwd(passwd);
                        result.add(targetInfo);
                    }
                }
            }
        }

        for (TargetInfo t : result) {
            System.out.println(t.toString());
        }
        return result.toArray(new TargetInfo[result.size()]);
    }

    static final String NUM_INPUT_FILES = "mapreduce.input.num.files";

    public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
        TargetInfo[] files = listStatus(job);

        // Save the number of input files in the job-conf
        job.setLong(NUM_INPUT_FILES, files.length);

        // generate splits
        ArrayList<ScpInputSplit> splits = new ArrayList<ScpInputSplit>(numSplits);
        for (TargetInfo file : files) {
            // splits.add(new ScpInputSplit(file));
            splits.add(new ScpInputSplit(file.getHost(), file.getPath(), file.getUserId(), file.getPwd()));
        }

        return splits.toArray(new ScpInputSplit[splits.size()]);
    }

    @Override
    public RecordReader<Text, Text> getRecordReader(InputSplit split, JobConf job, Reporter reporter) throws IOException {
        // TODO Auto-generated method stub
        return (RecordReader<Text, Text>) new HmcRecordReader((ScpInputSplit) split, job);
    }

    public void validateInput(JobConf arg0) throws IOException {
        // TODO Auto-generated method stub

    }

    public static class ScpInputSplit implements InputSplit, Writable {

        String host;
        String path;
        String userId;
        String pwd;

        public ScpInputSplit() {

        }

        public ScpInputSplit(String host, String path, String userId, String pwd) {
            this.host = host;
            this.path = path;
            this.userId = userId;
            this.pwd = pwd;
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            // TODO Auto-generated method stub
            host = UTF8.readString(in);
            path = UTF8.readString(in);
            userId = UTF8.readString(in);
            pwd = UTF8.readString(in);
        }

        @Override
        public void write(DataOutput out) throws IOException {
            // TODO Auto-generated method stub
            UTF8.writeString(out, host);
            UTF8.writeString(out, path);
            UTF8.writeString(out, userId);
            UTF8.writeString(out, pwd);
        }

        @Override
        public long getLength() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String[] getLocations() throws IOException {
            // TODO Auto-generated method stub
            return new String[] {};
        }

        public String getHost() {
            return host;
        }

        public String getPath() {
            return path;
        }

        public String getUserId() {
            return userId;
        }

        public String getPwd() {
            return pwd;
        }

    }

}

package com.nexr.hmc.mr;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapred.RecordReader;

import com.nexr.hmc.mr.HmcFileInputFormat.ScpInputSplit;
import com.nexr.hmc.ssh.JschRunner;
import com.nexr.hmc.ssh.SshExecCommand;
import com.nexr.hmc.ssh.util.IoUtil;

public class HmcRecordReader implements RecordReader<Text, Text> {

    private ScpInputSplit split;
    private Configuration conf;
    private boolean process = false;

    private CompressionCodecFactory compressionCodecs = null;
    private long start;
    private long pos;
    private long end;
    private LineReader in;
    int maxLineLength;

    InputStream is;

    @Deprecated
    public static class LineReader extends com.nexr.hmc.mr.LineReader {
        LineReader(InputStream in) {
            super(in);
        }

        LineReader(InputStream in, int bufferSize) {
            super(in, bufferSize);
        }

        public LineReader(InputStream in, Configuration conf) throws IOException {
            super(in, conf);
        }

        LineReader(InputStream in, byte[] recordDelimiter) {
            super(in, recordDelimiter);
        }

        LineReader(InputStream in, int bufferSize, byte[] recordDelimiter) {
            super(in, bufferSize, recordDelimiter);
        }

        public LineReader(InputStream in, Configuration conf, byte[] recordDelimiter) throws IOException {
            super(in, conf, recordDelimiter);
        }
    }

    public HmcRecordReader(ScpInputSplit split, Configuration job) throws IOException {
        this.split = split;
        this.conf = job;

        JschRunner runner = new JschRunner(split.getUserId(), split.getHost());
        runner.setPassword(split.getPwd());
        try {
            is = runner.openFile(split.getPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.maxLineLength = job.getInt("mapred.linerecordreader.maxlength", Integer.MAX_VALUE);
        start = 0;

        end = start + is.available();

        boolean skipFirstLine = false;

        if (start != 0) {
            skipFirstLine = true;
            --start;

            is.skip(start);

        }

        in = new LineReader(is, job, "\n".getBytes());

        if (skipFirstLine) { // skip first line and re-establish "start".
            start += in.readLine(new Text(), 0, (int) Math.min((long) Integer.MAX_VALUE, end - start));
        }
        this.pos = start;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
    	JschRunner runner = new JschRunner(split.getUserId(), split.getHost());
      runner.setPassword(split.getPwd());
      SshExecCommand command = new SshExecCommand("mv " + split.getPath() + " " + split.getPath()+".FIN",  null);
      runner.run(command);
    }

    @Override
    public Text createKey() {
        // TODO Auto-generated method stub
        return new Text();
    }

    @Override
    public Text createValue() {
        // TODO Auto-generated method stub
        return new Text();
    }

    @Override
    public long getPos() throws IOException {
        // TODO Auto-generated method stub
        return pos;
    }

    @Override
    public float getProgress() throws IOException {
        // TODO Auto-generated method stub
        // return process ? 1.0f : 0.0f;
        if (0 == end) {
            return 0.0f;
        } else {
            return Math.min(1.0f, (pos - 0) / (float) (end - 0));
        }
    }

    @Override
    public boolean next(Text key, Text value) throws IOException {
        // TODO Auto-generated method stub
        while (pos < end) {
            key.set(new Text(String.valueOf(pos)));

            
            int newSize = in.readLine(value, maxLineLength,
                                      Math.max((int)Math.min(Integer.MAX_VALUE, end-pos),
                                               maxLineLength));
            if (newSize == 0) {
              return false;
            }
            pos += newSize;
            if (newSize < maxLineLength) {
              return true;
            }

            // line too long. try again
            System.out.println("Skipped line of size " + newSize + " at pos " + (pos - newSize));
          }

          return false;
    }

}

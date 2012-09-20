package com.nexr.hmc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.mapred.JobConf;
import org.apache.log4j.Logger;

import com.nexr.hmc.builder.BOMessageBuilder;
import com.nexr.hmc.builder.MessageBuilder;
import com.nexr.hmc.model.bo.Message;

public class MessageFectory {
	static Logger log = Logger.getLogger(MessageFectory.class);

	static String BO_PRIFIX = "BO_ ";
	static String SIGNAL_PRIFIX = " SG_ ";

	// messageId, scheme
	public static Map<String, Message> messageList = new HashMap<String, Message>();
	public Map<String, MessageBuilder> messageBuilderList = new HashMap<String, MessageBuilder>();

	public MessageFectory(String dir) {
		messageBuilderList.put("BO_", new BOMessageBuilder());
		loadDbc(dir);
	}

	public MessageFectory(String dir, JobConf conf) {
		messageBuilderList.put("BO_", new BOMessageBuilder());
		try {
			loadHDFSDbc(dir, conf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	BufferedReader reader;

	public void loadHDFSDbc(String dir, JobConf conf) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        Path dbcRoot = new Path(dir);
        FileStatus[] files = fs.listStatus(dbcRoot, new PathFilter() {
            
            public boolean accept(Path name) {
                if (name.getName().endsWith(".dbc")) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        
        

        if (files.length > 0) {
            log.info("find " + files.length + " schema Files");
        } else {
            log.error("Scheme file is not exist");
            System.exit(0);
        }

        BufferedReader reader = null;
        String parentMessage = null;
        for (FileStatus file : files) {
            log.info(file.getPath().getName());
            try {
                reader = new BufferedReader(new InputStreamReader(new DataInputStream(fs.open(file.getPath()))));
                String line;
                while ((line = reader.readLine()) != null) {
                	 if (line.startsWith(MessageFectory.BO_PRIFIX)) {
                		 parentMessage = "BO_";
                		 messageBuilderList.get("BO_").loadMessage(line);
                	 } else if (line.startsWith(MessageFectory.SIGNAL_PRIFIX) && parentMessage != null) {
                		 messageBuilderList.get("BO_").loadMessage(line);
                	 }
            		}
            } catch (IOException ioe) {
                log.error(ioe.getMessage());
            }
        }
        
    }

	public void loadDbc(String dir) {
		if (!new File(dir).exists()) {
			log.error("Scheme file's directory " + dir + " is not exist !!");
			System.exit(0);
		}

		File[] files = new File(dir).listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				if (name.endsWith(".dbc")) {
					return true;
				} else {
					return false;
				}
			}
		});

		if (files.length > 0) {
			log.info("find " + files.length + " schema Files");
		} else {
			log.error("Scheme file is not exist");
			System.exit(0);
		}

		BufferedReader reader = null;
		String parentMessage = null;
		for (File file : files) {
			log.info(file.getName());
			try {
				reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("BO_")) {
						parentMessage = "BO_";
						messageBuilderList.get("BO_").loadMessage(line);
					} else if (line.startsWith(" SG_") && parentMessage != null) {
						messageBuilderList.get("BO_").loadMessage(line);
					}
				}
			} catch (FileNotFoundException e) {
				log.error(e.getMessage());
			} catch (IOException ioe) {
				log.error(ioe.getMessage());
			}
		}
	}
}

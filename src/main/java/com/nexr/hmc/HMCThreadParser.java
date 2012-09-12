package com.nexr.hmc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.nexr.hmc.model.bo.Message;

public class HMCThreadParser {
	static Logger log = Logger.getLogger(HMCThreadParser.class);

	static String dateFlag = "Begin Triggerblock";

	final static int DEFAILT_THREAD = 1;

	BufferedReader reader = null;
	MessageFectory mf = null;

	static FileSystem hdfs = null;
	private static ExecutorService indexLoadService = null;

	@SuppressWarnings("unused")
	public static void main(String args[]) {
		// args[0] dbc_dir, args[1] candata_dir, args[2] result_dir
//		 args[0] = "/Users/ryan/HMC/CANDB";
//		 args[1] = "/Users/ryan/HMC";
//		// args[2] = "hdfs://localhost:54310/hmc";
//		  args[2] = "file:///Users/ryan/Script/bk";
//		 args[3] = "3";

		log.info(args[3]);
		if (args[3] == null || args[3].equals("")) {
			indexLoadService = Executors.newFixedThreadPool(DEFAILT_THREAD);
		} else {
			indexLoadService = Executors.newFixedThreadPool(Integer.parseInt(args[3]
					.trim()));
		}

		HMCThreadParser parser = new HMCThreadParser();
		parser.loadMessageFactory(args[0]);

		URI uri = null;
		try {
			uri = new URI(args[2]);
		} catch (URISyntaxException e) {
			log.error(e.getMessage());
			System.exit(0);
		}

		File[] files = parser.getDataFiles(args[1]);
		log.info(files.length);
		if (files == null) {
			log.error("Data File .asc 's not exist!!");
			System.exit(0);
		}

		for (File file : files) {
			parser.runThread(file, args[2]);
		}
		indexLoadService.shutdown();

		while (true) {
			if (indexLoadService.isTerminated()) {
				log.info("Finish job");
				System.exit(0);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void runThread(File file, String path) {
		indexLoadService.execute(new WriteThread(file, path));
	}

	private File[] getDataFiles(String dataFile) {
		if (new File(dataFile).exists()) {
			File[] files = new File(dataFile).listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.endsWith(".asc")) {
						return true;
					}
					return false;
				}
			});

			if (files != null) {
				log.info("find " + files.length + " data Files");
				return files;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private void loadMessageFactory(String dbc_dir) {
		mf = new MessageFectory(dbc_dir);
		Iterator<Entry<String, Message>> iter = mf.messageList.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Entry<String, Message> entries = (Entry<String, Message>) iter.next();
			log.info("Key : " + entries.getKey() + " Value : " + entries.getValue());
		}
	}

	Configuration conf = new Configuration();


	private void writeToHdfs(File dataFile, String resultFile, String owner) {
		Path resultPath = new Path(resultFile);

		Configuration conf = new Configuration();
		try {
			hdfs = resultPath.getFileSystem(conf);

			if (!hdfs.exists(resultPath)) {
				hdfs.mkdirs(resultPath);
			}
		} catch (IOException e2) {
			log.error(e2.getMessage());
		}

		// write hdfs
		FSDataOutputStream out = null;
		try {
			Path file = new Path(resultPath, "Bo_message" + System.nanoTime() + "+"
					+ owner);
			log.info("Start : " + dataFile.getName() + ", write " + file.getName());
			out = hdfs.create(file);

		} catch (IOException e2) {
			log.error(e2.getMessage());
		}

		try {
			reader = new BufferedReader(new FileReader(dataFile));
			write(reader, out);
			out.close();
			log.info("Finish " + dataFile.getName());
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		} catch (IOException ioe) {
			log.error(ioe.getMessage());
		}

	}

	private void writeToLocal(File dataFile, String resultFile, String owner) {
		if (!new File(resultFile).exists()) {
			new File(resultFile).mkdirs();
		}

		File resultPath = new File(resultFile, "Bo_message" + System.nanoTime()
				+ "+" + owner);
		log.info("Start : " + dataFile.getName() + ", write " + resultPath.getName());
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(resultPath);
		} catch (IOException e1) {
			log.error(e1.getMessage());
		}
		try {
			reader = new BufferedReader(new FileReader(dataFile));
			write(reader, fos);
			fos.close();
			log.info("Finish " + dataFile.getName());
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		} catch (IOException ioe) {
			log.error(ioe.getMessage());
		}
	}

	private void write(BufferedReader reader, OutputStream output) {
		String dataLine = null;
		long beginTime = 0;
		try {
			while ((dataLine = reader.readLine()) != null) {
				if (dataLine.startsWith(dateFlag)) {
					Date startDate = new Date(dataLine
							.substring(dataLine.indexOf(dateFlag) + dateFlag.length(),
									dataLine.length()).trim());
					beginTime = startDate.getTime();
				}
				if (dataLine.contains("Rx")) {
					List<String> result = mf.messageBuilderList.get("BO_").dataParse(
							dataLine);
					for (String res : result) {
						output.write((res + "\n").getBytes());
						output.flush();
					}
				}
			}
		} catch (IOException ioe) {
			log.error(ioe.getMessage());
		}
	}

	class WriteThread extends Thread {
		File file;
		String path;
		String schema;
		String resultFile;

		public WriteThread(File file, String path) {
			setName("wt" + "-" + getId());
			this.path = path;
			this.file = file;
			URI uri = null;
			try {
				uri = new URI(path);
			} catch (URISyntaxException e) {
				log.error(e.getMessage());
			}
			this.schema = uri.getScheme();
			this.resultFile = uri.getPath();
		}

		public void run() {
			if (schema.toLowerCase().equals("hdfs")) {
				writeToHdfs(file, path, this.getName());
			} else if (schema.toLowerCase().equals("file")) {
				writeToLocal(file, resultFile, this.getName());
			}
		}

	}
}

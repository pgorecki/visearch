package pl.edu.uwm.wmii.visearch.representation;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.VectorWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.security.InvalidKeyException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.PathFilters;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterator;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirValueIterable;


import pl.edu.uwm.wmii.visearch.clustering.VM;
import pl.edu.uwm.wmii.visearch.core.ConfigFile;
import pl.edu.uwm.wmii.visearch.core.ImageToTextDriver;

public class Representation {

	private static final Logger log = LoggerFactory.getLogger(Representation.class);
	private static final Path BASE_DIR = new Path("visearch");
	private static final Path DESCRIPTORS_DIR = new Path("visearch/descriptors");
	private static final Path DICTIONARY_DIR = new Path("visearch/dictionary");
	private static final Path VISUAL_WORDS_DIR = new Path(
			"visearch/visualwords");

	
	private static void createInput(Configuration conf, Path inputDir,
			Path outputDir) throws Exception {

		//String descriptorsDir = configFile.get("descriptorsDir") + "/SIFT";
		//File files = new File(descriptorsDir);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		// wyczysc katalog z deskryptorami
		FileSystem fs = outputDir.getFileSystem(conf);
		fs.delete(outputDir, true);
		fs.mkdirs(outputDir);

		Path outputFile = new Path(outputDir, "all-descriptors");
		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf,
				outputFile, Text.class, VectorWritable.class);

		// key:value pair for output file
		Text key = new Text();
		VectorWritable val = new VectorWritable();

		// procedura tworzaca pojedynczy duzy plik z deskryptorami dla kazdego
		// obrazka
		// k: [hash obrazka]:[kolejny numer deskryptora]
		// v: [deskryptor, 128 elementow dla SIFT'a]
		int totalFiles = 0;
		int badFiles = 0;
		
		File files = new File(inputDir.toString());
		
		for (File f : files.listFiles()) {
			totalFiles++;
			String docId = f.getName().split("\\.")[0];
			log.info(String.valueOf(totalFiles));
			try {
				Document doc = dBuilder.parse(f);
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName("desc");
				for (int i = 0; i < nList.getLength(); i++) {
					String csv = nList.item(i).getTextContent();
					String[] csvParts = csv.split(",");
					double[] data = new double[csvParts.length];
					for (int j = 0; j < csvParts.length; j++) {
						data[j] = Integer.parseInt(csvParts[j].trim());
					}
					StringBuilder sb = new StringBuilder();
					sb.append(docId).append(":").append(i);
					key.set(sb.toString());
					val.set(new DenseVector(data));
					writer.append(key, val);
				}
			}
			catch (Exception e) {
				badFiles++;
				System.out.println(badFiles+"/"+totalFiles+" "+e);
			}
		}
		writer.close();
		System.out.println("Done making a bigfile, #files: "+(totalFiles-badFiles));
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = new Configuration();
		
		System.out.println("mem total (MB): "+Runtime.getRuntime().totalMemory()/(1024*1024));
		System.out.println("mem total: "+Runtime.getRuntime().totalMemory());
		System.out.println("mem free (a): "+Runtime.getRuntime().freeMemory());

		log.info("Configuration: "+conf.toString());
		log.info("fs.default.name: "+conf.get("fs.default.name"));

		FileSystem fs = FileSystem.get(conf);
		ConfigFile configFile = new ConfigFile("settings.cfg");
		
		boolean updateOnly = false;
		boolean makeBigfile = true;
		// input directory containing XML files
		Path inputDir = new Path(configFile.get("descriptorsDir") + "/SIFT");
		
		// output directory for HDFS bigfile
		Path bigfileDir = DESCRIPTORS_DIR;
		
		Path outputDir = VISUAL_WORDS_DIR;
		Path dictDir = DICTIONARY_DIR;
		
		int i=0;
		while (i<args.length) {
			String e = args[i++];
			
			if (e.equals("input")) {
				inputDir = new Path(args[i++]);
				continue;
			}
			
			if (e.equals("bigfile")) {
				bigfileDir = new Path(args[i++]);
				continue;
			}
			
			if (e.equals("output")) {
				outputDir = new Path(args[i++]);
				continue;
			}

			if (e.equals("dict")) {
				dictDir = new Path(args[i++]);
				continue;
			}
			
			if (e.equals("update")) {
				updateOnly = true;
				continue;
			}

			if (e.equals("skipbig")) {
				makeBigfile = false;
				continue;
			}
			
			System.out.println("Unknown option: "+e);
			
		}
		
		if (makeBigfile) {
			System.out.println("Skipping making bigfile");
			System.out.println("Input: "+inputDir);
			System.out.println("Bigfile: "+bigfileDir);
			createInput(conf, inputDir, bigfileDir);
		}
		else {
			System.out.println("Skipping making bigfile");
		}
		
		System.out.println("Running ImageToTextDriver");
		System.out.println("Bigfile: "+bigfileDir);
		System.out.println("Representation: "+outputDir);
		
		ImageToTextDriver.run(conf, bigfileDir, dictDir,
				outputDir, VM.RunSequential());
		System.out.println("Done.");

		System.out.println("Saving representations to db");
		
		String dbUrl = configFile.get("dbUrl");
		String dbUser = configFile.get("dbUser");
		String dbPass = configFile.get("dbPass");
		Connection dbConnection = DriverManager.getConnection("jdbc:" + dbUrl,
				dbUser, dbPass);
		log.info("Connected to {}", dbUrl);
		
		if (!updateOnly) {
			System.out.println("Erasing db");
			Statement statement = dbConnection.createStatement();
			statement.executeUpdate("DELETE FROM ImageRepresentations");
			statement.executeUpdate("DELETE FROM IFS");
		}

		for (Pair<Text, Text> entry : new SequenceFileDirIterable<Text, Text>(
				VISUAL_WORDS_DIR, PathType.LIST, conf)) {
			String docId = entry.getFirst().toString();
			String line = entry.getSecond().toString();
			StringTokenizer tokenizer = new StringTokenizer(line);
			
			System.out.println(docId);
			System.out.println("mem free (d): "+Runtime.getRuntime().freeMemory());
			
			Map<Integer, Integer> termFreq = new TreeMap<Integer, Integer>();
			while (tokenizer.hasMoreTokens()) {
				int key = Integer.parseInt(tokenizer.nextToken());
				if (termFreq.containsKey(key)) {
					termFreq.put(key, termFreq.get(key) + 1);
				} else {
					termFreq.put(key, 1);
				}
			}
			saveToDb(docId, termFreq, dbConnection);
			System.out.println("mem free (e): "+Runtime.getRuntime().freeMemory());
			System.gc();
			System.out.println("mem free (f): "+Runtime.getRuntime().freeMemory());
		}
		dbConnection.close();
		
		System.out.println("Done.");
	}
	
	
	private static void saveToDb(String docId, Map<Integer, Integer> termFreq,
			Connection dbConnection) throws InvalidKeyException, SQLException,
			IOException {

		String sql;
		PreparedStatement ps;
		
		System.out.println("Saving doc "+docId+", "+termFreq.size()+" terms");

		// build json string and IFS
		Iterator<Entry<Integer, Integer>> it = termFreq.entrySet().iterator();
		Map.Entry<Integer, Integer> e;
		String json = "{";
		while (it.hasNext()) {
			e = it.next();
			json += "\"" + e.getKey() + "\":\"" + e.getValue() + "\"";
			if (it.hasNext()) {
				json += ", ";
			}

			// save to IFS as well
			sql = "INSERT INTO IFS SELECT ?, ImageId FROM Images WHERE FileName LIKE ?";
			ps = dbConnection.prepareStatement(sql);
			ps.setInt(1, e.getKey());
			ps.setString(2, docId + "%");
			ps.executeUpdate();
		}
		json += "}";

		sql = "INSERT INTO ImageRepresentations SELECT ImageId, ? FROM Images WHERE FileName LIKE ?";
		ps = dbConnection.prepareStatement(sql);
		ps.setString(1, json);
		ps.setString(2, docId.toString() + "%");
		ps.executeUpdate();

	}


}

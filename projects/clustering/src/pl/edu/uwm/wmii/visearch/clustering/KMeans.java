package pl.edu.uwm.wmii.visearch.clustering;

/*
 * 
 * Z linii poleceń
 * /usr/local/mahout/bin/mahout kmeans -i kmeans/data1/in -c kmeans/data1/cl -o kmeans/data1/out -x 10 -k 2 -ow -cl
 *   opcja -ow nadpisuje katalog wyjściowy (nie trzeba ręcznie kasować)
 *   opcja -cl generuje katalog clusteredPoints, zawierający informację o tym który punt do którego klastra
 * /usr/local/mahout/bin/mahout clusterdump -i kmeans/data1/out/clusters-*-final
 * 
 * 
 */

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.clustering.conversion.InputDriver;
import org.apache.mahout.clustering.iterator.ClusterWritable;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.clustering.kmeans.RandomSeedGenerator;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.common.iterator.sequencefile.PathFilters;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirValueIterable;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.math.Vector;
import org.apache.mahout.utils.clustering.ClusterDumper;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;
import org.apache.mahout.clustering.classify.ClusterClassificationDriver;
import org.apache.mahout.clustering.classify.WeightedVectorWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;

import pl.edu.uwm.wmii.visearch.core.ConfigFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class KMeans {

	private static final boolean RUN_SEQUENTIAL = true;
	private static final Logger log = LoggerFactory.getLogger(KMeans.class);
	private static final Path BASE_DIR = new Path("visearch");
	private static final Path DESCRIPTORS_DIR = new Path("visearch/descriptors");
	private static final Path DICTIONARY_DIR = new Path("visearch/dictionary");
	private static final Path VISUAL_WORDS_DIR = new Path(
			"visearch/visualwords");
	private static final Path REPRESENTATIONS_DIR = new Path(
			"visearch/representations");

	private static void createInput(Configuration conf, ConfigFile configFile,
			Path outputDir) throws Exception {

		String descriptorsDir = configFile.get("descriptorsDir") + "/SIFT";
		File files = new File(descriptorsDir);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		FileSystem fs = outputDir.getFileSystem(conf);
		if (!fs.exists(outputDir)) {
			fs.mkdirs(outputDir);
		}

		// procedura tworzaca plik z deskryptorami dla kazdego obrazka
		for (File f : files.listFiles()) {
			String docId = f.getName().split("\\.")[0];
			Path outputFile = new Path(outputDir, docId);
			SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf,
					outputFile, Text.class, VectorWritable.class);

			// key:value pair for output file
			Text key = new Text();
			VectorWritable val = new VectorWritable();

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
				key.set(docId);
				val.set(new DenseVector(data));
				writer.append(key, val);
			}
			writer.close();
		}
	}

	private static void runClustering(Configuration conf, int k) throws IOException,
			ClassNotFoundException, InterruptedException {

		FileSystem fs = FileSystem.get(conf);
		Path clusters = new Path(BASE_DIR, new Path("initial-clusters"));

		fs.delete(DICTIONARY_DIR, true);

		DistanceMeasure measure = new EuclideanDistanceMeasure();
		double convergenceDelta = 0.01;
		int maxIterations = 10;

		// Random clusters
		clusters = RandomSeedGenerator.buildRandom(conf, DESCRIPTORS_DIR,
				clusters, k, measure);
		log.info("Random clusters generated, running K-Means");

		KMeansDriver.run(conf, DESCRIPTORS_DIR, clusters, DICTIONARY_DIR,
				measure, convergenceDelta, maxIterations, true, 0.0,
				RUN_SEQUENTIAL);

		log.info("KMeans done");

	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		log.info(conf.toString());
		log.info(conf.get("fs.default.name")); 

		FileSystem fs = FileSystem.get(conf);
		ConfigFile configFile = new ConfigFile("settings.cfg");

		// stworz pliki z deskryptorami na podstawie xml'i
		// TODO: w takim formacie Anazyler powinien zapisywac pliki od razu do
		// HDFS
		createInput(conf, configFile, DESCRIPTORS_DIR);

		// uruchom K-Means dla deskryptorow
		int k = Integer.parseInt(configFile.get("dictionarySize"));
		runClustering(conf, k);

		// musze przejsc przez kazdy plik z deskryptorami
		// i zapuscic osobno klasyfikacje, bo inaczej nie mam
		// informacji o tym, z jakiego obrazka pochodzi deskryptor
		// wiec nie wiem tez jak zbudowac reprezentacje
		// TODO: kazda iteracja takiej petli powinna pojsc jako osobny MAP
		for (FileStatus f : fs.listStatus(DESCRIPTORS_DIR)) {
			// przydzielenie punktow do klastrow i zapisanie w pliku
			ClusterClassificationDriver.run(conf, new Path(DESCRIPTORS_DIR, f
					.getPath().getName()),
			// DESCRIPTORS_DIR,
					DICTIONARY_DIR, new Path(VISUAL_WORDS_DIR, f.getPath()
							.getName()), 0.0, true, RUN_SEQUENTIAL);

		}

		Path representationsFile = new Path(REPRESENTATIONS_DIR, "part-0");
		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf,
				representationsFile, Text.class, VectorWritable.class);

		// key:value pair for output file
		Text key = new Text();
		VectorWritable val = new VectorWritable();

		// stworzenie histogramu i zapisanie do pliku jako sparse vector
		for (FileStatus f : fs.listStatus(VISUAL_WORDS_DIR)) {
			if (f.isDir()) {
				RandomAccessSparseVector freq = new RandomAccessSparseVector(10);
				for (Pair<IntWritable, WeightedVectorWritable> entry : new SequenceFileDirIterable<IntWritable, WeightedVectorWritable>(
						new Path(f.getPath(), "part-*"), PathType.GLOB, conf)) {
					int idx = entry.getFirst().get();
					freq.incrementQuick(idx, 1);
				}
				key.set(f.getPath().getName());
				val.set(freq);
				writer.append(key, val);
				
				log.info("REP: {}",key);
			}
		}
		writer.close();

		saveToDb(conf, configFile);

		log.info("Done");
	}

	private static void saveToDb(Configuration conf, ConfigFile configFile)
			throws InvalidKeyException, SQLException, IOException {
		String dbUrl = configFile.get("dbUrl");
		String dbUser = configFile.get("dbUser");
		String dbPass = configFile.get("dbPass");
		Connection dbConnection = DriverManager.getConnection("jdbc:" + dbUrl,
				dbUser, dbPass);
		log.info("Connected to {}", dbUrl);
		
		Statement statement = dbConnection.createStatement();
		statement.executeUpdate("DELETE FROM ImageRepresentations");
		statement.executeUpdate("DELETE FROM IFS");
		
		
		Path representationsFile = new Path(REPRESENTATIONS_DIR, "part-0");
		SequenceFile.Reader reader = new SequenceFile.Reader(FileSystem.get(conf), representationsFile, conf);
		Text key = new Text();
		VectorWritable val = new VectorWritable();
		String sql;
		while (reader.next(key, val)) {
			// save representation
			PreparedStatement ps;
			
			sql = "INSERT INTO ImageRepresentations SELECT ImageId, ? FROM Images WHERE FileName LIKE ?";
			ps = dbConnection.prepareStatement(sql);
			ps.setString(1, val.toString());
			ps.setString(2, key.toString()+"%");
			ps.executeUpdate();
			
			
			// save IFS
			Iterator<Vector.Element> it = val.get().nonZeroes().iterator();			
		
			while( it.hasNext()) {
				Vector.Element element = it.next();
				sql = "INSERT INTO IFS SELECT ?, ImageId FROM Images WHERE FileName LIKE ?";
				ps = dbConnection.prepareStatement(sql);
				ps.setInt(1, element.index());
				ps.setString(2, key.toString()+"%");
				ps.executeUpdate();
			}
		}

		dbConnection.close();

	}

}

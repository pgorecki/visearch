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
import java.util.ArrayList;
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
import org.apache.mahout.clustering.iterator.ClusteringPolicy;
import org.apache.mahout.clustering.iterator.KMeansClusteringPolicy;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.clustering.kmeans.RandomSeedGenerator;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.common.iterator.sequencefile.PathFilters;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirValueIterable;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.utils.clustering.ClusterDumper;
import org.apache.mahout.clustering.classify.ClusterClassificationDriver;
import org.apache.mahout.clustering.classify.ClusterClassifier;
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

public class KMeansToy {

	private static final Logger log = LoggerFactory.getLogger(KMeansToy.class);
	/**
	 * @param conf 
	 * @param configFile 
	 * @param args
	 */
	
	private static void setInput(Configuration conf, ConfigFile configFile) throws Exception {
		List<DenseVector> points = new ArrayList<DenseVector>();
		
		points.add(new DenseVector(new double[] {1.0,1.0}));
		points.add(new DenseVector(new double[] {0.0,1.0}));
		points.add(new DenseVector(new double[] {1.0,0.0}));

		points.add(new DenseVector(new double[] {11.0,11.0}));
		points.add(new DenseVector(new double[] {10.0,11.0}));
		points.add(new DenseVector(new double[] {11.0,10.0}));

		points.add(new DenseVector(new double[] {-11.0,-11.0}));
		points.add(new DenseVector(new double[] {-10.0,-11.0}));
		points.add(new DenseVector(new double[] {-11.0,-10.0}));

		
		Path path = new Path("kmeans/toy1/in/points.vec");
		FileSystem fs = path.getFileSystem(conf);

		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, path, IntWritable.class, VectorWritable.class);
		int i = 0;
		IntWritable key = new IntWritable();
		VectorWritable val = new VectorWritable();
		for (DenseVector p : points) {
			key.set(i++);
			val.set(p);
			writer.append(key, val);
		}
		writer.close();		
	}
	
	private static void runClustering(Configuration conf) throws IOException, ClassNotFoundException, InterruptedException {
		Path input 		= new Path("kmeans/toy1/in");
		Path clusters 	= new Path("kmeans/toy1/cl");
		Path output 	= new Path("kmeans/toy1/out");
		
		DistanceMeasure measure = new EuclideanDistanceMeasure();
		int k = 3;
		double convergenceDelta = 0.5;
		int maxIterations = 10;
		boolean runSequential = true;
		
		// delete output dir
		FileSystem.get(conf).delete(output, true);
		FileSystem.get(conf).mkdirs(output);
		
		FileSystem.get(conf).delete(clusters, true);
		FileSystem.get(conf).mkdirs(clusters);

		// Random clusters
		log.info("Random clusters points....");
		clusters = RandomSeedGenerator.buildRandom(conf, input, clusters, k, measure);
		log.info(clusters.toString());
		
	    log.info("Running KMeans");

	    // TODO: ustawić flagę -cl (klasyfikacja?)
		log.info(input.toString());
		log.info(clusters.toString());
		log.info(output.toString());
	    KMeansDriver.run(conf, input, clusters, output, measure, convergenceDelta,
	        maxIterations, false, 0.0, runSequential);
	    
	    log.info("KMeans done");
	}
	
	
	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();
		log.info("*** TOY KMEANS ***");
		log.info(conf.toString());
		log.info(conf.get("fs.default.name"));

		ConfigFile configFile = new ConfigFile("settings.cfg");
		
		setInput(conf, configFile);
		runClustering(conf);
		
		
		log.info("aa");
		String outDir = "kmeans/toy1/out/clusters-*-final";
	    for (ClusterWritable cw :
	           new SequenceFileDirValueIterable<ClusterWritable>(new Path(outDir, "part-*"), PathType.GLOB, conf)) {
	    	Cluster c = cw.getValue();
	    	//System.out.println(c.toString());
	    	System.out.println(c.getCenter().toString());
	    }
	    log.info("cc");
	    
	    NamedVector x; 
	    
	   
	    // simple classification
	    //ClusterClassifier.writePolicy(new KMeansClusteringPolicy(), new Path("kmeans/toy1/out"));
	    ClusterClassificationDriver.run(conf, 
	    		new Path("kmeans/toy1/in"),
	    		new Path("kmeans/toy1/out"),
	    		new Path("kmeans/toy1/words"),
	    		0.0,
	    		true,  // emit most likely
	    		true); // run sequential?
	    
	    Path clusterOutputPath = new Path("kmeans/toy1/out");
	    FileSystem fileSystem = clusterOutputPath.getFileSystem(conf);
	    FileStatus[] clusterFiles = fileSystem.listStatus(clusterOutputPath, PathFilters.finalPartFilter());
	    Path policyPath = clusterFiles[0].getPath();
	    
	    //ClusteringPolicy policy = ClusterClassifier.readPolicy()

	    //ClusteringPolicy policy = ClusterClassifier.readPolicy(finalClustersPath(conf, new Path("kmeans/toy1/out")));
	    
	    // read results
	    for (WeightedVectorWritable wvw :
	           new SequenceFileDirValueIterable<WeightedVectorWritable>(new Path("kmeans/toy1/words", "part-*"), PathType.GLOB, conf)) {
	    	System.out.println(wvw.toString());
	    }
	    
	    log.info("dd");
	    
	    for (Pair<IntWritable, WeightedVectorWritable> entry:
	    	new SequenceFileDirIterable<IntWritable, WeightedVectorWritable>(new Path("kmeans/toy1/words", "part-*"), PathType.GLOB, conf)) {
	    	
	    	System.out.println(entry.toString());
	    }
	    
	    
	    log.info("ee");
	    

		
		/*
	    Map<Integer, List<WeightedVectorWritable>> result = new TreeMap<Integer, List<WeightedVectorWritable>>();
	    for (Pair<IntWritable,WeightedVectorWritable> record :
	         new SequenceFileDirIterable<IntWritable,WeightedVectorWritable>(
	             pointsPathDir, PathType.LIST, PathFilters.logsCRCFilter(), conf)) {
	             */

		


		
	    log.info("Done!!!");

	    //InputDriver.runJob(input, directoryContainingConvertedInput, "org.apache.mahout.math.RandomAccessSparseVector");
	    /*
	    Path
	    
	    log.info("Running random seed to get initial clusters");
	    Path clusters = new Path(output, Cluster.INITIAL_CLUSTERS_DIR);
	    clusters = RandomSeedGenerator.buildRandom(conf, directoryContainingConvertedInput, clusters, k, measure);
	    log.info("Running KMeans");
	    KMeansDriver.run(conf, directoryContainingConvertedInput, clusters, output, measure, convergenceDelta,
	        maxIterations, true, 0.0, false);
	    // run ClusterDumper
	    ClusterDumper clusterDumper = new ClusterDumper(new Path(output, "clusters-*-final"), new Path(output,
	        "clusteredPoints"));
	    clusterDumper.printClusters(null);
		
		//SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path("outvec"), conf);
		
		*/

	}

}

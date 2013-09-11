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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.clustering.conversion.InputDriver;
import org.apache.mahout.clustering.iterator.ClusterWritable;
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
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.utils.clustering.ClusterDumper;
import org.apache.mahout.clustering.classify.WeightedVectorWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;


public class KMeans {

	private static final Logger log = LoggerFactory.getLogger(KMeans.class);
	/**
	 * @param conf 
	 * @param args
	 */
	
	private static void setInput(Configuration conf) throws IOException {
		List<DenseVector> points = new ArrayList<DenseVector>();
		
		points.add(new DenseVector(new double[] {1.0,1.0}));
		points.add(new DenseVector(new double[] {2.0,1.0}));
		points.add(new DenseVector(new double[] {3.0,1.0}));

		points.add(new DenseVector(new double[] {1.0,10.0}));
		points.add(new DenseVector(new double[] {2.0,10.0}));
		points.add(new DenseVector(new double[] {3.0,10.0}));

		points.add(new DenseVector(new double[] {1.0,20.0}));
		points.add(new DenseVector(new double[] {2.0,20.0}));
		points.add(new DenseVector(new double[] {3.0,20.0}));

		Path path = new Path("kmeans/data1/in/points.vec");
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
		Path input 		= new Path("kmeans/data1/in");
		Path clusters 	= new Path("kmeans/data1/cl");
		Path output 	= new Path("kmeans/data1/out");
		
		DistanceMeasure measure = new EuclideanDistanceMeasure();
		int k = 2;
		double convergenceDelta = 0.5;
		int maxIterations = 10;
		boolean runSequential = true;
		
		
		// Random clusters
		log.info("Random clusters points....");
		clusters = RandomSeedGenerator.buildRandom(conf, input, clusters, k, measure);
		log.info(clusters.toString());
		
	    log.info("Running KMeans");
	    // TODO: ustawić flagę -ow (overwrite)
	    // TODO: ustawić flagę -cl (klasyfikacja?)
	    KMeansDriver.run(conf, input, clusters, output, measure, convergenceDelta,
	        maxIterations, true, 0.0, runSequential);
	    
	    log.info("KMeans done");
		
	}
	
	
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		log.info(conf.toString());
		log.info(conf.get("fs.default.name"));

		log.info("saving Input to HDFS");
		setInput(conf);
		
		runClustering(conf);
		
		
		log.info("aa");
		String outDir = "kmeans/data1/out/clusters-*-final";
	    for (ClusterWritable cw :
	           new SequenceFileDirValueIterable<ClusterWritable>(new Path(outDir, "part-*"), PathType.GLOB, conf)) {
	    	Cluster c = cw.getValue();
	    	log.info(c.toString());
	    	log.info(c.getCenter().toString());
	    }
	    log.info("cc");
	    
	    

		
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

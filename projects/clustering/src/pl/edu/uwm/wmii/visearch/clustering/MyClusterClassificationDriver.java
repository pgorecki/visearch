package pl.edu.uwm.wmii.visearch.clustering;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.clustering.iterator.ClusterWritable;
import org.apache.mahout.clustering.iterator.ClusteringPolicy;
import org.apache.mahout.common.AbstractJob;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.commandline.DefaultOptionCreator;
import org.apache.mahout.common.iterator.sequencefile.PathFilters;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirValueIterable;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirValueIterator;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.VectorWritable;

import org.apache.mahout.clustering.classify.ClusterClassificationDriver;
import org.apache.mahout.clustering.classify.ClusterClassificationMapper;
import org.apache.mahout.clustering.classify.ClusterClassifier;
import org.apache.mahout.clustering.classify.WeightedVectorWritable;
import org.apache.mahout.clustering.classify.ClusterClassificationConfigKeys;

/**
 * Modyfikacja ClusterClassificationDriver polegająca na tym, że na wyjściu
 * zapisywane są klucze wektorow wejsciowych (zamiast weigthed vector writable,
 * czyli [pdf:wartosci] kazdego wektora)
 */
public final class MyClusterClassificationDriver extends AbstractJob {

	/**
	 * CLI to run Cluster Classification Driver.
	 */
	public int run(String[] args) throws Exception {

		addInputOption();
		addOutputOption();
		addOption(DefaultOptionCreator.methodOption().create());
		addOption(DefaultOptionCreator
				.clustersInOption()
				.withDescription(
						"The input centroids, as Vectors.  Must be a SequenceFile of Writable, Cluster/Canopy.")
				.create());

		if (parseArguments(args) == null) {
			return -1;
		}

		Path input = getInputPath();
		Path output = getOutputPath();

		if (getConf() == null) {
			setConf(new Configuration());
		}
		Path clustersIn = new Path(
				getOption(DefaultOptionCreator.CLUSTERS_IN_OPTION));
		boolean runSequential = getOption(DefaultOptionCreator.METHOD_OPTION)
				.equalsIgnoreCase(DefaultOptionCreator.SEQUENTIAL_METHOD);

		double clusterClassificationThreshold = 0.0;
		if (hasOption(DefaultOptionCreator.OUTLIER_THRESHOLD)) {
			clusterClassificationThreshold = Double
					.parseDouble(getOption(DefaultOptionCreator.OUTLIER_THRESHOLD));
		}

		run(getConf(), input, clustersIn, output,
				clusterClassificationThreshold, true, runSequential);

		return 0;
	}

	/**
	 * Constructor to be used by the ToolRunner.
	 */
	private MyClusterClassificationDriver() {
	}

	public static void main(String[] args) throws Exception {
		ToolRunner.run(new Configuration(),
				new MyClusterClassificationDriver(), args);
	}

	/**
	 * Uses {@link ClusterClassifier} to classify input vectors into their
	 * respective clusters.
	 * 
	 * @param input
	 *            the input vectors
	 * @param clusteringOutputPath
	 *            the output path of clustering ( it reads clusters-*-final file
	 *            from here )
	 * @param output
	 *            the location to store the classified vectors
	 * @param clusterClassificationThreshold
	 *            the threshold value of probability distribution function from
	 *            0.0 to 1.0. Any vector with pdf less that this threshold will
	 *            not be classified for the cluster.
	 * @param runSequential
	 *            Run the process sequentially or in a mapreduce way.
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	public static void run(Path input, Path clusteringOutputPath, Path output,
			Double clusterClassificationThreshold, boolean emitMostLikely,
			boolean runSequential) throws IOException, InterruptedException,
			ClassNotFoundException {
		Configuration conf = new Configuration();
		run(conf, input, clusteringOutputPath, output,
				clusterClassificationThreshold, emitMostLikely, runSequential);
	}

	public static void run(Configuration conf, Path input,
			Path clusteringOutputPath, Path output,
			Double clusterClassificationThreshold, boolean emitMostLikely,
			boolean runSequential) throws IOException, InterruptedException,
			ClassNotFoundException {
		if (runSequential) {
			classifyClusterSeq(conf, input, clusteringOutputPath, output,
					clusterClassificationThreshold, emitMostLikely);
		} else {
			classifyClusterMR(conf, input, clusteringOutputPath, output,
					clusterClassificationThreshold, emitMostLikely);
		}

	}

	private static void classifyClusterSeq(Configuration conf, Path input,
			Path clusters, Path output, Double clusterClassificationThreshold,
			boolean emitMostLikely) throws IOException {
		List<Cluster> clusterModels = populateClusterModels(clusters, conf);
		ClusteringPolicy policy = ClusterClassifier
				.readPolicy(finalClustersPath(conf, clusters));
		ClusterClassifier clusterClassifier = new ClusterClassifier(
				clusterModels, policy);
		selectCluster(input, clusterModels, clusterClassifier, output,
				clusterClassificationThreshold, emitMostLikely);

	}

	/**
	 * Populates a list with clusters present in clusters-*-final directory.
	 * 
	 * @param clusterOutputPath
	 *            The output path of the clustering.
	 * @param conf
	 *            The Hadoop Configuration
	 * @return The list of clusters found by the clustering.
	 * @throws IOException
	 */
	private static List<Cluster> populateClusterModels(Path clusterOutputPath,
			Configuration conf) throws IOException {
		List<Cluster> clusterModels = Lists.newArrayList();
		Path finalClustersPath = finalClustersPath(conf, clusterOutputPath);
		Iterator<?> it = new SequenceFileDirValueIterator<Writable>(
				finalClustersPath, PathType.LIST, PathFilters.partFilter(),
				null, false, conf);
		while (it.hasNext()) {
			ClusterWritable next = (ClusterWritable) it.next();
			Cluster cluster = next.getValue();
			cluster.configure(conf);
			clusterModels.add(cluster);
		}
		return clusterModels;
	}

	private static Path finalClustersPath(Configuration conf,
			Path clusterOutputPath) throws IOException {
		FileSystem fileSystem = clusterOutputPath.getFileSystem(conf);
		FileStatus[] clusterFiles = fileSystem.listStatus(clusterOutputPath,
				PathFilters.finalPartFilter());
		return clusterFiles[0].getPath();
	}

	/**
	 * Classifies the vector into its respective cluster.
	 * 
	 * @param input
	 *            the path containing the input vector.
	 * @param clusterModels
	 *            the clusters
	 * @param clusterClassifier
	 *            used to classify the vectors into different clusters
	 * @param output
	 *            the path to store classified data
	 * @param clusterClassificationThreshold
	 * @param emitMostLikely
	 *            TODO
	 * @throws IOException
	 */
	private static void selectCluster(Path input, List<Cluster> clusterModels,
			ClusterClassifier clusterClassifier, Path output,
			Double clusterClassificationThreshold, boolean emitMostLikely)
			throws IOException {
		Configuration conf = new Configuration();
		SequenceFile.Writer writer = new SequenceFile.Writer(
				input.getFileSystem(conf), conf,
				new Path(output, "part-m-" + 0), IntWritable.class, Text.class);
		for (Pair<Text, VectorWritable> entry : new SequenceFileDirIterable<Text, VectorWritable>(
				input, PathType.LIST, PathFilters.logsCRCFilter(), conf)) {
			Vector pdfPerCluster = clusterClassifier.classify(entry.getSecond()
					.get());
			if (shouldClassify(pdfPerCluster, clusterClassificationThreshold)) {
				classifyAndWrite(clusterModels, clusterClassificationThreshold,
						emitMostLikely, writer, entry.getFirst(), pdfPerCluster);
			}
		}
		writer.close();
	}

	private static void classifyAndWrite(List<Cluster> clusterModels,
			Double clusterClassificationThreshold, boolean emitMostLikely,
			SequenceFile.Writer writer, Text key, Vector pdfPerCluster)
			throws IOException {
		if (emitMostLikely) {
			int maxValueIndex = pdfPerCluster.maxValueIndex();
			write(clusterModels, writer, key, maxValueIndex);
		} else {
			writeAllAboveThreshold(clusterModels,
					clusterClassificationThreshold, writer, key, pdfPerCluster);
		}
	}

	private static void writeAllAboveThreshold(List<Cluster> clusterModels,
			Double clusterClassificationThreshold, SequenceFile.Writer writer,
			Text key, Vector pdfPerCluster) throws IOException {
		for (Element pdf : pdfPerCluster.nonZeroes()) {
			if (pdf.get() >= clusterClassificationThreshold) {
				int clusterIndex = pdf.index();
				write(clusterModels, writer, key, clusterIndex);
				// nie ma sensu zapisywac wiecej niz 1, bo i tak nie zapisujemy
				// wagi
				break;
			}
		}
	}

	private static void write(List<Cluster> clusterModels,
			SequenceFile.Writer writer, Text key, int maxValueIndex)
			throws IOException {
		Cluster cluster = clusterModels.get(maxValueIndex);
		System.out.println(cluster.getId() + " -> " + key);
		writer.append(new IntWritable(cluster.getId()), key);
	}

	/**
	 * Decides whether the vector should be classified or not based on the max
	 * pdf value of the clusters and threshold value.
	 * 
	 * @return whether the vector should be classified or not.
	 */
	private static boolean shouldClassify(Vector pdfPerCluster,
			Double clusterClassificationThreshold) {
		return pdfPerCluster.maxValue() >= clusterClassificationThreshold;
	}

	private static void classifyClusterMR(Configuration conf, Path input,
			Path clustersIn, Path output,
			Double clusterClassificationThreshold, boolean emitMostLikely)
			throws IOException, InterruptedException, ClassNotFoundException {

		conf.setFloat(
				ClusterClassificationConfigKeys.OUTLIER_REMOVAL_THRESHOLD,
				clusterClassificationThreshold.floatValue());
		conf.setBoolean(ClusterClassificationConfigKeys.EMIT_MOST_LIKELY,
				emitMostLikely);
		conf.set(ClusterClassificationConfigKeys.CLUSTERS_IN, clustersIn
				.toUri().toString());

		Job job = new Job(conf,
				"Cluster Classification Driver running over input: " + input);
		job.setJarByClass(ClusterClassificationDriver.class);

		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		job.setMapperClass(MyClusterClassificationMapper.class);
		job.setNumReduceTasks(0);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job, input);
		FileOutputFormat.setOutputPath(job, output);
		if (!job.waitForCompletion(true)) {
			throw new InterruptedException(
					"My Cluster Classification Driver Job failed processing "
							+ input);
		}
	}

	public static void run(Configuration conf, Path input,
			Path clusteringOutputPath, Path output,
			double clusterClassificationThreshold, boolean emitMostLikely,
			boolean runSequential) throws IOException, InterruptedException,
			ClassNotFoundException {
		if (runSequential) {
			classifyClusterSeq(conf, input, clusteringOutputPath, output,
					clusterClassificationThreshold, emitMostLikely);
		} else {
			classifyClusterMR(conf, input, clusteringOutputPath, output,
					clusterClassificationThreshold, emitMostLikely);
		}

	}

}

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
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.clustering.classify.ClusterClassifier;
import org.apache.mahout.clustering.classify.ClusterClassificationConfigKeys;
import org.apache.mahout.clustering.iterator.ClusterWritable;
import org.apache.mahout.clustering.iterator.ClusteringPolicy;
import org.apache.mahout.common.iterator.sequencefile.PathFilters;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirValueIterator;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.VectorWritable;

/**
 * Mapper for classifying vectors into clusters.
 */
public class ImageToTextMapper extends
    Mapper<WritableComparable<?>,VectorWritable,IntWritable,WritableComparable<?>> {
  
  private double threshold;
  private List<Cluster> clusterModels;
  private ClusterClassifier clusterClassifier;
  private IntWritable clusterId;
  private boolean emitMostLikely;
  
  @Override
  protected void setup(Context context) throws IOException, InterruptedException {
    super.setup(context);
    
    Configuration conf = context.getConfiguration();
    String clustersIn = conf.get(ClusterClassificationConfigKeys.CLUSTERS_IN);
    threshold = conf.getFloat(ClusterClassificationConfigKeys.OUTLIER_REMOVAL_THRESHOLD, 0.0f);
    emitMostLikely = conf.getBoolean(ClusterClassificationConfigKeys.EMIT_MOST_LIKELY, false);
    
    clusterModels = Lists.newArrayList();
    
    if (clustersIn != null && !clustersIn.isEmpty()) {
      Path clustersInPath = new Path(clustersIn);
      clusterModels = populateClusterModels(clustersInPath, conf);
      ClusteringPolicy policy = ClusterClassifier
          .readPolicy(finalClustersPath(clustersInPath));
      clusterClassifier = new ClusterClassifier(clusterModels, policy);
    }
    clusterId = new IntWritable();
  }
  
  /**
   * Mapper which classifies the vectors to respective clusters.
   */
  @Override
  protected void map(WritableComparable<?> key, VectorWritable vw, Context context)
    throws IOException, InterruptedException {
    if (!clusterModels.isEmpty()) {
      Vector pdfPerCluster = clusterClassifier.classify(vw.get());
      if (shouldClassify(pdfPerCluster)) {
        if (emitMostLikely) {
          int maxValueIndex = pdfPerCluster.maxValueIndex();
          write(key, context, maxValueIndex, 1.0);
        } else {
          writeAllAboveThreshold(key, context, pdfPerCluster);
        }
      }
    }
  }
  
  private void writeAllAboveThreshold(WritableComparable<?> key, Context context,
      Vector pdfPerCluster) throws IOException, InterruptedException {
    for (Element pdf : pdfPerCluster.nonZeroes()) {
      if (pdf.get() >= threshold) {
        int clusterIndex = pdf.index();
        write(key, context, clusterIndex, pdf.get());
      }
    }
  }
  
  private void write(WritableComparable<?> key, Context context, int clusterIndex, double weight)
    throws IOException, InterruptedException {
    Cluster cluster = clusterModels.get(clusterIndex);
    clusterId.set(cluster.getId());
    context.write(clusterId, key);
  }
  
  public static List<Cluster> populateClusterModels(Path clusterOutputPath, Configuration conf) throws IOException {
    List<Cluster> clusters = Lists.newArrayList();
    FileSystem fileSystem = clusterOutputPath.getFileSystem(conf);
    FileStatus[] clusterFiles = fileSystem.listStatus(clusterOutputPath, PathFilters.finalPartFilter());
    Iterator<?> it = new SequenceFileDirValueIterator<Writable>(
        clusterFiles[0].getPath(), PathType.LIST, PathFilters.partFilter(),
        null, false, conf);
    while (it.hasNext()) {
      ClusterWritable next = (ClusterWritable) it.next();
      Cluster cluster = next.getValue();
      cluster.configure(conf);
      clusters.add(cluster);
    }
    return clusters;
  }
  
  private boolean shouldClassify(Vector pdfPerCluster) {
    return pdfPerCluster.maxValue() >= threshold;
  }
  
  private static Path finalClustersPath(Path clusterOutputPath) throws IOException {
    FileSystem fileSystem = clusterOutputPath.getFileSystem(new Configuration());
    FileStatus[] clusterFiles = fileSystem.listStatus(clusterOutputPath, PathFilters.finalPartFilter());
    return clusterFiles[0].getPath();
  }
}


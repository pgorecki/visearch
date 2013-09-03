package pl.edu.uwm.wmii.visearch.analyzer;


/**
 * Creation at: Apr 10, 2013
 * 
 */



import java.awt.List;
import org.opencv.core.*;

import org.opencv.highgui.Highgui;
import org.opencv.imgproc.*;
import org.opencv.features2d.*;

/**
 * @author ksopyla
 *
 */
public class KeyPointExtractor {

	/**
	 * @param imageName
	 * @param method
	 * @param kp - output parameter
	 * @param des - output parameter
	 */
	public void Extract(String imageName, ExtractionMethod method,
			MatOfKeyPoint kp, Mat des) {
		// TODO Auto-generated method stub
		
		Mat imSrc = Highgui.imread(imageName);
		
		FeatureDetector fd= GetFeatureDetector(method);
		
		
		fd.detect(imSrc, kp); 
		
		DescriptorExtractor desExt = GetDescriptor(method); 		
		desExt.compute(imSrc, kp,des);

		
	}

	/**
	 * @param method
	 * @return
	 */
	private DescriptorExtractor GetDescriptor(ExtractionMethod method) {
		// TODO Auto-generated method stub
		
		
		switch (method) {
		case SIFT:
			return DescriptorExtractor.create(DescriptorExtractor.SIFT);
		case SURF:
			return DescriptorExtractor.create(DescriptorExtractor.SURF);
		}
		return null;
	}

	/**
	 * @param method
	 * @return
	 */
	private FeatureDetector GetFeatureDetector(ExtractionMethod method) {
		// TODO Auto-generated method stub
		switch (method) {
		case SIFT:
			return FeatureDetector.create(FeatureDetector.SIFT);
		case SURF:
			return FeatureDetector.create(FeatureDetector.SURF);
		}
		return null;
	}

	
	
	

}

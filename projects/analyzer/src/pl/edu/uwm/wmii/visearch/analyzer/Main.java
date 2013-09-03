package pl.edu.uwm.wmii.visearch.analyzer;

/**
 * Creation at: Apr 9, 2013
 * 
 */

import pl.edu.uwm.wmii.visearch.core.*;
import java.util.Date;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.opencv.core.*;

import org.opencv.highgui.Highgui;
import org.opencv.imgproc.*;
import org.opencv.features2d.*;

import pl.edu.uwm.wmii.visearch.core.ConfigFile;

/**
 * @author ksopyla
 * 
 */
public class Main {

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) throws Exception {

		ConfigFile configFile = new ConfigFile("settings.cfg");

		String descriptorsDir = configFile.get("desriptorsDir");
		String imagesDir = configFile.get("imagesDir");

		System.out.println("Welcome to OpenCV " + Core.VERSION);

		Date dt = new Date();

		System.out.println("start detecting " + dt.toString());

		ExtractionMethod exMethod = ExtractionMethod.SIFT;

		KeyPointExtractor extractor = new KeyPointExtractor();
		KeyPointXMLStorage storage = new KeyPointXMLStorage();
		DbManagement manager = new DbManagement(configFile.get("dbUrl"),
				configFile.get("dbUser"), configFile.get("dbPass")
		);
		manager.connect();
		
		List<String> paths = manager.getImagePaths(exMethod);
		while (!paths.isEmpty()) {

			for (String imageName : paths) {

				MatOfKeyPoint kp = new MatOfKeyPoint();
				Mat des = new Mat();

				extractor.Extract(imagesDir+"/"+imageName, exMethod, kp, des);
				Path p = Paths.get(imageName);
				String fileName = p.getFileName().toString();
				String descr = exMethod.toString();
				String descriptorXMLFile = descr + "/"
						+ fileName + ".xml";
				storage.save(kp, des, descriptorsDir + "/" + descriptorXMLFile, exMethod);
				manager.InsertDescriptor(fileName, exMethod, descriptorXMLFile);
				paths = manager.getImagePaths(exMethod);
			}
		}
		
		manager.disconnect();

		dt = new Date();
		System.out.println("end detecting " + dt.toString());

	}

}

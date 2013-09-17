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

		String descriptorsDir = configFile.get("descriptorsDir");
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


		long counter=1;
		List<String> pathsDB = manager.getImagePaths(exMethod);
		while (!pathsDB.isEmpty()) {

			for (String imageName : pathsDB) {

				MatOfKeyPoint kp = new MatOfKeyPoint();
				Mat des = new Mat();

				Path imagePath = Paths.get(imagesDir+"/"+imageName);
				File imageFile = new File(imagePath.toString());

				if(imageFile.exists())
				{

					System.out.print(counter+ " process file: "+imagePath.toString());

					extractor.Extract(imagePath.toString(), exMethod, kp, des);
					String fileName = imagePath.getFileName().toString();
					String descr = exMethod.toString();
					String descriptorXMLFile = descr + "/"+ fileName + ".xml";

					storage.save(kp, des, descriptorsDir + "/" + descriptorXMLFile, exMethod);
					manager.InsertDescriptor(fileName, exMethod, descriptorXMLFile);
					System.out.println(" -- [Ok]");
				}else {
					System.out.println(" ->File not exitst:"+imagePath.toString());
				}

			}

			pathsDB = manager.getImagePaths(exMethod);
		}

		manager.disconnect();

		dt = new Date();
		System.out.println("end detecting " + dt.toString());

	}
}

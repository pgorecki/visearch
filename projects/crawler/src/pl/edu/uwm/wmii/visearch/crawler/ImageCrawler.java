package pl.edu.uwm.wmii.visearch.crawler;

import pl.edu.uwm.wmii.visearch.core.*;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// OPENCV:
// http://docs.opencv.org/2.4.4-beta/doc/tutorials/introduction/desktop_java/java_dev_intro.html

//import org.opencv.core.Core;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */

/*
 * This class shows how you can crawl images on the web and store them in a
 * folder. This is just for demonstration purposes and doesn't scale for large
 * number of images. For crawling millions of images you would need to store
 * downloaded images in a hierarchy of folders
 */
public class ImageCrawler extends WebCrawler {

	private static int SAVED_IMAGE_SIZE;

	private static final Pattern filters = Pattern
			.compile(".*(\\.(css|js|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf"
					+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

	private static final Pattern imgPatterns = Pattern
			.compile(".*(\\.(bmp|gif|jpe?g|png|tiff?))$");

	private static File storageFolder;
	private static String domainFilter;
	private static Connection dbConnection;

	public static void configure(String domain, String storageFolderName,
			Connection dbConnection, ConfigFile configFile)
			throws NumberFormatException, InvalidKeyException {
		ImageCrawler.domainFilter = domain;
		ImageCrawler.dbConnection = dbConnection;

		storageFolder = new File(storageFolderName);
		if (!storageFolder.exists()) {
			storageFolder.mkdirs();
		}
		
		SAVED_IMAGE_SIZE = Integer.parseInt(configFile.get("crawlerDownloadImageSize"));
	}

	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		String domain = url.getDomain().toLowerCase();
		
		//System.out.println("should visit "+href+"?");

		boolean result = true;

		// reject css, js, etc. as in filters
		if (filters.matcher(href).matches()) {
			result = false;
		}

		if (ImageCrawler.domainFilter != null
				&& !url.getDomain().contains(ImageCrawler.domainFilter)) {
			result = false;
		}

		//System.out.format("%s (%d): %s\n", domain, url.getDepth(), href);
		System.out.print(".");
		return result;
	}

	@Override
	public void visit(Page page) {

		String url = page.getWebURL().getURL();
		System.out.println("visiting "+url);

		// We are only interested in processing images
		if (!(page.getParseData() instanceof BinaryParseData)) {
			return;
		}

		if (!imgPatterns.matcher(url).matches()) {
			return;
		}


		// Get a unique name for storing this image
		String extension = url.substring(url.lastIndexOf("."));
		String hashedName = Cryptography.MD5(url)+".png";

		// Create destination directory for the image
		String[] parts = page.getWebURL().getDomain().split("\\.");
		String domainPath = "";
		for (int i = parts.length - 1; i >= 0; i--) {
			domainPath += parts[i] + "/";
		}
		
		String finalDirName = storageFolder.getAbsolutePath() + "/"
				+ domainPath;
		
		File finalDir = new File(finalDirName);
		if (!finalDir.exists()) {
			finalDir.mkdirs();
		}

		// Read, resize and save image
		try {
			System.out.println(url);


			InputStream in = new ByteArrayInputStream(page.getContentData());
			BufferedImage imgIn = ImageIO.read(in);
			double w = imgIn.getWidth();
			double h = imgIn.getHeight();
			if (Math.min(w, h) < 100.0) {
				System.out.println("  !!! too small");
				return;
			}
				
			
			// image aspect ratio
			if (Math.max(w/h, h/w) > 4.0) {
				System.out.println("  !!! bad aspect: " + Math.max(w/h, h/w));
				return;
			}
				

			Image scaled = null;
			if (w > h && w > SAVED_IMAGE_SIZE) {
				scaled = imgIn.getScaledInstance(SAVED_IMAGE_SIZE, -1,
						Image.SCALE_SMOOTH);
			} else if (w <= h && h > SAVED_IMAGE_SIZE) {
				scaled = imgIn.getScaledInstance(-1, SAVED_IMAGE_SIZE,
						Image.SCALE_SMOOTH);
			}

			BufferedImage imgOut;
			if (scaled != null) {
				imgOut = new BufferedImage(scaled.getWidth(null),
						scaled.getHeight(null), BufferedImage.TYPE_INT_RGB);
				Graphics bg = imgOut.getGraphics();
				bg.drawImage(scaled, 0, 0, null);
				bg.dispose();
			}
			else {
				imgOut = imgIn;
			}
			
			ImageIO.write(imgOut, "png", new File(finalDirName + hashedName));			
			System.out.println("  saved to: "+ finalDirName + hashedName);
			
			 //TUTAJ ZAPISAC OBRAZEK DO BAZY DANYCH
			 
			  Timestamp stamp = new Timestamp(System.currentTimeMillis());
			  Date date = new Date(stamp.getTime());
			  long timestamp = date.getTime() / 1000;
			  
			   Statement statement= dbConnection.createStatement();
							 
			   try {
				   String polecenie="INSERT INTO `Images`(`FileName`, `FileDirectory`, `URL`, `Created`)"+"VALUES ('"+hashedName+"','"+domainPath+"','"+page.getWebURL().getURL()+"', NOW())";
				   statement.executeUpdate(polecenie);
				   statement.close();
			   }
			   catch (MySQLIntegrityConstraintViolationException e) {
				   System.out.println("TODO: update w bazie");
			   }
			 
			System.out.println(url + " saved to" + finalDirName + hashedName);
		} catch (Exception e) {
			System.out.println("Failed: " + url);
			e.printStackTrace();
		}
	}
}
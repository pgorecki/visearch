package pl.edu.uwm.wmii.visearch.crawler;

import pl.edu.uwm.wmii.visearch.core.ConfigFile;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CrawlerManager {

	// private static CrawlerManager crawlerManager = null;

	public static void main(String[] args) throws Exception {
		if (args.length <= 0) {
			System.out.println("You must specify seed url.");
			return;
		}

		ConfigFile configFile = new ConfigFile("settings.cfg");

		// create database connection object
		String dbUrl = configFile.get("dbUrl");
		String dbUser = configFile.get("dbUser");
		String dbPass = configFile.get("dbPass");

		Connection dbConnection = DriverManager.getConnection("jdbc:" + dbUrl,
				dbUser, dbPass);
		System.out.println("Connected to " + dbUrl);

		int numberOfCrawlers = 1;
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder("storage");
		config.setPolitenessDelay(1000);
		// config.setMaxDepthOfCrawling(3);
		// config.setMaxPagesToFetch(100);
		config.setIncludeBinaryContentInCrawling(true);
		config.setResumableCrawling(true);

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotsTxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotsTxtConfig,
				pageFetcher);

		try {
			String domainFilter = null;
			CrawlController controller = new CrawlController(config,
					pageFetcher, robotstxtServer);
			controller.addSeed(args[0]);
			ImageCrawler.configure(domainFilter, configFile.get("imagesDir"),
					dbConnection);
			System.out.println("Starting crawler @" + args[0]);
			controller.start(ImageCrawler.class, numberOfCrawlers);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dbConnection.close();
		System.out.println("Done!");		
	}

}

package pl.edu.uwm.wmii.visearch.analyzer;

/**
 * Creation at: Apr 9, 2013
 * @author ksopyla
 * 
 */



//wmii imports
import pl.edu.uwm.wmii.visearch.core.*;

//standard import
import java.util.Date;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;

import java.security.InvalidKeyException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


//opencv imports
import org.opencv.core.*;

import org.opencv.highgui.Highgui;
import org.opencv.imgproc.*;
import org.opencv.features2d.*;

//command line parser
import org.apache.commons.cli.*;

/**
 * @author ksopyla
 * 
 */
public class Main {

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	static private CommandLine cmd;
	static private Options opt= new Options();

	public static void main(String[] args) throws Exception {

		createCommandParser(args);
		showHelp();

		
		if(cmd.hasOption("a")){
			//jeden parametr -a - to uruchamiamy w trybie przetwarzania wszystkich obrazów
			//Przetwarzane są obrazy ściągnięte przez crawlera i zapisane do bazy (przetwarzane są te co nie zostały wcześniej przetworzone)
			
			System.out.println("Compute all descriptors");
			computeAllDescriptors();
		}
		else if(cmd.hasOption('i') && cmd.hasOption('o'))
		{
			//przetwarzamy tylko jeden obrazek
			String imgParam = cmd.getOptionValue('i');
			String outFolder = cmd.getOptionValue('o');
			computeDescriptorsForImg(imgParam,outFolder);
			
			
//			Path imgPath = Paths.get(imgParam);			
//			String imgName = imgPath.getFileName().toString();
//			String descName = imgName+".xml";
//			String xmlPath =Paths.get(outFolder, descName).toString(); 
//			System.out.println(imgParam+"->"+outFolder);
//			System.out.println("Compute file "+imgParam);
//			System.out.println("Save xml file "+xmlPath);

			System.exit(0);
		} else {

			System.out.println("Wrong input parameters");
			System.exit(1);
		}

	}
	
	/**
	 * 
	 * @param imgPath - ścieżka do pliki obrazu
	 * @param outFolder - ścieżka do folderu do zapisu w xml'u deskryptorów
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private static void computeDescriptorsForImg(String imgPath, String outFolder) throws IOException,
	InvalidKeyException, SQLException, ClassNotFoundException {

		


		Date dt = new Date();
		

		//wybrany deskryptor
		ExtractionMethod exMethod = ExtractionMethod.SIFT;

		//klasa do ekstrakcji punktów kluczowych
		KeyPointExtractor extractor = new KeyPointExtractor();
		//klasa pozwalająca na zapis do pliku xml, wyekstrachowanych punktów kluczowych
		KeyPointXMLStorage storage = new KeyPointXMLStorage();

		

		//Tablica punktów kluczowych do wypełnienia
		MatOfKeyPoint kp = new MatOfKeyPoint();
		//tablica 2D deskryptorów
		Mat des = new Mat();

		Path imagePath = Paths.get(imgPath);
		File imageFile = new File(imgPath);

		if(imageFile.exists())
		{
			System.out.print(" process file: "+imgPath);

			long start=System.currentTimeMillis();
			
			//ekstrakcja punktów kluczowych oraz utworzenie deskryptorów
			extractor.Extract(imgPath, exMethod, kp, des);

			//utwórz scieżke zapisu dla pliku xml z deskryptorami
			String fileName = Paths.get(imgPath).getFileName().toString();
			String descr = exMethod.toString();
			String descriptorXMLFile = descr + "/"+ fileName + ".xml";
						
			//zapisz plik na dysku
			storage.save(kp, des, outFolder + "/" + descriptorXMLFile, exMethod);

			double elapsed = (0.0+System.currentTimeMillis()-start)/1000;
			
			System.out.println(", elapsed:"+elapsed+" -- [Ok]");
		}else {
			System.out.println(" ->File not exitst:"+imagePath.toString());
		}

	}
	

	/**
	 * Tworzy parser dla lini commend, korzysta z commons-cli (dodany jako pakiet maven)
	 * @param args
	 */
	private static void createCommandParser(String[] args) {
		

		opt.addOption("a","all", false, "compute descriptors for images from database");

		opt.addOption("i", "img", true, "path to image");
		opt.addOption("o","output", true, "output folder for xml file, which contains descriptors");

		opt.addOption("m", "method", true, "extraction method SIFT, SURF, default SIFT");


		// ** now lets parse the input
		CommandLineParser parser = new BasicParser();

		try{
			cmd = parser.parse(opt, args);

		}catch (ParseException pe){ 
			showHelp();
			return; 
		}

	}

	/**
	 * 
	 */
	private static void showHelp() {
		HelpFormatter formatter  = new HelpFormatter();
		formatter.printHelp("java -jar visearch.analyzer ", opt);
		System.out.println("");
	}

	/**
	 * Oblicza punkty kluczowe i deskryptory na wszystkich obrazach pobranych z bazy danych i zapisuje 
	 * w plikach xml w zależności do wybranego deskryptora. 
	 * Przetwarza pliki obrazów które zostały pobrane przez crawleara i jeszcze nie mają wyliczonych deskryptorów.
	 * Informacje o folderach w których zaspisuje pobierane są z pliku settings.cfg (domyślnie znajduje się on w /home/user/.visearch/
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private static void computeAllDescriptors() throws IOException,
	InvalidKeyException, SQLException, ClassNotFoundException {

		//odczytaj pliki z ustawieniami
		ConfigFile configFile = new ConfigFile("settings.cfg");

		//pobierz ścieżkę do folderu w którym mają być zapisane deskryptory
		String descriptorsDir = configFile.get("descriptorsDir");
		//pobierz ścieżkę do folderu w którym są zapisane ściągniete obrazy 
		String imagesDir = configFile.get("imagesDir");


		Date dt = new Date();
		//Rozpoczynamy detekcję
		System.out.println("** Start detecting " + dt.toString());

		//wybrany deskryptor
		ExtractionMethod exMethod = ExtractionMethod.SIFT;

		//klasa do ekstrakcji punktów kluczowych
		KeyPointExtractor extractor = new KeyPointExtractor();
		//klasa pozwalająca na zapis do pliku xml, wyekstrachowanych punktów kluczowych
		KeyPointXMLStorage storage = new KeyPointXMLStorage();

		//klasa dostępu do danych, url, user, pass zapisane są w pliku settings.cfg
		DbManagement manager = new DbManagement(configFile.get("dbUrl"),
				configFile.get("dbUser"), configFile.get("dbPass")
				);
		manager.connect();


		long counter=1;
		//pobieramy listę obrazów do przetworzenia
		List<String> pathsDB = manager.getImagePaths(exMethod);

		while (!pathsDB.isEmpty()) {
			//dla każdej ścieżki do obrazu
			for (String imageName : pathsDB) {

				//Tablica punktów kluczowych do wypełnienia
				MatOfKeyPoint kp = new MatOfKeyPoint();
				//tablica 2D deskryptorów
				Mat des = new Mat();

				Path imagePath = Paths.get(imagesDir+"/"+imageName);
				File imageFile = new File(imagePath.toString());

				if(imageFile.exists())
				{

					System.out.print(counter+ " process file: "+imagePath.toString());

					extractor.Extract(imagePath.toString(), exMethod, kp, des);

					//utwórz scieżke zapisu dla pliku xml z deskryptorami
					String fileName = imagePath.getFileName().toString();
					String descr = exMethod.toString();
					String descriptorXMLFile = descr + "/"+ fileName + ".xml";

					//zapisz plik na dysku
					storage.save(kp, des, descriptorsDir + "/" + descriptorXMLFile, exMethod);

					//oznacz w bazie że plik został przetworzony
					manager.InsertDescriptor(fileName, exMethod, descriptorXMLFile);


					System.out.println(" -- [Ok]");
				}else {
					System.out.println(" ->File not exitst:"+imagePath.toString());
				}

			}
			//pobierz następną paczkę obrazów
			pathsDB = manager.getImagePaths(exMethod);
		}

		manager.disconnect();

		dt = new Date();
		System.out.println("** End detecting " + dt.toString());
	}
}

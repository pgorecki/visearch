package pl.edu.uwm.wmii.visearch.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.util.Properties;


public class ConfigFile {

	
	private static Properties props = new Properties();
	
	private static final String DEFAULT_DIR = new File(System.getProperty("user.home"),".visearch").getAbsolutePath();

	
	private void init(String directory, String filename) throws IOException {
		props = new Properties();
		File f = new File(directory, filename);
		
		try {
			props.load(new FileInputStream(f.getPath()));
			System.out.println("Config loaded from "+f.getAbsolutePath());
		}
		catch (IOException e) {
			f = new File(DEFAULT_DIR, filename);
			props.load(new FileInputStream(f.getPath()));
			System.out.println("Config loaded from "+f.getAbsolutePath());
		}
	}
	
	
	public ConfigFile(String filename) throws IOException {
		init(null, filename);
	}
	
	
	public ConfigFile(String directory, String filename) throws IOException {
		init(directory, filename);
	}


	public String get(String key) throws InvalidKeyException {
		String result = props.getProperty(key);
		if (result==null) {
			throw new InvalidKeyException("Key not found: "+key);
		}
		return result.trim();
	}

	
	public String get(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

}

package pl.edu.uwm.wmii.visearch.core;

public class VM {

	// Checks for presence of -DuseMapReduce=0 VM argument
	public static boolean RunSequential() {
		String prop = System.getProperty("useMapReduce");
		
		// if useMapReduce is set, and its value is zero()
		// -DuseMapReduce=0 VM argument
		if (prop!=null && Integer.parseInt(prop)==0) {
			return true;
		}
		return false;
	}

}

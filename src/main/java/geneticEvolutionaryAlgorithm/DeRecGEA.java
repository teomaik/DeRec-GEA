package geneticEvolutionaryAlgorithm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import calculator.MetricsCalculator;
import cppDepFinder.TestDep;
import metrics.ClassMetrics;

public class DeRecGEA {
	
	private static Hashtable<String, ArrayList<String>> artifactsWithDependencies;
	
	public static boolean start(String progrLang, String prjName, String pathToProjectFolder, String pathToDbCredFile) throws Exception {

		Exception exc = checkArguments(progrLang, prjName, pathToProjectFolder, pathToDbCredFile); // TODO Test this
		if (exc != null) {
			throw exc; // TODO Test this
		}

		switch (progrLang) {
		case "java":
			MetricsCalculator.start(pathToProjectFolder);
			artifactsWithDependencies = getClassesWithDependencies(MetricsCalculator.getClassMetricsContainer().getClassToMetricsIter());
			// doStuff
			break;
		case "c":
			artifactsWithDependencies = TestDep.getArtWithDeps(pathToProjectFolder);
			break;
		case "cpp":

			break;
		default:
			throw new Exception("Invalid programming language was provided. Analysis for '"+progrLang+"' is not supported!");
		}
		
		GEA gea = new GEA(artifactsWithDependencies);
		boolean result = gea.startGEA();
		
		return true;
	}

	//for MetricsCalculator (java projects)
	private static Hashtable<String, ArrayList<String>> getClassesWithDependencies(Iterator<Entry<String, ClassMetrics>> itClasses){
		Hashtable<String, ArrayList<String>> ret = new Hashtable<String, ArrayList<String>>();
		
		while (itClasses.hasNext()) {
			Entry<String, ClassMetrics> e = itClasses.next();
			ArrayList<String> deps = new ArrayList<String>();
			Iterator<String> itDeps = (Iterator<String>)e.getValue().getDependenciesIterator();
			System.out.println("Class: "+e.getKey());
			while (itDeps.hasNext()) {
				String dep = itDeps.next();
				System.out.println("\tdep: "+dep);
				deps.add(dep);
				itDeps.remove();
			}
			
			ret.put(e.getKey(), deps);
			itClasses.remove();
		}
		
		return ret;
	}
	
	private static Exception checkArguments(String langType, String prjName, String pathToProjectFolder,
			String pathToDbCredFile) {

		if (prjName == null || prjName.isEmpty() || prjName.trim().length() == 0 || langType == null
				|| langType.isEmpty() || langType.trim().length() == 0 || pathToProjectFolder == null
				|| pathToProjectFolder.isEmpty() || pathToProjectFolder.trim().length() == 0 || pathToDbCredFile == null
				|| pathToDbCredFile.isEmpty() || pathToDbCredFile.trim().length() == 0) {
			return new Exception("Wrong arguments, agruments are empty or null");
		}
		try {
			File file = new File(pathToProjectFolder);
			if (!file.isDirectory()) {
				return new Exception("Project folder not found, path is invalid");
			}
			file = new File(pathToDbCredFile);
			if (!file.exists()) {
				return new Exception("File not found, path is invalid");
			}
			if (file.isDirectory()) {
				return new Exception("Path is invalid, points to folder, not file");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new Exception("One of the specified files created a problem");
		}
		return null;
	}

}

package geneticEvolutionaryAlgorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import calculator.MetricsCalculator;
import cppDepFinder.TestDep;
import geneticEvolutionaryAlgorithm.database.DbController;
import geneticEvolutionaryAlgorithm.utils.GEA_Result;
import metrics.ClassMetrics;

public class DeRec {
	
	private static Hashtable<String, ArrayList<String>> artifactsWithDependencies;
	
	public static boolean start(String progrLang, String prjName, String pathToProjectFolder, String pathToDbCredFile) throws Exception {

		Exception exc = checkArguments(progrLang, prjName, pathToProjectFolder, pathToDbCredFile); // TODO Test this
		if (exc != null) {
			throw exc; // TODO Test this
		}

		DbController sqlCtrl = new DbController(pathToDbCredFile);
		if(!sqlCtrl.isReady()) {
			return false;
		}
		sqlCtrl.closeConn();
		
		switch (progrLang) {
		case "java":
			MetricsCalculator.start(pathToProjectFolder);
			artifactsWithDependencies = getClassesWithDependencies(MetricsCalculator.getClassMetricsContainer().getClassToMetricsIter());
			// doStuff
			break;
		case "c":
			//!!! BEWARE he who enters the cppDepFinder codebase. Legacy trash-tier code
			artifactsWithDependencies = TestDep.getArtWithDeps(pathToProjectFolder);
			break;
		case "cpp":
			//!!! BEWARE he who enters the cppDepFinder codebase. Legacy trash-tier code
			artifactsWithDependencies = TestDep.getArtWithDeps(pathToProjectFolder);
			break;
		default:
			throw new Exception("Invalid programming language was provided. Analysis for '"+progrLang+"' is not supported!");
		}
		
		GEA gea = new GEA(artifactsWithDependencies);
		boolean result = gea.startGEA();
		
		
		if(result == false) {
			System.out.println("Something went wrong during the execution");
			return false;
		}

		sqlCtrl = new DbController(pathToDbCredFile);
		if(!sqlCtrl.isReady()) {
			return false;
		}
		
		GEA_Result oldResult = gea.getOldResult();
		GEA_Result newResult = gea.getNewResult();
		System.out.println("\n\n------------------------------------------------------");
		System.out.println("\n"+oldResult.toStringMetrics());
		System.out.println("\n"+newResult.toStringMetrics());
		
		return sqlCtrl.insertIndividualsToDatabase(prjName, oldResult, newResult);

//		return true;
	}

	//for MetricsCalculator (java projects)
	private static Hashtable<String, ArrayList<String>> getClassesWithDependencies(Iterator<Entry<String, ClassMetrics>> itClasses){
		Hashtable<String, ArrayList<String>> ret = new Hashtable<String, ArrayList<String>>();
		
		while (itClasses.hasNext()) {
			Entry<String, ClassMetrics> e = itClasses.next();
			ArrayList<String> deps = new ArrayList<String>();
			Iterator<String> itDeps = (Iterator<String>)e.getValue().getDependenciesIterator();
//			System.out.println("Class: "+e.getKey());
			while (itDeps.hasNext()) {
				String dep = itDeps.next();
//				System.out.println("\tdep: "+dep);
				deps.add(dep);
				//itDeps.remove();
			}
			
			ret.put(e.getKey(), deps);
			//itClasses.remove();
		}
		
		return ret;
	}
	
	private static Exception checkArguments(String langType, String prjName, String pathToProjectFolder,
			String pathToDbCredFile) {

		if (prjName == null || prjName.isEmpty() || prjName.trim().length() == 0 || langType == null
				|| langType.isEmpty() || langType.trim().length() == 0 || pathToProjectFolder == null
				|| pathToProjectFolder.isEmpty() || pathToProjectFolder.trim().length() == 0 || pathToDbCredFile == null
				|| pathToDbCredFile.isEmpty() || pathToDbCredFile.trim().length() == 0) {
			return new Exception("Wrong arguments, agruments are empty or null. You should provide:"
					+ "\n1: Programming language type (java, c, cpp)"
					+ "\n2: A project name"
					+ "\n3: The absolute path to the project's folder"
					+ "\n3: The absolute path to the database's credentials file");
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

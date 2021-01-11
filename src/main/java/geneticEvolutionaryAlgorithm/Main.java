package geneticEvolutionaryAlgorithm;

import java.io.IOException;
import java.util.ArrayList;

import calculator.MetricsCalculator;
import containers.ClassMetricsContainer;
import geneticEvolutionaryAlgorithm.entities.Artifact;
import geneticEvolutionaryAlgorithm.entities.DeRec_Individual;

public class Main {

	public static void main(String[] args) throws IOException {

		
////	// <test java
//	String progrLang = "java";
//	String prjName = "test";
//	String pathToProjectFolder = "C:\\Users\\temp\\Downloads\\jcommander-main";
//	String pathToDbCredFile = "C:\\Users\\temp\\Documents\\GitHub\\Workspace\\UoM\\dbCredentials_uom.txt";
////	// >test end
		
				// <test Cpp
		String progrLang = "c";
		String prjName = "test";
		String pathToProjectFolder = "C:\\Users\\temp\\Downloads\\rodinia-master";
		String pathToDbCredFile = "C:\\Users\\temp\\Documents\\GitHub\\Workspace\\UoM\\dbCredentials_uom.txt";
		// >test end
		
		
////		// <test java
//		String progrLang = "java";
//		String prjName = "test";
//		String pathToProjectFolder = "C:\\Users\\temp\\Downloads\\jcommander-main";
//		String pathToDbCredFile = "C:\\Users\\temp\\Documents\\GitHub\\Workspace\\UoM\\dbCredentials_uom.txt";
////		// >test end

//		// <test Cpp
//		String progrLang = "c";
//		String prjName = "test";
//		String pathToProjectFolder = "C:\\Users\\temp\\Downloads\\kameleon2";
//		String pathToDbCredFile = "C:\\Users\\temp\\Documents\\GitHub\\Workspace\\UoM\\dbCredentials_uom.txt";
//		// >test end

		try {
			DeRecGEA.start(progrLang, prjName, pathToProjectFolder, pathToDbCredFile);
		} catch (Exception e) {
			System.out.println("Error in main, durring test execution");
			e.printStackTrace();
			return;
		}

		System.out.println("текос!");

		// ---------------------------------------------------------------------------------------------------------------
//		ArrayList<Artifact> classes = new ArrayList<Artifact>();
//		  for(int i=0; i<30; i++) {
//			  classes.add(new Artifact("Class_"+i+".java"));
//		  }
//		  DeRec_Individual indv = new DeRec_Individual(classes);
//		  indv.recreateMeAsRandomIndividual();
//		  
//		  System.out.println("\nper Component:\n");
//		  System.out.println(indv.toStringComps());
//		  System.out.println("\nper Class:\n");
//		  System.out.println(indv.toStringClasses());
//		  
//	    }

		// ---------------------------------------------------------------------------------------------------------------
//
//	        MetricsCalculator.start("C:\\Users\\temp\\Documents\\GitHub\\Workspace\\UoM\\SDK4ED\\_Tools\\Probability_To_Change_GUI - Package");
//	        
//
//	        ClassMetricsContainer cont = MetricsCalculator.getClassMetricsContainer();
//	        cont.test();
//	        
//

	}
}

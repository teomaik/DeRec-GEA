package test;

import java.io.IOException;

import geneticEvolutionaryAlgorithm.DeRec;

public class TestMain {

	public static void main(String[] args) throws IOException {

		String pathToDbCredFile = "C:\\Users\\temp\\Documents\\GitHub\\Workspace\\UoM\\dbCredentials_uom.txt";
		
		
		// <test java
		String progrLang = "java";
		String prjName = "jCommander";
		String pathToProjectFolder = "C:\\Users\\temp\\Downloads\\jcommander-main";
		// >test end

//			// <test Cpp
//			String progrLang = "c";
//			String prjName = "Eodinia";
//			String pathToProjectFolder = "C:\\Users\\temp\\Downloads\\rodinia-master";
//			// >test end

//			// <test Cpp
//			String progrLang = "c";
//			String prjName = "Airbus";
//			String pathToProjectFolder = "C:\\Users\\temp\\Downloads\\kameleon2";
//			// >test end

			try {
				boolean result = DeRec.start(progrLang, prjName, pathToProjectFolder, pathToDbCredFile);
				if(!result) {
					throw new Exception("The execution failed");
				}
				System.out.println("\n\n\nсысто текос!");
				System.exit(0);
			} catch (Exception e) {
				System.out.println("Error in main, durring test execution");
				System.out.println(e.getMessage());
				System.out.println("\n\n\nкахос текос!");
				System.exit(1);
			}

		}
}

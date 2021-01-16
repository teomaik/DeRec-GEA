package geneticEvolutionaryAlgorithm;

import java.io.IOException;
import java.util.ArrayList;

import calculator.MetricsCalculator;
import containers.ClassMetricsContainer;
import geneticEvolutionaryAlgorithm.entities.Artifact;
import geneticEvolutionaryAlgorithm.entities.DeRec_Individual;

public class Main {

	public static void main(String[] args) throws IOException {

		if(args.length!=4) {
			System.out.println("Wrong arguments. You must provide the following:"
					+ "\n1: Programming language type (java, c, cpp)"
					+ "\n2: A project name"
					+ "\n3: The absolute path to the project's folder"
					+ "\n3: The absolute path to the database's credentials file");
			System.exit(1);
		}

		try {
			boolean result = DeRec.start(args[0], args[1], args[2], args[3]);
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

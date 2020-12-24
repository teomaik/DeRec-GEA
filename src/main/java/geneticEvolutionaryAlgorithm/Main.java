package geneticEvolutionaryAlgorithm;

import java.io.IOException;

import calculator.MetricsCalculator;
import containers.ClassMetricsContainer;

public class Main {

	  public static void main(String[] args) throws IOException {
	        String line;
//	        MetricsCalculator.start("C:\\Users\\temp\\Downloads\\jcommander-main");

	        MetricsCalculator.start("C:\\Users\\temp\\Documents\\GitHub\\Workspace\\UoM\\SDK4ED\\_Tools\\Probability_To_Change_GUI - Package");
	        

	        ClassMetricsContainer cont = MetricsCalculator.getClassMetricsContainer();
	        cont.test();
	        
	        //BufferedReader br = new BufferedReader(MetricsCalculator.printResults());
//	        while ((line = br.readLine()) != null)
//	            System.out.println(line);
	    }

}

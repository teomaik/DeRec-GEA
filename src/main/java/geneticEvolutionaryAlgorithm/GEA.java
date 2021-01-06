package geneticEvolutionaryAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;

import geneticEvolutionaryAlgorithm.entities.Component;
import geneticEvolutionaryAlgorithm.entities.DeRec_Individual;
import metrics.ClassMetrics;

public class GEA {

	final int terminationCriteria = 50;
	final int populationSize = 200;
	
	private HashMap<String, ArrayList<String>> classesWithDeps;
	
	private ArrayList<DeRec_Individual> population;
	private Component root;
	
	
	public GEA(HashMap<String, ArrayList<String>> cls) {
		this.classesWithDeps = cls;
		
		createRootComponent();
		
	}

	public boolean startGEA() {
		return recursive_Evolutionary_Algorithm(root);
	}
	
	private boolean recursive_Evolutionary_Algorithm(Component component) {	//TODO
		
		population_Initialization(component);
		DeRec_Individual fittestIndv = population.get(0);
		
		int terminationCounter = 50;
		int genCounter = 0;;
		
		while(terminationCounter>0) {
			
			//doStuff TODO mutate, crossover
			
			if(fittestIndv.getFinalFitness() < population.get(0).getFinalFitness()) {
				terminationCounter = this.terminationCriteria;
			}else {
				terminationCounter--;
			}
			
			genCounter++;
		}
		
		
		return false;
	}
	
	private void population_Initialization(Component component) {
		
	}
	
	
}

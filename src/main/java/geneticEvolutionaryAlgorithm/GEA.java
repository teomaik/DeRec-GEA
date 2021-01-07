package geneticEvolutionaryAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import geneticEvolutionaryAlgorithm.entities.Artifact;
import geneticEvolutionaryAlgorithm.entities.Component;
import geneticEvolutionaryAlgorithm.entities.DeRec_Individual;
import javassist.bytecode.Descriptor.Iterator;
import metrics.ClassMetrics;

public class GEA {

	final int terminationCriteria = 50;
	final int populationSize = 200;
	
	private Hashtable<String, ArrayList<String>> classesWithDeps;
	
	private ArrayList<DeRec_Individual> population;
	private Component root;
	
	
	public GEA(Hashtable<String, ArrayList<String>> cls) {
		this.classesWithDeps = cls;
	}

	public boolean startGEA() {
		this.root = createComponent("root", classesWithDeps);
		return recursive_Evolutionary_Algorithm(root);
	}
	
	private boolean recursive_Evolutionary_Algorithm(Component component) {	//TODO

		//<test
		boolean test = true;
		if(test) {
			return true;
		}
		//>test end
		
		
		population_Initialization(component);
		DeRec_Individual fittestIndv = population.get(0);
		
		int terminationCounter = 50;
		int genCounter = 0;
		
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
		System.out.println("************* implement 1"); //TODO
	}
	
	private Component createComponent(String name, Hashtable<String, ArrayList<String>> cls) {
		Component ret = new Component(name);
		
		java.util.Iterator<Entry<String, ArrayList<String>>> it = cls.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, ArrayList<String>> e = it.next();
//			System.out.println("Adding to root component class: "+(String)e.getKey()); //TODO Remove this, only here for testing
			ret.addClass(new Artifact((String)e.getKey()));
			it.remove();
		}
		return ret;
	}
}

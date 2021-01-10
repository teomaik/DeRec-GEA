package geneticEvolutionaryAlgorithm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import geneticEvolutionaryAlgorithm.entities.Artifact;
import geneticEvolutionaryAlgorithm.entities.Component;
import geneticEvolutionaryAlgorithm.entities.DeRec_Individual;

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
		System.out.println("GEA.classesWithDeps size: "+classesWithDeps.size());
		
//		Iterator<Entry<String, ArrayList<String>>> it = classesWithDeps.entrySet().iterator();
//		while (it.hasNext()) {
//			Map.Entry<String, ArrayList<String>> e = (Map.Entry<String, ArrayList<String>>) it.next();
//			System.out.println("Class: " + e.getKey());
//			for (String s : e.getValue()) {
//				System.out.println("\tDep to: " + s);
//			}
//
//			it.remove();
//		}

		this.root = createComponent("root", classesWithDeps);
		return recursive_Evolutionary_Algorithm(root);
	}

	private boolean recursive_Evolutionary_Algorithm(Component component) { // TODO

		try {
			population_Initialization(component);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		DeRec_Individual fittestIndv = population.get(0);

		// <test
		boolean test = true;
		if (test) {
			return true;
		}
		// >test end

		int terminationCounter = 50;
		int genCounter = 0;

		while (terminationCounter > 0) {

			// doStuff TODO mutate, crossover

			if (fittestIndv.getFinalFitness() < population.get(0).getFinalFitness()) {
				terminationCounter = this.terminationCriteria;
			} else {
				terminationCounter--;
			}

			genCounter++;
		}

		return false;
	}

	private void population_Initialization(Component component) throws Exception {
		System.out.println("************* implement GEA.population_Initialization()"); //TODO
		
		this.population = new ArrayList<DeRec_Individual>();
		for (int i = 0; i < this.populationSize; i++) {
			System.out.println("this.classesWithDeps size: "+this.classesWithDeps.size());
			this.population.add(new DeRec_Individual(component, this.classesWithDeps));
			System.out.println("Number of comps for new indv: "+this.population.get(i).getComponentNumber());
			this.population.get(i).calculateFitness();
		}
	}

	private Component createComponent(String name, Hashtable<String, ArrayList<String>> cls) {
		Component ret = new Component(name);

		System.out.println("Project number of artifacts: "+cls.size());
		
		Iterator<Entry<String, ArrayList<String>>> it = cls.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> e = it.next();
//			System.out.println("Adding to root component class: "+(String)e.getKey()); //TODO Remove this, only here for testing
			ret.addArtifact(new Artifact((String) e.getKey()));
			//it.remove();
		}
		System.out.println("Root number of artifacts: "+ret.getNumberOfClasses());
		return ret;
	}
}

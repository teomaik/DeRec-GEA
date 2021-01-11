package geneticEvolutionaryAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import geneticEvolutionaryAlgorithm.entities.Artifact;
import geneticEvolutionaryAlgorithm.entities.Component;
import geneticEvolutionaryAlgorithm.entities.DeRec_Individual;

public class GEA {

	final int terminationCriteria = 300;
	final int populationSize = 100;

	private Hashtable<String, ArrayList<String>> classesWithDeps;

	private ArrayList<DeRec_Individual> population;
	private Component root;

	public GEA(Hashtable<String, ArrayList<String>> cls) {
		this.classesWithDeps = cls;
	}

	public boolean startGEA() {
		System.out.println("GEA.classesWithDeps size: " + classesWithDeps.size());

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
		
//		// <test
//		boolean test = true;
//		if (test) {
//			return true;
//		}
//		// >test end
		
		try {
			population_Initialization(component);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		DeRec_Individual fittestIndv = population.get(0);



		int terminationCounter = 0;
		int genCounter = 0;

		while (terminationCounter < terminationCriteria) {
			System.out.println("( " + (terminationCounter+1) + "/" + terminationCriteria + " ) Generation " + genCounter
					+ ", Fitness: " + fittestIndv.getFinalFitness() +", #Components: "+fittestIndv.getComponentNumber());
			// doStuff TODO mutate, crossover

			mutatePopulation();
			calcFitness();
			
			crossoverPopulation();
			calcFitness();
			removePopulationOverhead();

			if (fittestIndv.getFinalFitness() < population.get(0).getFinalFitness()) {
				terminationCounter = 0;
				fittestIndv = population.get(0);
			} else {
				terminationCounter++;
			}

			genCounter++;
		}

		// TODO
		return false;
	}

	private void mutatePopulation() {
		IntStream.range(1, this.populationSize/3).parallel().forEach(i -> {
			this.population.get(i).mutate();
		});
	}

//	private void crossoverPopulation() {
//		for(int i=0; i<this.populationSize/3; i+=2) {
//			this.population.add((DeRec_Individual)this.population.get(i).crossover(this.population.get(i+1)));
//		}
//	}
	private void crossoverPopulation() {
		for(int i=0; i<this.populationSize/3; i++) {
			int mateId = ThreadLocalRandom.current().nextInt(i, this.population.size());
			this.population.add((DeRec_Individual)this.population.get(i).crossover(this.population.get(mateId)));
		}
	}
	private void removePopulationOverhead() {
		while(this.population.size()>this.populationSize) {
			this.population.remove(populationSize);
		}
	}
	
	private void calcFitness() {
		
		IntStream.range(0, populationSize).parallel().forEach(i -> {
			try {
				this.population.get(i).calculateFitness();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		Collections.sort(this.population);
	}

	private void population_Initialization(Component component) throws Exception {
		System.out.println("************* implement GEA.population_Initialization()"); // TODO

		this.population = new ArrayList<DeRec_Individual>();
		for (int i = 0; i < this.populationSize; i++) {
//			System.out.println("this.classesWithDeps size: "+this.classesWithDeps.size());
			this.population.add(new DeRec_Individual(component, this.classesWithDeps));
//			System.out.println("Number of comps for new indv: "+this.population.get(i).getComponentNumber());
			this.population.get(i).calculateFitness();
		}
		Collections.sort(this.population);
	}

	private Component createComponent(String name, Hashtable<String, ArrayList<String>> cls) {
		Component ret = new Component(name);

		System.out.println("Project number of artifacts: " + cls.size());

		Iterator<Entry<String, ArrayList<String>>> it = cls.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> e = it.next();
//			System.out.println("Adding to root component class: "+(String)e.getKey()); //TODO Remove this, only here for testing
			ret.addArtifact(new Artifact((String) e.getKey()));
			// it.remove();
		}
		System.out.println("Root number of artifacts: " + ret.getNumberOfClasses());
		return ret;
	}
}

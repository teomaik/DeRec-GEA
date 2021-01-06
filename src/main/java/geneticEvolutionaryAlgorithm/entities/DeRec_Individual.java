package geneticEvolutionaryAlgorithm.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import metrics.ClassMetrics;

public class DeRec_Individual extends Metricable implements Individual {
	// final static HashMap<String, ClassMetrics> classesAndDeps;

	private Hashtable<String, Artifact> classes;
	private ArrayList<Component> components;
	private String parentCompName;
	private double fitness = 0;

	// TODO create dependencies by getting Dimi's HashTable
	// TODO a good clone method

	public DeRec_Individual(Hashtable<String, Artifact> oldClasses, HashMap<String, ArrayList<String>> classesAndDeps) { // ***TODO DEBUG
		super();
		this.createNewArtifacts(oldClasses);
		this.findClassDependencies(classesAndDeps);

		//this.removeUnwantedDependencies();
		this.recreateMeAsRandomIndividual();
		
		try {
			this.calculate_Metrics();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DeRec_Individual(Component component, HashMap<String, ArrayList<String>> classesAndDeps) { // ***TODO DEBUG
		super();

		this.createNewArtifacts(component.getMyClasses());
		this.findClassDependencies(classesAndDeps);
		parentCompName = component.getName();
		//this.removeUnwantedDependencies();
		this.recreateMeAsRandomIndividual();
		
		try {
			this.calculate_Metrics();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createNewArtifacts(Hashtable<String, Artifact> oldClasses) { // TODO na pairnei Hashtable
		this.classes = new Hashtable<String, Artifact>();

		Iterator it = oldClasses.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String className = (String) pair.getKey();
			// creares new Artifact
			this.classes.put(className, new Artifact(className));
			it.remove(); // avoids a ConcurrentModificationException
		}
	}

	private void findClassDependencies(HashMap<String, ArrayList<String>> classesAndDeps) {

		Iterator it = this.classes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();

			Artifact artifact = (Artifact) pair.getValue();

			ArrayList<String> cm = classesAndDeps.get(artifact.getName());
			Iterator<String> it2 = cm.iterator();
			// clses
			while (it2.hasNext()) {
				String dep = it2.next();
				if (!this.classes.contains(dep)) {
					it2.remove(); // avoids a ConcurrentModificationException
					continue;
				}

				// adds an Artifact as a dependency to a class
				this.classes.get(artifact.getName()).addDependency(this.classes.get(dep));

				it2.remove(); // avoids a ConcurrentModificationException
			}

			it.remove(); // avoids a ConcurrentModificationException
		}
	}

	public void crossover(Individual temp_mate) {
		DeRec_Individual mate = (DeRec_Individual)temp_mate;
		
		System.out.println("****************CROSSOVER MUST BE IMPLEMENTED"); //TODO
	}

	public void mutate() {
		this.mutate(ThreadLocalRandom.current().nextInt(3, 5 + (this.components.size() / 10)));
	}
	
	public void mutate(int times) {
		ArrayList<Artifact> classesList = new ArrayList<Artifact>();
		Iterator<Entry<String, Artifact>> it = this.classes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			classesList.add((Artifact)pair.getValue());
		}
		Collections.sort(classesList);

		if (times < 3) {
			times = 3;
		}

		System.out.println("****************MUTATION MUST BE IMPLEMENTED"); //TODO
		
//		int auxiliaryMutation = times;
//		while (auxiliaryMutation > 0) {
//			int c = ThreadLocalRandom.current().nextInt(0, 5);
//			switch (c) {
//			case 0:
//				splitComponentRandom();
//				break;
//			case 1:
//				mergeComponentsSemiRand();
//				break;
//			}
//			auxiliaryMutation--;
//		}
//		moveClassesAround(new ArrayList<Integer>(), times);
	}

//	private void splitComponentRandom() {
//		int idx = ThreadLocalRandom.current().nextInt(0, this.gene.length);
//		String comp = "";
//		comp = this.components[idx];
//		if (comp.isEmpty()) {
//			return;
//		}
//		String newComp = "" + (this.actualUsedComponents + 1);
//		for (int c = 0; c < this.components.length; c++) {
//			if (!this.components[c].equals(comp)) {
//				continue;
//			}
//			if (ThreadLocalRandom.current().nextInt(0, 2) == 1) {
//				this.components[c] = newComp;
//			}
//		}
//		this.tidyUp();
//	}
//
//	private void mergeComponentsSemiRand() {
//		if (this.actualUsedComponents <= 2) {
//			return;
//		}
//		List<String> compToMerge = new ArrayList<String>();
//		int i = 0;
//		for (String c : this.getComponentsString()) {
//			if (this.getFinalFitnessByComp().get(i) < this.finalFitness) {
//				compToMerge.add(c);
//			}
//			i++;
//		}
//		if (compToMerge.size() < 2) {
//			return;
//		}
//		String comp1, comp2;
//		if (compToMerge.size() == 2) {
//			comp1 = compToMerge.get(0);
//			comp2 = compToMerge.get(1);
//		} else {
//			int rand1 = ThreadLocalRandom.current().nextInt(0, compToMerge.size());
//			int rand2 = ThreadLocalRandom.current().nextInt(0, compToMerge.size());
//			while (rand1 == rand2) {
//				rand2 = ThreadLocalRandom.current().nextInt(0, compToMerge.size());
//			}
//			comp1 = compToMerge.get(rand1);
//			comp2 = compToMerge.get(rand2);
//		}
//		for (int c = 0; c < this.components.length; c++) {
//			if (!this.components[c].equals(comp1))
//				continue;
//			this.components[c] = comp2;
//		}
//		this.tidyUp();
//	}
//
//	private void moveClassesAround(List<Integer> exclude, int times) {
//		this.checkComponentValidity();
//		double finFitLowest = this.finalFitnessArray[0];
//		int idxLowest = 0;
//		for (int i = 1; i < this.gene.length; i++) {
//			if (finFitLowest < this.finalFitnessArray[i]) {
//				boolean flag = true;
//				for (Integer idx : exclude) {
//					if (i == idx) {
//						flag = false;
//						break;
//					}
//				}
//				if (!flag) {
//					continue;
//				}
//				finFitLowest = this.finalFitnessArray[i];
//				idxLowest = i;
//				exclude.add(i);
//			}
//			i++;
//		}
//		String oldComp = this.components[idxLowest];
//		int numOfComps = this.actualUsedComponents;
//		String newComp = "" + ThreadLocalRandom.current().nextInt(1, numOfComps + 1);
//		if (numOfComps > 1) {
//			while (newComp.equals(oldComp)) {
//				newComp = "" + ThreadLocalRandom.current().nextInt(1, numOfComps + 1);
//			}
//		}
//		this.components[idxLowest] = newComp;
//		times--;
//		this.tidyUp();
//		if (times > 0) {
//			moveClassesAround(exclude, times);
//		}
//	}

	// Individual's fitness
	public void calculateFitness() throws Exception {
		this.calculate_Metrics();
//		this.fitness = this.getCohesion() - this.getCoupling();
	}

	// Cohesion and Coupling on Individual Level
	public void calculate_Metrics() throws Exception {

		if (components.size() <= 0) {
			throw new Exception("DeRec_Individual has 0 Components, but tried to calculate its metrics");
		}

		IntStream.range(0, components.size()).parallel().forEach(i -> components.get(i).calculate_Metrics());

		double tempCohesion = 0;
		double tempCoupling = 0;
		for (int i = 0; i < components.size(); i++) {
			tempCohesion += components.get(i).getCohesion();
			tempCoupling += components.get(i).getCoupling();
		}

		this.setCohesion(tempCohesion / this.components.size());
		this.setCoupling(tempCoupling / this.components.size());
	}

	// expects an ArrayList of interconected Artifacts, with the Dependencies
	// already set
	public void recreateMeAsRandomIndividual() {

		int num_of_comps = this.classes.size() / 20 + 3;
		int numOfComps = ThreadLocalRandom.current().nextInt(2, num_of_comps + 1);

		this.components = new ArrayList<Component>();
		for (int i = 1; i < numOfComps + 1; i++) {
			this.components.add(new Component(String.valueOf(i)));
		}

		for (int c = 0; c < this.classes.size(); c++) {
			int componentIndex = ThreadLocalRandom.current().nextInt(0, this.components.size());
			this.components.get(componentIndex).addClass(this.classes.get(c));
		}
	}

	// remove the depenencies of classes no longer in this instance of the
	// experiment (the dependencies should be added before the end of the GEA) TODO
	public void removeUnwantedDependencies() {

		this.classes.entrySet().parallelStream().forEach(e -> {

			Hashtable<String, Artifact> newDeps = new Hashtable<String, Artifact>();

			Iterator<Entry<String, Artifact>> it = ((Artifact) e.getValue()).getDependencies();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				Artifact art = (Artifact) pair.getValue(); // ***DEBUG ***TODO ***TEST an leitourgei

				if(!this.classes.contains(art.getName())) {
					((Artifact) e.getValue()).removeDependency(art);
				}
				
				it.remove(); // avoids a ConcurrentModificationException
			}

		});

//			IntStream.range(0, classDeps.size()).parallel().forEach(i -> {});

	}

	public String toStringComps() {
		String ret = "";
		for (Component comp : this.components) {
			ret += "\n" + comp.toString();
		}
		return ret;
	}

//	public String toStringClasses() {
//		String ret = "";
//		int i = 0;
//		for (Artifact art : this.classes) {
//			ret += i + ": " + art.getName() + ", comp: " + art.getComponent().getName() + "\n";
//			i++;
//		}
//
//		return ret;
//	}

	public ArrayList<Component> getComponents() {
		return components;
	}

	@Override
	public double getFitness() {
		return this.getFinalFitness();
	}
}

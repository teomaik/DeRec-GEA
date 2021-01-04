package geneticEvolutionaryAlgorithm.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class DeRec_Individual extends Metricable implements Individual{
	private ArrayList<Artifact> classes;
	private ArrayList<Component> components;
	private String parentCompName;
	private double fitness=0;
	
	//TODO create dependencies by getting Dimis HashTable
	//TODO a clone method
	
	public DeRec_Individual(ArrayList<Artifact> classes) {	//***TODO DEBUG
		super();
		this.classes = classes;
		parentCompName = "root";
		this.removeUnwantedDependencies();
		this.recreateMeAsRandomIndividual();
	}
	
	public DeRec_Individual(Component component) {	//***TODO DEBUG
		super();
		this.classes = component.getMyClasses();
		parentCompName = component.getName();
		this.removeUnwantedDependencies();
		this.recreateMeAsRandomIndividual();
	}

	public void crossover() {
		// TODO Auto-generated method stub
	}
	
	public void mutate(int times) {
		Collections.sort(classes);
		if (times <= 0) {
			times = ThreadLocalRandom.current().nextInt(3, 5 + (this.components.size() / 10));
		}
		if (times < 3) {
			times = 3;
		}
		
		int auxiliaryMutation = times;
		while (auxiliaryMutation > 0) {
			int c = ThreadLocalRandom.current().nextInt(0, 5);
			switch (c) {
			case 0:
				splitComponentRandom();
				break;
			case 1:
				mergeComponentsSemiRand();
				break;
			}
			auxiliaryMutation--;
		}
		moveClassesAround(new ArrayList<Integer>(), times);
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
	
	//Individual's fitness
	public void calculateFitness() throws Exception {
		// TODO Auto-generated method stub
		this.calculate_Metrics();

		this.fitness = this.getCohesion() - this.getCoupling();
	}

	//Cohesion and Coupling on Individual Level
	public void calculate_Metrics() throws Exception {

		if(components.size()<=0) {
			throw new Exception("DeRec_Individual has 0 Components, but tried to calculate its metrics");
		}
		
		IntStream.range(0, components.size()).parallel().forEach(i ->  components.get(i).calculate_Metrics());
		
		double tempCohesion = 0;
		double tempCoupling = 0;
		for(int i=0; i<components.size(); i++) {
			tempCohesion += components.get(i).getCohesion();
			tempCoupling += components.get(i).getCoupling();
		}
		
		this.setCohesion(tempCohesion/this.components.size());
		this.setCoupling(tempCoupling/this.components.size());
	}

	//expects an ArrayList of interconected Artifacts, with the Dependencies already set
	public void recreateMeAsRandomIndividual() {
		
		int num_of_comps = this.classes.size()/20 + 3;
		int numOfComps = ThreadLocalRandom.current().nextInt(2, num_of_comps + 1);

		this.components = new ArrayList<Component>();
		for(int i=1; i<numOfComps+1; i++) {
			this.components.add(new Component(String.valueOf(i)));
		}
		
		for(int c=0; c<this.classes.size(); c++) {
			int componentIndex = ThreadLocalRandom.current() .nextInt(0, this.components.size());
			this.components.get(componentIndex).addClass(this.classes.get(c));
		}
	}

	//remove the depenencies of classes no longer in this instance of the experiment (the dependencies should be added before the end) TODO
	public void removeUnwantedDependencies() {
		for(Artifact cls : this.classes) {
			ArrayList<Artifact> newDeps = new ArrayList<Artifact>();
			ArrayList<Artifact> classDeps = cls.getDependencies();

			IntStream.range(0, classDeps.size()).parallel().forEach(i ->  {
				boolean exists = false;
				Artifact thisDep = classDeps.get(i);
				for(Artifact art : this.classes) {
					if(thisDep.equals(art)) {
						exists = true;
						break;
					}
				}
				if(exists) {
					newDeps.add(thisDep);
				}
			});
			cls.setDependencies(newDeps);
		}
	}
	
	public String toStringComps() {
		String ret="";
		for(Component comp : this.components) {
			ret += "\n"+ comp.toString();
		}
		return ret;
	}
	
	public String toStringClasses() {
		String ret="";
		int i=0;
		for(Artifact art : this.classes) {
			ret+=i+": "+art.getName()+", comp: "+art.getComponent().getName()+"\n";
			i++;
		}
		
		return ret;
	}

	public ArrayList<Component> getComponents() {
		return components;
	}
}

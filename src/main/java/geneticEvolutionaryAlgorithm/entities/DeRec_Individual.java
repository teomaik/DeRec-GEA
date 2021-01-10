package geneticEvolutionaryAlgorithm.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class DeRec_Individual extends Metricable implements Individual, Comparable<DeRec_Individual> {
	// final static HashMap<String, ClassMetrics> classesAndDeps;

	private Hashtable<String, Artifact> artifacts;
	private ArrayList<Component> components;
	private String parentCompName;
	private double fitness = 0;

	private Hashtable<String, ArrayList<String>> baseClasses;
	// TODO create dependencies by getting Dimi's HashTable
	// TODO a good clone method

	public DeRec_Individual(Component component, Hashtable<String, ArrayList<String>> classesAndDeps) { // ***TODO DEBUG
		super();

		this.baseClasses = classesAndDeps;
		this.createNewArtifacts(component.getMyClasses());
		
//		System.out.println("classesAndDeps size: "+classesAndDeps.size());
//		Iterator<Entry<String, ArrayList<String>>> it = classesAndDeps.entrySet().iterator();
//		while(it.hasNext()) {
//			Map.Entry<String, ArrayList<String>> e = (Map.Entry<String, ArrayList<String>>)it.next();
//			System.out.println("Class: "+e.getKey());
//			for(String s : e.getValue()) {
//				System.out.println("\tdep: "+s);
//			}
//		}
//		if(!it.hasNext()) {
//			int i=0;
//			i = 3/i;
//		}
		
		this.findClassDependencies(classesAndDeps);
		parentCompName = component.getName();
		// this.removeUnwantedDependencies();
		this.recreateMeAsRandomIndividual();

		try {
			this.calculate_Metrics();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DeRec_Individual(Hashtable<String, Artifact> oldClasses, Hashtable<String, ArrayList<String>> classesAndDeps,
			HashMap<String, ArrayList<String>> comps) { // ***TODO
		super();

		this.baseClasses = classesAndDeps;
		this.createNewArtifacts(oldClasses);
		this.findClassDependencies(classesAndDeps);
		// this.removeUnwantedDependencies();
		try {
			this.createGivenStructure(comps);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			this.calculate_Metrics();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createGivenStructure(HashMap<String, ArrayList<String>> comps) throws Exception {
		Iterator<Entry<String, ArrayList<String>>> it = comps.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> e = (Map.Entry<String, ArrayList<String>>) it.next();
			Component newComp = new Component(e.getKey());
			this.components.add(newComp);
			ArrayList<String> artsComp = e.getValue();
			for (String art : artsComp) {
				if (this.artifacts.get(art) == null) {
					throw new Exception("No Artifact found during Crossover!");
				}
				newComp.addArtifact(this.artifacts.get(art));
			}

			//it.remove();
		}
	}

	private void createNewArtifacts(Hashtable<String, Artifact> oldClasses) { // TODO na pairnei Hashtable
		this.artifacts = new Hashtable<String, Artifact>();

		System.out.println("OLDCLASSES size: "+oldClasses.size());
		
		Iterator<Entry<String, Artifact>> it = oldClasses.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Artifact> pair = (Map.Entry<String, Artifact>) it.next();
			String className = (String) pair.getKey();
			// creares new Artifact
			this.artifacts.put(className, new Artifact(className));
			//it.remove(); // avoids a ConcurrentModificationException
		}
	}

	private void findClassDependencies(Hashtable<String, ArrayList<String>> classesAndDeps) {

		System.out.println("classesAndDeps size: "+classesAndDeps.size());
		
		Iterator<Entry<String, Artifact>> it = this.artifacts.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Artifact> pair = (Map.Entry<String, Artifact>) it.next();

			Artifact artifact = pair.getValue();

			if(!classesAndDeps.contains(artifact.getName())) {
				System.out.println("classesAndDeps does not contain: "+artifact.getName());
			}
//			if(!classesAndDeps.contains(pair.getKey())) {
//				System.out.println("classesAndDeps does not contain: "+pair.getKey());
//			}
			
			ArrayList<String> deps = classesAndDeps.get(artifact.getName());
//			if(deps==null) {
//				System.out.println("CM is null");
//			}
//			if(deps.isEmpty()) {
//				System.out.println("CM is empty");
//			}
			Iterator<String> it2 = deps.iterator();
			// clses
			while (it2.hasNext()) {
				String dep = it2.next();
				if (!this.artifacts.contains(dep)) {
					//it2.remove(); // avoids a ConcurrentModificationException
					continue;
				}

				// adds an Artifact as a dependency to a class
				this.artifacts.get(artifact.getName()).addDependency(this.artifacts.get(dep));

				//it2.remove(); // avoids a ConcurrentModificationException
			}

			//it.remove(); // avoids a ConcurrentModificationException
		}
	}

	public Individual crossover(Individual temp_mate) {	//TODO TEST THIS THING!!!!!!!!!!!!!!
		
		System.out.println("***NEED TEST!!!!!! - DeRec_Individual.Crossover()");
		
		DeRec_Individual mate = (DeRec_Individual) temp_mate;
			
		// new component structure of the resulting individual
		HashMap<String, ArrayList<String>> childCompStructure = new HashMap<String, ArrayList<String>>(); 
		
		for(Component t_comp : this.components) {
			childCompStructure.put("this_"+t_comp.getName(), new ArrayList<String>());
		}
		for(Component m_comp : mate.getComponents()) {
			childCompStructure.put("mate_"+m_comp.getName(), new ArrayList<String>());
		}

		Iterator<Entry<String, Artifact>> it = this.artifacts.entrySet().iterator();
		if(it.hasNext()) {
			Map.Entry<String, Artifact> e = (Map.Entry<String, Artifact>)it.next();
			String compName;
			if(this.getArtifactFitness(e.getKey()) > mate.getArtifactFitness(e.getKey())) {
				compName = "this_"+this.getArtifactComponentName(e.getKey());
			}else {
				compName = "mate_"+mate.getArtifactComponentName(e.getKey());
			}
			
			ArrayList<String> arts = childCompStructure.get(compName);
			arts.add(e.getKey());
			childCompStructure.put(compName, arts);
			
			//it.remove();
		}

		return new DeRec_Individual(this.artifacts, this.baseClasses, childCompStructure);
	}

	public double getArtifactFitness(String artifactName) {
		return this.artifacts.get(artifactName).getFinalFitness();
	}
	
	public String getArtifactComponentName(String artifactName) {
		return this.artifacts.get(artifactName).getComponent().getName();
	}
	
	// return a HashTable with this Individuals Component Strucure. It contains the
	// Component's name as a key, and a List with it's classes as a Value
	public HashMap<String, ArrayList<String>> exportComponentStructure() {
		HashMap<String, ArrayList<String>> ret = new HashMap<String, ArrayList<String>>();

		for (Component c : this.components) {
			ArrayList<String> cls = new ArrayList<String>();
			Iterator<Entry<String, Artifact>> it = c.getMyClassesIterator();
			while (it.hasNext()) {
				Map.Entry<String, Artifact> e = it.next();
				cls.add(e.getKey());
				//it.remove();
			}
			if (cls.size() == 0) {
				continue;
			}

			ret.put(c.getName(), cls);
		}

		return ret;
	}

	public int getComponentNumber() {
		return this.getComponents().size();
	}

	public void mutate() {
		this.mutate(ThreadLocalRandom.current().nextInt(3, 5 + (this.components.size() / 10)));
	}

	public void mutate(int times) {
		if (this.components.size() <= 1 || this.artifacts.size() <= 2) {
			return;
		}

		ArrayList<Artifact> classesList = new ArrayList<Artifact>();
		Iterator<Entry<String, Artifact>> it = this.artifacts.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Artifact> pair = (Map.Entry<String, Artifact>) it.next();
			classesList.add((Artifact) pair.getValue());
			//it.remove();
		}
		Collections.sort(classesList);

		if (times < 3) {
			times = 3;
		}

		System.out.println("****************MUTATION MUST BE COMPLETED"); // TODO

		while (times > 0) {

			int mut_type = ThreadLocalRandom.current().nextInt(0, 11);

			if (mut_type == 10) { // Split or Merge Components
				// do later...... or not
			} else {
				moveClassRandom(classesList);
			}
			times--;
		}
	}

	private void moveClassRandom(ArrayList<Artifact> classesList) {

		if (this.components.size() < 2) {
			return;
		}

		int artIdx = ThreadLocalRandom.current().nextInt(classesList.size() / 2, classesList.size());
		Artifact art = classesList.get(artIdx);

		int newComp = ThreadLocalRandom.current().nextInt(0, this.components.size());
		while (art.getComponent().equals(this.components.get(newComp))) {
			newComp = ThreadLocalRandom.current().nextInt(0, this.components.size());
		}

		changeArtifactsComponent(art, this.components.get(newComp));
	}

	private void changeArtifactsComponent(Artifact art, Component newComponent) {
		art.changeComponent(newComponent);
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

		// remove any empty components
		int c = 0;
		int compSize = this.components.size();
		while (c < compSize) {
			if (this.components.get(c).getNumberOfClasses() == 0) {
				this.components.remove(c);
				c--;
				compSize--;
			} else {
				this.components.get(c).setName(String.valueOf(c));
			}
			c++;
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

		int num_of_comps = this.artifacts.size() / 20 + 3;
		int numOfComps = ThreadLocalRandom.current().nextInt(2, num_of_comps + 1);

		this.components = new ArrayList<Component>();
		for (int i = 1; i < numOfComps + 1; i++) {
			this.components.add(new Component(String.valueOf(i)));
		}

		System.out.println("My comp number before fit_calc: "+this.components.size());
		System.out.println("My Artifact number before fit_calc: "+this.artifacts.size());
		
		Iterator<Entry<String, Artifact>> it = this.artifacts.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Artifact> e = (Map.Entry<String, Artifact>) it.next();

			int componentIndex = ThreadLocalRandom.current().nextInt(0, this.components.size());
			this.components.get(componentIndex).addArtifact(e.getValue());
			//it.remove();
		}
		
	}

	// remove the depenencies of classes no longer in this instance of the
	// experiment (the dependencies should be added before the end of the GEA) TODO
	public void removeUnwantedDependencies() {

		this.artifacts.entrySet().parallelStream().forEach(e -> {

			Iterator<Entry<String, Artifact>> it = ((Artifact) e.getValue()).getDependencies();
			while (it.hasNext()) {
				Map.Entry<String, Artifact> pair = (Map.Entry<String, Artifact>) it.next();
				Artifact art = (Artifact) pair.getValue(); // ***DEBUG ***TODO ***TEST an leitourgei

				if (!this.artifacts.contains(art.getName())) {
					((Artifact) e.getValue()).removeDependency(art);
				}

				//it.remove(); // avoids a ConcurrentModificationException
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

	@Override
	public int compareTo(DeRec_Individual otherObj) {
		double compareNumber = otherObj.getFitness();
		if (this.getFitness() > compareNumber) {
			return -1;
		} else if (this.getFitness() == compareNumber) {
			return 0;
		}
		return 1;
	}
}

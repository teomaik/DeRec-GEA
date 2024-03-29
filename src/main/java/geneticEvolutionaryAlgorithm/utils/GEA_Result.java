package geneticEvolutionaryAlgorithm.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import geneticEvolutionaryAlgorithm.BaseClasses.Metricable;
import geneticEvolutionaryAlgorithm.entities.Artifact;
import geneticEvolutionaryAlgorithm.entities.Component;

public class GEA_Result extends Metricable {
	private Hashtable<String, Artifact> artifacts;
	private ArrayList<Component> components;
	private String name;

	public GEA_Result(String name, Component component, Hashtable<String, ArrayList<String>> classesAndDeps) { // ***TODO
																												// DEBUG
		super();
		this.components = component.getMyComponents();
		this.artifacts = component.getMyClasses();

		this.name = name;
		this.findClassDependencies(classesAndDeps);

		try {
			this.calculate_Metrics();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public String toStringMetrics() {
		return (this.name+"Fitness: "+this.getFinalFitness()+", Coupling: "+this.getCoupling()+", Cohesion: "+this.getCohesion()+"\nComponents: "+this.components.size());
	}
	
	public String toString() {
		String ret = this.name+"Fitness: "+this.getFinalFitness()+", Coupling: "+this.getCoupling()+", Cohesion: "+this.getCohesion()+"\nComponents: "+this.components.size();
		for(Component comp : components) {
			ret+=comp.toString(0, "");
		}
		return ret;
	}
	
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
	
	private void findClassDependencies(Hashtable<String, ArrayList<String>> classesAndDeps) {

		Iterator<Entry<String, Artifact>> it = this.artifacts.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Artifact> pair = (Map.Entry<String, Artifact>) it.next();
			Artifact artifact = pair.getValue();
			ArrayList<String> deps = classesAndDeps.get(artifact.getName());

			//artifacts dependencies
			Iterator<String> it2 = deps.iterator();
			// clses
			while (it2.hasNext()) {
				String dep = it2.next();
				if (!this.artifacts.containsKey(dep)) {
					continue;
				}
				// adds an Artifact as a dependency to a class
				artifact.addDependency(this.artifacts.get(dep));
			}
			artifact.calculate_Metrics();
		}
	}

	public ArrayList<Component> getComponents() {
		return components;
	}

	public Iterator<Entry<String, Artifact>> getArtifactsIterator() {
		return artifacts.entrySet().iterator();
	}
}

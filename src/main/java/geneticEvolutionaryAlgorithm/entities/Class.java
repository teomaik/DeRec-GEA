package geneticEvolutionaryAlgorithm.entities;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class Class implements Metricable{
	public final String name;
	private Component component;
	private ArrayList<Class> dependencies;
	
	private double coupling=0;
	private double cohesion=0;
	
	public Class(String name) {
		super();
		this.name = name;
	}
	
	
	public void calculate_Metrics() {
		for(int i=0; i<dependencies.size(); i++) {
			if(dependencies.get(i).getComponent().equals(this.component)) {
				this.cohesion++;
			}else {
				this.coupling++;
			}
		}
	}


	public void addDependency(Class newDependency) {
		boolean exists = false;
		for(int i=0; i<dependencies.size(); i++) {
			if(dependencies.get(i).equals(newDependency)) {
				return;
			}
		}
		this.dependencies.add(newDependency);
	}
	
	
	public Component getComponent() {
		return component;
	}



	public void setComponent(Component component) {
		this.component = component;
	}



	public ArrayList<Class> getDependencies() {
		return dependencies;
	}



	public void setDependencies(ArrayList<Class> dependencies) {
		this.dependencies = dependencies;
	}



	public double getCoupling() {
		return coupling;
	}


	public double getCohesion() {
		return cohesion;
	}


}

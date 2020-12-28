package geneticEvolutionaryAlgorithm.entities;

import java.util.ArrayList;
import java.util.stream.IntStream;

//a source file. Class in case of Java and file in case of C/Cpp
public class Artifact extends Metricable{
	private final String name;


	private Component component;
	private ArrayList<Artifact> dependencies = new ArrayList<Artifact>();
	
	
	public Artifact(String name) {
		super();
		this.name = name;
	}
	
	//Cohesion and Coupling for class level
	public void calculate_Metrics() {
		double tempCohesion = 0;
		double tempCoupling = 0;
		
		for(int i=0; i<dependencies.size(); i++) {
			if(dependencies.get(i).getComponent().equals(this.component)) {
				tempCohesion++;
			}else {
				tempCoupling++;
			}
		}
		
		this.setCohesion(tempCohesion);
		this.setCoupling(tempCoupling);
	}


	public void addDependency(Artifact newDependency) {
		boolean exists = false;
		for(int i=0; i<dependencies.size(); i++) {
			if(dependencies.get(i).equals(newDependency)) {
				return;
			}
		}
		this.dependencies.add(newDependency);
	}
	
	//return is this class has a dependency on a specific class
	public boolean isDependantOn(Artifact cls) {
		for (int i = 0; i < dependencies.size(); i++) {
			if(dependencies.get(i).equals(cls)) return true;
		}
		return false;
	}
	
	public Component getComponent() {
		return component;
	}



	public void setComponent(Component component) {
		this.component = component;
	}



	public ArrayList<Artifact> getDependencies() {
		return dependencies;
	}



	public void setDependencies(ArrayList<Artifact> dependencies) {
		this.dependencies = dependencies;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		String ret=this.name+"\n";
		int count=0;
		for(Artifact art:this.dependencies) {
			ret+= "\n\t"+count+": "+art.getName();
			count++;
		}
		return ret;
	}
}

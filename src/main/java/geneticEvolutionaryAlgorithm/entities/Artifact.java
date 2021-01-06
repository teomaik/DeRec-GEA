package geneticEvolutionaryAlgorithm.entities;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//a source file. Class in case of Java and file in case of C/Cpp
public class Artifact extends Metricable implements Comparable<Artifact> {
	private final String name;

	private Component component;
	private Hashtable<String, Artifact> dependencies = new Hashtable<String, Artifact>();

	public Artifact(String name) {
		super();
		this.name = name;
	}

	// Cohesion and Coupling for class level
	public void calculate_Metrics() {

		double tempCohesion = 0;
		double tempCoupling = 0;

		Iterator it = this.getDependencies();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();

			Artifact art = (Artifact) pair.getValue(); // ***DEBUG ***TODO ***TEST an leitourgei
			if (art.getComponent().equals(this.component)) {
				tempCohesion++;
			} else {
				tempCoupling++;
			}
			it.remove(); // avoids a ConcurrentModificationException
		}
		this.setCohesion(tempCohesion);
		this.setCoupling(tempCoupling);
	}

	public void addDependency(Artifact newDependency) {
		if (this.dependencies.contains(newDependency.getName())) {
			throw new IllegalArgumentException("Dependency for Artifact already exists");
		}
		this.dependencies.put(newDependency.getName(), newDependency);
	}
	
	public void removeDependency(Artifact newDependency) {
		this.dependencies.remove(newDependency.getName(), newDependency);
	}

	// return is this class has a dependency on a specific class
	public boolean isDependantOn(Artifact cls) {
		return dependencies.contains(cls.getName());
	}

	public Component getComponent() {
		return component;
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	public Iterator<Entry<String, Artifact>> getDependencies() {
		return dependencies.entrySet().iterator();
	}

	public void setDependencies(Hashtable<String, Artifact> newDeps) {
		this.dependencies = newDeps;
	}

	public String getName() {
		return name;
	}

//	public String toString() {
//		String ret=this.name+"\n";
//		int count=0;
//		for(Artifact art:this.dependencies) {
//			ret+= "\n\t"+count+": "+art.getName();
//			count++;
//		}
//		return ret;
//	}

	@Override
	public int compareTo(Artifact compareArtifact) {
		double compareNumber = compareArtifact.getFinalFitness();
		if (this.getFinalFitness() > compareNumber) {
			return -1;
		} else if (this.getFinalFitness() == compareNumber) {
			return 0;
		}
		return 1;
	}
}

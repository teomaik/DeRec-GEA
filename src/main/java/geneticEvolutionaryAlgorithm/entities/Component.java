package geneticEvolutionaryAlgorithm.entities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import geneticEvolutionaryAlgorithm.BaseClasses.Metricable;

public class Component extends Metricable implements Comparable<Component> {

	private String name;

	public Component(String name) {
		super();
		this.name = name;
	}

	private Hashtable<String, Artifact> myClasses = new Hashtable<String, Artifact>();
	private ArrayList<Component> myComponents = new ArrayList<Component>();

	// Cohesion and Coupling for component level
	public void calculate_Metrics() {

		this.setCohesion(0);
		this.setCoupling(0);
		if (myClasses.size() == 0) {
			return;
		}

		double tempCohesion = 0;
		double tempCoupling = 0;

		if (this.myComponents.size() != 0) {
			for (Component comp : this.myComponents) {
				comp.calculate_Metrics();
				tempCohesion += comp.getCohesion();
				tempCoupling += comp.getCoupling();
			}
			this.setCohesion(tempCohesion / this.myComponents.size());
			this.setCoupling(tempCoupling / this.myComponents.size());
		} else {
			Iterator it = myClasses.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				Artifact art = (Artifact) pair.getValue(); // ***DEBUG ***TODO ***TEST an leitourgei
				art.calculate_Metrics();
				tempCohesion += myClasses.get(pair.getKey()).getCohesion(); // ***DEBUG ***TODO ***TEST an leitourgei
				tempCoupling += myClasses.get(pair.getKey()).getCoupling(); // ***DEBUG ***TODO ***TEST an leitourgei

				// it.remove(); // avoids a ConcurrentModificationException
			}
			this.setCohesion(tempCohesion / myClasses.size());
			this.setCoupling(tempCoupling / myClasses.size());
		}

	}

	public boolean needsMoreSpliting(int maxArtifactsPerComponent) {
		if (this.myComponents.size() == 0) {
			return this.myClasses.size() > maxArtifactsPerComponent;
		}
		boolean ret = true;
		for (Component comp : this.myComponents) {
			ret = ret && comp.needsMoreSpliting(maxArtifactsPerComponent);
		}
		return ret;
	}

	// find if the component contains a specific class
	public boolean containsArtifact(Artifact cls) {
		return this.myClasses.containsKey(cls.getName());
	}

	public void removeArtifact(Artifact cls) {
		this.myClasses.remove(cls.getName(), cls);
	}

	public void addArtifact(Artifact cls) {
		if (!this.containsArtifact(cls)) {
			this.myClasses.put(cls.getName(), cls);
			cls.setComponent(this);
		}
	}

	public void addComponent(Component component) {
		this.myComponents.add(component);
	}

	public int size() {
		return this.myClasses.size();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Hashtable<String, ArrayList<String>> getComponentStructure(String path) {
		Hashtable<String, ArrayList<String>> ret = new Hashtable<String, ArrayList<String>>();
		
		path = path+this.name;
		if (this.myComponents.size() != 0) {
			for (Component comp : this.myComponents) {
				Hashtable<String, ArrayList<String>> temp = comp.getComponentStructure(path+".");
				Iterator<Entry<String, ArrayList<String>>> it = temp.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, ArrayList<String>> e = it.next();
					ret.put(e.getKey(), e.getValue());
				}
			}
			return ret;
		}else {
			ArrayList<String> cls = new ArrayList<String>();
			Iterator<Entry<String, Artifact>> it = this.myClasses.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Artifact> e = it.next();
				cls.add(e.getKey());
			}
			ret.put(path, cls);
		}

		return ret;
	}
	
	public String toString(int level, String path) {
		String pronoun = "";
		for (int i = 0; i < level; i++) {
			pronoun += "\s";
		}
		String ret = "";
		ret += "\n" + pronoun + "+" + path + this.name;

		if (this.myComponents.size() != 0) {
			for (Component comp : this.myComponents) {
				ret += comp.toString(level + 1, path + this.name + ".");
			}
			return ret;
		}

		Iterator<Entry<String, Artifact>> it = this.myClasses.entrySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			i++;
			Entry<String, Artifact> e = it.next();
			ret += "\n" + pronoun + "\s" + i + ": " + e.getKey();
		}

		return ret;
	}

	public int getNumberOfClasses() {
		return this.myClasses.size();
	}

	public Iterator<Entry<String, Artifact>> getMyClassesIterator() {
		return myClasses.entrySet().iterator();
	}

	public Hashtable<String, Artifact> getMyClasses() {
		return myClasses;
	}

	public ArrayList<Component> getMyComponents() {
		return myComponents;
	}

	public void setMyComponents(ArrayList<Component> myComponents) {
		this.myComponents = myComponents;
	}

	@Override
	public int compareTo(Component other) {
		double compareNumber = other.getFinalFitness();
		if (this.getFinalFitness() > compareNumber) {
			return -1;
		} else if (this.getFinalFitness() == compareNumber) {
			return 0;
		}
		return 1;
	}
	
	public void findClassDependencies(Hashtable<String, ArrayList<String>> classesAndDeps) {

		Iterator<Entry<String, Artifact>> it = this.myClasses.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Artifact> pair = (Map.Entry<String, Artifact>) it.next();

			Artifact artifact = pair.getValue();

			ArrayList<String> deps = classesAndDeps.get(artifact.getName());

			Iterator<String> it2 = deps.iterator();
			// clses
			while (it2.hasNext()) {
				String dep = it2.next();
				if (!this.myClasses.containsKey(dep)) {
					// it2.remove(); // avoids a ConcurrentModificationException
					continue;
				}

				// adds an Artifact as a dependency to a class
				this.myClasses.get(artifact.getName()).addDependency(this.myClasses.get(dep));

				// it2.remove(); // avoids a ConcurrentModificationException
			}

			// it.remove(); // avoids a ConcurrentModificationException
		}
	}
}

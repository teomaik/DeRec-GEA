package geneticEvolutionaryAlgorithm.entities;

import java.util.ArrayList;

public class Component extends Metricable {

	private String name;
	public Component(String name) {
		super();
		this.name = name;
	}

	private ArrayList<Artifact> myClasses = new ArrayList<Artifact>();


	//Cohesion and Coupling for component level
	public void calculate_Metrics() {
		if(myClasses.size()<=0) {
			this.setCohesion(0);
			this.setCoupling(0);
			return;
		}
		
		double tempCohesion = 0;
		double tempCoupling = 0;
		
		for (int i = 0; i < myClasses.size(); i++) {
			myClasses.get(i).calculate_Metrics();
			tempCohesion += myClasses.get(i).getCohesion();
			tempCoupling += myClasses.get(i).getCoupling();

		}

		this.setCohesion(tempCohesion/myClasses.size());
		this.setCoupling(tempCoupling/myClasses.size());
	}

	//find if the component contains a specific class
	public boolean hasClass(Artifact cls) {
		for (int i = 0; i < myClasses.size(); i++) {
			if(myClasses.get(i).equals(cls)) return true;
		}
		return false;
	}
	
	public void remove(Artifact cls) {
		for (int i = 0; i < myClasses.size(); i++) {
			if(myClasses.get(i).equals(cls)) {
				myClasses.get(i).setComponent(null);
				myClasses.remove(i);
			}
		}
	}
	
	public void addClass(Artifact cls) {
		if(!this.hasClass(cls)) {
			this.myClasses.add(cls);
			cls.setComponent(this);
		}
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
	
	public String toString() {
		String ret=this.name+"\n";
		int i=0;
		for(Artifact art : myClasses) {
			ret+="\n\t"+i+": "+art.getName();
			i++;
		}
		
		return ret;
	}

	public ArrayList<Artifact> getMyClasses() {
		return myClasses;
	}
}

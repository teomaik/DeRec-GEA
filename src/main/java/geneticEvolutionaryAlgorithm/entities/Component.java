package geneticEvolutionaryAlgorithm.entities;

import java.util.ArrayList;

public class Component extends Metricable {

	private ArrayList<Artifact> myClasses;


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
	
}

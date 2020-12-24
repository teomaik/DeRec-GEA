package geneticEvolutionaryAlgorithm.entities;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class DeRec_Individual extends Metricable implements Individual{
	private ArrayList<Artifact> classes;
	private ArrayList<Component> components;
	private double fitness=0;
	
	public void crossover() {
		// TODO Auto-generated method stub
	}
	
	public void mutate() {
		
		// TODO Auto-generated method stub
	}
	
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
//			this.setCohesion(0);
//			this.setCoupling(0);
//			return;
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
		
		// TODO Auto-generated method stub
	}


}

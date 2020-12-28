package geneticEvolutionaryAlgorithm.entities;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class DeRec_Individual extends Metricable implements Individual{
	private ArrayList<Artifact> classes;
	private ArrayList<Component> components;
	private double fitness=0;
	
	public DeRec_Individual(ArrayList<Artifact> classes) {	//***TODO DEBUG
		super();
		this.classes = classes;
		this.removeUnwantedClasses();
	}

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

	public void removeUnwantedClasses() {
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
}

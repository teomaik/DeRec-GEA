package geneticEvolutionaryAlgorithm.BaseClasses;

public class Metricable {
	
	public void calculate_Metrics() throws Exception {}
	
	private double cohesion=0;
	private double coupling=0;
	private double finalFitness=0;
	
	public double getCohesion() {
		return this.cohesion;
	}
	public double getCoupling() {
		return this.coupling;
	}
	public void setCohesion(double cohesion) {
		this.cohesion = cohesion;
	}
	public void setCoupling(double coupling) {
		this.coupling = coupling;
	}
	public double getFinalFitness() {
		if(cohesion+coupling==0) {
			this.finalFitness = 0;
			return 0;
		}
		
		this.finalFitness = cohesion/(cohesion+coupling)-coupling/(cohesion+coupling);
		
		return finalFitness;
	}
	public void setFinalFitness(double finalFitness) {
		this.finalFitness = finalFitness;
	}
}

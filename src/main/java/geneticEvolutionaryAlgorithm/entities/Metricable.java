package geneticEvolutionaryAlgorithm.entities;

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
		this.finalFitness = cohesion-coupling;
		return finalFitness;
	}
	public void setFinalFitness(double finalFitness) {
		this.finalFitness = finalFitness;
	}
}

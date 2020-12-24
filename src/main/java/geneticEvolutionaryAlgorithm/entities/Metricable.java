package geneticEvolutionaryAlgorithm.entities;

public class Metricable {
	
	public void calculate_Metrics() throws Exception {}
	
	private double cohesion=0;
	private double coupling=0;
	
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
}

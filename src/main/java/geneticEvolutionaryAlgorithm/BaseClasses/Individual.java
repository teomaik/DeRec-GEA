package geneticEvolutionaryAlgorithm.BaseClasses;

public interface Individual {
	
	abstract Individual crossover(Individual mate);

	abstract void mutate();
	
	abstract void calculateFitness() throws Exception;

	abstract double getFitness();
}

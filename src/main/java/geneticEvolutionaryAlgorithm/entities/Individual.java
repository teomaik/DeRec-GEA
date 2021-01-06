package geneticEvolutionaryAlgorithm.entities;

import java.util.ArrayList;

public interface Individual {
	
	abstract void crossover(Individual mate);

	abstract void mutate();
	
	abstract void calculateFitness() throws Exception;

	abstract double getFitness();
}

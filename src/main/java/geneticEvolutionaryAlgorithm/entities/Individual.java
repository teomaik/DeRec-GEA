package geneticEvolutionaryAlgorithm.entities;

import java.util.ArrayList;

public interface Individual {
	
	abstract void crossover();

	abstract void mutate();
	
	abstract void calculateFitness() throws Exception;
}

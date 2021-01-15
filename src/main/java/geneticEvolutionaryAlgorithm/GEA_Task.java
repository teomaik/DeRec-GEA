package geneticEvolutionaryAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import geneticEvolutionaryAlgorithm.entities.Component;
import geneticEvolutionaryAlgorithm.entities.DeRec_Individual;

public class GEA_Task implements Callable<Boolean>{
	
	ExecutorService executor;
	List<Future<Boolean>> futures;
	private Hashtable<String, ArrayList<String>> classesWithDeps;

	final int terminationCriteria = 300;
	final int populationSize = 100;	
	final int maxNumberOfArtPerComp = 20;
	
	
	private ArrayList<DeRec_Individual> population;
	
	Component componentToSplit;
	
	public GEA_Task(ExecutorService executor, List<Future<Boolean>> futures, Hashtable<String, ArrayList<String>> classesWithDeps, Component componentToSplit) {
		super();
		this.executor = executor;
		this.futures = futures;
		this.classesWithDeps = classesWithDeps;
		this.componentToSplit = componentToSplit;
	}

	@Override
	public Boolean call() throws Exception {
		System.out.println("Spliting a component with "+this.componentToSplit.getNumberOfClasses()+" Artifacts");
		try {
			population_Initialization(componentToSplit);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		DeRec_Individual fittestIndv = population.get(0);



		int terminationCounter = 0;
		int genCounter = 0;

		while (terminationCounter < terminationCriteria) {
//			System.out.println("( " + (terminationCounter+1) + "/" + terminationCriteria + " ) Generation " + genCounter
//					+ ", Fitness: " + fittestIndv.getFinalFitness() +", #Components: "+fittestIndv.getComponentNumber());

			mutatePopulation();
			calcFitness();
			
			crossoverPopulation();
			calcFitness();
			removePopulationOverhead();

			if (fittestIndv.getFinalFitness() < population.get(0).getFinalFitness()) {
				terminationCounter = 0;
				fittestIndv = population.get(0);
			} else {
				terminationCounter++;
			}
			
			genCounter++;
		}

		if(fittestIndv.getComponents().size()==1) {
			return true;
		}
		
		ArrayList<Component> newComps = fittestIndv.getComponents();
		for(Component comp : newComps) {
			this.componentToSplit.addComponent(comp);
		}
		
		ArrayList<GEA_Task> tasks = new ArrayList<GEA_Task>();
		for(Component comp : newComps) {
			System.out.println("Component: "+comp.getName()+", #classes: "+comp.getNumberOfClasses()+", needsSplit: "+comp.needsMoreSpliting(this.maxNumberOfArtPerComp));
			if(!comp.needsMoreSpliting(this.maxNumberOfArtPerComp)) {
				continue;
			}
			tasks.add(new GEA_Task(this.executor, this.futures, this.classesWithDeps, comp));
		}
		
		 synchronized(this.futures) {
		      for(GEA_Task task : tasks) {
		    	  this.futures.add(executor.submit(task));
		      }
		  }
		

			System.out.println("Split "+this.componentToSplit.getNumberOfClasses()+" Artifacts into "+fittestIndv.getComponents().size()+" new Components");
		// TODO
		return true;
	}

	private void mutatePopulation() {
		IntStream.range(1, this.populationSize/3).parallel().forEach(i -> {
			this.population.get(i).mutate();
		});
	}

//	private void crossoverPopulation() {
//		for(int i=0; i<this.populationSize/3; i+=2) {
//			this.population.add((DeRec_Individual)this.population.get(i).crossover(this.population.get(i+1)));
//		}
//	}
	private void crossoverPopulation() {
		for(int i=0; i<this.populationSize/3; i++) {
			int mateId = ThreadLocalRandom.current().nextInt(i, this.population.size());
			this.population.add((DeRec_Individual)this.population.get(i).crossover(this.population.get(mateId)));
		}
	}
	private void removePopulationOverhead() {
		while(this.population.size()>this.populationSize) {
			this.population.remove(populationSize);
		}
	}
	
	private void calcFitness() {
		
		IntStream.range(0, populationSize).parallel().forEach(i -> {
			try {
				this.population.get(i).calculateFitness();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		Collections.sort(this.population);
	}

	private void population_Initialization(Component component) throws Exception {

		this.population = new ArrayList<DeRec_Individual>();
		for (int i = 0; i < this.populationSize; i++) {
//			System.out.println("this.classesWithDeps size: "+this.classesWithDeps.size());
			this.population.add(new DeRec_Individual(component, this.classesWithDeps));
//			System.out.println("Number of comps for new indv: "+this.population.get(i).getComponentNumber());
			this.population.get(i).calculateFitness();
		}
		Collections.sort(this.population);
	}

}

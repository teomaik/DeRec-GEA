package geneticEvolutionaryAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import geneticEvolutionaryAlgorithm.entities.Artifact;
import geneticEvolutionaryAlgorithm.entities.Component;
import geneticEvolutionaryAlgorithm.entities.DeRec_Individual;

public class GEA {

	private Hashtable<String, ArrayList<String>> classesWithDeps;

	private Component root;

	public GEA(Hashtable<String, ArrayList<String>> cls) {
		this.classesWithDeps = cls;
	}

	public boolean startGEA() throws Exception {
		System.out.println("GEA.classesWithDeps size: " + classesWithDeps.size());

//		Iterator<Entry<String, ArrayList<String>>> it = classesWithDeps.entrySet().iterator();
//		while (it.hasNext()) {
//			Map.Entry<String, ArrayList<String>> e = (Map.Entry<String, ArrayList<String>>) it.next();
//			System.out.println("Class: " + e.getKey());
//			for (String s : e.getValue()) {
//				System.out.println("\tDep to: " + s);
//			}
//
//			it.remove();
//		}

		this.root = createComponent("root", classesWithDeps);
		
		List<Future<Boolean>> futures = Collections.synchronizedList(new ArrayList<Future<Boolean>>());
		int threadNum = Runtime.getRuntime().availableProcessors();
		if (threadNum <= 0) {
			throw new Exception("Unexpected error, trouble determining thread number");
		}
		ExecutorService executor = Executors.newFixedThreadPool(threadNum);
		GEA_Task task = new GEA_Task(executor, futures, this.classesWithDeps, this.root);
		synchronized (futures) {
			futures.add(executor.submit(task));
		}
		
//		// A) Await all runnables to be done (blocking)
//		for(Future<?> future : futures)
//		    future.get(); // get will block until the future is done
//
//		// B) Check if all runnables are done (non-blocking)
//		boolean allDone = true;
//		for(Future<?> future : futures){
//		    allDone &= future.isDone(); // check if future is done
//		}
		
		// A) Await all runnables to be done (blocking)
		int current=0;
		int size = futures.size();
		while(current<size) {
			futures.get(current).get(); // get will block until the future is done
			current++;
			size = futures.size();
		}

		
		executor.shutdown();
		System.out.println("Executor is shutting down");

		root.calculate_Metrics();
		System.out.println("\n\nFinal fitness: "+root.getFinalFitness());
//		System.out.println(root.toString(0, ""));
		
		DeRec_Individual newIndv = Util_GEA.getComponentFromStructureGEA("", root.getComponentStructure(""), classesWithDeps);
		//DeRec_Individual //TODO
		//Create 2 indvs (from the GEA_Utils class), one old one, and one new one. The syso their fitnesses, and then insert them both in the database
		return true;
	}

	private Component createComponent(String name, Hashtable<String, ArrayList<String>> cls) {
		Component ret = new Component(name);

		System.out.println("Project number of artifacts: " + cls.size());

		Iterator<Entry<String, ArrayList<String>>> it = cls.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> e = it.next();
//			System.out.println("Adding to root component class: "+(String)e.getKey()); //TODO Remove this, only here for testing
			ret.addArtifact(new Artifact((String) e.getKey()));
			// it.remove();
		}
		System.out.println("Root number of artifacts: " + ret.getNumberOfClasses());
		return ret;
	}
}

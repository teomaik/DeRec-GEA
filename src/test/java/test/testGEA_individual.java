package test;

import java.io.IOException;
import java.util.ArrayList;

import geneticEvolutionaryAlgorithm.entities.Artifact;
import geneticEvolutionaryAlgorithm.entities.DeRec_Individual;

public class testGEA_individual {

	public static void main(String[] args) throws IOException {
		
	}

//	public static void testRecreateIndvAsRandom() {
//		ArrayList<Artifact> classes = new ArrayList<Artifact>();
//		for (int i = 0; i < 30; i++) {
//			classes.add(new Artifact("Class_" + i + ".java"));
//		}
//		DeRec_Individual indv = new DeRec_Individual(classes);
//		indv.recreateMeAsRandomIndividual();
//
//		System.out.println("\nper Component:\n");
//		System.out.println(indv.toStringComps());
//		System.out.println("\nper Class:\n");
//		System.out.println(indv.toStringClasses());
//
//	}
//
//	public static void removeUnwatedDeps() {
//		ArrayList<Artifact> classes = new ArrayList<Artifact>();
//		for (int i = 0; i < 7; i++) {
//			classes.add(new Artifact("Class_" + i + ".java"));
//		}
//		DeRec_Individual indv = new DeRec_Individual(classes);
//
//		Artifact newArt = new Artifact("ShouldNotBeHere.java");
//		classes.get(0).addDependency(classes.get(1));
//		classes.get(0).addDependency(classes.get(2));
//		classes.get(0).addDependency(classes.get(3));
//		classes.get(0).addDependency(newArt);
//
//		System.out.println("\nbefore dep check");
//		System.out.println(classes.get(0).toString());
//		indv.removeUnwantedDependencies();
//		System.out.println("\nafter dep check");
//		System.out.println(classes.get(0).toString());
//	}
}

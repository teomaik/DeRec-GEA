package geneticEvolutionaryAlgorithm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import geneticEvolutionaryAlgorithm.entities.Artifact;
import geneticEvolutionaryAlgorithm.entities.Component;
import geneticEvolutionaryAlgorithm.entities.DeRec_Individual;

public class Util_GEA {
	
	public static DeRec_Individual getComponentFromStructureGEA(String name, Hashtable<String, ArrayList<String>> stucture, Hashtable<String, ArrayList<String>> classesWithDeps) {
		Component ret = new Component(name);
		
		Iterator<Entry<String, ArrayList<String>>> it = stucture.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, ArrayList<String>> e = it.next();
			Component component = new Component(e.getKey());
			for(String s: e.getValue()) {
				component.addArtifact(new Artifact(s));
				ret.addArtifact(new Artifact(s));
			}
			ret.addComponent(component);
		}

		return new DeRec_Individual(ret, classesWithDeps, true);
	}
	
	public static DeRec_Individual getComponentFromStructureOld(String name, Hashtable<String, ArrayList<String>> classesWithDeps) {
		Component ret = new Component(name);
		
		Hashtable<String, ArrayList<String>> stucture = new Hashtable<String, ArrayList<String>>();
//		Iterator<Entry<String, ArrayList<String>>> it = classesWithDeps.entrySet().iterator();
//		while(it.hasNext()) {
//			Entry<String, ArrayList<String>> e = it.next();
//			Component component = new Component(e.getKey());
//			for(String s: e.getValue()) {
//				component.addArtifact(new Artifact(s));
//				ret.addArtifact(new Artifact(s));
//			}
//			ret.addComponent(component);
//		}
		
		return new DeRec_Individual(ret, classesWithDeps, true);
	}
	
	//return the Pachage (Component) name, from the classes Name
	public static String filterName(String cname) {
		String parts[] = cname.split("\\.");
		String filteredName = "";
		for (int i = 0; i < parts.length - 1; i++) {
			if (i != parts.length - 2)
				filteredName += parts[i] + ".";
			else
				filteredName += parts[i];
		}

		return filteredName;
	}
}

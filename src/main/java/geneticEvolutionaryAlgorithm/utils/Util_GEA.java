package geneticEvolutionaryAlgorithm.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import geneticEvolutionaryAlgorithm.entities.Artifact;
import geneticEvolutionaryAlgorithm.entities.Component;
import geneticEvolutionaryAlgorithm.entities.DeRec_Individual;

public class Util_GEA {
	
	public static Component getComponentFromStructureGEA(String name, Hashtable<String, ArrayList<String>> stucture, Hashtable<String, ArrayList<String>> classesWithDeps) {
		Component ret = new Component(name);
		
		Iterator<Entry<String, ArrayList<String>>> it = stucture.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, ArrayList<String>> e = it.next();
			Component component = new Component(e.getKey());
			for(String s: e.getValue()) {
				Artifact art = new Artifact(s);
				ret.addArtifact(art);
				component.addArtifact(art);
			}
			ret.addComponent(component);
		}
//		ret.findClassDependencies(classesWithDeps);
//		ret.calculate_Metrics();
		return ret;
	}
	
	public static Component getComponentFromStructureOld(String name, Hashtable<String, ArrayList<String>> classesWithDeps) {
		Component ret = new Component(name);
		
		Hashtable<String, ArrayList<String>> stucture = new Hashtable<String, ArrayList<String>>();
		Iterator<Entry<String, ArrayList<String>>> it = classesWithDeps.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, ArrayList<String>> e = it.next();
			stucture.put(filterName(e.getKey()), new ArrayList<String>());
		}
		
		Iterator<Entry<String, ArrayList<String>>> it2 = classesWithDeps.entrySet().iterator();
		while(it2.hasNext()) {
			Entry<String, ArrayList<String>> e = it2.next();
			ArrayList<String> classes = stucture.get(filterName(e.getKey()));
			classes.add(e.getKey());
			stucture.put(filterName(e.getKey()), classes);
		}
		return getComponentFromStructureGEA(name, stucture, classesWithDeps);
	}
	
	public static DeRec_Individual getIndividualFromStructureGEA(String name, Hashtable<String, ArrayList<String>> stucture, Hashtable<String, ArrayList<String>> classesWithDeps) {
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
	
	public static DeRec_Individual getIndividualFromStructureOld(String name, Hashtable<String, ArrayList<String>> classesWithDeps) {
		Component ret = new Component(name);
		
		Hashtable<String, ArrayList<String>> stucture = new Hashtable<String, ArrayList<String>>();
		Iterator<Entry<String, ArrayList<String>>> it = classesWithDeps.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, ArrayList<String>> e = it.next();
			stucture.put(filterName(e.getKey()), new ArrayList<String>());
		}
		
		Iterator<Entry<String, ArrayList<String>>> it2 = classesWithDeps.entrySet().iterator();
		while(it2.hasNext()) {
			Entry<String, ArrayList<String>> e = it2.next();
			ArrayList<String> classes = stucture.get(filterName(e.getKey()));
			classes.add(e.getKey());
			stucture.put(filterName(e.getKey()), classes);
		}
		return getIndividualFromStructureGEA(name, stucture, classesWithDeps);
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

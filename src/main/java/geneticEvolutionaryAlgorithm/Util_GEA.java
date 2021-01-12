package geneticEvolutionaryAlgorithm;

public class Util_GEA {
	
	//return the Pachage (Component) name, from the classes Name
	private String filterName(String cname) {
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

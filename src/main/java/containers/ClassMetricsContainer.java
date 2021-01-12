package containers;

import metrics.ClassMetrics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ClassMetricsContainer {
	private final Map<String, ClassMetrics> classToMetrics = new HashMap<>();

	public ClassMetrics getMetrics(String name) {
		ClassMetrics cm = this.classToMetrics.get(name);
		if (cm == null) {
			cm = new ClassMetrics();
			this.classToMetrics.put(name, cm);
		}
		return cm;
	}
	
	public Iterator<Entry<String, ClassMetrics>> getClassToMetricsIter() {
		return classToMetrics.entrySet().iterator();
	}
}

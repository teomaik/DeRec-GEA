package containers;

import metrics.ClassMetrics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ClassMetricsContainer {
	private final HashMap<String, ClassMetrics> classToMetrics = new HashMap<>();

	public ClassMetrics getMetrics(String name) {
		ClassMetrics cm = this.classToMetrics.get(name);
		if (cm == null) {
			cm = new ClassMetrics();
			this.classToMetrics.put(name, cm);
		}
		return cm;
	}
	
	public void test() {
		Iterator it = classToMetrics.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        //System.out.println(pair.getKey() + " = " + (ClassMetrics)pair.getValue());
	        ClassMetrics cm = this.classToMetrics.get(pair.getKey());
	        
	        System.out.println("Class : "+pair.getKey());
	        cm.test_afferentCoupledClasses();
	        
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	}

	public Iterator<Entry<String, ClassMetrics>> getClassToMetricsIter() {
		return classToMetrics.entrySet().iterator();
	}
}

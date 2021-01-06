package geneticEvolutionaryAlgorithm.entities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Component extends Metricable {

	private String name;
	public Component(String name) {
		super();
		this.name = name;
	}

	private Hashtable<String, Artifact> myClasses = new Hashtable<String, Artifact>();
	private ArrayList<Component> myComponents = new ArrayList<Component>();


	//Cohesion and Coupling for component level
	public void calculate_Metrics() {
		
		this.setCohesion(0);
		this.setCoupling(0);
		if(myClasses.size()==0) {
			return;
		}
		
		double tempCohesion = 0;
		double tempCoupling = 0;
		
		Iterator it = myClasses.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			Artifact art = (Artifact) pair.getValue();	//***DEBUG ***TODO ***TEST an leitourgei
			art.calculate_Metrics();
			tempCohesion += myClasses.get(pair.getKey()).getCohesion();	//***DEBUG ***TODO ***TEST an leitourgei
			tempCoupling += myClasses.get(pair.getKey()).getCoupling();	//***DEBUG ***TODO ***TEST an leitourgei
			
			it.remove(); // avoids a ConcurrentModificationException
		}

		this.setCohesion(tempCohesion/myClasses.size());
		this.setCoupling(tempCoupling/myClasses.size());
	}

	//find if the component contains a specific class
	public boolean hasClass(Artifact cls) {
		return this.myClasses.contains(cls.getName());
	}
	
	public void remove(Artifact cls) {
		this.myClasses.remove(cls.getName(), cls);
	}
	
	public void addClass(Artifact cls) {
		if(!this.hasClass(cls)) {
			this.myClasses.put(cls.getName(), cls);
			cls.setComponent(this);
		}
	}
	
	public int size() {
		return this.myClasses.size();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
//	public String toString() {
//		String ret=this.name+"\n";
//		int i=0;
//		for(Artifact art : myClasses) {
//			ret+="\n\t"+i+": "+art.getName();
//			i++;
//		}
//		
//		return ret;
//	}
	
	public Iterator<Entry<String, Artifact>> getMyClassesIterator() {
		return myClasses.entrySet().iterator();
	}

	public Hashtable<String, Artifact> getMyClasses() {
		return myClasses;
	}
}

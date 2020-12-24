package metrics;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class ClassMetrics extends Metrics {
	protected int noc;
	private float rfc;
	private int dit;
	private boolean visited;
	private boolean isPublicClass;
	private boolean isAbstract; 
	private final HashSet<String> afferentCoupledClasses; // Ca
	private final HashSet<MethodDeclaration> polymorphicMethods; // Nop

	public ClassMetrics() {
		this.noc = 0;
		this.dit = -1;
		this.rfc = 0;
		this.visited = false;
		this.afferentCoupledClasses = new HashSet<>();
		this.polymorphicMethods = new HashSet<>();
	}

	public void setGMOODLowLevel() {
		this.dsc = 1;
		this.nom = this.wmc;
		if(this.noc>0 && this.ana == 0){
			this.noh = 1;
		}
	}
	
	public void setQMOODHighLevel() {

		this.reusability = -0.25 * this.dcc + 0.25 * this.camc + 0.5 * this.npm + 0.5 * this.dsc;
	    this.flexibility = -0.25 * this.dcc + 0.25 * this.dam + 0.5 * this.moa + 0.5 * this.nop;
	    this.understandability = -0.33 * this.ana + 0.33 * this.dam + 0.33 * this.camc - 0.33 * this.dcc - 0.33 * this.nop - 0.33 * this.nom - 0.33 * this.dsc;
	    this.functionality = 0.12 * this.camc + 0.22 * this.nop + 0.22 * this.npm + 0.22 * this.dsc + 0.22 * this.noh;
	    this.extendibility = 0.5 * this.ana - 0.5 * this.dcc + 0.5 * this.mfa + 0.5 * this.nop;
	    this.effectiveness = 0.2 * this.ana + 0.2 * this.dam + 0.2 * this.moa + 0.2 * this.mfa + 0.2 * this.nop;    
	
	}

	public void test_afferentCoupledClasses() {
		for(String s : afferentCoupledClasses){
			System.out.println("dep class: "+s);
		}
//		Iterator it = afferentCoupledClasses.iterator();
//	    while (it.hasNext()) {
//	        Map.Entry pair = (Map.Entry)it.next();
//	        System.out.println(pair.getKey() + " = " + (ClassMetrics)pair.getValue());
//	        it.remove(); // avoids a ConcurrentModificationException
//	    }
	}
	
	public void incWmc() {
		this.wmc += 1.0F;
	}

	public void incNoc() {
		this.noc += 1;
	}

	public int getNoc() {
		return this.noc;
	}

	public void setRfc(float r) {
		this.rfc = r;
	}

	public float getRfc() {
		return this.rfc;
	}

	public void setDit(int d) {
		this.dit = d;
	}

	public int getDit() {
		return this.dit;
	}

	public int getCa() {
		return this.afferentCoupledClasses.size();
	}

	public void addAfferentCoupling(String name) {
		this.afferentCoupledClasses.add(name);
	}

	//public float getNop() {
	//	return this.polymorhicMethods.size();
	//}

	public void incNpm() {
		this.npm += 1.0F;
	}

	public boolean isPublic() {
		return this.isPublicClass;
	}

	public void setPublic() {
		this.isPublicClass = true;
	}

	public void setAbstract() {
		this.isAbstract = true;
	}

	public boolean isAbstract() {
		return this.isAbstract;
	}

	public static boolean isJdkClass(String s) {
		return (s.startsWith("java.")) || (s.startsWith("javax."))
				|| (s.startsWith("org.omg.")) || (s.startsWith("org.w3c.dom."))
				|| (s.startsWith("org.xml.sax."));
	}

	public String toString() {
		float size = 1.0F;
		float hierarchies = -1.0F;
		float abstraction = this.mfa;
		float encapsulation = this.dam;
		float coupling = this.cbo;
		float cohesion = this.lcom;
		float composition = this.moa;
		float ancestors = this.ana;
		float polymorphism = this.nop;
		float messaging = getNpm();
		float complexity = this.wmc;
		return "\t\t" + size + "\t\t" + hierarchies + "\t\t" + abstraction
				+ "\t\t" + encapsulation + "\t\t\t" + coupling + "\t\t"
				+ cohesion + "\t\t" + composition + "\t\t" + ancestors + "\t\t"
				+ polymorphism + "\t\t" + messaging + "\t\t" + complexity;
	}

public String getOutput(){
	StringBuilder builder = new StringBuilder();
	
	/*
	 * Chidamber & Kemerer metrics
	 */
	builder.append(this.wmc).append(";");
	builder.append(this.dit).append(";");
	builder.append(this.noc).append(";");
	builder.append(this.cbo + this.getCa()).append(";");
	builder.append(this.rfc).append(";");
	builder.append(this.lcom).append(";");
	/*
	 *  Li & Henry metrics
	 */
	if (this.wmc_cc == -1) builder.append("-1000;");
		else builder.append(this.wmc_cc).append(";");
	builder.append(this.dit).append(";");
	builder.append(this.noc).append(";");
	builder.append(this.rfc).append(";");
	if(lcom == -1) builder.append("-1000;"); 
	else builder.append(this.lcom).append(";");
	builder.append(this.wmc).append(";"); // (NOM: number of methods) as in WMC of C&K metrics
	builder.append(this.mpc).append(";");
	builder.append(this.dac).append(";");
 	builder.append(this.size1).append(";");
	builder.append(this.size2).append(";");
	/*
	 * Bansyia metrics
	 */
	builder.append(this.dsc).append(";");
	builder.append(this.noh).append(";");
	builder.append(this.ana).append(";");
	if (this.dam == -1) builder.append("-1000;"); 
		else builder.append(this.dam).append(";");
	builder.append(this.cbo).append(";");      // DCC = CBO = efferent coupling
	if (this.camc == -1) builder.append("-1000;");
		else builder.append(this.camc).append(";");
	builder.append(this.moa).append(";");
	builder.append(this.mfa).append(";");
	builder.append(this.nop).append(";");
	builder.append(this.npm).append(";"); // CIS is calculated as NPM
	builder.append(this.wmc).append(";"); // NOM is calculated as WMC of C&K metrics suite
	/*
	 * QMOOD
	 */
	builder.append(this.reusability).append(";");
	builder.append(this.flexibility).append(";");
	builder.append(this.understandability).append(";");
	builder.append(this.functionality).append(";");
	builder.append(this.extendibility).append(";");
	builder.append(this.effectiveness).append(";");
	/*
	 * ..extra!
	 */
	builder.append(this.getCa());	// FanIn is calculated as Ca

		return builder.toString();
	}

	public void setVisited() {
		this.visited = true;
	}

	public boolean isVisited() {
		return this.visited;
	}

	public void incNop(int amount) {
		this.nop += amount;
	}

	public void addPolymorhicMethods(Collection<MethodDeclaration> methods) {
		this.polymorphicMethods.addAll(methods);
	}
}

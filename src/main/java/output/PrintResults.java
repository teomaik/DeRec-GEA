package output;

import calculator.MetricsCalculator;
import metrics.ClassMetrics;
import metrics.PackageMetrics;
import metrics.ProjectMetrics;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Map.Entry;
import java.util.Set;

public class PrintResults implements CkjmOutputHandler {
	private PrintStream p;
	private ByteArrayOutputStream os;

	public PrintResults() {
		this.os = new ByteArrayOutputStream();
		this.p = new PrintStream(this.os);
		printHeader();
	}

	public void printHeader() {
		this.p.print("Name;");
		this.p.print("WMC;DIT;NOCC;CBO;RFC;LCOM;");						// Chidamber & Kemerer metrics
		this.p.print("WMC;DIT;NOCC;RFC;LCOM;NOM;MPC;DAC;SIZE1;SIZE2;");	// Li & Henry metrics
		this.p.print("DSC;NOH;ANA;DAM;DCC;CAMC;MOA;MFA;NOP;CIS;NOM;");	// Bansyia metrics (CIS is calculated as NPM)
		this.p.print("Reusability;Flexibility;Understandability;Functionality;Extendibility;Effectiveness;");	// QMOOD?
		this.p.print("FanIn\n");	// EXTRA! (FanIn is calculated as Ca)
	}
	
   public void handleClass(String name, ClassMetrics c) {
		c.setGMOODLowLevel();
	 	c.setQMOODHighLevel();
     	this.p.println(name + ";" + c.getOutput());
   }

	@Override
	public void handleProject(String projectName, ProjectMetrics projectMetrics) {
		Set<Entry<String, PackageMetrics>> rootPackages = MetricsCalculator.getProjectMetricsContainer()
				.getPackages(projectName).entrySet();
		Entry<String, PackageMetrics> currentPackage;
		for (Entry<String, PackageMetrics> rootPackage : rootPackages) {
			currentPackage = rootPackage;
			handlePackage(currentPackage.getKey());
		}
	}
	 
  	private void handlePackage(String packageName) {
	  	Set<Entry<String, PackageMetrics>> subPackages = MetricsCalculator.getPackageMetricsContainer()
			  	.getPackageSubpackages(packageName).entrySet();
	  	Entry<String, PackageMetrics> currentPackage;
	  	for (Entry<String, PackageMetrics> subPackage : subPackages) {
		  	currentPackage = subPackage;
		  	handlePackage(currentPackage.getKey());
	  	}

	  	Set<Entry<String, ClassMetrics>> classes = MetricsCalculator.getPackageMetricsContainer()
			  	.getPackageClasses(packageName).entrySet();
	  	Entry<String, ClassMetrics> currentClass;
	  	for (Entry<String, ClassMetrics> aClass : classes) {
		  	currentClass = aClass;
		  	handleClass(currentClass.getKey(),
				  	currentClass.getValue());
	  	}
  	}

	public StringReader getOutput(){
		try {
			return new StringReader(this.os.toString("UTF-8"));
		} catch (Exception e) {
			return null;
		}
	}

}

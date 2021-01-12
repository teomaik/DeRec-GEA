package output;

import calculator.MetricsCalculator;
import metrics.ClassMetrics;
import metrics.PackageMetrics;
import metrics.ProjectMetrics;

import java.io.*;
import java.util.Map.Entry;
import java.util.Set;

import static java.lang.System.exit;

public class PrintResults implements CkjmOutputHandler {

	private PrintStream p;
	private ByteArrayOutputStream os;

	public PrintResults() {
		try {
			os  = new ByteArrayOutputStream();
			this.p = new PrintStream(os);
			printHeader();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exiting...");
			exit(-1);
		}
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
     	this.p.println(name + ";" + c.getCsvOutput());
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
		this.p.close();
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
			return new StringReader(os.toString("UTF8"));
		} catch (UnsupportedEncodingException ignored) {}
		return null;
	}
}

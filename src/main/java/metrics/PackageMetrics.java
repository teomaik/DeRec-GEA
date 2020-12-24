package metrics;

import java.util.Map.Entry;
import java.util.Set;
import calculator.MetricsCalculator;

public class PackageMetrics extends WideMetrics {
	public void calculateAllMetrics(String packageName) {
		if (isCalculated())
			return;
		this.classesCount += MetricsCalculator.getPackageMetricsContainer()
				.getPackageClasses(packageName).size();

		Set<Entry<String, PackageMetrics>> subPackages = MetricsCalculator.getPackageMetricsContainer()
				.getPackageSubpackages(packageName).entrySet();
		Entry<String, PackageMetrics> currentPackage;

		for (Entry<String, PackageMetrics> subPackage : subPackages) {
			currentPackage = subPackage;
			currentPackage.getValue()
					.calculateAllMetrics( currentPackage.getKey());
			this.classesCount += currentPackage.getValue().classesCount;
			increaseMetrics(currentPackage.getValue());
		}

		Set<Entry<String, ClassMetrics>> classes = MetricsCalculator.getPackageMetricsContainer()
				.getPackageClasses(packageName).entrySet();
		Entry<String, ClassMetrics> currentClass;
		for (Entry<String, ClassMetrics> aClass : classes) {
			currentClass = aClass;
			increaseMetrics(currentClass.getValue());
		}
		finalizeMetrics();
		this.calculated = true;
	}

	private void increaseMetrics(ClassMetrics classMetrics) {
		this.ana += classMetrics.getAna();
		this.cbo += classMetrics.getCbo();
		this.moa += classMetrics.getMoa();
		this.nop += classMetrics.getNop();
		this.lcom += classMetrics.getLcom();
		this.npm += classMetrics.getNpm();
		this.wmc += classMetrics.getWmc();
		this.mfa += classMetrics.getMfa();
		if (!Float.isNaN(classMetrics.getDam())) {
			if (Float.isNaN(getDam()))
				this.dam = 0.0F;
			this.dam += classMetrics.getDam();
			this.damValidClassesCount += 1;
		}
		if ((classMetrics.ana == 0.0F) && (classMetrics.noc > 0))
			this.noh = (int) ((float) (this.noh + 1.0D));
		this.dsc += 1.0F;
	}

	private void increaseMetrics(PackageMetrics packageMetrics) {
		this.ana += packageMetrics.ana * packageMetrics.classesCount;
		this.cbo += packageMetrics.cbo * packageMetrics.classesCount;
		this.moa += packageMetrics.moa * packageMetrics.classesCount;
		this.nop += packageMetrics.nop * packageMetrics.classesCount;
		this.lcom += packageMetrics.lcom * packageMetrics.classesCount;
		this.npm += packageMetrics.npm * packageMetrics.classesCount;
		this.wmc += packageMetrics.wmc * packageMetrics.classesCount;
		this.mfa += packageMetrics.mfa * packageMetrics.classesCount;
		if (!Float.isNaN(packageMetrics.dam)) {
			if (Float.isNaN(this.dam))
				this.dam = 0.0F;
			this.dam += packageMetrics.dam
					* packageMetrics.damValidClassesCount;
			this.damValidClassesCount += packageMetrics.damValidClassesCount;
		}

		this.noh += packageMetrics.noh;
		this.dsc += packageMetrics.dsc;
	}

	private void finalizeMetrics() {
		this.ana /= this.classesCount;
		this.cbo /= this.classesCount;
		this.moa /= this.classesCount;
		this.nop /= this.classesCount;
		this.lcom /= this.classesCount;
		this.npm /= this.classesCount;
		this.wmc /= this.classesCount;
		this.mfa /= this.classesCount;
		this.dam /= this.damValidClassesCount;
	}
}
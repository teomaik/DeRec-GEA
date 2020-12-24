package metrics;

import calculator.MetricsCalculator;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class ProjectMetrics extends WideMetrics {
	public void calculateAllMetrics(String project) {
		if (isCalculated()) {
			return;
		}
		HashMap<String, PackageMetrics> packageMetrics = MetricsCalculator.getProjectMetricsContainer()
				.getPackages(project);
		Set<Entry<String, PackageMetrics>> rootPackages = packageMetrics.entrySet();
		Entry<String, PackageMetrics> currentPackage;
		for (Entry<String, PackageMetrics> rootPackage : rootPackages) {
			currentPackage = rootPackage;
			(currentPackage.getValue())
					.calculateAllMetrics(currentPackage.getKey());
			this.classesCount += currentPackage.getValue().classesCount;
			increaseMetrics(currentPackage.getValue());
		}
		finalizeMetrics();
		this.calculated = true;
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
}

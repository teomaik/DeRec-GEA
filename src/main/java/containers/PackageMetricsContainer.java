package containers;

import calculator.MetricsCalculator;
import metrics.ClassMetrics;
import metrics.PackageMetrics;

import java.util.HashMap;

public class PackageMetricsContainer {
	private final HashMap<String, PackageMetrics> packageToMetric;
	private final HashMap<String, HashMap<String, ClassMetrics>> packageClasses;
	private final HashMap<String, HashMap<String, PackageMetrics>> packageSubpackages;

	public PackageMetricsContainer() {
		this.packageToMetric = new HashMap<>();
		this.packageClasses = new HashMap<>();
		this.packageSubpackages = new HashMap<>();
	}

	public PackageMetrics getMetrics(String name) {
		PackageMetrics packageMetrics = this.packageToMetric
				.get(name);
		if (packageMetrics == null) {
			packageMetrics = new PackageMetrics();
			this.packageToMetric.put(name, packageMetrics);
		}
		return packageMetrics;
	}

	public boolean containsPackage(String packageName) {
		return (this.packageSubpackages.containsKey(packageName))
				|| (this.packageToMetric.containsKey(packageName));
	}

	public HashMap<String, ClassMetrics> getPackageClasses(String packageName) {
		return this.packageClasses.computeIfAbsent(packageName, k -> new HashMap<>());
	}

	public HashMap<String, PackageMetrics> getPackageSubpackages(
			String packageName) {
		return this.packageSubpackages
				.computeIfAbsent(packageName, k -> new HashMap<>());
	}

	public void addClassToPackage(String packageName, String javaClass,
			ClassMetrics javaMetrics) {
		HashMap<String, ClassMetrics> packageClasses = getPackageClasses(packageName);
		packageClasses.put(javaClass, javaMetrics);
	}

	public void addPackage(String packageName) {
		if (existsInSubPackages(packageName)) {
			return;
		}
		int dotIndex = packageName.lastIndexOf('.');

		if (dotIndex == -1) {
			getPackageSubpackages(packageName);
			String currentProject = MetricsCalculator.getCurrentProject();
			MetricsCalculator.getProjectMetricsContainer()
					.getPackages(currentProject)
					.put(packageName, getMetrics(packageName));
			return;
		}

		String parentPackage = packageName.substring(0, dotIndex);
		String childPackage = packageName;
		while (!parentPackage.isEmpty()) {
			dotIndex = childPackage.lastIndexOf('.');
			if (dotIndex == -1) {
				getPackageSubpackages(parentPackage);
				String currentProject = MetricsCalculator.getCurrentProject();
				MetricsCalculator.getProjectMetricsContainer()
						.getPackages(currentProject)
						.put(parentPackage, getMetrics(parentPackage));
				break;
			}

			parentPackage = childPackage.substring(0, dotIndex);
			if (!existsInSubPackages(childPackage)) {
				getPackageSubpackages(parentPackage).put(childPackage,
						getMetrics(childPackage));
			}
			childPackage = parentPackage;
		}
	}

	private boolean existsInSubPackages(String packageName) {
		String parent = getParentPackage(packageName);
		return (this.packageSubpackages.containsKey(parent))
				&& ((this.packageSubpackages.get(parent))
				.containsKey(packageName));
	}

	private String getParentPackage(String packageName) {
		int packageSepetatorIndex = packageName.lastIndexOf('.');
		if (packageSepetatorIndex == -1)
			return "";
		return packageName.substring(0, packageSepetatorIndex);
	}
}

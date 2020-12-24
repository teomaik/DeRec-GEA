package containers;

import metrics.PackageMetrics;
import metrics.ProjectMetrics;

import java.util.HashMap;

public class ProjectMetricsContainer {
	private final HashMap<String, ProjectMetrics> projectToMetric;
	private final HashMap<String, HashMap<String, PackageMetrics>> projectPackages;

	public ProjectMetricsContainer() {
		this.projectToMetric = new HashMap<>();
		this.projectPackages = new HashMap<>();
	}

	public HashMap<String, ProjectMetrics> getProjects() {
		return this.projectToMetric;
	}

	public ProjectMetrics getMetrics(String name) {
		ProjectMetrics pm = this.projectToMetric.get(name);
		if (pm == null) {
			pm = new ProjectMetrics();
			this.projectToMetric.put(name, pm);
		}
		return pm;
	}

	public HashMap<String, PackageMetrics> getPackages(String name) {
		return this.projectPackages.computeIfAbsent(name, k -> new HashMap<>());
	}
}

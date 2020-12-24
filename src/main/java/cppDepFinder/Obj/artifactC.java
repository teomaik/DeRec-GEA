package cppDepFinder.Obj;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class artifactC {

	private String path;
	private String name;

	private List<artifactC> artifactDeps = new ArrayList<artifactC>();

	public artifactC(depFile tmp) {

		if (tmp == null || !tmp.isInitialized()) {
			throw new IllegalArgumentException("Invalid values. They must not be null, empty or blank");
		}
		
		this.path = replaceLast(tmp.getPath(), new File(tmp.getPath()).getName(), "");
		
		String[] split = new File(tmp.getPath()).getName().split("\\.");
		this.name = replaceLast(tmp.getName(), "." + split[split.length - 1], "");
		System.out.println("Created Artifact  " + tmp.getName() + " ---> " + this.name);
	}

	private String replaceLast(String text, String regex, String replacement) {
		return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}

	public boolean isMyArtifact(depFile tmp) {
		if (tmp == null || tmp.getName() == null) {
			throw new IllegalArgumentException("Invalid values. They must not be null, empty or blank");
		}
		
		String tempPath = replaceLast(tmp.getPath(), new File(tmp.getPath()).getName(), "");
		
		String[] split = tmp.getName().split("\\.");		
		String tempName = replaceLast(tmp.getName(), "." + split[split.length - 1], "");
		if (this.path.equals(tempPath) && this.name.equals(tempName)) {
			return true;
		}
		return false;
	}

	public boolean addArtifactDep(artifactC art) {
		if (art == null || art.getName() == null) {
			throw new IllegalArgumentException("Invalid values. They must not be null, empty or blank");
		}
		if (isSameArtifact(art)) {
			return false;
		}
		for (artifactC tmp : this.artifactDeps) {
			if (tmp.isSameArtifact(art)) {
				return false;
			}
		}
		this.artifactDeps.add(art);
		return true;
	}

	private boolean isSameArtifact(artifactC tmp) {
		if (tmp == null || tmp.getName() == null) {
			throw new IllegalArgumentException("Invalid values. They must not be null, empty or blank");
		}
		if (this.name.equals(tmp.getName()) && this.path.equals(tmp.getPath())) {
			return true;
		}
		return false;
	}

	public String toString() {
		return this.name + " ( " + this.path + " ), noDeps: " + this.artifactDeps.size();
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public List<artifactC> getArtifactDeps() {
		return artifactDeps;
	}
}

package cppDepFinder.Obj;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ArtifactC {

	private String path;
	private String name;

	private List<ArtifactC> artifactDeps = new ArrayList<ArtifactC>();

	public ArtifactC(DepFile tmp) {

		if (tmp == null || !tmp.isInitialized()) {
			throw new IllegalArgumentException("Invalid values. They must not be null, empty or blank");
		}
		
		this.path = replaceLast(tmp.getPath(), new File(tmp.getPath()).getName(), "");
		
		String[] split = new File(tmp.getPath()).getName().split("\\.");
		this.name = replaceLast(tmp.getName(), "." + split[split.length - 1], "");
		//System.out.println("Created Artifact  " + tmp.getName() + " ---> " + this.name);
	}

	private String replaceLast(String text, String regex, String replacement) {
		return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}

	public boolean isMyArtifact(DepFile tmp) {
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

	public boolean addArtifactDep(ArtifactC art) {
		if (art == null || art.getName() == null) {
			throw new IllegalArgumentException("Invalid values. They must not be null, empty or blank");
		}
		if (isSameArtifact(art)) {
			return false;
		}
		for (ArtifactC tmp : this.artifactDeps) {
			if (tmp.isSameArtifact(art)) {
				return false;
			}
		}
		this.artifactDeps.add(art);
		return true;
	}

	private boolean isSameArtifact(ArtifactC tmp) {
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

	public List<ArtifactC> getArtifactDeps() {
		return artifactDeps;
	}
}

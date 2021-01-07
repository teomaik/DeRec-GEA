package cppDepFinder.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import cppDepFinder.Obj.ArtifactC;
import cppDepFinder.Obj.DepFile;

public class ArtifactList {

	private List<ArtifactC> artifacts = new ArrayList<ArtifactC>();

	public ArtifactList(SourceDeps srcDps) {
		if (srcDps == null) {
			throw new IllegalArgumentException("Invalid values. They must not be null, empty or blank");
		}
		initArtifactList(srcDps);
		calculateArtifactDeps(srcDps);
	}

	private void initArtifactList(SourceDeps srcDps) {
		for (DepFile tmpSrcDep : srcDps.getDepFileList()) {
			boolean exists = false;
			for (ArtifactC tmpArt : this.artifacts) {
				if (tmpArt.isMyArtifact(tmpSrcDep)) {
					exists = true;
					break;
				}
			}
			if (exists) {
				continue;
			}
			this.artifacts.add(new ArtifactC(tmpSrcDep));
			//System.out.println(this.artifacts.get(this.artifacts.size()-1).getName()+"   added");
		}
		//System.out.println("*** artifactC count: " + this.artifacts.size());
	}

	private void calculateArtifactDeps(SourceDeps srcDps) {
		for (DepFile tmpSrcDep : srcDps.getDepFileList()) {
			for (ArtifactC tmpArt : this.artifacts) {
				if (!tmpArt.isMyArtifact(tmpSrcDep)) {
					continue;
				}
				for (DepFile tmpDep : tmpSrcDep.getDeps()) {
					for (ArtifactC tmpArt2 : this.artifacts) {
						if (!tmpArt2.isMyArtifact(tmpDep)) {
							continue;
						}
						tmpArt.addArtifactDep(tmpArt2);
						break;
					}
				}
				break;
			}
		}
	}

	String redundantPath = "";

	public void setReduntantPath(String rdPath) {
		if (rdPath == null) {
			throw new IllegalArgumentException("Invalid values. They must not be null, empty or blank");
		} else if (rdPath.isEmpty()) {
			this.redundantPath = "";
		} else {
			this.redundantPath = rdPath;
		}
	}

	private String getSutableName(ArtifactC tmp) {
		return tmp.getPath().replace(this.redundantPath, "").replace(File.separator, ".") + tmp.getName();
	}

	private String getSutablePackageName(ArtifactC tmp) {
		String ret = tmp.getPath().replace(this.redundantPath, "").replace(File.separator, ".");
		ret = replaceLast(ret, ".", "");
		return ret;
	}

	private String replaceLast(String text, String regex, String replacement) {
		return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}

//	public ClassAxisObject[] getClassAxisObjectArray() {
//		ClassAxisObject[] ret = new ClassAxisObject[this.artifacts.size()];
//		for (int i = 0; i < this.artifacts.size(); i++) {
//			ret[i] = new ClassAxisObject(getSutableName(this.artifacts.get(i)));
//			ret[i].setPackageName(getSutablePackageName(this.artifacts.get(i)));
//		}
//		for (int i = 0; i < ret.length; i++) {
//			List<Axis> newList = new ArrayList<Axis>();
//			String thisName = getSutableName(this.artifacts.get(i));
//			for (artifactC tempDep : this.artifacts.get(i).getArtifactDeps()) {
//				newList.add(new Axis("desc", getSutableName(tempDep), thisName));
//			}
//
//			ret[i].setAxisListForGEA(newList);
//		}
//		return ret;
//	}
	
	public Hashtable<String, ArrayList<String>> getClsDeps(){
		Hashtable<String, ArrayList<String>> ret = new Hashtable<String, ArrayList<String>>();
		
		for (int i = 0; i < this.artifacts.size(); i++) {
			String className = getSutableName(this.artifacts.get(i));
			ArrayList<String> deps = new ArrayList<String>();
			
			for (ArtifactC tempDep : this.artifacts.get(i).getArtifactDeps()) {
				deps.add(getSutableName(tempDep));
			}
			ret.put(className, deps);
		}
		return ret;
	}

	public void printArtifactList() {
		//System.out.println("---> Artifact List:");
		for (int i = 0; i < this.artifacts.size(); i++) {
			//System.out.println("#" + i + ": " + this.artifacts.get(i).toString());
		}
	}
}

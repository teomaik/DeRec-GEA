package cppDepFinder;

import java.io.File;
import java.util.List;

import cppDepFinder.util.artifactList;
import cppDepFinder.util.fileReader;
import cppDepFinder.util.sourceDeps;
import genetic_algorithm.TreeGEAController;
import gr.uom.java.metric.probability.xml.ClassAxisObject;

public class testDep {

	public testDep(String prjName, String path, String pathToDbCredFile) {
		if (prjName == null || prjName.isEmpty() || path == null || path.isEmpty() || pathToDbCredFile == null
				|| pathToDbCredFile.isEmpty()) {
			throw new IllegalArgumentException("Invalid values. They must not be null, empty or blank");
		}
		try {
			ClassAxisObject[] cls = findDependency(path);
			if (cls == null || cls.length == 0) {
				System.out.println("Null class table");
				return;
			}
			runGea(cls, prjName, pathToDbCredFile);
			System.out.println("FIN");
		} catch (Exception e) {
			System.out.println("error");
		}
	}
	
	public String replaceLast(String text, String regex, String replacement) {
		return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}
	 
	private ClassAxisObject[] findDependency(String path) {
		
		File fileTmp = new File(path);
		String redundantPath = replaceLast(path, fileTmp.getName(), "");
		
		System.out.println("***---> " + path);
		System.out.println("***---> " + redundantPath);
		fileReader fr = new fileReader();
		try {
			List<File> fileList = fr.getFileListCpp(path);
			sourceDeps srcTest = new sourceDeps(fileList);
			System.out.println(srcTest.getResultCount());
			artifactList artifacts = new artifactList(srcTest);
			artifacts.printArtifactList();
			artifacts.setReduntantPath(redundantPath);
			ClassAxisObject[] cls = artifacts.getClassAxisObjectArray();
			System.out.println("cls.length: " + cls.length);
			int count = 0;
			for (ClassAxisObject tmp : cls) {
				count++;
				System.out.println("***# " + count + ": " + tmp.getName() + ", noDeps: " + tmp.getAxisGEANumber());
			}
			return cls;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	private void runGea(ClassAxisObject[] cls, String prjName, String path) {
		TreeGEAController geaCont = new TreeGEAController(cls, prjName, path);
	}

}

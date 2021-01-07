package cppDepFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import cppDepFinder.util.ArtifactList;
import cppDepFinder.util.FileReader;
import cppDepFinder.util.SourceDeps;

public class TestDep {

	public static Hashtable<String, ArrayList<String>> getArtWithDeps(String path) {
		
		File fileTmp = new File(path);
		String redundantPath = replaceLast(path, fileTmp.getName(), "");
		
		FileReader fr = new FileReader();
		
		List<File> fileList = fr.getFileListCpp(path);
		SourceDeps srcTest = new SourceDeps(fileList);
		ArtifactList artifacts = new ArtifactList(srcTest);
		artifacts.printArtifactList();
		artifacts.setReduntantPath(redundantPath);
		return artifacts.getClsDeps();
		
	}

	private static String replaceLast(String text, String regex, String replacement) {
		return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}


}

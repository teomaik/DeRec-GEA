package cppDepFinder.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cppDepFinder.Obj.DepFile;

public class SourceDeps {

	private List<File> files = new ArrayList<File>();;
	private List<DepFile> depFileList = new ArrayList<DepFile>();

	public SourceDeps(List<File> temp) {
		if (temp == null || temp.size() == 0) {
			throw new IllegalArgumentException("Invalid values. They must not be null, empty or blank");
		}
		this.files = temp;
		createDepFiles();
		testPrint();
	}

	private void testPrint() {
		for (DepFile tmp : depFileList) {
			if (!tmp.isSourceFile())
				continue;
			//System.out.println("***#1 testPrint name " + tmp.getName() + ", deps: " + tmp.getDeps().size());
		}
	}

	public String getResultCount() {
		return "Files Count: " + files.size() + "\ndepFiles Count:" + depFileList.size();
	}

	private void createDepFiles() {
		try {
			for (File f : files) {
				getDepFileList().add(new DepFile(f.getName(), f.getAbsolutePath(), f, this.getDepFileList()));
			}
			for (DepFile s : this.getDepFileList()) {
				s.calcStartDeps();
			}
		} catch (Exception e) {
			//System.out.println(e.getMessage());
		}
	}

	public List<DepFile> getDepFileList() {
		return depFileList;
	}
}

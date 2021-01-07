package cppDepFinder.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileReader {

	private final String[] extensions = { ".c", ".h", ".cpp", ".cc", ".cp", ".cxx", ".c++", ".cu", ".hpp", ".hh", ".hp",
			".hxx", ".h++", ".hcu" };

	private final String[] sourceExtensions = { ".c", ".cpp", ".cc", ".cp", ".cxx", ".c++", ".cu" };

	public FileReader() {
	}

	public List<File> getFileListCpp(String directoryName) {
		if (directoryName == null || directoryName.length()==0 || directoryName.isEmpty()) {
			throw new IllegalArgumentException("Invalid values. They must not be null, empty or blank");
		}
		List<File> tempor = new ArrayList<>();
		File directory = new File(directoryName);
		if (!directory.exists() || !directory.isDirectory()) {
			throw new IllegalArgumentException("Invalid values. Path must point to a directory");
		}
		listFilesOfInterestCpp(directoryName, tempor);
		//System.out.println("***Total number of files:" + tempor.size());
		return tempor;
	}

	private void listFilesOfInterestCpp(String directoryName, List<File> files) {
		File directory = new File(directoryName);
		//System.out.println("directoryName: " + directoryName);
		//System.out.println("directoryPath: " + directory.getAbsolutePath());
		File[] fList = directory.listFiles();
		if (fList != null)
			for (File file : fList) {
				String fileName = file.getName();
				if (file.isFile()) {
					for (String ext : this.getExtensions()) {
						if (fileName.endsWith(ext)) {
							files.add(file);
							break;
						}
					}
				} else if (file.isDirectory()) {
					listFilesOfInterestCpp(file.getAbsolutePath(), files);
				}
			}
	}

	public String[] getSourceExtensions() {
		return sourceExtensions;
	}

	private String[] getExtensions() {
		return extensions;
	}

}

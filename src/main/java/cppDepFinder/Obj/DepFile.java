package cppDepFinder.Obj;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cppDepFinder.util.FileReader;


public class DepFile {

	private File file = null;
	private String name = null;
	private String path = null;
	private ArrayList<DepFile> deps = new ArrayList<DepFile>();
	private boolean isSrc = false;

	private List<DepFile> filesList = new ArrayList<DepFile>();

	public DepFile(String n, String p, File f) {
		if (n == null || n.isEmpty() || f == null || !f.exists() || p == null || p.isEmpty()) {
			throw new IllegalArgumentException("Invalid values. They must not be null, empty or blank");
		}
		this.setName(n);
		this.setPath(p);
		this.setFile(f);
	}

	public DepFile(String n, String p, File f, List<DepFile> dfl) {
		if (n == null || n.isEmpty() || f == null || !f.exists() || p == null || p.isEmpty()) {
			throw new IllegalArgumentException("Invalid values. They must not be null, empty or blank");
		}
		this.setName(n);
		this.setPath(p);
		this.setFile(f);
		this.setFilesList(dfl);
	}

	public boolean isInitialized() {
		if (this.name == null || this.path == null) {
			return false;
		}
		return true;
	}

	private void addNewDependancy(DepFile newDep) {
		if (this.getPath() == newDep.getPath()) {
			return;
		}
		for (DepFile x : this.getDeps()) {
			if (x.getPath() == newDep.getPath()) {
				return;
			}
		}
		this.getDeps().add(newDep);
	}

	public void updateDeps() {
		if (!this.isSrc) {
			return;
		}
		int i = 0;
		int end = this.getDeps().size();
		ArrayList<DepFile> tmpDeps = new ArrayList<DepFile>();
		while (i < end) {
			if (this.getDeps().get(i).isSourceFile()) {
				i++;
				continue;
			}
			ArrayList<DepFile> childDeps = this.getDeps().get(i).getDeps();
			for (int k = 0; k < childDeps.size(); k++) {
				tmpDeps.add(childDeps.get(k));
			}

			end = this.getDeps().size();
			i++;
		}
		for (DepFile tmp : tmpDeps) {
			this.addNewDependancy(tmp);
		}
	}

	public void calcStartDeps() {
		if (this.getName() == null) {
			return;
		}
		FileReader fr = new FileReader();
		String[] srcExt = fr.getSourceExtensions();
		for (String ext : srcExt) {
			if (this.getName().endsWith(ext)) {
				this.set_isSourceFile(true);
				break;
			}
		}
		File file = this.getFile();
		try {
			Scanner input = new Scanner(new FileInputStream(file));
			while (input.hasNextLine()) {
				String line = input.nextLine();
				line = line.replaceAll("\\s", "");
				if (!line.startsWith("#include")) {
					continue;
				}
				line = line.replaceFirst("#include", "");
				String firstChar = String.valueOf(line.charAt(0));
				if (firstChar.equals("<")) {
					firstChar = ">";
				}
				line = line.replaceFirst(".", "");
				int iend = line.indexOf(firstChar);
				String newDep = "";
				if (iend == -1) {
					continue;
				}
				newDep = line.substring(0, iend);
				if (newDep.contains("/")) {
					String[] split = newDep.split("/");
					newDep = split[split.length - 1];
				}
				for (DepFile h : this.getFilesList()) {
					if (newDep == null || newDep.isEmpty() || h.getName() == null) {
						continue;
					}
					if (h.getName().equals(newDep)) {
						this.addNewDependancy(h);
					}
				}
			}
			input.close();
		} catch (Exception e) {
			//System.out.println("Exception at depFile StartDeps \n" + e.getMessage());
		}
	}

	public void printDepsC() {
		if (!this.isSourceFile()) {
			return;
		}
		String ret = this.getPath() + "   :";
		for (DepFile d : deps) {
			ret += "\n\t--->" + d.getPath();
		}
		//System.out.println(ret);
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	private void setPath(String path) {
		this.path = path;
	}

	private File getFile() {
		return file;
	}

	private void setFile(File file) {
		this.file = file;
	}

	public ArrayList<DepFile> getDeps() {
		return deps;
	}

	private List<DepFile> getFilesList() {
		return filesList;
	}

	private void setFilesList(List<DepFile> depFilesList) {
		this.filesList = depFilesList;
	}

	public boolean isSourceFile() {
		return isSrc;
	}

	private void set_isSourceFile(boolean isSrc) {
		this.isSrc = isSrc;
	}

}

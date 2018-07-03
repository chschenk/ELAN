package mpi.eudico.client.annotator.lexicon.api;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.lexicon.api.Param;

/**
 * Class that is to be used to create a Lexicon Service Client Factory from an extension.
 * @author Micha Hulsbosch
 *
 */
public class LexSrvcClntBundle {
	private String description;
	private ClassLoader loader;
	private List<Param> paramList;
	private String name;
	private URL[] javaLibs;
	private URL[] nativeLibs;
	private File baseDir;
	private String type;
	private String binaryName;
	
	public LexSrvcClntBundle() {
		super();
	}
	
	public LexSrvcClntBundle(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public ClassLoader getLoader() {
		return loader;
	}

	public void setLoader(ClassLoader loader) {
		this.loader = loader;
	}

	public List<Param> getParamList() {
		if (paramList == null) {
			return null;
		}
		//make a copy of the list
		ArrayList<Param> params = new ArrayList<Param>(paramList.size());
		for (Param p : paramList) {
			try {
			params.add((Param) p.clone());
			} catch (CloneNotSupportedException cnse) {
				
			}
		}
		
		return params;
	}

	public void setParamList(List<Param> paramList) {
		this.paramList = paramList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public URL[] getJavaLibs() {
		return javaLibs;
	}

	public void setJavaLibs(URL[] javaLibs) {
		this.javaLibs = javaLibs;
	}

	public URL[] getNativeLibs() {
		return nativeLibs;
	}

	public void setNativeLibs(URL[] nativeLibs) {
		this.nativeLibs = nativeLibs;
	}

	public File getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	public void setLexSrvcClntClass(String binaryName) {
		this.binaryName = binaryName;
	}

	public void setLexExecutionType(String type) {
		this.type = type;
	}

	public Object getLexExecutionType() {
		return type;
	}

	public String getLexSrvcClntClass() {
		return binaryName;
	}
}

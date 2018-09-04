package mpi.eudico.client.annotator.interannotator;

import java.util.List;

import mpi.eudico.server.corpora.clom.AnnotationCore;

/**
 * Simple class to store some information and objects for annotator comparisons.
 */
public class CompareUnit {
	// only public members
	public String fileName;
	public String tierName;
	public String annotator;
	public List<AnnotationCore> annotations;
	
	public CompareUnit() {
		super();
	}

	public CompareUnit(String fileName, String tierName, String annotator) {
		super();
		this.fileName = fileName;
		this.tierName = tierName;
		this.annotator = annotator;
	}
}

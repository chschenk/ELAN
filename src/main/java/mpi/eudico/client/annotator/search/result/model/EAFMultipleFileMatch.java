package mpi.eudico.client.annotator.search.result.model;

import mpi.search.content.result.model.AbstractContentMatch;

/**
 * Created on Aug 17, 2004
 * @author Alexander Klassmann
 * @version Aug 17, 2004
 */
public class EAFMultipleFileMatch extends AbstractContentMatch {
	final private String value;
	private String id;

	public EAFMultipleFileMatch(String value){
		this.value = value;
	}
	
	public EAFMultipleFileMatch(String id, String value){
		this.id = id;
		this.value = value;
	}

	public void setFileName(String s){
		fileName = s;
	}
	
	public void setTierName(String s){
		tierName = s;
	}
	
	public void setLeftContext(String s){
		leftContext = s;
	}
	
	public void setRightContext(String s){
		rightContext = s;
	}
	
	public void setBeginTimeBoundary(long time){
		beginTime = time;
	}
	

	public void setEndTimeBoundary(long time){
		endTime = time;
	}
	
	/* (non-Javadoc)
	 * @see mpi.eudico.server.corpora.clom.AnnotationCore#getValue()
	 */
	@Override
	public String getValue() {
		return value;
	}
	
	public void setMatchedSubstringIndices(int[][] substringIndices){
		this.matchedSubstringIndices = substringIndices;
	}
	
	public void setId(String id) {
		this.id = id;	
	}
	
	public String getId(){
		return id;
	}
	//add children and parent context mod.Coralie Villes
	@Override
	public String getChildrenContext() {
		return null;
	}

	@Override
	public String getParentContext() {
		return null;
	}
}

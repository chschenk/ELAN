package mpi.search.content.result.model;

import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.search.result.model.Match;

/**
 * Created on Jul 22, 2004
 * @author Alexander Klassmann
 * @version Jul 22, 2004
 */
public interface ContentMatch extends AnnotationCore, Match{

	public String getFileName();

	public String getTierName();
	
	public String getLeftContext();
	
	public String getRightContext();
	//mod. Coralie Villes
	public String getParentContext();
	
	public String getChildrenContext();
	
	public int[][] getMatchedSubstringIndices();
	
	public int getIndex();
	
}

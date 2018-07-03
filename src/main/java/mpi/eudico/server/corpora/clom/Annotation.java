package mpi.eudico.server.corpora.clom;

import java.util.List;

import mpi.eudico.server.corpora.event.ParentAnnotationListener;
import mpi.eudico.server.corpora.util.ACMEditableObject;

public interface Annotation
	extends AnnotationCore, Comparable<Annotation>, ACMEditableObject, ParentAnnotationListener, ParentAnnotation {

	public void setValue(String theValue);
	public void updateValue(String theValue);
	public Tier getTier();
	public void markDeleted(boolean deleted);
	public boolean isMarkedDeleted();

	public List<Annotation> getChildrenOnTier(Tier tier);

	/**
	* Checks if this Annotation has a parent Annotation.
	*/
	public boolean hasParentAnnotation();

	/**
	 * Returns this Annotation's parent Annotation.
	 */
	public Annotation getParentAnnotation();
	
	/**
	 * Returns the id of the annotation
	 * @return
	 */
	public String getId();
	
	/**
	 * Sets the id of the annotation
	 * @param s
	 */
	public void setId(String s);
	
	// By Micha:
	/**
	 * Returns the id of a CVEntry if this annotation is associated with one
	 */
	public String getCVEntryId();
	
	/**
	 * Sets the id of a CVEntry
	 * 
	 * @param cVEntryId the CVEntry id
	 */
	public void setCVEntryId(String cVEntryId);

}

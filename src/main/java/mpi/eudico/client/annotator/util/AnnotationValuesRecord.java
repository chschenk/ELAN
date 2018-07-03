package mpi.eudico.client.annotator.util;

import mpi.eudico.server.corpora.clom.Annotation;


/**
 * An AnnotationDataRecord extended with a field + getter and setter for
 * the new value. Used for undo/redo.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class AnnotationValuesRecord extends AnnotationDataRecord {
	private String newLabelValue;

    /**
     * Construcor.
     *
     * @param annotation the annotation
     */
	public AnnotationValuesRecord(Annotation annotation) {
		super(annotation);
	}
	
	
    /**
	 * @param tierName tier name
	 * @param value the annotation value
	 * @param beginTime the begin time 
	 * @param endTime the end time
	 */
	public AnnotationValuesRecord(String tierName, String value,
			long beginTime, long endTime) {
		super(tierName, value, beginTime, endTime);
	}


	/**
     * Returns the new label or value of the annotation
     *
     * @return the new label or value of the annotation
     */
    public String getNewLabelValue() {
        return newLabelValue;
    }

    /**
     * Sets the new label or value of the annotation
     *
     * @param newLabelValue the new value
     */
    public void setNewLabelValue(String newLabelValue) {
        this.newLabelValue = newLabelValue;
    }

}

package mpi.eudico.client.annotator.turnsandscenemode;

import mpi.eudico.server.corpora.clom.Annotation;

/**
 * A wrapper class for a real or a "virtual" annotation in a table- or list model. 
 * A virtual annotation represents a gap, an empty space between annotations, a space
 * where annotations can be created.
 * 
 * @author Han Sloetjes
 */
public class TaSAnno {
	private Annotation ann;
	private String participant;
	private long bt;
	private long et;
	private String curValue;
	
	public TaSAnno(Annotation annotation) {
		ann = annotation;
		setParticipant(annotation);
	}
	
	public TaSAnno(long begin, long end) {
		bt = begin;
		et = end;
		setParticipant(null);
	}

	private void setParticipant(Annotation annotation) {
		if (annotation != null) {
			participant = annotation.getTier().getParticipant();
		} else {
			participant = "";
		}
	}
	
	public String getParticipant() {
		return participant;
	}
	
	public Annotation getAnnotation() {
		return ann;
	}

	public void setAnnotation(Annotation ann) {
		this.ann = ann;
		setParticipant(ann);
	}

	public long getBeginTime() {
		if (ann != null) {
			return ann.getBeginTimeBoundary();
		}
		return bt;
	}

	public void setBeginTime(long bt) {
		this.bt = bt;
	}

	public long getEndTime() {
		if (ann != null) {
			return ann.getEndTimeBoundary();
		}
		return et;
	}

	public void setEndTime(long et) {
		this.et = et;
	}
	
	public String getText() {
		if (ann != null) {
			return ann.getValue();
		}
		else if (curValue != null) {
			return curValue;
		}
		
		return "";
	}
	
	/**
	 * This can or could be used to store text that has been entered in a segment 
	 * where there is no annotation yet.
	 * 
	 * @param text the entered text
	 */
	public void setText(String text) {
		curValue = text;
	}
}
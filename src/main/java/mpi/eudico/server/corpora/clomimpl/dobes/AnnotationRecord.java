/*
 * Created on Jun 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mpi.eudico.server.corpora.clomimpl.dobes;

/**
 * @author hennie
 */
public class AnnotationRecord {

	public static final String ALIGNABLE = "alignable";
	public static final String REFERENCE = "reference";
	
	private String annotId;
	private String annotType;
	private String beginTimeSlotId;
	private String endTimeSlotId;
	private String referredAnnotId;
	private String previousAnnotId;
	private String annotValue;
	private String extRefId;
	private String cvEntryId;
	
	private TimeSlotRecord beginTimeSlotRecord;
	private TimeSlotRecord endTimeSlotRecord;
	
	public String getAnnotationId() {
		return annotId;
	}
	
	public void setAnnotationId(String annotId) {
		this.annotId = annotId;
	}
	
	public String getAnnotationType() {
		return annotType;
	}
	
	public void setAnnotationType(String annotType) {
		this.annotType = annotType;
	}
	
	public String getBeginTimeSlotId() {
		if (beginTimeSlotRecord != null) {
			return "ts" + beginTimeSlotRecord.getId();
		}
		return beginTimeSlotId;
	}
	
	public void setBeginTimeSlotId(String beginTSId) {
		beginTimeSlotId = beginTSId;
	}
	
	public String getEndTimeSlotId() {
		if (endTimeSlotRecord != null) {
			return "ts" + endTimeSlotRecord.getId();
		}
		return endTimeSlotId;
	}
	
	public void setEndTimeSlotId(String endTSId) {
		endTimeSlotId = endTSId;
	}

	public String getReferredAnnotId() {
		return referredAnnotId;
	}
	
	public void setReferredAnnotId(String refAnnotId) {
		referredAnnotId = refAnnotId;
	}
	
	public String getPreviousAnnotId() {
		return previousAnnotId;
	}
	
	public void setPreviousAnnotId(String previousAnnotId) {
		this.previousAnnotId = previousAnnotId;
	}
	
	/**
	 * Returns the id of an external reference object
	 * 
	 * @return the extRefId the id of an external reference, e.g. a concept defined in ISO DCR
	 */
	public String getExtRefId() {
		return extRefId;
	}

	/**
	 * Sets the external reference id.
	 * 
	 * @param extRefId the extRefId to set
	 */
	public void setExtRefId(String extRefId) {
		this.extRefId = extRefId;
	}
	
	/**
	 * @return the cvEntryId
	 */
	public String getCvEntryId() {
		return cvEntryId;
	}

	/**
	 * @param cvEntryId the cvEntryId to set
	 */
	public void setCvEntryId(String cvEntryId) {
		this.cvEntryId = cvEntryId;
	}
	
	public String getValue() {
		return annotValue;
	}
	
	public void setValue(String annotValue) {
		this.annotValue = annotValue;
	}
	
	@Override
	public String toString() {
		String result = "";
		
		result += "id:        " + annotId + "\n";
		result += "type:      " + annotType + "\n";
		result += "begin id:  " + beginTimeSlotId + "\n";
		result += "end id:    " + endTimeSlotId + "\n";
		result += "ref'ed id: " + referredAnnotId + "\n";
		result += "prev id:   " + previousAnnotId + "\n";
		result += "extref id: " + extRefId + "\n";
		result += "cventry id: " + cvEntryId + "\n";
		result += "value:     " + annotValue + "\n";
		
		return result;
	}

	/**
	 * @return Returns the beginTimeSlotRecord.
	 */
	public TimeSlotRecord getBeginTimeSlotRecord() {
		return beginTimeSlotRecord;
	}

	/**
	 * @param beginTimeSlotRecord The beginTimeSlotRecord to set.
	 */
	public void setBeginTimeSlotRecord(TimeSlotRecord beginTimeSlotRecord) {
		this.beginTimeSlotRecord = beginTimeSlotRecord;
	}

	/**
	 * @return Returns the endTimeSlotRecord.
	 */
	public TimeSlotRecord getEndTimeSlotRecord() {
		return endTimeSlotRecord;
	}

	/**
	 * @param endTimeSlotRecord The endTimeSlotRecord to set.
	 */
	public void setEndTimeSlotRecord(TimeSlotRecord endTimeSlotRecord) {
		this.endTimeSlotRecord = endTimeSlotRecord;
	}

}

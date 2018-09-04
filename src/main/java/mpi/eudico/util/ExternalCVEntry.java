package mpi.eudico.util;

/**
 * This class no longer adds any fields to CVEntry.
 * Consider removing it if this is likely to remain so.
 * 
 * @author olasei
 */
@SuppressWarnings("serial")
public class ExternalCVEntry extends CVEntry {

	public ExternalCVEntry(BasicControlledVocabulary parent, String content, String desc, String id) {
		super(parent, content, desc);
		setId(id);
	}
	
	public ExternalCVEntry(BasicControlledVocabulary parent) {
		super(parent);
	}
	
	public ExternalCVEntry(BasicControlledVocabulary parent, ExternalCVEntry orig) {
		super(parent, orig);
	}
	
	public ExternalCVEntry(BasicControlledVocabulary parent, CVEntry orig) {
		super(parent, orig);
	}	
}

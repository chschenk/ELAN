package mpi.eudico.client.annotator.multiplefilesedit;

import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

public class InconsistentTypeException extends Exception {
	private static final long serialVersionUID = -8494027511309895429L;
	private LinguisticType inconsistent_type;
	
	public InconsistentTypeException(LinguisticType inconsistent_type) {
		this.inconsistent_type = inconsistent_type;
	}
	
	public LinguisticType getInconsistentType() {
		return inconsistent_type;
	}

}

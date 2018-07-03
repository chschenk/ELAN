package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.ExternalReference;


/**
 * A class for storing information about references to external resources or
 * externally  defined concepts.
 *
 * @author Han Sloetjes
 */
public class ExternalReferenceImpl implements ExternalReference, Cloneable {
	private static final long serialVersionUID = 8946176200603135792L;
	private String value;
    private int type;
    
    /**
     * Constructor.
     *
     * @param value the value of the reference
     * @param type the type of reference, one of the constants declared in
     *        <code>ExternalReference</code>
     */
    public ExternalReferenceImpl(String value, int type) {
        super();
        this.value = value;
        // check the type??
        this.type = type;
    }
    
    /**
     * Constructor with a String representation of the type.
     * 
     * @param value
     * @param type as returned from getTypeString()
     */
    public ExternalReferenceImpl(String value, String type) {
    	super();
    	this.value = value;
    	this.type = ExternalReference.UNDEFINED;
    	
		if (type != null) {
			if (type.equals("iso12620")) {
				this.type = ExternalReference.ISO12620_DC_ID;
			} else if (type.equals("resource_url")) {
				this.type = ExternalReference.RESOURCE_URL;
			} else if (type.equals("ecv")) {
				this.type = ExternalReference.EXTERNAL_CV;
			} else if (type.equals("cve_id")) {
				this.type = ExternalReference.CVE_ID;
			} else if (type.equals("lexen_id")) {
				this.type = ExternalReference.LEXEN_ID;
			}
		}
    }

    /**
     * @see mpi.eudico.server.corpora.clom.ExternalReference#getReferenceType()
     */
    @Override
	public int getReferenceType() {
        return type;
    }

    /**
     * @see mpi.eudico.server.corpora.clom.ExternalReference#getValue()
     */
    @Override
	public String getValue() {
        return value;
    }

    /**
     * @see mpi.eudico.server.corpora.clom.ExternalReference#setReferenceType(int)
     */
    @Override
	public void setReferenceType(int refType) {
        type = refType;
    }

    /**
     * @see mpi.eudico.server.corpora.clom.ExternalReference#setValue(java.lang.String)
     */
    @Override
	public void setValue(String value) {
        this.value = value;
    }

    /**
     * @see mpi.eudico.server.corpora.clom.ExternalReference#paramString()
     */
    @Override
	public String paramString() {
        if (type == ExternalReference.ISO12620_DC_ID) {
            return "DCR: " + value;
        } else if (type == ExternalReference.RESOURCE_URL) {
            return "URL: " + value;
        } else if (type == ExternalReference.EXTERNAL_CV) {
            return "ECV: " + value;
        }

        return value;
    }

    /**
     * Creates a clone of this object.
     *
     * @see java.lang.Object#clone()
     */
    @Override
	public ExternalReference clone() throws CloneNotSupportedException {
        ExternalReferenceImpl cloneER = new ExternalReferenceImpl(value, type);

        return cloneER;
    }

    /**
     * Returns true if the type is equal and the values are equal or both null.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object obj) {
        if (obj instanceof ExternalReferenceImpl) {
            ExternalReferenceImpl other = (ExternalReferenceImpl) obj;

            if (other.getReferenceType() != type) {
                return false;
            }

            if ((other.getValue() != null) && !other.getValue().equals(value)) {
                return false;
            }

            if ((value != null) && !value.equals(other.getValue())) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }
    
    @Override
    public int hashCode() {
    	int h = 7;
    	if (value != null) {
    		h += value.hashCode();
    	}
    	h += type * 17;
    	return h;
    }

	@Override
	public String getTypeString() {
		switch (type) {
		case ExternalReference.UNDEFINED:
		default:
			return "undefined";
		case ExternalReference.ISO12620_DC_ID:
			return "iso12620";
		case ExternalReference.RESOURCE_URL:
			return "resource_url";
		case ExternalReference.EXTERNAL_CV:
			return "ecv";
		case ExternalReference.CVE_ID:
			return "cve_id";
		case ExternalReference.LEXEN_ID:
			return "lexen_id";
		}
	}
}

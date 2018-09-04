package mpi.eudico.server.corpora.clomimpl.abstr;

/**
 * A descriptor for any kind of file that is linked to or associated with  a
 * transcription, other than audio and video files (or the subjects of
 * transcription).  All fields are public.
 * NB: an interface and abstract super class could/should be created that 
 * is extended by this class and MediaDescriptors.
 * 
 * @author Han Sloetjes
 */
public class LinkedFileDescriptor implements Cloneable {
    /** Holds value of constant unknown MIME type */
    public final static String UNKNOWN_MIME_TYPE = "unknown";

	/** text MIME type */
	public final static String TEXT_TYPE = "text/plain";
	
	/** xml MIME type */
	public final static String XML_TYPE = "text/xml";

	/** svg MIME type */
	public final static String SVG_TYPE = "image/svg+xml";	
	
	/** cmdi MIME type */
	public final static String CMDI_TYPE = "application/x-cmdi+xml";	
	
	/** imdi MIME type */
	public final static String IMDI_TYPE = "application/x-imdi+xml";	
	
    /** the url of the linked file */
    public String linkURL;
    
    /** holds the relative URL, relative to the containing document */
    public String relativeLinkURL;

    /** the mimetype string */
    public String mimeType;

    /** the time origin or offset */
    public long timeOrigin;

    /**
     * the url of a media file that this file is associated with  in whichever
     * way
     */
    public String associatedWith;

    /**
     * the url of a configuration file that might contain information  on how
     * to treat this file
     */
    public String configFile; //??

    /**
     * Constructor.
     *
     * @param linkURL the url of the linked file
     * @param mimeType a mimetype string
     *
     * @throws NullPointerException if the link url is null
     */
    public LinkedFileDescriptor(String linkURL, String mimeType) {
        if (linkURL == null) {
            throw new NullPointerException();
        }

        this.linkURL = linkURL;

        if (mimeType != null) {
            this.mimeType = mimeType;
        } else {
            this.mimeType = UNKNOWN_MIME_TYPE;
        }
    }

    /**
     * Returns a string representation of this descriptor
     *
     * @return a string representation of this descriptor
     */
    @Override
	public String toString() {
        return linkURL + " " + mimeType + " " + timeOrigin + " " +
        associatedWith + " " + configFile;
    }

    /**
     * Returns a deep copy of this LinkedFileDescriptor.
     *
     * @return a deep copy of this LinkedFileDescriptor
     */
    @Override
	public Object clone() {
        try {
            LinkedFileDescriptor cloneLFD = (LinkedFileDescriptor) super.clone();

            if (linkURL != null) {
                cloneLFD.linkURL = new String(linkURL);
            }
            
            if (relativeLinkURL != null) {
            	cloneLFD.relativeLinkURL = new String(relativeLinkURL);
            }
            
            if (mimeType != null) {
                cloneLFD.mimeType = new String(mimeType);
            }

            if (associatedWith != null) {
                cloneLFD.associatedWith = new String(associatedWith);
            }

            if (configFile != null) {
                cloneLFD.configFile = new String(configFile);
            }

            cloneLFD.timeOrigin = timeOrigin;

            return cloneLFD;
        } catch (CloneNotSupportedException cnse) {
            // should not happen
            // throw an exception?
            return null;
        }
    }

    /**
     * Overrides <code>Object</code>'s equals method by checking all  fields of
     * the other object to be equal to all fields in this  object.
     *
     * @param obj the reference object with which to compare
     *
     * @return true if this object is the same as the obj argument; false
     *         otherwise
     */
    @Override
	public boolean equals(Object obj) {
        if (obj == null) {
            // null is never equal
            return false;
        }

        if (obj == this) {
            // same object reference 
            return true;
        }

        if (!(obj instanceof LinkedFileDescriptor)) {
            // it should be a LinkedFileDescriptor object
            return false;
        }

        // check the fields
        LinkedFileDescriptor other = (LinkedFileDescriptor) obj;
        
        if (!sameString(this.linkURL, other.linkURL)) {
        	return false;
        }

        if (!sameString(this.relativeLinkURL, other.relativeLinkURL)) {
        	return false;
        }

        if (!sameString(this.mimeType, other.mimeType)) {
        	return false;
        }

        if (!sameString(this.associatedWith, other.associatedWith)) {
        	return false;
        }

        if (!sameString(this.configFile, other.configFile)) {
        	return false;
        }

        if (this.timeOrigin != other.timeOrigin) {
            return false;
        }

        return true;
    }
    
    private boolean sameString(String s1, String s2) {
    	if (s1 == null) {
    		return s2 == null;
    	}
    	if (s2 == null) {
    		return false;	// because s1 != null
    	}
    	return s1.equals(s2);
    }
    
    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((associatedWith == null) ? 0 : associatedWith.hashCode());
		result = prime * result
				+ ((configFile == null) ? 0 : configFile.hashCode());
		result = prime * result + ((linkURL == null) ? 0 : linkURL.hashCode());
		result = prime * result
				+ ((mimeType == null) ? 0 : mimeType.hashCode());
		result = prime * result
				+ ((relativeLinkURL == null) ? 0 : relativeLinkURL.hashCode());
		result = prime * result + (int) (timeOrigin ^ (timeOrigin >>> 32));
		return result;
	}
}

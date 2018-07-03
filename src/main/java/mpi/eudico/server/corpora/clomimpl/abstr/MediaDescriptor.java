package mpi.eudico.server.corpora.clomimpl.abstr;

/**
 * DOCUMENT ME!
 * $Id: MediaDescriptor.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class MediaDescriptor implements Cloneable{
    /** Holds value of property DOCUMENT ME! */
    public final static String MPG_MIME_TYPE = "video/mpeg";

    /** Holds value of property DOCUMENT ME! */
    public final static String WAV_MIME_TYPE = "audio/x-wav";
    
	/** Holds value of constant unknown MIME type */
	public final static String UNKNOWN_MIME_TYPE = "unknown";
	
	/** a constant for QuickTime file types */
	public static final String QUICKTIME_MIME_TYPE = "video/quicktime";
	
	/** a constant for mp4 file types */
	public static final String MP4_MIME_TYPE = "video/mp4";
	
	/** a constant for any video type */
	public static final String GENERIC_VIDEO_TYPE = "video/*";
	
	/** a constant for any audio type */
	public static final String GENERIC_AUDIO_TYPE = "audio/*";
	
	/** a constant for the jpg image type */
	public static final String JPEG_TYPE = "image/jpeg";

    /** Holds value of property DOCUMENT ME! */
    public String mediaURL;
    
    /** holds the relative URL, relative to the containing document */
    public String relativeMediaURL;

    /** Holds value of property DOCUMENT ME! */
    public String mimeType;

    /** Holds value of property DOCUMENT ME! */
    public long timeOrigin;

    /** Holds value of property DOCUMENT ME! */
    public String extractedFrom;
    
    public boolean isValid;

    /**
     * Creates a new MediaDescriptor instance
     *
     * @param theMediaURL DOCUMENT ME!
     * @param theMimeType DOCUMENT ME!
     */
    public MediaDescriptor(String theMediaURL, String theMimeType) {
        mediaURL = theMediaURL;
        if (theMimeType != null) {
			mimeType = theMimeType;
        } else {
        	mimeType = UNKNOWN_MIME_TYPE;
        }
        
        isValid = true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String toString() {
        return mediaURL + " " + mimeType + " " + timeOrigin + " " +
        extractedFrom;
    }
    
    /**
     * Returns a deep copy of this MediaDescriptor.
     * 
     * @return a deep copy of this MediaDescriptor 
     */
    @Override
	public Object clone() {
    	try {
    		MediaDescriptor cloneMD =(MediaDescriptor)super.clone();
    		if (mediaURL != null) {
				cloneMD.mediaURL = new String(mediaURL);
    		}
    		if (relativeMediaURL != null) {
    			cloneMD.relativeMediaURL = new String(relativeMediaURL);
    		}
    		if (mimeType != null) {
    			cloneMD.mimeType = new String(mimeType);
    		}
    		if (extractedFrom != null) {
    			cloneMD.extractedFrom = new String(extractedFrom);
			}
    		cloneMD.timeOrigin = timeOrigin;
    		cloneMD.isValid = isValid;
    		
    		return cloneMD;
    	} catch (CloneNotSupportedException cnse) {
    		// should not happen
    		// throw an exception?
    		return null;
    	}
    }
    
    /**
     * Overrides <code>Object</code>'s equals method by checking all 
     * fields of the other object to be equal to all fields in this 
     * object.
     * 
     * @param obj the reference object with which to compare
     * @return true if this object is the same as the obj argument; false otherwise
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
    	if (!(obj instanceof MediaDescriptor)) {
    		// it should be a MediaDescriptor object
    		return false;
    	}
    	// check the fields
    	MediaDescriptor other = (MediaDescriptor) obj;
    	if (!sameString(this.mediaURL, other.mediaURL)) { 
    		return false;
    	}
    	if (!sameString(this.relativeMediaURL, other.relativeMediaURL)) { 
        	return false;
        }
		if (!sameString(this.mimeType, other.mimeType)) { 
			return false;
		}
		if (!sameString(this.extractedFrom, other.extractedFrom)) { 
			return false;
		}
		if (this.timeOrigin != other.timeOrigin) {
			return false;   	
		}
		if (this.isValid != other.isValid) {
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
    
    /**
     * Use exactly the fields as used in equals().
     */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((extractedFrom == null) ? 0 : extractedFrom.hashCode());
		result = prime * result + (isValid ? 1231 : 1237);
		result = prime * result
				+ ((mediaURL == null) ? 0 : mediaURL.hashCode());
		result = prime * result
				+ ((mimeType == null) ? 0 : mimeType.hashCode());
		result = prime
				* result
				+ ((relativeMediaURL == null) ? 0 : relativeMediaURL.hashCode());
		result = prime * result + (int) (timeOrigin ^ (timeOrigin >>> 32));
		return result;
	}
}

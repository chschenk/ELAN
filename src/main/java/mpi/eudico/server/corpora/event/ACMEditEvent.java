package mpi.eudico.server.corpora.event;

import java.util.EventObject;


/**
 * DOCUMENT ME!
 * $Id: ACMEditEvent.java 45803 2016-12-16 16:42:46Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class ACMEditEvent extends EventObject {
    // operation constants

    /** Holds value of property DOCUMENT ME! */
    public static final int ADD_TIER = 0;

    /** Holds value of property DOCUMENT ME! */
    public static final int REMOVE_TIER = 1;

    /** Holds value of property DOCUMENT ME! */
    public static final int CHANGE_TIER = 2;

    /** Holds value of property DOCUMENT ME! */
    public static final int ADD_ANNOTATION_HERE = 3;

    /** Holds value of property DOCUMENT ME! */
    public static final int ADD_ANNOTATION_BEFORE = 4;

    /** Holds value of property DOCUMENT ME! */
    public static final int ADD_ANNOTATION_AFTER = 5;

    /** Holds value of property DOCUMENT ME! */
    public static final int REMOVE_ANNOTATION = 6;

    /** Holds value of property DOCUMENT ME! */
    public static final int CHANGE_ANNOTATION_TIME = 7;

    /** Holds value of property DOCUMENT ME! */
    public static final int CHANGE_ANNOTATION_VALUE = 8;

    /** Holds value of property DOCUMENT ME! */
    public static final int ADD_LINGUISTIC_TYPE = 9;

    /** Holds value of property DOCUMENT ME! */
    public static final int REMOVE_LINGUISTIC_TYPE = 10;

    /** Holds value of property DOCUMENT ME! */
    public static final int CHANGE_LINGUISTIC_TYPE = 11;

    /** Holds value of property DOCUMENT ME! */
    public static final int CHANGE_ANNOTATION_GRAPHICS = 12;
    
	/** marks any change in a ControlledVocabulary */
	public static final int CHANGE_CONTROLLED_VOCABULARY = 13;
	
	/** any changes in an unspecified number of annotations 
	 *  on an unspecified number of tiers */
	public static final int CHANGE_ANNOTATIONS = 14;
	
	/** marks any change in the external reference from an annotation */
	public static final int CHANGE_ANNOTATION_EXTERNAL_REFERENCE = 15;

	/** the add lexicon query bundle event */
	public static final int ADD_LEXICON_QUERY_BUNDLE = 16;

	/** the change lexicon query bundle event */
	public static final int CHANGE_LEXICON_QUERY_BUNDLE = 17;

	/** the delete link to lexicon event */
	public static final int DELETE_LEXICON_LINK = 18;

	/** the add link to lexicon event */
	public static final int ADD_LEXICON_LINK = 19;
	
	/** the add a comment event */
	public static final int ADD_COMMENT = 20;
	
	/** the add a comment event */
	public static final int REMOVE_COMMENT = 21;
	
	/** the add a comment event */
	public static final int CHANGE_COMMENT = 22;
	
	/** the add a reference link event */
	public static final int ADD_REFERENCE_LINK = 23;
	
	/** the remove a reference link event */
	public static final int REMOVE_REFERENCE_LINK = 24;
	
	/** the change a reference link event */
	public static final int CHANGE_REFERENCE_LINK = 25;
	
	/** the add a reference link set event */
	public static final int ADD_REFERENCE_LINK_SET = 26;
	
	/** the remove a reference link set event */
	public static final int REMOVE_REFERENCE_LINK_SET = 27;
	
    // members
    private int operation;
    private Object modification;

    // constructor
    public ACMEditEvent(Object invalidatedObject, int theOperation,
        Object theModification) {
        super(invalidatedObject);

        operation = theOperation;
        modification = theModification;
    }

    // methods

    /**
     * Returns the Object that is invalidated by the ACM edit operation.
     *
     * @return DOCUMENT ME!
     */
    public Object getInvalidatedObject() {
        return getSource();
    }

    /**
     * Return an integer constant indicating the nature of the ACM edit
     * operation. These constants are defined by this class itself.
     *
     * @return DOCUMENT ME!
     */
    public int getOperation() {
        return operation;
    }

    /**
     * Returns the object that modifies the invalidated Object. Example: in
     * case of adding an annotation to a tier, the tier is invalidated, and
     * the modification is the added annotation.
     *
     * @return DOCUMENT ME!
     */
    public Object getModification() {
        return modification;
    }
}

package mpi.dcr.isocat;

/**
 * Some ISOCat DCR constants.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public interface ISOCatConstants {
    /**
     * as of mid 2009 there is a new dcr model and the pid's have now a prefix
     */
    public static final String PID_PREFIX = "http://www.isocat.org/datcat/DC-";

    /** the base url of the REST service */
    //public static final String BASE_URL = "http://www.isocat.org/rest/";
    public static final String BASE_URL = "http://www.datcatinfo.net/rest/";
    
    /** the data category selection element */
    public static final String DCS = "dcif:dataCategorySelection";

    /** the data category element */
    public static final String DC = "dcif:dataCategory";

    /** profile element */
    public static final String PROF = "dcif:profile";

    /** the pid attribute */
    public static final String PID = "pid";

    /** id attribute */
    public static final String ID_ATT = "identifier";

    /** name attribute */
    public static final String NAME_ATT = "name";

    /** definition attribute */
    public static final String DEF_ATT = "definition";

    /** the isA element */
    public static final String IS_A = "isA";

    /** the identifier element */
    public static final String ID = "dcif:identifier";

    /** the name element */
    public static final String NAME = "dcif:name";

    /** the definition element */
    public static final String DEF = "dcif:definition";

    /** the language element */
    public static final String LANG = "dcif:language";

    /** the language section element */
    public static final String LANG_SEC = "dcif:languageSection";
}

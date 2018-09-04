package mpi.eudico.server.corpora.clomimpl.flex;

/**
 * Constants for parsing Flex files
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public interface FlexConstants {
    /** the element document */
    public static final String DOC = "document";
    
    /** the element interlinear-text */
    public static final String IT = "interlinear-text";

    /** the element paragraph */
    public static final String PARAGR = "paragraph";   

    /** the element phrase */
    public static final String PHRASE = "phrase";
 
    /** the element word */
    public static final String WORD = "word";    

    /** the element morph */
    public static final String MORPH = "morph";

    /** the element item */
    public static final String ITEM = "item";

    /** the attribute type */
    public static final String TYPE = "type";

    /** the attribute lang */
    public static final String LANG = "lang";
    
    /** the attribute font */
    public static final String FONT = "font";
    
    /** the attribute vernacular */
    public static final String VERNACULAR = "vernacular";

    /** the element language */
    public static final String LANGUAGE = "language";
    
    /** the element media */
    public static final String MEDIA = "media";
        
    /** the element languages */
    public static final String LANGUAGES = "languages";

    /** the attribute txt */
    public static final String TXT = "txt";    
    
    /** the attribute location */
    public static final String LOCATION = "location";    
    
    /** the attribute punct */
    public static final String PUNCT = "punct";
    
    /** the attribute guid */
    public static final String GUID = "guid";
   
    /** the attribute begin time */
    public static final String BEGIN_TIME = "begin-time-offset";
    
    /** the attribute end time */
    public static final String END_TIME = "end-time-offset";
    
    /** the attribute speaker */
    public static final String SPEAKER = "speaker";
    
    public static final String FLEX_GUID_ANN_PREFIX = "_flexid_";   
    
    public static final String PUNCT_ISOCAT_URL = "http://www.isocat.org/rest/dc/1372";
    
    public static final String[] DEFINED_TYPES = {"txt", "cf", "hn", "variantTypes",
    	"gls", "lit", "msa", "pos", "title", "title-abbreviation", "source", "comment",
    	"text-is-translation","description"};
}

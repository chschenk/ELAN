package mpi.eudico.client.annotator.md.imdi;

/**
 * A limited set of IMDI constants.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public interface ImdiConstants {
    /** property METATRANSCRIPT */
    public static final String METATRANSCRIPT = "METATRANSCRIPT";

    /** property MDGroup */
    public static final String MDGROUP = "MDGroup";

    /** property SESSION */
    public static final String SESSION = "Session";
    
    /** property PROJECT */
    public static final String PROJECT = "Project";

    /** property KEYS */
    public static final String KEYS = "Keys";

    /** property KEY */
    public static final String KEY = "Key";

    /** property CONTENT */
    public static final String CONTENT = "Content";

    /** property ACTORS */
    public static final String ACTORS = "Actors";

    /** property ACTOR */
    public static final String ACTOR = "Actor";

    /** property RESOURCES */
    public static final String RESOURCES = "Resources";

    /** moets important container elements */
    public static final String[] CONTAINERS = new String[] {
            METATRANSCRIPT, MDGROUP, SESSION, KEYS, CONTENT, ACTORS, RESOURCES
        };
}

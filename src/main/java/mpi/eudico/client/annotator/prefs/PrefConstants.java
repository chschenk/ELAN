/**
 * 
 */
package mpi.eudico.client.annotator.prefs;

/**
 * Interface defining some constants for reading and writing of preferences xml files.
 * 
 * @author Han SLoetjes
 */
public interface PrefConstants {
	// elements
	public static final String PREF = "pref";
	public static final String PREF_GROUP = "prefGroup";
	public static final String PREF_LIST = "prefList";
	public static final String BOOLEAN = "Boolean";
	public static final String INT = "Int";
	public static final String LONG = "Long";
	public static final String FLOAT = "Float";
	public static final String DOUBLE = "Double";
	public static final String STRING = "String";
	public static final String OBJECT = "Object";
	// attributes
	public static final String KEY_ATTR = "key";
	public static final String CLASS_ATTR = "class";
	public static final String VERS_ATTR = "version";
}

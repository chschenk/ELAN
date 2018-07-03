package mpi.eudico.client.annotator.prefs;

/**
 * A Pref object representing a Java Object. The className field specifies the
 * fully qualified Java class name this object represents. The value is a
 * String  reprresentation of the object's value (e.g. class
 * name=java.awt.Color,  value="120,42,250").
 *
 * @author Han Sloetjes, MPI
 */
public class ObjectPref {
    private String className;
    private String value;
    private Object object;

    /**
     * Creates an instance with null values.
     */
    public ObjectPref() {
    }

    /**
     * Creates an instance from the specified class name and value objects.
     *
     * @param className the fully qualified Java class name
     * @param value the object's value as a String
     */
    public ObjectPref(String className, String value) {
        this.className = className;
        this.value = value;
    }

    /**
     * Returns the fully qualified class name
     *
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the fully qualified class name
     *
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Returns the string representation of the object
     *
     * @return the value as string
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the string representation of the object
     *
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the real object
     *
     * @return the object
     */
    public Object getObject() {
        return object;
    }

    /**
     * Sets the Object object
     *
     * @param object the object to set
     */
    public void setObject(Object object) {
        this.object = object;
    }
}

package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.Property;


/**
 * Simple implementation of a property class. 
 * This class stores a property that <strong>can</strong> have a name
 * and <strong>can</strong> have a value.
 * <p>
 * The name is immutable and can be specified only when constructing.
 * 
 * @author Han Sloetjes, MPI
 */
public class PropertyImpl implements Property {
    private String name;
    private Object value;

    /**
     * Create a property with the given name and value.
     * 
     * @param name
     * @param value
     */
    public PropertyImpl(String name, Object value) {
    	this.name = name;
    	this.value = value;
    }
    
    /**
     * Create a property with the name and value of the given property.
     */
    public PropertyImpl(Property orig) {
    	this.name = orig.getName();
    	this.value = orig.getValue();
    }
    
    /**
     * @see mpi.eudico.server.corpora.clom.Property#getName()
     */
    @Override
	public String getName() {
        return name;
    }

    /**
     * @see mpi.eudico.server.corpora.clom.Property#getValue()
     */
    @Override
	public Object getValue() {
        return value;
    }

    /**
     * @see mpi.eudico.server.corpora.clom.Property#setValue(java.lang.Object)
     */
    @Override
	public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Returns the name and value separated by a colon if both are not null. If
     * name is null value.toString is returned, if value is null the name is
     * returned. If both are null an empty String is returned.
     *
     * @return a string representation of the property
     */
    @Override
	public String toString() {
        if (name == null) {
            if (value == null) {
                return "";
            } else {
                return value.toString();
            }
        } else if (value == null) {
            return name;
        }

        return name + ": " + value.toString();
    }
}

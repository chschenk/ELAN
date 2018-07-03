package mpi.eudico.client.util;

/**
 * An object that encapsulates an Object, typically a String, and that is used to identify 
 * a "sub header" table cell in a JTable.
 */
public class TableSubHeaderObject {
    private Object content;
    
    /**
     * Creates a new TableSubHeaderObject instance.
     * 
     * @param content the content of this object
     */
    public TableSubHeaderObject(Object content) {
        super();
        this.content = content;
    }
    
    /**
     * Returns the content of this object.
     * 
     * @return Returns the content.
     */
    public Object getContent() {
        return content;
    }
    
    /**
     * Sets the content.
     * 
     * @param content The content to set.
     */
    public void setContent(Object content) {
        this.content = content;
    }
    
    /**
     * Returns the content as a String.
     * 
     * @return the content as a String
     */
    @Override
	public String toString() {
        if (content != null) {
            return content.toString();
        } else {
            return "";
        }
    }
    
}

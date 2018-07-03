package mpi.eudico.client.util;


public class SelectableObject<T> {
    private T value;
    private boolean selected;
    
    public SelectableObject() {
    }
    
    public SelectableObject(T value, boolean selected) {
        this.value = value;
        this.selected = selected;
    }
    
    @Override
	public String toString() {
        if (value != null) {
            return value.toString();    
        }
        return null;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public T getValue() {
        return value;
    }
    
    public void setValue(T value) {
        this.value = value;
    }
}

package mpi.eudico.server.corpora.clomimpl.flex;

import java.util.ArrayList;
import java.util.List;


/**
 * Record that can be used for any type of Flex container element, i.e. any
 * element other than "item"
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ContainerElem {
    /** the type of container, e.g. paragraph, phrase etc. */
    public String flexType;
   
    /** use the "guid" attribute of some elements as the annotation id */
    public String id;

    /** a reference to the parent, for traversing upwards */
    public ContainerElem parent;
    public long bt = -1;
    public long et = -1;    
    public String speaker = null;    
    private List<Item> items;
    private List<ContainerElem> childElems;

    /**
     * Creates a new container of the specified type.
     *
     * @param flexType
     */
    public ContainerElem(String flexType) {
        super();
        this.flexType = flexType;
    }

    /**
     * Adds an item that will eventually be converted to an annotation. Items
     * of type "txt" will always be the first in the list and will be the
     * parent of the other items.
     *
     * @param item the item to add
     */
    public void addItem(Item item) {
        if (items == null) {
            items = new ArrayList<Item>(10);
        }
        // check if there is already an item with the same type and lang attribute, 
        // append value if so  
        for (Item i : items) {
        	if (i.type.equals(item.type) && ((i.lang == null && item.lang == null) || (i.lang != null && i.lang.equals(item.lang)))) {
        		if (i.value != null) {
        			if (item.value != null) {
        				i.value += (" | " + item.value);
        			}
        		} else {
        			i.value = item.value;
        		}
        		return;
        	}
        }
        
//        if (item.type == FlexConstants.TXT) {
//        	// if there is already an "txt" item in the list, then add at the end
//        	if (items.size() > 0 && items.get(0).type == FlexConstants.TXT) {
//        		items.add(item);
//        	} else {
//        		items.add(0, item);
//        	}
//        } else {
            items.add(item);
//        }
    }

    /**
     * Returns the items, or null.
     *
     * @return the items
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Adds a child container element to the list of children.
     *
     * @param elem the container to add
     */
    public void addElement(ContainerElem elem) {
        if (childElems == null) {
            childElems = new ArrayList<ContainerElem>();
        }

        elem.parent = this;
        childElems.add(elem);
    }

    /**
     * Returns the list of child elements, or null.
     * 
     * @return the child elements or null
     */
	public List<ContainerElem> getChildElems() {
		return childElems;
	}
    
}

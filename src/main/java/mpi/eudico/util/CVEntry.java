package mpi.eudico.util;

import java.awt.Color;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import mpi.eudico.server.corpora.clom.ExternalReference;


/**
 * $Id: CVEntry.java 20115 2010-09-29 12:34:59Z wilelb $
 * An entry in a ContolledVocabulary.<br>
 * An entry has a value and an optional description.  Pending: the entries'
 * value in a controlled vocabulary should be unique. We could override the
 * equals(o) method of <code>Object</code> to return
 * this.value.equals(((CVEntry)o).getValue()). This however would not be
 * consistent with hashCode().
 *
 */
@SuppressWarnings("serial")
public class CVEntry implements Comparable<CVEntry>, Serializable {
    private ValueDesc[] contents;
    /** field for reference to an external concept or entity, like a Data Category */
    private ExternalReference externalRef;
    private int shortcutKeyCode = -1;
    private Color prefColor;
    protected BasicControlledVocabulary parent;
	private String id;

    private void init(BasicControlledVocabulary parent)
    {
    	this.parent = parent;
    	int numLangs = parent.getNumberOfLanguages();
    	this.contents = new ValueDesc[numLangs];
    	this.id = "";
    	
    	for (int i = 0; i < numLangs; i++) {
    		this.contents[i] = new ValueDesc("", null);
    	}
    }
    
    public CVEntry(BasicControlledVocabulary parent) {
    	init(parent);
    }
    
    /**
     * Creates a new entry with the specified value.
     *
     * @param value the value
     *
     * @see #CVEntry(String,String)
     */
//    @Deprecated
//    public CVEntry(BasicControlledVocabulary parent, String value) {
//        this(parent, value, null);
//    }

    /**
     * Creates a new entry with the specified value and the specified
     * description.
     *
     * @param value the value
     * @param description the description
     *
     */
    @Deprecated
    public CVEntry(BasicControlledVocabulary parent, String value, String description) {
    	this(parent, parent.getDefaultLanguageIndex(), value, description);
    }
    
    /**
     * Creates a new entry with the specified value and the specified
     * description, which are in the specified language (index).
     *
     * @param value the value
     * @param description the description
     *
     */
    public CVEntry(BasicControlledVocabulary parent, int index, String value, String description) {
        if (value == null) {
            throw new IllegalArgumentException("The value can not be null.");
        }

    	init(parent);
    	ensureCapacity(index + 1);
        this.contents[index] = new ValueDesc(value, description);
    }
    
    /**
     * Creates a copy of the specified entry.
     * 
     * @param origEntry the entry to copy
     */
    public CVEntry(CVEntry origEntry) {
    	this(origEntry.getParent(), origEntry);
	}

    /**
     * Creates a deep copy of the specified entry.
     * 
     * @param newParent the new Vocabulary that is going to contain the Entry.
     *        (it needs to have the same languages...)
     * @param origEntry the entry to copy
     */
    public CVEntry(BasicControlledVocabulary newParent, CVEntry origEntry) {
        if (origEntry == null) {
            throw new IllegalArgumentException("The CVEntry origEntry can not be null.");
        }

        this.parent = newParent;
        final int numLangs = origEntry.contents.length;
		this.contents = new ValueDesc[numLangs];
		for (int i = 0; i < numLangs; i++) {
			this.contents[i] = origEntry.contents[i].clone();
		}
        this.externalRef = origEntry.getExternalRef();
        this.prefColor = origEntry.getPrefColor();
        this.shortcutKeyCode = origEntry.getShortcutKeyCode();
        this.id = "";
        
        this.setId(origEntry.id);        
	}

    /**
     * Creates a copy of the specified entry.
     * For language index, replace the value and description with new values.
     * The new entry is suitable as a replacement for the original one.
     * 
     * @param origEntry the entry to copy
     */
    public CVEntry(CVEntry origEntry, int index, String value, String description) {
    	this(origEntry.getParent(), origEntry);
    	
    	ensureCapacity(index + 1);
    	contents[index] = new ValueDesc(value, description);
    }
    
    /**
     * Create a new entry, based on this one, which copies all the non-language
     * related values. This can be used for re-shuffling the languages in
     * an entry.
     * @param newParent
     * @return
     */
    public CVEntry cloneExceptValues(BasicControlledVocabulary newParent) {
		CVEntry newEntry = new CVEntry(newParent);
		
		newEntry.setId(getId());
		newEntry.setExternalRef(getExternalRef());
		newEntry.setPrefColor(getPrefColor());
		newEntry.setShortcutKeyCode(getShortcutKeyCode());
    
		return newEntry;
    }
    
    /**
	 * @return the parent
	 */
	public BasicControlledVocabulary getParent() {
		return parent;
	}

    private void ensureCapacity(int size)
    {
    	assert(size <= parent.getNumberOfLanguages());
    	
    	if (size <= contents.length)
    		return;
    	ValueDesc[] newContents = Arrays.copyOf(contents, size);
    	// Fill in the new slots with dummy values
    	for (int i = contents.length; i < size; i++) {
    		newContents[i] = new ValueDesc("", null);
    	}
    	contents = newContents;
    }
    
    /**
     * Returns the value of the given language.
     *
     * @param index the language index
     * @return the value
     */
    public String getValue(int index) {
    	if (index < contents.length)
    		return contents[index].getValue();
    	return "";
    }

    /**
     * Sets the value for the given language.
     *
     * @param index the language index
     * @param s the value
     */
    public void setValue(int index, String s) {
    	ensureCapacity(index + 1);
    	contents[index].setValue(s);
    }
    
    /**
     * Returns the default value.
     *
     * @return the value
     */
    @Deprecated
    public String getValue() {
        return getValue(parent.getDefaultLanguageIndex());
    }

    /**
     * Sets the default value.
     *
     * @return the value
     */
    @Deprecated
    public void setValue(String s) {
    	setValue(parent.getDefaultLanguageIndex(), s);
    }
    
    /**
     * Returns a description.
     * 
     * @param index The index obtained from BasicControlledVocabulary.getIndexOfLanguage(...)
     * @return the description or null
     */
    public String getDescription(int index) {
    	if (index < contents.length)
    		return contents[index].getDescription();
    	return null;
    }

	/**
     * Sets a description of this entry.
     *
     * @param index The index obtained from BasicControlledVocabulary.getIndexOfLanguage(...)
     * @param description the description
     */
    public void setDescription(int index, String description) {
    	ensureCapacity(index + 1);
    	contents[index].setDescription(description);
    }

    /**
     * Returns the default description.
     *
     * @return the description or null
     */
    @Deprecated
    public String getDescription() {
    	return getDescription(parent.getDefaultLanguageIndex());
    }

	/**
     * Sets the default description of this entry.
     *
     * @param description the description
     */
    @Deprecated
    public void setDescription(String description) {
    	setDescription(parent.getDefaultLanguageIndex(), description);
    }


    /**
     * Since the CVs have been made multi-language, the entries have an ID.
     * This used to be the case only for ExternalCVEntry-s.
     * All ids are unique (within each CV) so when you set it, it might
     * be changed if it isn't unique.
     * @return
     */
	public String getId() {
		return this.id;
	}
	
	/**
	 * See {@link #getId()}.
	 */
	public void setId(String id) {
		if (id == null) {
			id = "";
		}
		if (!this.id.isEmpty()) {
			parent.removeId(this);
		}
		this.id = id;
		parent.addId(this);
	}
	
	/**
	 * To be called from {@link BasicControlledVocabulary#ensureIdIsUnique(CVEntry)}
	 * to change the id in case it wasn't unique. It should not call back there.
	 * @param id
	 */
	protected void internalSetId(String id) {
		this.id = id;
	}
	
   /**
     * Returns the reference to an externally defined concept or entity.
     * 
	 * @return the externalRef the reference to an external concept or entity
	 */
	public ExternalReference getExternalRef() {
		return externalRef;
	}

	/**
	 * Sets the reference to an externally defined concept or entity.
	 * 
	 * @param externalRef the reference to an external concept or entity
	 */
	public void setExternalRef(ExternalReference externalRef) {
		this.externalRef = externalRef;
	}

	/**
	 * Returns the shortcut key to use to select this entry value.
	 * 
	 * @return the shortcut key code
	 */
	public int getShortcutKeyCode() {
		return shortcutKeyCode;
	}

	/**
	 * Sets the shortcut key to use to select this entry value.
	 * 
	 * @param shortcutKeyCode the new key code
	 */
	public void setShortcutKeyCode(int shortcutKeyCode) {
		this.shortcutKeyCode = shortcutKeyCode;
	}

	/**
	 * Returns the preferred color for display in viewer components.
	 * 
	 * @return the preferred color, can be null
	 */
	public Color getPrefColor() {
		return prefColor;
	}

	/**
	 * Sets the preferred color for this entry.
	 * 
	 * @param prefColor the preferred color
	 */
	public void setPrefColor(Color prefColor) {
		this.prefColor = prefColor;
	}

	/**
     * Implementation of the comparable interface.
     * Comparison does not include the external reference, if any.
     * In order the different languages are compared.
     *
     * @param other the object this class is compared to
     * @return compareTo of 'value's, or, if they are equal, compareTo of 'description's
     */
    @Override
	public int compareTo(CVEntry other) {
    	
    	// Check reference equality first: that is easy.
    	if (this == other) {
    		return 0;
    	}
    	
    	int upb = Math.min(contents.length, other.contents.length);
    	
    	for (int i = 0; i < upb; i++) {
    		ValueDesc vd1 = contents[i];
    		ValueDesc vd2 = other.contents[i];
    		
    		int compare = vd1.compareTo(vd2);
    		if (compare != 0) {
    			return compare;
    		}
    	}
    	
    	int compare = contents.length - other.contents.length;

        return compare;
    }

    /**
     * Overrides <code>Object</code>'s equals method by checking if values and
     * descriptions of the two objects are equal, and the external reference too.
     *
     * Note, that also subclasses of this class might be equal to this class!!!
     *
     * @param obj the reference object with which to compare
     *
     * @return true if this object is the same as the obj argument; false
     *         otherwise
     */
    @Override
	public boolean equals(Object obj) {
        if (!(obj instanceof CVEntry)) {
            return false;
        }

        // check the fields
        CVEntry other = (CVEntry) obj;
        
        // Reference equality implies value equality
        if (this == other) {
        	return true;
        }

        if (this.compareTo(other) == 0) {
            if (externalRef == null) {
            	return externalRef == other.getExternalRef();
            } else  {
            	return externalRef.equals(other.getExternalRef());
            }
        }

        return false;
    }

    /**
     * returns hashCode of 'value'.
     *
     * (note that it is not necessary to return different hashcodes if objects are not equal;
     * including the field 'description' would cause problems since it is mutable)
     *
     * @return hashCode
     */
    @Override
	public int hashCode() {
        return contents[0].value.hashCode();
    }

    /**
     * Overrides <code>Object</code>'s toString() method to just return  the
     * value of this entry.<br>
     * This way this object can easily be used directly in Lists, ComboBoxes
     * etc.
     *
     * @return the value
     */
    @Override
	public String toString() {
    	return getValue(parent.getDefaultLanguageIndex());
//    	int index = parent.getDefaultLanguageIndex();
//    	String l = parent.getLanguageId(index);
//    	String v = getValue(index);
//    	return l + ":" + v;
    }
    
    /**
     * A local class to store the combination of Value and Description.
     * Is Comparable<> and Cloneable.
     * 
     * @author olasei
     */
    private static class ValueDesc implements Comparable<ValueDesc>, Cloneable {
    	private String value;
		private String description;
    	    	
		ValueDesc(String value, String description) {
    		this.value = value;
    		this.description = description;
    	}
		
		@Override
		public ValueDesc clone() {
			try {
				return (ValueDesc)super.clone();
			} catch (CloneNotSupportedException e) {
				// Can't happen
				e.printStackTrace();
				return new ValueDesc(value, description);
			}
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

    	public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		/**
		 * compareTo of 'value's, or, if they are equal, compareTo of 'description's
		 */
		@Override
		public int compareTo(ValueDesc o) {
			int compare = value.compareTo(o.value);
			if (compare != 0)
				return compare;
			String d1 = description == null ? "" : description;
			String d2 = o.description == null ? "" : o.description;
			return d1.compareTo(d2);
		}
    }
    
    /**
     * A Comparator that selects and compares the ValueDesc of one of the languages.
     * Assumes the CVEntries to compare are from the same BasicControlledVocabulary.
     * @author olasei
     */
    public static class CVELangComparator implements Comparator<CVEntry> {
    	private int index;
    	
    	CVELangComparator(int index) {
    		this.index = index;
    	}
    	
		@Override
		public int compare(CVEntry lhs, CVEntry rhs) {
			return lhs.contents[index].compareTo(rhs.contents[index]);
		}
    }

    /**
     * Remove the value and description with the given index.
     * If the index isn't in the range of contained values,
     * no work is needed. It is assumed the relevant word
     * was never added to this entry.
     * @param index
     */
	protected void removeLanguage(int index) {
		int newSize = contents.length - 1;
		if (index >= 0 && index <= newSize) {
			ValueDesc newContents[] = new ValueDesc[newSize];

			/*
			 * Copy all elements except number 'index'.
			 */
			if (index > 0) {
				System.arraycopy(contents, 0, newContents, 0, index);
			}
			if (index < newSize) {
				System.arraycopy(contents, index + 1, newContents, index, newSize - index);			
			}

			contents = newContents;
		}
	}
}

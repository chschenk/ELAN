package mpi.eudico.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.prefs.RecentLanguages;
import mpi.eudico.util.multilangcv.LangInfo;

/**
 * A ControlledVocabulary holds a restricted list of entries.<br>
 * The entries should be unique in the sence that the value of the entries
 * must be unique.  Pending: we are using a List now and take care ourselves
 * that all elements are unique. Could use some kind of Set when we would
 * decide to let CVEntry override the equals() method. Pending: should the
 * entries always be sorted (alphabetically)?  <b>Note:</b> this class is not
 * thread-safe.
 *
 * This class has no undo/redo - functionality!
 * 
 * There are many @Deprecated methods in this class. They are the non-
 * multi-language aware methods, and they should be removed as soon as possible.
 *
 * @author Han Sloejes, Alex Klassmann, Olaf Seibert
 * $Id: BasicControlledVocabulary.java 20115 2010-09-29 12:34:59Z wilelb $
 */
public class BasicControlledVocabulary implements Iterable<CVEntry>, Cloneable {
    /** constant for the move-to-top edit type */
    public static final int MOVE_TO_TOP = 0;

    /** constant for the move-up edit type */
    public static final int MOVE_UP = 1;

    /** constant for the move-down edit type */
    public static final int MOVE_DOWN = 2;

    /** constant for the move-to-bottom edit type */
    public static final int MOVE_TO_BOTTOM = 3;
    
    /** Ids/label for the initial language, when we don't know what it is yet. */
    public static final String DEFAULT_LANGUAGE_ID = "und";
    public static final String DEFAULT_LANGUAGE_DEF = "http://cdb.iso.org/lg/CDB-00130975-001";
    public static final String DEFAULT_LANGUAGE_LABEL = "undetermined (und)";
    
    protected List<CVEntry> entries;
    private String name;
    protected boolean initMode;
    private List<CVLangInfo> languages;
    private int defaultLanguageIndex;
    private int numberOfLanguages;    
    private String preferenceLanguage;
    protected Map<String, CVEntry> idToEntry;
    
    private static final CVLangInfo defaultLangInfo = new CVLangInfo(DEFAULT_LANGUAGE_ID, DEFAULT_LANGUAGE_DEF, DEFAULT_LANGUAGE_LABEL, "");

    /**
     * Creates a CV with the specified name and description and the specified description.
     *
     * @param name the name of the CV
     * @param description the description of the CV
     *
     * throws IllegalArgumentException when the name is <code>null</code>
     *            or of length 0
     */
    public BasicControlledVocabulary(String name, String description) {
        if ((name == null) || (name.length() == 0)) {
            throw new IllegalArgumentException(
                "The name can not be null or empty.");
        }

        this.name = name;
        this.defaultLanguageIndex = 0;
        this.numberOfLanguages = 1;

        entries = new ArrayList<CVEntry>();
        languages = new ArrayList<CVLangInfo>(2);
        idToEntry = new HashMap<String, CVEntry>();
        // Add the default language, which is a special case
        addLanguage(new CVLangInfo(defaultLangInfo));
        setDescription(0, description);
    }

    /**
     * Creates a CV with the specified name and empty description.
     *
     * @param name the name of the CV
     *
     * throws IllegalArgumentException when the name is <code>null</code>
     *         or of length 0
     */
    public BasicControlledVocabulary(String name) {
        this(name, "");
    }
    
    /**
     * Copy constructor
     * 
     * @param orig
     */
    public BasicControlledVocabulary(BasicControlledVocabulary orig) {
        clone(orig);
    }

    /**
     * This function makes a copy of the languages and descriptions of
     * another vocabulary, overriding the ones possibly already set.
     * The CVEntries are cleared.
     */
    public void cloneStructure(BasicControlledVocabulary orig) {   	
    	setName(orig.getName());
    	entries.clear();
    	languages.clear();
    	numberOfLanguages = 0;
    	
    	int nLangs = orig.getNumberOfLanguages();
    	for (int i = 0; i < nLangs; i++) {
    		CVLangInfo li = new CVLangInfo(orig.languages.get(i));
    		addLanguage(li);
    	}
    }
    
    /**
     * This function clones all CVEntries from an original vocabulary.
     * This CV will contain copies of the entries.
     * <p>
     * This assumes that the languages match.
     * 
     * @param orig CV to clone the entries from
     */
    
    public void cloneEntries(BasicControlledVocabulary orig) {
    	initMode = true;
    	entries.clear();
    	for (CVEntry e : orig) {
    		addEntry(new CVEntry(this, e));
    	}
    	initMode = false;
    }
    
    /**
     * Combine cloneStructure() and cloneEntries().
     * 
     * @param orig CV to clone structure and entries from.
     */
    public void clone(BasicControlledVocabulary orig) {
    	cloneStructure(orig);
    	cloneEntries(orig);
    }

    /**
     * An implementation of clone() that uses the copy constructor.
     * (This implies that subclasses must override)
     */
    @Override // Cloneable
	public BasicControlledVocabulary clone() {
    	return new BasicControlledVocabulary(this);
    }
    
    /**
     * Sets the description of this CV for the default language.
     *
     * @param description the new description of the CV
     */
    @Deprecated
    public void setDescription(String description) {
    	setDescription(defaultLanguageIndex, description);
    }

    /**
     * Returns the description of the CV.
     *
     * @return the description of the CV, can be <code>null</code>
     */
    @Deprecated
    public String getDescription() {
        return getDescription(defaultLanguageIndex);
    }

    /**
     * Sets the description of this CV for this language.
     *
     * @param description the new description of the CV
     */
    public void setDescription(int languageIndex, String description) {
    	languages.get(languageIndex).setDescription(description);

        if (!initMode) {
            handleModified();
        }
    }

    /**
     * Returns the label for this language.
     *
     * @return the label of the language, can be <code>null</code>
     */
    public String getLanguageLabel(int languageIndex) {
        return languages.get(languageIndex).getLabel();
    }

    /**
     * Returns the description of the CV for this language.
     *
     * @return the description of the CV, can be <code>null</code>
     */
    public String getDescription(int languageIndex) {
        return languages.get(languageIndex).getDescription();
    }

    /**
     * Returns an array containing all entries in this Vocabulary.
     * <p>
     * This method is rather inefficient because the array it returns is a fresh copy.
     * If possible, use {@link #iterator()} instead.
     *
     * @return an array of entries
     */
    public CVEntry[] getEntries() {
        return entries.toArray(new CVEntry[] {  });
    }

    /**
     * Returns an array containing all entries in this Vocabulary,
     * simplified to a single language.
     * <p>
     * This method is rather inefficient because the array it returns is a fresh copy.
     *
     * @return an array of simple entries
     */
    public SimpleCVEntry[] getSimpleEntries(int langIndex) {
    	int size = entries.size();
    	SimpleCVEntry[] simple = new SimpleCVEntry[size];
    	
    	for (int i = 0; i < size; i++) {
    		simple[i] = new SimpleCVEntry(entries.get(i), langIndex);
    	}
    	
    	return simple;
    }
    
    /**
     * Returns a read-only iterator over the entries.
     * This can't be used if you want to change the collection of CVEntries
     * while iterating over them.
     * Implements the Iterable interface.
     */
	@Override
	public Iterator<CVEntry> iterator() {
		return Collections.unmodifiableList(entries).iterator();
	}

	/**
	 * A cheap way to get the number of CVEntries in the vocabulary.
	 * Much better than {@code getEntries().length}...
	 * @return
	 */
    public int size() {
    	return entries.size();
    }
    
	/**
	 * A cheap check to see if there are entries in the vocabulary.
	 * Much better than {@code getEntries().length == 0}...
	 * @return
	 */
	public boolean isEmpty() {
		return entries.isEmpty();
	}
	
    /**
     * Returns a sorted array of entries. The values are sorted  using the
     * String.compareTo(String) method applied to the language as specified.
     * As a side effect, the internal array of entries is sorted too.
     *
     * @param langIndex the language to sort on
     * @return a sorted array of CVEntry objects
     */
    public CVEntry[] getEntriesSortedByAlphabet(int langIndex) {
        CVEntry[] allEntries = getEntries();
        Arrays.sort(allEntries, new CVEntry.CVELangComparator(langIndex));
        
        entries.clear();
        for(int i=0; i< allEntries.length; i++){
        	entries.add(allEntries[i]);        	
        }
        return allEntries;
    }
    
    /**
     * Returns a sorted array of entries. The values are sorted  using the
     * String.compareTo(String) method applied to the default language.
     *
     * @return a sorted array of CVEntry objects
     */
//    public CVEntry[] getEntriesSortedByAlphabet() {
//    	return getEntriesSortedByAlphabet(defaultLanguageIndex);
//    }
    
    /**
     * Returns a array of reverse sorted entries.
     * The values are sorted using the CVEntry.ValueDesc.compareTo() method,
     * applied to the language as specified.
     * As a side effect, the internal array of entries is sorted too.
     *
     * @param langIndex the language to sort on
     * @return a sorted array of CVEntry objects
     */
    public CVEntry[] getEntriesSortedByReverseAlphabetOrder(int langIndex) {
        CVEntry[] allEntries = getEntries();
        Arrays.sort(allEntries, Collections.reverseOrder(new CVEntry.CVELangComparator(langIndex)));
        
        entries.clear();
        for(int i=0; i< allEntries.length; i++){
        	entries.add(allEntries[i]);        	
        }
        return allEntries;
    }
    
    /**
     * Returns a array of reverse sorted entries.
     * The values are sorted using the CVEntry.ValueDesc.compareTo() method,
     * applied to the language as specified.
     *
     * @return a sorted array of CVEntry objects
     */
//    @Deprecated
//    public CVEntry[] getEntriesSortedByReverseAlphabetOrder() {
//    	return getEntriesSortedByReverseAlphabetOrder(defaultLanguageIndex);
//    }

    /**
     * Returns an array containing the values (Strings) of the entries, ordered alphabetically.<br>
     * This is convenience method to get an ordered view on the entry values
     * in the CV.
     *
     * @return an sorted array of Strings containing the values in this CV
     */
    public String[] getValuesSortedByAlphabet(int langIndex) {
        String[] values = getEntryValues(langIndex);
        Arrays.sort(values);

        return values;
    }

    /**
     * Returns an array containing the values (Strings) of the entries. This is
     * a rather expensive
     * convenience method to get a view on the entry values in the CV.
     * It is better to use {@link #getValuesIterable(int index)}.
     *
     * @return an array of Strings containing the values in this CV
     */
    protected String[] getEntryValues(int langIndex) {
        String[] values = new String[entries.size()];

        for (int i = 0; i < entries.size(); i++) {
            values[i] = entries.get(i).getValue(langIndex);
        }

        return values;
    }

    /**
     * Returns an array containing the values (Strings) of the entries. This is
     * a rather expensive
     * convenience method to get a view on the entry values in the CV.
     * 
     * Replaced by Iterable<String> getEntryIterable(int index).
     *
     * @return an array of Strings containing the values in this CV
     */
//    @Deprecated
//    public String[] getEntryValues() {
//    	return getEntryValues(defaultLanguageIndex);
//    }

    /**
     * Return an Iterable<String> to get at all entry values
     * in a given language.
     */
    public Iterable<String> getValuesIterable(int index) {
    	return new ValuesIterable(index);
    }

    protected class ValuesIterable implements Iterable<String> {
    	private int index;

    	public ValuesIterable(int index) {
    		this.index = index;
    	}
    	@Override
    	public Iterator<String> iterator() {
    		return new ValuesIterator(index);
    	}
    }

    protected class ValuesIterator implements Iterator<String> {
    	private int index;
    	private int pos;

    	public ValuesIterator(int index) {
    		this.index = index;
    		pos = 0;
    	}

    	@Override
    	public boolean hasNext() {
    		return pos < entries.size();
    	}

    	@Override
    	public String next() {
    		return entries.get(pos++).getValue(index);
    	}

    	@Override
    	public void remove() {
    		throw new UnsupportedOperationException();
    	}
    }
    
    /**
     * Returns the CVEntry with the specified value, if present.
     * It looks at the default language.
     *
     * @param value the value of the entry
     *
     * @return the CVEntry with the specified value, or null
     */
    public CVEntry getEntryWithValue(String value) {
    	return getEntryWithValue(defaultLanguageIndex, value);    
    }
    
    /**
     * Returns the CVEntry with the specified value, if present.
     *
     * @param value the value of the entry
     *
     * @return the CVEntry with the specified value, or null
     */
    public CVEntry getEntryWithValue(int languageIndex, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        for (CVEntry entry : entries) {
            //ignore case ?
            if (entry.getValue(languageIndex).equals(value)) {
                return entry;
            }
        }

        return null;
    }

	/**
	 * Returns the entry with the given id.
	 * 
	 * @param entryId
	 * @return the CVEntry or null
	 */
	public CVEntry getEntrybyId(String entryId) {
		if (entryId == null) {
			return null;
		}
		
		return idToEntry.get(entryId);
	}

    /**
     * @param initMode if true, don't call handleModified
     */
    public void setInitMode(boolean initMode) {
        this.initMode = initMode;
    }

    /**
     * Sets the name of this CV.
     *
     * @param name the new name of the CV
     *
     * throws IllegalArgumentException when the name is <code>null</code>
     *            or of length 0
     */
    public void setName(String name) {
        if ((name == null) || (name.length() == 0)) {
            throw new IllegalArgumentException(
                "The name can not be null or empty.");
        }

        this.name = name;

        if (!initMode) {
            handleModified();
        }
    }

    /**
     * Returns the name of the CV.
     *
     * @return the name of this CV
     */
    public String getName() {
        return name;
    }

	/**
     * A shorthand for adding more than one CVEntry at a time.
     *
     * @param entries an array of entries
     */
    public void addAll(CVEntry[] entries) {
        if (entries != null) {
            for (int i = 0; i < entries.length; i++) {
                addEntry(entries[i]);
            }
        }

        if (!initMode) {
            handleModified();
        }
    }

    /**
     * Adds a new CVEntry to the List.
     * Checks for uniqueness of words in their respective languages.
     *
     * @param entry the new entry
     *
     * @return true if the entry was successfully added, false otherwise
     */
    public boolean addEntry(CVEntry entry) {
        if (entry == null) {
            return false;
        }

        assert(entry.getParent() == this);
        
        // Each of the words should be unique in its respective language,
        // or empty.
        for (int i = 0; i < numberOfLanguages; i++) {
        	String le = entry.getValue(i);
        	
        	if (!le.isEmpty() && getEntryWithValue(i, le) != null)
        		return false;
        }
        
        if (entry.getId().isEmpty()) {
        	// Only needed when empty (default): otherwise this has been checked before.
        	ensureIdIsUnique(entry);
        }
        entries.add(entry);

        if (!initMode) {
            handleModified();
        }

        return true;
    }
    
    /**
     * Move a number of CVEntries from another CV to this one.
     * This is not very effcient.
     *  
     * @param entry
     */
//    public void moveAll(Iterable<CVEntry> entries) {
//        if (entries != null) {
//            for (CVEntry entry: entries) {
//                moveEntry(entry);
//            }
//        }
//
//        if (!initMode) {
//            handleModified();
//        }
//    }
    
    /**
     * Move a CVEntry from another CV to this one.
     *  
     * @param entry
     */
//    public void moveEntry(CVEntry entry) {
//    	entry.getParent().removeEntry(entry);
//    	entry.parent = this;
//    	addEntry(entry);
//    }

    /**
	 * @return the defaultLanguageIndex
	 */
	public int getDefaultLanguageIndex() {
		return defaultLanguageIndex;
	}

	/**
	 * @param defaultLanguageIndex the defaultLanguageIndex to set
	 */
	protected void setDefaultLanguageIndex(int defaultLanguageIndex) {
		this.defaultLanguageIndex = defaultLanguageIndex;
	}
	
	/**
	 * Get the language as set by {@link #setPreferenceLanguage(String)}.
	 */
	public String getPreferenceLanguage() {
		return preferenceLanguage;
	}
	
	/**
	 * Set the preferred language in term of an identifier.
	 * If possible, set the default language index to this language.
	 * Maybe the CV doesn't contain this language, but if it will do so
	 * in the future, it will switch to it at that time.
	 * @param language
	 */
	public void setPreferenceLanguage(String language) {
		this.preferenceLanguage = language;
		languagesChanged();
	}

	/**
	 * @return the numberOfLanguages
	 */
	public int getNumberOfLanguages() {
		return numberOfLanguages;
	}

	/**
	 * The number of languages can (for now anyway) be changed
	 * only if there are no Entries in this Vocabulary.
	 * Such a change would cause the Entries to become incorrect.
	 * @param numberOfLanguages the numberOfLanguages to set
	 */
	private void setNumberOfLanguages(int numberOfLanguages) {
		assert (this.numberOfLanguages == numberOfLanguages || entries.isEmpty());
		assert (numberOfLanguages > 0);
		this.numberOfLanguages = numberOfLanguages;
	}

	/**
     * Removes all entries from this ControlledVocabulary.
     */
    public void clear() {
        entries.clear();
        idToEntry.clear();

        if (!initMode) {
            handleModified();
        }
    }

    /**
     * Checks whether the specified CVEntry is in this CV.<br>
     * <b>Note:</b> This only checks for object equality.
     *
     * @param entry the CVEntry
     *
     * @return true if entry is in the CV, false otherwise
     *
     * @see #containsValue(String)
     */
    public boolean contains(CVEntry entry) {
        if (entry == null) {
            return false;
        }

        return entries.contains(entry);
    }

    /**
     * Checks whether there is a CVEntry with the specified value in this
     * CV.<br>
     *
     * @param value the value
     *
     * @return true if there is an entry with this value in the CV, false
     *         otherwise
     *
     * @see #contains(CVEntry)
     */
//    public boolean containsValue(String value) {
//    	return containsValue(defaultLanguageIndex, value);
//    }
    
    /**
     * Checks whether there is a CVEntry with the specified value in this
     * CV.<br>
     *
     * @param index the language index
     * @param value the value
     *
     * @return true if there is an entry with this value in the CV, false
     *         otherwise
     *
     * @see #contains(CVEntry)
     */
    public boolean containsValue(int index, String value) {
        if (value == null) {
            return false;
        }

        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getValue(index).equals(value)) { //ignore case??

                return true;
            }
        }

        return false;
    }

    /**
     * Overrides <code>Object</code>'s equals method by checking all  fields of
     * the other object to be equal to all fields in this  object.
     *
     * @param obj the reference object with which to compare
     *
     * @return true if this object is the same as the obj argument; false
     *         otherwise
     */
    @Override
	public boolean equals(Object obj) {
        if (obj == null) {
            // null is never equal
            return false;
        }

        if (obj == this) {
            // same object reference 
            return true;
        }

        if (!(obj instanceof BasicControlledVocabulary)) {
            // it should be a BasicControlledVocabulary object
            return false;
        }

        // check the fields
        BasicControlledVocabulary other = (BasicControlledVocabulary) obj;

        if (!name.equals(other.getName())) {
            return false;
        }

        // Compare the languages.
        // For now we require the order to be the same!
        // (Otherwise, this must be taken into account as well
        // when comparing CVEntries.
        if (languages != null && !languages.equals(other)) {
            return false;
        }

        // compare cventries, ignoring the order in the list
        boolean entriesEqual = true;

        CVEntry entry;
        CVEntry[] otherEntries = other.getEntries();
loop: 
        for (int i = 0; i < entries.size(); i++) {
            entry = entries.get(i);

            for (CVEntry otherEntry : other) {
                if (entry.equals(otherEntry)) {
                    continue loop;
                }
            }

            // if we get here the cv entries are unequal
            entriesEqual = false;

            break;
        }

        return entriesEqual;
    }

    /**
     * return arbitrary fix number since class is mutable
     * @return hashCode
     */
    @Override
	public int hashCode() {
        return 1;
    }

    /**
     * This is a checked way to change the value of an existing CVEntry.<br>
     * This method (silently) does nothing when the specified entry is not  in
     * this ControlledVocabulary, or when the value already exists in this
     * CV.
     *
     * @param entry the CVEntry
     * @param value the new value for the entry
     */
//    @Deprecated
//    public void modifyEntryValue(CVEntry entry, String value) {
//    	modifyEntryValue(entry, defaultLanguageIndex, value);
//    }
    
    /**
     * This is a checked way to change the value of an existing CVEntry.<br>
     * This method (silently) does nothing when the specified entry is not  in
     * this ControlledVocabulary, or when the value already exists in this
     * CV.
     *
     * @param entry the CVEntry
     * @param value the new value for the entry
     */
    public void modifyEntryValue(CVEntry entry, int langIndex, String value) {
        if ((entry == null) || (value == null)) {
            return;
        }

        if (!entries.contains(entry)) {
            return;
        }

        if (containsValue(langIndex, value)) {
            return;
        }

        // the entry is in the list and the new value is not, 
        // replace the oldEntry with a new one, because of undo-ability.
        // However this means that we need to be very careful to preserve the id,
        // which is supposed to be unique, and to update the index properly.
        String origId = entry.getId();
        CVEntry newEntry = new CVEntry(entry);	// gets some new id
        newEntry.setValue(langIndex, value);	// change the word
        int index = entries.indexOf(entry);
        entries.set(index, newEntry);
        removeId(entry);						// forget the new id
        newEntry.setId(origId);					// set the original id

        if (!initMode) {
            handleModified();
        }
    }

    /**
         * Moves a set of entries up or down in the list of entries of the Vocabulary.
         *
         * @param entryArray the entries to move
     * @param moveType the type of move action, one of MOVE_TO_TOP, MOVE_UP,
     *        MOVE_DOWN or MOVE_TO_BOTTOM
     */
    public void moveEntries(CVEntry[] entryArray, int moveType) {
        switch (moveType) {
        case MOVE_TO_TOP:
            moveToTop(entryArray);

            break;

        case MOVE_UP:
            moveUp(entryArray);

            break;

        case MOVE_DOWN:
            moveDown(entryArray);

            break;

        case MOVE_TO_BOTTOM:
            moveToBottom(entryArray);

            break;

        default:
            break;
        }
    }

    /**
     * Removes a set of entries from the Vocabulary.
     *
     * @param entryArray the entries to remove
     * @return true if action was completed successfully
     */
    public boolean removeEntries(CVEntry[] entryArray) {
        if (entryArray == null) {
            return false;
        }

        boolean removed = false;

        for (CVEntry entry : entryArray) {
            boolean b = entries.remove(entry);
            idToEntry.remove(entry.getId());

            if (b) {
                removed = true;
            }
        }

        if (removed) {
            if (!initMode) {
                handleModified();
            }
        }

        return removed;
    }

    /**
     * Removes an entry from the Vocabulary.
     *
     * @param entry the entry to remove
     * @return true if action was completed successfully
     */
    public boolean removeEntry(CVEntry entry) {
        boolean b = entries.remove(entry);

        idToEntry.remove(entry.getId());
        
        if (b && !initMode) {
            handleModified();
        }

        return b;
    }

    /**
     * Removes the CVEntry with the specified value from the CV, if present.
     *
     * @param value the value to remove
     *
     */
//    @Deprecated
//    public void removeValue(String value) {
//    	removeValue(defaultLanguageIndex, value);
//    }
    
    /**
     * Removes the CVEntry with the specified value from the CV, if present.
     *
     * @param langIndex the language index of the value
     * @param value the value to remove
     *
     */
    public void removeValue(int langIndex, String value) {
        if (value == null) {
            return;
        }

        CVEntry entry = null;
        boolean removed = false;

        for (int i = 0; i < entries.size(); i++) {
            entry = entries.get(i);

            if (entry.getValue(langIndex).equals(value)) { //ignore case ??
                entries.remove(i);
                removed = true;

                break;
            }
        }

        if (removed) {
            idToEntry.remove(entry.getId());
            if (!initMode) {
                handleModified();
            }
        }
    }

    /**
     * Removes all existing CVEntries and adds the specified new entries.
     *
     * @param newEntries the new entries for the CV
     */
    public void replaceAll(CVEntry[] newEntries) {
        if (newEntries == null) {
            return;
        }

        entries.clear();
        idToEntry.clear();

        addAll(newEntries);

        if (!initMode) {
            handleModified();
        }
    }

    /**
     * Replace an entry with another. Transfer the ID from the old to the new one.
     * 
     * We can't be 100% sure that the new entry should really have the same ID as
     * the replaced one (unlike {@see #modifyEntryValue(CVEntry, int, String)})
     * but in all uses so far, this is true.
     * 
     * @param oldEntry the entry to be replaced
     * @param newEntry replacement
     * @return true if action was completed successfully
     */
    public boolean replaceEntry(CVEntry oldEntry, CVEntry newEntry) {
        if ((oldEntry == null) || (newEntry == null)) {
            return false;
        }

        int index = entries.indexOf(oldEntry);

        if (index == -1) {
            return false;
        }
        // Deep in our hearts we expect reference equality here...
        if (entries.get(index) != oldEntry) {
        	// ...
        }

        entries.set(index, newEntry);
        
        // Update id map
        newEntry.internalSetId(oldEntry.getId());
        replaceInIndex(oldEntry, newEntry);
        
        if (!initMode) {
            handleModified();
        }

        return true;
    }
    
    protected void replaceInIndex(CVEntry oldEntry, CVEntry newEntry) {
        idToEntry.remove(oldEntry.getId());
        idToEntry.put(newEntry.getId(), newEntry);
    }

    /**
     * Override Object's toString method to return the name of the CV.
     *
     * @return the name of the CV
     */
    @Override
	public String toString() {
        return name;
    }

    /**
     * Sends a general notification to an interested Object, that this CV has been changed.<br>
     * This method does not specify the kind of modification.
     */
    protected void handleModified() {
    }

    /**
     * Moves the CVEntries in the array to the top of the list.<br>
     * It is assumed that the entries come in ascending order!
     *
     * @param entryArray the array of CVEntry objects
     */
    protected void moveToTop(CVEntry[] entryArray) {
        if ((entryArray == null) || (entryArray.length == 0)) {
            return;
        }

        CVEntry entry = null;
        boolean moved = false;

        for (int i = 0; i < entryArray.length; i++) {
            entry = entryArray[i];

            boolean removed = entries.remove(entry);

            if (removed) {
                moved = true;
                entries.add(i, entry);
            }
        }

        if (moved) {
            if (!initMode) {
                handleModified();
            }
        }
    }

    /**
     * Moves the CVEntries in the array down one position in the list.<br>
     * It is assumed that the entries come in ascending order!
     *
     * @param entryArray the array of CVEntry objects
     */
    private void moveDown(CVEntry[] entryArray) {
        if ((entryArray == null) || (entryArray.length == 0)) {
            return;
        }

        CVEntry entry = null;
        boolean moved = false;
        int curIndex;

        for (int i = entryArray.length - 1; i >= 0; i--) {
            entry = entryArray[i];
            curIndex = entries.indexOf(entry);

            if ((curIndex >= 0) && (curIndex < (entries.size() - 1))) {
                boolean removed = entries.remove(entry);

                if (removed) {
                    moved = true;
                    entries.add(curIndex + 1, entry);
                }
            }
        }

        if (moved) {
            if (!initMode) {
                handleModified();
            }
        }
    }

    /**
     * Moves the CVEntries in the array to the bottom of the list.<br>
     * It is assumed that the entries come in ascending order!
     *
     * @param entryArray the array of CVEntry objects
     */
    private void moveToBottom(CVEntry[] entryArray) {
        if ((entryArray == null) || (entryArray.length == 0)) {
            return;
        }

        CVEntry entry = null;
        boolean moved = false;

        for (int i = 0; i < entryArray.length; i++) {
            entry = entryArray[i];

            boolean removed = entries.remove(entry);

            if (removed) {
                moved = true;
                entries.add(entry);
            }
        }

        if (moved) {
            if (!initMode) {
                handleModified();
            }
        }
    }

    /**
     * Moves the CVEntries in the array up one position in the list.<br>
     * It is assumed that the entries come in ascending order!
     *
     * @param entryArray the array of CVEntry objects
     */
    private void moveUp(CVEntry[] entryArray) {
        if ((entryArray == null) || (entryArray.length == 0)) {
            return;
        }

        CVEntry entry = null;
        boolean moved = false;
        int curIndex;

        for (int i = 0; i < entryArray.length; i++) {
            entry = entryArray[i];
            curIndex = entries.indexOf(entry);

            if (curIndex > 0) {
                boolean removed = entries.remove(entry);

                if (removed) {
                    moved = true;
                    entries.add(curIndex - 1, entry);
                }
            }
        }

        if (moved) {
            if (!initMode) {
                handleModified();
            }
        }
    }
    
    /*
     * Some methods to keep the mapping from ID to Entry up to date.
     */
    private int idcounter = 0;
    
    /**
     * Ensure that every CVEntry has a unique id.
     * If the id has to be changed, it is put into the index.
     * 
     * @return true if this already was the case. If it returns false, the id was changed.
     */
    protected boolean ensureIdIsUnique(CVEntry entry) {
		String id = entry.getId();
		
		if (id == null || id.isEmpty() || idToEntry.containsKey(id)) {
			// We need to give this entry a new id. Try a few.
			for (;;) {
				String newid = "cveid" + String.valueOf(idcounter);
				idcounter++;
				if (!idToEntry.containsKey(newid)) {
					entry.internalSetId(newid);	// version that does not call us back 
					idToEntry.put(newid, entry);
					return false;
				}
			}
		}
		
		return true;
    }
    
    /**
     * Call this method before the id has changed, so it can be removed from the index.
     * @param entry
     */
    protected void removeId(CVEntry entry) {
    	String id = entry.getId();
    	if (id != null) {
    		CVEntry toremove = idToEntry.get(id);
    		if (toremove != entry) {
    			System.err.printf("Entry associated with id '%s' (%s) is not this Entry (%s)!\n", id, toremove, entry.toString());
    		}
    		idToEntry.remove(id);
    	}
    }
    
    /**
     * Call this method after the id has changed, so it can be added to the index.
     * If the id is not unique, it will be changed.
     * @param entry
     */
    protected void addId(CVEntry entry) {
    	if (ensureIdIsUnique(entry)) {
    		idToEntry.put(entry.getId(), entry);
    	}
    }
    
    /**
     * Add a description of a new language.
     * Two ids are given: a short one and a long one.
     * The short id is possibly for human display or as XML id. It must also be unique.
     * The long id is a formal id which may be some kind of persistent identifier.
     * The label is some sort of human name for the language.
     * <p>
     * The first language starts out with special defaults so the first time
     * a language is added, the id strings are used for the first language, and
     * the number of languages is not increased.
     * <p>
     * Adding a language is cheap, since the existing CVEntries are not
     * updated (they simply know to return empty strings for languages
     * beyond what they contain).
     * 
     * @return the index that was assigned to the new language, or -1 on error. 
     */
    public int addLanguage(String id, String longId, String label) {
    	CVLangInfo li = new CVLangInfo(id, longId, label, "");
    	
    	return addLanguage(li);
    }
    
    public int addLanguage(LangInfo li) {
    	CVLangInfo cvli = new CVLangInfo(li);
    	
    	return addLanguage(cvli);
    }
    
    private int addLanguage(CVLangInfo li) {
    	if (!checkIds(-1, li.getId(), li.getLongId())) {
    		return -1;
    	}
    	// Checking this may be a bit overkill: the selection is currently
    	// made from the very same list, without allowing edits.
    	// However, that can change (and has, in the past).
        if (!RecentLanguages.getInstance().canAddLanguage(li)) {
        	return -1;
        }
    	
    	if (languages.size() == 1 && languages.get(0).valueEquals(defaultLangInfo)) {
    		languages.set(0, li);
    	} else {
    		languages.add(li);
    	}
    	
    	if (languages.size() > numberOfLanguages) {
    		setNumberOfLanguages(languages.size());
    	}

        if (!initMode) {
            handleModified();
        }
        RecentLanguages.getInstance().addRecentLanguage(li);
        languagesChanged();

        return languages.size() - 1;
    }
    
    /**
     * Check if a language ID satisfies the limitations.
     * It must be an XML id, but we limit it to just lowercase letters.
     * Also check if the proposed new language IDs are unique.
     * Disregard the name at index oldIndex, since we're renaming that one.
     * @param id
     * @return
     */
    private boolean checkIds(int oldIndex, String id, String longId) {
    	if (!id.matches("^[a-z][a-z0-9]*$")) {
    		return false;
    	}
    	if (longId.isEmpty()) {
    		return false;
    	}
    	
    	if (languages.size() > 0) {
			for (int i = 0; i < numberOfLanguages; i++) {
				if (i != oldIndex) {
			    	CVLangInfo li = languages.get(i);
					if (li.getId().equals(id) || li.getLongId().equals(longId)) {
						return false;
					}
				}
			}
    	}
		return true;
    }

    /**
     * Remove a language.
     * This is fairly expensive, since all existing CVEntries need to be
     * adjusted to remove one of their words.
     * @param index
     */
    public void removeLanguage(int index) {
    	if (index >= 0 && index < languages.size() && languages.size() > 1) {
            // If this was the preferred language, make sure it isn't used any more.
            if (index == defaultLanguageIndex) {
            	setDefaultLanguageIndex(0);
            }

            // Remove from languages
    		languages.remove(index);
    		
    		// Remove words from all entries
    		for (CVEntry e : entries) {
    			e.removeLanguage(index);
    		}
    		
    		numberOfLanguages--;
            if (!initMode) {
                handleModified();
            }
            
            languagesChanged();
    	}
    }
    
    /**
     * Access function to change the ids (and label) of a language.
     * @param index
     * @param id
     * @param longId
	 * @param label
     */
    public boolean setLanguageIds(int index, String id, String longId, String label) {
    	if (!checkIds(index, id, longId)) {
    		return false;
    	}
    	// Preserve the description
    	String description = languages.get(index).getDescription();
    	CVLangInfo newInfo = new CVLangInfo(id, longId, label, description);
        if (!RecentLanguages.getInstance().canAddLanguage(newInfo)) {
        	return false;
        }
    	languages.set(index, newInfo);
        if (!initMode) {
            handleModified();
        }
        RecentLanguages.getInstance().addRecentLanguage(newInfo);
        languagesChanged();
    	return true;
    }
    
    public LangInfo getLangInfo(int index) {
    	return languages.get(index);    	
    }
    
    public String getLanguageId(int index) {
    	return languages.get(index).getId();
    }
    
    public String getLongLanguageId(int index) {
    	return languages.get(index).getLongId();
    }
    
    /**
     * Get the index of a language with the given id (short or long).
     * Access functions on the CVEntry use the index to determine the language efficiently.
     * @param id
     * @return
     */
    public int getIndexOfLanguage(String id) {
    	for (int i = 0; i < languages.size(); i++) {
    		if (languages.get(i).getId().equals(id) ||
    			languages.get(i).getLongId().equals(id)) {
    			return i;
    		}
    	}
    	return -1;
    }

    /**
     * React to changed set of contained languages or changed preference.
     * If the preferred language isn't in this CV, leave it unchanged.
     */
    public void languagesChanged() {
		int index = getIndexOfLanguage(this.preferenceLanguage);
		if (index >= 0) {
			setDefaultLanguageIndex(index);
		}
    }
            
    /**
     * Make use of the LangInfo class to extend it with the one language-dependent
     * field that we need.
     * 
     * @author olasei
     */
	private static class CVLangInfo extends LangInfo {
    	private String description;
    
    	public CVLangInfo(String id, String longId, String label, String description) {
    		super(id, longId, label);
    		this.description = description;
    	}
    	
    	public CVLangInfo(CVLangInfo other) {
    		super(other);
    		this.description = other.description;   		
    	}

    	public CVLangInfo(LangInfo other) {
    		super(other);
    		this.description = "";   		
    	}

    	/**
    	 * Description of the CV in this language.
    	 */
		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
    }
}

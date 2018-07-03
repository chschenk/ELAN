package mpi.eudico.client.annotator.interlinear.edit.lexicon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.mpi.lexan.analyzers.lexicon.LexAtom;
import nl.mpi.lexan.analyzers.lexicon.LexCont;
import nl.mpi.lexan.analyzers.lexicon.LexEntry;
import nl.mpi.lexan.analyzers.lexicon.LexanLexicon;
import nl.mpi.lexan.analyzers.lexicon.LexiconChangeEvent;
import nl.mpi.lexan.analyzers.lexicon.LexiconChangeListener;
import nl.mpi.lexan.analyzers.lexicon.LexiconChangeEvent.LexiconChangeType;
import nl.mpi.lexiconcomponent.events.LexiconEditListener;
import nl.mpi.lexiconcomponent.events.LexiconEvent;
import nl.mpi.lexiconcomponent.events.LexiconEvent.LexiconEventType;
import nl.mpi.lexiconcomponent.impl.EntryImpl;
import nl.mpi.lexiconcomponent.impl.LexiconFields;
import nl.mpi.lexiconcomponent.impl.LexiconImpl;
import nl.mpi.lexiconcomponent.impl.SenseImpl;
import nl.mpi.lexiconcomponent.model.Entry;
import nl.mpi.lexiconcomponent.model.Sense;
import nl.mpi.lexiconcomponent.query.EntryFieldGetter;
import nl.mpi.lexiconcomponent.query.EntryFieldGetterFactory;
import nl.mpi.lexiconcomponent.query.SenseFieldGetter;
import nl.mpi.lexiconcomponent.query.SenseFieldGetterFactory;

/**
 * An implementation of a LexanLexicon which wraps a lexicon-component 
 * Lexicon.
 *  
 * @author Han Sloetjes
 */
public class LexanLexiconImpl implements LexanLexicon, LexiconEditListener {
	//private final static String lexUnit = LexiconFields.ENTRY_LEX_UNIT.getFieldName(); 
	//private final static String entryId = LexiconFields.ENTRY_ID.getFieldName();
	//private final static String senseId = LexiconFields.SENSE_ID.getFieldName();
	//private final static String morphType = LexiconFields.ENTRY_MORPH_TYPE.getFieldName();

	private LexiconImpl sourceLexicon;
	private List<LexiconChangeListener> listeners;
	
	static final List<String> listId = Arrays.asList(new String[] { LexiconFields.ENTRY_ID.getFieldName() });
	static final List<String> listIds = Arrays.asList(new String[] { LexiconFields.ENTRY_ID.getFieldName(), 
			LexiconFields.SENSE_ID.getFieldName() });
	
	/**
	 * Constructor.
	 * 
	 * @param sourceLexicon the enclosed lexicon 
	 */
	public LexanLexiconImpl(LexiconImpl sourceLexicon) {
		this.sourceLexicon = sourceLexicon;
		// de-registration as listener performed by the host context
		// this.sourceLexicon.addLexiconEditListener(this);
	}

	@Override
	public String getName() {
		if (sourceLexicon != null) {
			return sourceLexicon.getHeader().getName();
		}
		return "Unknown";
	}
	
	/**
	 * Forwards the call the encapsulated lexicon-component Lexicon
	 */
	@Override
	public List<String> getEntryFieldNames() {
		if (sourceLexicon != null) {
			return sourceLexicon.getEntryFieldNames();
		}
		return null;
	}

	/**
	 * Performs a basic, minimal conversion of a lexicon-component Entry to a Lexan API LexEntry.
	 * Some fields/attributes are always included: the lexical-unit, the id of the Entry, 
	 * the morph-type. Maybe order should always be added as well?
	 * 
	 * @param entry the Lexicon Components entry, not null
	 * @return a Lexan API entry with a limited set of LexItems, a minimal, default set
	 */
	private LexanEntry toLexanEntry(EntryImpl entry) {
		LexanEntry lexanEntry = new LexanEntry(LexiconFields.ENTRY_LEX_UNIT.getFieldName(), 
				entry.getLexicalUnit());
		String id = entry.getId();
		lexanEntry.addLexItem(new LexAtom(LexiconFields.ENTRY_ID.getFieldName(), id));
		lexanEntry.setId(id);
		
		if (entry.getMorphType() != null) {
			lexanEntry.addLexItem(new LexAtom(LexiconFields.ENTRY_MORPH_TYPE.getFieldName(), 
					entry.getMorphType()));
		}

		return lexanEntry;
	}
	
	/**
	 * First creates a minimal LexEntry and then adds the specified field.
	 * 
	 * @param entry the Lexicon Components entry, not null
	 * @param fieldId the field to include in the Lexan API entry
	 * TODO null could be interpreted to add all available fields? Or the keyword "all"?
	 *  
	 * @return a Lexan API entry with a limited set of LexItems, a minimal, 
	 * default set plus the requested field
	 */
	private LexEntry toLexanEntry(EntryImpl entry, String fieldId) {
		// first create a minimal LexEntry
		LexanEntry lexanEntry = toLexanEntry(entry);
		
		// check if the requested field isn't already there because it was added by default
		if (lexanEntry.getLexItem(fieldId) == null) {
			// TODO if the field is a subelement or attribute of Sense or if there are multiple
			// instances of the field, enclose them in a LexCont object?
			
			EntryFieldGetter efg = EntryFieldGetterFactory.createGetter(fieldId);
			for (String fieldvalue : efg.getValues(entry)) {
				lexanEntry.addLexItem(new LexAtom(fieldId, fieldvalue));
			}
		}
		
		return lexanEntry;
	}
	
	private List<EntryFieldGetter> createGetters(List<String> fieldIds) {
		List<EntryFieldGetter> efgs = new ArrayList<EntryFieldGetter>(fieldIds.size());
		
		for (String f : fieldIds) {
			efgs.add(EntryFieldGetterFactory.createGetter(f));
		}
		
		return efgs;
	}
	
	/**
	 * Creates a Lexan LexEntry variant of the EntryImpl, including the listed fields using the listed
	 * getter objects. Treats fields under Sense separately if more than one field of Sense is included.
	 * The processing performed in this method makes it not very efficient in case it is called in an iteration.
	 * Modifying/copying the lists and the lists for the Sense objects would need to be done only once in such case.
	 * TODO consider adding a variant of this method accepting a list of EntryImpl objects and returning
	 * a list of LexEntry objects.
	 *  
	 * @param entry the entry to convert
	 * @param fieldIds the identifiers of the fields to include/copy, if null all fields are copied 
	 * @param efgs the getter objects to get the field values
	 * @return a LexEntry object
	 */
	private LexEntry toLexanEntry(EntryImpl entry, List<String> fieldIds, List<EntryFieldGetter> efgs) {
		// first create a minimal LexEntry
		LexanEntry lexanEntry = toLexanEntry(entry);
		List<String> includeFields = fieldIds;
		List<String> includeSenseFields = null;
		List<SenseFieldGetter> sfgs = null;
		
		if (fieldIds == null) {
			includeFields = getEntryFieldNames();// all fields
			efgs = createGetters(includeFields);
		}
		// if any of the fieldIds starts with sense, create per Sense getters
		// and if sense/grammatical-category is not listed, add it
		int numSenseFields = 0;
		boolean gramCatIncluded = false;// special treatment of gram. category
		for (String field : includeFields) {
			if (field.startsWith(LexiconFields.SENSE.getFieldName())) {
				numSenseFields++;
				if (field.equals(LexiconFields.SENSE_GRAM_CAT.getFieldName())) {
					gramCatIncluded = true;
					break;
				}
			}
		}
		
		// if only one sense field is included and it happens to be grammatical category,
		// don't bother grouping the sense fields in a container, otherwise do create 
		// separate containers for senses
		if (numSenseFields > 1 || (numSenseFields == 1 && !gramCatIncluded)) {
			includeFields = new ArrayList<String>(includeFields);// copy the list
			includeSenseFields = new ArrayList<String>();
			sfgs = new ArrayList<SenseFieldGetter>();
			
			for (int i = includeFields.size() - 1; i >= 0; i--) {
				String f = includeFields.get(i);
				if (f.startsWith(LexiconFields.SENSE.getFieldName())) {
					includeSenseFields.add(f);
					sfgs.add(SenseFieldGetterFactory.createGetter(f.substring(LexiconFields.SENSE.getFieldName().length() + 1)));
				}
			}
			if (!gramCatIncluded) {
				includeSenseFields.add(0, LexiconFields.SENSE_GRAM_CAT.getFieldName());
				sfgs.add(0, SenseFieldGetterFactory.createGetter(
						LexiconFields.SENSE_GRAM_CAT.getFieldName().substring(LexiconFields.SENSE.getFieldName().length() + 1)));
			}
		}
		
		
		int i = 0;
		for (EntryFieldGetter getter : efgs) {
			String fieldId = includeFields.get(i);
			i++;
			if (includeSenseFields != null && includeSenseFields.contains(fieldId)) {
				continue;
			}
			// check if the requested fields aren't already there
			if (lexanEntry.getLexItem(fieldId) == null) {
				List<String> values = getter.getValues(entry);
				
				for (String value : values) {
					// THIS VARIANT DOES NOT CREATE LEXCONT OBJECTS!
					// It just adds multiple atoms to the entry with the same type.
					lexanEntry.addLexItem(new LexAtom(fieldId, value));
				}
			}
		}
		// conditionally add containers for sense fields
		if (includeSenseFields != null) {
			for (Sense s : entry.getSense()) {			
				LexCont lexCont = new LexCont(LexiconFields.SENSE.getFieldName());
				
				for (int j = 0; j < includeSenseFields.size(); j++) {
					String fieldId = includeSenseFields.get(j);
					SenseFieldGetter getter = sfgs.get(j);					
					List<String> values = getter.getValues((SenseImpl) s);
					
					for (String value : values) {
						// no further grouping
						lexCont.addLexItem(new LexAtom(fieldId, value));
					}
				}
				
				if (lexCont.getLexItems() != null) {
					lexanEntry.addLexItem(lexCont);
				}
			}
		}
		
		return lexanEntry;
	}
	
	/**
	 * 
	 * @param queryAtom not null and the type of the query object is not null
	 * @return a list of Lexan API entries
     * <p>
     * For each result new objects may be generated.
     * If you query for the same entries multiple times, you may get different objects. 
	 */
	private List<LexEntry> getListOfMatchEntries(LexAtom queryAtom) {
		List<LexEntry> matchEntries = new ArrayList<LexEntry>();
		EntryFieldGetter getter = EntryFieldGetterFactory.createGetter(queryAtom.getType());
		
		for (Entry sourceEntry : sourceLexicon.getEntries()) {
			EntryImpl entryImpl = (EntryImpl) sourceEntry;
			List<String> values = getter.getValues(entryImpl);

			for (String value : values) {
				if (queryAtom.getLexValue() == null || queryAtom.getLexValue().equals(value)) {// or value.matches(queryAtom.getLexValue() ? to support regex?
					matchEntries.add(toLexanEntry(entryImpl, queryAtom.getType()));
				}
			}
		}
		
		return matchEntries;
	}
	
	/**
	 * 
	 * @param queryAtom not null and the type of the query object is not null
	 * @return a list of Lexan API entries
     * <p>
     * For each result new objects may be generated.
     * If you query for the same entries multiple times, you may get different objects. 
	 */
	private List<LexEntry> getListOfMatchEntries(LexAtom queryAtom, List<String> fieldsToInclude) {
		List<LexEntry> matchEntries = new ArrayList<LexEntry>();
		
		List<String> allFields = fieldsToInclude;
		if (!fieldsToInclude.contains(queryAtom.getType())) {
			// don't modify the list that is passed as an argument
			allFields = new ArrayList<String>(fieldsToInclude.size() + 1);
			allFields.add(queryAtom.getType());
			allFields.addAll(fieldsToInclude);
		}
		
		EntryFieldGetter getter = EntryFieldGetterFactory.createGetter(queryAtom.getType());
		List<EntryFieldGetter> efgs = createGetters(allFields);
		
		for (Entry sourceEntry : sourceLexicon.getEntries()) {
			EntryImpl entryImpl = (EntryImpl) sourceEntry;
			List<String> values = getter.getValues(entryImpl);

			for (String value : values) {
				// TODO: If the queryAtom selects some entries, but the fieldsToInclude
				// could multiply them (for instance, select on 'lexical-unit' but include 'sense/id'),
				// we might want results for each sense.
				// Right now, it just includes multiple copies of the 'sense/id' field.
				if (queryAtom.getLexValue() == null || 
				    queryAtom.getLexValue().equals(value)) {// or value.contains(queryAtom.getLexValue())   value.matches(queryAtom.getLexValue() ? to support regex?
					matchEntries.add(toLexanEntry(entryImpl, allFields, efgs));
				}
			}
		}
		
		return matchEntries;
	}
	
	/**
	 * 
	 * @param queryItem the item to search for, contains the field name and the value search string. 
	 * If the value part of the query object is null, all entries will be returned that have that
	 * field (and if it is not empty).
	 * 
	 * @return a list of lexical entries that match the query value (exact match, case sensitive?).
	 * The returned LexEntry object(s) will only contain the specified field and some default fields. 
	 * Or null if the queryItem is null.
     * <p>
     * For each result new objects may be generated.
     * If you query for the same entries multiple times, you may get different objects. 
	 */
	@Override
	public List<LexEntry> getEntries(LexAtom queryItem) {
		if (queryItem == null) {
			// throw exception? log?	
			return null;
		}
		if (queryItem.getType() == null) {// could also be implemented as match any field?
			// log
			return null;
		}
		
		return getListOfMatchEntries(queryItem);
	}
	
	/**
	 * 
	 * @param queryItem the item to search for, contains the field name and the value search string.
	 * If the value part of the query object is null, all entries will be returned that have that
	 * field (and if it is not empty).
	 * @param fieldsToInclude the entry fields to be included in each returned entry. If null all fields will be returned
	 * 
	 * @return a list of lexical entries that match the query value (exact match, case sensitive).
	 *  The returned list can contain LexAtom and LexCont objects.
     * <p>
     * For each result new objects may be generated.
     * If you query for the same entries multiple times, you may get different objects. 
	 */
	@Override
	public List<LexEntry> getEntries(LexAtom queryItem, List<String> fieldsToInclude) {
		if (queryItem == null) {
			// throw exception? log?	
			return null;
		}
		if (queryItem.getType() == null) {// could also be implemented as match any field?
			// log
			return null;
		}
		
		return getListOfMatchEntries(queryItem, fieldsToInclude);
	}
	
	/**
     * @return the (Lexan API) lexical entry or null, the entry will only contain the minimal set of fields.
     * <p>
     * For each result new objects may be generated.
     * If you query for the same entries multiple times, you may get different objects. 
	 */
	@Override
	public LexEntry getEntryById(String id) {
		EntryImpl sourceEntry = sourceLexicon.getEntryWithId(id);
		
		if (sourceEntry != null) {
			return toLexanEntry(sourceEntry);
		}
		
		return null;
	}

    /**
     * Gets a Lexan API entry with the specified id.
     * 
     * @param id the unique id of an entry
     * @param fields the fields to include in the returned entry. If null all fields will be returned
     *  
     * @return the lexical entry or null, the entry will contain the minimal set of fields and the 
     * requested fields.
     * <p>
     * For each result new objects may be generated.
     * If you query for the same entries multiple times, you may get different objects. 
     */
	@Override
	public LexEntry getEntryById(String id, List<String> includeFields) {
		EntryImpl sourceEntry = sourceLexicon.getEntryWithId(id);
		
		if (sourceEntry != null) {
			return toLexanEntry(sourceEntry, includeFields, createGetters(includeFields));
		}
		return null;
	}

	// ######################## Lexicon Component Listener implementation ################################
	// Notifications from the lexiconComponent.
	
	@Override // LexanLexicon
	public void addLexiconChangeListener(LexiconChangeListener listener) {
		if (listener != null) {
			if (listeners == null) {
				listeners = new ArrayList<LexiconChangeListener>(2);
			}
			listeners.add(listener);
		}
	}
	
	@Override // LexanLexicon
	public void removeLexiconChangeListener(LexiconChangeListener listener) {
		if (listener != null && listeners != null) {
			listeners.remove(listener);
		}		
	}
	
	protected void notifyListeners (LexiconChangeEvent event) {
		if (event != null && listeners != null) {
			for (LexiconChangeListener l : listeners) {
				l.lexiconChanged(event);
			}
		}
	}

	@Override // LexiconEditListener
	public void lexiconEdited(LexiconEvent<LexiconImpl> event) {
		if (event.getSource() == sourceLexicon &&
			event.getEditObject() == sourceLexicon) {
			LexiconChangeType changeType = convertChangeType(event.getType());
			LexiconChangeEvent newEvent = new LexiconChangeEvent(
					this,
					this,
					changeType);
			
			notifyListeners(newEvent);
		}
	}

	@Override // LexiconEditListener
	public void lexiconEntryEdited(LexiconEvent<EntryImpl> event) {
		if (event.getSource() == sourceLexicon) {
			LexiconChangeType changeType = convertChangeType(event.getType());
			EntryImpl editObj = event.getEditObject();
			
			if (event.getType() == LexiconEventType.REMOVE) {
				// copy the entire entry by passing null for the list of field identifiers
				LexEntry le = toLexanEntry(editObj, null, null);
				
				if (le != null) {
					notifyListeners(new LexiconChangeEvent(this, le, changeType));
				}
			} else {	
				/*
				// why get multiple (LexEntry) entries just to select the one with the same
				// id to create an event for? This at least fails in case of a REMOVE/DELETE event 
				// (see above) because it is no longer in the list of entries of the lexicon.
				LexAtom la = new LexAtom(lexUnit, editObj.getLexicalUnit());
				List<LexEntry> lle = getEntries(la, listId);
				
				if (lle != null) {
					for (LexEntry le : lle) {
						if (le.getId().equals(editObj.getId())) {
							LexiconChangeEvent newEvent = new LexiconChangeEvent(
									this,
									le,
									changeType);
							
							notifyListeners(newEvent);
						}
					}
				}
				*/
				// copy the entire entry by passing null for the list of field identifiers
				LexEntry le = toLexanEntry(editObj, null, null);
				
				if (le != null) {
					notifyListeners(new LexiconChangeEvent(this, le, changeType));
				}
			}
		}
	}

	@Override // LexiconEditListener
	public void lexiconSenseEdited(LexiconEvent<SenseImpl> event) {
		if (event.getSource() == sourceLexicon) {
			LexiconChangeType changeType = convertChangeType(event.getType());
			SenseImpl editObj = event.getEditObject();
			// in case of deletion of a sense the EntryImpl might already be null
			EntryImpl entryObj = editObj.getEntry();
			if (entryObj != null) {
				/*
				LexAtom la = new LexAtom(lexUnit, entryObj.getLexicalUnit());
				List<LexEntry> lle = getEntries(la, listIds);
				// not sure if this is necessary, why not create a LexanEntry directly from the netryObj?
				if (lle != null) {
					for (LexEntry le : lle) {
						LexAtom atom;
						
						if (le.getId().equals(editObj.getId()) &&
							(atom = (LexAtom) le.getLexItem(senseId)) != null &&
							atom.getLexValue().equals(editObj.getId())) {
							LexiconChangeEvent newEvent = new LexiconChangeEvent(
									this,
									le,
									changeType);
							
							notifyListeners(newEvent);
						}
					}
				}
				*/
				// copy the entire entry by passing null for the list of field identifiers
				LexEntry le = toLexanEntry(entryObj, null, null);
				
				if (le != null) {
					notifyListeners(new LexiconChangeEvent(this, le, changeType));
				}
			}
		} else if (event.getSource() instanceof EntryImpl) {
			EntryImpl entryObj = (EntryImpl) event.getSource();
			if (entryObj.getLexicon() == sourceLexicon) {
				LexiconChangeType changeType = convertChangeType(event.getType());
				SenseImpl editObj = event.getEditObject();
				
				if (event.getType() == LexiconEventType.REMOVE) {
					LexEntry le = toLexanEntry(entryObj, listIds, createGetters(listIds));
					
					if (le != null) {
						notifyListeners(new LexiconChangeEvent(this, le, changeType));
					}
				} else {
					// old implementation
					LexAtom la = new LexAtom(LexiconFields.ENTRY_LEX_UNIT.getFieldName(), 
							entryObj.getLexicalUnit());
					List<LexEntry> lle = getEntries(la, listIds);
					// not sure if this is necessary, why not create a LexanEntry directly from the netryObj?
					if (lle != null) {
						for (LexEntry le : lle) {
							LexAtom atom;
							
							if (le.getId().equals(editObj.getId()) &&
								(atom = (LexAtom) le.getLexItem(LexiconFields.SENSE_ID.getFieldName())) != null &&
								atom.getLexValue().equals(editObj.getId())) {
								LexiconChangeEvent newEvent = new LexiconChangeEvent(
										this,
										le,
										changeType);
								
								notifyListeners(newEvent);
							}
						}
					}
				}
			}
		}
	}
	
	protected LexiconChangeType convertChangeType(LexiconEventType eventType) {
		LexiconChangeType changeType = LexiconChangeType.CHANGE;
		switch (eventType) {
		case ADD:
			changeType = LexiconChangeType.ADD;
			break;
		case CHANGE:
			changeType = LexiconChangeType.CHANGE;
			break;
		case ORDER_CHANGE:
			changeType = LexiconChangeType.CHANGE;
			break;
		case REMOVE:
			changeType = LexiconChangeType.DELETE;
			break;
		case SORT_ORDER_CHANGE:
			changeType = LexiconChangeType.CHANGE;
			break;
		default:
			break;
		}
		
		return changeType;
	}
}

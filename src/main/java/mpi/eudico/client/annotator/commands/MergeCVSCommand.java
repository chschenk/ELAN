package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;

/**
 * A Command to merge two Controlled Vocabularies from two different Transcriptions,
 * i.e. entries present in the second CV but not in the first are copied to the first
 * CV.
 *
 * @author Han Sloetjes
 */
public class MergeCVSCommand implements UndoableCommand {
    private String commandName;
    
    // receiver
    private TranscriptionImpl transcription;
    private ControlledVocabulary conVoc;
    private ControlledVocabulary secConVoc;
    private ControlledVocabulary mergedConVoc;
    private ArrayList<CVEntry> copiedEntries;
    
    /**
     * Creates a new MergeCVSCommand
     */
    public MergeCVSCommand(String name) {
        commandName = name;
    }

    /**
     * Removes the entries that have been added by the merging process.
     * For a complicated merge, that model isn't sufficient, and we just
     * kept a copy of the old vocabulary.
     * 
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#undo()
     */
    @Override
	public void undo() {
        if (conVoc != null && copiedEntries != null && copiedEntries.size() > 0) {
            CVEntry entry;
            for (int i = 0; i < copiedEntries.size(); i++) {
                entry = copiedEntries.get(i);
                conVoc.removeEntry(entry);
            }
        } else if (mergedConVoc != null && conVoc != null) {
        	// Double-check if not too much changed
        	ControlledVocabulary check = transcription.getControlledVocabulary(mergedConVoc.getName());
        	if (check == mergedConVoc) {	// maybe too strict? maybe just check if != null?
        		transcription.replaceControlledVocabulary(conVoc);
        	}        	
        }
    }

    /**
     * Again adds the previously added entries.
     * 
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#redo()
     */
    @Override
	public void redo() {
        if (conVoc != null && copiedEntries != null && copiedEntries.size() > 0) {
            CVEntry entry;
            for (int i = 0; i < copiedEntries.size(); i++) {
                entry = copiedEntries.get(i);
                conVoc.addEntry(entry);
            }
        } else if (mergedConVoc != null && conVoc != null) {
        	// Double-check if not too much changed
        	ControlledVocabulary check = transcription.getControlledVocabulary(conVoc.getName());
        	if (check == conVoc) {	// maybe too strict? maybe just check if != null?
        		transcription.replaceControlledVocabulary(mergedConVoc);
        	}
        }
    }

    /**
     * Merges two ControlledVocabularies.<br>
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the first Controlled
     *        Vocabulary (Controlled Vocabulary)</li> <li>arg[1] = the second
     *        Controlled Vocabulary (Controlled Vocabulary)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        conVoc = (ControlledVocabulary) arguments[0];
        secConVoc = (ControlledVocabulary) arguments[1];
        
        if ((transcription.getControlledVocabulary(conVoc.getName()) != null) &&
                (secConVoc != null)) {
            
            // Simple and common case:
        	// both vocabularies have one language, and it is the same one.
            boolean simple;
            simple = conVoc.getNumberOfLanguages() == 1 &&
            		 secConVoc.getNumberOfLanguages() == 1 &&
            		 conVoc.getLongLanguageId(0).equals(secConVoc.getLongLanguageId(0));
            
            if (simple) {
                CVEntry[] first = conVoc.getEntriesSortedByAlphabet(0);
                CVEntry[] second = secConVoc.getEntriesSortedByAlphabet(0);
                CVEntry nextEntry;

                copiedEntries = new ArrayList<CVEntry>();

            outerloop:
	            for (int i = 0; i < second.length; i++) {
	                for (int j = 0 ; j < first.length; j++) {
	                    // compare second[i] and first[j]
	                    if (first[j].getValue(0).equals(second[i].getValue(0))) {
	                        continue outerloop;
	                    }
	                    if (j == first.length - 1 || first[j].getValue(0).compareTo(second[i].getValue(0)) > 0) {
	                        // the second entry is not in the list...
	                        nextEntry = new CVEntry(conVoc, second[i]);
	                        copiedEntries.add(nextEntry);
	                        conVoc.addEntry(nextEntry);
	                        continue outerloop;
	                    }
	                }
	            }
            } else {
            	 mergedConVoc = complicatedMerge(conVoc, secConVoc);
            	 transcription.replaceControlledVocabulary(mergedConVoc);
            }            
        }
    }

    /**
     * This function does a non-trivial merge between two vocabularies, making a new one.
     * The new one has the merge of the languages of the inputs.
     * It has the name of the first input.
     * @param dest
     * @param src
     */
    private ControlledVocabulary complicatedMerge(ControlledVocabulary one,
			ControlledVocabulary two) {
    	ControlledVocabulary dest = new ControlledVocabulary(one.getName());    	
    	
    	Map<String, Integer> langCache = new HashMap<String, Integer>();
    	List<String> overlap = new ArrayList<String>();
    	
    	int nLangs1 = one.getNumberOfLanguages();
    	
    	// Get the languages from the first input into the output
    	for (int i = 0; i < nLangs1; i++) {
    		String id = one.getLanguageId(i);
    		String longId = one.getLongLanguageId(i);
    		String label = one.getLanguageLabel(i);
    		int index = dest.addLanguage(id, longId, label);
    		
    		langCache.put(longId, index);
    	}
    	
    	// Then add the languages from the second input,
    	// insofar as they aren't there yet.
    	int nLangs2 = two.getNumberOfLanguages();
    	
    	for (int i = 0; i < nLangs2; i++) {
    		String id = two.getLanguageId(i);
    		String longId = two.getLongLanguageId(i);
    		String label = two.getLanguageLabel(i);
    		
    		if (langCache.containsKey(longId)) {
    			overlap.add(longId);
    		} else {
    			// Make 'id' unique if necessary
    		    while (one.getIndexOfLanguage(id) >= 0) {
    				id += String.valueOf((int)(Math.random() * 10000));
    			}
	    		int index = dest.addLanguage(id, longId, label);
	    		
	    		langCache.put(longId, index);
    		}
    	}
    	
    	if (overlap.isEmpty()) {
    		// Simple sub-case: there is no need to merge any entries from
    		// both inputs, since they have all different languages.
    		putIn(dest, one);
    		putIn(dest, two);
    	} else if (overlap.size() == 1 || true) {
    		// More complex sub-case. There could be an overlap of just
    		// one, or of more than one language. We simply disregard
    		// any problems that may happen in the latter case.
    		// Maybe TODO something clever?
    		String overlap1 = overlap.get(0);
    		int indexd = dest.getIndexOfLanguage(overlap1);
    		int index2 = two.getIndexOfLanguage(overlap1);
    		putIn(dest, one);
    		mergeIn(dest, indexd, two, index2);
    	}
    	   	
		return dest;    	
	}
    
	/**
	 * Make a quick mapping from language index values in {@code src} to 
	 * corresponding index values in {@code dest} 
	 * instead of doing it again and again.
	 */
    private int[] makeIndexMapping(ControlledVocabulary dest, ControlledVocabulary src)
    {
    	int nLanguages = src.getNumberOfLanguages();    	
    	int mapping[] = new int[nLanguages];
    	
    	for (int i = 0; i < nLanguages; i++) {
    		int mapped = dest.getIndexOfLanguage(src.getLongLanguageId(i));
    		mapping[i] = mapped;
    	}
    	
    	return mapping;
    }

    private void putIn(ControlledVocabulary dest, ControlledVocabulary src) {
    	int nLanguages = src.getNumberOfLanguages();
    	
    	int mapping[] = makeIndexMapping(dest, src);
    	
    	for (CVEntry e : src) {
    		CVEntry newEntry = e.cloneExceptValues(dest);
    		
    		for (int i = 0; i < nLanguages; i++) {
    			int mapped = mapping[i];
    			newEntry.setValue(mapped, e.getValue(i));
    			newEntry.setDescription(mapped, e.getDescription(i));
    		}
    		dest.addEntry(newEntry);
    	}
    }
    
    private void mergeIn(ControlledVocabulary dest, int destIndex, ControlledVocabulary src, int srcIndex) {
    	int nLanguages = src.getNumberOfLanguages();
    	
    	int mapping[] = makeIndexMapping(dest, src);
    	
    	for (CVEntry e : src) {
    		// Find the entry in dest which contains
    		// the same word in the overlapping language.
    		// If there is no such entry, we need to create a new one.
    		String overlapValue = e.getValue(srcIndex);
    		CVEntry destEntry = null;
    		int dontCopyIndex = srcIndex;	// don't copy over the word from the common language
    		if (!overlapValue.isEmpty()) {
    			destEntry = dest.getEntryWithValue(destIndex, overlapValue);
    		}
    		if (destEntry == null) {
    			destEntry = e.cloneExceptValues(dest);
       			dest.addEntry(destEntry);
    			dontCopyIndex = -1;		// copy all languages
    		}
    		
    		/*
    		 * When merging a new entry into an existing one, don't change the
    		 * language that we detected as overlap. Keeping old data seems
    		 * more logical than changing to any possibly different description.
    		 * But don't change anything into an empty string, just in case.
    		 * (Overlap in more than 1 language is for the rest ignored here!!)
    		 */
    		for (int i = 0; i < nLanguages; i++) {
    			if (i != dontCopyIndex) {
	    			int mapped = mapping[i];
	    			String v = e.getValue(i);
	    			if (!v.isEmpty()) {
	    				destEntry.setValue(mapped, v);
	    			}
	    			String d = e.getDescription(i);
	    			if (d != null && !d.isEmpty()) {
	    				destEntry.setDescription(mapped, d);
	    			}
    			}
    		}
    	}
    }
    
	/**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }

}

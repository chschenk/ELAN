package mpi.eudico.client.annotator.commands;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.imports.MergeUtil;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.util.TranscriptionECVLoader;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.EAFSkeletonParser;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;

/**
 * Import tiers (if necessary with Linguistic Types and ControlledVocabularies)
 * from an eaf or etf into a transcription. 
 */
public class ImportTiersCommand implements UndoableCommand, ClientLogger {
    private String commandName;
    
    // receiver
    private TranscriptionImpl transcription;
    
    private List<TierImpl> tiersAdded;
    private List<LinguisticType> typesAdded;
    private List<ControlledVocabulary> cvsAdded;
    private List<LexiconLink> lexLinksAdded;

    private Map<String, Color> tierColorsAdded;
    private Map<String, Color> tierHighlightsAdded;
    private Map<String, Font> tierFontsAdded;
    private Map<String, Map<String, Map<String, Object>>> cvPrefsAdded;
    
    public ImportTiersCommand(String name) {
        commandName = name;
    }
    
    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#undo()
     */
    @Override
	public void undo() {
        if(transcription == null) {
            LOG.warning("The transcription is null.");
            return;
        }
        if (tiersAdded == null) {
            LOG.warning("No tiers have been added.");
            return;
        }
        for (TierImpl t : tiersAdded) {
            transcription.removeTier(t);
        }
        for (LinguisticType lt :  typesAdded) {
            transcription.removeLinguisticType(lt);
        }
        for (ControlledVocabulary cv : cvsAdded) {
            transcription.removeControlledVocabulary(cv);
        }
        for (LexiconLink ll : lexLinksAdded) {
        	transcription.removeLexiconLink(ll);
        }
        
        removeImportedPreferences();
    }

    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#redo()
     */
    @Override
	public void redo() {
        if(transcription == null) {
            LOG.warning("The transcription is null.");
            return;
        }
        if (tiersAdded == null) {
            LOG.warning("No tiers can be added.");
            return;
        }
        for (LexiconLink ll : lexLinksAdded) {
        	transcription.addLexiconLink(ll);
        }
        for (ControlledVocabulary cv : cvsAdded) {
            transcription.addControlledVocabulary(cv);
        }       
        for (LinguisticType lt : typesAdded) {
            transcription.addLinguisticType(lt);
        }
        for (TierImpl t : tiersAdded) {
            transcription.addTier(t);
        }
        
        addImportedPreferences();
        Preferences.notifyListeners(transcription);
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the fileName of an eaf
     *        or etf file (String)</li> </ul>
     */
    @SuppressWarnings("unchecked")
	@Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        String fileName = (String) arguments[0];
        if (fileName == null) {
            LOG.warning("The filename is null");
            return; // report??
        }
        
        fileName = FileUtility.pathToURLString(fileName).substring(5);
        
        TranscriptionImpl srcTrans = null;
        try {
            EAFSkeletonParser parser = new EAFSkeletonParser(fileName);
            parser.parse();
            List<TierImpl> tiers = parser.getTiers();
            List<String> tierOrder = parser.getTierOrder();
            List<LinguisticType> types = parser.getLinguisticTypes();
            List<ControlledVocabulary> cvs = parser.getControlledVocabularies();
            
            srcTrans = new TranscriptionImpl();
            srcTrans.setPathName(fileName);
            //srcTrans.setNotifying(false);
            srcTrans.setLinguisticTypes(new ArrayList<LinguisticType>(types));

            for (int i = 0; i < tierOrder.size(); i++) {
            	String tierName = tierOrder.get(i);
                for (int j = 0; j < tiers.size(); j++) {
                	TierImpl tier = tiers.get(j);
                	if (tierName.equals(tier.getName())) {
                		srcTrans.addTier(tier);	
                	}
                }
            }
            
            for (ControlledVocabulary cv : cvs) {
                srcTrans.addControlledVocabulary(cv);
            }
            
        } catch (ParseException pe) {
            LOG.warning(pe.getMessage());
            pe.printStackTrace();
            return;
        }
        // store current tiers types, cvs
        //transcription.setNotifying(false);
        
        List<TierImpl> currentTiers = new ArrayList<TierImpl>(transcription.getTiers());
        List<LinguisticType> currentTypes = new ArrayList<LinguisticType>(transcription.getLinguisticTypes());
        List<ControlledVocabulary> currentCvs = new ArrayList<ControlledVocabulary>(transcription.getControlledVocabularies());
        List<LexiconLink> currentLexLinks = new ArrayList<LexiconLink>(transcription.getLexiconLinks().values());
        
        MergeUtil mergeUtil = new MergeUtil();
        List<TierImpl> tiersAddable = mergeUtil.getAddableTiers(srcTrans, transcription, null);
        if (tiersAddable == null || tiersAddable.size() == 0) {
            LOG.warning("There are no tiers that can be imported");
            transcription.setNotifying(true);
            return;
        }
        tiersAddable = mergeUtil.sortTiers(tiersAddable);
        mergeUtil.addTiersTypesAndCVs(srcTrans, transcription, tiersAddable);
        //store the tiers, types and cvs that have been added, for undo/redo
        //transcription.setNotifying(true);
        
        tiersAdded = new ArrayList<TierImpl>();
        typesAdded = new ArrayList<LinguisticType>();
        cvsAdded = new ArrayList<ControlledVocabulary>();
        lexLinksAdded = new ArrayList<LexiconLink>();
        
        for (TierImpl t : transcription.getTiers()) {
            if (!currentTiers.contains(t)) {
                tiersAdded.add(t);
            }
        }
        
        for (LinguisticType lt : transcription.getLinguisticTypes()) {
            if (!currentTypes.contains(lt)) {
                typesAdded.add(lt);
                
                if (lt.getLexiconQueryBundle() != null) {
                	if (!currentLexLinks.contains(lt.getLexiconQueryBundle().getLink())) {
                		transcription.addLexiconLink(lt.getLexiconQueryBundle().getLink());
                		lexLinksAdded.add(lt.getLexiconQueryBundle().getLink());
                	}
                }
            }
        }
        
        TranscriptionECVLoader ecvLoader = new TranscriptionECVLoader();
        for (ControlledVocabulary cv : transcription.getControlledVocabularies()) {
            if (!currentCvs.contains(cv)) {
                cvsAdded.add(cv);
                if (cv instanceof ExternalCV) {        	
                	ecvLoader.loadExternalCVs(transcription, null);
                }
            }
        }
        // import the preferences of a template if the source of the import was an .etf file
        if (fileName.toLowerCase().endsWith(".etf")) {
        	Preferences.importPreferences(srcTrans, 
        			fileName.substring(0, fileName.lastIndexOf('.'))  + ".pfsx");
        }
        
        // update preferences
        if (!tiersAdded.isEmpty()) {
        	// tier main colors
        	Map<String, Color> orgColors = Preferences.getMapOfColor("TierColors", srcTrans);
        	if (orgColors != null) {
        		for (TierImpl t : tiersAdded) {
        			Color c = orgColors.get(t.getName());
        			if (c != null) {
        				if (tierColorsAdded == null) {
        					tierColorsAdded = new HashMap<String, Color>(tiersAdded.size());
        				}
        				tierColorsAdded.put(t.getName(), c);
        			}
        		}
        	}
        	// tier highlight colors
        	Map<String, Color> orgHLColors = Preferences.getMapOfColor("TierHighlightColors", srcTrans);
        	if (orgHLColors != null) {
        		for (TierImpl t : tiersAdded) {
        			Color c = orgHLColors.get(t.getName());
        			if (c != null) {
        				if (tierHighlightsAdded == null) {
        					tierHighlightsAdded = new HashMap<String, Color>(tiersAdded.size());
        				}
        				tierHighlightsAdded.put(t.getName(), c);
        			}
        		}
        	}
        	// tier fonts
        	Map<String, Font> orgFonts = Preferences.getMapOfFont("TierFonts", srcTrans);
        	if (orgFonts != null) {
        		for (TierImpl t : tiersAdded) {
        			Font f = orgFonts.get(t.getName());
        			if (f != null) {
        				if (tierFontsAdded == null) {
        					tierFontsAdded = new HashMap<String, Font>(tiersAdded.size());
        				}
        				tierFontsAdded.put(t.getName(), f);
        			}
        		}
        	}
        }
        
        // check new cv's too?
        if (!cvsAdded.isEmpty()) {
            Map<String, Object> importPrefs = null;
            boolean oldPrefs = false;

        	Object cvPrefObj = Preferences.get(Preferences.CV_PREFS, srcTrans);
        	if (cvPrefObj instanceof Map) {
        		importPrefs = (Map<String, Object>) cvPrefObj;
        	} else {        		
            	cvPrefObj = Preferences.get(Preferences.CV_PREFS_OLD_2_7, srcTrans);
            	if (cvPrefObj instanceof Map) {
            		importPrefs = (Map<String, Object>) cvPrefObj;
            		oldPrefs = true;
            	}        		
        	}
        	
        	if (importPrefs != null) {
        		for (ControlledVocabulary cv : cvsAdded) {
        			importPreferencesFor(importPrefs, cv, oldPrefs);
        		}
        	}
        }
        
    	// finally add the collected preferences
    	addImportedPreferences();
        Preferences.notifyListeners(transcription);
    }

    /**
     * Returns the name of the command
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }

    /**
     * Removes colors and fonts etc. that have been imported.
     */
    @SuppressWarnings("unchecked")
	private void removeImportedPreferences() {
        if (tierColorsAdded != null) {
        	Map<String, Color> colors = Preferences.getMapOfColor("TierColors", transcription);
        	if (colors != null) {// shouldn't
	        	Iterator<String> tierIter = tierColorsAdded.keySet().iterator();
	        	while (tierIter.hasNext()) {
	        		colors.remove(tierIter.next());
	        	}
        	}
        }
        
        if (tierHighlightsAdded != null) {
			Map<String, Color> highlightColors = Preferences.getMapOfColor("TierHighlightColors", 
					transcription);
			if (highlightColors != null) {// shouldn't
				Iterator<String> tierIter = tierHighlightsAdded.keySet().iterator();
				while (tierIter.hasNext()) {
					highlightColors.remove(tierIter.next());
				}
			}
        }
        
        if (tierFontsAdded != null) {
        	Map<String, Font> fonts = Preferences.getMapOfFont("TierFonts", transcription);
			if (fonts != null) {// shouldn't
				Iterator<String> tierIter = tierFontsAdded.keySet().iterator();
				while (tierIter.hasNext()) {
					fonts.remove(tierIter.next());
				}
			}
        }
        // check cv's
        if (cvPrefsAdded != null) {
	    	HashMap<String, Map<String, Map<String, Object>>> cvPrefs = 
					(HashMap<String, Map<String, Map<String, Object>>>) Preferences.getMap(
							Preferences.CV_PREFS, transcription);
	        if (cvPrefs != null) {
	        	Iterator<String> cvPrefIter = cvPrefsAdded.keySet().iterator();
	        	while(cvPrefIter.hasNext()) {
	        		cvPrefs.remove(cvPrefIter.next());
	        	}
	        }
        }
        
        Preferences.notifyListeners(transcription);
    }
    
    /**
     * Adds the collected preferred colors and fonts for the imported tiers.
     */
    @SuppressWarnings("unchecked")
	private void addImportedPreferences() {
		if (tierColorsAdded != null) {
			Map<String, Color> colors = Preferences.getMapOfColor("TierColors", transcription);
			if (colors == null) {
				Preferences.set("TierColors", new HashMap<String, Color>(tierColorsAdded), 
						transcription);
			} else {
				colors.putAll(tierColorsAdded);
			}
		}
		
		if (tierHighlightsAdded != null) {
			Map<String, Color> highlightColors = Preferences.getMapOfColor("TierHighlightColors", 
					transcription);
			if (highlightColors == null) {
				Preferences.set("TierHighlightColors", new HashMap<String, Color>(tierHighlightsAdded), 
						transcription);
			} else {
				highlightColors.putAll(tierHighlightsAdded);
			}
		}
		
		if (tierFontsAdded != null) {
			Map<String, Font> fonts = Preferences.getMapOfFont("TierFonts", transcription);
			if (fonts == null) {
				Preferences.set("TierFonts", new HashMap<String, Font>(tierFontsAdded),
						transcription);
			} else {
				fonts.putAll(tierFontsAdded);
			}
		}
		
		if (cvPrefsAdded != null) {
        	HashMap<String, Map<String, Map<String, Object>>> cvPrefs = 
					(HashMap<String, Map<String, Map<String, Object>>>) Preferences.getMap(
							Preferences.CV_PREFS, transcription);
			if (cvPrefs == null) {
				cvPrefs = new HashMap<String, Map<String, Map<String, Object>>>();
				Preferences.set(Preferences.CV_PREFS, cvPrefs, transcription);
			}
			cvPrefs.putAll(cvPrefsAdded);
		}
		
    }
    
    /**
     * Checks, loads and applies CV entry preferences and stores the preferences in a map
     * (without applying them to Preferences yet) for undo/redo.
     */
	@SuppressWarnings("unchecked")
	private void importPreferencesFor(Map<String, Object> importPrefs, ControlledVocabulary cv,
			boolean oldStylePrefs) {
        if (importPrefs != null && cv != null) {
    		final String color = "Color";
    		final String keyCode = "KeyCode";
    		// for storing copied prefs
			Map<String, Map<String, Object>> copyCVPref = new HashMap<String, Map<String,Object>>();
			Map<String, Object> copyEntPref = null;
    		
        	Map<String, Object> hm = (Map<String, Object>) importPrefs.get(cv.getName());
        	Map<String, Object> entMap;
        	if (hm != null) {
        		for (CVEntry cve : cv) {
        			String key = oldStylePrefs ? cve.getValue(0) : cve.getId();
        			entMap = (Map<String, Object>) hm.get(key);
        			if (entMap != null) {
        				Object c = entMap.get(color);
        				if (c instanceof Color) {	
        					cve.setPrefColor((Color) c);
        					if (copyEntPref == null) {
        						copyEntPref = new HashMap<String, Object>(3);
        					}
        					copyEntPref.put(color, c);
        				}
        				Object k = entMap.get(keyCode);
        				if (k instanceof Integer) {
        					cve.setShortcutKeyCode((Integer) k);
        					if (copyEntPref == null) {
        						copyEntPref = new HashMap<String, Object>(3);
        					}
        					copyEntPref.put(keyCode, k);
        				}
        				
        				if (copyEntPref != null) {
        					copyCVPref.put(cve.getId(), copyEntPref);
        				}
        			}
        		}
        		
        		if (!copyCVPref.isEmpty()) {
        			// store for undo / redo
        			if (cvPrefsAdded == null) {
        				cvPrefsAdded = new  HashMap<String, Map<String, Map<String, Object>>>();      				
        			}
        			cvPrefsAdded.put(cv.getName(), copyCVPref);
        		}
        	}
        }
	}
}

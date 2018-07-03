package mpi.eudico.client.annotator.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.turnsandscenemode.TaSSpecialMarkers;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
/**
 * A command to save a transcription produced in Simple-ELAN
 * with separated tiers for translation and speakers (in case
 * there are special markers for that within annotations).
 * 
 * Current implementation is not optimized; existing annotations are checked
 * several times for the special markers, first to accommodate the creation of
 * new tiers, then to split annotations into text and translations,
 * possibly per speaker
 */
public class ExportRegularMultitierEafCommand implements Command {
	private String name;
	private String translationTypeName = "translation";
	private String transSuffix = "trans";
	private String unknownSpeakerName = "unknown"; 
	
	/**
	 * Constructor
	 * @param name name of the command
	 */
	public ExportRegularMultitierEafCommand(String name) {
		super();
		this.name = name;
	}

	/**
	 * Shows a file chooser, converts tiers with compound annotations
	 * (transcription and translation or speaker markers) to separate
	 * tiers and saves the result as a regular eaf file.
	 * 
	 * @param receiver the transcription to convert and save
	 * @param arguments null
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		TranscriptionImpl transcription = (TranscriptionImpl) receiver;
		if (transcription == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Cannot export the transcription: null");
			}
			return;// log
		}
		FileChooser chooser = new FileChooser(ELANCommandFactory.getRootFrame(transcription));
		File origFile = null;
		//chooser.createAndShowFileDialog(title, dialogType, extensions, mainFilterExt, prefStringToLoadtheCurrentPath, selectedFileName);
    	String fileName = transcription.getName();
    	
    	if (!fileName.equals(TranscriptionImpl.UNDEFINED_FILE_NAME)) {
    		String filePath = FileUtility.urlToAbsPath(transcription.getFullPath());
    		origFile = new File(filePath);
    		chooser.setCurrentDirectory(filePath.substring(0, filePath.indexOf(fileName)));    
    		fileName = fileName.substring(0,fileName.lastIndexOf('.')) + ".eaf";
    		
    		chooser.createAndShowFileDialog(ElanLocale.getString("SaveDialog.Title"), FileChooser.SAVE_DIALOG, null,
        			FileExtension.EAF_EXT, null, fileName); 
    	} else {
    		chooser.createAndShowFileDialog(ElanLocale.getString("SaveDialog.Title"), FileChooser.SAVE_DIALOG, 
        			FileExtension.EAF_EXT, "LastUsedEAFDir"); 
    	}
    	File f = chooser.getSelectedFile();

    	if (f != null) {
    		// test if it is not the same path as the original
    		if (origFile != null) {
    			if (f.compareTo(origFile) == 0) {
    				if (LOG.isLoggable(Level.WARNING)) {
    					LOG.warning("Cannot overwrite the original file");
    				}
    				JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription), 
    						ElanLocale.getString("ExportRegularEAF.Message.OverwriteOriginal"), 
    						ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    				return;
    			}
    		}
    		// write the file and load it into a new transcription, as an alternative to creating 
    		// a copy of the transcription in memory
    		String nextAbsPath = f.getAbsolutePath();
    		TranscriptionImpl nextTranscription = null;
    		//System.out.println(nextAbsPath);
    		try {
    			ACMTranscriptionStore.getCurrentTranscriptionStore().storeTranscriptionIn(transcription, null, null, nextAbsPath, 
    				ACMTranscriptionStore.getCurrentEAFParser().getFileFormat());
    			
    			nextTranscription = new TranscriptionImpl(nextAbsPath);
    		} catch (IOException ioe) {
    			// show message
				JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription), 
						ElanLocale.getString("Message.Error.Save"), 
						ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    			return;
    		}
    		
    		if (nextTranscription != null) {
    			// set unchanged if necessary
    			if (nextTranscription.isChanged()) {
	    			nextTranscription.setUnchanged();	    			
    			}
    			splitTiers(nextTranscription);
    			
    			if (nextTranscription.isChanged()) {
	    			try {
	        			ACMTranscriptionStore.getCurrentTranscriptionStore().storeTranscriptionIn(nextTranscription, null, null, nextAbsPath, 
	            				ACMTranscriptionStore.getCurrentEAFParser().getFileFormat());
	    			} catch (IOException ioe) {
	    				JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription), 
	    						ElanLocale.getString("Message.Error.Save"), 
	    						ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
	    			}
    			}
    		}
    	}
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Checks the contents of tiers to see if a translation tier needs to be added 
	 * and/or if tiers per speaker need to be created
	 * 
	 * @param tr the transcription to operate on
	 */
	private void splitTiers(TranscriptionImpl tr) {
		// scan tiers for the markers that indicate that the contents should be split
		List<TierImpl> topTiers = tr.getTopTiers();
		List<TierImpl> tiersWithTrans = new ArrayList<TierImpl>();
		List<TierImpl> tiersWithSpeakers = new ArrayList<TierImpl>();
		
		for (TierImpl t : topTiers) {
			boolean transDetect = false;
			boolean speakDetect = false;
			
			for (Annotation a : t.getAnnotations()) {
				if (!speakDetect) {
					if (TaSSpecialMarkers.hasSpeakerMarker(a.getValue())) {
						tiersWithSpeakers.add(t);
						speakDetect = true;
					}
				}

				if (!transDetect) {
					if (TaSSpecialMarkers.hasTranslationMarker(a.getValue())) {
						tiersWithTrans.add(t);
						transDetect = true;
					}
				}

				if (transDetect && speakDetect) {
					break;
				}				
			}
		}
		
		if (tiersWithSpeakers.isEmpty() && tiersWithTrans.isEmpty()) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.info("No tiers to process, no tiers with translation or speaker markings.");
			}
			return;
		}
		
		Map<TierImpl, List<TierImpl>> speakerTierMap = null;
		Map<TierImpl, TierImpl> transTierMap = null;
		
		if (!tiersWithSpeakers.isEmpty()) {
			speakerTierMap = createSpeakerTiers(tr, tiersWithSpeakers);
		}
		if (!tiersWithTrans.isEmpty()) {
			transTierMap = createNewTranslationTiers(tr, tiersWithTrans, speakerTierMap);
		}
		// now annotations are created
		splitAnnotations(tr, speakerTierMap, transTierMap);
	}

	/**
	 * If an annotation contains translation markers and/or speaker markers,
	 * the contents is split up and distributed over several annotations, on several tiers.
	 * 
	 * @param tr the transcription
	 * @param speakerTierMap a map with source tiers as keys, lists of per speaker tiers as values
	 * @param transTierMap a map of 'text' tiers as keys to dependent translation tiers
	 */
	private void splitAnnotations(TranscriptionImpl tr,
			Map<TierImpl, List<TierImpl>> speakerTierMap,
			Map<TierImpl, TierImpl> transTierMap) {
		List<TierImpl> tiersToProcess = new ArrayList<TierImpl>();
		if (speakerTierMap != null) {
			tiersToProcess.addAll(speakerTierMap.keySet());
		}
		if (transTierMap != null) {
			tiersToProcess.addAll(transTierMap.keySet());
		}
		
		List<TierImpl> processedTiers = new ArrayList<TierImpl>();
		
		for (TierImpl t : tiersToProcess) {
			if (processedTiers.contains(t)) {
				continue;
			}
			// this concerns tiers, not necessarily all annotations
			boolean multiSpeak = speakerTierMap != null && speakerTierMap.containsKey(t);
			boolean hasTranslation = transTierMap != null && transTierMap.containsKey(t);
			Map<String, String> perSpeakerText = new HashMap<String, String>((multiSpeak ? 10 : 1));
			Map<String, String> perSpeakerTrans = new HashMap<String, String>(hasTranslation ? 10 : 0);
			String unknownSpeakerTierName = t.getName() + "@" + unknownSpeakerName;
			
			for (AbstractAnnotation aa : t.getAnnotations()) {
				perSpeakerText.clear();
				perSpeakerTrans.clear();
				// separate out speakers first, then translations
				int[][] spIndices = TaSSpecialMarkers.getSpeakerIndices(aa.getValue());		
				int valLength = aa.getValue().length();
				
				if (spIndices != null) {
					// split the value first, then check translations, the indices mark the speaker/tier name positions
					
					if (spIndices[0][0] != 0) {
						// check if there is text before the first speaker label
						String s = aa.getValue().substring(0, spIndices[0][0]).trim();
						if (s.length() > 0) {
							perSpeakerText.put(unknownSpeakerTierName, s);
						}
					}
					for (int i = 0; i < spIndices.length; i++) {
						String sp = aa.getValue().substring(spIndices[i][0], spIndices[i][1]);
						String val = "";
						if (i < spIndices.length - 1) {
							val = aa.getValue().substring(spIndices[i][1], spIndices[i + 1][0]);
						} else {
							if (spIndices[i][1] < valLength - 1) {
								val = aa.getValue().substring(spIndices[i][1] );
							}
						}
						perSpeakerText.put(sp, val);
					}
				} else {// no speakers found
					if (multiSpeak) {
						perSpeakerText.put(unknownSpeakerTierName, aa.getValue());// add to the unknown tier
					} else {
						perSpeakerText.put(t.getName(), aa.getValue());// add under the original tier		
					}
				}
				// now translations
				if (hasTranslation) {
					Iterator<String> speakIt = perSpeakerText.keySet().iterator();
					while (speakIt.hasNext()) {
						String key = speakIt.next();
						String value = perSpeakerText.get(key);
						int valLen = value.length();
						
						int[] transIndices = TaSSpecialMarkers.getTranslationIndices(value);
						if (transIndices != null) {
							// should be one index per speaker
							String txt = "";
							String trans = "";
							if (transIndices[0] > 0) {
								txt = value.substring(0, transIndices[0]).trim();
							}
							if (transIndices[0] + TaSSpecialMarkers.TRANS_MARKER.length() < valLen - 1) {
								trans = value.substring(
										transIndices[0] + TaSSpecialMarkers.TRANS_MARKER.length()).trim();
							}
							perSpeakerText.put(key, txt);// concurrent modification ex?
							perSpeakerTrans.put(key, trans);
						}
					}
				}
				// create annotations 
				Iterator<String> speakIt = perSpeakerText.keySet().iterator();
				while (speakIt.hasNext()) {
					String key = speakIt.next();
					String value = perSpeakerText.get(key);
					String trans = perSpeakerTrans.get(key);
					
					if (!multiSpeak) {//replace the original, add a translation
						aa.setValue(value);
						TierImpl tri = transTierMap.get(t);
						long time = (aa.getBeginTimeBoundary() + aa.getEndTimeBoundary()) / 2;
						Annotation ta = tri.createAnnotation(time, time);
						if (ta != null && trans != null) {
							ta.setValue(trans);
						}
					} else {
						//TierImpl baseTier = tr.getTierWithId(key);
						List<TierImpl> tispList = speakerTierMap.get(t);
						if (tispList == null) {
							continue;
						}
						String tname = cleanTierName(key);
						
						for (TierImpl tt : tispList) {
							if (tt.getName().equals(tname)) {
								Annotation ta = tt.createAnnotation(aa.getBeginTimeBoundary(), aa.getEndTimeBoundary());
								if (ta != null && value != null) {
									ta.setValue(value);
								}
								if (hasTranslation) {
									TierImpl tri = transTierMap.get(tt);
									long time = (aa.getBeginTimeBoundary() + aa.getEndTimeBoundary()) / 2;
									Annotation tta = tri.createAnnotation(time, time);
									if (tta != null && trans != null) {
										tta.setValue(trans);
									}
								}
								break;
							}
						}
					}
				}
			}
			processedTiers.add(t);
		}
	}

	/**
	 * Creates and add tiers and tier types based on special markers found in annotations
	 * 
	 * @param tr the transcription everything belongs to
	 * @param tiersWithSpeakers a list of tiers in which special speaker markers have been found
	 * 
	 * @return a map with the original tiers as keys and lists of related/derived speaker tiers as values 
	 */
	private Map<TierImpl, List<TierImpl>> createSpeakerTiers(
			TranscriptionImpl tr, List<TierImpl> tiersWithSpeakers) {
		Map<TierImpl, List<TierImpl>> sptMap = new HashMap<TierImpl, List<TierImpl>>();
		
		for (TierImpl t : tiersWithSpeakers) {
			List<TierImpl> allRelSpeakTiers = new ArrayList<TierImpl>(5);
			sptMap.put(t, allRelSpeakTiers);
			// extract all speaker names
			List<String> uniqNames = new ArrayList<String>();
			uniqNames.add(t.getName() + "@" + unknownSpeakerName);// add unknown
			
			for (AbstractAnnotation aa : t.getAnnotations()) {
				int[][] speakerIndices = TaSSpecialMarkers.getSpeakerIndices(aa.getValue());
				if (speakerIndices != null) {
					for (int[] indices : speakerIndices) {
						String tierName = aa.getValue().substring(indices[0], indices[1]);
						if (!uniqNames.contains(tierName)) {
							uniqNames.add(tierName);
						}
					}
				}
			}
			// now create tiers for all speakers, add speaker "unknown"
			for (String tierName : uniqNames) {
				tierName = cleanTierName(tierName);
				int atIndex = tierName.indexOf('@');
				String tierTypeName = tierName.substring(0, atIndex);
				String participant = tierName.substring(atIndex + 1);
				
				if (tierTypeName == null || tierTypeName.isEmpty()) {
					tierTypeName = t.getName();
				}
				
				if (atIndex == 0) {// starts with @, prepend with type name
					tierName = tierTypeName + tierName;
				}
				
				LinguisticType lt = tr.getLinguisticTypeByName(tierTypeName);
				if (lt == null) {
					lt = new LinguisticType(tierTypeName, t.getLinguisticType());
					tr.addLinguisticType(lt);
				}
				TierImpl spTier = tr.getTierWithId(tierName);
				if (spTier == null) {
					spTier = new TierImpl(tierName, participant, tr, lt);
					spTier.setAnnotator(t.getAnnotator());
					tr.addTier(spTier);
				}
				if (!allRelSpeakTiers.contains(spTier)) {
					allRelSpeakTiers.add(spTier);
				}
			}
		}
		
		return sptMap;
	}

	/**
	 * 
	 * @param tr the transcription
	 * @param tiersWithTrans a list of tiers with at least one annotation with a translation marker
	 * @param speakerTierMap a map with as keys tiers with at least one annotation with a 
	 * speaker / tier indication and as values lists of new tiers derived from the encountered
	 * speaker / tier names
	 * 
	 * @return a map of source tiers to dependent translation tiers
	 */
	private Map<TierImpl, TierImpl> createNewTranslationTiers(
			TranscriptionImpl tr, List<TierImpl> tiersWithTrans,
			Map<TierImpl, List<TierImpl>> speakerTierMap) {
		LinguisticType translationType = tr.getLinguisticTypeByName(translationTypeName);
		if (translationType == null) {
			translationType = new LinguisticType(translationTypeName);
			translationType.addConstraint(new SymbolicAssociation());
			translationType.setTimeAlignable(false);
			tr.addLinguisticType(translationType);
		} else {
			// check if it is of the right kind
			if (translationType.getConstraints().getStereoType() != Constraint.SYMBOLIC_ASSOCIATION) {
				// create a new one
				LinguisticType nextTransType = null;
				int count = 1;
				while(count < 20) {
					String nextName = translationTypeName + "-" + count;
					nextTransType = tr.getLinguisticTypeByName(nextName);
					if (nextTransType != null) {
						if (nextTransType.getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
							break;// use this one
						}
					} else {
						// create and break
						nextTransType = new LinguisticType(nextName);
						nextTransType.addConstraint(new SymbolicAssociation());
						nextTransType.setTimeAlignable(false);
						tr.addLinguisticType(nextTransType);
						break;
					}
					count++;
				}
				translationType = nextTransType;
			} else {
				// we can use this one
			}
		}
		if (translationType == null) {
			// log this error
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Could not find or create a tier type for translation tiers.");
			}
			return null;
		}
		
		Map<TierImpl, TierImpl> tierTransMap = new HashMap<TierImpl, TierImpl>();
		
		for (TierImpl t : tiersWithTrans) {
			List<TierImpl> derivedSpeakTiers = null;
			if (speakerTierMap != null) {
				derivedSpeakTiers = speakerTierMap.get(t);
			}
			boolean derivedTiersExist = 
					(derivedSpeakTiers != null && !derivedSpeakTiers.isEmpty());
			
			if (derivedTiersExist) {
				tierTransMap.put(t, null);// marking that the original tier contains translations
				// create translation tiers for derived tiers
				for (TierImpl derTier : derivedSpeakTiers) {
					String derTierName = getUniqueTierName(tr, derTier.getName(), transSuffix);
					TierImpl derTransTier = new TierImpl(derTier, derTierName, derTier.getParticipant(), tr, translationType);
					derTransTier.setAnnotator(derTier.getAnnotator());
					tr.addTier(derTransTier);
					tierTransMap.put(derTier, derTransTier);
				}
			} else {
				// create a translation tier for this tier if there are derived speaker tiers
				String tierName = getUniqueTierName(tr, t.getName(), transSuffix);
				TierImpl transTier = new TierImpl(t, tierName, t.getParticipant(), tr, translationType);
				transTier.setAnnotator(t.getAnnotator());
				tr.addTier(transTier);
				tierTransMap.put(t, transTier);
			}
		}
		
		return tierTransMap;
	}
	

    /**
     * Creates a unique name for a new tier by adding a numerical suffix.
     *
     * @param transcription the transcription the tier name should be unique for
     * @param name the base name of the tier
     * @param affix the prefix/suffix to add to a tier name, currently used as prefix
     *
     * @return the name for a copy, unique within the transcription
     */
    private String getUniqueTierName(TranscriptionImpl transcription, String name, String affix) {
        String nName = affix + "-" + name;

        if (transcription.getTierWithId(nName) != null) {
            for (int i = 1; i < 50; i++) {
            	String nextName = nName + "-" + i;

                if (transcription.getTierWithId(nextName) == null) {
                    return nextName;
                }
            }
        }

        return nName;
    }

    /**
     * Removes a ":" from the end of a tier name, if it is there.
     * 
     * @param tierName
     * @return tierName or the tier name with the last character removed
     */
    private String cleanTierName(String tierName) {
    	if (tierName.endsWith(":") && tierName.length() > 1) {
    		return tierName.substring(0, tierName.length() - 1);
    	}
    	return tierName;
    }
}

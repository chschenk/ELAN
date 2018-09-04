package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.imports.MergeUtil;
import mpi.eudico.client.annotator.prefs.PreferencesWriter;
import mpi.eudico.client.annotator.timeseries.AbstractTSTrack;
import mpi.eudico.client.annotator.timeseries.TimeSeriesConstants;
import mpi.eudico.client.annotator.timeseries.config.SamplePosition;
import mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration;
import mpi.eudico.client.annotator.timeseries.config.TSTrackConfiguration;
import mpi.eudico.client.annotator.timeseries.io.TSConfigurationEncoder;
import mpi.eudico.client.annotator.timeseries.io.TSConfigurationParser;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.IoUtil;

import org.w3c.dom.Element;

/**
 * A Command to merge two transcriptions and write the result to file.
 * Merging here means that a selected set of tiers with their annotations from one 
 * transcription are added to another transcription. If one or more of the tiers already exist
 * in the destination transcription annotations will either be overwritten or preserved, 
 * whichever the user chooses.
 * It is assumed that the destination transcription may be altered, that the destination 
 * file path has been checked and that, if the file already exists, it may be overwritten.
 */
public class MergeTranscriptionsCommand implements Command, ClientLogger {
    private String commandName;
    private List<ProgressListener> listeners;
    
    private TranscriptionImpl destTrans;
    private TranscriptionImpl srcTrans;
    private String fileName;
    private List<String> selTiers;
    private boolean overwrite;
    private boolean addLinkedFiles;
    private boolean copyAndRenameTiers = false;
    
    /**
     * A command to merge two transcriptions.
     * 
     * @param theName the name of the command
     */
    public MergeTranscriptionsCommand(String theName) {
        commandName = theName;
    }

	/**
	 * Merges two transcriptions. <b>Note: </b>it is assumed the types and
	 * order of the arguments are correct.
	 * 
	 * @param receiver
	 *            the receiving transcription
	 * @param arguments
	 *            the arguments:
	 *            <ul>
	 *            <li>arg[0] = the second transcription (TranscriptionImpl)</li>
	 *            <li>arg[1] = the path to the destination file (String)</li>
	 *            <li>arg[2] = the names of the tiers to add to the destination
	 *            (List&lt;String>)</li>
	 *            <li>arg[3] = a flag to indicate whether existing annotations may be
	 *            overwritten/modified (Boolean)</li>
	 *            <li>arg[4] = a flag to indicate whether linked files from the second source
	 *            should be added to the first (Boolean)</li>
	 *            <li>arg[5] = a flag to indicate that tiers with the same name should not be
	 *            merged but imported as a numbered copy (A_tier-1 etc.) (Boolean)</li>
	 *            </ul>
	 * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object,
	 *      java.lang.Object[])
	 */
    @SuppressWarnings("unchecked")
	@Override
	public void execute(Object receiver, Object[] arguments) {
        destTrans = (TranscriptionImpl) receiver;
        srcTrans = (TranscriptionImpl) arguments[0];
        fileName = (String) arguments[1];
        selTiers = (List<String>) arguments[2];
        overwrite = ((Boolean) arguments[3]).booleanValue();
        addLinkedFiles = ((Boolean) arguments[4]).booleanValue();
        if (arguments.length >= 6) {
        	copyAndRenameTiers = ((Boolean) arguments[5]).booleanValue();
        }
        
        if (destTrans != null) {
            destTrans.setNotifying(false);
        } else {
            progressInterrupt("No first transcription (destination) specified.");
            return;
        }
        
        if (srcTrans == null) {
            progressInterrupt("No second transcription (source) specified");
            return;
        }
        
        if (fileName == null) {
            progressInterrupt("No filename specified");
            return;
        }
        
        if (selTiers == null) {
            progressInterrupt("No tiers specifed");
            return;
        }
        // do the work in a separate thread
        new MergeThread().start();
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
     * Adds a ProgressListener to the list of ProgressListeners.
     *
     * @param pl the new ProgressListener
     */
    public synchronized void addProgressListener(ProgressListener pl) {
        if (listeners == null) {
            listeners = new ArrayList<ProgressListener>(2);
        }

        listeners.add(pl);
    }

    /**
     * Removes the specified ProgressListener from the list of listeners.
     *
     * @param pl the ProgressListener to remove
     */
    public synchronized void removeProgressListener(ProgressListener pl) {
        if ((pl != null) && (listeners != null)) {
            listeners.remove(pl);
        }
    }
    
    /**
     * Notifies any listeners of a progress update.
     *
     * @param percent the new progress percentage, [0 - 100]
     * @param message a descriptive message
     */
    private void progressUpdate(int percent, String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressUpdated(this,
                    percent, message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has completed.
     *
     * @param message a descriptive message
     */
    private void progressComplete(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressCompleted(this,
                    message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has been interrupted.
     *
     * @param message a descriptive message
     */
    private void progressInterrupt(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressInterrupted(this,
                    message);
            }
        }
    }
    
    /**
     * A thread that performs the actual merging.
     */
    class MergeThread extends Thread {
        
        MergeThread() {
            super();
        }
        
        MergeThread(String name) {
            super(name);
        }
        
        @Override
		public void run() {
            try {
	            progressUpdate(5, "Checking tiers to add...");
	            MergeUtil mergeUtil = new MergeUtil();
	            Map<String, String> tierNameMap = null;
	            
	            if (copyAndRenameTiers) {
	            	tierNameMap = mergeUtil.getRenamingTierMap(srcTrans, 
	            			destTrans, selTiers);
	            	//System.out.println(tierNameMap);
	            	Iterator<Entry<String, String>> entryIt = tierNameMap.entrySet().iterator();	            	
	            	while (entryIt.hasNext()) {
	            		Entry<String, String> entry = entryIt.next();
	            		if (!entry.getKey().equals(entry.getValue())) {
	            			// rename the tier in the source transcription
	            			// should be safe, no null test required
	            			srcTrans.getTierWithId(entry.getKey()).setName(entry.getValue());
	            			// replace the tier name in the list of selected tiers
	            			selTiers.set(selTiers.indexOf(entry.getKey()), entry.getValue());
	            			// or
//	            			selTiers.remove(entry.getKey());
//	            			selTiers.add(entry.getValue());
	            		}
	            	}
	            }
	            // list of tiers ( and/or annotations) that can be added
	            List<TierImpl> tiersToAdd = mergeUtil.getAddableTiers(srcTrans, destTrans, selTiers);
	            progressUpdate(10, "Sorting the tiers to add...");
	            // order tiers hierarchically and add the new tiers
	            tiersToAdd = mergeUtil.sortTiers(tiersToAdd);
	            progressUpdate(20, "Creating the tiers. linguistic types, cv's...");
	            mergeUtil.addTiersTypesAndCVs(srcTrans, destTrans, tiersToAdd);
	            //addTiersTypesAndCVs(tiersToAdd);
	            progressUpdate(30, "Adding annotations...");
	            int numIndivTiers = 0;
	            TierImpl t;
	            for (int i =  0; i < tiersToAdd.size(); i++) {
	                t = tiersToAdd.get(i);
	                if (!t.hasParentTier() || !tiersToAdd.contains(t.getParentTier())) {
	                    numIndivTiers++;
	                }
	            }
	            if (numIndivTiers > 0) {	            
		            int progPerTier = 60 / numIndivTiers;
		            int count = 1;
		            for (int i =  0; i < tiersToAdd.size(); i++) {
		                t = tiersToAdd.get(i);
		                if (!t.hasParentTier() || !tiersToAdd.contains(t.getParentTier())) {
		                    addAnnotations(t);
		                    progressUpdate(30 + count * progPerTier, 
		                            "Merging of tier " + t.getName() + " done.");
		                }
		            }
	            }
	            
	            List<TSSourceConfiguration> confList = null;
	            
	            if(addLinkedFiles){
	            	List<MediaDescriptor> mediadescriptors = srcTrans.getMediaDescriptors();
	            	List<MediaDescriptor> destmediaDescriptors = destTrans.getMediaDescriptors();
	            	
	            	for(int i=0; i<mediadescriptors.size(); i++){
	            		if(!destmediaDescriptors.contains(mediadescriptors.get(i))){
	            			destmediaDescriptors.add(mediadescriptors.get(i));	            			
	            		}
	            	}
	            	
	            	List<LinkedFileDescriptor> descriptors = srcTrans.getLinkedFileDescriptors();
	            	List<LinkedFileDescriptor> destDescriptors = destTrans.getLinkedFileDescriptors();
	            	
	            	confList = getTSConfigList(destDescriptors, descriptors);
	            	
	            	for(int i=0; i< descriptors.size(); i++){
	            		if(!destDescriptors.contains(descriptors.get(i))){
	            			destDescriptors.add(descriptors.get(i));	            			
	            		}
	            	}
	            } 	            
//	            else {
//	            	destTrans.getLinkedFileDescriptors().removeAllElements();
//	            }     
	            
	            //ts config file
	        	if(confList != null && confList.size() > 0){
	        		TSConfigEncoder encoder = new TSConfigEncoder();
	        		encoder.encodeAndSave(confList);
	        	}
	        	
	        	//ts config file
	        	/*
	        	if(confList != null && confList.size() > 0){
	        		TSConfigEncoder encoder = new TSConfigEncoder();
	        		encoder.encodeAndSave(confList);
	        	}	        	
	        	*/
	        	
	        	progressUpdate(92, "Saving transcription...");
	            // save the transcription
				int saveAsType = SaveAs27Preferences.saveAsType(destTrans);
	            TranscriptionStore transcriptionStore = ACMTranscriptionStore.getCurrentTranscriptionStore();
	            transcriptionStore.storeTranscription(destTrans, null,
	                new ArrayList<TierImpl>(0), fileName,
	                saveAsType);
	            LOG.info("Transcription saved to " + fileName); 
	            
	            //preference file
	            //copy the first source file pfsx file
	            PreferencesWriter xmlPrefsWriter = new PreferencesWriter();    		
	        	String prefName = fileName.substring(0, fileName.lastIndexOf('.'));
	        	prefName = prefName + ".pfsx";
	        	// merge important elements of the preferences of the source transcription into the preferences
	        	// of the destination
	        	Map/*<String, Map>*/ destPrefs = Preferences.loadPreferencesForFile(destTrans.getFullPath());
	        	Map<String, Object> srcPrefs = Preferences.loadPreferencesForFile(srcTrans.getFullPath());
	        	mergePrefs(destPrefs, srcPrefs);
	        	xmlPrefsWriter.encodeAndSave(destPrefs, prefName); 	        	
	            
	        	// could reverse the changes in the source transcription
	        	if (copyAndRenameTiers && tierNameMap != null) {
	            	Iterator<Entry<String, String>> entryIt = tierNameMap.entrySet().iterator();	            	
	            	while (entryIt.hasNext()) {
	            		Entry<String, String> entry = entryIt.next();
	            		if (!entry.getKey().equals(entry.getValue())) {
	            			// rename the tier in the source transcription
	            			// should be safe, no null test required
	            			srcTrans.getTierWithId(entry.getValue()).setName(entry.getKey());
	            			// replace the tier name in the list of selected tiers
	            			selTiers.set(selTiers.indexOf(entry.getValue()), entry.getKey());
	            			// or
//	            			selTiers.remove(entry.getValue());
//	            			selTiers.add(entry.getKey());
	            		}
	            	}
	        	}
	        	
	            progressComplete("Merging complete");
            } catch (Exception ex) {
                LOG.severe("Error while merging: " + ex.getMessage());
                ex.printStackTrace();
                progressInterrupt("Error while merging: " + ex.getMessage());
            }
        }
        
        private List<TSSourceConfiguration> getTSConfigList(List<LinkedFileDescriptor> firstSrcDescriptors, List<LinkedFileDescriptor> secondSrcDescriptors){
        	List<TSSourceConfiguration> confList = new ArrayList<TSSourceConfiguration>();
        	List<String> trackNamesList = new ArrayList<String>();
        	
        	addConfs(firstSrcDescriptors, trackNamesList, confList);
        	addConfs(secondSrcDescriptors, trackNamesList, confList);
        	
        	return confList;
        }        
        
        private void addConfs(List<LinkedFileDescriptor> descriptors, List<String> trackNamesList, List<TSSourceConfiguration> confList  ){
        	int i = 0;
        	TSSourceConfiguration srcConf = null;
        	TSTrackConfiguration tracConf;
        	List<TSSourceConfiguration> confs;
        	TSConfigurationParser parser = new TSConfigurationParser();
        	while( i < descriptors.size() ){
        		String path = descriptors.get(i).linkURL;
        		if(path.endsWith(TimeSeriesConstants.CONF_SUFFIX)){
        			path = FileUtility.urlToAbsPath(path);
                     if (path.startsWith("file:")) {
                         path = path.substring(5);
                     }
        			
        			confs = parser.parseSourceConfigs(path);
        			if(confs != null && confs.size() > 0){
        				for(int c = 0 ; c< confs.size(); c++){
        					if(confs.get(c) instanceof TSSourceConfiguration){
                				srcConf = (TSSourceConfiguration) confs.get(c);
                				Iterator it = srcConf.objectKeySet().iterator();
                				String newTrackName = null;
                				while(it.hasNext()){
                					String trackName = (String) it.next();
                					tracConf = (TSTrackConfiguration) srcConf.getObject(trackName);
                    				String num;
                    				int n = 0;
                    				int index = trackName.lastIndexOf('-');        
                    				newTrackName = trackName;
                    				
                    				if(trackNamesList.contains(newTrackName)){
                    					if(index > 0 && index <trackName.length()-1){
                    						num = trackName.substring(index+1);
                    						try{
                    							n = Integer.parseInt(num);
                    							n = n+1;
                    						}catch (NumberFormatException ne){
                    							n = 0;
                    						}
                    					} 
                    					newTrackName = trackName.substring(0, index) + "-" + n;
                    					
                        				while(trackNamesList.contains(newTrackName)){
                        					n++;
                        					newTrackName = newTrackName.substring(0, index) + "-" + n;
                        					
                        				}
                    				}        					
                    				
                    				trackNamesList.add(newTrackName);
                    				tracConf.setTrackName(newTrackName);
                    				srcConf.removeObject(trackName);
                    				srcConf.putObject(newTrackName, tracConf);                    				
                				}
                				confList.add(srcConf);
        					}
        				}
        				descriptors.remove(i); 
        			}  			
        		} else {
        			i++;
        		}
        	}
        }
        	                
        private void addAnnotations(TierImpl tier) {
            Tier parent = tier.getParentTier();
            TierImpl destTier = destTrans.getTierWithId(tier.getName());
            
            if (destTier == null) {
                LOG.warning("Destination tier " + tier.getName() + " not found in destination description");
                return;
            }

            DefaultMutableTreeNode recordNode = null;
            
            if (parent != null) {
                LinguisticType lt = tier.getLinguisticType();
                if (lt.getConstraints() == null) {
                    LOG.warning("Error: illegal type for tier: " + tier.getName());
                    return; 
                }
                if (lt.getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
                    List<AbstractAnnotation> annotations = tier.getAnnotations();
                    for (AbstractAnnotation ann : annotations) {
                    	List<Annotation> overlapAnn = destTier.getOverlappingAnnotations(ann.getBeginTimeBoundary(),
                                ann.getEndTimeBoundary());
                    	if (overlapAnn.size() > 0) {
                    		if (overwrite) {
                                recordNode = AnnotationRecreator.createTreeForAnnotation(ann);
                                for (Annotation a : overlapAnn) {
                                	destTier.removeAnnotation(a);
                                }
                                AnnotationRecreator.createAnnotationFromTree(destTrans,
                                        recordNode);
                    		}// else skip this annotation
                    		
                    	} else {// no overlapping annotations, always create
                            recordNode = AnnotationRecreator.createTreeForAnnotation(ann);
                            AnnotationRecreator.createAnnotationFromTree(destTrans,
                                    recordNode);
                    	}
                    }                    
                } else {
                    // subdivision, copy groupwise
                    ArrayList<DefaultMutableTreeNode> group = new ArrayList<DefaultMutableTreeNode>();
                    TierImpl rootTier = tier.getRootTier();
                    Annotation curParent = null;
                    List<AbstractAnnotation> annotations = tier.getAnnotations();
                    Set<Annotation> existingAnnos = new TreeSet<Annotation>();
                    
                    for (AbstractAnnotation ann : annotations) {
                    	List<Annotation> overlapAnn = destTier.getOverlappingAnnotations(ann.getBeginTimeBoundary(),
                                ann.getEndTimeBoundary());
                    	
                        int numOverlap = overlapAnn.size();
                        if (curParent == null) {
                        	existingAnnos.addAll(overlapAnn);
                            // add to first group
                            if (overwrite || numOverlap == 0) {
                                group.add(AnnotationRecreator.createTreeForAnnotation(
                                        ann));
                            }

                            curParent = rootTier.getAnnotationAtTime(ann.getBeginTimeBoundary());
                        } else if (rootTier.getAnnotationAtTime(
                                    ann.getBeginTimeBoundary()) == curParent) {
                        	existingAnnos.addAll(overlapAnn);
                            // add to current group
                            if (overwrite || numOverlap == 0) {
                                group.add(AnnotationRecreator.createTreeForAnnotation(
                                        ann));
                            }
                        } else {
                            // finish group
                            if (group.size() > 0) {
                            	if (overwrite && lt.getConstraints().getStereoType() == 
                            			Constraint.SYMBOLIC_SUBDIVISION && !existingAnnos.isEmpty()) {
                            		for (Annotation a : existingAnnos) {
                            			destTier.removeAnnotation(a);
                            		}
                            	}
                                AnnotationRecreator.createAnnotationsSequentially(destTrans, group);
                            }
                    		existingAnnos.clear();
                            group = new ArrayList<DefaultMutableTreeNode>();
                            curParent = rootTier.getAnnotationAtTime(ann.getBeginTimeBoundary());
                            existingAnnos.addAll(overlapAnn);
                            // add to new group
                            if (overwrite || numOverlap == 0) {
                                group.add(AnnotationRecreator.createTreeForAnnotation(
                                        ann));
                            }
                        }
                    }
                    // add the last group...
                    if (group.size() > 0) {
                    	if (overwrite && lt.getConstraints().getStereoType() == Constraint.SYMBOLIC_SUBDIVISION &&
                    			!existingAnnos.isEmpty()) {
                    		for (Annotation a : existingAnnos) {
                    			destTier.removeAnnotation(a);
                    		}
                    		existingAnnos.clear();
                    	}
                        AnnotationRecreator.createAnnotationsSequentially(destTrans, group);
                    }
                }
                
            } else {
                // no parent tier
                List<AbstractAnnotation> annotations = tier.getAnnotations();
                for (AbstractAnnotation ann : annotations) {
                    if (overwrite || destTier.getOverlappingAnnotations(ann.getBeginTimeBoundary(), 
                            ann.getEndTimeBoundary()).size() == 0) {
                        recordNode = AnnotationRecreator.createTreeForAnnotation(ann);
                        AnnotationRecreator.createAnnotationFromTree(destTrans,
                                recordNode);
                    }
                }
            }
            LOG.info("Merging of tier " + tier.getName() + " done.");
        }
        
        /**
         * Copies some preferences from the second file to the destination preferences.
         * This probably should be moved to a utility class. 
         * 
         * @param destPrefs the receiving map of a set of preferences
         * @param srcPrefs the originating preferences map, the preferences of the second transcription
         */
        private void mergePrefs(Map<String, Map> destPrefs, Map srcPrefs) {
    	    if (destPrefs == null) {
    	    	return;//log?
    	    }
    	    if (srcPrefs == null) {
    	    	return;//log?
    	    }
    	    // copy tier font preferences
    	    final String TIER_FONTS = "TierFonts";
    	    Object srcPrefMap = srcPrefs.get(TIER_FONTS);
    	    Object destPrefMap = destPrefs.get(TIER_FONTS);
    	    
    	    if(srcPrefMap instanceof Map) {
    	    	Map<String, Object> srcFontsMap = (Map<String, Object>) srcPrefMap;
    	    	Map<String, Object> destFontMap = null;
    	    	
    	    	if (destPrefMap instanceof Map) {
    	    		destFontMap = (Map<String, Object>) destPrefMap;
    	    	} else {
    	    		destFontMap = new HashMap<String, Object>();
    	    		destPrefs.put(TIER_FONTS, destFontMap);
    	    	}
    	    	
    	    	for (String key : srcFontsMap.keySet()) {
    	    		if (selTiers == null || selTiers.contains(key)) {// only add if the tier has been copied
	    	    		if (!destFontMap.containsKey(key)) {// only add if it isn't there yet
	    	    			destFontMap.put(key, srcFontsMap.get(key));
	    	    		}
    	    		}
    	    	}
    	    }
    	    // tier colors
    	    final String TIER_COLORS = "TierColors";
    	    srcPrefMap = srcPrefs.get(TIER_COLORS);
    	    destPrefMap = destPrefs.get(TIER_COLORS);
    	    
    	    if(srcPrefMap instanceof Map) {
    	    	Map<String, Object> srcColMap = (Map<String, Object>) srcPrefMap;
    	    	Map<String, Object> destColMap = null;
    	    	
    	    	if (destPrefMap instanceof Map) {
    	    		destColMap = (Map<String, Object>) destPrefMap;
    	    	} else {
    	    		destColMap = new HashMap<String, Object>();
    	    		destPrefs.put(TIER_COLORS, destColMap);
    	    	}
    	    	
    	    	for (String key : srcColMap.keySet()) {
    	    		if (selTiers == null || selTiers.contains(key)) {// only add if the tier has been copied
	    	    		if (!destColMap.containsKey(key)) {// only add if it isn't there yet
	    	    			destColMap.put(key, srcColMap.get(key));
	    	    		}
    	    		}
    	    	}
    	    }
    	    // tier Highlights
    	    final String TIER_HIGH = "TierHighlightColors";
    	    srcPrefMap = srcPrefs.get(TIER_HIGH);
    	    destPrefMap = destPrefs.get(TIER_HIGH);
    	    
    	    if(srcPrefMap instanceof Map) {
    	    	Map<String, Object> srcHighMap = (Map<String, Object>) srcPrefMap;
    	    	Map<String, Object> destHighMap = null;
    	    	
    	    	if (destPrefMap instanceof Map) {
    	    		destHighMap = (Map<String, Object>) destPrefMap;
    	    	} else {
    	    		destHighMap = new HashMap<String, Object>();
    	    		destPrefs.put(TIER_HIGH, destHighMap);
    	    	}
    	    	
    	    	for (String key : srcHighMap.keySet()) {
    	    		if (selTiers == null || selTiers.contains(key)) {// only add if the tier has been copied
	    	    		if (!destHighMap.containsKey(key)) {// only add if it isn't there yet
	    	    			destHighMap.put(key, srcHighMap.get(key));
	    	    		}
    	    		}
    	    	}
    	    }
    	    
    	    // CV preferences
    	    final String CV_PREFS = Preferences.CV_PREFS;
    	    srcPrefMap = srcPrefs.get(CV_PREFS);
    	    destPrefMap = destPrefs.get(CV_PREFS);
    	    
    	    if(srcPrefMap instanceof Map) {
    	    	@SuppressWarnings("unchecked")
				Map<String, Object> srcCVMap = (Map<String, Object>) srcPrefMap;
    	    	Map<String, Object> destCVMap = null;
    	    	
    	    	if (destPrefMap instanceof Map) {
    	    		destCVMap = (Map<String, Object>) destPrefMap;
    	    	} else {// or add the source map itself?
    	    		destCVMap = new HashMap<String, Object>();
    	    		destPrefs.put(CV_PREFS, destCVMap);
    	    	}
    	    	// iterator for Controlled Vocabularies
    	    	//Iterator cvIt = srcCVMap.keySet().iterator();
    	    	//Object key;

    	    	for (String key : srcCVMap.keySet()) {
    	    	//while (cvIt.hasNext()) {
    	    		//key = cvIt.next();
    	    		// check if the CV is actually in the destination?
    	    		if (destTrans.getControlledVocabulary(key) == null) {
    	    			continue;
    	    		}
    	    		
    	    		if (!destCVMap.containsKey(key)) {
    	    			// add the complete map if it isn't there yet
    	    			destCVMap.put(key, srcCVMap.get(key));
    	    		} else {// TODO with the merging of transcriptions currently CV's are not merged when they have the same name 
    	    			Object curDestCVObject = destCVMap.get(key);
    	    			Object curSrcCVObject = srcCVMap.get(key);
    	    			
    	    			if (curDestCVObject instanceof Map && curSrcCVObject instanceof Map) {
    	    				@SuppressWarnings("unchecked")
							Map<String, Object> curSrcCV = (Map<String, Object>) curSrcCVObject;
    	    				@SuppressWarnings("unchecked")
							Map<String, Object> curDestCV = (Map<String, Object>) curDestCVObject;
    	    				
    	        	    	for (String entryKey : curSrcCV.keySet()) {
    	    					
    	    					if (!curDestCV.containsKey(entryKey)) {
    	    						// only add an entry key value pair if it isn't there
    	    						curDestCV.put(entryKey, curSrcCV.get(entryKey));
    	    					}
    	    				}
    	    			}
    	    		}
    	    	}
    	    	
    	    }
        }
        
    } // end of MergeThread    
    
    //
    private class TSConfigEncoder extends TSConfigurationEncoder{
    	
    	public TSConfigEncoder(){
    		super();
    	}
    	
    	/**
         * Creates a DOM tree and saves the file.
         *
         * @param transcription the Transcription, used for creation of file path
         * @param tsConfigs a collection of configuration objects of all linked
         *        timeseries sources
         */
        public void encodeAndSave(Collection<TSSourceConfiguration> tsConfigs) {

           configFile = createPath(fileName);
            
           doc = createNewDocument();

            if (doc != null) {
                Element docElement = createDOM(tsConfigs);

                try {
                    IoUtil.writeEncodedFile("UTF-8", configFile.substring(5),
                        docElement);
                    LOG.info("Configuration file saved: " + configFile);

                    // optionally add config file as a linked file to the transcription
                    LinkedFileDescriptor lfd;

                    for (int i = 0;
                            i < destTrans.getLinkedFileDescriptors().size();
                            i++) {
                        lfd = destTrans.getLinkedFileDescriptors().get(i);

                        //if (lfd.linkURL.equals(configFile)) {
                        if (lfd.linkURL.toLowerCase().endsWith(TimeSeriesConstants.CONF_SUFFIX)) {
                            return;
                        }
                    }

                    // create and add a new descriptor if we get here
                    lfd = new LinkedFileDescriptor(configFile, LinkedFileDescriptor.XML_TYPE);
                    destTrans.getLinkedFileDescriptors().add(lfd);
                } catch (Exception e) {
                    LOG.warning("Could not save configuration file: " + e.getMessage());
                }
            }
        }
        
        @Override
		protected Element createTrackElement(TSTrackConfiguration trConfig) {
            if ((trConfig == null) || (trConfig.getTrackName() == null)) {
                return null;
            }

            AbstractTSTrack track;
            Element trackElem = doc.createElement(TimeSeriesConstants.TRACK);
            trackElem.setAttribute(TimeSeriesConstants.NAME, trConfig.getTrackName());

            // properties
            Enumeration propEnum = trConfig.propertyNames();
            String prop;
            String val;
            Element propElem;
            
            String minVal = null;
            String maxVal = null;

            while (propEnum.hasMoreElements()) {
                prop = (String) propEnum.nextElement();
                val = trConfig.getProperty(prop);
               
                if(prop.equals(TimeSeriesConstants.DERIVATION)){
                	trackElem.setAttribute(TimeSeriesConstants.DERIVATION , val);
                }                
                else if(prop.equals(TimeSeriesConstants.DESC)){
                	Element descElem = doc.createElement(TimeSeriesConstants.DESC);
                    descElem.appendChild(doc.createTextNode(val));
                    trackElem.appendChild(descElem);
                }                
                else if(prop.equals(TimeSeriesConstants.UNITS)){
                	Element unitElem = doc.createElement(TimeSeriesConstants.UNITS);
                    unitElem.appendChild(doc.createTextNode(val));
                    trackElem.appendChild(unitElem);
                }                
                else if(prop.equals(TimeSeriesConstants.MIN)){
                	minVal = val;
                }                 
                else if(prop.equals(TimeSeriesConstants.MAX)){
                	maxVal = val;
                } else if(prop.equals(TimeSeriesConstants.COLOR)){
                	Element colElem = doc.createElement(TimeSeriesConstants.COLOR);                    
                    colElem.appendChild(doc.createTextNode(val));
                    trackElem.appendChild(colElem);
                } else {
                	propElem = createPropertyElement(prop, val);

                    if (propElem != null) {
                        trackElem.appendChild(propElem);
                    }
                }
            }
            
            if (minVal != null && maxVal != null) {
                Element rangeElem = doc.createElement(TimeSeriesConstants.RANGE);
                rangeElem.setAttribute(TimeSeriesConstants.MIN, minVal);
                rangeElem.setAttribute(TimeSeriesConstants.MAX, maxVal);
                trackElem.appendChild(rangeElem);
            }

            SamplePosition spos = trConfig.getSamplePos();

            if (spos != null) {
                Element spElem = doc.createElement(TimeSeriesConstants.POSITION);

                if (spos.getDescription() != null) {
                    Element ds = doc.createElement(TimeSeriesConstants.DESC);
                    ds.appendChild(doc.createTextNode(spos.getDescription()));
                    spElem.appendChild(ds);
                }

                for (int i = 0; i < spos.getRows().length; i++) {
                    Element pos = doc.createElement(TimeSeriesConstants.SAMPLE_POS);
                    pos.setAttribute(TimeSeriesConstants.ROW,
                        String.valueOf(spos.getRows()[i]));
                    pos.setAttribute(TimeSeriesConstants.COL,
                        String.valueOf(spos.getColumns()[i]));
                    spElem.appendChild(pos);
                }

                trackElem.appendChild(spElem);
            }
            
            return trackElem;
        }
    }
}

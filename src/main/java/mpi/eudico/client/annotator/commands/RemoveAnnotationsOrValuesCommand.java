package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.AnnotationValuesRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Removes all annotations or annotation values from the 
 * selected tiers. Or removes only the annotation which matches
 * with the given value in the selected tiers.
 *
 * @author Aarthy Somasundaram
 * @version Oct 20,2010
 */
public class RemoveAnnotationsOrValuesCommand implements UndoableCommand, ClientLogger {
    private String commandName;
    private TranscriptionImpl transcription;
    List<String> tierNames;
    boolean annotations;
    boolean annotationValues;
    boolean allAnnotations ;
    boolean annotationWithVal;
    String value;
    private ArrayList<AnnotationValuesRecord> records;
    private HashMap<String, List<DefaultMutableTreeNode>> delAnnRecordsMap;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public RemoveAnnotationsOrValuesCommand(String name) {
        commandName = name;
    }

    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#undo()
     */
    @Override
	public void undo() {
    	setWaitCursor(true);
        transcription.setNotifying(false); 
        
        if(annotationValues){

        	String name = null;
        	TierImpl tier = null;

        	if (tierNames != null) {
        		if (records != null) {
        			AbstractAnnotation ann = null;
        			AnnotationValuesRecord record = null;

        			for (int i = 0; i < records.size(); i++) {
        				record = records.get(i);

        				name = record.getTierName();

        				if ((tier == null) || !tier.getName().equals(name)) {
        					tier = transcription.getTierWithId(name);
        				}

        				if (tier != null) {
        					ann = (AbstractAnnotation) tier.getAnnotationAtTime(record.getBeginTime());

        					if ((ann != null) &&
        							(ann.getEndTimeBoundary() == record.getEndTime())) {
        						restoreValueAndRefs(ann, record);
        					} else {
        						LOG.warning(
        							"The annotation could not be found for undo");
        					}
        				} else {
        					LOG.warning("The tier could not be found: " + name);
        				}
        			}
        		} else {
        			LOG.info("No annotation records have been stored for undo.");
        		}
        	} else {
            LOG.warning("No tier names have been stored.");
        	}
        } else if(annotations){
        	
    		if ((transcription != null) && (delAnnRecordsMap != null)) {
    			
    	        List<DefaultMutableTreeNode> delAnnRecords =  new  ArrayList<DefaultMutableTreeNode>();    	        
    	        String name = null;
    	        
    	        for (int i = 0; i < tierNames.size(); i++) {      		 
    	             name = tierNames.get(i);
    	             delAnnRecords = delAnnRecordsMap.get(name);
    	             if(delAnnRecords == null){
    	            	 continue;
    	             }
            	     
    	             if (name != null) {
            	    	TierImpl tier = transcription.getTierWithId(name);
    	
    	            if (tier == null) {
    	                ClientLogger.LOG.warning("The tier could not be found: " +
    	                    name);
    	
    	                return;
    	            }
    	
    	            int curPropMode = 0;
    	
    	            curPropMode = transcription.getTimeChangePropagationMode();
    	
    	            if (curPropMode != Transcription.NORMAL) {
    	                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
    	            }
    	            transcription.setNotifying(false);
    	            setWaitCursor(true);
    	            
    	            if(allAnnotations){
    	            	if (!tier.hasParentTier()) {
    	            		for (DefaultMutableTreeNode n : delAnnRecords) {
    	            			AnnotationRecreator.createAnnotationFromTree(transcription,
    	            					n, true);
    	            		}
    	            	} 
    	            	// if the parent tier is already selected, then that will take care the depend
    	            	else if(!tierNames.contains(tier.getParentTier().getName()) ){
    	            		AnnotationRecreator.createAnnotationsSequentially(transcription,
    	            				delAnnRecords, true);
    	            	}
    	            } else if(annotationWithVal){
    	            	for (DefaultMutableTreeNode n : delAnnRecords) {
	            			AnnotationRecreator.createAnnotationFromTree(transcription,
	            					n, true);
    	            	} 
    	            }
    	            
    	            // restore the time propagation mode
    	            transcription.setTimeChangePropagationMode(curPropMode);
            	    }
    	        }
    		}
    	}
    	
    	transcription.setNotifying(true);
		setWaitCursor(false);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#redo()
     */
    @Override
	public void redo() {
    	setWaitCursor(true);
		transcription.setNotifying(false);
		
    	if(annotationValues){
    		String name = null;
    		TierImpl tier = null;

    		if (tierNames != null) {
    			if (records != null) {
    				AbstractAnnotation ann = null;
    				AnnotationValuesRecord record = null;

    				for (int i = 0; i < records.size(); i++) {
    					record = records.get(i);
    					name = record.getTierName();

    					if ((tier == null) || !tier.getName().equals(name)) {
    						tier = transcription.getTierWithId(name);
    					}

    					if (tier != null) {
    						ann = (AbstractAnnotation) tier.getAnnotationAtTime(record.getBeginTime());

    						if ((ann != null) &&
    								(ann.getEndTimeBoundary() == record.getEndTime())) {
    							removeValueAndRefs(ann);
    						} else {
    							LOG.warning("The annotation could not be found for redo");
    						}
    					} else {
    						LOG.warning("Could not find tier for redo: " + name);
    					}
    				}
    			} else {
    				LOG.info("No annotation records have been stored for undo.");
    			}
    		} else {
    			LOG.warning("No tier names have been stored.");
    		}
    	}else if(annotations){
    		deleteAnnotations();
    	}

    	transcription.setNotifying(true);
        setWaitCursor(false);     
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are correct.<br>
     * If arg[2] is null it is assumed that there is no number part.
     *
     * @param receiver the TranscriptionImpl
     * @param arguments the arguments: <ul>
     * 		  	<li>arg[0] = the selected tiers (ArrayList)</li> 
     * 			<li>arg[1] = remove annotations (Boolean)</li> 
     * 			<li>arg[2] = remove annotation values (Boolean)</li> 
     * 			<li>arg[3] = remove all annotations (Boolean)</li> 
     * 			<li>arg[4] = remove annotation with value (Boolean)</li> 
     * 			<li>arg[5] = value to match the annotations (String) (null when arg[3] is true)</li> 
     *      </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        tierNames = (List) arguments[0];       
        annotations = (Boolean) arguments[1];
        annotationValues = (Boolean) arguments[2];
        allAnnotations = (Boolean) arguments[3];
        annotationWithVal = (Boolean) arguments[4];
        value = (String) arguments[5];
              
        if ((tierNames == null) || (tierNames.size() == 0)) {
            LOG.warning("No tier selected.");
            transcription.setNotifying(true);
            setWaitCursor(false);

            return;
        } 
        if(annotationWithVal){
        	// HS Aug 2013 allow value to be the empty string in case of removal of annotations
        	if(value == null || (value.length() == 0 && annotationValues) ){
        		LOG.warning("Search value is empty.");
                transcription.setNotifying(true);
                setWaitCursor(false);
                
                return;
        	}
        }

      if(annotations){
    	  deleteAnnotations();
    	  
      }else if(annotationValues){
    	  deleteAnnotationValues();
      
      }
    }
    
     /**
     * Gets all the annotations to be deleted/removed
     *
     * @return hashMap with the tier and the list of 
     * 		annotations in that tier to be deleted
     */    
    private HashMap<String, List<AbstractAnnotation>> getAnnotationsToBeDeleted(){
    	
    	TierImpl tier = null;
        List<AbstractAnnotation> anns;
        HashMap<String, List<AbstractAnnotation>> annsMap = new  HashMap<String, List<AbstractAnnotation>>();
        String name;
        
        if(allAnnotations){
       	 
       	 for (int i = 0; i < tierNames.size(); i++) {
       		    anns = new ArrayList<AbstractAnnotation>();
                name = tierNames.get(i);
                tier = transcription.getTierWithId(name);
                
                if (tier != null) {
               	 	anns.addAll(tier.getAnnotations());
                } else {
               	 LOG.warning("The tier " + name + " does not exist.");
                }
                annsMap.put(name, anns);                
       	 	}
        } 
        
        if(annotationWithVal){    
       	 tier = null;       	 
       	 name =null;
       	 
       	 for (int i = 0; i < tierNames.size(); i++) {
       		anns = new ArrayList<AbstractAnnotation>();
                name = tierNames.get(i);
                tier = transcription.getTierWithId(name);
                
           	 if (tier != null) {
           		 List<AbstractAnnotation> annotations = tier.getAnnotations();
           		 for (AbstractAnnotation annotation : annotations) {
           			 if(annotation.getValue().compareToIgnoreCase(value) == 0){
           				 anns.add(annotation);
           			 }
           		 } 		 
                } else {
                    LOG.warning("The tier " + name + " does not exist.");
                }
           	annsMap.put(name, anns);
            }
        }
        return annsMap;
    }

    /**
     * Actual deletion of annotations
     *     
     */    
    private void deleteAnnotations() {
    	setWaitCursor(true);
        transcription.setNotifying(false);

        
        delAnnRecordsMap = new HashMap<String, List<DefaultMutableTreeNode>>();
       
        List<AbstractAnnotation> anns =  new  ArrayList<AbstractAnnotation>();  
        
        List<DefaultMutableTreeNode> delAnnRecords =  new  ArrayList<DefaultMutableTreeNode>();
        
        HashMap<String, List<AbstractAnnotation>> annsMap = getAnnotationsToBeDeleted(); 
        String name = null;
        AnnotationValuesRecord record = null;
        
        if(allAnnotations){
        	for (int i = 0; i < tierNames.size(); i++) {      		 
        		name = tierNames.get(i);
        		anns = annsMap.get(name);
        		TierImpl tier = transcription.getTierWithId(name);
            
        		//if a dependent tier is selected, then its stores the parent tier for undo and redo
        		if(tier.hasParentTier() && !tierNames.contains(tier.getParentTier().getName()) ){
            	 tier = transcription.getTierWithId(name);
            	 List<AbstractAnnotation> annotations = tier.getParentTier().getAnnotations();
            	 for (AbstractAnnotation aa : annotations) {
            		 delAnnRecords.add( AnnotationRecreator.createTreeForAnnotation(aa));
            	 }
             	}
        		if(anns != null && anns.size() > 0){
        			for (AbstractAnnotation ann : anns){  
        				tier = (TierImpl)ann.getTier();            		 
            		 
        				record = new AnnotationValuesRecord(ann);
        				ann = (AbstractAnnotation) tier.getAnnotationAtTime((record.getBeginTime() +
        						record.getEndTime()) / 2); 	                 
 	                
        				if(ann != null){
        					// if dependent tiers are selected, the respective parent tier is already stored. 
        					// so only if a parent tier is selected, this executes and stores the annotation
        					if(!tier.hasParentTier()){
        						delAnnRecords.add( AnnotationRecreator.createTreeForAnnotation(ann)); 	
        					}
 	                		 tier.removeAnnotation(ann);
        				}
        			}
        			delAnnRecordsMap.put(name, delAnnRecords);
        		}
        	}
        } else if(annotationWithVal)   {
        	for (int i = 0; i < tierNames.size(); i++) {      		 
        		name = tierNames.get(i);
        		anns = annsMap.get(name);
        		TierImpl tier = transcription.getTierWithId(name);            
        		
        		if(anns != null && anns.size() > 0){
        			for (AbstractAnnotation ann : anns){  
        				tier = (TierImpl)ann.getTier();            		 
            		 
        				record = new AnnotationValuesRecord(ann);
        				ann = (AbstractAnnotation) tier.getAnnotationAtTime((record.getBeginTime() +
        						record.getEndTime()) / 2); 	                 
 	                
        				if(ann != null){        					
 	                		 delAnnRecords.add( AnnotationRecreator.createTreeForAnnotation(ann)); 	                	
 	                		 tier.removeAnnotation(ann);
        				}
        			}
        			delAnnRecordsMap.put(name, delAnnRecords);
        		}
        	}
        	
        }

        transcription.setNotifying(true);
        setWaitCursor(false);
    	
    }
        
    /**
     * Actual deletion of annotations values
     *     
     */  
    private void deleteAnnotationValues() {
    	setWaitCursor(true);
        transcription.setNotifying(false);
    	
    	
    	TierImpl tier = null;
        List<AbstractAnnotation> anns = new ArrayList<AbstractAnnotation>();;
        String name;
        
        if(allAnnotations){
        	for (int i = 0; i < tierNames.size(); i++) {        		
                name = tierNames.get(i);
                tier = transcription.getTierWithId(name);
                
                if (tier != null) {
               	 	anns.addAll(tier.getAnnotations());
               	 
                } else {
               	 LOG.warning("The tier " + name + " does not exist.");
                }                        
       	 	}
        }else if(annotationWithVal){  
        	for (int i = 0; i < tierNames.size(); i++) {        		
        		name = tierNames.get(i);
        		tier = transcription.getTierWithId(name);
                
           	 	if (tier != null) {
           	 		List<AbstractAnnotation> annotations = tier.getAnnotations();
           	 		for (AbstractAnnotation annotation : annotations) {
           			 
           	 			if(annotation.getValue().compareToIgnoreCase(value) == 0){
           	 				anns.add(annotation);
           	 			}
           	 		}		 
                } else {
                    LOG.warning("The tier " + name + " does not exist.");
                }
        	}
        }
        
        records = new ArrayList<AnnotationValuesRecord>(anns.size());

        AbstractAnnotation ann = null;       
        AnnotationValuesRecord record = null;

        for (int i = 0; i < anns.size(); i++) {
            ann = anns.get(i);            
            record = new AnnotationValuesRecord(ann);           
            records.add(record);
            removeValueAndRefs(ann);
        }

        transcription.setNotifying(true);
        setWaitCursor(false);
	}
    
    /**
     * Currently removes all external references. It is unclear whether this
     * is the best approach in all cases. It seems all right for content related
     * references (CV entry, lexical entry, ISOcat DC), for resource url's it
     * might be different  
     * 
     * @param ann the annotation to remove the value from including 
     * external references 
     */
    private void removeValueAndRefs(AbstractAnnotation ann) {
    	ann.setCVEntryId(null);
    	ann.setExtRef(null);
    	
    	ann.setValue("");
    }
    
    /**
     * 
     * @param ann the annotation to restore value and external references for
     * 
     * @param record the record containing the information to restore
     */
    private void restoreValueAndRefs(AbstractAnnotation ann, AnnotationDataRecord record) {
    	ann.setCVEntryId(record.getCvEntryId());
    	ann.setExtRef(record.getExtRef());
    	
    	ann.setValue(record.getValue());
    }
   
    /**
     * Changes the cursor to either a 'busy' cursor or the default cursor.
     *
     * @param showWaitCursor when <code>true</code> show the 'busy' cursor
     */
    private void setWaitCursor(boolean showWaitCursor) {
        if (showWaitCursor) {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getPredefinedCursor(
                    Cursor.WAIT_CURSOR));
        } else {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    @Override
	public String getName() {
        return commandName;
    }
}

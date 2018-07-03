package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A command that creates empty annotations or creates annotations with values of
 * parent tiers , on the selected dependent tiers 
 *
 * Created on Oct 26, 2010
 * @author Aarthy Somasundaram
 * @version Oct 26,2010
 */
public class CreateAnnsOnDependentTiersCommand implements UndoableCommand, ClientLogger {
	 private TranscriptionImpl transcription;
	 private String commandName;
	 private List<String> emptyAnnTiers;	
	 private List<String> annWithValTiers;	
	 private boolean overWrite;	
	 
	 private HashMap<String, List<AnnotationDataRecord>> createdAnnos;	
	 private HashMap<String, List<AnnotationDataRecord>> editedAnnos;

	/**
     * Constructor.
     *
     * @param name the name of the command
     */
    public CreateAnnsOnDependentTiersCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are correct.<br>
     *
     * @param receiver the TranscriptionImpl
     * @param arguments the arguments: 
     * 		<ul>
     * 		  	<li>arg[0] = the tiers on which empty annotations should be created (ArrayList)</li>      			
     * 			<li>arg[1] = the tiers on which annotations along the values should be created (ArrayList)</li>      
     * <		<li>arg[2] = If true, overwrite the annotation values (Boolean)</li>     			
     *      </ul>
     */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		transcription = (TranscriptionImpl) receiver;        
        emptyAnnTiers = (List<String>) arguments[0];  
        annWithValTiers = (List<String>) arguments[1]; 
        overWrite = (Boolean) arguments[2];
        
        createdAnnos = new HashMap<String, List<AnnotationDataRecord>>();
        editedAnnos = new HashMap<String, List<AnnotationDataRecord>>();
        if(emptyAnnTiers != null && emptyAnnTiers.size() > 0){
        	createEmptyAnnotations();
        }
        if(annWithValTiers != null && annWithValTiers.size() > 0){
        	createAnnotationsWithValue();
        }
	}
	
	/**
     * Iterates over the annotations of the parent tier and creates 
     * empty annotation on the selected dependent tiers     
     */
	private void createEmptyAnnotations() {
		
		setWaitCursor(true);
        transcription.setNotifying(false);
        
        int curPropMode = transcription.getTimeChangePropagationMode();
        
        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);    
        }        
        
        List<TierImpl> tierList = new ArrayList<TierImpl>();
		for (int i = 0; i < emptyAnnTiers.size(); i++) {
        	TierImpl tier = (TierImpl) transcription.getTierWithId(emptyAnnTiers.get(i)); 
        		
        	//check if tier is valid
            if (tier == null) {
                ClientLogger.LOG.severe(emptyAnnTiers.get(i) + "was not found");
                
                return;
             }
            tierList.add(tier);  
		}
		tierList = sortTiers(tierList);        
        
		for (TierImpl tier : tierList) {
            TierImpl parentTier = (TierImpl) tier.getParentTier();            
            List<AbstractAnnotation> annotations = parentTier.getAnnotations(); 
            
            List<AnnotationDataRecord> records = new ArrayList<AnnotationDataRecord>();
            
            for (AbstractAnnotation parentAnn : annotations) {
            	Annotation aa = null;
                if (tier.getAnnotationAtTime(parentAnn.getBeginTimeBoundary()) == null) {
                	if (tier.isTimeAlignable()) {
                		/*
                		 * If we insist in creating the annotations, even when the parent
                		 * isn't fully time-aligned, we could call
                		if (parentAnn instanceof AlignableAnnotation) {
                			((AlignableAnnotation) parentAnn).makeTimeAligned();
                		}
                		 * but that would change other tiers than the target.
                		 * Not to mention the extra work for undo.
                		 */
                		aa = tier.createAnnotation(parentAnn.getBeginTimeBoundary(), parentAnn.getEndTimeBoundary());
                    } else {
                    	long time = (parentAnn.getBeginTimeBoundary() + parentAnn.getEndTimeBoundary()) / 2;
                        aa = tier.createAnnotation(time, time);
                    }
                }                 
                if(aa != null){
                     records.add(new AnnotationDataRecord(aa));
                }
            }
            if(records.size() >0){
            	createdAnnos.put(tier.getName(), records);
            }
        }
        transcription.setNotifying(true);
        setWaitCursor(false);
	}

	/**
     * Iterates over the annotations of the parent tier and creates 
     * annotation with values on the selected dependent tiers     
     */
	private void createAnnotationsWithValue() {
		setWaitCursor(true);
        transcription.setNotifying(false);
        
        int curPropMode = transcription.getTimeChangePropagationMode();
        
        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }
        
        List<TierImpl> tierList = new ArrayList<TierImpl>();
		for (int i = 0; i < annWithValTiers.size(); i++) {
        	TierImpl tier = (TierImpl) transcription.getTierWithId(annWithValTiers.get(i)); 
        		
        	//check if tier is valid
            if (tier == null) {
                ClientLogger.LOG.severe(annWithValTiers.get(i) + "was not found");
                
                return;
             }
            tierList.add(tier);  
		}
		tierList = sortTiers(tierList);
        
        for (int i = 0; i < tierList.size(); i++) {        	
        	TierImpl tier = tierList.get(i); 
            TierImpl parentTier = (TierImpl) tier.getParentTier();            
            List<AbstractAnnotation> annotations = parentTier.getAnnotations(); 
            
            List<AnnotationDataRecord> records = new ArrayList<AnnotationDataRecord>();
            List<AnnotationDataRecord> valueRecords = new ArrayList<AnnotationDataRecord>();
           
            for(int y=0; y< annotations.size(); y++){            			
            	Annotation aa = null;
                AbstractAnnotation ann = annotations.get(y);
                if(tier.getAnnotationAtTime(ann.getBeginTimeBoundary()) == null){
                	if(tier.isTimeAlignable()){
                		aa = tier.createAnnotation(ann.getBeginTimeBoundary(), ann.getEndTimeBoundary());
                    } else {
                    	long time = (ann.getBeginTimeBoundary() + ann.getEndTimeBoundary()) / 2;
                         aa = tier.createAnnotation(time, time);                        
                    }
                	
                	if(aa != null){
                		aa.setValue(ann.getValue());
                        records.add(new AnnotationDataRecord(aa));
                   }
                	
                } else {
                	AnnotationDataRecord record = null;
                	aa = tier.getAnnotationAtTime(ann.getBeginTimeBoundary());
                	
                	if(aa != null){
                		record = new AnnotationDataRecord(aa); 
                		if(record.getValue() != null && record.getValue().length() > 0){
                			if(overWrite){
                				aa.setValue(ann.getValue());
                				valueRecords.add(record);
                			}
                		} else{
                			valueRecords.add(record);
                			aa.setValue(ann.getValue());
                		}
                	}
                }       
            } 
            if(records.size() >0){
            	createdAnnos.put(tier.getName(), records);
            }
        
            if(valueRecords.size() >0){
            	editedAnnos.put(tier.getName(), valueRecords);
            }  
        }
        transcription.setNotifying(true);
        setWaitCursor(false);
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
    
    
    private List<TierImpl> sortTiers(List<TierImpl> tierList){    
    	
    	List<TierImpl> sortedTiers = new ArrayList<TierImpl>();
    	
    	Map<TierImpl, DefaultMutableTreeNode> nodes = new HashMap<TierImpl, DefaultMutableTreeNode>();
        DefaultMutableTreeNode sortedRootNode = new DefaultMutableTreeNode(
                    "Root");

        for (TierImpl tier : tierList) {
            DefaultMutableTreeNode n = new DefaultMutableTreeNode(tier);

            nodes.put(tier, n);
        }
        
        for (TierImpl tier : tierList) {
        	
        	if (nodes.get(tier.getParentTier()) == null) {
               sortedRootNode.add(nodes.get(tier));
            } else {
            	nodes.get(tier.getParentTier()).add(nodes.get(tier));
            }
        }
        
        Enumeration nodeEnum = sortedRootNode.preorderEnumeration();

         // skip root
         nodeEnum.nextElement();

         while (nodeEnum.hasMoreElements()) {
            DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode) nodeEnum.nextElement();
            sortedTiers.add((TierImpl)nextnode.getUserObject());
         }
    	return sortedTiers;	
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

	/**
     * Deletes the new annotations on the dependent tiers
     */
	@Override
	public void undo() {
		setWaitCursor(true);
        transcription.setNotifying(false);
        
        List<AnnotationDataRecord> records;
        List<AnnotationDataRecord> valueRecords;
        
        if(transcription != null){
        	if(emptyAnnTiers != null || annWithValTiers!=null ){
        		if (createdAnnos.size() > 0 ||  editedAnnos.size() > 0) {
    				for( int i=0; i< emptyAnnTiers.size(); i++){
    					TierImpl tier = (TierImpl) transcription.getTierWithId(emptyAnnTiers.get(i));
    					if (tier != null) {
    						records = createdAnnos.get(tier.getName());    						
    						if(records == null){
    							continue;
    						}
    						for (int x = 0; x < records.size(); x++) {
    							AnnotationDataRecord record = records.get(x);
    							Annotation ann = tier.getAnnotationAtTime((record.getBeginTime() +  record.getEndTime()) / 2);
                				if (ann != null) {
    								tier.removeAnnotation(ann);
    							}else {
                					LOG.warning("The annotation could not be found for undo");
    							}
    						}	
    					} else {
                		 	LOG.warning("The tier could not be found: " + emptyAnnTiers.get(i));
    					}
    				} 
    				for( int i=0; i< annWithValTiers.size(); i++){
    					TierImpl tier = (TierImpl) transcription.getTierWithId(annWithValTiers.get(i));
    					if (tier != null) {
    						records = createdAnnos.get(tier.getName());
    						valueRecords = editedAnnos.get(tier.getName());
    						if(records == null && valueRecords == null){
    							continue;
    						}
    						if(records != null){
    							for (int x = 0; x < records.size(); x++) {
    								AnnotationDataRecord record = records.get(x);
    								Annotation ann = tier.getAnnotationAtTime((record.getBeginTime() +  record.getEndTime()) / 2);
    								if (ann != null) {
    									tier.removeAnnotation(ann);
    								}else {
    									LOG.warning("The annotation could not be found for undo");
    								}
    							}
    						}
    						if(valueRecords != null){
    							for (int x = 0; x < valueRecords.size(); x++) {
    								AnnotationDataRecord record = valueRecords.get(x);
    								Annotation ann = tier.getAnnotationAtTime((record.getBeginTime() +  record.getEndTime()) / 2);
    								if (ann != null) {
    									ann.setValue(record.getValue());
    								}else {
    									LOG.warning("The annotation could not be found for undo");
    								}
    							}
    						}
    					} else {
                		 	LOG.warning("The tier could not be found: " + annWithValTiers.get(i));
    					}
    				}
    			} else {
        			LOG.info("No annotation records have been stored for undo.");
        		}
        	} else {
                 LOG.warning("No tier names have been stored.");
            }
        }
        
		setWaitCursor(false);
        transcription.setNotifying(true);		
	}
	
	 /**
     * Recreates the  newly created annotations
     * on the dependent tiers      
     */
	@Override
	public void redo() {		
		setWaitCursor(true);
        transcription.setNotifying(false);
        
        
        
		List<AnnotationDataRecord> records;
		List<AnnotationDataRecord> valueRecords;
		if (transcription != null) {
            for(int i=0; i<emptyAnnTiers.size(); i++){
            	TierImpl tier = (TierImpl) transcription.getTierWithId(emptyAnnTiers.get(i));
            	 if (tier != null) {
            		records = createdAnnos.get(tier.getName());
            		if(records == null){
            			 continue;
            		 }            		
            		 for (int x = 0; x < records.size(); x++) {
            			 AnnotationDataRecord record = records.get(x);             					 
            			 if(record != null){
            				 if(tier.isTimeAlignable()){
            					 tier.createAnnotation(record.getBeginTime(), record.getEndTime());
            				 }else{
            					 long time = (record.getBeginTime() +  record.getEndTime())/2;
            					 tier.createAnnotation(time, time);                             		
            				 }
            			 }
            		 }
            	 } 
            }            
            for(int i=0; i<annWithValTiers.size(); i++){
            	TierImpl tier = (TierImpl) transcription.getTierWithId(annWithValTiers.get(i));
            	 if (tier != null) {
            		records = createdAnnos.get(tier.getName());
            		valueRecords = editedAnnos.get(tier.getName());
            		if(records == null && valueRecords == null){
							continue;
					}       		
            		 if(records != null){
            			 for (int x = 0; x < records.size(); x++) {
            				 AnnotationDataRecord record = records.get(x); 
            				 Annotation ann;
            				 if(record != null){
            					 if(tier.isTimeAlignable()){
            						 ann = tier.createAnnotation(record.getBeginTime(), record.getEndTime());
            					 }else{
            						 long time = (record.getBeginTime() +  record.getEndTime())/2;
            						 ann = tier.createAnnotation(time, time);                             		
            					 }
            					 
            					 if(ann != null){
            						 ann.setValue(record.getValue());
            					 }
            				 }
            			 }
            		 }
            		 if(valueRecords != null){
  						for (int x = 0; x < valueRecords.size(); x++) {
  							AnnotationDataRecord record = valueRecords.get(x);
  							Annotation ann = tier.getAnnotationAtTime((record.getBeginTime() +  record.getEndTime()) / 2);
  							if (ann != null) {
  								 String value = ((TierImpl) tier.getParentTier()).getAnnotationAtTime(record.getBeginTime()).getValue();
  								ann.setValue(value);
  							}
  						}
            		 }
            	 }
            }
		}				 
		setWaitCursor(false);
        transcription.setNotifying(true);		
	}
}

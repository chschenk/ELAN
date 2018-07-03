package mpi.eudico.client.annotator.export.multiplefiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.interlinear.Interlinear;
import mpi.eudico.client.annotator.interlinear.ToolboxEncoder;
import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxEncoderInfo;

/**
 * Final Step 4 
 * Actual export id done here.
 * The ui is progess monitor
 * 
 * @author aarsom
 * @version Feb,2012
 *
 */
@SuppressWarnings("serial")
public class MultipleFileToolBoxExportStep4 extends AbstractMultiFileExportProgessStepPane {
    private int charsPerLine, timeFormat;
    private boolean correctTimes;
    private boolean typeFileSelected;
    private String databaseType;
    private boolean wrapLines;
    private boolean wrapNextLine;
    private boolean includeEmptyLines;
    private boolean appendFileName;
    private String recordMarker;
    private Map<String, String> recordMarkerMap;
	private boolean useDetectedRecordMarker = false;
    private List<String> markersWithBlankLinesList;
    private boolean includeMediaMarker;
    private String mediaMarkerName;
    private boolean useAudioFile = false;
    private boolean useRelFilePath;
	private ToolboxEncoderInfo tbEncoderInfo; 
	 private Map<String, List<String>> tierMap;
	
	
    private final String elanBeginLabel = Constants.ELAN_BEGIN_LABEL;
    private final String elanEndLabel = Constants.ELAN_END_LABEL;
    private final String elanParticipantLabel = Constants.ELAN_PARTICIPANT_LABEL;
	
    /**
     * Constructor
     *
     * @param multiPane the container pane
     */
    public MultipleFileToolBoxExportStep4(MultiStepPane multiPane){
    	super(multiPane);
    }
    /**
     * Calls doFinish.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {    
    	charsPerLine = (Integer) multiPane.getStepProperty("CharsPerLine");
        timeFormat = (Integer) multiPane.getStepProperty("TimeFormat");
        correctTimes = (Boolean) multiPane.getStepProperty("CorrectTimes");
        typeFileSelected = (Boolean) multiPane.getStepProperty("TypeFileSelected");
        databaseType = (String) multiPane.getStepProperty("DatabaseType");
        wrapLines = (Boolean) multiPane.getStepProperty("WrapLines");
        wrapNextLine = (Boolean) multiPane.getStepProperty("WrapNextLine");
        includeEmptyLines = (Boolean) multiPane.getStepProperty("IncludeEmptyLines");
        appendFileName = (Boolean) multiPane.getStepProperty("AppendFileNameWithRecordMarker");
        recordMarker = (String) multiPane.getStepProperty("RecordMarker");    
        recordMarkerMap = (Map<String, String>) multiPane.getStepProperty("RecordMarkersMap");
        useDetectedRecordMarker = (Boolean) multiPane.getStepProperty("UseDetectedRecordMarker");
        
        tierMap = (Map<String, List<String>>) multiPane.getStepProperty("TierListMap");
        
        markersWithBlankLinesList = (List<String>) multiPane.getStepProperty("");
        includeMediaMarker = (Boolean) multiPane.getStepProperty("IncludeMediaMarkerCB");
        mediaMarkerName = (String) multiPane.getStepProperty("MediaMarkerName");
        if(multiPane.getStepProperty("AudiofileType") != null){
        	useAudioFile = (Boolean) multiPane.getStepProperty("AudiofileType");
        }        
        useRelFilePath = (Boolean) multiPane.getStepProperty("UseRelFilePath");
        
        super.enterStepForward();  
    }
    
    /**
     * The actual writing.
     *
     * @param fileName path to the file, not null
     * @param orderedTiers tier names, ordered by the user, min size 1	     
     *
     * @return true if all went well, false otherwise
     */
    @Override
	protected boolean doExport(TranscriptionImpl transcription, final String fileName) {	
    	
    	if(tbEncoderInfo == null){
    		initilaizeToolboxEncoderInfo();
    	}
        
        if (correctTimes) {
            List<MediaDescriptor> mds = transcription.getMediaDescriptors();

            if ((mds != null) && !mds.isEmpty()) {
                long mediaOffset = mds.get(0).timeOrigin;
                tbEncoderInfo.setTimeOffset(mediaOffset);
            }  else {
            	tbEncoderInfo.setTimeOffset(0L);
            }
        }
        
        if (includeMediaMarker) {
        	String mediaFileName = getMediaFileName(transcription);
        	if(mediaFileName != null){
        		tbEncoderInfo.setIncludeMediaMarker(true);
	        	tbEncoderInfo.setMediaMarker(mediaMarkerName);
	        	tbEncoderInfo.setMediaFileName(mediaFileName);
        	} else {
        		tbEncoderInfo.setIncludeMediaMarker(false);
	        	tbEncoderInfo.setMediaMarker(null);
	        	tbEncoderInfo.setMediaFileName(null);
        	}        	
        }
        
        String oriFilePath = FileUtility.urlToAbsPath(transcription.getFullPath());
        
        String recordMarkerForThisFile = recordMarker;
        if(useDetectedRecordMarker){        	
        	recordMarkerForThisFile = this.recordMarkerMap.get(oriFilePath);
        	if(recordMarkerForThisFile == null){
        		recordMarkerForThisFile = recordMarker;
        	}
        } 
        
        if(appendFileName){
        	String file = FileUtility.fileNameFromPath(fileName);
        	file = file.substring(0,file.indexOf('.'));
        	recordMarkerForThisFile = recordMarkerForThisFile + " " + file;
        }

        tbEncoderInfo.setRecordMarker(recordMarkerForThisFile);
        
        List<String> selectedTiersInThisTrans = new ArrayList<String>();
        List<String> tierInThisTrans = tierMap.get(oriFilePath);
        if(tierInThisTrans != null){
        	for(String tierName :selectedTiers){        	
            	if(tierInThisTrans.contains(tierName)){
            		selectedTiersInThisTrans.add(tierName);
            	} else if(tierName.equals(elanBeginLabel) ||
            			tierName.equals(elanEndLabel) ||
            			tierName.equals(elanParticipantLabel)){
            		selectedTiersInThisTrans.add(tierName);
            	}
            }
        }
        
        
        if(recordMarkerForThisFile != null && selectedTiersInThisTrans.contains(recordMarkerForThisFile)){
        	if(!selectedTiersInThisTrans.get(0).equals(recordMarkerForThisFile)){
        		selectedTiersInThisTrans.remove(recordMarkerForThisFile);
        		selectedTiersInThisTrans.add(0, recordMarkerForThisFile);
        	}
        }    

        tbEncoderInfo.setOrderedVisibleTiers(selectedTiersInThisTrans);
        
        if (fileName != null) {
            try {
            	ToolboxEncoder encoder = new ToolboxEncoder();
                encoder.encodeAndSave(transcription,
                        tbEncoderInfo,
                        transcription.getTiersWithIds(selectedTiersInThisTrans),
                        fileName);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(MultipleFileToolBoxExportStep4.this,
                        ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                        "(" + ioe.getMessage() + ")",
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.ERROR_MESSAGE);   
            }
        }

        return true;
    }   
    
    private void initilaizeToolboxEncoderInfo(){	    	
		int markerSource = ToolboxEncoderInfo.TIERNAMES; // default

        if (typeFileSelected) {
            markerSource = ToolboxEncoderInfo.TYPFILE;
        }
        
        tbEncoderInfo = new ToolboxEncoderInfo(charsPerLine,
                markerSource, timeFormat);
        tbEncoderInfo.setCorrectAnnotationTimes(correctTimes);

        if (databaseType != null) {
            tbEncoderInfo.setDatabaseType(databaseType);
        }
        // the new options
        if (charsPerLine != Integer.MAX_VALUE) {
	        tbEncoderInfo.setWrapLines(wrapLines);
	        if (wrapLines) {
	        	if (wrapNextLine) {
	        		tbEncoderInfo.setLineWrapStyle(Interlinear.NEXT_LINE);
	        	} else {
	        		tbEncoderInfo.setLineWrapStyle(Interlinear.END_OF_BLOCK);
	        	}
	        } else {
	        	tbEncoderInfo.setLineWrapStyle(Interlinear.NO_WRAP);
	        }
        } else {
        	// no block and no line wrapping
        	tbEncoderInfo.setWrapLines(false);
        	tbEncoderInfo.setLineWrapStyle(Interlinear.NO_WRAP);
        }	
        
        tbEncoderInfo.setIncludeEmptyMarkers(includeEmptyLines);
        tbEncoderInfo.setMarkersWithBlankLines(markersWithBlankLinesList); 
	}
    
    private String getMediaFileName(TranscriptionImpl trans){
		String mediaFileName = null;
		List<MediaDescriptor> mds = trans.getMediaDescriptors();
		if (mds != null && mds.size() > 0) {	    		  	
    		if(mds.size() == 1){
    			mediaFileName = mds.get(0).mediaURL;
    		}else {
    			MediaDescriptor md = null;	  
    			for (int i = 0; i < mds.size(); i++) {
    				md = mds.get(i);
    				String type = md.mimeType;
    				if(type != null){
    					if(useAudioFile){
    						if(type.equals(MediaDescriptor.WAV_MIME_TYPE) || 
    								type.equals(MediaDescriptor.GENERIC_AUDIO_TYPE)){
	    						mediaFileName = md.mediaURL;
	    						break;
	    					}
	    				} else {
	    					if(MediaDescriptorUtil.isVideoType(md)){
	    						mediaFileName = md.mediaURL;
	    						break;
	    					}
	    				}
    				}
    				
	    		}
    		}	    		
		}
		
		if(mediaFileName != null){
			if(useRelFilePath){
				mediaFileName = FileUtility.fileNameFromPath(mediaFileName);
			} else{
				String fileURL = FileUtility.urlToAbsPath(mediaFileName);
    			int numSlash = 0;
    			for (int j = 0; j < fileURL.length(); j++) {
    				if (fileURL.charAt(j) == '/') {
    					numSlash++;
    				} else {
    					break;
    				}
    			}
    			
    			if (numSlash != 0 && numSlash !=2 ) {
    				fileURL = fileURL.substring(numSlash);
    			}
    			mediaFileName = fileURL.replace('/', '\\');
			}
		}
		return mediaFileName;
	}	
}
    
  
package mpi.eudico.client.annotator.util;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.util.TimeFormatter;

/**
 * A transferable annotation, using an AnnotationdataRecord as the transferable object.
 * 
 * Note June 2006 transferal of application/x-java-serialized-object objects between jvm's 
 * doesn't seem to work on Mac OS X. The Transferable sun.awt.datatransfer.ClipboardTransferable@8b4cb8 
 * doesn't support any flavor with human presentable name "application/x-java-serialized-object" 
 */
public class TransferableAnnotation implements Transferable, ClipboardOwner {
    private AnnotationDataRecord record;
    private final static DataFlavor[] flavors;
    private static final int STRING = 0;
    private static final int ANNOTATION = 1; 

    private String copyOption = Constants.TEXTANDTIME_STRING;
    private String prefTimeFormat = null;
    private boolean useCopyCurrentTimeFormat = false;
    
    static {
        DataFlavor flav = AnnotationDataFlavor.getInstance();
        
        if (flav == null) {
            flavors = new DataFlavor[]{DataFlavor.stringFlavor};
        } else {
            flavors = new DataFlavor[]{DataFlavor.stringFlavor, flav};
        }
               
    }
        
    /**
     * Creates a new TransferableAnnotation
     * @param record the transferable object
     */
    public TransferableAnnotation(AnnotationDataRecord record) {
        if (record == null) {
            throw new NullPointerException("AnnotationDataRecord is null.");
        }
        this.record = record;
        
        String stringPref = Preferences.getString("EditingPanel.CopyOption", null);        
        // string should be a non-locale string, a string from Constants        
		if (stringPref != null) {
		    copyOption = stringPref;
		}
		prefTimeFormat = Preferences.getString("CurrentTime.Copy.TimeFormat", null);
		if (prefTimeFormat != null) {
			Boolean boolPref = Preferences.getBool("CopyAnnotation.UseCopyCurrentTimeFormat", null);
			if (boolPref != null) {
				useCopyCurrentTimeFormat = boolPref.booleanValue();
			}
		}
    }
    
    /**
     * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
     */
    @Override
	public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    /**
     * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
     */
    @Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (flavor == null) {
            return false; // could throw NullPointer Exc.
        }
        
        for (DataFlavor flavor2 : flavors) {
            if (flavor.equals(flavor2)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
     */
    @Override
	public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }

        if (flavors.length > 1 && flavor.equals(flavors[ANNOTATION])) {
            return record; // clone?? once on the clipboard the record don't seem to change anymore
        } else if (flavor.equals(flavors[STRING])) {
            return recordParamString();
        }
        
        return null;
    }
    
    /**
     * Returns a string representation of the annotation data record.
     * 
     * The citation variant has been proposed to have this format:
     * annotation (media file name, (tier name?), bt, et)
     * 
     * @return a string representation of the annotation data record
     */
    private String recordParamString() {
        if (record != null) {   
            if (copyOption.equals(Constants.TEXT_STRING)) {
        	// copy annotation only
            	return record.getValue();
            }  else if (copyOption.equals(Constants.CITE_STRING)) {
            	String fileName = record.getFilePath();
            	// normalize path?
            	if (fileName != null) {
            		if (fileName.indexOf('/') > -1 && fileName.lastIndexOf('/') < fileName.length() - 2) {
            			fileName = fileName.substring(fileName.lastIndexOf('/') + 1);	
            		}
            		return record.getValue() + " (" + fileName + ", " + record.getTierName() + ", " + getFormattedTime(record.getBeginTime()) +
                    		", " + getFormattedTime(record.getEndTime()) + ")";
            	} else {
            		return record.getValue() + ",T=" + record.getTierName() + ",B=" + getFormattedTime(record.getBeginTime()) +
                		",E=" + getFormattedTime(record.getEndTime());
            	}
            } else {//TEXTANDTIME_STRING
            	return record.getValue() + ",T=" + record.getTierName() + ",B=" + getFormattedTime(record.getBeginTime()) +
            		",E=" + getFormattedTime(record.getEndTime());
            }
        } else {
            return "null";
        }
    }
    
    /**
     * 
     * @param time the time to convert
     * @return the formatted time string, depending on the setting for Copy Current Time and the flag
     * whether or not to use the same format
     */
    private String getFormattedTime(long time) {
    	if (prefTimeFormat != null && useCopyCurrentTimeFormat) {
    		if (prefTimeFormat.equals(Constants.HHMMSSMS_STRING)) {
    			return TimeFormatter.toString(time);
    		} else if (prefTimeFormat.equals(Constants.SSMS_STRING)) {
    			return TimeFormatter.toSSMSString(time);
    		} else if (prefTimeFormat.equals(Constants.NTSC_STRING)) {
    			return TimeFormatter.toTimecodeNTSC(time);
    		} else if (prefTimeFormat.equals(Constants.PAL_STRING)) {
    			return TimeFormatter.toTimecodePAL(time);
    		} else if (prefTimeFormat.equals(Constants.PAL_50_STRING)) {
    			return TimeFormatter.toTimecodePAL50(time);
    		}
    	}
    	
    	return String.valueOf(time);// default, ms value as string
    }   

    /**
     * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
     */
    @Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
        record = null;
    }

}

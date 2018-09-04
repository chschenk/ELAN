package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.IllegalEditException;

import javax.swing.JOptionPane;


/**
 * Command that shifts all annotations on a specified root tier that are
 * completely inside a specified time interval. The user has been prompted to
 * specify the amount of ms to shift the annotations, to the left or to the
 * right. All depending annotations are shifted as well.
 *
 * @author Han Sloetjes
 * @version 1.0, Nov 2008
 * @version Nov 2016 
 * 	- invoke the generation of an ACMEditEvent if annotations have been shifted
 * 	- corrected the calculation of the interval for the undo action 
 */
public class ShiftAnnotationsCommand implements UndoableCommand {
    String commandName;

    // receiver; the transcription 
    TranscriptionImpl transcription;
    private TierImpl tier;
    Long bt;
    Long et;
    Long shiftValue;
    long availBT;// the begin time extreme of the available interval in which annotations can be shifted
    long availET;// the end time extreme of the available interval in which annotations can be shifted

    /**
     * Creates a new ShiftAnnotationsCommand instance
     *
     * @param name the name of the command
     */
    public ShiftAnnotationsCommand(String name) {
        commandName = name;
    }

    /**
     * Shift the annotations back.
     */
    @Override
	public void redo() {
    	if (transcription != null) {
    		transcription.setNotifying(false);
    		int numShifted = shift(tier, shiftValue, bt, et);
    		
            if (numShifted > 0) {
            	transcription.handleModification(transcription, ACMEditEvent.CHANGE_ANNOTATIONS, 
            			transcription);
            }
            
    		transcription.setNotifying(true);
    	}
    }

    /**
     * Shift the annotations again.
     */
    @Override
	public void undo() {
    	if (transcription != null) {
    		transcription.setNotifying(false);
    		
//    		int numShifted = shift(tier, -shiftValue, (bt == 0 ? bt : (bt + shiftValue)),
//	            ((et == Long.MAX_VALUE) ? et : (et + shiftValue)));
    		int numShifted = shift(tier, -shiftValue, availBT, availET);
	        
            if (numShifted > 0) {
            	transcription.handleModification(transcription, ACMEditEvent.CHANGE_ANNOTATIONS, 
            			transcription);
            }
            
	        transcription.setNotifying(true);
    	}
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: <ul><li>arg[0] = the (active) tier
     *        (TierImpl)</li> <li>arg[1] = the begin of the time interval the
     *        annotations in which are  to be shifted (Long)</li> <li>arg[2] =
     *        the end time of that interval (Long)</li> <li>arg[3] = the shift
     *        value (Long)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        if (arguments != null) {
            tier = (TierImpl) arguments[0];
            bt = (Long) arguments[1];
            et = (Long) arguments[2];
            shiftValue = (Long) arguments[3];
            
    		availBT = getBoundaryBefore(tier, bt);
    		availET = getBoundaryAfter(tier, et);
    		
            transcription.setNotifying(false);
            
            int numShifted = shift(tier, shiftValue, bt, et);
            
            if (numShifted > 0) {
            	transcription.handleModification(transcription, ACMEditEvent.CHANGE_ANNOTATIONS, 
            			transcription);
            }
            transcription.setNotifying(true);
        }
    }

    int shift(TierImpl tier, long shiftValue, long bt, long et) {
        if (tier != null) {
        	
            try {
                return tier.shiftAnnotations(shiftValue, bt, et);
            } catch (IllegalArgumentException iae) {
                JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(
                        transcription),
                    ElanLocale.getString("ShiftAllDialog.Warn5") + " " +
                    iae.getMessage(), ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
                iae.printStackTrace();
            } catch (IllegalEditException iee) {
                JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(
                        transcription),
                    ElanLocale.getString("ShiftAllDialog.Warn5") + " " +
                    iee.getMessage(), ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
                iee.printStackTrace();
            }
                       
        }
        
        return 0;
    }
    
    /**
     * Calculates the available space to the left side of the provided selection begin time or
     * cross hair time. Shifting has to be limited to this boundary on the left side. 
     * 
     * @param tier the tier inspected
     * @param bt the selection begin time or the reference cross hair time
     * @return the limit on the left side within which the shifting has to take place 
     * or 0 if no annotation is found to the left
     */
    long getBoundaryBefore(TierImpl tier, long bt) {
    	if (bt == 0) {
    		return bt;
    	}
    	Annotation a = tier.getAnnotationBefore(bt);
    	if (a != null) {
    		return a.getEndTimeBoundary();
    	}
    	
    	return 0;
    }

    /**
     * Calculates the available space to the right side of the provided selection end time or
     * cross hair time. Shifting has to be limited to this boundary on the right side. 
     * 
     * @param tier the tier inspected
     * @param et the selection end time or the reference cross hair time
     * @return the limit on the right side within which the shifting has to take place 
     * or Long.MAX_VALUE if no annotation found
     */
    long getBoundaryAfter(TierImpl tier, long et) {
    	if (et == Long.MAX_VALUE) {
    		return et;
    	}
    	
    	Annotation a = tier.getAnnotationAfter(et);
    	if (a != null) {
    		return a.getBeginTimeBoundary();
    	}
    	
    	return Long.MAX_VALUE;
    }
    
    /**
     * Returns the name of the command.
     *
     * @return the name
     */
    @Override
	public String getName() {
        return commandName;
    }
}

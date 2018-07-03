package mpi.eudico.client.annotator.commands;

import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ShiftAnnotationsDialog;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Creates a dialog to specify how much the annotations on all tiers
 * left or right from the crosshair should be shifted.
 * 
 * @author Han Sloetjes
 * @version 1.0, Nov 2008
  */
public class ShiftAnnotationsLROfDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new ShiftAnnotationsLROfDlgCommand instance
     *
     * @param commandName the name of the command
     */
    public ShiftAnnotationsLROfDlgCommand(String commandName) {
        super();
        this.commandName = commandName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: <ul><li>arg[0] = the begin of the time
     *        interval the annotations in which are  to be shifted (Long)</li>
     *        <li>arg[1] = the end time of that interval (Long)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl transcription = (TranscriptionImpl) receiver;

        if (arguments != null) {
            Long bt = (Long) arguments[0];
            Long et = (Long) arguments[1];
            
            List<TierImpl> rootTiers = transcription.getTopTiers();
            TierImpl tier = null;
            AlignableAnnotation ann;
            
            long min = Long.MIN_VALUE;
            long max = Long.MAX_VALUE;
            
            for (int i = 0; i < rootTiers.size(); i++) {
                AlignableAnnotation beforeAnn = null;
                AlignableAnnotation afterAnn = null;
                AlignableAnnotation firstIn = null;
                AlignableAnnotation lastIn = null;
            	tier = rootTiers.get(i);
            	
                if (tier.hasParentTier() || !tier.isTimeAlignable()) {
                    continue;
                }
                List<AbstractAnnotation> annos = tier.getAnnotations();
                Iterator annIt = annos.iterator();
                
                while (annIt.hasNext()) {
                    ann = (AlignableAnnotation) annIt.next();
                    
                    if ((ann.getEnd().getTime() <= bt) ||
                            ((ann.getBegin().getTime() < bt) &&
                            (ann.getEnd().getTime() < et))) {
                        beforeAnn = ann;

                        continue;
                    }
                    
                    if ((ann.getBegin().getTime() <= bt && ann.getEnd().getTime() > et) || 
                    		(ann.getBegin().getTime() < bt && ann.getEnd().getTime() >= et)) {
                    	break;
                    }
                    
                    if ((ann.getBegin().getTime() >= et) ||
                            ((ann.getBegin().getTime() > bt) &&
                            (ann.getEnd().getTime() > et))) {
                        afterAnn = ann;

                        break;
                    }
                    
                    // if the annotation is not before the interval, left or right overlapping,
                    // or after the interval it is inside the interval
                    if (firstIn == null) {
                        firstIn = ann;
                    }

                    lastIn = ann;
                }
                
                if (firstIn == null) {
                	continue;
                }
                
                if (beforeAnn != null) {
                    min = Math.max(min, beforeAnn.getEnd().getTime() -
                        firstIn.getBegin().getTime()); // negative value
                } else {
                    min = Math.max(min, -firstIn.getBegin().getTime());
                }
                
                if (afterAnn != null) {
                    max = Math.min(max, afterAnn.getBegin().getTime() -
                        lastIn.getEnd().getTime());
                } else {
                    max = Math.min(max, Integer.MAX_VALUE);
                }
            }
            
            if ((min == 0) && (max == 0)) {
                // no space to shift anything
                JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(
                        transcription),
                    ElanLocale.getString("ShiftAllDialog.Warn4"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);

                return;
            }
            
            // create dialog
            long shiftValue = 0L;
            ShiftAnnotationsDialog dlg = new ShiftAnnotationsDialog(transcription, min, max);
            dlg.setVisible(true); //blocks
            shiftValue = dlg.getValue();

            if (shiftValue != 0) {
                Command com = ELANCommandFactory.createCommand(transcription,
                        ELANCommandFactory.SHIFT_ALL_ANNOTATIONS_LROf);
                com.execute(transcription,
                    new Object[] { bt, et, new Long(shiftValue) });
            }
        }
    }

    /**
     * Returns the name of the command
     *
     * @return the name
     */
    @Override
	public String getName() {
        return commandName;
    }
}

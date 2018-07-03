package mpi.eudico.client.annotator.commands;

import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ShiftAnnotationsDialog;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * Creates an input dialog to specify the amount of milliseconds to shift one
 * or more annotations. Performs some checks.
 *
 * @author Han Sloetjes
 * @version 1.0, Nov 2008
 */
public class ShiftAnnotationsDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new ShiftAnnotationsDlgCommand instance
     *
     * @param name the name
     */
    public ShiftAnnotationsDlgCommand(String name) {
        commandName = name;
    }

    /**
	 * <b>Note: </b>it is assumed the types and order of the arguments are
	 * correct.
	 *
	 * @param receiver
	 *            the Transcription
	 * @param arguments
	 *            the arguments:
	 *            <ul>
	 *            <li>arg[0] = the (active) tier (TierImpl). Must be a root tier
	 *                         (which should make it time-alignable as well).</li>
	 *            <li>arg[1] = the begin of the time interval the annotations in
	 *                         which are to be shifted (Long)</li>
	 *            <li>arg[2] = the end time of that interval (Long)</li>
	 *            </ul>
	 */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        Transcription transcription = (Transcription) receiver;

        if (arguments != null) {
            TierImpl tier = (TierImpl) arguments[0];
            Long bt = (Long) arguments[1];
            Long et = (Long) arguments[2];

            if (tier.hasParentTier() || !tier.isTimeAlignable()) {
            	// not a root tier
                JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(
                        transcription),
                    ElanLocale.getString("ShiftAllDialog.Warn6"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // determine min. and max. shift value
            List<AbstractAnnotation> annos = tier.getAnnotations();
            AlignableAnnotation ann;
            AlignableAnnotation beforeAnn = null;
            AlignableAnnotation afterAnn = null;
            AlignableAnnotation firstIn = null;
            AlignableAnnotation lastIn = null;
            Iterator annIt = annos.iterator();

            while (annIt.hasNext()) {
                ann = (AlignableAnnotation) annIt.next();

                if ((ann.getEnd().getTime() <= bt) ||
                        ((ann.getBegin().getTime() < bt) &&
                        (ann.getEnd().getTime() < et))) {
                    beforeAnn = ann;

                    continue;
                }

                if ((ann.getBegin().getTime() >= et) ||
                        ((ann.getBegin().getTime() > bt) &&
                        (ann.getEnd().getTime() > et))) {
                    afterAnn = ann;

                    break;
                }
                
                if ((ann.getBegin().getTime() <= bt && ann.getEnd().getTime() > et) || 
                		(ann.getBegin().getTime() < bt && ann.getEnd().getTime() >= et)) {
                	break;
                }
                
                // if the annotation is not before the interal, left or right overlapping,
                // or after the interval it is inside the interval
                if (firstIn == null) {
                    firstIn = ann;
                }

                lastIn = ann;
            }

            if (firstIn == null) {
                // no annotations in the interval
                JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(
                        transcription),
                    ElanLocale.getString("ShiftAllDialog.Warn3"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);

                return;
            }

            // calculate min and max
            long min = 0;
            long max = 0;

            if (beforeAnn != null) {
                min = beforeAnn.getEnd().getTime() -
                    firstIn.getBegin().getTime(); // negative value
            } else {
                min = -firstIn.getBegin().getTime();
            }

            if (afterAnn != null) {
                max = afterAnn.getBegin().getTime() -
                    lastIn.getEnd().getTime();
            } else {
                max = Integer.MAX_VALUE;
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
                        ELANCommandFactory.SHIFT_ANNOTATIONS);
                com.execute(transcription,
                    new Object[] { tier, bt, et, new Long(shiftValue) });
            }
        }
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

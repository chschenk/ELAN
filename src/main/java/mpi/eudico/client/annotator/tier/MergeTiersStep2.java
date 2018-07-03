package mpi.eudico.client.annotator.tier;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Second step of this wizard: entering of a  name for the new tier, selection
 * of it's linguistic type and optional selection of the format of the
 * duration  as annotation values. The new annotations can also be a concatenation
 * of merged annotation values.
 *
 * @author Han Sloetjes
 * @version 1.0 July 2008
 */
@SuppressWarnings("serial")
public class MergeTiersStep2 extends CalcOverlapsStep2 implements ListSelectionListener,
    ActionListener, CaretListener {

    /**
     * Constructor
     *
     * @param multiPane the container pane
     * @param transcription the transcription
     */
    public MergeTiersStep2(MultiStepPane multiPane,
        TranscriptionImpl transcription) {
        super(multiPane, transcription);
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("OverlapsDialog.DefineDest");
    }

    /**
     * Check and store properties, if all conditions are met.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
        if (super.leaveStepForward()) {
        	if (durationRB.isSelected()) {
                multiPane.putStepProperty("ContentType", "Duration");
                if (msRB.isSelected()) {
                    multiPane.putStepProperty("Format",
                    	Integer.valueOf(Constants.MS));
                } else if (secRB.isSelected()) {
                    multiPane.putStepProperty("Format",
                    	Integer.valueOf(Constants.SSMS));
                } else if (hourRB.isSelected()) {
                    multiPane.putStepProperty("Format",
                    	Integer.valueOf(Constants.HHMMSSMS));
                }
        	} else {
        		multiPane.putStepProperty("ContentType", "Concatenation");
        	}
            return true;
        }

        return false;
    }

    /**
     * Receives events from the textfield and the content checkbox.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	super.actionPerformed(e);
    }
}

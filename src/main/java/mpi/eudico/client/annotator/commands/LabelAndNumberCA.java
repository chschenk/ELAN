package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;


/**
 * A CommandAction that creates the dialog.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class LabelAndNumberCA extends CommandAction {
    /**
     * Creates a new LabelAndNumberCA instance
     *
     * @param viewerManager the viewermanager
     */
    public LabelAndNumberCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.LABEL_AND_NUMBER);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.LABEL_N_NUM_DLG);
    }

    /**
     * Returns the viewermanager as the central object giving access to all
     * necessary components.
     *
     * @return the viewermanager
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * Check if there are tiers first.
     *
     * @param event the action event
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        if (vm.getTranscription().getTiers().size() < 1) {
            JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(
                    vm.getTranscription()),
                ElanLocale.getString("LabelAndNumberDialog.Warning.NoTiers"),
                ElanLocale.getString("Message.Error"),
                JOptionPane.WARNING_MESSAGE);

            return;
        }

        super.actionPerformed(event);
    }
}

package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;


/**
 * A CommandAction that creates the dialog.
 * 
 * @author Aarthy Somasundaram
 * @version Oct 20, 2010
  */
public class RemoveAnnotationsOrValuesCA extends CommandAction {
    /**
     * Creates a new RemoveAnnotationsOrValuesCA instance
     *
     * @param viewerManager the viewermanager
     */
    public RemoveAnnotationsOrValuesCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.REMOVE_ANNOTATIONS_OR_VALUES);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.REMOVE_ANNOTATIONS_OR_VALUES_DLG);
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
                ElanLocale.getString("RemoveAnnotationsOrValuesDlg.Warning.NoTiers"),
                ElanLocale.getString("Message.Error"),
                JOptionPane.WARNING_MESSAGE);

            return;
        }

        super.actionPerformed(event);
    }
}

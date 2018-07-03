package mpi.eudico.client.annotator.commands;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;

public class AddParticipantCA extends CommandAction {
	/**
     * Creates a new AddParticipantDlgCA instance
     *
     * @param viewerManager the viewermanager
     */
	public AddParticipantCA(ViewerManager2 viewerManager) {
		super(viewerManager, ELANCommandFactory.ADD_PARTICIPANT);
	}
	
	/**
     * Creates a new add participant dialog command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.ADD_PARTICIPANT_DLG);
    }
    
    /**
     * Returns the transcription
     *
     * @return the transcription
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

package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;

import mpi.eudico.client.annotator.dcr.ELANDCRDialog;

import java.awt.event.ActionEvent;


/**
 * A menu action that creates a dialog to edit the locally stored ISO 12620
 * Data Category selection.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class EditLocalDCRMA extends FrameMenuAction {
    /**
     * Creates a new EditLocalDCRMA instance
     *
     * @param name name of the action
     * @param frame the parent Elan frame
     */
    public EditLocalDCRMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        ELANDCRDialog dialog = new ELANDCRDialog(frame, true,
                ELANDCRDialog.LOCAL_MODE);
        dialog.pack();
        dialog.setVisible(true);
    }
}

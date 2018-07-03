package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.gui.EditRecentLanguagesDialog;

/**
 * A MenuAction that brings up a JDialog for editing 
 * a list of "recent" content languages.
 *
 * @author Olaf Seibert
 */

@SuppressWarnings("serial")
public class EditLanguagesMA extends MenuAction {

	public EditLanguagesMA(String name) {
		super(name);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        new EditRecentLanguagesDialog(null).setVisible(true);
	}
}

package mpi.eudico.client.annotator.commands;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * A command that sets the author field of a document.
 * 
 * @author Han Sloetjes
 *
 */
public class SetAuthorCommand implements UndoableCommand {
	private String name;
	private Transcription transcription;
	private String oldAuthor;
	private String nextAuthor;
	
	/**
	 * Constructor.
	 * 
	 * @param name the name
	 */
	public SetAuthorCommand(String name) {
		this.name = name;
	}

	/**
	 * Applies the new author name again.
	 */
	@Override
	public void redo() {
		if (transcription != null) {
			transcription.setAuthor(oldAuthor);
		}
	}

	/**
	 * Restores the previous value of the author value.
	 */
	@Override
	public void undo() {
		if (transcription != null) {
			transcription.setAuthor(oldAuthor);
		}
	}
 
	/**
	 * @param receiver the transcription
	 * @param arguments null
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		if (receiver instanceof Transcription) {
			transcription = (Transcription) receiver;
		}
		
		if (transcription == null) {
			return;
		}

		oldAuthor = transcription.getAuthor();
		
		String input = (String) JOptionPane.showInputDialog(ELANCommandFactory.getRootFrame(transcription), 
				ElanLocale.getString("Frame.ElanFrame.SetAuthorMessage"), 
				ElanLocale.getString("Menu.Edit.Author"), JOptionPane.PLAIN_MESSAGE, null, null,
				oldAuthor);
		
		if (input != null) {
			nextAuthor = input;
			transcription.setAuthor(nextAuthor);
			transcription.setChanged();
		}
	}

	/**
	 * Returns the name of the command.
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

}

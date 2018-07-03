package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;

import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.FileExtension;

import mpi.eudico.server.corpora.clom.Transcription;

import java.io.File;


/**
 * Shows a save dialog and passes the selected filepath to the Preferences
 * export method.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ExportPrefsCommand implements Command {
    private String commandName;
    private Transcription transcription;

    /**
     * Creates a new ExportPrefsCommand instance
     *
     * @param name name of the command
     */
    public ExportPrefsCommand(String name) {
        commandName = name;
    }

    /**
     * Shows the save dialog and exports the Preferences.
     *
     * @param receiver the Transcription/document of which to save the
     *        preferences
     * @param arguments null
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (Transcription) receiver;

        if (transcription == null) {
            return;
        }

        String filePath = promptForExportFile();

        if (filePath == null) {
            return;
        }

        Preferences.exportPreferences(transcription, filePath);
    }

    /**
     * Prompts the user to specify a location and name for the prefs file.
     *
     * @return the file path
     */
    private String promptForExportFile() {       
        
        FileChooser chooser = new FileChooser(ELANCommandFactory.getRootFrame(transcription));
        chooser.createAndShowFileDialog(ElanLocale.getString("ExportDialog.ExportToFile"), FileChooser.SAVE_DIALOG,  
        		FileExtension.ELAN_XML_PREFS_EXT, "LastUsedExportDir");

        File prefFile = chooser.getSelectedFile();
        if (prefFile != null) {
            return prefFile.getAbsolutePath();
        }
        return null;
    }

    /**
     * Returns the name.
     *
     * @return the name!
     */
    @Override
	public String getName() {
        return commandName;
    }
}

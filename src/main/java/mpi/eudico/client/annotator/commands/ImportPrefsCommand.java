package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;

import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.FileExtension;

import mpi.eudico.server.corpora.clom.Transcription;

import java.io.File;


/**
 * Shows an open/import dialog and passes the selected file to the Preferences
 * import method.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ImportPrefsCommand implements Command {
    private String commandName;
    private Transcription transcription;

    /**
     * Creates a new ImportPrefsCommand instance
     *
     * @param name name of the command
     */
    public ImportPrefsCommand(String name) {
        commandName = name;
    }

    /**
     * Shows the dialog and imports the Preferences.
     *
     * @param receiver the Transcription/document to apply the preferences to
     * @param arguments null
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (Transcription) receiver;

        if (transcription == null) {
            return;
        }

        String filePath = promptForImportFile();

        if (filePath == null) {
            return;
        }

        Preferences.importPreferences(transcription, filePath);
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

    /**
     * Prompts the user to browse to an ELAN xml preferences file, checks a little.
     *
     * @return the file path
     */
    private String promptForImportFile() {
        String prefDir = Preferences.getString("", null);

        if (prefDir == null) {
            prefDir = System.getProperty("user.dir");
        }
        FileChooser chooser = new FileChooser(ELANCommandFactory.getRootFrame(transcription));
        
        chooser.createAndShowFileDialog(ElanLocale.getString("ImportDialog.Title.Select"), FileChooser.OPEN_DIALOG, 
        		ElanLocale.getString("ImportDialog.Approve"), null, FileExtension.ELAN_XML_PREFS_EXT, 
        		false, "LastUsedExportDir", FileChooser.FILES_ONLY, null);
       
        File prefFile = chooser.getSelectedFile();
        if (prefFile != null) {            
             return prefFile.getAbsolutePath();
        }

        return null;
    }
}

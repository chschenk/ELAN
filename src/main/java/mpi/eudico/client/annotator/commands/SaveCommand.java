package mpi.eudico.client.annotator.commands;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * DOCUMENT ME!
 *
 * @author Hennie Brugman
 */
public class SaveCommand implements Command {
    private String commandName;

    /**
     * Creates a new SaveCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public SaveCommand(String name) {
        commandName = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param receiver DOCUMENT ME!
     * @param arguments DOCUMENT ME!
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        Transcription tr = (Transcription) receiver;

        TranscriptionStore eafTranscriptionStore = (TranscriptionStore) arguments[0];

		int saveAsType = SaveAs27Preferences.saveAsType(tr);

        // for the moment, don't deal with visible tiers
        try {
            eafTranscriptionStore.storeTranscription(tr, null, new ArrayList<TierImpl>(0), null,
                    saveAsType);
            tr.setUnchanged();
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(tr),
                    //ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                    "Unable to save the transcription file: " +
                    "(" + ioe.getMessage() + ")",
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getName() {
        return commandName;
    }
}

package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;

import mpi.eudico.client.annotator.gui.ImportShoeboxWAC;

import mpi.eudico.server.corpora.clom.DecoderInfo;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;


/**
 * Action that starts an Import Shoebox sequence.
 *
 * @author Han Sloetjes, MPI
 */
public class ImportShoeboxMA extends FrameMenuAction {
    /**
     * Creates a new ImportShoeboxMA instance.
     *
     * @param name the name of the action (command)
     * @param frame the associated frame
     */
    public ImportShoeboxMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Shows an import shoebox dialog and creates a new transcription.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        Object result = ImportShoeboxWAC.showDialog(frame,
                ImportShoeboxWAC.SHOEBOX);

        if (result instanceof DecoderInfo) {
            String txtFileName = ((DecoderInfo) result).getSourceFilePath();

            try {
                TranscriptionImpl nextTranscription = new TranscriptionImpl(txtFileName,
                        (DecoderInfo) result);

                // A new Shoebox transcription starts with changed = false when the shoebox file
                // contains no tiers
                nextTranscription.setChanged();

                // replace all backslashes by forward slashes
                txtFileName = txtFileName.replace('\\', '/');

                int lastSlash = txtFileName.lastIndexOf('/');
                String toolboxPath = txtFileName.substring(0, lastSlash);

                boolean validMedia = true;

                if (frame != null) {
                    validMedia = frame.checkMedia(nextTranscription, toolboxPath);
                }

                //boolean validMedia = checkMedia(nextTranscription, toolboxPath);
                if (!validMedia) {
                    // ask if no media session is ok, if not return
                    int answer = JOptionPane.showConfirmDialog(frame,
                            ElanLocale.getString(
                                "Frame.ElanFrame.IncompleteMediaQuestion"),
                            ElanLocale.getString(
                                "Frame.ElanFrame.IncompleteMediaAvailable"),
                            JOptionPane.YES_NO_OPTION);

                    if (answer != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                // create a new frame
                FrameManager.getInstance().createFrame(nextTranscription);
            } catch (Exception e) {
                String message = (e.getMessage() != null) ? e.getMessage()
                                                          : e.getClass()
                                                             .getName();
                JOptionPane.showMessageDialog(frame,
                    (ElanLocale.getString("ImportDialog.Message.UnknownError") +
                    "\n" + message), ElanLocale.getString("Message.Error"),
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}

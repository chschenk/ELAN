package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;

import mpi.eudico.client.annotator.gui.ImportTranscriberDialog;

import mpi.eudico.server.corpora.clom.DecoderInfo;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;


/**
 * Action that starts an Import Transcriber sequence.
 *
 * @author Han Sloetjes, MPI
 */
public class ImportTranscriberMA extends FrameMenuAction {
    /**
     * Creates a new ImportTranscriberMA instance.
     *
     * @param name the name of the action (command)
     * @param frame the associated frame
     */
    public ImportTranscriberMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Shows an import Transcriber dialog and creates a new transcription.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        ImportTranscriberDialog dialog = new ImportTranscriberDialog(frame);
        Object value = dialog.showDialog();

        if (value == null) {
            return;
        }

        DecoderInfo decInfo = (DecoderInfo) value;

        if (decInfo.getSourceFilePath() == null) {
            return;
        }

        try {
            /*
               String path = fullPath;
               Preferences.set("LastUsedTranscriberDir",
                   (new File(path)).getParent(), null);
               // replace all backslashes by forward slashes
               path = path.replace('\\', '/');
             */

            //long before = System.currentTimeMillis();
            //Transcription transcription = new TranscriptionImpl(new File(path).getAbsolutePath());
            String path = decInfo.getSourceFilePath();
            Transcription transcription = new TranscriptionImpl(path, decInfo);

            //long after = System.currentTimeMillis();
            //System.out.println("open eaf took " + (after - before) + "ms");
            transcription.setChanged();

            int lastSlash = path.lastIndexOf('/');
            String transcriberPath = path.substring(0, lastSlash);
            boolean validMedia = true;

            if (frame != null) {
                validMedia = frame.checkMedia(transcription, transcriberPath);
            }

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

            FrameManager.getInstance().createFrame(transcription);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

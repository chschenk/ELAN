package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.FileExtension;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.event.ActionEvent;

import java.io.File;

import javax.swing.JOptionPane;


/**
 * Action that starts an Import CHAT sequence.
 *
 * @author Han Sloetjes, MPI
 */
public class ImportCHATMA extends FrameMenuAction {
    /**
     * Creates a new ImportCHATMA instance.
     *
     * @param name the name of the action (command)
     * @param frame the associated frame
     */
    public ImportCHATMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Shows an import CHAT dialog and creates a new transcription.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
		JOptionPane.showMessageDialog(frame, ElanLocale.getString("ExportCHATDialog.Message.CLANutility"), 
				"ELAN", JOptionPane.INFORMATION_MESSAGE);
		FileChooser chooser = new FileChooser(frame);
		chooser.createAndShowFileDialog(ElanLocale.getString("Frame.ElanFrame.OpenDialog.Title"), FileChooser.OPEN_DIALOG, 
			FileExtension.CHAT_EXT, "LastUsedCHATDir");
		
        File fileTemp = chooser.getSelectedFile();
        if(fileTemp != null){    
            //open the cha and get the Transcription from it
            try {
                // When config files are possible check if eaf or configuration file
                //           String path = chooser.getSelectedFile().getAbsolutePath();
                String path = fileTemp.getAbsolutePath(); 
                // replace all backslashes by forward slashes
                path = path.replace('\\', '/');

                //long before = System.currentTimeMillis();
                Transcription transcription = new TranscriptionImpl(new File(
                            path).getAbsolutePath());

                //long after = System.currentTimeMillis();
                //System.out.println("open eaf took " + (after - before) + "ms");
                transcription.setChanged();

                int lastSlash = path.lastIndexOf('/');
                String chatPath = path.substring(0, lastSlash);
                boolean validMedia = true;

                if (frame != null) {
                    frame.checkMedia(transcription, chatPath);
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
}

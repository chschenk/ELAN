package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.ImportCSVDialog;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.delimitedtext.DelimitedTextDecoderInfo;

import java.awt.event.ActionEvent;

import java.io.File;


/**
 * Shows a file chooser and next creates an import options window for tab
 * delimited or csv file.
 *
 * @author Han Sloetjes
 */
public class ImportDelimitedTextMA extends FrameMenuAction {
    /**
     * Creates a new ImportDelimitedTextMA instance
     *
     * @param name the name of the menu
     * @param frame the ELAN frame
     */
    public ImportDelimitedTextMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates a chooser to select a .csv or .txt file and next creates an
     * import  delimited text dialog.
     *
     * @param e action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	FileChooser chooser = new FileChooser(frame);
    	chooser.createAndShowFileDialog(ElanLocale.getString(
                "Frame.ElanFrame.OpenDialog.Title"), FileChooser.OPEN_DIALOG,
                FileExtension.CSV_EXT, "LastUsedCSVDir");
        File csvFile = chooser.getSelectedFile();
        String fullPath = null;
        if (csvFile != null) {
            fullPath = chooser.getSelectedFile().getAbsolutePath();
            File fileTemp = new File(fullPath);
           
            csvFile = fileTemp;
            // don't check if file is a '.csv' or '.txt' file
            /*
               if (fileTemp.toString().toLowerCase().endsWith(".csv") == false &&
                       fileTemp.toString().toLowerCase().endsWith(".txt") == false) {
                   String strMessage = ElanLocale.getString("Menu.Dialog.Message1");
                   strMessage += fullPath;
                   strMessage += ElanLocale.getString("Menu.Dialog.Message3");
                   String strError = ElanLocale.getString("Message.Error");
                   JOptionPane.showMessageDialog(frame, strMessage, strError,
                       JOptionPane.ERROR_MESSAGE);
                   return;
               }
             */

            ImportCSVDialog dialog = new ImportCSVDialog(frame, fileTemp);
            Object value = dialog.showDialog();
            
            if (value == null || !(value instanceof DelimitedTextDecoderInfo)) {
                return;
            }
            
            DelimitedTextDecoderInfo decInfo = (DelimitedTextDecoderInfo) value;
            try {
                
                String path = fullPath;               
                // replace all backslashes by forward slashes
                path = path.replace('\\', '/');

                //long before = System.currentTimeMillis();
                //Transcription transcription = new TranscriptionImpl(new File(path).getAbsolutePath());
                path = decInfo.getSourceFilePath();
                Transcription transcription = new TranscriptionImpl(path, decInfo);

                //long after = System.currentTimeMillis();
                //System.out.println("open eaf took " + (after - before) + "ms");
                transcription.setChanged();

                FrameManager.getInstance().createFrame(transcription);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
        }
    }
}

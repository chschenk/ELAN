package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.ExportWordListDialog;


/**
 * Creates a dialog to select tiers from multiple files for a word list export.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ExportWordsMultiMA extends AbstractProcessMultiMA {
    /**
     * Creates a new ExportWordsMultiMA instance
     *
     * @param name name of the action
     * @param frame the containing frame
     */
    public ExportWordsMultiMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates a <code>{@link ExportWordListDialog}</code>
     *
     * @param e the event!
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	List<File> files = getMultipleFiles(frame,
                ElanLocale.getString("ExportDialog.WordList.Title"));

        if ((files == null) || (files.size() == 0)) {
            return;
        }

        // create a exportwordlistdialog with these files to allow tier selection
        new ExportWordListDialog(frame, true, files).setVisible(true);
    }
}

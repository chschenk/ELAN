package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.ExportWordListDialog;
/**
 * Creates a dialog to select tiers from multiple files for a annotation list
 * export.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ExportAnnotationsMultiMA extends AbstractProcessMultiMA {
    /**
     * Creates a new ExportAnnotationsMultiMA instance
     *
     * @param name name of the action
     * @param frame the containing frame
     */
    public ExportAnnotationsMultiMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates an <code>{@link ExportWordListDialog}</code>.
     *
     * @param e the event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        List<File> files = getMultipleFiles(frame,
                ElanLocale.getString("ExportDialog.AnnotationList.Title"));

        if ((files == null) || (files.size() == 0)) {
            return;
        }

        // create a exportwordlistdialog with these files to allow tier selection
        new ExportWordListDialog(frame, true, files,
            ExportWordListDialog.ANNOTATIONS).setVisible(true);
    }
}

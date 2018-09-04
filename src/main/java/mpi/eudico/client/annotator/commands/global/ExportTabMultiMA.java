package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.ExportTabDialog;


/**
 * Creates a dialog to select tiers from multiple files for tab delimited text
 * export.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ExportTabMultiMA extends AbstractProcessMultiMA {
    /**
     * Creates a new ExportTabMultiMA instance
     *
     * @param name name of the action
     * @param frame the containing frame
     */
    public ExportTabMultiMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates an ExportTabDialog.
     *
     * @param e the event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        List<File> files = getMultipleFiles(frame,
                ElanLocale.getString("ExportTiersDialog.Title"));

        if ((files == null) || files.isEmpty()) {
            return;
        }

        new ExportTabDialog(frame, true, files).setVisible(true);
    }
}

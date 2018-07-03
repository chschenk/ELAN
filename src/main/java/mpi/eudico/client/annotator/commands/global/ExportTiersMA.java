package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.ExportTiersDialog;


/**
 * Creates an exporting tiers dialog
 *
 * @author Jeffrey Lemein
 * @version March 2010
 */
@SuppressWarnings("serial")
public class ExportTiersMA extends AbstractProcessMultiMA {
    /**
     * Creates a dialog for exporting tiers
     *
     * @param name action name
     * @param frame the parent frame
     */
    public ExportTiersMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates a export tiers dialog
     */
    @Override
    public void actionPerformed(ActionEvent e) { 	
    	List<File> files = getMultipleFiles(frame,
                ElanLocale.getString("ExportTabDialog.Title"));

        if ((files == null) || (files.size() == 0)) {
            return;
        }
        
        new ExportTiersDialog(frame, true, files).setVisible(true);
    }
}

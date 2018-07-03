/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.eudico.client.annotator.export;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.util.Transcription2TeX;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * $Id: ExportTeXDialog.java 44062 2015-07-16 14:15:18Z olasei $
 *
 * @author $Author$
 * @version Aug 2005 Identity removed
 */
@SuppressWarnings("serial")
public class ExportTeXDialog extends AbstractTierExportDialog {
 
    /**
     * Creates a new ExportTabDialog instance
     *
     * @param parent DOCUMENT ME!
     * @param modal DOCUMENT ME!
     * @param transcription DOCUMENT ME!
     * @param selection DOCUMENT ME!
     */
    public ExportTeXDialog(Frame parent, boolean modal,
        TranscriptionImpl transcription, Selection selection) {
        super(parent, modal, transcription, selection);
        makeLayout();
        extractTiers();
        updateLocale();
        postInit();
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        String filename = "resources/testdata/elan/elan-example2.eaf";
        TranscriptionImpl transcription = new TranscriptionImpl(filename);
        JFrame frame = new JFrame();
        javax.swing.JDialog dialog = new ExportTeXDialog(frame, false,
                transcription, null);
        dialog.setVisible(true);
    }

    /**
     * @see mpi.eudico.client.annotator.AbstractExportDialog#export(File)
     */
    @Override
	public boolean startExport() throws IOException {
        String fileExtension = "tex";
        File exportFile = promptForFile("ExportTeXDialog.Title",
               null, new String[] { fileExtension }, false);

        if (restrictCheckBox.isSelected()) {
            Transcription2TeX.exportTiers(transcription,
                getSelectedTiers().toArray(new String[0]),
                exportFile, selection.getBeginTime(),
                selection.getEndTime());
        } else {
            Transcription2TeX.exportTiers(transcription,
                getSelectedTiers().toArray(new String[0]),
                exportFile);
        }

        return true;
    }

    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
        if (model != null) {
            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                model.removeRow(i);
            }

            if (transcription != null) {
                List<TierImpl> v = transcription.getTiers();
                model.extractTierNames(v);
            }
        }
    }

    @Override
	protected void makeLayout() {
        super.makeLayout();

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(restrictCheckBox, gridBagConstraints);
    }

    @Override
	protected void updateLocale() {
    		super.updateLocale();
        setTitle(ElanLocale.getString("ExportTeXDialog.Title"));
        titleLabel.setText(ElanLocale.getString("ExportTeXDialog.TitleLabel"));
    }
}

/*
 * File:     ExportStatistics.java
 * Project:  MPI Linguistic Application
 * Date:     28 January 2007
 *
 * Feature added by Ouriel Grynszpan, European contract MATHESIS IST-027574
 * CNRS UMR 7593, Paris, France
 *
 * Copyright (C) 2001-2005  Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A dialog for exporting statistics for a tier as a tab delimited text file.
 * <p>
 * Except that the dialog is never shown, and inherits from a type of dialog
 * which facilitates selection of tiers, which isn't used...
 * <p>
 * It only uses it for its promptForFile() method.
 *
 * @author Ouriel Grynszpan
 */
@SuppressWarnings("serial")
public class ExportStatistics extends AbstractTierExportDialog {
    /** a tab char */
    final static public String TAB = "\t";
    final static private String NEWLINE = "\n";
    private JTable statTable;

    /**
     * Creates a new ExportStatistics instance
     *
     * @param parent the parent frame
     * @param modal the modal property
     * @param transcription the transcription
     * @param statTable the statistics table
     */
    public ExportStatistics(Frame parent, boolean modal,
        TranscriptionImpl transcription, JTable statTable) {
        super(parent, modal, transcription, null);
        this.statTable = statTable;

        try {
            startExport();
        } catch (Exception ee) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                "(" + ee.getMessage() + ")",
                ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Set the localized text on ui elements.
     *
     * @see mpi.eudico.client.annotator.export.AbstractTierExportDialog#updateLocale()
     */
    @Override
	protected void updateLocale() {
        setTitle(ElanLocale.getString("ExportTabDialog.Title"));
    }

    /**
     * Starts the actual exporting process.
     *
     * @return true if export succeeded
     *
     * @throws IOException can occur when writing to the file
     * @throws NullPointerException when the table is null
     */
    @Override
	protected boolean startExport() throws IOException {
        if (null == statTable) {
            throw new NullPointerException("The statistics table is null");
        }

        // prompt for file name and location
        File file = promptForFile(ElanLocale.getString("ExportTabDialog.Title"),
               null, FileExtension.TEXT_EXT, true);

        if (file == null) {
            return false;
        }

        FileOutputStream out = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(out, encoding);

        BufferedWriter writer = new BufferedWriter(osw);
        writer.write(toTabDelimitedText());
        writer.close();

        return true;
    }

    /**
     * Converts table to String, columns are separated by tabs rows are
     * separated by new lines.
     *
     * @return the tab delimited string
     */
    private String toTabDelimitedText() {
        String text = "";

        for (int j = 0; j < statTable.getColumnCount(); j++) {
            text += statTable.getColumnName(j);
            text += TAB;
        }

        text += NEWLINE;

        for (int i = 0; i < statTable.getRowCount(); i++) {
            for (int j = 0; j < statTable.getColumnCount(); j++) {
                text += statTable.getValueAt(i, j);
                text += TAB;
            }

            text += NEWLINE;
        }

        return text;
    }
}

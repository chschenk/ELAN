package mpi.eudico.client.annotator.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.grid.AnnotationTable;
import mpi.eudico.client.annotator.grid.GridViewerTableModel;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationCore;


/**
 * A class for exporting the contents of the Annotation table in the 
 * GridViewer or a search result frame.
 *
 * @author Han Sloetjes
 * @version 1.0, feb 2005
 */
public class ExportGridTable {
    /**
     * Constructor.
     */
    public ExportGridTable() {
    }

    /**
     * Exports the contents of hte table to a tab delimited text file.
     *
     * @param table the annotationtable
     */
    public void exportTableAsTabDelimitedText(AnnotationTable table) {
        if (table == null) {
            return;
        }
		String encoding = FileChooser.UTF_8;
		
		FileChooser chooser = new FileChooser(null);
		chooser.createAndShowFileAndEncodingDialog(ElanLocale.getString("ExportTabDialog.Title"), FileChooser.SAVE_DIALOG,
				FileExtension.TEXT_EXT, "LastUsedExportDir", encoding);
        File exportFile = chooser.getSelectedFile();
        if (exportFile != null) {
			encoding = chooser.getSelectedEncoding();			
        } else {
        	return;
        }

        // exportFile should not be null here
        BufferedWriter writer = null;

        GridViewerTableModel dataModel = null;
        boolean filtering = false;
        try {
            FileOutputStream out = new FileOutputStream(exportFile);
			OutputStreamWriter osw = null;
			try {
				osw = new OutputStreamWriter(out, encoding);
			} catch (UnsupportedCharsetException uce) {
				osw = new OutputStreamWriter(out, FileChooser.UTF_8);
			}
            writer = new BufferedWriter(osw);

            dataModel = table.getModel();
            filtering = dataModel.isFiltering();
            dataModel.setFiltering(false);
            
            List<String> visColumns = new ArrayList<String>();
			boolean tierNameColumnPresent = false;
			
            for (int i = 0; i < dataModel.getColumnCount(); i++) {
                String columnName = dataModel.getColumnName(i);

                if (table.isColumnVisible(columnName)) {
                    visColumns.add(columnName);
					if (columnName.equals(GridViewerTableModel.TIERNAME)) {
						tierNameColumnPresent = true;
					}
                }
            }
            
            String tierName = "";
			if (!tierNameColumnPresent && dataModel.getRowCount() > 0) {
            	for (int i = 0; i < dataModel.getColumnCount(); i++) {
					if (visColumns.contains(dataModel.getColumnName(i))) {
						Object o = dataModel.getValueAt(0, i);

						if (o instanceof Annotation) {
							tierName = ((Annotation) o).getTier().getName();
							break;
						}
					}
            	}           	
            }
            
			// first row are the table header values
			for (int i = 1; i < dataModel.getColumnCount(); i++) {
				if (visColumns.contains(dataModel.getColumnName(i))) {
					String header = (String)table.getColumnModel().getColumn(i).
						getHeaderValue();
					// replace 'Annotation' by the tier's name
					if (!tierNameColumnPresent && dataModel.getColumnName(i).
						equals(GridViewerTableModel.ANNOTATION) &&
						tierName.length() > 0) {
						header = tierName;
					}
					writer.write(header + "\t");
				}
			}
			writer.write("\n");
			
            // loop over rows and columns
            for (int i = 0; i < dataModel.getRowCount(); i++) {
                // the first column is the crosshair time indicator column, skip
                for (int j = 1; j < dataModel.getColumnCount(); j++) {
                    if (visColumns.contains(dataModel.getColumnName(j))) {
                        Object o = dataModel.getValueAt(i, j);

                        if (o instanceof Annotation) {
                            writer.write(((Annotation) o).getValue().replace('\n',
                                    ' '));
                        } else if (o instanceof AnnotationCore) {
                            writer.write(((AnnotationCore) o).getValue()
                                          .replace('\n', ' '));
                        } else if (o != null) {
                            writer.write(o.toString());
                        } else {
                            writer.write("");
                        }

                        writer.write("\t");
                    }
                }

                writer.write("\n");
            }

            writer.flush();
            writer.close();
        } catch (Exception ex) {
            // FileNotFound, IO, Security, Null etc
            JOptionPane.showMessageDialog(table,
                ElanLocale.getString("ExportDialog.Message.Error"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);
        } finally {
        	if(dataModel != null) {
				dataModel.setFiltering(filtering);
			}
            try {
                writer.close();
            } catch (Exception ee) {
            }
        }
    }
}

package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;


/**
 * A pane with a message label and a table with 2 columns; a checkbox column
 * and a column with strings identifying the transcriptions or the windows.
 * The (string) values of the checked rows will be returned by
 * getSelectedValues().
 *
 * @author Han Sloetjes, MPI
 */
@SuppressWarnings("serial")
public class ChangedTranscriptionsPane extends JPanel implements ListSelectionListener {
    private JLabel messageLabel;
    private JTable transTable;
    private JScrollPane pane;
    private DefaultTableModel model;
    private final int checkW = 30;
    private final int tableW = 380;

    /**
     * Creates the panel and initializes the table.
     *
     * @param changedTrans string representation of the transcriptions (name,
     *        path, url) or their windows
     */
    public ChangedTranscriptionsPane(List<String> changedTrans) {
        initComponents();
        fillTable(changedTrans);
    }

    private void initComponents() {
        messageLabel = new JLabel("<html>" +
                ElanLocale.getString("Frame.ElanFrame.UnsavedMultiple1") +
                "<br>" +
                ElanLocale.getString("Frame.ElanFrame.UnsavedMultiple2") +
                "</html>");
        //messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 16f));
        model = new DefaultTableModel() {
                    @Override
					public boolean isCellEditable(int row, int column) {
                        if (column > 0) {
                            return false;
                        }

                        return true;
                    }
                };
        model.setColumnCount(2);

        transTable = new JTable(model);
        transTable.setTableHeader(null);
        transTable.getColumnModel().getColumn(0)
                  .setCellEditor(new DefaultCellEditor(new JCheckBox()));
        transTable.getColumnModel().getColumn(0)
                  .setCellRenderer(new CheckBoxTableCellRenderer());
        transTable.getColumnModel().getColumn(0).setMaxWidth(checkW);
        transTable.getColumnModel().getColumn(0).setMinWidth(checkW);
        transTable.getColumnModel().getColumn(1)
                  .setPreferredWidth(tableW - checkW - 4);
        transTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        transTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transTable.getSelectionModel().addListSelectionListener(this);
;
        pane = new JScrollPane(transTable);
        pane.setPreferredSize(new Dimension(tableW, 80));

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 6, 10, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(messageLabel, gbc);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(pane, gbc);
    }
    
    /**
     * Adds a row to the table for each transcription with unsaved data.
     * The first column of each row is a Boolean, defaults to true, the second 
     * contains the transcription/frame name.
     * After the rows have been added the (minimum) width of the name column is calculated.
     * @param changedTrans a list containing the names of the changed transcriptions
     */
    private void fillTable(List<String> changedTrans) {
        if (changedTrans != null) {
            String ws = "";
            String loopString;

            for (int i = 0; i < changedTrans.size(); i++) {
                loopString = (String) changedTrans.get(i);
                model.addRow(new Object[] { Boolean.TRUE, loopString });

                if (loopString.length() > ws.length()) {
                    ws = loopString;
                }
            }

            if (ws.length() > 0) {
                // calculate pixel width
                int w = transTable.getFontMetrics(transTable.getFont())
                                  .stringWidth(ws);

                if (w > (tableW - checkW)) {
                    transTable.getColumnModel().getColumn(1).setMinWidth(w + 4);
                }
            }
        }
    }

    /**
     * Updates the checked state of the export checkboxes.
     *
     * @param lse the list selection event
     */
    @Override
	public void valueChanged(ListSelectionEvent lse) {
        if ((model != null) && lse.getValueIsAdjusting()) {
            int b = lse.getFirstIndex();
            int e = lse.getLastIndex();
            int col = 0;

            for (int i = b; i <= e; i++) {
                if (transTable.isRowSelected(i)) {
	                Object curValue = model.getValueAt(i, 0);
	                if (curValue instanceof Boolean) {
	                    if ((Boolean)curValue == Boolean.TRUE) {
	                        model.setValueAt(Boolean.FALSE, i, col);
	                    } else {
	                        model.setValueAt(Boolean.TRUE, i, col);
	                    }
	                }
                }
            }
        }
    }
    
    /**
     * Returns the values that are selected in the table.
     *
     * @return the selected values
     */
    public List<String> getSelectedValues() {
        if (model != null) {
            List<String> vals = new ArrayList<String>(model.getRowCount());

            for (int i = 0; i < model.getRowCount(); i++) {
                if (((Boolean) model.getValueAt(i, 0)).booleanValue()) {
                    vals.add((String)model.getValueAt(i, 1));
                }
            }

            return vals;
        }

        return null;
    }
}

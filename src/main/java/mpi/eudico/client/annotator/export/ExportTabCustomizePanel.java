package mpi.eudico.client.annotator.export;

import mpi.eudico.client.util.CheckBoxTableCellRenderer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;


/**
 * A panel with a table that supports selection and reordering of columns.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
@SuppressWarnings("serial")
public class ExportTabCustomizePanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private String[] headers;
    private boolean[] selected;

    /**
     * Creates a new ExportTabCustomizePanel instance based on the 
     * specified column names and the specified selected state flags.
     * The 2 arrays should be of the same length.  
     *
     * @param headers the column header values
     * @param selected the initial selected state of the columns
     */
    public ExportTabCustomizePanel(String[] headers, boolean[] selected) {
        this.headers = headers;
        this.selected = selected;
        initComponents();
    }
    
    /**
     * Creates a new ExportTabCustomizePanel instance based on the 
     * specified column names, setting all columns deselected.
     *
     * @param headers the column header values
     */
    public ExportTabCustomizePanel(String[] headers) {
        this.headers = headers;
        initComponents();
    }
    
    /**
     * Creates a new ExportTabCustomizePanel instance based on the 
     * specified column names, setting the columns at the specified indices selected.
     * 
     * @param headers the column header values
     * @param selectedIndices the indices of the selected columns
     */
    public ExportTabCustomizePanel(String[] headers, int[] selectedIndices) {
        this.headers = headers;
        selected = new boolean[headers.length];
        for(int i = 0; i < selectedIndices.length; i++) {
        	if (selectedIndices[i] < selected.length) {
        		selected[selectedIndices[i]] = true;
        	}
        }
        
        initComponents();
    }
    


    /**
     * Returns the order of the columns in the table. 
     *
     * @return the column order
     */
    public String[] getColumnOrder() {
        if (table.getTableHeader() != null) {
            Enumeration<TableColumn> colEnum = table.getTableHeader()
                                                    .getColumnModel()
                                                    .getColumns();
            List<String> colNames = new ArrayList<String>(table.getTableHeader()
                                                               .getColumnModel()
                                                               .getColumnCount());

            while (colEnum.hasMoreElements()) {
                colNames.add((String) colEnum.nextElement().getHeaderValue());
            }

            //System.out.println("CN: " + colNames);
            return colNames.toArray(new String[] {  });
        } else {
            return null;
        }
    }

    /**
     * Returns the indices of the selected columns.
     *
     * @return the indices of the selected columns
     */
    public int[] getSelectedColumnIndices() {
        List<Integer> colInd = new ArrayList<Integer>(headers.length);
        Boolean value;

        for (int i = 0; i < table.getColumnCount(); i++) {
            value = (Boolean) table.getValueAt(0, i);

            if (value.booleanValue()) {
                colInd.add(i);
            }
        }

        //System.out.println("SC: " + colInd);
        int[] sel = new int[colInd.size()];

        for (int i = 0; i < colInd.size(); i++) {
            sel[i] = colInd.get(i);
        }

        return sel;
    }

    /**
     * Returns the names of the selected columns, in left to right order.
     *
     * @return the names of the selected columns
     */
    public String[] getOrderedSelectedColumns() {
        String[] allCols = getColumnOrder();

        if (allCols != null) {
            int[] selCols = getSelectedColumnIndices();
            String[] selColNames = new String[selCols.length];

            for (int i = 0; i < selCols.length; i++) {
                if (selCols[i] < allCols.length) {
                    selColNames[i] = allCols[selCols[i]];
                }
            }

            return selColNames;
        }

        return null;
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);
        GridBagConstraints gridBagConstraints;
        model = new DefaultTableModel(headers, 1);

        for (int i = 0; i < headers.length; i++) {
            if (selected != null && i < selected.length) {
                model.setValueAt(Boolean.valueOf(selected[i]), 0, i);
            } else {
                model.setValueAt(Boolean.FALSE, 0, i);
            }
        }

        table = new JTable(model);

        JCheckBox cb = new JCheckBox();
        cb.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultCellEditor cbEditor = new DefaultCellEditor(cb);
        CheckBoxTableCellRenderer cbRenderer;
        cbRenderer = new CheckBoxTableCellRenderer();
        cbRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellEditor(cbEditor);
            table.getColumnModel().getColumn(i).setCellRenderer(cbRenderer);
        }
        int rowHeight = table.getRowHeight();
        table.setRowHeight((int)(1.5 * rowHeight));
        int rowMarg = table.getRowMargin();
        table.setRowMargin(Math.max(3, (int)(1.5 * rowMarg)));
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(new JScrollPane(table), gridBagConstraints);
        
        // add a mouse listener that updates the tool tip text with the name 
        // of the column the mouse pointer is over
        table.getTableHeader().addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				int col = ((JTableHeader) e.getSource()).columnAtPoint(e.getPoint());
				if (col > -1) {
					Object val = ((JTableHeader) e.getSource()).getColumnModel().getColumn(col).getHeaderValue();
					if (val != null) {
						((JTableHeader) e.getSource()).setToolTipText(val.toString());
					}
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				// stub
			}
		});
    }
}

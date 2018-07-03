package mpi.eudico.client.annotator.md.imdi;

import java.awt.Component;
//import java.awt.FontMetrics;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * This Cell renderer uses a TextArea to render multiple lines of text.
 * Adjusting the height of the rows is handled by the table.
 */
public class MultiLineValueRenderer implements TableCellRenderer {
    /** The empty String */
    private static final String EMPTY_STRING = "";

    /** Border for selected cell */
    private final Border BORDER_SELECTED = UIManager.getBorder(
            "Table.focusCellHighlightBorder");

    /** Border for unselected cell */
    private final Border BORDER_EMPTY = new EmptyBorder(1, 2, 0, 2);

    /** Foreground color for focused cell */
//    private final Color FC_FOREGROUND = UIManager.getColor(
//            "Table.focusCellForeground");

    /** Background color for focused cell */
//    private final Color FC_BACKGROUND = UIManager.getColor(
//            "Table.focusCellBackground");
    
    private JTextArea textArea;

    /**
     * Creates a new instance
     * 
     * @param wrap if true both LineWrap and WrapStyleWord are set true,
     * if false the line wrapping of the values should be done beforehand
     */
    public MultiLineValueRenderer(boolean wrap) {
        super();
        textArea = new JTextArea();
        textArea.setOpaque(true);
        textArea.setWrapStyleWord(wrap);
        textArea.setLineWrap(wrap);
    }
    
    /**
     * Calls updateUI of the text area.
     */
    public void updateUI() {
    	if (textArea != null) {
    		textArea.updateUI();
    	}
    }
    
    /**
     * Returns a configured text area.
     */
    @Override
	public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
//    	if (isSelected) {
//            textArea.setForeground(table.getSelectionForeground());
//            textArea.setBackground(table.getSelectionBackground());
//        } else {
        	textArea.setForeground(table.getForeground());
        	textArea.setBackground(table.getBackground());
//        }
    	
    	if (textArea.getFont() != table.getFont()) {
    		textArea.setFont(table.getFont());
    	}

        if (hasFocus) {
        	textArea.setBorder(BORDER_SELECTED);

//            if (table.isCellEditable(row, column)) {
//                textArea.setForeground(FC_FOREGROUND);
//                textArea.setBackground(FC_BACKGROUND);
//            }
        } else {
        	textArea.setBorder(BORDER_EMPTY);
        }
        
        textArea.setText((value == null) ? EMPTY_STRING : value.toString());
        
    	return textArea;
    }
    
}

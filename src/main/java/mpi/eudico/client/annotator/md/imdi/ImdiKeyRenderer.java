package mpi.eudico.client.annotator.md.imdi;

import java.awt.Component;
import java.awt.FontMetrics;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * Renders metadata keys by removing a prepending index, the METATRANSCRIPT
 * element and MDGroup element etc.
 * After that, the key text is truncated, if needed, at text begin 
 * (least significant part).
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ImdiKeyRenderer extends DefaultTableCellRenderer {
    private FontMetrics metrics;
    private final String PREP = "...";

    /** Border for selected cell */
    private final Border BORDER_SELECTED = UIManager.getBorder(
            "Table.focusCellHighlightBorder");

    /** Border for unselected cell */
    private final Border BORDER_EMPTY = new EmptyBorder(1, 2, 0, 2);
    
    /**
	 * 
	 */
	public ImdiKeyRenderer() {
		super();
		setVerticalAlignment(SwingConstants.TOP);
	}

	/**
     * Changes the contents of the value for rendering.
     *
     * @param table the table
     * @param value the value from the table model
     * @param isSelected the selected state, ignored
     * @param hasFocus ignored, the table is not editable
     * @param row the row
     * @param column the column
     *
     * @return the renderer with modified text and possibly style
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
//        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
//            row, column);
 	   super.setForeground(table.getForeground());
	   super.setBackground(table.getBackground());
	   
	   setFont(table.getFont());

       if (hasFocus) {
       	super.setBorder(BORDER_SELECTED);
       } else {
       	super.setBorder(BORDER_EMPTY);
       }
       
        if (value instanceof String) {
            String text = (String) value;
            int index = -1;

            if (text.length() > 0) {
                if (text.startsWith("(")) {
                    index = text.indexOf(")");

                    if ((index > -1) && (index < (text.length() - 1))) {
                        text = text.substring(index + 1);
                    }
                }

                index = text.indexOf(ImdiConstants.METATRANSCRIPT);

                if ((index > -1) &&
                        (text.length() > ImdiConstants.METATRANSCRIPT.length())) {
                    text = text.substring(ImdiConstants.METATRANSCRIPT.length() +
                            1);
                }

                index = text.indexOf(ImdiConstants.MDGROUP);

                if ((index > -1) &&
                        (text.length() > (index +
                        ImdiConstants.MDGROUP.length() + 1))) {
                    text = text.replace((ImdiConstants.MDGROUP + "."), "");
                }
            }

            setText(getTruncatedString(text,
                    table.getCellRect(row, column, false).width));
        }

        return this;
    }

    /**
     * Recursively removes the part before the first "." if the text is wider
     * than the maximal (column) width. If any part has been removed, the rest
     * is prepended with "...".
     *
     * @param text the text to render
     * @param maxWidth the max available (column) width
     *
     * @return the truncated text
     */
    private String getTruncatedString(String text, int maxWidth) {
        metrics = getFontMetrics(getFont());

        int tw = metrics.stringWidth(text);

        if (tw < (maxWidth - 5)) {
            return text;
        }

        // recursively remove the part before the first "."
        String loopText = text;

        while (tw >= (maxWidth - 5)) {
            int index = text.indexOf('.');

            if (index == -1) {
                return text;
            }

            text = text.substring(index + 1);
            loopText = PREP + text;
            tw = metrics.stringWidth(loopText);
        }

        return loopText;
    }
}

package mpi.eudico.client.annotator.md.imdi;


import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableModel;

/**
 * A specialized table that keeps record of the height of different rows.
 *
 */
@SuppressWarnings("serial")
public class MDTable extends JTable implements ComponentListener {
	private int defaultRowHeight;
	private ImdiKeyRenderer keyRenderer;
	private MultiLineValueRenderer valRenderer;

	/**
	 * 
	 * @param dm
	 */
	public MDTable(TableModel dm) {
		super(dm);
		if (dm.getRowCount() > 0) {
			calculateRowHeights();
		}
		addComponentListener(this);
	}

	@Override
	protected void createDefaultRenderers() {
		super.createDefaultRenderers();
		// assume two columns, first with md keys, second with md values
		keyRenderer = new ImdiKeyRenderer();
		getColumn(dataModel.getColumnName(0)).setCellRenderer(keyRenderer);
		valRenderer = new MultiLineValueRenderer(true);
		getColumn(dataModel.getColumnName(1)).setCellRenderer(valRenderer);
		// Don't let our work here be discarded by createDefaultColumnsFromModel()!
		// Possible alternative: override createDefaultColumnsFromModel() instead.
		setAutoCreateColumnsFromModel(false);
	}

	@Override
	public void columnMarginChanged(ChangeEvent e) {
		// for listening to changes in width of the columns!
		super.columnMarginChanged(e);
		calculateRowHeights();
	}

	/**
	 * Calls super and updates the renderers. 
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		if (keyRenderer != null) {
			keyRenderer.updateUI();
		}
		if (valRenderer != null) {
			valRenderer.updateUI();
		}
	}

	/**
	 * Returns the default row height.
	 */
	@Override
	public int getRowHeight() {
		if (defaultRowHeight > 0) {
			return defaultRowHeight;
		} else {
			return super.getRowHeight();
		}
	}

	@Override
	public void setModel(TableModel dataModel) {
		super.setModel(dataModel);
		// recalculate row heights
		calculateRowHeights();
	}

	/**
	 * Returns the height of the specified row, if not calculated it returns the default height.
	 * 
	 *  @param row the row index
	 */
	@Override
	public int getRowHeight(int row) {
		return super.getRowHeight(row);
	}

	/**
	 * Always returns false.
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		calculateRowHeights();
		revalidate();
	}

	/**
	 * Recalculates the heights of all rows.
	 */
	public void calculateRowHeights() {
		int numRows = dataModel.getRowCount();
		Font f = getFont();
		if (f == null) {
			return;
		}
		FontMetrics metrics = getFontMetrics(f);
		if (metrics == null) {
			return;
		}
		//rowHeightsMap.clear();
		defaultRowHeight = metrics.getHeight() + 2;// can be done elsewhere?
		int columnWidth = getCellRect(0, 1, false).width - 4;// border.left, border.right
		String val;
		int numLines;
		
		for (int i = 0; i < numRows; i++) {
			val = (String) dataModel.getValueAt(i, 1);// hardcoded column index?
			if (val == null || val.length() == 0) {
				continue;
			}
			numLines = calculateNumLines(metrics, getGraphics(), columnWidth, val);
			int h = numLines * defaultRowHeight;// store in rowheight map?
			if (getRowHeight(i) != h) {
				setRowHeight(i, h);
			}
		}
	}

	/**
	 * Calculates the number of lines (roughly) needed for the text.
	 * Could store the wrapped text lines and use the textarea of the 
	 * renderer without wrapping.
	 * 
	 * @param fm font metrics
	 * @param g the graphics object
	 * @param width the width available for text
	 * @param text the text to render
	 * @return the number of lines
	 */
	private int calculateNumLines(FontMetrics fm, Graphics g, int width, String text) {
    	int totalWidth = (int) fm.getStringBounds(text, g).getWidth();
    	if (totalWidth <= width) {
    		return 1;
    	}

    	int numLines = 0;
    	StringBuilder builder = new StringBuilder(text);
    	int beginIndex = 0;
    	int endIndex = -1;
    	int lastTestedEndIndex = 0;
    	char[] seq;
    	
    	for (int i = 0; i < builder.length(); i++) {
    		endIndex++;
    		if (Character.isWhitespace(builder.charAt(i))) {
				if (endIndex > beginIndex) {
					seq = new char[endIndex - 1 - beginIndex];
					builder.getChars(beginIndex, endIndex - 1, seq, 0);
					if (fm.getStringBounds(seq, 0, seq.length, g).getWidth() > width) {//too wide
						numLines++;
						if (lastTestedEndIndex > beginIndex) {
							beginIndex = lastTestedEndIndex + 1;
						} else {// one word wider than width, divide width by available width, rounded up?
							beginIndex = endIndex + 1;
						}
					} else {
						lastTestedEndIndex = endIndex;
						
						if (builder.charAt(i) == '\n') {
							numLines++;
							beginIndex = endIndex + 1;
						}
						continue;
					}
				}// else { end of string length? }
			
    		}
    		
    		if (i == builder.length() - 1) {
    			if (endIndex > beginIndex) {//remaining line
    				seq = new char[endIndex - beginIndex];
					builder.getChars(beginIndex, endIndex, seq, 0);
					int w = (int) fm.getStringBounds(seq, 0, seq.length, g).getWidth();
					if (w > width) {
						// add the number of lines needed for the width of the remaining string, rounded up
						numLines += ((w / width) + 1);
					} else {
						numLines++;
					}
    			}
    		}
    	}
    	
    	return numLines == 0 ? 1 : numLines;
    }

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		calculateRowHeights();
		revalidate();
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}
}

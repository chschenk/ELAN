package mpi.eudico.client.util;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * A mouse motion listener that sets the tooltip to the value of the table
 * header.
 * 
 * @author Han Sloetjes
 */
public class TableHeaderToolTipAdapter extends MouseMotionAdapter {

	private JTableHeader header;

	/**
	 * Constructor.
	 * 
	 * @param header
	 *            the table header
	 */
	public TableHeaderToolTipAdapter(JTableHeader header) {
		super();
		this.header = header;
	}

	/**
	 * Sets the tooltip to the value of the column header at the mouse location.
	 * 
	 * @see java.awt.event.MouseMotionAdapter#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		if (header != null) {
			int colIndex = header.columnAtPoint(e.getPoint());
			if (colIndex > -1) {
				TableColumn col = header.getColumnModel().getColumn(colIndex);
				if (col != null && col.getHeaderValue() != null) {
					header.setToolTipText(col.getHeaderValue().toString());
				}
			}
		}
	}

}

package mpi.eudico.client.annotator.turnsandscenemode;

import java.awt.event.KeyEvent;

import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableModel;

/**
 * A custom table for the one-column-view of this mode.
 * Currently only processKeyBinding is overridden to return false,
 * but more functionality that is now e.g. in the viewer could be built-in. 
 */
@SuppressWarnings("serial")
public class TaSTable extends JTable {

	/**
	 * @param dm
	 */
	public TaSTable(TableModel dm) {
		super(dm);
	}

	@Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
			int condition, boolean pressed) {
		return false;
	}
	
}

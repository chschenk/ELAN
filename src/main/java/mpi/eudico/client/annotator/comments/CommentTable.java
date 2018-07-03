package mpi.eudico.client.annotator.comments;

import java.awt.Color;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.ScrollFriendlyTable;
import mpi.eudico.client.annotator.viewer.TableColumnHider;
import mpi.eudico.server.corpora.clom.Transcription;

@SuppressWarnings("serial")
public class CommentTable extends ScrollFriendlyTable {
	private final static String COLUMN_WIDTH_PREFS = ".Columns";
	private final static String COLUMN_ORDER_PREFS = ".Columns.Order";
	private final static String COLUMN_HIDDN_PREFS = ".Columns.Hidden";

	TableRowSorter<CommentTableModel> sorter;
	TableColumnHider hider;
	JScrollPane scrollCommentList;
	
	public CommentTable(CommentTableModel tableModel) {
		super(tableModel);

		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		sorter = new TableRowSorter<CommentTableModel>(tableModel);
		setRowSorter(sorter);
		setShowGrid(false);
		hider = new TableColumnHider(this);
		
		int ncols = tableModel.getColumnCount();
		String[] columnNames = new String[ncols];
		for (int i = 0; i < ncols; i++) {
			columnNames[i] = tableModel.getColumnName(i);
		}
		@SuppressWarnings("unused") // the hider uses it
		JPopupMenu tablePopup = hider.newPopupMenu(columnNames);
		// TODO: Some columns should truncate at the left if the column is too narrow.
		// Code examples that I have found are more complicated than it is worth.
		
		addMouseListener(hider);
	}

    /**
     * If the table is placed in a scroll pane, the user would like to have the popup
     * menu also in the empty area that isn't covered by the table.
     * Therefore the TableColumnHider must also listen to mouse events.
     * 
     * Also, make the background colour in the empty area identical to that
     * of the table.
     *  
     * @param scrollCommentList
     */
	public void setScrollPane(JScrollPane scrollCommentList) {
		this.scrollCommentList = scrollCommentList;

		scrollCommentList.addMouseListener(hider);
		scrollCommentList.getViewport().setBackground(getBackground());
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (scrollCommentList != null) {
			scrollCommentList.getViewport().setBackground(bg);
		}
	}
	
	/**
	 * If you want to filter the rows that are visible, use this shortcut function.
	 * 
	 * @param f
	 */
    public void setRowFilter(RowFilter<? super CommentTableModel, ? super Integer> f) {
    	sorter.setRowFilter(f);
    }

    // Implement table cell tool tips.
    @Override
    public String getToolTipText(MouseEvent e) {
        String tip = null;
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);

        try {
            tip = getValueAt(rowIndex, colIndex).toString();
        } catch (RuntimeException e1) {
            // catch null pointer exception if mouse is over an empty line
        	return "";
        }
        
		// It seems some versions of Java don't like tooltips a lot
		// wider than the screen, or some sort of maximum size like
		// that. 325 works for a test laptop with jre 1.7 on Linux but
		// is actually too small for the screen of my Mac (with jre 1.6)
		// (which needs no limitation, not even with java
		// look-and-feel).
		final int maxLength = 325;
		if (tip.length() < maxLength) {
			return tip;
		} else {
			return tip.substring(0, maxLength);
		}        	
    }
    
    /**
     * Set a typical bunch of listeners at once.
     * To avoid forcing the user to implement them all,
     * check which interfaces are actually implemented.
     * 
     * Do addMouseListener(),
     * 
     * 
     * @param l
     */
    public void addListeners(Object l) {
    	if (l instanceof MouseListener) {
    		addMouseListener((MouseListener) l);    		
    	}
    	if (l instanceof ListSelectionListener) {
    		getSelectionModel().addListSelectionListener((ListSelectionListener) l);
    	}
    	if (l instanceof KeyListener) {
    		addKeyListener((KeyListener) l);    		
    	}
    }

    public void savePreferences(String prefix, Transcription transcription) {
		Map<String, Integer> widthPrefs = new HashMap<String, Integer>();
		List<String> orderPrefs = new ArrayList<String>();
		
		TableColumnModel cm = getColumnModel();
		int num = getColumnCount();

		// Note: the TableColumnModel registers the view order of the columns,
		// not the conceptual order. It is unlike the TableModel in that way.
		for (int i = 0; i < num; i++) {
			TableColumn col = cm.getColumn(i);
			String name = (String)col.getHeaderValue();
			int width = cm.getColumn(i).getWidth();
			widthPrefs.put(name, Integer.valueOf(width));
			orderPrefs.add(name);
		}
		
		Preferences.set(prefix + COLUMN_WIDTH_PREFS, widthPrefs, transcription, false, false);
		Preferences.set(prefix + COLUMN_ORDER_PREFS, orderPrefs, transcription, false, false);
		
		Preferences.set(prefix + COLUMN_HIDDN_PREFS, hider.getPreferences(),
				transcription, false, false);
    }
    
	/**
	 * Set the preferred column widths based on the Preferences.
	 * Also restore their order.
	 * <p>
	 * <b>Note: This uses the GUI names of the columns in the preferences,
	 * which depend on the language.</b> 
	 */
    public void applyPreferences(String prefix, Transcription transcription) {
		// Set the widths of the columns.
    	
    	Map<String, Integer> widthPrefs = Preferences.getMapOfInt(prefix + COLUMN_WIDTH_PREFS, transcription);
		if (widthPrefs != null) {
			TableColumnModel cm = getColumnModel();
			int num = cm.getColumnCount();

			for (int i = 0; i < num; i++) {
				final TableColumn column = cm.getColumn(i);
				String name = (String)column.getHeaderValue();
				Integer width = widthPrefs.get(name);
				if (width != null) {
					column.setPreferredWidth(width);
					column.setWidth(width);
				}
			}
		}

		// Reorder the columns.
		
		List<String> orderPrefs = Preferences.getListOfString(prefix + COLUMN_ORDER_PREFS, transcription);
		if (orderPrefs != null) {
			TableColumnModel cm = getColumnModel();
			int numCols = getColumnCount();
			int num = orderPrefs.size();

			int to = 0;
			for (int i = 0; i < num && to < numCols; i++) {
				String name = orderPrefs.get(i);
				// Find where the column currently is,
				// and move it to position `to'.
				for (int from = to; from < numCols; from++) {
					TableColumn col = cm.getColumn(from);
					if (name.equals(col.getHeaderValue())) {
						cm.moveColumn(from, to);
						// Only increment `to' if the column was really found
						to++;
						break;
					}
 				}
			}
		}

		// Hide some columns. If not set, hide SENDER and RECIPIENT.

		List<String> hiddenPref = Preferences.getListOfString(prefix + COLUMN_HIDDN_PREFS, transcription);
		if (hiddenPref == null) {
			TableModel model = getModel();
			List<String> list = new ArrayList<String>(2);
			list.add(model.getColumnName(DefaultCommentTableModel.SENDER));
			list.add(model.getColumnName(DefaultCommentTableModel.RECIPIENT));
			hiddenPref = list;
		}

		hider.applyPreferences(hiddenPref);
    }
}

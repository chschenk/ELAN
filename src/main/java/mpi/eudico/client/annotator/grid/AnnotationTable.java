package mpi.eudico.client.annotator.grid;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clom.Annotation;

/**
 * JTable for showing Annotations
 * Extracted from GridViewer on Jun 29, 2004
 * @author Alexander Klassmann
 * @version Jun 29, 2004
 */
@SuppressWarnings("serial")
public class AnnotationTable extends JTable {
	private static int MIN_COLUMN_WIDTH = 15; // swing default
	
	/** stores width of parent component */
	private int width = -1;
	/** preferred font per tier */
	private Map<Object, Font> prefTierFonts;

	/** stores default minimal widths of columns */
	private static Map<String, Integer> preferredWidths = new HashMap<String, Integer>();

	/** stores default maximal widths of columns */
	private static Map<String, Integer> maxWidths = new HashMap<String, Integer>();
	private boolean deselectCommits = false;
	/** a mapping of tiers to mappings of CVEId values (controlled vocabulary entries) to preferred color */
	private Map<String, Map<String, Color>> annColorsMap;
	
	//annotation columns don't have a default width; cf adjustAnnotationColumns()
	static {
		preferredWidths.put(GridViewerTableModel.TIMEPOINT, Integer.valueOf(15));
		preferredWidths.put(GridViewerTableModel.COUNT, Integer.valueOf(40));
		preferredWidths.put(GridViewerTableModel.FILENAME, Integer.valueOf(100));
		preferredWidths.put(GridViewerTableModel.TIERNAME, Integer.valueOf(80));
		preferredWidths.put(GridViewerTableModel.LEFTCONTEXT, Integer.valueOf(100));
		preferredWidths.put(GridViewerTableModel.RIGHTCONTEXT, Integer.valueOf(100));
		//mod. Coralie Villes add Parent and child column
        preferredWidths.put(GridViewerTableModel.PARENT, Integer.valueOf(100));
        preferredWidths.put(GridViewerTableModel.CHILD, Integer.valueOf(100));
		preferredWidths.put(GridViewerTableModel.BEGINTIME, Integer.valueOf(80));
		preferredWidths.put(GridViewerTableModel.ENDTIME, Integer.valueOf(80));
		preferredWidths.put(GridViewerTableModel.DURATION, Integer.valueOf(80));
		maxWidths.put(GridViewerTableModel.TIMEPOINT, Integer.valueOf(15));
		maxWidths.put(GridViewerTableModel.COUNT, Integer.valueOf(40));
		maxWidths.put(GridViewerTableModel.BEGINTIME, Integer.valueOf(120));
		maxWidths.put(GridViewerTableModel.ENDTIME, Integer.valueOf(120));
		maxWidths.put(GridViewerTableModel.DURATION, Integer.valueOf(120));
	}

	/**
	 * 
	 * @see javax.swing.JTable#JTable(TableModel)
	 */
	public AnnotationTable(GridViewerTableModel dataModel) {
		super(dataModel);
		
		prefTierFonts = new HashMap<Object, Font>();
		setFont(Constants.DEFAULTFONT);
		setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		for (int i = 0; i < dataModel.getColumnCount(); i++) {
			TableColumn column = getColumnModel().getColumn(i);
			String columnName = dataModel.getColumnName(i);
			column.setIdentifier(columnName);
			setColumnVisible(columnName, true);
		}

		getColumn(GridViewerTableModel.TIMEPOINT).setResizable(false);
		getColumn(GridViewerTableModel.COUNT).setResizable(false);

		getTableHeader().setReorderingAllowed(false);

		//after this component gets visible, add component listener to parent component		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				//table is visible ?
				if (getWidth() > 0) {
					//component listener to parent component (e.g. viewport of scrollpane)
					getParent().addComponentListener(new ComponentAdapter() {
						//adjust columns if width of viewport changes
						@Override
						public void componentResized(ComponentEvent e) {
							if (getParent().getWidth() != width) {
								width = getParent().getWidth();
								adjustAnnotationColumns();
							}
						}
					});
					removeComponentListener(this);
					adjustAnnotationColumns();
				}
			}
		});
	}

	@Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		return false;
	}

    /**
     * Override to set rendering hints.
     */
	/*
	protected void paintComponent(Graphics g) {
		 only for J 1.4, new solutions for 1.5 and 1.6
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }    
        super.paintComponent(g);
	}
	*/
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return new Dimension(640, 200);
	}

	/**
	 * Restores the default row height after an inline edit operation.
	 * If this method is called by the GridEditor, the editor may be called to commit
	 * instead of cancel, depending on a preference flag.
	 * 
	 * @param e the change event
	 */
	@Override
	public void editingStopped(ChangeEvent e) {		
		if (deselectCommits && e.getSource() instanceof GridEditor) {
			((GridEditor) e.getSource()).commitEdit();
		}
		super.editingStopped(e);
		setRowHeight(getRowHeight());
	}

	/**
	 * Restores the default row height after an inline edit operation.
	 * 
	 * @param e the change event
	 */
	@Override
	public void editingCanceled(ChangeEvent e) {
		super.editingCanceled(e);
		setRowHeight(getRowHeight());
	}

	/**
	 * sets column width default value if true, to 0 if false
	 * @param columnName
	 * @param visible
	 */
	public void setColumnVisible(String columnName, boolean visible) {
		TableColumn column = null;
		try {
			column = getColumn(columnName);
			// attention: don't change order of settings! A.K.
			if (visible) {
				column.setResizable(true);
				column.setMaxWidth(getDefaultMaxWidth(columnName));
				column.setMinWidth(MIN_COLUMN_WIDTH);
				column.setPreferredWidth(getDefaultPreferredWidth(columnName));
			}
			else {
				column.setMinWidth(0);
				column.setMaxWidth(0); // -> preferredWidth = 0
				column.setResizable(false);
			}
		}
		catch (IllegalArgumentException e) {
			System.out.println("Warning : no column with name " + columnName);
		}
	}

	/**
	 * distributes remaining table space among annotation columns
	 * to be called after other columns had been set/removed or component resized
	 */
	protected void adjustAnnotationColumns() {
		if (getParent() != null) {
			Set<TableColumn> visibleAnnotationColumns = new HashSet<TableColumn>();
			int sumOfOtherWidths = 0;
			for (int i = 0; i < getColumnCount(); i++) {
				TableColumn column = getColumnModel().getColumn(i);
				if (!preferredWidths.containsKey(column.getIdentifier()) && column.getMaxWidth() > 0) {
					visibleAnnotationColumns.add(column);
				}
				else {
					sumOfOtherWidths += column.getPreferredWidth();
				}
			}

			int remainingSpace = getParent().getWidth() - sumOfOtherWidths;
			if (remainingSpace > 0) {
				for (Iterator<TableColumn> iter = visibleAnnotationColumns.iterator(); iter.hasNext();) {
					iter.next().setPreferredWidth(
						remainingSpace / visibleAnnotationColumns.size());
				}
			}
		}
	}

	/**
	 * returns if column is visible (e.g. width>0)
	 * @param columnName
	 * @return boolean
	 */
	public boolean isColumnVisible(String columnName) {
		TableColumn column = null;
		try {
			column = getColumn(columnName);
		}
		catch (IllegalArgumentException e) {
			System.out.println("Warning : no column with name " + columnName);
		}
		return column != null ? column.getWidth() > 0 : false;
	}

	/**
	 * Returns default width for columns.
	 * @param columnName the column name
	 * @return the default preferred width
	 */
	private int getDefaultPreferredWidth(String columnName) {
		return preferredWidths.containsKey(columnName)
			? preferredWidths.get(columnName).intValue()
			: MIN_COLUMN_WIDTH;
		//swing default
	}

	/**
	 * Returns default width for columns.
	 * @param columnName the column name
	 * @return the default max width
	 */
	private int getDefaultMaxWidth(String columnName) {
		return maxWidths.containsKey(columnName)
			? maxWidths.get(columnName).intValue()
			: Integer.MAX_VALUE;
		//swing default
	}

	/**
	  * method from ElanLocaleListener not implemented in AbstractViewer
	  */
	public void updateLocale() {
		for (int i = 0; i < dataModel.getColumnCount(); i++) {
			int index = getColumnModel().getColumnIndex(dataModel.getColumnName(i));
			getColumnModel().getColumn(index).setHeaderValue(
				ElanLocale.getString("Frame.GridFrame." + dataModel.getColumnName(i)));
		}
	}

	public void setFontSize(int fontSize) {
		setFont(Constants.DEFAULTFONT.deriveFont((float) fontSize));
		setRowHeight((fontSize + 2 < 16 ? 16 : fontSize + 3));
		// update preferred fonts
		if (prefTierFonts.size() > 0) {
			Object key;
			Font f;
			Iterator<Object> keyIt = prefTierFonts.keySet().iterator();
			while (keyIt .hasNext()) {
				key = keyIt.next();
				f = prefTierFonts.get(key);
				if (f != null) {
					prefTierFonts.put(key, f.deriveFont((float)fontSize));
				}
			}
		}
	}

	public int getFontSize() {
		return getFont().getSize();
	}
	
	/**
	 * Sets the names of the fonts for tiers. Based on this map and the current
	 * fontsize Font objects are created.
	 * 
	 * @param fontNames a map of tier name to font name<br>
	 * NB the table model identifier for the annotations of the main tier in the 
	 * table is used for that tier instead of its name 
	 */
	public void setFontsForTiers(Map<String, String> fontNames) {
		prefTierFonts.clear();
		if (fontNames != null) {
			for (Map.Entry<String, String> e : fontNames.entrySet()) {
				String key = e.getValue();
				String fn = e.getValue();
				if (fn != null) {
					prefTierFonts.put(key, new Font(fn, Font.PLAIN, getFontSize()));
				}
			}
		}	
	}
	
	/**
	 * Returns the font for the specified column. Only applicable for annotation
	 * columns.
	 * 
	 * @param column the column index
	 * @return the (preferred) font
	 */
	public Font getFontForColumn(int column) {
		if (dataModel instanceof GridViewerTableModel && 
				prefTierFonts.size() > 0) {
			String name = ((GridViewerTableModel) dataModel).getColumnName(column);
			Font f = prefTierFonts.get(name);
			if (f == null) {
				return getFont();
			} else {
				return f;
			}
		}
		
		return getFont();
	}

	/**
	 * Sets the colors from CV entries to be used based on CV Ids.
	 * 
	 * Strictly speaking, this map isn't really needed. The color can be found
	 * from the Annotation->Tier->Linguistic Type->CV->Entry(by Id)->Color.
	 * This can be cached as it is looked up, in particular the TierName->CV.
	 * 
	 * @param cMap the mapping, per tier an CVEId to color mapping
	 */
	public void setColorsForAnnotations(Map<String, Map<String, Color>> cMap) {
		annColorsMap = cMap;
	}
	
	/**
	 * Returns the preferred color for an annotation, based on controlled vocabulary preferences.
	 * 
	 * @param a the Annotation for which to find a color.
	 * @return a color or null.
	 */
	public Color getColorForAnnotation(Annotation a) {
		String cveId = a.getCVEntryId();
		if (cveId == null || cveId.isEmpty()) {
			return null;
		}
		
		if (annColorsMap != null && annColorsMap.size() > 0) {
			String tierName = a.getTier().getName();
			Map<String, Color> colors = annColorsMap.get(tierName);
			if (colors != null) {
				return colors.get(cveId);
			}
		}
		
		return null;
	}
	
	/**
	 * toggles string representation of time (HH:MM:SS.sss versus milliseconds)
	 */
	public void toggleTimeFormat() {
		if (dataModel instanceof GridViewerTableModel) {
			String timeFormat =
				GridViewerTableModel.HHMMSSsss.equals(
					((GridViewerTableModel) dataModel).getTimeFormat())
					? GridViewerTableModel.MILLISECONDS
					: GridViewerTableModel.HHMMSSsss;
			((GridViewerTableModel) dataModel).setTimeFormat(timeFormat);

			repaint();
		}
	}

	/**
	 * Sets the time format for the table. Supported are hh:mm:ss:ms, pal, ntsc, ms
	 * 
	 * @param format the format
	 */
	public void setTimeFormat(String format) {
		if (format != null) {
			((GridViewerTableModel) dataModel).setTimeFormat(format);
			
			repaint();
		}
	}
	
	/**
	 * invokes adjustment of annotation columns
	 * @see javax.swing.event.TableModelListener#tableChanged(TableModelEvent)
	 */
	@Override
	public void tableChanged(TableModelEvent e) {
		super.tableChanged(e);
		adjustAnnotationColumns();
	}

	/**
	 * When true deselecting a cell that is being edited leads to a commit of the changes.
	 * @param deselectCommits The deselectCommits to set.
	 */
	public void setDeselectCommits(boolean deselectCommits) {
		this.deselectCommits = deselectCommits;
	}
	
	/**
	 * This table was constructed with a GridViewerTableModel,
	 * so we can safely return it.
	 */
	@Override
	public GridViewerTableModel getModel() {
		return (GridViewerTableModel)super.getModel();
	}
}

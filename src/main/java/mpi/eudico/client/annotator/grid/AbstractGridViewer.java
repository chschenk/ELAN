package mpi.eudico.client.annotator.grid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.SelectionUser;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.server.corpora.clom.AnnotationCore;

/**
 * Class which shows the annotations on a selected tier in a table.<br>
 * Apart from the annotation values it can show begintime, endtime and 
 * duration of each annotation. The annotations are numbered.
 * Media time, selection and active annotation are marked by the table
 * renderer. 
 * The viewer has two modes: a single tier mode, where the annotations of only 
 * one tier are shown in the table and a multi tier mode, where a parent tier 
 * is displayed together with depending tiers that have a symbolic association
 * with the parent tier.
 */
@SuppressWarnings("serial")
abstract public class AbstractGridViewer extends AbstractViewer{

	/**
	 * 
	 */
	protected GridViewerPopupMenu popup;

	/**
	 * 
	 */
	private JScrollPane scrollPane;

	/**
	 * 
	 */
	protected AnnotationTable table;

	/**
	 * 
	 */
	protected GridViewerTableModel dataModel;

	private int lastRowIndex = -1;
	boolean isCreatingAnnotation = false;

	/** A logger to replace System.out calls. */
	protected static final Logger LOG = Logger.getLogger(GridViewer.class.getName());

	public AbstractGridViewer(AnnotationTable table) {
		this.table = table;
		this.dataModel = table.getModel();
		initTable();
		placeTable();
		popup = new GridViewerPopupMenu(table);
		updateLocale();
	}

	protected void initTable() {
		table.setDefaultRenderer(Object.class, createTableCellRenderer());
		setTableListener();
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {				    
					popup.show(AbstractGridViewer.this.table, e.getPoint().x, e.getPoint().y);
				}
			}
		});
		table.setFont(Constants.DEFAULTFONT);
		table.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
	}

	protected TableCellRenderer createTableCellRenderer() {
		return new GridRenderer(this, dataModel);
	}

	private void placeTable() {
		setLayout(new BorderLayout());
		scrollPane = new JScrollPane(table);
		scrollPane.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Needed so components don't disappear when resizing the frame too small
	 * ??
	 * @return the minimum size
	 */
	@Override
	public Dimension getMinimumSize() {
		return table.getPreferredScrollableViewportSize();
	}

	/**
	 * Needed so components don't disappear when resizing the frame too small
	 * ??
	 * @return the preferred size
	 */
	@Override
	public Dimension getPreferredSize() {
		return table.getPreferredScrollableViewportSize();
	}

	/**
	 * Notification that the selection has been changed from SelectionUser.
	 * 
	 * @see SelectionUser
	 */
	@Override
	public void updateSelection() {
		repaint();
	}

	/**
	 * method from ElanLocaleListener not implemented in AbstractViewer
	 */
	@Override
	public void updateLocale() {
		table.updateLocale();
		if (popup != null) {
			popup.updateLocale();
		}
		//popup = new GridViewerPopupMenu(table);
		repaint();
	}

	/**
	 * This method sets listeners for the table
	 */
	protected void setTableListener() {
		table.getSelectionModel().addListSelectionListener(
			new AnnotationTableListSelectionListener(this, table));
	}

	/**
	 * Needed so the current annotation / red triangle is always visible
	 */
	private void correctLastRowIndex() {
		int annSize = dataModel.getRowCount();

		if (annSize == 0) {
			lastRowIndex = 0;
		}

		long mediatime = getMediaTime();

		//if button 'go to begin' is clicked, scroll to first row
		if (mediatime == 0) {
			lastRowIndex = 0;

			return;
		}

		//if button 'go to end' is clicked, scroll to last row
		if (mediatime >= getMediaDuration()) {
			lastRowIndex = annSize - 1;

			return;
		}

		int annotationColumn = dataModel.findColumn(GridViewerTableModel.ANNOTATION);
		AnnotationCore aa = null;
		for (int i = 0; i < annSize; i++) {
			aa = (AnnotationCore) dataModel.getValueAt(i, annotationColumn);

			if ((mediatime >= aa.getBeginTimeBoundary())
				&& (mediatime < aa.getEndTimeBoundary())) {
				lastRowIndex = i;

				return;
			}
		}
	}

	/**
	 * Needed so the current annotation / red triangle is always visible
	 */
	private void scrollIfNeeded() {
		//if needed scroll so current annotation is visible
		if (lastRowIndex != -1) {
			if (lastRowIndex >= table.getRowCount()) {
				lastRowIndex = table.getRowCount() - 1;
			}
			synchronized (table) {
				Rectangle viewportRectangle = scrollPane.getViewport().getViewRect();
				Rectangle tableRectangle = table.getCellRect(lastRowIndex, 0, true);

				if (!viewportRectangle.contains(tableRectangle)) {
					tableRectangle.height = viewportRectangle.height - tableRectangle.height;
					table.scrollRectToVisible(tableRectangle);
				}
			}
		}
	}

	/**
	 * AR notification that some media related event happened method from
	 * ControllerListener not implemented in AbstractViewer
	 *
	 * @param event DOCUMENT ME!
	 */
	@Override
	public void controllerUpdate(ControllerEvent event) {
		doUpdate();
		table.repaint();
	}

	protected void doUpdate() {
		correctLastRowIndex();
		scrollIfNeeded();
		table.adjustAnnotationColumns();
	}

	@Override
	public void setBackground(Color color) {
		scrollPane.getViewport().setBackground(color);
		super.setBackground(color);
	}
	
	/**
	 * Returns the current font size.
	 * 
	 * @return the current font size
	 */
	public int getFontSize() {
		return table.getFontSize();
	}

	/**
	 * Sets the font size.
	 * 
	 * @param size the new font size
	 */
	public void setFontSize(int size) {
		if (popup != null) {
			popup.setFontSize(size);
		}
	}

}
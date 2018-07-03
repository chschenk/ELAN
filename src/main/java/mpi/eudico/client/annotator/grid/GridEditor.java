package mpi.eudico.client.annotator.grid;

import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.InlineEditBoxListener;
import mpi.eudico.client.annotator.gui.InlineEditBox;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * CellEditor for editing annotations
 * Extracted from GridViewer on Jun 29, 2004
 * @version November 2004
 */
@SuppressWarnings("serial")
public class GridEditor extends DefaultCellEditor {
	final private AbstractViewer viewer;
	final private GridViewerTableModel tableModel;
	final private String EMPTY = "";
	private Annotation annotation;
	private InlineEditBox inlineEditBox;
	// flag for the inline edit box
	private boolean enterCommits = true;// May 2013: changed historic default

	/**
	 * Creates a new GridEditor instance
	 */
	public GridEditor(AbstractViewer viewer, GridViewerTableModel dataModel) {
		super(new JTextField());
		getComponent().setEnabled(false);
		this.viewer = viewer;
		this.tableModel = dataModel;
	}

	/**
	 * Configures a textediting component, possibly an inline editbox and
	 * returns it.
	 *
	 * @param table the table containing the cell
	 * @param value the current value of the table cell
	 * @param isSelected the selected state of the cell
	 * @param row the row index of the cell
	 * @param column the column index of the cell
	 *
	 * @return a text editing component
	 */
	@Override
	public Component getTableCellEditorComponent(
		JTable table,
		Object value,
		boolean isSelected,
		int row,
		int column) {
		annotation = null;
		if (inlineEditBox == null) {
			inlineEditBox = new InlineEditBox(true);
			if(viewer instanceof InlineEditBoxListener){
				inlineEditBox.addInlineEditBoxListener((InlineEditBoxListener)viewer);
			}
		}
		if (table.getColumnName(column).equals(GridViewerTableModel.ANNOTATION)) {
			annotation = (Annotation) tableModel.getAnnotationCore(row);
			configureEditBox(table, row, column);
			inlineEditBox.startEdit();
			return inlineEditBox.getEditorComponent();
		}
		else if (value instanceof Annotation) {
			annotation = (Annotation) value;
			configureEditBox(table, row, column);
			inlineEditBox.startEdit();
			return inlineEditBox.getEditorComponent();
		}
		else if (value instanceof String) {
			try {
				String tierName = tableModel.getColumnName(column);
				AbstractAnnotation parentAnn =
					(AbstractAnnotation) tableModel.getAnnotationCore(row);
				if (tierName != null && parentAnn != null) {
					TierImpl childTier =
						((TranscriptionImpl) parentAnn.getTier().getTranscription()).getTierWithId(
							tierName);
					if (childTier != null) {
						TierImpl parentTier = childTier.getParentTier();
						long time =
							(parentAnn.getBeginTimeBoundary() + parentAnn.getEndTimeBoundary()) / 2;
						if (parentTier == parentAnn.getTier()
							|| parentTier.getAnnotationAtTime(time) != null) {
							// make sure the next ACMEditEvent does not result in an update of the tablemodel
							// and a repaint; this would cancel the inline edit box
							//	GridViewer.this.isCreatingAnnotation = true;
							annotation = childTier.createAnnotation(time, time);
						}
					}
				}

				if (annotation != null) {
					tableModel.setValueAt(annotation, row, column);
					configureEditBox(table, row, column);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							if (inlineEditBox != null) {
								inlineEditBox.startEdit();
							}
						}
					});
					return inlineEditBox.getEditorComponent();
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
				//	LOG.warning(LogUtil.formatStackTrace(ex));
				return getComponent();
			}
		}
		System.out.println(
			"Warning: Cell ("
				+ row
				+ ","
				+ column
				+ ") not handled by editor. Should not be editable!");
		return getComponent();
	}

	private void configureEditBox(JTable table, int row, int column) {
		viewer.setActiveAnnotation(annotation);
		inlineEditBox.setAnnotation(annotation);
		inlineEditBox.setEnterCommits(enterCommits);
		table.setRowHeight(row, (int) (1.5 * table.getRowHeight()));
		Font ff = table.getFont();
		if (table instanceof AnnotationTable) {
			ff = ((AnnotationTable) table).getFontForColumn(column);
		}
		
		if (inlineEditBox.isUsingControlledVocabulary()) {
			inlineEditBox.configureEditor(
				JComboBox.class,
				ff,
				table.getCellRect(row, column, true).getSize());
		}
		else {
			inlineEditBox.configureEditor(
				JScrollPane.class,
				ff,
				table.getCellRect(row, column, true).getSize());
		}
	}

	/**
	 * Returns the value of the editor component
	 *
	 * @return the value of the editor component
	 */
	@Override
	public Object getCellEditorValue() {
		if (annotation != null) {
			return annotation.getValue();
		}
		else {
			return EMPTY;
		}
	}

	public void updateLocale() {
		if (inlineEditBox != null) {
			inlineEditBox.updateLocale();
		}
	}
	
	/**
	 * Called from the AnnotationTable when the edited cell is deselected. Normally this
	 * cancels the edit, but with the proper user preference setting the edit 
	 * will be comitted here.
	 */
	public void commitEdit() {
		if (inlineEditBox != null) {
			inlineEditBox.commitEdit();
		}
	}

    /**
     * Sets the flag that determines that Enter commits without modifier.
     * 
     * @param enterCommits the Enter commits flag
     */
    public void setEnterCommits(boolean enterCommits) {
    	    this.enterCommits = enterCommits;
    }

	/**
	 * Cancel the inline edit box.
	 */
	@Override
	public void cancelCellEditing() {
		//super.cancelCellEditing();
		
	}

	public void setKeyStrokesNotToBeConsumed(List<KeyStroke> ksList){
		if (inlineEditBox != null) {
			inlineEditBox.setKeyStrokesNotToBeConsumed(ksList);
		}
	}
}

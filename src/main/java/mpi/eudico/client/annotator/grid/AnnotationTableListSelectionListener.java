package mpi.eudico.client.annotator.grid;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationCore;

/**
 * Created on Oct 22, 2004
 * @author Alexander Klassmann
 * @version Oct 22, 2004
 */
public class AnnotationTableListSelectionListener implements ListSelectionListener {
	final protected AbstractViewer viewer;
	final protected JTable table;

	/**
	 * Update active annotation and selection in AbstractViewer
	 */
	public AnnotationTableListSelectionListener(AbstractViewer viewer, JTable table) {
		this.viewer = viewer;
		this.table = table;
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {

		if (e.getValueIsAdjusting() == false) {
			return;
		}

		if (table.getSelectedRowCount() <= 0) {
			return;
		}

		GridViewerTableModel tableModel = (GridViewerTableModel) table.getModel();

		//if value in selected column is instance of Annotation
		//(e.g. children Annotation in MultiTierViewer), take this, 
		//else take 'main' annotationCore
		Object object = tableModel.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
		AnnotationCore ann =
			object instanceof Annotation
				? (Annotation) object
				: tableModel.getAnnotationCore(table.getSelectedRow());

		if (ann instanceof Annotation) {
			viewer.setActiveAnnotation((Annotation) ann);
		}

		// HS 4 dec 03: setActiveAnnotation should handle setSelection; only in case of more selected rows
		// (mouse drag) the selection may be set here
		if (table.getSelectedRowCount() > 1) {
			int[] rows = table.getSelectedRows();
			long selectedBeginTime = tableModel.getAnnotationCore(rows[0]).getBeginTimeBoundary();
			long selectedEndTime =
				tableModel.getAnnotationCore(rows[rows.length - 1]).getEndTimeBoundary();

			viewer.setSelection(selectedBeginTime, selectedEndTime);
		}
	}
}

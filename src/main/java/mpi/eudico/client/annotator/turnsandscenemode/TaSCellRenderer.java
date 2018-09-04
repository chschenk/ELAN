package mpi.eudico.client.annotator.turnsandscenemode;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * A renderer for a row in the view. Extend the TaSCellPanel, which class 
 * is also used as the cell editor.
 * The preferred size of that panel is used for storing the height of each row. 
 */
@SuppressWarnings("serial")
public class TaSCellRenderer extends TaSCellPanel implements TableCellRenderer {
	/** the viewer is used for retrieving media time and time selection etc. */
	private TurnsAndSceneViewer viewer;
	
	public TaSCellRenderer(TurnsAndSceneViewer viewer) {
		super();
		this.viewer = viewer;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		TaSAnno tasAnn = null;
		if (value instanceof TaSAnno) {
			tasAnn = (TaSAnno) value;
			setTaSAnnotation(tasAnn);
		} else {
			setTaSAnnotation(null);
		}
		
		setFont(table.getFont());
		setSelected(isSelected);
		// get info from viewer concerning time selection and crosshair, maybe active annotation?
		
		boolean inTimeSelection = false;
		if (tasAnn != null) {
			if (viewer.getSelectionBeginTime() < viewer.getSelectionEndTime()) {
				inTimeSelection = viewer.getSelectionBeginTime() < tasAnn.getEndTime() && 
						viewer.getSelectionEndTime() > tasAnn.getBeginTime();
			}
			setDecorations(inTimeSelection, 
					tasAnn.getBeginTime() <= viewer.getMediaTime() && viewer.getMediaTime() < tasAnn.getEndTime(), 
							viewer.getActiveAnnotation() != null && tasAnn.getAnnotation() == viewer.getActiveAnnotation(),
							false /* is active gap */);
		}
		return this;
	}
	
}

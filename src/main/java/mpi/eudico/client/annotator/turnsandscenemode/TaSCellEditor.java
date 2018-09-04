package mpi.eudico.client.annotator.turnsandscenemode;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * A table cell editor that uses a TaSCellPanel component in editing mode. 
 * The same panel is used as a cell renderer.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class TaSCellEditor extends DefaultCellEditor {
	/**
	 * the viewer is used to obtain information about media position and time selection.
	 * the editor might be connected as listener directly instead?
	 */
	private TurnsAndSceneViewer viewer;
	private TaSCellPanel editorPanel;
	private Object curValue;
	
	/**
	 * Constructor with the pre-configured TaSCellPanel as one of the arguments.
	 * @param viewer the "host" viewer
	 * @param editorPanel the actual cell editor component
	 */
	public TaSCellEditor(TurnsAndSceneViewer viewer, TaSCellPanel editorPanel) {
		super(new JTextField());
		setClickCountToStart(1);
		this.viewer = viewer;
		this.editorPanel = editorPanel;
	}

	@Override
	public Component getComponent() {
		return editorPanel;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		curValue = value;
		TaSAnno tasAnn = null;
		if (value instanceof TaSAnno) {
			tasAnn = (TaSAnno) value;
			editorPanel.setTaSAnnotation(tasAnn);
		}
		
		editorPanel.setFont(table.getFont());
		//editorPanel.setSelected(isSelected);
		// get info from viewer concerning time selection and crosshair, maybe active annotation?
		
		if (tasAnn != null) {
			updateDecoration(tasAnn);
		}
		
		editorPanel.startEditing();
		
		return editorPanel;
	}
	
	public void startEditing() {
		editorPanel.startEditing();
	}

	@Override
	public Object getCellEditorValue() {
		// create an object containing the text and the caret position and to which the current media time
		// can be added on the level of the viewer
		return curValue;
	}

	@Override
	public boolean stopCellEditing() {
		// TODO maybe check if edited text should be stored
		return super.stopCellEditing();
	}

	@Override
	public void cancelCellEditing() {
		// TODO discard changes or commit them
		super.cancelCellEditing();
	}

	@Override
	protected void fireEditingStopped() {
		super.fireEditingStopped();
	}

	@Override
	protected void fireEditingCanceled() {
		super.fireEditingCanceled();
	}
	
	/**
	 * Decoration here means indication of selected time interval, media play head position,
	 * maybe active annotation?
	 */
	private void updateDecoration(){
		if (curValue instanceof TaSAnno) {
			updateDecoration((TaSAnno) curValue);
		}
	}

	/**
	 * @param tasAnno the current segment, not null!
	 */
	private void updateDecoration(TaSAnno tasAnno){
		boolean inTimeSelection = false;			
		if (viewer.getSelectionBeginTime() < viewer.getSelectionEndTime()) {
			inTimeSelection = viewer.getSelectionBeginTime() < tasAnno.getEndTime() && 
					viewer.getSelectionEndTime() > tasAnno.getBeginTime();
		}
		editorPanel.setDecorations(inTimeSelection, 
				tasAnno.getBeginTime() <= viewer.getMediaTime() && viewer.getMediaTime() < tasAnno.getEndTime(), 
						viewer.getActiveAnnotation() != null && tasAnno.getAnnotation() == viewer.getActiveAnnotation(),
						tasAnno.getAnnotation() == null);
	}
	
	public void updatMediaTime(long mediaTime) {
		editorPanel.updatMediaTime(mediaTime);
	}
	
	public void repaint() {
		updateDecoration();
	}
	
}

package mpi.eudico.client.annotator.transcriptionMode;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * Cell Editor for the transcription table
 * 
 * @author Aarthy Somasundaram
 *
 */
@SuppressWarnings("serial")
public class TranscriptionTableCellEditor extends DefaultCellEditor {	
	
	private static final String EMPTY = "";
	private Annotation annotation;
	private TranscriptionTableEditBox inlineEditBox;
	private TranscriptionViewer viewer;
	private int startEditInOneClick = 1;
	
	/**
	 * Creates an instance of TranscriptionTableCellEditor
	 *
	 * @param viewer	 
	 */
	public TranscriptionTableCellEditor(TranscriptionViewer viewer) {
		super(new JTextField());
		getComponent().setEnabled(false);
		this.viewer =viewer;
		setClickCountToStart(startEditInOneClick);
	}

	@Override
	public Component getTableCellEditorComponent(
		JTable table,
		Object value,
		boolean isSelected,
		int row,
		int column) {	
		annotation = null;
		if (inlineEditBox == null) {
			inlineEditBox = new TranscriptionTableEditBox(viewer, (TranscriptionTable)table);
		}
		
		if (value instanceof Annotation) {
			annotation = (Annotation) value;
			configureEditBox(table, row, column);
			viewer.updateMedia(annotation.getBeginTimeBoundary(),annotation.getEndTimeBoundary());
			if(viewer.isAutoPlayBack()){
				viewer.playInterval(annotation.getBeginTimeBoundary(),annotation.getEndTimeBoundary());
			}
			inlineEditBox.startEdit();	
			return inlineEditBox.getEditorComponent();
		} else if (value instanceof AnnotationCellPlaceholder) {
			AnnotationCellPlaceholder cellPH = (AnnotationCellPlaceholder) value;
			if (cellPH.canCreate) {
				//asynchronously creates a new annotation
				createAnnotation( table, row, column);
					
				return getComponent();	
			}
		}
			
		return getComponent();
	}
	
	 public TranscriptionTableEditBox getEditorComponent(){
	       if (inlineEditBox != null) {
	            return inlineEditBox;
	        }
	        return null;
	 }
	
	/**
	 * TODO reconsider. move creation to viewer and wait for ACM event instead
	 * Creates a new annotation on the current selected cell
	 * 
	 * @param table 
	 * @param row
	 * @param column
	 * 
	 * @version Feb. 2017 Moved creation of the new annotation to the viewer, where 
	 * an undoable action is used to create the annotation. After receiving an ACM edit event
	 * the table model is updated and editing starts
	 */
	private void createAnnotation(JTable table, int row, int column){	
		int columnNo = table.convertColumnIndexToModel(column);
		int columnIndexInMap = columnNo - 1 ;
				
		AbstractAnnotation ann = null;
		TierImpl currentTier = null;
		long beginTime = 0L;
		long endTime = 0L;
		
		Object val;
		for(int i= 1; i< table.getColumnCount(); i++){
			if(i == column){
				continue;
			}			
			val =  table.getValueAt(row, i);
			if(val instanceof Annotation){
				 ann = (AbstractAnnotation)val;
				 break;
			}
		}
		
		// if there is no annotation for reference, get the time info from the
		// string value of the current cell			
		if (ann == null) {	
			Object valueObj = table.getValueAt(row, column);
			if (valueObj instanceof AnnotationCellPlaceholder) {
				AnnotationCellPlaceholder cellPH = (AnnotationCellPlaceholder) valueObj;
				beginTime = cellPH.bt;
				endTime = cellPH.et;		
				currentTier = (TierImpl) (viewer.getViewerManager().getTranscription()).
						getTierWithId(cellPH.tierName);
			}
		} else {
			beginTime = ann.getBeginTimeBoundary();
			endTime = ann.getEndTimeBoundary();
			TierImpl linkedTier = (TierImpl) ann.getTier();			
			if(linkedTier.getLinguisticType().getConstraints() != null &&
					linkedTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){				
				
				TierImpl parentTier = (TierImpl) linkedTier.getParentTier();
				while(parentTier.getLinguisticType().getConstraints() != null && 
						parentTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
					parentTier = (TierImpl) parentTier.getParentTier();
				}
				currentTier = (viewer.getTierMap().get(parentTier).get(columnIndexInMap));
			} else {
				currentTier =  (viewer.getTierMap().get(linkedTier).get(columnIndexInMap));
			}
		}
		
		if(currentTier != null){
			final TierImpl targetTier = currentTier;
			final long bt = beginTime;
			final long et = endTime;
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					viewer.createAnnotation(targetTier, bt, et);
				}
			});
//			
//			if(currentTier.isTimeAlignable()){
//				//newAnnotation =  currentTier.createAnnotation(beginTime, endTime);				
//			} else {				
//				long time =	(beginTime + endTime) / 2;
//				//newAnnotation = ((TierImpl) currentTier).createAnnotation(time, time);
//			}
		}		

	}

	/**
	 * Configures the edit box, for the active cell
	 * in the table
	 * 
	 * @param table
	 * @param row
	 * @param column
	 */
	private void configureEditBox(JTable table, int row, int column) {			
		inlineEditBox.setAnnotation(annotation);
		//table.setRowHeight(row, (int) (1.5 * table.getRowHeight(row)));
		//table.setRowHeight(row, (int) (table.getRowHeight(row)));
		Font ff = null;
		if (table instanceof TranscriptionTable) {
			ff = ((TranscriptionTable) table).getFontForTier( ((Annotation)table.getValueAt(row, column)).getTier().getName() );
			if(ff == null){
				ff = table.getFont();
			}
			
			ff = new Font(ff.getFontName(), ff.getStyle(), ((TranscriptionTable) table).getFontSize());
		}
		
		if (inlineEditBox.isUsingControlledVocabulary()) {
			table.setRowHeight(row, 120);
			inlineEditBox.configureEditor(
					JScrollPane.class, ff,
				table.getCellRect(row, column, true).getSize());
		} else {			
			inlineEditBox.configureEditor(
				JTextArea.class,	ff,
				table.getCellRect(row, column, true).getSize());
		}
	}
	
	@Override
	public Object getCellEditorValue() {
		if (annotation != null) {
			return annotation;
		}
		else {
			return EMPTY;
		}
	}
	
	public void showPopUp(Component comp, int x, int y) {
		if (inlineEditBox != null)
			inlineEditBox.showPopUp(comp, x, y);
	}

	public void updateLocale() {
		if (inlineEditBox != null)
			inlineEditBox.updateLocale();
	}
	
	public void commitChanges() {
		if (inlineEditBox != null && inlineEditBox.isDeselectCommitChanges()){
			inlineEditBox.commitChanges();
		}
	}

	@Override
	public void cancelCellEditing() {
		//super.cancelCellEditing();
		if (inlineEditBox != null) {
			inlineEditBox.cancelEdit();
		}
	}
}

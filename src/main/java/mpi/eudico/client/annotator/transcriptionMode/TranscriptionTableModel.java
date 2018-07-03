package mpi.eudico.client.annotator.transcriptionMode;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

/**
 * Table model for the transcription table
 * 
 * @author aarsom
 *  
 */
@SuppressWarnings("serial")
public class TranscriptionTableModel extends DefaultTableModel {
	// as of October 2016 the column type identifiers, the tier types in the order of configuration
	public static final String NUM_COLUMN_ID = "@No@";
	private String columnIdentifiers[] = {NUM_COLUMN_ID}; 
	private List<String> nonEditableTiers;	
	private boolean autoCreateAnn = true;

	
	 public TranscriptionTableModel(){		 
		 for (int i = 0; i < columnIdentifiers.length; i++) {
			this.addColumn(columnIdentifiers[i]);
		 }
		 setColumnIdentifiers(columnIdentifiers);
	 }
	 
	 @Override
	public boolean isCellEditable(int row, int column) {		
		 Object obj = getValueAt(row, column);
		 String tierName = getTierName(row, column);
		 if(tierName != null && nonEditableTiers != null && nonEditableTiers.contains(tierName)){
			 return false;			 
		 }	
		 if(obj instanceof AnnotationCellPlaceholder){
			 return ((AnnotationCellPlaceholder) obj).canCreate && autoCreateAnn;
		 } else{
			 return ( obj instanceof Annotation);
		 }
	 }
	 
	 /**
	  * The passed column names are the names of the tier types, the prefix visible in
	  * the table header is added here. The first column showing the row index is always
	  * there and its label is not part of the passed names.
	  * 
	  * @param columnNames the names of the tier types per column
	  */
	 public void updateModel(final List<String> columnNames){
		 columnIdentifiers = new String[columnNames.size() + 1];
		 columnIdentifiers[0] = NUM_COLUMN_ID;
		 
		 for(int i = 0; i < columnNames.size(); i++){
			 columnIdentifiers[i + 1] = columnNames.get(i);
		 }
		 
		 setColumnIdentifiers(columnIdentifiers);

	 }
	 
	 /**
	  * Updates a column identifier after a change in a type's name.
	  * 
	  * @param oldName the old name of the type
	  * @param newName the new name
	  */
	 public void updateTypeName(String oldName, String newName) {
		 for (int i = 1; i < columnIdentifiers.length; i++) {
			 if (columnIdentifiers[i].equals(oldName)) {
				 columnIdentifiers[i] = newName;
				 setColumnIdentifiers(columnIdentifiers);				 
				 return;
			 }
		 }
	 }
	 
	 /**
	  * As identifiers for the columns are used the type names, not the rendered
	  * column header value (which contains localized strings).
	  * 
	  * @return the column identifiers, the tier types per column
	  */
	 public String[] getColumnIdentifiers(){
		 return columnIdentifiers;
	 }
	 
	 public void setNonEditableTiers(List<String> nonEditableTiers){
		this.nonEditableTiers = nonEditableTiers; 
	 }
	 
	 public void setAutoCreateAnnotations(boolean create){
		 autoCreateAnn = create; 
	 }
	 
	 public boolean isAnnotationsCreatedAutomatically(){
		 return autoCreateAnn;
	 }
	 
	 /**
	  * Returns the name of the tier in the given cell
	  * 
	  * @param row
	  * @param column
	  * @return the tier name
	  */
	 private String getTierName(int row, int column){ 
	   	String tierName = null;	    	
	   	Object val = getValueAt(row, column);
	   	if(val instanceof Annotation){
	   		AbstractAnnotation ann = (AbstractAnnotation)val;
	   		 tierName = ann.getTier().getName();
	   	} else if (val instanceof AnnotationCellPlaceholder){
	   		return ((AnnotationCellPlaceholder)val).tierName;
	   	}

	   	return tierName;
	 }

}

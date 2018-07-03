package mpi.eudico.client.annotator.multiplefilesedit;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class TableListener implements TableModelListener{
	@Override
	public void tableChanged(TableModelEvent e) {
		int row = e.getFirstRow();
		int col = e.getColumn();
		if(row >=0 && col>=0 ){
			//TableModel model = (TableModel)e.getSource();
			//String column_name = model.getColumnName(col);
			//Object data = model.getValueAt(row, col);
		}
	}

}

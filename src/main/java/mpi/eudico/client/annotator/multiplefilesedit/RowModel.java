package mpi.eudico.client.annotator.multiplefilesedit;

import java.util.HashMap;
import java.util.Map;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class RowModel {
	private Map<Integer, TableCellEditor> editor_data;
	private Map<Integer, TableCellRenderer> renderer_data;

	public RowModel() {
		editor_data = new HashMap<Integer, TableCellEditor>();
		renderer_data = new HashMap<Integer, TableCellRenderer>();
	}

	public void addRendererForRow(int row, TableCellRenderer e) {
		renderer_data.put(Integer.valueOf(row), e);
	}

	public void removeRendererForRow(int row) {
		renderer_data.remove(Integer.valueOf(row));
	}

	public TableCellRenderer getRenderer(int row) {
		return renderer_data.get(Integer.valueOf(row));
	}
	
	public void addEditorForRow(int row, TableCellEditor e) {
		editor_data.put(Integer.valueOf(row), e);
	}

	public void removeEditorForRow(int row) {
		editor_data.remove(Integer.valueOf(row));
	}

	public TableCellEditor getEditor(int row) {
		return editor_data.get(Integer.valueOf(row));
	}
}

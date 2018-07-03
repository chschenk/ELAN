package mpi.eudico.client.annotator.linkedmedia;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;


/**
 * A TableModel for a table displaying information on other, non audio/video
 * file  descriptors.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class LFDescriptorTableModel extends LinkedFilesTableModel {
	private List<LinkedFileDescriptor> descriptors;

    /**
     * Constructs an empty LFDescriptorTableModel.
     */
    public LFDescriptorTableModel() {
        this(new ArrayList<LinkedFileDescriptor>(0));
    }

    /**
     * Constructs a LFDescriptorTableModel and fills the model with  the data
     * from the specified Vector.
     *
     * @param descriptors the collection of LinkedFileDescriptors
     */
    public LFDescriptorTableModel(List<LinkedFileDescriptor> descriptors) {
        this.descriptors = (descriptors != null) ? descriptors : new ArrayList<LinkedFileDescriptor>();

        columnIds = new ArrayList<String>();
        columnIds.add(ElanLocale.getString(LABEL_PREF + NAME));
        columnIds.add(ElanLocale.getString(LABEL_PREF + URL));
        columnIds.add(ElanLocale.getString(LABEL_PREF + MIME_TYPE));
        columnIds.add(ElanLocale.getString(LABEL_PREF + ASSOCIATED_WITH));
        columnIds.add(ElanLocale.getString(LABEL_PREF + OFFSET));
        columnIds.add(ElanLocale.getString(LABEL_PREF + LINK_STATUS));

        types = new ArrayList<Class<?>>(columnIds.size());
        types.add(String.class);
        types.add(String.class);
        types.add(String.class);
        types.add(String.class);
        types.add(Integer.class);
        types.add(Boolean.class);

        initData();
    }

    /**
     * Initialises Lists of row data from the media descriptors.
     */
    private void initData() {
        data = new ArrayList<List<Object>>(descriptors.size());

        for (int i = 0; i < descriptors.size(); i++) {
            LinkedFileDescriptor desc = descriptors.get(i);
            List<Object> rowData = new ArrayList<Object>(getColumnCount());

            String url = desc.linkURL;
            String name = FileUtility.fileNameFromPath(url);
            rowData.add(name);
            rowData.add(url);
            rowData.add(desc.mimeType);
            rowData.add((desc.associatedWith != null) ? desc.associatedWith : N_A);
            rowData.add(Integer.valueOf((int) desc.timeOrigin));

            // check if the file exists
            boolean linked = FileUtility.fileExists(desc.linkURL);
            rowData.add(Boolean.valueOf(linked));

            data.add(rowData);
        }
    }

    /**
     * Note: silently returns instead of throwing an
     * ArrayIndexOutOfBoundsException
     *
     * @param rowIndex the row to remove
     */
    public void removeRow(int rowIndex) {
        if ((rowIndex >= 0) && (rowIndex < data.size())) {
            data.remove(rowIndex);
            descriptors.remove(rowIndex);
            fireTableDataChanged();
        }
    }

    /**
     * Adds a row with the data of the LinkedFileDescriptor to the model.
     *
     * @param desc the new LinkedFileDescriptor
     *
     * @see #addLinkDescriptor(LinkedFileDescriptor)
     */
    public void addRow(LinkedFileDescriptor desc) {
        if (desc == null) {
            return;
        }

        descriptors.add(desc);

        ArrayList<Object> rowData = new ArrayList<Object>(getColumnCount());
        String url = desc.linkURL;
        String name = FileUtility.fileNameFromPath(url);
        rowData.add(name);
        rowData.add(url);
        rowData.add(desc.mimeType);
        rowData.add((desc.associatedWith != null) ? desc.associatedWith : N_A);
        rowData.add(Integer.valueOf((int) desc.timeOrigin));

        // check if the file exists
        boolean linked = FileUtility.fileExists(desc.linkURL);
        rowData.add(Boolean.valueOf(linked));

        data.add(rowData);
        fireTableDataChanged();
    }

    /**
     * Adds a LinkedFileDescriptor to the Vector of LinkedFileDescriptor.
     *
     * @param md the new LinkedFileDescriptor
     *
     * @see #addRow(LinkedFileDescriptor)
     */
    public void addLinkDescriptor(LinkedFileDescriptor md) {
        addRow(md);
    }

    /**
     * Notification that the data in some LinkedFileDescriptor has been changed
     * so the row value list should be updated.
     */
    public void rowDataChanged() {
        initData();
        fireTableDataChanged();
    }
    
	/**
	 * Sep 2013: Apply the change to the model if the column is offset column.  
	 * 
	 * @param value the new value for the cell
	 * @param rowIndex the row index of the cell
	 * @param columnIndex the column index of the cell
	 */
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if ((rowIndex < 0) || (rowIndex >= data.size()) || (columnIndex < 0) ||
                (columnIndex >= columnIds.size())) {
            return;
        }
        
        if (value == null) {
        	return;
        }
        
        if (value instanceof Integer && (Integer) value < 0) {
        	return;        	
        }

		if (getColumnClass(columnIndex).isInstance(value)) {
			data.get(rowIndex).set(columnIndex, value);
			fireTableCellUpdated(rowIndex, columnIndex);
			// update the underlying copy of the media descriptor in case it the offset changed
			// (and that is currently the only property for which editing is supported)
			String columnName = getColumnName(columnIndex);//
			
			if (ElanLocale.getString(LABEL_PREF + OFFSET).equals(columnName)) {// superfluous test
				if (value instanceof Integer) { // again superfluous
					LinkedFileDescriptor md = descriptors.get(rowIndex);
					md.timeOrigin = (Integer) value;
				}
			}
		}		
	}
}

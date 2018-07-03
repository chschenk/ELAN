package mpi.search.content.query.viewer;

import java.awt.Component;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;

import mpi.search.SearchLocale;

/**
 * Created on Jun 23, 2004
 * @author Alexander Klassmann
 * @version Jun 23, 2004
 */
public class LocalizeListCellRenderer extends DefaultListCellRenderer {

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(
		JList list,
		Object value,
		int index,
		boolean isSelected,
		boolean cellHasFocus) {

		String valueString = null;

		if (value != null) {
			valueString = value.toString();
			String localizedValueString = SearchLocale.getString(valueString);
			if (localizedValueString != null && localizedValueString.length() > 0)
				valueString = localizedValueString;
		}

		return super.getListCellRendererComponent(
			list,
			valueString,
			index,
			isSelected,
			cellHasFocus);
	}

}

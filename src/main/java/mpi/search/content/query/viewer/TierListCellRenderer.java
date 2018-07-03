package mpi.search.content.query.viewer;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import mpi.search.SearchLocale;
import mpi.search.content.model.CorpusType;

/**
 * Created on Apr 14, 2004
 * @author Alexander Klassmann
 * @version Apr 14, 2004
 */
public class TierListCellRenderer extends DefaultListCellRenderer {
	private final CorpusType type;

	/**
	 * 
	 * @param type
	 */
	public TierListCellRenderer(CorpusType type) {
		this.type = type;
	}

	/**
	 * If TYPE contains long name of tier, use that one.
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(JList, Object, int, boolean, boolean)
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

			if (type != null) {
				String longName = type.getUnabbreviatedTierName(valueString);
				if (longName != null) {
					valueString = longName;
				}
				else {
					String localizedValueString = SearchLocale.getString(valueString);
					if (localizedValueString != null && localizedValueString.length() > 0)
						valueString = localizedValueString;
				}
			}
		}

		return super.getListCellRendererComponent(
			list,
			valueString,
			index,
			isSelected,
			cellHasFocus);
	}

}

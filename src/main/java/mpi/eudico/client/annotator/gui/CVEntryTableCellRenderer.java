package mpi.eudico.client.annotator.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import mpi.eudico.util.CVEntry;

/**
 * A cell renderer for CVEntry objects, uses an icon to show preferred color and shortcut key.
 * 
 * @author Olaf Seibert; Han Sloetjes
 *
 */
@SuppressWarnings("serial")
public class CVEntryTableCellRenderer extends DefaultTableCellRenderer  {
	private CVEIcon icon;

	/**
	 * Constructor.
	 */
	public CVEntryTableCellRenderer() {
		super();
		icon = new CVEIcon();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean cellHasFocus, int row, int col) {

		icon.color = null;
		icon.s = null;

		if (value instanceof CVEntry) {
			CVEntry entry = (CVEntry)value;
			
			icon.color = entry.getPrefColor();
			int code = entry.getShortcutKeyCode();
			if (code != -1) {
				icon.s = KeyEvent.getKeyText(code);
			}
			setIcon(icon);
			col = table.convertColumnIndexToModel(col);
			super.getTableCellRendererComponent(table, entry.getValue(col), isSelected,
					cellHasFocus, row, col);
		} else {
			super.getTableCellRendererComponent(table, value, isSelected,
				cellHasFocus, row, col);
		}
		setIcon(icon);
		setHorizontalTextPosition(SwingConstants.RIGHT);
		
		return this;
	}

	/**
	 * Overrides paintIcon, getWidth and getHeight.
	 * 
	 * @author Han Sloetjes
	 */
	private class CVEIcon extends ImageIcon {
		int width = 20;
		Color color;
		String s;

		/**
		 * Constructor
		 */
		public CVEIcon() {
			super();
		}

		@Override
		public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			if (color != null) {
				g.setColor(color);
				g.fillRect(x, y, width, c.getHeight());
			}
			if (s != null) {
				g.setColor(c.getForeground());
				g.setFont(c.getFont());
				int sw = c.getFontMetrics(c.getFont()).stringWidth(s);
				
				g.drawString(s, x + (width - sw) / 2, c.getHeight() - ((c.getHeight() - c.getFont().getSize()) / 2) - 1);
			}
		}

		@Override
		public int getIconHeight() {
			return getHeight() - 4;
		}

		@Override
		public int getIconWidth() {
			return width;
		}
	}
}

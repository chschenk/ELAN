package mpi.eudico.client.annotator.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.SwingConstants;

import mpi.eudico.util.CVEntry;

/**
 * A cell renderer for CVEntry objects, uses an icon to show preferred color and shortcut key.
 * 
 * @author Han Sloetjes
 *
 */
@SuppressWarnings("serial")
public class CVEntryListCellRenderer extends DefaultListCellRenderer {
	private CVEIcon icon;

	/**
	 * Constructor.
	 */
	public CVEntryListCellRenderer() {
		super();
		icon = new CVEIcon();
	}

	@Override
	/**
	 * Calls super and adds the configured icon.
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		if (value instanceof CVEntry) {
			icon.color = ((CVEntry)value).getPrefColor();
			int code = ((CVEntry)value).getShortcutKeyCode();
			if (code == -1) {
				icon.s = null;
			} else {
				icon.s = KeyEvent.getKeyText(code);
			}
		} else {
			icon.color = null;
			icon.s = null;
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
			return CVEntryListCellRenderer.this.getHeight() - 4;
		}

		@Override
		public int getIconWidth() {
			return width;
		}
		
	}

}

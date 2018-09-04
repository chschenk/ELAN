package mpi.eudico.client.annotator.search.query.viewer;

import java.awt.Component;
import java.awt.event.*;
import java.util.Locale;
import javax.swing.JMenuItem;

import mpi.eudico.client.im.ImUtil;
import mpi.search.content.query.viewer.AbstractConstraintPanel;
import mpi.search.content.query.viewer.AbstractPopupMenu;

/**
 * Created on Aug 18, 2004
 * @author Alexander Klassmann
 * @version Aug 18, 2004
 */
public class EAFPopupMenu extends AbstractPopupMenu implements FocusListener {
	final static private Locale[] imLocales = ImUtil.getLanguages();

	public EAFPopupMenu(Component component, AbstractConstraintPanel constraintPanel) {
		super(component, constraintPanel);
	}

	@Override
	public void fillMenu() {
		setLabel("Select Language");
		try {
			JMenuItem item;
			for (int i = 0; i < imLocales.length; i++) {
			    if (i == 0 && imLocales[i] == Locale.getDefault()) {
			        item = new JMenuItem(imLocales[i].getDisplayName() + " (System default)");
			    } else {
			        item = new JMenuItem(imLocales[i].getDisplayName());
			    }
				add(item);
				item.addActionListener(this);
			}

			component.addFocusListener(this);
		}
		catch (java.lang.NoSuchMethodError nsme) {
			System.err.println("No input methods found. Probably class ImUtil not in java.lib.ext");
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (javax.swing.SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
			show(component, e.getX(), e.getY());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		for (int i = 0; i < imLocales.length; i++) {
			if (i == 0 && imLocales[i] == Locale.getDefault() && command.indexOf(imLocales[i].getDisplayName()) > -1) {
				ImUtil.setLanguage(component, imLocales[i]);
				break;
			}
			if (command.equals(imLocales[i].getDisplayName())) {
				ImUtil.setLanguage(component, imLocales[i]);
				break;
			}
		}
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		// this has negative side effects, like changing the keyboard/language setting of the system
		//ImUtil.setLanguage(component, component.getLocale());
	}
	
	@Override
	public void focusLost(FocusEvent e) {
	}
}

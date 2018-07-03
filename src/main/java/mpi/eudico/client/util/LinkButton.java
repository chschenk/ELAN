package mpi.eudico.client.util;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Functions like a JButton, but looks like a link in a HTML-page
 * Created on Oct 26, 2004
 * @author Alexander Klassmann
 * @version Oct 26, 2004
 */
public class LinkButton extends JEditorPane {
	private String actionName;
	private boolean enabled = true;

	public LinkButton(final Action action) {
		setContentType("text/html");
		setEditable(false);
		actionName = (String) action.getValue(Action.NAME);

		addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (enabled)
						action.actionPerformed(new ActionEvent(this, 0, e.getDescription()));
				}
			}
		});

		action.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if ("enabled".equals(e.getPropertyName()))
					setEnabled(action.isEnabled());
			}
		});
		
		setToolTipText((String) action.getValue(Action.SHORT_DESCRIPTION));
		reset();
	}

	public void setLabel(String label){
		actionName = label != null ? label : "";
		reset();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			reset();
		}
	}

	private void reset() {
		if (enabled)
			setText(
				"<A HREF=\"" + actionName + "\"><font size=\"3\">" + actionName + "</font></A>");
		else
			setText("<font size=\"3\">" + actionName + "</font>");
	}

	//	public Dimension getPreferredSize() {
	//		return new Dimension(super.getPreferredSize().width, super.getPreferredSize().height - 2);
	//	}

}

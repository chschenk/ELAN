package mpi.eudico.client.annotator.turnsandscenemode.commands;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.global.FrameMenuAction;
import mpi.eudico.client.annotator.gui.HTMLViewer;
import mpi.eudico.client.annotator.util.WindowLocationAndSizeManager;

/**
 * An action to show some help or manual contained in a single html file.
 */
@SuppressWarnings("serial")
public class HelpTaSMA extends FrameMenuAction {
	/* could consider to use  a frame instead of a dialog */
	private static JFrame helpDialog = null;
	private float fontSize = 14f;
	private final Font htmlFont = new Font("Serif", Font.PLAIN, (int)fontSize);
	private JEditorPane htmlPane;

	public HelpTaSMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (helpDialog != null) {
			helpDialog.toFront();
			return;
		}
		
		try {
			HTMLViewer htmlViewer = new HTMLViewer("/simple-elan.html", false, 
					ElanLocale.getString("Menu.Help"));
			helpDialog = htmlViewer.createHTMLFrame();
			helpDialog.pack();
			
			if (htmlViewer.getHTMLPane() != null) {
				htmlPane = htmlViewer.getHTMLPane();
				htmlPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
				htmlPane.setFont(htmlFont.deriveFont(1.2f * htmlFont.getSize()));
			}
			
			helpDialog.setVisible(true);
			helpDialog.addWindowListener(new WindowHandler());
			addShortcutActions();
			// restore size and location
			WindowLocationAndSizeManager.postInit(helpDialog, "HelpFrame-TaS");
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(frame, ElanLocale.getString("Message.LoadHelpFile"), 
					ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
			//t.printStackTrace();
		}
	}
	
	/**
	 * Add font size actions.
	 */
	private void addShortcutActions() {
		if (helpDialog != null) {
			final int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
			FontZoomAction inAction = new FontZoomAction(1);
			String actKeyIn = "IncreaseFont";
			helpDialog.getRootPane().getActionMap().put(actKeyIn, inAction);
			helpDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, menuShortcutKeyMask, false), actKeyIn);
			
			FontZoomAction outAction = new FontZoomAction(-1);
			String actKeyOut = "DecreaseFont";
			helpDialog.getRootPane().getActionMap().put(actKeyOut, outAction);
			helpDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, menuShortcutKeyMask, false), actKeyOut);
			
			FontZoomAction defAction = new FontZoomAction(0);
			String actKeyDef = "DefaultFont";
			helpDialog.getRootPane().getActionMap().put(actKeyDef, defAction);
			helpDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke(KeyEvent.VK_0, menuShortcutKeyMask, false), actKeyDef);
		}
		
	}
	
	private class WindowHandler extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			WindowLocationAndSizeManager.storeLocationAndSizePreferences(e.getWindow(), "HelpFrame-TaS");
			helpDialog.removeWindowListener(this);
			helpDialog = null;
		}		
	}

	/**
	 * An action for increasing/decreasing the html rendering font size.
	 *  
	 * If this works well this might move to HTMLViewer and maybe be optional,
	 * switched on or off by a constructor parameter.
	 */
	private class FontZoomAction extends AbstractAction {
		private int zoomDirection;
		
		/**
		 * @param zoomDirection -1 means decrease font size, 
		 * 0 means default font size, 1 means increase font size
		 */
		public FontZoomAction(int zoomDirection) {
			super();
			this.zoomDirection = zoomDirection;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (htmlPane != null) {
				float curSize = htmlPane.getFont().getSize();
				
				switch(zoomDirection) {
				case -1:
					if (curSize >= 6) {
						htmlPane.setFont(htmlFont.deriveFont(0.8f * curSize));
					}
					break;
				case 0:
					htmlPane.setFont(htmlFont.deriveFont(fontSize));
				break;
				case 1:
					if (curSize <= 100) {
						htmlPane.setFont(htmlFont.deriveFont(1.2f * curSize));
					}
					break;
					default:
				}
			}		
		}
	}
	
}

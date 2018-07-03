package mpi.eudico.client.annotator.turnsandscenemode.commands;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.SIMPLELAN;
import mpi.eudico.client.annotator.commands.global.FrameMenuAction;

@SuppressWarnings("serial")
public class AboutTaSMA extends FrameMenuAction {

	public AboutTaSMA(String name, ElanFrame2 frame) {
		super(name, frame);	
	}
	
	@Override
	public void updateLocale() {
		super.updateLocale();
		putValue(Action.NAME, String.format(ElanLocale.getString(commandId), SIMPLELAN.getApplicationName()));
	}

	/**
	 * Show a simple about dialog.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(frame, getPanel(),
        		String.format(ElanLocale.getString("Menu.Help.AboutDialog"), SIMPLELAN.getApplicationName()),
                JOptionPane.PLAIN_MESSAGE, null);
	}

	private JComponent getPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		Icon icon = null;
        try {
            icon = new ImageIcon(this.getClass()
                                     .getResource("/mpi/eudico/client/annotator/resources/SIMPLE-ELAN128.png"));
        } catch (Throwable t) {
        	//
        }
        
        StringBuilder textBuf = new StringBuilder("<html>");
        textBuf.append("<b>");
        textBuf.append(SIMPLELAN.getApplicationName());
        textBuf.append("<br>");
        textBuf.append("Version: ");
        textBuf.append(SIMPLELAN.getVersionString());
        textBuf.append("<br><br>");
        textBuf.append("Copyright \u00A9 2017");
        textBuf.append("<br>");
        textBuf.append("Max-Planck-Institute for Psycholinguistics");
        textBuf.append("<br>");
        textBuf.append("Nijmegen, The Netherlands");
        textBuf.append("<br><br>");
        textBuf.append(SIMPLELAN.getApplicationName() + " is supported by:");
        textBuf.append("<br>");
        textBuf.append("ARC Centre of Excellence for the Dynamics of Language");
        textBuf.append("</html>");

        JLabel label = new JLabel(textBuf.toString()
                                         .replaceAll("\\u000A", "<br>"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(new JLabel(icon), gbc);
        
        gbc.gridx = 1;
        panel.add(label, gbc);
        
		return panel;
	}
}

package mpi.eudico.client.annotator.recognizer.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import mpi.eudico.client.annotator.ElanLocale;

/**
 * Temporary dialog for recognizer parameters.
 * @author Han Sloetjes
 *
 */
public class ParamDialog extends JDialog implements ActionListener {
	private AbstractRecognizerPanel panel;
	private JComponent paramComp;
	private JButton closeButton;	
	private JButton helpButton;
	
	public ParamDialog(JFrame owner, AbstractRecognizerPanel panel, JComponent paramComp) {
		super(owner, false);
		this.panel = panel;
		this.paramComp = paramComp;
		setTitle(ElanLocale.getString("Recognizer.RecognizerPanel.Parameters"));
		initComponents();
	}
	
	private void initComponents() {
		helpButton = new JButton(new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Help24.gif")));
		helpButton.setToolTipText(ElanLocale.getString("Button.Help.ToolTip"));
		helpButton.addActionListener(this);	
		helpButton.setEnabled(panel.isHelpAvailable());		
		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.fill = GridBagConstraints.NONE;
		getContentPane().add(helpButton, gbc);
		
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridy = 1;
		getContentPane().add(paramComp, gbc);
		
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0.0;
		closeButton = new JButton(ElanLocale.getString("Button.Close"));
		closeButton.addActionListener(this);
		getContentPane().add(closeButton, gbc);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	  @Override
	public void dispose() {
		  if (panel != null) {
				if(paramComp instanceof JScrollPane){
					getRootPane().remove(paramComp);
				} else {
					getContentPane().remove(paramComp);
				}				
				panel.attachParamPanel(paramComp);
				setVisible(false);
				super.dispose();
			}
	    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == closeButton) {
			dispose();
		} if(e.getSource() == helpButton){
			panel.showHelpDialog();
		}
	}
}

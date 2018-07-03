package mpi.eudico.client.annotator.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;

public class ShortcutReplaceDialog extends JDialog implements ActionListener {
	
	private ShortcutsUtil scu = ShortcutsUtil.getInstance();
	private ShortcutPanel scPanel;
	private JTextArea messageField;
	private JButton replaceButton;
	private JButton cancelButton;
	
	ShortcutReplaceDialog(ShortcutPanel caller, String actionDesc, KeyStroke ks)
	{
		super((JDialog) javax.swing.SwingUtilities.getWindowAncestor(caller),ElanLocale.getString("Shortcuts.Replace.Title"));
		scPanel = caller;
		setLayout( new GridBagLayout() );   
        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(2, 6, 2, 6);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = insets;
        JPanel newContentPane = new JPanel();
        add( newContentPane, gbc ); 
        newContentPane.setLayout(new GridBagLayout());
        GridBagConstraints innergbc = new GridBagConstraints();
        Insets innerinsets = new Insets(2, 6, 2, 6);
        innergbc.fill = GridBagConstraints.BOTH;
        innergbc.weightx = 1.0;
        innergbc.weighty = 1.0;
        innergbc.insets = innerinsets;
        
		JDialog owner = (JDialog) javax.swing.SwingUtilities.getWindowAncestor(caller);		
		String eol = System.getProperty("line.separator"); 
		String message = scu.getDescriptionForKeyStroke(ks) + " " +  ElanLocale.getString("Shortcuts.Message.AlreadyInUse") + 
				eol +"' " +  actionDesc + " '" + eol + ElanLocale.getString("Shortcuts.Replace.DoYouReally");
		messageField = new JTextArea(message);
		messageField.setFocusable(false);
		messageField.setBackground(new JLabel().getBackground());
		
		messageField.setAlignmentY(CENTER_ALIGNMENT);

		newContentPane.add(messageField,innergbc);
		
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 2, 2));
		 
		replaceButton = new JButton(ElanLocale.getString("Button.Reassign"));
		replaceButton.addActionListener(this);
		replaceButton.setVerticalTextPosition(AbstractButton.CENTER);
		replaceButton.setHorizontalTextPosition(AbstractButton.LEADING);
		buttonPanel.add(replaceButton);
        

        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        cancelButton.setVerticalTextPosition(AbstractButton.CENTER);
        cancelButton.setHorizontalTextPosition(AbstractButton.LEADING);
        buttonPanel.add(cancelButton);

        innergbc.gridy = 1;
        innergbc.fill = GridBagConstraints.NONE;
        innergbc.weighty = 0.0;
        innergbc.weightx = 0.0;
        newContentPane.add(buttonPanel, innergbc);
        
        newContentPane.setOpaque(true); //content panes must be opaque
        this.setContentPane(newContentPane);	
	}
	

	 @Override
	public void actionPerformed( ActionEvent e )
	    {
		    	if (e.getSource() == cancelButton)
		    	{
		    		scPanel.replaceShortcut = false;
			  	this.setVisible(false);
		    	}
		    	else if (e.getSource() == replaceButton)
		    	{
		    		scPanel.replaceShortcut = true;
		    		this.setVisible(false);
		    	}
	    }
}

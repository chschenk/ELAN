package mpi.eudico.client.annotator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;

/**
 * A panel to change / remove a single keyboard shortcut combination.
 * 
 * @author alekoe
 *
 */
public class ShortcutEditPanel extends JPanel implements KeyListener, ActionListener {

	protected JTextField entryField;
	protected KeyStroke newShortcut;	
	protected String newShortcutAsText;
	protected KeyStroke currentShortcut;
	protected String currentShortCutAsText;
	protected String currentAction;
	protected JButton cancelButton;
	protected JButton okButton;
	protected JButton applyAllModesButton;
	protected JButton removeButton; 
	private ShortcutPanel shortcutPanel;
	
	/**
	 * Constructor
	 * @param shortcutPanel the calling shortcut panel
	 * @param actionID the ID of the action for which this window is created
	 * @param actionKey the KeyStroke currently assigned to this action
	 */
    public ShortcutEditPanel(ShortcutPanel shortcutPanel, String actionID, KeyStroke actionKey) 
    {
        super();
        
        this.shortcutPanel = shortcutPanel;
        this.currentAction = actionID;
        this.currentShortcut = actionKey;
        this.currentShortCutAsText = ShortcutsUtil.getInstance().getDescriptionForKeyStroke(currentShortcut);
        newShortcut = currentShortcut;
        
        
        JLabel descField = new JLabel(ElanLocale.getString("Shortcuts.Editor.Title"));
     

        entryField = new JTextField();
        // how big should it be 
        entryField.setPreferredSize(new Dimension(170,25));
        
        // configure as keylistener
        entryField.setFocusable(true);
        entryField.setFocusTraversalKeysEnabled(false);        
        entryField.addKeyListener(this);
        entryField.setText(currentShortCutAsText);   
        
        okButton = new JButton(ElanLocale.getString("Button.Apply"));
        okButton.addActionListener(this);  
        
        applyAllModesButton = new JButton(ElanLocale.getString("Shortcuts.Button.ApplyAll"));
        applyAllModesButton.addActionListener(this);       
        
        removeButton = new JButton(ElanLocale.getString("Shortcuts.Button.Clear"));
        removeButton.addActionListener(this);
      
        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 6, 0));       
        buttonPanel.add(okButton);
        buttonPanel.add(applyAllModesButton);        
        buttonPanel.add(cancelButton);
       
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4,6,4,6);
        add(descField, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(entryField, gbc);
        
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        add(removeButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(10,6,4,6);        
        add(buttonPanel, gbc);
    }

    /**
     * Returns the new value for the action or null if nothing changed.
     *  
     * @return the new KeyStroke for the action
     */
    public KeyStroke getValue() {
    	return newShortcut;
    }
    
    /**
     * actionListener method that catches whether a button is pressed and either
     * closes the window and hands the new shortcut to the parent ShortcutPanel (OK button)
     * closes the window without changing anything (Cancel button)
     * empties the text field (Remove button)
     * 
     */
    @Override
	public void actionPerformed( ActionEvent e )
    {
	    	if (e.getSource() == cancelButton)
	    	{
	    		SwingUtilities.getWindowAncestor(this).setVisible(false);
	    	}
	    	else if (e.getSource() == removeButton)
	    	{
        	  	newShortcut = null;
        	  	newShortcutAsText = "";
        	  	entryField.setText(newShortcutAsText);
        	  	entryField.requestFocus();
	    	}
	    	else if (e.getSource() == okButton)
	    	{
	    		// new shortcut has been set, or it is unchanged
	    		SwingUtilities.getWindowAncestor(this).setVisible(false);
	    		if (shortcutPanel != null) {
	    			shortcutPanel.changeShortcut(newShortcut, false);
	    		}
	    	}	    
	    	
	    	else if (e.getSource() == applyAllModesButton)
	    	{
	    		// new shortcut has been set, or it is unchanged
	    		SwingUtilities.getWindowAncestor(this).setVisible(false);
	    		if (shortcutPanel != null) {
	    			shortcutPanel.changeShortcut(newShortcut, true);
	    		}
	    	}	    	
    }
    
    /**
     * 
     * creates the window for editing a specific shortcut and shows it to the user
     * 
     * @param owner the ShortcutPanel from which this method was called
     * @param actionID the ID of the action for which the shortcut should be changed
     */
    public static void createAndShowGUI(ShortcutPanel caller, String actionID, List<Integer> codes) {
        //Create and set up the window.
    		
    	JDialog owner = (JDialog) javax.swing.SwingUtilities.getWindowAncestor(caller);
    	final ShortcutsUtil scu = ShortcutsUtil.getInstance();
    	String actionName = scu.getDescriptionForAction(actionID);
   		KeyStroke actionKeyStroke = null;
   		if(codes != null){
   			actionKeyStroke = KeyStroke.getKeyStroke(codes.get(0), codes.get(1));
   		}
        JDialog frame = new JDialog(owner, (ElanLocale.getString("Shortcuts.Editor.Label") + " '" + actionName + "'"), true);

        //Create and set up the content pane.
        ShortcutEditPanel newContentPane = new ShortcutEditPanel(caller, actionID,actionKeyStroke);
        
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        
        //Display the window.
        frame.pack();
        frame.setLocationRelativeTo(owner);
        frame.setVisible(true);

    }

    /**
     * action that is fired every time a key is pressed
     * the pressed key(s) is/are saved in member variables
     */
	@Override
	public void keyPressed(KeyEvent ke) 
	{
		entryField.setText("");
		newShortcut = KeyStroke.getKeyStrokeForEvent(ke);
		String myKeyString = KeyEvent.getKeyText(ke.getKeyCode());
        String myModifierString = KeyEvent.getModifiersExText(ke.getModifiersEx());
        
        if (ke.getModifiersEx() == 0)
        {
        		newShortcutAsText = myKeyString;	
        }
        else
        {
        		newShortcutAsText = myModifierString + " + " + myKeyString;        	
        }        				
	}

	/**
	 * action that is fired when a pressed key is released
	 * after all keys are released, the key combination will be shown in the text box 
	 */
	@Override
	public void keyReleased(KeyEvent ke) 
	{
		// only change the field's content after the keys have been released
		entryField.setText(newShortcutAsText);
	}
	
	/**
	 *  has to be implemented, but is not used
	 */
	@Override
	public void keyTyped(KeyEvent ke) 
	{
		
	}

}

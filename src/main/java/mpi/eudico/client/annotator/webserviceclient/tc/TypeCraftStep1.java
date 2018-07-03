package mpi.eudico.client.annotator.webserviceclient.tc;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.webserviceclient.typecraft.TCWsClient;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;

import javax.swing.border.EmptyBorder; 
import javax.swing.event.ListSelectionEvent;


/**
 * Dialog for entering username and password. This dialog is the first step in
 * the TypeCraft (TC) web services wizzard dialog. Still to be done: enable
 * password entry only when something has entered. Also, show some kind of a
 * login status in the panel: not logged in, login failure, logged in.
 * 
 * example: ImportPraatTGStep1 / ImportPraatTGStep2
 */

public class TypeCraftStep1 extends StepPane implements ActionListener {
    
    // Create a panel for the user to login. Implement this panel as a stepPane.
         
    private JButton loginButton;
    private JTextField usernameTextField;
    private JPasswordField passwordField;
    private int loginStatus;
    
    /*
     * 0 = not logged in, 1 = login failed, 2 = logged in. Use a real
     * enumeration type of course.
     */
    
    /**
     * Create a new instance of the first step of the wizard.
     *
     * @param multiPane the parent pane
     */
    public TypeCraftStep1(MultiStepPane multiPane) {
        super(multiPane);
        initComponents();
    }
    
    private String username;
    private String password;
    
    /**
     * Initialize userName, passWord and other components in the first step of
     * the wizzard dialog.
     * 
     * @see mpi.eudico.client.annotator.gui.multistep.StepPane#initComponents()
     */
    @Override
	public void initComponents() {
	
	// First, create labels. In any case supply a label for the username and
	// password textfield.
       
        JLabel usernameLabel, passwordLabel;
        
        usernameLabel = new JLabel("Username");
        passwordLabel = new JLabel("Password");
        
        // Next. create text input fields.
        
        usernameTextField = new JTextField("");  
        usernameTextField.setEnabled(true);
        passwordField = new JPasswordField("");  
        passwordField.setEnabled(true);
        
        // Create the final component: a login button.
        
        loginButton = new JButton("Log in");
        
        // Put the components in a new panel by means of a gridbaglayout.
        
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        Insets insets = new Insets(2, 0, 2, 0);
        Insets labelInsets = new Insets(2, 0, 2, 20);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = labelInsets;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // Position the components on the grid.

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        add(usernameLabel,gbc);
        gbc.gridx = 1;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(usernameTextField,gbc);
        usernameTextField.addActionListener(this); // ?
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = labelInsets;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        add(passwordLabel,gbc);
        gbc.gridx = 1;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(passwordField,gbc);
        passwordField.addActionListener(this);

        gbc.insets = new Insets(20, 0, 0, 0);
        //gbc.gridwidth = 2;
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        add(loginButton, gbc);
        loginButton.addActionListener(this);
        
        //usernameTextField.requestFocus();
    }
    
    /**
     * Should lListen for entry of text and click on login button. Enable the login
     * button once a valid username has been entered.
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {

		Object source;
		source = e.getSource();
		
		if (source == loginButton) {
		    username = usernameTextField.getText();
		    if (username != null){ 
		    	char[] pwd = passwordField.getPassword();
		    	if (pwd != null && pwd.length > 0) {
		    		password = new String(pwd);
		    	}
		    } else {
		    	showMessage("Please enter your user name");
		    }
		    
			if (password != null){
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);			
		    } else {
		    	showMessage("Please enter your password");
		    }
			
	    	Object wsclObj = multiPane.getStepProperty("TCWsClient");
	    	TCWsClient tcws = null;
	    	if (wsclObj instanceof TCWsClient) {
	    		tcws = (TCWsClient) wsclObj;
	    	} else {
	    		tcws = new TCWsClient();
	    		multiPane.putStepProperty("TCWsClient", tcws);
	    	}
	    	
	    	String sessionId = tcws.login(username, password);
	    	if (sessionId == null) {
	    		showMessage("Login failed!");
	    	} else {
	    		TCWsClient.storedSessionId = sessionId;
	    		multiPane.putStepProperty("SessionId", sessionId);
	    		multiPane.nextStep();
	    	}
		} 
    }
    
    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return "TypeCraft login";
    }
    
    /**
     * Do we need to do something here?
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {
    	if (TCWsClient.storedSessionId != null) {
			if (multiPane.getStepProperty("SessionId") == null) {
				multiPane.putStepProperty("SessionId", TCWsClient.storedSessionId );
			}
	    	Object clObj = multiPane.getStepProperty("TCWsClient");
	    	if (clObj == null) {
	    		multiPane.putStepProperty("TCWsClient", new TCWsClient());
	    	}
	    	multiPane.nextStep();
    	} else {
    		usernameTextField.requestFocus();
    	}
    }
    
    /**
     * Do we need to do something here?
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
	
        return true;
    }

    /**
     * Enable the next/finish button. Do we need to check something here?
     *
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent lse) {

    }

    private void showMessage(String message) {
    	JOptionPane.showMessageDialog(this, message, ElanLocale.getString("Message.Warning"), 
    			JOptionPane.WARNING_MESSAGE);
    }

	@Override
	public void addNotify() {
		super.addNotify();
		//System.out.println("Step 1 addNotify");
		enterStepForward();
	}
    
    
    
}

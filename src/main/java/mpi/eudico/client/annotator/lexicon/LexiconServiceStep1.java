package mpi.eudico.client.annotator.lexicon;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.lexicon.LexiconIdentification;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClient;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClientException;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClientFactory;

/**
 * The first step of the Lexicon Service Wizard
 * It lets the user fill in the name of the Lexicon Link, the Lexicon Service Type, the URL and the username 
 * and password 
 * @author Micha Hulsbosch
 *
 */
public class LexiconServiceStep1 extends StepPane implements ActionListener, ItemListener, KeyListener {

	private LexiconLink oldLink;
	
	private JLabel nameLabel;
	private JTextField nameField;
	private JLabel questionLabel;
	private JComboBox lexiconTypeList;
	private JLabel urlLabel;
	private JLabel userNameLabel;
	private JLabel passWordLabel;
	private JTextField urlTextField;
	private JTextField userNameTextField;
	private JTextField passWordTextField;
	private JPanel fieldsPanel;
	private JLabel lexiconTypeLabel;
	private JLabel descriptionLabel;
	private JTextArea descriptionText;
	private JScrollPane descriptionTextScroller;
	private Map<String, LexiconServiceClientFactory> lexiconServiceClientFactories;

	private TranscriptionImpl transcription;

	private JButton defaultURLButton;
	
	public LexiconServiceStep1(MultiStepPane multiPane, LexiconLink link,
			Transcription tr) {
		super(multiPane);
		this.oldLink = link;
		this.transcription = (TranscriptionImpl) tr;
		if (!transcription.isLexiconServicesLoaded()) {
			try {
				new LexiconClientFactoryLoader().loadLexiconClientFactories(transcription);
			} catch (Exception exc) {//just any exception
				ClientLogger.LOG.warning("Error while loading lexicon service clients: " + exc.getMessage());
			}
		}
		multiPane.putStepProperty("oldLink", link);
		lexiconServiceClientFactories = transcription.getLexiconServiceClientFactories();
		initComponents();
		postInit();
	}
	
	@Override
	protected void initComponents() {
		setLayout(new GridBagLayout());
	    setBorder(new EmptyBorder(12, 12, 12, 12));
		
	    fieldsPanel = new JPanel(new GridBagLayout());
	    GridBagConstraints c = new GridBagConstraints();
	    JLabel nameQuestionLabel = new JLabel(ElanLocale.getString("EditLexSrvcDialog.Label.EnterServiceName"));
	    nameLabel = new JLabel(ElanLocale.getString("EditLexSrvcDialog.Label.Name"));
		nameField = new JTextField();
		nameField.addKeyListener(this);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,0,0,0);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		fieldsPanel.add(nameQuestionLabel, c);
		
		c.insets = new Insets(5,0,0,5);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		fieldsPanel.add(nameLabel, c);
		
		c.insets = new Insets(5,0,0,0);
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 1;
		fieldsPanel.add(nameField, c);
	    
		questionLabel = new JLabel(ElanLocale.getString("EditLexSrvcDialog.Label.EnterWorkspaceInfo"));
		
		lexiconTypeLabel = new JLabel(ElanLocale.getString("EditLexSrvcDialog.Label.Type"));
		if (lexiconServiceClientFactories != null) {
			lexiconTypeList = new JComboBox((new ArrayList(lexiconServiceClientFactories.keySet()).toArray()));
		} else {
			lexiconTypeList = new JComboBox(new String[]{});
		}
		lexiconTypeList.addItemListener(this);
		descriptionLabel = new JLabel(ElanLocale.getString("EditLexSrvcDialog.Label.Description"));
		descriptionText = new JTextArea();
		descriptionText.setEditable(false);
		descriptionText.setLineWrap(true);
		descriptionText.setWrapStyleWord(true);
		descriptionTextScroller = new JScrollPane(descriptionText);
		urlLabel = new JLabel(ElanLocale.getString("EditLexSrvcDialog.Label.Url"));
		urlTextField = new JTextField();
		urlTextField.addKeyListener(this);
		defaultURLButton = new JButton(ElanLocale.getString("EditLexSrvcDialog.Button.DefaultUrl"));
		defaultURLButton.addActionListener(this);
		
		userNameLabel = new JLabel(ElanLocale.getString("EditLexSrvcDialog.Label.Username"));
		userNameTextField = new JTextField();
		userNameTextField.addKeyListener(this);
		
		passWordLabel = new JLabel(ElanLocale.getString("EditLexSrvcDialog.Label.Password"));
		passWordTextField = new JPasswordField();
		passWordTextField.addKeyListener(this);
		
		if(oldLink != null && oldLink != null) {
			nameField.setText(oldLink.getName());
			urlTextField.setText(oldLink.getSrvcClient().getUrl());
			userNameTextField.setText(oldLink.getSrvcClient().getUsername());
			passWordTextField.setText(oldLink.getSrvcClient().getPassword());
		}
		
		
		c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10,0,0,5);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		fieldsPanel.add(questionLabel, c);
		
		c.insets = new Insets(5,0,0,5);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		fieldsPanel.add(lexiconTypeLabel, c);
		
		c.insets = new Insets(5,0,0,0);
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 3;
		fieldsPanel.add(lexiconTypeList, c);
		
		c.insets = new Insets(5,0,0,5);
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 4;
		fieldsPanel.add(descriptionLabel, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(5,0,0,0);
		c.weightx = 0.5;
		//c.weighty = 0.5;
		c.ipady = 40;
		c.gridx = 1;
		c.gridy = 4;
		fieldsPanel.add(descriptionTextScroller, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5,0,0,5);
		c.weightx = 0;
		c.weighty = 0;
		c.ipady = 0;
		c.gridx = 0;
		c.gridy = 5;
		fieldsPanel.add(urlLabel, c);
		
		JPanel URLPanel = new JPanel(new GridBagLayout());
		GridBagConstraints urlc = new GridBagConstraints();
		urlc.fill = GridBagConstraints.HORIZONTAL;
		urlc.weightx = 1.0;
		urlc.insets = new Insets(4, 0, 0, 6);
		URLPanel.add(urlTextField, urlc);
		urlc.weightx = 0.0;
		urlc.gridx = 1;
		urlc.insets = new Insets(6, 0, 0, 0);
		URLPanel.add(defaultURLButton, urlc);
		
		c.insets = new Insets(5,0,0,0);
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 5;
		fieldsPanel.add(URLPanel, c);
		
		c.insets = new Insets(5,0,0,5);
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 6;
		fieldsPanel.add(userNameLabel, c);
		
		c.insets = new Insets(5,0,0,0);
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 6;
		fieldsPanel.add(userNameTextField, c);
		
		c.insets = new Insets(5,0,0,5);
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 7;
		fieldsPanel.add(passWordLabel, c);
		
		c.insets = new Insets(5,0,0,0);
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 7;
		fieldsPanel.add(passWordTextField, c);
		
		this.setLayout(new GridBagLayout());
		
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 1;
		c.insets = new Insets(5,0,0,0);
		
		add(fieldsPanel, c);
	}

	private void postInit() {
		showDescription();
		setUrl();
		checkFields();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == urlTextField) {
			checkFields();
		} else if (ae.getSource() == defaultURLButton) {
			setUrl();
		} else if (ae.getSource() == userNameTextField) {
			checkFields();
		} else if (ae.getSource() == passWordTextField) {
			checkFields();
		}
	}

	/**
	 * Checks whether all fields are filled
	 */
	private void checkFields() {
		if(!urlTextField.getText().equals("") &&
				!userNameTextField.getText().equals("") &&
				!passWordTextField.getText().equals("") &&
				!nameField.getText().equals("")) {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
		} else {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		showDescription();
		setUrl();
	}

	private void showDescription() {
		if (lexiconServiceClientFactories == null || lexiconServiceClientFactories.isEmpty()) {// should not happen
			descriptionText.setText("");
		} else {
			descriptionText.setText(lexiconServiceClientFactories.get(lexiconTypeList.getSelectedItem()).getDescription());
		}
	}
	
	private void setUrl() {
		if(oldLink == null) {
			if (lexiconServiceClientFactories != null && lexiconServiceClientFactories.size() > 0) {// this should not happen...
				urlTextField.setText(lexiconServiceClientFactories.get(lexiconTypeList.getSelectedItem()).getDefaultUrl());
			} else {
				urlTextField.setText("");
			}
		} else {
			urlTextField.setText(oldLink.getSrvcClient().getUrl());
		}
	}

	/**
	 * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
	 */
	@Override
	public String getStepTitle() {
        return ElanLocale.getString("EditLexSrvcDialog.Title.Step1");
    }
	
	/**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {
        // the next button is already disabled
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepBackward() {
    	multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
        multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, false);
    }
    
    /**
     * tries to connect to the lexicon service and get the lexica of the workspace of the user
     * 
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
    	Boolean nameExists = transcription.getLexiconLinks().containsKey(nameField.getText());
    	if((oldLink == null && nameExists) || (oldLink != null && nameExists &&  !oldLink.getName().equals(nameField.getText()))) {
    		JOptionPane.showMessageDialog(this,
    			    ElanLocale.getString("EditLexSrvcDialog.Message.ServicenameExists"),
    			    ElanLocale.getString("Message.Warning"),
    			    JOptionPane.WARNING_MESSAGE);
    		nameField.requestFocus();
    	} else {
	    	try {
	    		LexiconServiceClient client = lexiconServiceClientFactories.get(lexiconTypeList.getSelectedItem()).
	    			createClient(urlTextField.getText(), userNameTextField.getText(), passWordTextField.getText());
	    		multiPane.putStepProperty("serviceClient", client);
	    		multiPane.putStepProperty("linkName", nameField.getText());
	    		List<LexiconIdentification> lexicaIds = client.getLexiconIdentifications();
	    		Collections.sort(lexicaIds);
	    		multiPane.putStepProperty("lexicaIds", lexicaIds);
	    		
				return true;
			}
			catch (LexiconServiceClientException ex) {
				// Show why the user couldn't do the action
				String title = ElanLocale.getString("LexiconLink.Action.Error");
				String message = title + "\n" + ElanLocale.getString("LexiconServiceClientException.Cause") + 
					" " + ex.getMessageLocale();
				JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
			}
    	}
    	return false;
    }
   
    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepBackward()
     */
    @Override
	public boolean leaveStepBackward() {
    	return true;
    }

    @Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent ae) {
		if(ae.getSource() == urlTextField) {
			checkFields();
		} else if (ae.getSource() == userNameTextField) {
			checkFields();
		} else if (ae.getSource() == passWordTextField) {
			checkFields();
		} else if (ae.getSource() == nameField) {
			checkFields();
		}
	}

	@Override
	public void keyTyped(KeyEvent ae) {
	}
}

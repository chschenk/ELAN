package mpi.eudico.client.annotator.webserviceclient.tc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.imports.MergeUtil;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.PropertyImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.webserviceclient.typecraft.TCParser;
import mpi.eudico.webserviceclient.typecraft.TCTextId;
import mpi.eudico.webserviceclient.typecraft.TCWsClient;
import mpi.eudico.webserviceclient.typecraft.TCtoTranscription;

import org.xml.sax.SAXException;

/* 
 * ElanLocale is not used at the moment. Still to be implemented. 
 * 
 */

/**
 * Step in which a text can be downloaded either by selecting it from the list of own
 * texts or by entering the id of a text.
 * 
 * example: ImportPraatTGStep1 / ImportPraatTGStep2
 */
@SuppressWarnings("serial")
public class TypeCraftStep2 extends StepPane implements ActionListener{
    
    private JButton listTextButton;
    private JButton importTextButton;
    private JList textList;
    private DefaultListModel model;
    private JLabel loadByIdLabel;
    private JTextField idTextField;
    private JButton loadByIdButton;
    
    /**
     * Create a new instance of the first step of the wizard.
     *
     * @param multiPane the parent pane
     */
    public TypeCraftStep2(MultiStepPane multiPane) {
        super(multiPane);
        initComponents();
    }

    /**
     * Initialize buttons, textfield and list pane etc.
     * 
     * @see mpi.eudico.client.annotator.gui.multistep.StepPane#initComponents()
     */
    @Override
	public void initComponents() {
	
	// First, create the text import button
	
        listTextButton = new JButton("List my texts");

        // Next, create text a list box.
        model = new DefaultListModel();
        textList = new JList(model);
        textList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        
        // Finally, create a button for text import.
        
        importTextButton = new JButton("Import selected text");
        importTextButton.setEnabled(false);
        // Put the components in a new panel by means of a gridbaglayout.
        
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        Insets insets = new Insets(2, 0, 2, 0);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.insets = insets;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // Position the components on the grid.
        
        // Still to be done: evaluate button sizes and change when necessary

        add(listTextButton,gbc);
        listTextButton.addActionListener(this);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 3;
        add(new JScrollPane(textList),gbc);
        
        // Still to be done: fill textList
     
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        add(importTextButton,gbc);
        importTextButton.addActionListener(this);
        
        loadByIdLabel = new JLabel("Import text by ID: ");
        idTextField = new JTextField();
        loadByIdButton = new JButton("Import");
        loadByIdButton.addActionListener(this);
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 2, 0);
        add(loadByIdLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(idTextField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(loadByIdButton, gbc);
    }
    
    /**
     * Button event handling. 
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {

    	Object source;
    	source = e.getSource();
	
		if (source == listTextButton) {
			Object sid = multiPane.getStepProperty("SessionId");
			Object wsclObj = multiPane.getStepProperty("TCWsClient");
			if (sid instanceof String && wsclObj instanceof TCWsClient) {
				String sessionId = (String) sid;
				TCWsClient tcws = (TCWsClient) wsclObj;
				List<TCTextId> texts = tcws.listTexts(sessionId);
				if (texts == null) {
					showMessage("Failed to list the texts.");
				} else if (texts.size() == 0) {
					showMessage("There are no texts of this user.");
				} else {
					model.clear();
					for (int i = 0; i < texts.size(); i++) {
						model.addElement(texts.get(i));
					}
					textList.setSelectedIndex(0);
					importTextButton.setEnabled(true);
				}
			} else {
				// show message and go to login
				showMessage("Not logged in or the session timed out.");
				multiPane.previousStep();
			}
		}
		else if (source == importTextButton) {
			Object sid = multiPane.getStepProperty("SessionId");
			Object wsclObj = multiPane.getStepProperty("TCWsClient");
			if (sid instanceof String && wsclObj instanceof TCWsClient) {
			   String sessionId = (String) sid;
			   TCWsClient tcws = (TCWsClient) wsclObj;
				TCTextId selText = (TCTextId) textList.getSelectedValue();
				if (selText != null) {
					String textId = selText.id;
					String result = tcws.downloadText(textId, sessionId);
					if (result == null) {
						showMessage("Failed to download the specified text");
						return;
					} else {
						parseText(result);
					}
				}
		   }
//	    multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
		}   
		else if (source == loadByIdButton) {
			String id = idTextField.getText();
			if (id != null) {
				id = id.trim();
				if (id.length() != 0) {
					Object sid = multiPane.getStepProperty("SessionId");
					Object wsclObj = multiPane.getStepProperty("TCWsClient");
					if (sid instanceof String && wsclObj instanceof TCWsClient) {
					   String sessionId = (String) sid;
					   TCWsClient tcws = (TCWsClient) wsclObj;

						String result = tcws.downloadText(id, sessionId);
						if (result == null) {
							showMessage("Failed to download the specified text");
							return;
						} else {
							parseText(result);
						}
						
				   }
				} else {
					showMessage("No Text ID specified!");
				}
			} else {
				showMessage("No Text ID specified!");
			}
		}
	 
    }
    
    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return "TypeCraft download text";
    }
    
    /**
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {
        
	// none for the moment 
    }
    
    /**
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {

        return true;
    }

    /**
     *
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent lse) {

    }
    
    private void showMessage(String message) {
    	JOptionPane.showMessageDialog(this, message, ElanLocale.getString("Message.Warning"), 
    			JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Parses the xml and adds the annotations to the existing transcription.
     * 
     * @param result
     */
    private void parseText(String result) {
    	TCParser parser = new TCParser(result);
    	try {
    		parser.parse();
    	} catch (SAXException se) {
    		showMessage("A parsing error occurred: " + se.getMessage());
    		return;
    	} catch (IOException ioe) {
    		showMessage("An input error occurred: " + ioe.getMessage());
    		return;
    	}
    	TCtoTranscription tc2t = new TCtoTranscription();
    	TranscriptionImpl trans = tc2t.createTranscription(parser.getPhraseRecords(), parser.getTextRecord());
    	if (trans == null) {
    		showMessage("Could not extract tiers and annotations from the returned content.");
    		return;
    	}
    	// get the transcription to add to
    	TranscriptionImpl oldTrans = null;
    	Object oldTransObj = multiPane.getStepProperty("transcription");
    	if (oldTransObj instanceof TranscriptionImpl) {
    		oldTrans = (TranscriptionImpl) oldTransObj;
    	}
    	
    	if (oldTrans != null) { //add to existing transcription
    		// copy properties
			List<Property> props = trans.getDocProperties();
			Property prop;
			
			for (int i = 0; i < props.size(); i++) {
				prop = props.get(i);
				PropertyImpl np = new PropertyImpl(prop);
				oldTrans.addDocProperty(np);
			}
			
    		Tier oldPhraseTier = oldTrans.getTierWithId(TCtoTranscription.PHRASE);
    		
    		if (oldPhraseTier != null) {
    			// add to existing tiers or rename the new tiers before adding?
    			// currently simply adds without checks
    			DefaultMutableTreeNode node = null;
    			AbstractAnnotation aa = null;
    			TierImpl phraseTier = trans.getTierWithId(TCtoTranscription.PHRASE);
    			if (phraseTier != null) {
	    			List<AbstractAnnotation> annos = phraseTier.getAnnotations();
	    			
	    			oldTrans.setNotifying(false);
	    			
	    			for (int i = 0; i < annos.size(); i++) {
	    				aa = annos.get(i);
	    				node = AnnotationRecreator.createNodeForAnnotation(aa);
	    				AnnotationRecreator.createAnnotationFromTree(oldTrans, node);
	    			}
	    			
	    			oldTrans.setNotifying(true);
	    			
	    			showMessage("The text has been imported.");
    			} else {
    				showMessage("No phrase tier found, import failed.");
    			}
    		} else {
    			// add all tiers
    			MergeUtil merger = new MergeUtil();
    			List<TierImpl> addTiers = merger.getAddableTiers(trans, oldTrans, null);
    			
    			merger.addTiersTypesAndCVs(trans, oldTrans, addTiers);
    			// hier there must already be utility method for this
    			DefaultMutableTreeNode node = null;
    			AbstractAnnotation aa = null;
    			TierImpl phraseTier = trans.getTierWithId(TCtoTranscription.PHRASE);
    			if (phraseTier != null) {
	    			List<AbstractAnnotation> annos = phraseTier.getAnnotations();
	    			
	    			oldTrans.setNotifying(false);
	    			
	    			for (int i = 0; i < annos.size(); i++) {
	    				aa = annos.get(i);
	    				node = AnnotationRecreator.createTreeForAnnotation(aa);
	    				AnnotationRecreator.createAnnotationFromTree(oldTrans, node, true);
	    			}
	    			
	    			oldTrans.setNotifying(true);
	    			
	    			showMessage("The text has been imported.");
    			} else {
    				showMessage("No phrase tier found, import failed.");
    			}
    		}
    	} else {
    		// do something with the new transcription
    		multiPane.putStepProperty("transcription", trans);
    		FrameManager.getInstance().createFrame(trans);
    	}

    }
}

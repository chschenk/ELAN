package mpi.dcr;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;

import mpi.dcr.isocat.Profile;
import mpi.dcr.isocat.RestDCRConnector;


/**
 * A panel with 2 or 3 sub panels, one with a list of (pre) selected profiles,
 * one with the data categories of the selected profile and a description
 * panel for the selected data category.
 *
 * @author Han Sloetjes
 * @version 1.0
 * @version 2.0 change from old Syntax server GMT webservice to ISOcat REST webservice
 * @author aarsom
 * @version 3.0 The list with the data categories of selected profile is replaced with 
 * JTable and the description panel updated to show multi-lingual values
 * 
 */
@SuppressWarnings("serial")
public class RemoteDCSelectPanel extends AbstractDCSelectPanel2
    implements ActionListener {
   
	/** the profiles panel */
    private JPanel profPanel;

    /** the list of profiles */
    private JList profList;
    
    private JLabel errorLabel;

    /** the profiles list model */
    protected DefaultListModel profModel;
    
    //protected JButton refreshButton;
    /**
     * Creates a new RemoteDCSelectPanel instance
     *
     * @param connector the connector
     */
    public RemoteDCSelectPanel(ILATDCRConnector connector) {
        super(connector);

        if (connector instanceof RestDCRConnector) {
            this.connector = (RestDCRConnector) connector;
        }

        initComponents();
    }

    /**
     * Creates a new RemoteDCSelectPanel instance
     *
     * @param connector the connector
     * @param resBundle a resource bundle
     */
    public RemoteDCSelectPanel(ILATDCRConnector connector,
        ResourceBundle resBundle) {
        super(connector, resBundle);

        if (connector instanceof RestDCRConnector) {
            this.connector = (RestDCRConnector) connector;
        }

        initialize();
    }

    

    /**
     * Creates a new RemoteDCSelectPanel instance
     */
    public RemoteDCSelectPanel() {
        super();
        initialize();
    }

    /**
     * Initializes the ui components.
     */
    @Override
	protected void initComponents() {
    	setLayout(new GridBagLayout());
    	
        // profile panel
        profPanel = new JPanel(new GridBagLayout());
        profModel = new DefaultListModel();
        profList = new JList(profModel);
        
        errorLabel = new JLabel();
        errorLabel.setVisible(false);
        
        profPanel.setMinimumSize(dim);
        profPanel.setPreferredSize(dim);

        profList.getSelectionModel()
                .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        profList.addListSelectionListener(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = globalInsets;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        profPanel.add(new JScrollPane(profList), gbc);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = globalInsets;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        add(errorLabel, gbc);
        
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = globalInsets;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(profPanel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(catPanel, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        add(descPanel, gbc);

        updateLocale();
        
        // load all profiles, better do this asynchroniously
        if (connector != null) {
            try {
            	ArrayList<Profile> profiles = (ArrayList<Profile>) connector.getProfiles();
                addProfiles(profiles);
            } catch (DCRConnectorException dce) {
                // log
                dce.printStackTrace();
                
                String errorMessage;
            	if (resBundle != null) {
            		errorMessage = resBundle.getString("DCR.Message.NoConnection");
            	} else {
            		errorMessage = "Could not connect to the registry: ";
            	}
            	errorMessage = errorMessage + " " + dce.getMessage();
            	errorLabel.setText(errorMessage);
            	errorLabel.setVisible(true);
            	errorLabel.setForeground(Color.RED);
            	//JOptionPane.showMessageDialog(RemoteDCSelectPanel.this, errorMessage, "", JOptionPane.ERROR_MESSAGE);
            	
            }
        }
    }

    /**
     * Applies localized texts to the elements, if provided.
     */
    @Override
	protected void updateLocale() {
        super.updateLocale();
       
        String selProf = "Select Profile";
        String selCats = "Select Categories";

        if (resBundle != null) {
        	try {
                selProf = resBundle.getString("DCR.Label.SelectProfile");
            } catch (Exception ex) {
            }
        	
            try {
                selCats = resBundle.getString("DCR.Label.SelectCategories");
            } catch (Exception ex) {
            }
        }
        
        profPanel.setBorder(new TitledBorder(selProf));
        catPanel.setBorder(new TitledBorder(selCats));
    }

    /**
     * Adds the profiles in the specified list to the current list.
     *
     * @param profiles profiles to add
     */
    public void addProfiles(List<Profile> profiles) {
        if (profiles != null) {
        	Profile profile;

profilesloop: 
            for (int i = 0; i < profiles.size(); i++) {
            	profile = profiles.get(i);

                if (profile == null) {
                    continue;
                }

                for (int j = 0; j < profList.getModel().getSize(); j++) {
                    if (profile.getId().equals(((Profile)profList.getModel().getElementAt(j)).getId())) {
                        continue profilesloop;
                    }
                }

                // not in the list already
                ((DefaultListModel) profList.getModel()).addElement(profile);
            }

            if (profList.getModel().getSize() == 1) {
                profList.setSelectedIndex(0);
            }
        }
    }
    

    /**
     * Returns a list of data categories belonging to the specified profile.
     *
     * @param profile the ID of the profile
     *
     * @return a list of data categories
     */
    protected List<DCSmall> getDataCategories(Profile profile) {
        List<DCSmall> datCats = null;
        
        try {
            datCats = connector.getDCSmallList(profile.getId(), null);

            if (datCats.size() == 0) {
            	String message;
            	if (resBundle != null) {
            		message = resBundle.getString("DCR.Message.NoCategories");
            	} else {
            		message = "No categories available in this profile";
            	}
                JOptionPane.showMessageDialog(this, 
                		message, "", 
                		JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (DCRConnectorException dce) {
        	String message;
        	if (resBundle != null) {
        		message = resBundle.getString("DCR.Message.NoConnection");
        	} else {
        		message = "Could not connect to the registry: ";
        	}
            JOptionPane.showMessageDialog(this, (message + " " + dce.getMessage()), "", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException iae) {
        	String message;
        	if (resBundle != null) {
        		message = resBundle.getString("DCR.Message.NoConnection");
        	} else {
        		message = "Could not connect to the registry: ";
        	}
            JOptionPane.showMessageDialog(this, (message + " " + iae.getMessage()), "", JOptionPane.ERROR_MESSAGE);
        }
        
        return datCats;
    }
    
    /**
     * Handles changes in the category selection
     *
     * @param e list selection event
     */
    @Override
	public void valueChanged(ListSelectionEvent e) {
    	
    	if(e.getSource() == profList){
    		if (!e.getValueIsAdjusting()) {
    			
    			langCombo.removeActionListener(this);
    	        langCombo.removeAllItems();
    	        langCombo.addItem(EMPTY);
    	        langCombo.addActionListener(this);
    	        	
    	        nameValueLabel.setText(EMPTY);
    	        descArea.setText("");
    			
                if (profList.getSelectedIndex() > -1) {
                    Profile profile = (Profile) profList.getSelectedValue();
                    // busy cursor
                    this.setCursor(super.BUSY_CURSOR);
                   
                    updateCategories(getDataCategories(profile));
                    
                    this.setCursor(super.DEFAULT_CURSOR);
                    // not busy
 
                } else {
                    // empty the category list/tree
                    updateCategories(new ArrayList<DCSmall>());
                }
            }
    	} else {
    		super.valueChanged(e);
    		
    	}
    }
}

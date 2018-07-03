package mpi.eudico.client.annotator.prefs.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;


/**
 * A dialog setting and changing user preferences.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class EditPrefsDialog extends ClosableDialog implements ActionListener,
    TreeSelectionListener {
    private JLabel titleLabel;
    private JScrollPane treeScrollPane;
    private JPanel prefCatPanel;
    private JButton applyButton;
    private JButton cancelButton;
    private JPanel buttonPanel;
    private JTree catTree;
    private HashMap<String, String> catKeyMap;
    private HashMap<String, JPanel> activatedPanels;
    //private GridBagConstraints prefCatGBC;
    private CardLayout cardLayout;
    private JPanel currentEditPanel = null;
    
    /**
     * Creates a new EditPrefsDialog instance
     *
     * @param owner the parent
     * @param modal modal flag
     *
     * @throws HeadlessException
     */
    public EditPrefsDialog(Frame owner, boolean modal)
        throws HeadlessException {
        this(owner, "", modal);
    }

    /**
     * Creates a new EditPrefsDialog instance
     *
     * @param owner the parent
     * @param title the dialog title
     * @param modal modal flag
     *
     * @throws HeadlessException
     */
    public EditPrefsDialog(Frame owner, String title, boolean modal)
        throws HeadlessException {
        super(owner, title, modal);
        
        initComponents();
    }

    /**
     * Creates a new EditPrefsDialog instance
     *
     * @param owner the parent!
     * @param modal modal flag
     *
     * @throws HeadlessException
     */
    public EditPrefsDialog(Dialog owner, boolean modal)
        throws HeadlessException {
        this(owner, "", modal);
    }

    /**
     * Creates a new EditPrefsDialog instance
     *
     * @param owner the parent!
     * @param title the dialog title
     * @param modal modal flag
     *
     * @throws HeadlessException
     */
    public EditPrefsDialog(Dialog owner, String title, boolean modal)
        throws HeadlessException {
        super(owner, title, modal);
        initComponents();
    }

    private void postInit() {
        pack();

        int w = 720;
        int h = 450;
        setSize((getSize().width < w) ? w : getSize().width,
            (getSize().height < h) ? h : getSize().height);
        setLocationRelativeTo(getParent());
    }

    /**
     * Adds a title panel, a preferences category tree panel, a placeholder
     * panel for category specific panels and a button panel to the layout.
     */
    private void initComponents() {
        catKeyMap = new HashMap<String, String>();
        activatedPanels = new HashMap<String, JPanel>();
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        titleLabel = new JLabel();
        int origFontSize = titleLabel.getFont().getSize();
        float titleSize = 1.2f * origFontSize;
        titleLabel.setFont(titleLabel.getFont().deriveFont(titleSize));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
/*
        prefCatGBC = new GridBagConstraints();
        prefCatGBC.anchor = GridBagConstraints.NORTHWEST;
        prefCatGBC.fill = GridBagConstraints.BOTH;
        prefCatGBC.weightx = 1.0;
        prefCatGBC.weighty = 1.0;
        prefCatGBC.gridx = 0;
        prefCatGBC.gridy = 0;
*/
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(6, 6, 10, 6);
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        getContentPane().add(titleLabel, gbc);

        catTree = new JTree(new DefaultMutableTreeNode(""));
        catTree.getSelectionModel()
               .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        //catTree.setRootVisible(false);
        //catTree.setShowsRootHandles(true);
        final DefaultTreeCellRenderer defaultTreeCellRenderer = (DefaultTreeCellRenderer) catTree.getCellRenderer();
		defaultTreeCellRenderer.setLeafIcon(null);
        defaultTreeCellRenderer.setOpenIcon(null);
        defaultTreeCellRenderer.setClosedIcon(null);

        // the preferences categories tree
        treeScrollPane = new JScrollPane(catTree);

        Dimension dim = new Dimension(200, 300);
        treeScrollPane.setPreferredSize(dim);
        treeScrollPane.setMinimumSize(dim);
        treeScrollPane.setBackground(Color.WHITE);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.weighty = 1.0;
        gbc.gridy = 1;
        getContentPane().add(treeScrollPane, gbc);

        cardLayout = new CardLayout();
        prefCatPanel = new JPanel(cardLayout);
        //prefCatPanel.add(new JLabel("Select a category..."));
        //cardLayout.addLayoutComponent(new JLabel("Select a category..."), "Intro_xxx");
        prefCatPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.SelectCategory")), "Intro_xxx");
        cardLayout.show(prefCatPanel, "Intro_xxx");

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = insets;
        gbc.gridy = 1;
        gbc.gridx = 1;
        getContentPane().add(prefCatPanel, gbc);

        // buttons
        buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        applyButton = new JButton();
        applyButton.addActionListener(this);
        cancelButton = new JButton();
        cancelButton.addActionListener(this);
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = insets;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.gridwidth = 2;
        getContentPane().add(buttonPanel, gbc);

        updateLocale();
        postInit();
        catTree.addTreeSelectionListener(this);
    }

    private void updateLocale() {
        setTitle(ElanLocale.getString("PreferencesDialog.Title"));
        titleLabel.setText(ElanLocale.getString("PreferencesDialog.Title"));
        applyButton.setText(ElanLocale.getString("Button.Apply"));
        cancelButton.setText(ElanLocale.getString("Button.Cancel"));

        // tree rows
        String val = ElanLocale.getString("PreferencesDialog.Category.Edit");
        catKeyMap.put(val, "PreferencesDialog.Category.Edit");
        ((DefaultMutableTreeNode) catTree.getModel().getRoot()).add(new DefaultMutableTreeNode(
                val));
        val = ElanLocale.getString("PreferencesDialog.Category.CV");
        catKeyMap.put(val, "PreferencesDialog.Category.CV");
        ((DefaultMutableTreeNode) catTree.getModel().getRoot()).add(new DefaultMutableTreeNode(
                val));
        val = ElanLocale.getString("PreferencesDialog.Category.Media");
        catKeyMap.put(val, "PreferencesDialog.Category.Media");
        ((DefaultMutableTreeNode) catTree.getModel().getRoot()).add(new DefaultMutableTreeNode(
                val));
        val = ElanLocale.getString("PreferencesDialog.Category.Metadata");
        catKeyMap.put(val, "PreferencesDialog.Category.Metadata");
        ((DefaultMutableTreeNode) catTree.getModel().getRoot()).add(new DefaultMutableTreeNode(
                val));
        val = ElanLocale.getString("PreferencesDialog.Category.OS");
        catKeyMap.put(val, "PreferencesDialog.Category.OS");
        ((DefaultMutableTreeNode) catTree.getModel().getRoot()).add(new DefaultMutableTreeNode(
                val));
        val = ElanLocale.getString("PreferencesDialog.Category.Preferences");
        catKeyMap.put(val, "PreferencesDialog.Category.Preferences");
        ((DefaultMutableTreeNode) catTree.getModel().getRoot()).add(new DefaultMutableTreeNode(
                val));
        
        /* Mod by mark */
        val=ElanLocale.getString("PreferencesDialog.Category.UI");
        catKeyMap.put(val, "PreferencesDialog.Category.UI");
        ((DefaultMutableTreeNode) catTree.getModel().getRoot()).add(new DefaultMutableTreeNode(
                val));
        /* --- END --- */
        val = ElanLocale.getString("PreferencesDialog.Category.Viewer");
        catKeyMap.put(val, "PreferencesDialog.Category.Viewer");
        ((DefaultMutableTreeNode) catTree.getModel().getRoot()).add(new DefaultMutableTreeNode(
                val));
        
        catTree.setEditable(false);
        catTree.expandRow(0);
    }

    /**
     * Creates the panel corresponding to the specified key.
     *
     * @param key the key, as specified in the language properties files
     *
     * @return the edit panel
     */
    private JPanel getPanelForKey(String key) {
        if (key == null) {
            return null;
        }

        if (key.equals("PreferencesDialog.Category.Edit")) {
            return new EditingPanel();
        } else if (key.equals("PreferencesDialog.Category.CV")) {
            return new CVPanel();
        } else if (key.equals("PreferencesDialog.Category.Media")) {
            return new MediaNavPanel();
        } else if (key.equals("PreferencesDialog.Category.Metadata")) {
        	return new MetadataPanel();
        } else if (key.equals("PreferencesDialog.Category.OS")) {
        	return new PlatformPanel();
        } else if (key.equals("PreferencesDialog.Category.Preferences")) {
        	return new GeneralPrefsPanel();
        } /* Mod by mark */
        else if (key.equals("PreferencesDialog.Category.UI")) {
        	return new UIPrefsPanel();
        } /* --- END --- */ else if (key.equals("PreferencesDialog.Category.Viewer")) {
        	return new ViewerPanel();
        }

        return null;
    }

    private void applyChanges() {
        if (activatedPanels.size() == 0) {
            return;
        }

        Map<String, Object> allChanges = new HashMap<String, Object>();

        for (JPanel nextObj : activatedPanels.values()) {

            if (nextObj instanceof PreferenceEditor) {
            	PreferenceEditor ed = (PreferenceEditor) nextObj;

                if (ed.isChanged()) {
                    allChanges.putAll(ed.getChangedPreferences());
                }
            }
        }

        if (allChanges.size() > 0) {
            String key = null;
            Object val = null;
            
        	for (Map.Entry<String, Object> e : allChanges.entrySet()) {
        		key = e.getKey();
        		val = e.getValue();

                Preferences.set(key, val, null);
            }
    	    // repeat the last change: trigger saving the preferences
    	    Preferences.set(key, val, null, true, true);
        }
    }
    
    private void closeDialog(){
    	// remove the temporary value
        Preferences.set("Media.VideosCentre.Temporary", null, null);	    	
    	dispose();     
    	
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        if (event.getSource() == applyButton) {
        	
        	if(currentEditPanel instanceof EditingPanel){
            	if(!((EditingPanel)currentEditPanel).validateInputs()){ 
            		return;
            	}
            } else if(currentEditPanel instanceof CVPanel){
            	if(!((CVPanel)currentEditPanel).validateInputs()){ 
            		return;
            	}
            }
        	
        	applyChanges();            
            setVisible(false);
            closeDialog();            
        } else if (event.getSource() == cancelButton) {        	        	
            setVisible(false);
            closeDialog();
        }
    }

    /**
     * Handles selection changes in the category tree.
     *
     * @param e the event
     */
    @Override
	public void valueChanged(TreeSelectionEvent e) {
    	if(currentEditPanel instanceof EditingPanel){
        	if(!((EditingPanel)currentEditPanel).validateInputs()){ 
        		catTree.removeTreeSelectionListener(this);
        		catTree.setSelectionPath(e.getOldLeadSelectionPath());
        		catTree.addTreeSelectionListener(this);	
        		return;
        	}
        } else if(currentEditPanel instanceof CVPanel){
        	if(!((CVPanel)currentEditPanel).validateInputs()){ 
        		catTree.removeTreeSelectionListener(this);
        		catTree.setSelectionPath(e.getOldLeadSelectionPath());
        		catTree.addTreeSelectionListener(this);	
        		return;
        	}
        }
    	
        Object selNode = e.getPath().getLastPathComponent();

        if (selNode instanceof DefaultMutableTreeNode) {
            String key = (String) ((DefaultMutableTreeNode) selNode).getUserObject();
            String val = catKeyMap.get(key);
            
            if ((currentEditPanel != null) &&
                    (activatedPanels.get(val) == currentEditPanel)) {
                return;
            } else if (activatedPanels.get(val) != null) {
            	
            	if(activatedPanels.get(val) instanceof ViewerPanel){
                	Boolean value = Preferences.getBool("Media.VideosCentre.Temporary",null);	
                	if (value != null) {
                		((ViewerPanel)activatedPanels.get(val)).updateVideoInCentre(value);
                	}
                }           	
            	
            	cardLayout.show(prefCatPanel, val);

                currentEditPanel = activatedPanels.get(val);
            } else {
                JPanel nextPanel = getPanelForKey(val);

                if (nextPanel != null) {
                	
                	if(nextPanel instanceof ViewerPanel){
                    	Boolean value = Preferences.getBool("Media.VideosCentre.Temporary",null);	
                    	if (value != null) {
                    		((ViewerPanel)nextPanel).updateVideoInCentre(value);
                    	}
                    }
                	prefCatPanel.add(nextPanel, val);
                	cardLayout.show(prefCatPanel, val);
                    currentEditPanel = nextPanel;
                    activatedPanels.put(val, nextPanel);
                }
            }
        }
    }
}

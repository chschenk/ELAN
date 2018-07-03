package mpi.eudico.client.annotator.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl.AnnotatorGetter;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl.LanguageGetter;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl.LinguisticTypeNameGetter;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl.ParticipantGetter;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl.ValueGetter;

/**
 * A dialog that gives the user the possibility to change the visibility of
 * more tiers at a time.
 *
 * @author Han Sloetjes
 * @author Olaf Seibert
 */
@SuppressWarnings("serial")
public class ShowHideMoreTiersDlg extends ClosableDialog
    implements ActionListener, MouseListener {
	
    /** A constant for unspecified participant or linguistic type */
    private final String NOT_SPECIFIED = "not specified";
    
    private final String SHOW_TIERS = ElanLocale.getString(
							"MultiTierControlPanel.Menu.ShowTiers") ;
    private final String SHOW_TYPES = ElanLocale.getString(
							"MultiTierControlPanel.Menu.ShowLinguisticType");
    private final String SHOW_PART = ElanLocale.getString(
							"MultiTierControlPanel.Menu.ShowParticipant");
    private final String SHOW_ANN = ElanLocale.getString(
							"MultiTierControlPanel.Menu.ShowAnnotator") ;
    private final String SHOW_LANG = ElanLocale.getString(
			                "MultiTierControlPanel.Menu.ShowLanguage") ;
    // the above final fields are language (locale) dependent and therefore less suitable for 
    // storage in the preferences, hence the values below 
    private final String SHOW_TIERS_LIPREF = "ShowTiers";
    private final String SHOW_TYPES_LIPREF = "ShowTypes";
    private final String SHOW_PART_LIPREF = "ShowParticipants";
    private final String SHOW_ANN_LIPREF = "ShowAnnotators";
    private final String SHOW_LANG_LIPREF = "ShowLanguages";
    
    // components
    private JPanel checkboxPanel;   
    private JButton showAllButton;
    private JButton hideAllButton;
    private JButton applyButton;
    private JButton cancelButton;    
    private JButton sortButton;
    private JButton sortDefaultButton;
    private JTabbedPane showTabPane;
    
    private Transcription trans;   
    private List<String> allTierNames;
    private List<String> allLinTypeNames;
    private List<String> allParticipants;
    private List<String> allAnnotators;
    private List<String> allLanguages;
       
    private List<String> visibleTypeNames;
    private List<String> visibleTierNames;
    private List<String> visibleParts;
    private List<String> visibleAnns;
    private List<String> visibleLanguages;
    private int currentTabIndex = 0;    

    private boolean rootTiersOnly = false;
    
    private boolean valueChanged = false;

	private List<String> hiddenTiers;
    
    /**
     * Creates and shows a modal dialog, displaying checkbox items for each
     * tier in the transcription. The user can change the visibility of
     * more tiers   at a time.
     *
     * @param trans the transcription
     * @param visibleTiers the currently visible tiers
     */
    public ShowHideMoreTiersDlg(Transcription trans, List/*<String | TierImpl>*/ visibleTiers) {    	
    	this(trans,visibleTiers, null);
    }
    
    /**
     * Creates and shows a modal dialog, displaying checkbox items for each
     * tier in the transcription. The user can change the visibility of
     * more tiers at a time.
     *
     * @param trans the transcription
     * @param visibleTiers the currently visible tiers
     * @param component, the component used to set the location of the this dialog
     */
    public ShowHideMoreTiersDlg(Transcription trans, List/*<String | TierImpl>*/ visibleTiers, Component component) {
       this(trans, visibleTiers, component, false);
    }
    
    public ShowHideMoreTiersDlg(Transcription trans, List/*<String | TierImpl>*/ visibleTiers, Component component, boolean rootTiersOnly) {
        this(trans, null, visibleTiers, component, rootTiersOnly);
     }    

    public ShowHideMoreTiersDlg(Transcription trans, List<String> tierOrder, List/*<String | TierImpl>*/ visibleTiers, Component component) {
       this(trans, tierOrder, visibleTiers, component, false);
    }
    
    /**
     * Creates and shows a modal dialog, displaying checkbox items for each
     * tier in the transcription. The user can change the visibility of
     * more tiers at a time.
     *
     * @param trans the transcription
     * @param tierOrder current order of tiers
     * @param visibleTiers the currently visible tiers
     * @param component, the component used to set the location of the this dialog
     * @param rootTiersOnly show only root tiers in table
     */
    public ShowHideMoreTiersDlg(Transcription trans, List<String> tierOrder, List/*<String | TierImpl>*/ visibleTiers, Component component, boolean rootTiersOnly) {
        super(ELANCommandFactory.getRootFrame(trans),
            ElanLocale.getString("MultiTierControlPanel.Menu.VisibleTiers"),
            true);
        
        this.trans = trans;       
        this.rootTiersOnly = rootTiersOnly;
        
        initialize(tierOrder, visibleTiers);
        initDialog();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocation(component);       
    }
    
    private void initialize(List<String> tierOrder, List/*<String | TierImpl>*/ visibleTiers){    	
    	if(tierOrder == null){    		
        	allTierNames = ELANCommandFactory.getViewerManager(trans).getTierOrder().getTierOrder();
        	
        } else {
        	allTierNames = tierOrder;
        }   
    	
    	if(rootTiersOnly){
    		TierImpl tier;
        	int i=0;
        	while (i < allTierNames.size()){
        		tier = (TierImpl) trans.getTierWithId(allTierNames.get(i));
        		if(tier.hasParentTier()){
        			allTierNames.remove(i);
        		}else{
        			i++;
        		}
        	}
    	}
    	
        visibleTierNames = new ArrayList<String>();
        if(visibleTiers != null){
        	TierImpl tier = null;
        	Object obj = null;
    		for(int i=0; i< visibleTiers.size(); i++){
    			obj = visibleTiers.get(i);
    			if(obj instanceof TierImpl){
    				tier = (TierImpl) visibleTiers.get(i);
    				if(allTierNames.contains(tier.getName()) && !visibleTierNames.contains(tier.getName())){
    					visibleTierNames.add(tier.getName());
    				}
    			} else if (obj instanceof String){
    				if(allTierNames.contains((String)obj) && !visibleTierNames.contains((String)obj)){
    					visibleTierNames.add((String)obj);
    				}    				
//    				visibleTierNames.addAll(visibleTiers);
//    				break;
    			}
    		}
    	}   
        allLinTypeNames = new ArrayList<String>();       
        allParticipants = new ArrayList<String>();
        allAnnotators = new ArrayList<String>();            
        allLanguages = new ArrayList<String>();            
        visibleTypeNames = new ArrayList<String>();
        visibleParts = new ArrayList<String>();
        visibleAnns = new ArrayList<String>();
        visibleLanguages = new ArrayList<String>();
        
        hiddenTiers = new ArrayList<String>();
    }
    

    /**
     * Create components and add them to the content pane.
     */
    private void initDialog() {
        showAllButton = new JButton(ElanLocale.getString(
                    "MultiTierControlPanel.Menu.ShowAllTiers"));
        showAllButton.addActionListener(this);
        hideAllButton = new JButton(ElanLocale.getString(
                    "MultiTierControlPanel.Menu.HideAllTiers"));
        hideAllButton.addActionListener(this);
        applyButton = new JButton(ElanLocale.getString("Button.OK"));
        applyButton.addActionListener(this);
        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        
        sortButton = new JButton(ElanLocale.getString("MultiTierControlPanel.Menu.Button.Sort"));
        sortButton.addActionListener(this);
        
        sortDefaultButton = new JButton(ElanLocale.getString("MultiTierControlPanel.Menu.Button.Default"));
        sortDefaultButton.addActionListener(this);
        
        showTabPane = new JTabbedPane(); 
        
        JPanel buttonPanel1 = new JPanel(new GridBagLayout());
        buttonPanel1.add(sortButton);
        buttonPanel1.add(sortDefaultButton);
        buttonPanel1.add(showAllButton);
        buttonPanel1.add(hideAllButton);            

        GridLayout gl = new GridLayout(1, 2, 6, 2);
        JPanel buttonPanel2 = new JPanel(gl);          
        buttonPanel2.add(applyButton);
        buttonPanel2.add(cancelButton);
        

        getContentPane().setLayout(new GridBagLayout());
        getContentPane().setPreferredSize(new Dimension(600,400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(2, 6, 2, 6);
        gbc.gridy = 1;
        gbc.gridx = 0;
        getContentPane().add(buttonPanel1, gbc);
        gbc.gridy = 1;
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        getContentPane().add(buttonPanel2, gbc);

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        getContentPane().add(showTabPane, gbc);
        getRootPane().setDefaultButton(applyButton);

        
        checkboxPanel = new JPanel();
        BoxLayout box = new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS);
        checkboxPanel.setLayout(box);
        JCheckBox checkbox;
        TierImpl tier;
        String value;

        for (int i = 0; i < allTierNames.size(); i++) {
            tier = (TierImpl) trans.getTierWithId(allTierNames.get(i));
            checkbox = new JCheckBox(tier.getName());            
            checkboxPanel.add(checkbox);
            
            value = tier.getParticipant();
            if(value.isEmpty()){
            	value = NOT_SPECIFIED;
            }
            
            if(!allParticipants.contains(value)){
            	allParticipants.add(value);
            }

            value = tier.getAnnotator();
            if(value.isEmpty()){
            	value = NOT_SPECIFIED;
            }
            
            if(!allAnnotators.contains(value)){
            	allAnnotators.add(value);
            }
            
            value = tier.getLangRef();
            if(value == null || value.isEmpty()){
            	value = NOT_SPECIFIED;
            }
            
            if(!allLanguages.contains(value)){
            	allLanguages.add(value);
            }
            
            value = tier.getLinguisticType().getLinguisticTypeName();
            if(!allLinTypeNames.contains(value)){
            	allLinTypeNames.add(value);
            }
        }
       
        showTabPane.add(SHOW_TIERS, new JScrollPane(checkboxPanel));
    
        // "Show Linguistic Type(s)" tab
        checkboxPanel = new JPanel();
        box = new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS);
        checkboxPanel.setLayout(box);      
        for (int i = 0; i < allLinTypeNames.size(); i++) {        	
        	checkbox = new JCheckBox(allLinTypeNames.get(i));
        	checkboxPanel.add(checkbox);
        }
        showTabPane.add(SHOW_TYPES ,new JScrollPane(checkboxPanel));
    
        // "Show Participant(s)" tab
        checkboxPanel = new JPanel();    
        box = new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS);
        checkboxPanel.setLayout(box);
        for (int i = 0; i < allParticipants.size(); i++) {                
        	checkbox = new JCheckBox(allParticipants.get(i));
        	checkboxPanel.add(checkbox);
        }
        showTabPane.addTab(SHOW_PART , new JScrollPane(checkboxPanel));
  
        // "Show Annotator(s)" tab
        checkboxPanel = new JPanel();
        box = new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS);
        checkboxPanel.setLayout(box);
        for (int i = 0; i < allAnnotators.size(); i++) {                
        	checkbox = new JCheckBox(allAnnotators.get(i));
       		checkboxPanel.add(checkbox);
        }
        showTabPane.addTab(SHOW_ANN, new JScrollPane(checkboxPanel));  
        
        // "Show Language(s)" tab
        checkboxPanel = new JPanel();
        box = new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS);
        checkboxPanel.setLayout(box);
        for (int i = 0; i < allLanguages.size(); i++) {                
        	checkbox = new JCheckBox(allLanguages.get(i));
       		checkboxPanel.add(checkbox);
        }
        showTabPane.addTab(SHOW_LANG, new JScrollPane(checkboxPanel));  

        showTabPane.addMouseListener(this);
        
        showTiers();
    }
    
    public JTabbedPane getTabPane() {       	
    	return showTabPane;
    }
        
    /**
     * Get the JPanel that represents the index'th tab in the tab pane.
     * <p>
     * We constructed them all with a scroll pane around it, so reach inside it.
     * 
     * @param index
     * @return
     */
	private JPanel getCheckboxPanel(int index) {
		return (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView();
	}
    
	/**
	 * Fill a tab pane with fresh unchecked checkboxes.
	 * 
	 * @param index the index of the tab
	 * @param sortedList the texts for the checkboxes
	 */
    private void updateTab(int index, List<String> sortedList) {
    	JPanel checkboxPanel = getCheckboxPanel(index);
    	checkboxPanel.removeAll();

    	for (String s : sortedList) {
    		checkboxPanel.add(new JCheckBox(s));
    	}
    	
    	showTabPane.repaint(); 
    	updateTabAtIndex(index);
    }
        
    /**
     * Position the dialog relative to the given component.
     * 
     * @param component
     */
    private void setLocation(Component component) { 
    	if(component instanceof mpi.eudico.client.annotator.viewer.MultiTierControlPanel){
    		Point p = new Point(0, 0);
    		SwingUtilities.convertPointToScreen(p, component);

    		int windowHeight = SwingUtilities.getWindowAncestor(component)
                                         .getHeight();

    		if (this.getHeight() > windowHeight) {
    			// don't let the dialog be higher than the window
    			this.setSize(this.getWidth(), windowHeight);
    		}

    		p.x += component.getWidth();

    		// line at bottom
    		p.y -= (this.getHeight() - component.getHeight());
    		setLocation(p);     		
    	} else{
    		setLocationRelativeTo(getParent());
    	} 
    }
    
    public List<String> getVisibleTierNames(){  
    	return visibleTierNames;
    }
    
    /**
     * Returns the currently used selection mode
     * 
     * (i.e whether the selection of tiers is based on
     * types / participant/ tier names/ annotators / languages)
     * 
     * @return an identifier of the currently selected tab
     * 
     * @see #setSelectionMode(String, List)
     */
    public String getSelectionMode(){
    	if(visibleTypeNames.size() > 0){
    		return SHOW_TYPES_LIPREF;
    	} else if(visibleParts.size() > 0){
    		return SHOW_PART_LIPREF;
    	} else if(visibleAnns.size() > 0){
    		return this.SHOW_ANN_LIPREF;
    	} else if(visibleLanguages.size() > 0){
    		return this.SHOW_LANG_LIPREF;
    	}
    	return this.SHOW_TIERS_LIPREF;
    }
    
    /** 
     * Sets and removes the hidden tiers from selection
     * 
     * @param hiddenTiers
     */
    private void setHiddenTiers(List<String> hiddenTiers){   
    	if (hiddenTiers == null) {
    		return;
    	}
    	
    	this.hiddenTiers = hiddenTiers;
    	
		visibleAnns.clear();
     	visibleTypeNames.clear();  
     	visibleParts.clear();   
     	visibleLanguages.clear();  
   	
		ValueGetter getter = null;
		List<String> visibleValues = null;

    	String selectionMode = showTabPane.getTitleAt(currentTabIndex);
    	if(selectionMode.equals(SHOW_ANN)){
    		getter = new AnnotatorGetter();
    		visibleValues = visibleAnns;
    	}else if(selectionMode.equals(SHOW_TYPES)){
    		getter = new LinguisticTypeNameGetter();
    		visibleValues = visibleTypeNames;
    	}else if(selectionMode.equals(SHOW_PART)){
    		getter = new ParticipantGetter();
    		visibleValues = visibleParts;
    	} else if (selectionMode.equals(SHOW_LANG)) {    		
    		getter = new LanguageGetter();
    		visibleValues = visibleLanguages;
    	} else {
    		return;
    	}
    	
    	// Deduce visible participants (etc) from the visible tiers.
    	for (String visibleTierName : visibleTierNames) {
    		TierImpl t = (TierImpl) trans.getTierWithId(visibleTierName);
    		String value = getter.getSortValue(t); //t.getParticipant();

    		if (value.isEmpty()) {
    			value = NOT_SPECIFIED;
    		}

    		if (!visibleValues.contains(value)) {
    			visibleValues.add(value);
    		}
    	}

    	// Deduce visible tiers from visible participants (etc),
    	// but leave out the hidden tiers.
    	visibleTierNames.clear();        	
    	for (String tierName : allTierNames) {
    		TierImpl tier = (TierImpl) trans.getTierWithId(tierName);            	
    		String value = getter.getSortValue(tier); //tier.getParticipant();            	
    		if (value.isEmpty()) {
    			value = NOT_SPECIFIED;
    		}
    		if (visibleValues.contains(value) && !hiddenTiers.contains(tierName)) {
    			visibleTierNames.add(tier.getName());
    		}
    	}

    	updateTabAtIndex(currentTabIndex);
    }
    
    /**
     * Sets the selection mode and also the tiers hidden in that mode
     * 
     * HS Jan 2017 for selection mode test for both the old, locale dependent values 
     * and the new, locale independent values
     * @param setSelectType sets the tab to activate
     * @param hiddenTiers the tiers that are hidden in this mode
     * 
     * @see #getSelectionMode()
     */
    public void setSelectionMode(String selectionMode, List<String> hiddenTiers){      
    	if(selectionMode == null){
    		return;
    	}
    	if(selectionMode.equals(SHOW_ANN_LIPREF) || selectionMode.equals(SHOW_ANN)){  
            currentTabIndex = showTabPane.indexOfTab(SHOW_ANN);    		
    	}else if(selectionMode.equals(SHOW_TYPES_LIPREF) || selectionMode.equals(SHOW_TYPES)){    		
            currentTabIndex = showTabPane.indexOfTab(SHOW_TYPES);    
    	}else if(selectionMode.equals(SHOW_PART_LIPREF) || selectionMode.equals(SHOW_PART)){    		        	
    		currentTabIndex = showTabPane.indexOfTab(SHOW_PART);    
    	}else if(selectionMode.equals(SHOW_LANG_LIPREF) || selectionMode.equals(SHOW_LANG)){    		        	
    		currentTabIndex = showTabPane.indexOfTab(SHOW_LANG);    
    	} else {
    		currentTabIndex = showTabPane.indexOfTab(SHOW_TIERS);     		
    	}    	
    	showTabPane.setSelectedIndex(currentTabIndex);
    	setHiddenTiers(hiddenTiers);
    }
    
    /**
     * Returns the list of hidden ties of a 
     * certain group(type /participants/ annotators)
     * 
     * A tier is hidden if it was automatically selected because it has
     * the correct Participant (etc), but the user later de-selected it in
     * the Tier pane.
     * 
     * @return
     */
    public List<String> getHiddenTiers(){
    	if(trans == null){
    		return null;
    	}
    	ValueGetter getter = null;
    	List<String> visibleValues = null;
    	List<String> hiddenTiers = new ArrayList<String>();
    	
    	if(visibleTypeNames.size() > 0) {
    		getter = new LinguisticTypeNameGetter();
    		visibleValues = visibleTierNames;
    	} else if(visibleParts.size() > 0){    		
    		getter = new ParticipantGetter();
    		visibleValues = visibleParts;
    	} else if(visibleAnns.size() > 0){
    		getter = new AnnotatorGetter();
    		visibleValues = visibleAnns;
    	} else if(visibleLanguages.size() > 0){
    		getter = new LanguageGetter();
    		visibleValues = visibleLanguages;
    	} else {
    		return null;
    	}
    	
    	// Deduce which tiers should match the criterium,
    	// but were deselected anyway.
		for (String tierName : allTierNames) {
        	TierImpl tier = (TierImpl) trans.getTierWithId(tierName);            	
        	String value = getter.getSortValue(tier); // .getLangRef();            	
        	if (value.isEmpty()) {
        		value = NOT_SPECIFIED;
        	}
        	if (visibleValues.contains(value)) {
        		if (!visibleTierNames.contains(tierName)) {
        			hiddenTiers.add(tierName);
        		}
        	}
		}
    	
    	if (hiddenTiers.size() > 0) {
    		return hiddenTiers;
    	}  
    	
    	return null;
    }
    
    /**
     * Set the checkboxes of the Tiers tab.
     */
    private void showTiers(){
    	int index = showTabPane.indexOfTab(SHOW_TIERS);
  	    setCheckboxes(index, visibleTierNames);
    }
    
    /**
    * Take an index of one of the tab panes, and a list of Strings.
    * The checkboxes in that tab pane that represent one of the strings
    * get selected. The others get deselected.
    * 
    * @param index the index of the tabpane to process
    * @param select the checkboxes to select
    */
   private void setCheckboxes(int index, List<String> select) {
	   JPanel checkboxPanel = getCheckboxPanel(index);

	   for (Component comp : checkboxPanel.getComponents()) {
		   if (comp instanceof JCheckBox) {
			   JCheckBox box = (JCheckBox)comp;
			   boolean selected = select.contains(box.getText());
			   box.setSelected(selected);
		   }
	   }	   
   }
   
    /**
     * Check if the selection of tiers has changed.
     * If so, clear the selections of all other values, and hiddenTiers.
     */
    private void updateTiers(){
    	int index = showTabPane.indexOfTab(SHOW_TIERS);
    	JPanel checkboxPanel = getCheckboxPanel(index);
    	
    	List<String> oldVisibleTierNames = new ArrayList<String>();
    	oldVisibleTierNames.addAll(visibleTierNames);
    	boolean valueChanged = false;
    	
    	visibleTierNames.clear();
    	
    	Component[] boxes = checkboxPanel.getComponents();
        for (Component comp : boxes) {
            if (comp instanceof JCheckBox) {
            	JCheckBox box = (JCheckBox)comp;
            	if (box.isSelected()) {
            		String tierName = box.getText();                		
            		visibleTierNames.add(tierName);
            		if (!oldVisibleTierNames.contains(tierName)) {
            			// Changed from de-selected to selected.
            			// If the tier was considered hidden, it now no longer is.
            			if (hiddenTiers.contains(tierName)) {
            				hiddenTiers.remove(tierName);
            				// if v == false, v = false. Right.
            				//if(!valueChanged)
            				//	valueChanged = false;
            			} else {
            				// Tracking the hidden tiers has become pointless now.
            				valueChanged = true;
            			}
            		}
            	}
            	// If the box is now de-selected, no action is taken.
            	// Below we may add it to the hidden tiers,
            	// if we haven't given up on tracking it.
            }
        }
        
        if (valueChanged) {
        	hiddenTiers.clear();
        	//System.err.println("updateTiers: Cleared hiddenTiers");
        	visibleTypeNames.clear();    	
            visibleParts.clear();  
            visibleAnns.clear();
            visibleLanguages.clear();
        } else {
        	// Check all previously selected tiers.
        	// All those that are not selected any more, are called hidden.

        	for (String oldVisibleTierName : oldVisibleTierNames) {
        		if (!visibleTierNames.contains(oldVisibleTierName)) {
        			if (!hiddenTiers.contains(oldVisibleTierName)) {
        				hiddenTiers.add(oldVisibleTierName);
        	        	//System.err.printf("Adding to hiddenTiers: %s\n", oldVisibleTierName);
        			}
        		}
        	}
        	//System.err.printf("Added to hiddenTiers, now %s\n", hiddenTiers.toString());
        }
    }
    
    /**
     * Looking at one of the non-Tier panes, update the Tiers pane.
     * 
     * @param index which tab
     * @param visibleValues previously recorded selections (used to detect changes and to store new selections)
     * @param getter accessor for the field of the Tier
     * @return true if the selection in the pane was changed.
     */
    private boolean updateTiersFromOtherPane(int index, List<String> visibleValues, ValueGetter getter) {
    	JPanel checkboxPanel = getCheckboxPanel(index);
    	
    	boolean changed = false;        	
    	List<String> selectedValues = new ArrayList<String>();
    	
    	Component[] boxes = checkboxPanel.getComponents();
        for (Component comp : boxes) {
            if (comp instanceof JCheckBox) {
            	JCheckBox box = (JCheckBox)comp;
            	if (box.isSelected()) {
            		String value = box.getText();                		
            		if (!selectedValues.contains(value)) {
            			selectedValues.add(value);
            		}
            	} 
            }
        }
        
        if (selectedValues.size() != visibleValues.size() ){
        	changed = true;
        } else {
        	for (String value : selectedValues) {
        		if (!visibleValues.contains(value)) {
        			changed = true;
        			break;
        		}
        	}
        }              
        
        if (changed) {
        	if (visibleValues.isEmpty()) {
            	hiddenTiers.clear();
            	//System.err.println("updateTiersFromPane: Cleared hiddenTiers");
            }
        	// Copy selectedValues to visibleValues
        	visibleValues.clear();
        	visibleValues.addAll(selectedValues);
        	
        	visibleTierNames.clear();
         	
            for (String tierName : allTierNames) {
            	TierImpl tier = (TierImpl) trans.getTierWithId(tierName);            	
            	String value = getter.getSortValue(tier);            	
            	if (value.isEmpty()) {
            		value = NOT_SPECIFIED;
            	}
            	if (visibleValues.contains(value)) {
            		visibleTierNames.add(tierName);
            	}
            } 
            
            //System.err.printf("updateTiersFromPane: removing hidden tiers\n");
            //System.err.printf("%s - %s => ", visibleTierNames.toString(), hiddenTiers.toString());
            visibleTierNames.removeAll(hiddenTiers);
            //System.err.printf("%s\n", visibleTierNames.toString());
            
            // Clear all other lists (except visibleTierNames)
            List<?> allLists[] = { visibleTypeNames, visibleParts, visibleAnns, visibleLanguages };
            
            for (List<?> l : allLists) {
            	if (l != visibleValues) {
            		l.clear();
            	}
            }
        }
        return changed;
    }
    
    /**
     * Check or uncheck all tier checkboxes.
     *
     * @param selected if true select all checkboxes, unselect all
     *        otherwise
     */
    private void setAllSelected(boolean selected) {
    	int index = showTabPane.getSelectedIndex();
    	JPanel checkboxPanel = getCheckboxPanel(index);
    	
        Component[] boxes = checkboxPanel.getComponents();

        for (Component comp : boxes) {
            if (comp instanceof JCheckBox) {
                ((JCheckBox) comp).setSelected(selected);
            }
        }
    }
    
    /**
     * Disposes this dialog.
     */
    private void close() {
        setVisible(false);
        dispose();
    }
    
    public boolean isValueChanged(){
    	return valueChanged;
    }

    /**
     * Actions following a button click.
     *
     * @param ae the Action Event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource(); 
        if (source == showAllButton) {
            setAllSelected(true);
        } else if (source == hideAllButton) {
            setAllSelected(false);
        } else if (source == applyButton) { 
        	valueChanged = true;
        	updateChanges(showTabPane.getSelectedIndex());           
            close();
        } else if (source == cancelButton) {
        	valueChanged = false;
            close();
        }else if(source == sortButton){        	
        	int index = showTabPane.getSelectedIndex();
        	updateChanges(index);
        	sortAlphabetically(index);
        } else if(source == sortDefaultButton){        
        	int index = showTabPane.getSelectedIndex();
        	updateChanges(index);
        	sortInDefaultOrder(index);
        }
    }
    
    private void sortInDefaultOrder(int index){
    	String tabName = showTabPane.getTitleAt(index);        	
    	if(tabName.equals(SHOW_TYPES)){  
    		updateTab(index, allLinTypeNames);
    	} else if(tabName.equals(SHOW_TIERS)){
    		updateTab(index, allTierNames);
    	} else if(tabName.equals(SHOW_PART)){
    		updateTab(index, allParticipants);
    	} else if(tabName.equals(SHOW_ANN)){  
    		updateTab(index, allAnnotators);
    	} else if(tabName.equals(SHOW_LANG)){  
    		updateTab(index, allLanguages);
    	}         	
    }
    
    private void sortAlphabetically(int index){    	
    	String tabName = showTabPane.getTitleAt(index);  
    	String[] array = null;
    	if(tabName.equals(SHOW_TYPES)){        		
    		array = allLinTypeNames.toArray(new String[allLinTypeNames.size()]);
    	} else if(tabName.equals(SHOW_TIERS)){
    		array = allTierNames.toArray(new String[allTierNames.size()]);
    	} else if(tabName.equals(SHOW_PART)){
    		array = allParticipants.toArray(new String[allParticipants.size()]);
    	} else if(tabName.equals(SHOW_ANN)){        		
    		array = allAnnotators.toArray(new String[allAnnotators.size()]);
    	} else if(tabName.equals(SHOW_LANG)){        		
    		array = allLanguages.toArray(new String[allLanguages.size()]);
    	} else {
    		return;
    	}

    	Arrays.sort(array);
		List<String> sortedValues = new ArrayList<String>();
		for (String s : array) {
			sortedValues.add(s);
		}  
		updateTab(index, sortedValues);
    }
    
    /**
     * Propagate changes from a non-Tier tab to the Tier tab.
     * Called when leaving a tab.
     * @param index
     */
    private void updateChanges(int index){
    	String tabName = showTabPane.getTitleAt(index);
    	
    	if(tabName.equals(SHOW_TYPES)){
			updateTiersFromOtherPane(index, visibleTypeNames, new LinguisticTypeNameGetter());
    	} else if(tabName.equals(SHOW_TIERS)){
    		updateTiers();
    	} else if(tabName.equals(SHOW_PART)){
			updateTiersFromOtherPane(index, visibleParts, new ParticipantGetter());
    	} else if(tabName.equals(SHOW_ANN)){
			updateTiersFromOtherPane(index, visibleAnns, new AnnotatorGetter());
    	} else if(tabName.equals(SHOW_LANG)){
			updateTiersFromOtherPane(index, visibleLanguages, new LanguageGetter());
    	} 
    }
    
    /**
     * Re-initialize the checkboxes from the visibleParts (etc) arrays.
     * Called when entering a tab.
     * @param index
     */
    private void updateTabAtIndex(int index){
    	String tabName = showTabPane.getTitleAt(index);
    	List<String> visible;
    	
    	if(tabName.equals(SHOW_TYPES)){
    		visible = visibleTypeNames;
    	} else if(tabName.equals(SHOW_TIERS)){
			visible = visibleTierNames;
    	} else if(tabName.equals(SHOW_PART)){
    		visible = visibleParts;
    	} else if(tabName.equals(SHOW_ANN)){
			visible = visibleAnns;
    	} else if(tabName.equals(SHOW_LANG)){
			visible = visibleLanguages;
    	} else {
    		return;
    	}
	   setCheckboxes(index, visible);
    }

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getSource() == showTabPane) {
			//int index = showTabPane.getSelectedIndex();
			updateChanges(currentTabIndex);
		}	
	}

	@Override
	public void mouseReleased(MouseEvent e) {	
		if (e.getSource() == showTabPane) {
			currentTabIndex = showTabPane.getSelectedIndex();
			updateTabAtIndex(currentTabIndex);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
	}
}

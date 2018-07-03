package mpi.eudico.client.annotator.dcr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.border.TitledBorder;

import mpi.dcr.DCRConnectorException;
import mpi.dcr.DCSmall;
import mpi.dcr.ILATDCRConnector;
import mpi.eudico.client.annotator.ElanLocale;

/**
 * Panel to add or remove a data category reference
 * on an annotation
 * 
 * @author aarsom
 *
 */
public class AnnotationDCPanel2 extends LocalDCSPanel {
   
	/** the tier key label */
    protected JLabel tierKeyLabel;
    
    /** the tier key label */
    protected JTextArea tierValueArea;
    
    /** the tier key label */
    protected JLabel annKeyLabel;
    
    /** the tier key label */
    protected JTextArea annValueArea;
    
    /** the tier panel */
    protected JPanel tierPanel;
    
    /** the dcr panel */
    protected JPanel dcrPanel;

    /** the identifier key label */
    protected JLabel identifierKeyLabel;
    
    /** the identifier value area */
    protected JTextArea identifierValueArea;
    
    /** the id key label */
    protected JLabel idKeyLabel;
    
    /** the id value area */
    protected JTextArea idValueArea;
    
    /** the dcr label */
    protected JLabel profileKeyLabel;
    
    /** the profile value area */
    protected JTextArea profileValueArea;

    /** the delete ref button */
    protected JButton deleteButton; 

    /**
     * Creates a new AnnotationDCPanel instance
     *
     * @param connector the connector
     */
    public AnnotationDCPanel2(ILATDCRConnector connector) {
        super(connector);
    }

    /**
     * Creates a new AnnotationDCPanel instance
     *
     * @param connector the connector
     * @param resBundle the resource bundle
     */
    public AnnotationDCPanel2(ILATDCRConnector connector,
        ResourceBundle resBundle) {
        super(connector, resBundle);
    }

    /**
     * Updates the text fields with information for the data category with
     * the specified id.
     *
     * @param tierName name of the tier
     * @param value value of the annotation 
     * @param dcId the id of the category
     */
    public void setAnnotation(String tierName, String value, String dcId) {
    	tierValueArea.setText(tierName);
    	annValueArea.setText(value);
    	
        // retrieve the identifier string
        DCSmall sm = ELANLocalDCRConnector.getInstance().getDCSmall(dcId);
        
        if(sm != null){
            DCSmall dc;

            // select the dc in the table
            for(int i = 0 ; i < catTable.getRowCount(); i++ ){
                dc = (DCSmall) catTable.getValueAt(i, 0);
                if(dc.getId().equals(sm.getId())){
                    catTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        } else {
             updateDescriptionPanel(sm);
        }
        
        if(sm == null && dcId != null){
        	idValueArea.setText(dcId);
        }
    }
    
    /**
	 * Scrolls the current editing row to
	 * the center of the table if needed
	 * 
	 */
	public void scrollIfNeededAutomatically(int row){	
		JViewport viewport = (JViewport) catTable.getParent();
		Rectangle rect = catTable.getCellRect(row, 0, true);		   
		Rectangle viewRect = viewport.getViewRect();
		rect.setLocation(rect.x-viewRect.x, rect.y-viewRect.y);
		viewport.scrollRectToVisible(rect);
	}   		

    /**
     * Returns the current data category id.
     *
     * @return the current category id
     */
    public String getAnnotationDCId() {
        if ((idValueArea.getText() != null) &&
                (idValueArea.getText().length() > 0)) {
            return idValueArea.getText().trim();
        }

        return null;
    }
    
    /**
     * Override the initialization of the description panel
     * Adds new components to show data category reference
     * on the annotation 
     * 
     */
    @Override
	protected void initializeDescriptionPanel(){
    	super.initializeDescriptionPanel();
    	
    	descPanel.removeAll();
    	
    	identifierKeyLabel = new JLabel();
    	
    	identifierValueArea = new JTextArea();
    	identifierValueArea.setEditable(false);
    	identifierValueArea.setLineWrap(true);
    	identifierValueArea.setWrapStyleWord(true);
    	
    	idKeyLabel = new JLabel();
    	
    	idValueArea = new JTextArea();
    	idValueArea.setEditable(false);
    	idValueArea.setLineWrap(true);
    	idValueArea.setWrapStyleWord(true);
    	
    	profileKeyLabel = new JLabel();
    	
    	profileValueArea = new JTextArea();
    	profileValueArea.setEditable(false);
    	profileValueArea.setLineWrap(true);
    	profileValueArea.setWrapStyleWord(true);
    	
    	deleteButton = new JButton();
    	deleteButton.addActionListener(this);
    	
        GridBagConstraints gbc = new GridBagConstraints();
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	gbc.fill = GridBagConstraints.NONE;
    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.insets = globalInsets;
    	descPanel.add(langLabel, gbc);
           
    	gbc.gridx = 1;
    	gbc.insets = spacerInsets;
    	descPanel.add(langCombo,gbc);
    	
    	gbc.gridx = 0;
    	gbc.gridy = gbc.gridy+1;
    	gbc.insets = globalInsets;
    	descPanel.add(identifierKeyLabel,gbc);
    	
    	gbc.gridx = 1;
    	gbc.insets = spacerInsets;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx =1.0;
    	descPanel.add(identifierValueArea,gbc);
    	
    	gbc.gridx = 0;
    	gbc.gridy = gbc.gridy+1;
    	gbc.insets = globalInsets;
    	gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
    	descPanel.add(idKeyLabel,gbc);
    	
    	gbc.gridx = 1;
    	gbc.insets = spacerInsets;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx =1.0;
    	descPanel.add(idValueArea,gbc);
    	
    	gbc.gridx = 0;
    	gbc.gridy = gbc.gridy+1;
    	gbc.insets = globalInsets;
    	gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
    	descPanel.add(profileKeyLabel,gbc);
    	
    	gbc.gridx = 1;
    	gbc.insets = spacerInsets;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx =1.0;
    	descPanel.add(profileValueArea,gbc);
    	
    	gbc.gridx = 0;
    	gbc.gridy = gbc.gridy+1;
    	gbc.insets = globalInsets;
    	gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
    	descPanel.add(nameKeyLabel,gbc);
          
    	gbc.gridx = 1;
    	gbc.insets = spacerInsets;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx =1.0;
    	descPanel.add(nameValueLabel,gbc);

    	gbc.gridx = 0;
    	gbc.gridwidth = 2;
    	gbc.gridy = gbc.gridy+1;
    	gbc.insets = globalInsets;
    	gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
    	descPanel.add(descLabel,gbc);
            
    	gbc.gridy = gbc.gridy+1;
    	gbc.insets = globalInsets;
    	gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx =1.0;
        gbc.weighty = 1.0;
        descPanel.add(descScroll,gbc);
    }
    	

    /**
     * Initializes ui components.
     */
    @Override
	protected void initComponents() {  
        tierKeyLabel = new JLabel();
        
    	tierValueArea = new JTextArea();
    	tierValueArea.setEditable(false);
    	
    	annKeyLabel = new JLabel();
    	
    	annValueArea = new JTextArea();
    	annValueArea.setEditable(false);
    	annValueArea.setLineWrap(true);
    	annValueArea.setWrapStyleWord(true);
    	    	
    	tierPanel = new JPanel(new GridBagLayout());
    	
    	dcrPanel = new JPanel(new GridBagLayout());
    	
    	super.initComponents();
    	
    	setSingleSelection(true);
    	
    	remove(catPanel);
        remove(descPanel);
        
    	// tierPanel
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.insets = globalInsets;
    	gbc.fill = GridBagConstraints.NONE;
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	tierPanel.add(tierKeyLabel,gbc);
        
        gbc.gridx = 1;
    	tierPanel.add(tierValueArea,gbc);
        
        gbc.gridx = 0;
    	gbc.gridy = gbc.gridy+1;
    	tierPanel.add(annKeyLabel,gbc);
        
        gbc.gridx = 1;
    	gbc.weightx = 1.0;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	tierPanel.add(annValueArea,gbc);
    	
    	//dcrPanel
    	gbc = new GridBagConstraints();
    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.insets = globalInsets;
    	gbc.weightx = 1.0;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	dcrPanel.add(tierPanel,gbc);
    	
    	gbc.gridy = gbc.gridy +1;
    	gbc.weighty = 1.0;
    	gbc.fill = GridBagConstraints.BOTH;
    	dcrPanel.add(descPanel,gbc);
    	
    	gbc.gridy = gbc.gridy+1;
    	gbc.fill = GridBagConstraints.NONE;
    	gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        dcrPanel.add(deleteButton,gbc);
    	
    	setLayout(new GridBagLayout());
    	
    	gbc = new GridBagConstraints();
    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	//gbc.weightx = 1.0;
        gbc.weighty = 1.0;        
        gbc.insets = globalInsets;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	add(dcrPanel,gbc);
        
        gbc.gridx = 1;
    	gbc.weightx = 1.0;
//        gbc.weighty = 1.0;
    	//gbc.fill = GridBagConstraints.VERTICAL;
        gbc.fill = GridBagConstraints.BOTH;
        add(catPanel,gbc);
    }

    /**
     * @see mpi.dcr.LocalDCSelectPanel#updateLocale()
     */
    @Override
	protected void updateLocale() {
        super.updateLocale();
        String isocat = "ISO data category";
        String ident = "Identifier";
        String id = "Id";
        String profs = "Profiles";
        String tierName = "Tier Name";
        String ann = "Annotation";
        String remove ="Remove Reference";
        String remCat = "Remove Categories";
        
        if (resBundle != null) {
        	try{
        		isocat = resBundle.getString(ElanLocale.getString("DCR.Label.ISOCategory"));
        	}catch (Exception ex) {
            }
            try {
                ident = resBundle.getString("DCR.Label.Identifier");
            } catch (Exception ex) {
            }

            try {
                id = resBundle.getString("DCR.Label.Id");
            } catch (Exception ex) {
            }

            try {
                profs = resBundle.getString("DCR.Label.Profiles");
            } catch (Exception ex) {
            }
            
            try {
                tierName = resBundle.getString("EditTierDialog.Label.TierName");
            } catch (Exception ex) {
            }
            
            try {
                ann = resBundle.getString("Frame.GridFrame.ColumnAnnotation");
            } catch (Exception ex) {
            }
            
            try {
                remove = resBundle.getString("DCR.Label.RemoveReference");
            } catch (Exception ex) {
            }
            
            try {
                remCat = resBundle.getString("DCR.Label.RemoveCategory");
            } catch (Exception ex) {
            }
        }
        
        identifierKeyLabel.setText(ident);
        idKeyLabel.setText(id);
        profileKeyLabel.setText(profs);
        dcrPanel.setBorder(new TitledBorder(isocat));
        
        tierKeyLabel.setText(tierName);
        annKeyLabel.setText(ann);

        deleteButton.setText(remove);
        removeCatsButton.setText(remCat);
    }

    /**
     * Updates the description panel with the given dc
     * 
     * @dc, values to be updated in the description panel
     */
    @Override
	protected void updateDescriptionPanel(DCSmall dc) {
    	super.updateDescriptionPanel(dc);
    	
    	if (dc != null) {
			identifierValueArea.setText(dc.getIdentifier());
			idValueArea.setText(dc.getId());
			
			if (dc.getProfiles().length == 1) {
                profileValueArea.setText(dc.getProfiles()[0].getName());
            } else {
            	StringBuilder buf = new StringBuilder();

                for (int i = 0; i < dc.getProfiles().length; i++) {
                    buf.append(dc.getProfiles()[i].getName());

                    if (i != (dc.getProfiles().length - 1)) {
                        buf.append(", ");
                    }
                }

                profileValueArea.setText(buf.toString());
            }    			
            return;
        }
    	
    	identifierValueArea.setText(EMPTY);
        idValueArea.setText(EMPTY);
        profileValueArea.setText(EMPTY);
    }
    
    /**
     * Refresh the current data category
     */
    @Override
	protected void refreshCategory(){    	
    	DCSmall dc = null;
    	
    	this.setCursor(BUSY_CURSOR);
    	if (catTable.getSelectedRow() > -1){
    		super.refreshCategory();
    	} else {
    		// if data category not available in the cache
    		if(idValueArea.getText().trim().length() > 0){ 
    			try {
    				dc = connector.getDataCategory(idValueArea.getText().trim());
    			} catch (DCRConnectorException e1) {
    				// TODO Auto-generated catch block
    				e1.printStackTrace();
    			}
        		
        		if(dc == null){
        			// add to table
        			dc = getDC(idValueArea.getText().trim());
        			if (dc != null) {
	        			ArrayList<DCSmall> list = new ArrayList<DCSmall>();
	        			list.add(dc);
	        			addCategories(list);
	        			catTable.setRowSelectionInterval(catTable.getRowCount()-1, catTable.getRowCount()-1);
        			}
        		} 
    		}
    	}
    	this.setCursor(DEFAULT_CURSOR);
    }
   
    
    /**
     * @see mpi.dcr.LocalDCSelectPanel#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == deleteButton) {
        	updateDescriptionPanel(null);
        } else {
        	super.actionPerformed(e);
        }
    }
}

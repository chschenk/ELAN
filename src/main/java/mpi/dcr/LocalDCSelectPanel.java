package mpi.dcr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import mpi.dcr.isocat.RestDCRConnector;


/**
 * A panel with user interface elements to interact with a locally stored  data
 * category selection.
 *
 * @author Han Sloetjes
 * @version 1.0
 * 
 * @author aarsom
 * @version 2.0 the JList is replaced with a JTable and the values are multilingual
 */
@SuppressWarnings("serial")
public class LocalDCSelectPanel extends AbstractDCSelectPanel2 {
    
	/** refresh button*/
    protected JButton refreshButton;
	
	/** refresf categories button */
    protected JButton refreshCatsButton;
	
	/** add categories button */
    protected JButton addCatsButton;

    /** remove categories button */
    protected JButton removeCatsButton;
    
    protected JPanel buttonPanel;
    
    protected List<DCSmall> allDatCats;
    
    protected final Logger LOG = Logger.getLogger("DCR");
   

    /**
     * Creates a new LocalDCSelectPanel instance
     *
     * @param connector the (local) connector to use
     */
    public LocalDCSelectPanel(ILATDCRConnector connector) {
        super(connector);

        if (connector instanceof LocalDCRConnector) {
            this.connector = (LocalDCRConnector) connector;
        }

        initialize();
    }

    /**
     * Creates a new LocalDCSelectPanel instance
     *
     * @param connector the connector
     * @param resBundle a resource bundle with localized strings
     */
    public LocalDCSelectPanel(ILATDCRConnector connector,
        ResourceBundle resBundle) {
        super(connector, resBundle);

        if (connector instanceof LocalDCRConnector) {
            this.connector = (LocalDCRConnector) connector;
        }

        initialize();
    }

    /**
     * Creates a new LocalDCSelectPanel instance
     */
    public LocalDCSelectPanel() {
        super();
        initialize();
    }

    /**
     * Initializes ui components.
     */
    @Override
	protected void initComponents() {
        try {
            allDatCats = connector.getDCSmallList(null, null);

            if (allDatCats == null) {
                allDatCats = new ArrayList<DCSmall>();
            }
        } catch (DCRConnectorException dce) {
            allDatCats = new ArrayList<DCSmall>();
        }
        
        setLayout(new GridBagLayout());

        // categories panel
        refreshCatsButton = new JButton();
        addCatsButton = new JButton();
        removeCatsButton = new JButton();
        refreshButton = new JButton();
        
        ImageIcon refreshIcon = null; 
       
        try {
         	refreshIcon = new ImageIcon(this.getClass()
                    .getResource("/toolbarButtonGraphics/general/Refresh16.gif"));
        } catch (Exception ex) {}
        
        if(refreshIcon != null){
        	refreshButton.setIcon(refreshIcon);
        	refreshCatsButton.setIcon(refreshIcon);
        }
      
        refreshButton.addActionListener(this);
        refreshCatsButton.addActionListener(this);
        addCatsButton.addActionListener(this);
        removeCatsButton.addActionListener(this);

        buttonPanel = new JPanel(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = super.globalInsets;
        gbc.gridx = 0;
        gbc.gridy = 0;
       // gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        buttonPanel.add(refreshCatsButton, gbc);
        
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 1;
        buttonPanel.add(addCatsButton, gbc);
        
        gbc.gridx = 2;
        buttonPanel.add(removeCatsButton, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy = GridBagConstraints.REMAINDER;
    	gbc.insets = globalInsets;
    	gbc.fill = GridBagConstraints.NONE;
    	gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.weightx =0.0;
        gbc.weighty = 0.0;
        gbc.gridwidth = 2;
        descPanel.add(refreshButton,gbc);
       
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = super.globalInsets;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        catPanel.add(buttonPanel, gbc);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = super.globalInsets;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(catPanel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weightx = 0.0;
        add(descPanel, gbc);

        updateLocale();
        
        updateCategories(allDatCats);
    }

    /**
     * Applies localized texts to elements, if provided.
     */
    @Override
	protected void updateLocale() {
        super.updateLocale();

        String selCat = "Select Category";
        String addCat = "Add Categories";
        String remCat = "Remove Categories";
        String refresh = "Refresh";
        String refreshAllToolTip = "refresh categories";
        String refreshToolTip = "refresh category";

        if (resBundle != null) {
        	try {
        		selCat = resBundle.getString("DCR.Label.SelectCategory");
        	} catch (Exception ex) {
        	}
            
        	try {
                addCat = resBundle.getString("DCR.Label.AddCategories");
            } catch (Exception ex) {
            }

            try {
                remCat = resBundle.getString("DCR.Label.RemoveCategories");
            } catch (Exception ex) {
            }
            
            try {
                refresh = resBundle.getString("DCR.Label.Refresh");
            } catch (Exception ex) {
            }
            
            try {
            	refreshAllToolTip = resBundle.getString("DCR.Label.RefreshAll.ToolTip	");
            } catch (Exception ex) {
            }
           
            
            try {
            	refreshToolTip = resBundle.getString("DCR.Label.Refresh.ToolTip	");
            } catch (Exception ex) {
            }
        }
        
        catPanel.setBorder(new TitledBorder(selCat));
        addCatsButton.setText(addCat);
        removeCatsButton.setText(remCat);
        refreshCatsButton.setToolTipText(refreshAllToolTip);        
        refreshButton.setToolTipText(refreshToolTip);
        
        if(refreshCatsButton.getIcon() == null){
        	refreshCatsButton.setText(refresh);
        	refreshButton.setText(refresh);
        }
    }  

    /**
     * Creates a dialog to connect to a remote DCR and adds the selected
     * categories to the cache.
     */
    protected void selectAndAddCategories() {
        //String dcrLoc = "http://lux12.mpi.nl/isocat/rpc/syntax";
        //String dcrName = "SYNTAX ISO12620";
        // accept the default
        /*
           LATDCRConnector remConnector = new LATDCRConnector();
           //remConnector.setDCRLocation(dcrLoc);
           //remConnector.setName(dcrName);
           RemoteDCSelectPanel dcsp = new RemoteDCSelectPanel(remConnector);
           //dcsp.addProfiles(pp.getSelectedProfileNames());
           JDialog catFrame = new JDialog();
           catFrame.setModal(true);
           catFrame.setContentPane(dcsp);
           catFrame.pack();
           catFrame.setVisible(true);// blocks
           List dcsToAdd = dcsp.getSelectedCategories();
           addCategories(dcsToAdd);
         */
    }

    /**
     * Adds the categories from the specified list to the local list.
     *
     * @param dcsToAdd the data categories to add
     */
    protected void addCategories(List<DCSmall> dcsToAdd) {
        if ((dcsToAdd != null) && (dcsToAdd.size() > 0)) {
            // add the new categories
            List<DCSmall> addedDcs = new ArrayList<DCSmall>(dcsToAdd.size());
            DCSmall small1 = null;
            DCSmall small2 = null;
            
dcloop: 
            for (int i = 0; i < dcsToAdd.size(); i++) {
                small1 = dcsToAdd.get(i);

                for (int j = 0; j < allDatCats.size(); j++) {
                    small2 = allDatCats.get(j);

                    if (small1.getIdentifier().equals(small2.getIdentifier())) {
                        // already in the list, replace
                    	allDatCats.set(j, small1);
                        continue dcloop;
                    }
                }

                // not in the list
                addedDcs.add(small1);
            }

            if (addedDcs.size() > 0) {
                allDatCats.addAll(addedDcs);
                updateCategories(allDatCats);
            }
            
            if (connector instanceof LocalDCRConnector) {
                try {
                    ((LocalDCRConnector) connector).addDataCategories(dcsToAdd);
                } catch (DCRConnectorException dce) {
                    // message
                }
            }
        }
    }

    private void removeCategories() {
    	List<DCSmall> remDatCats = new ArrayList<DCSmall>();
    	Object sel = null;

      	int[] rows =  catTable.getSelectedRows();
      	Arrays.sort(rows);
      	if(rows != null){
      		for(int i = rows.length-1; i >= 0; i--){
      			sel = catTable.getValueAt(rows[i], 0);;        			
      			if (sel instanceof DCSmall) {
      				allDatCats.remove(sel);
      				catModel.removeRow(rows[i]);
      				remDatCats.add((DCSmall)sel);
      			}
      		}
        }
        	
      	if (remDatCats.size() > 0 && connector instanceof LocalDCRConnector) {
      		try {
      			((LocalDCRConnector) connector).removeCategories(remDatCats);
      		} catch (DCRConnectorException dce) {
      			// message
      		}
      	}
    }

    /**
     * The action event handling.
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addCatsButton) {
            selectAndAddCategories();
        } else if (e.getSource() == removeCatsButton) {
            removeCategories();
        } else if(e.getSource() == refreshCatsButton) {
        	refreshCatergories();
        } else if(e.getSource() == refreshButton){
    		refreshCategory();
    	} else {
        	super.actionPerformed(e);
        }
    }
    
    /**
     * Get the data category from the registry
     * 
     * @param dcID, the id of the Data category
     * @return the data category from the registry
     */
    protected DCSmall getDC(String dcID){
    	if (dcID == null || "-".equals(dcID)) {
    		return null;
    	}
    	if(connector instanceof RestDCRConnector){
			remoteConnector = (RestDCRConnector) connector;
		} else if( remoteConnector == null){
			remoteConnector = new RestDCRConnector();
		}
    	DCSmall dc = null;
		try {
			dc = remoteConnector.getDataCategory(dcID);
			
		} catch (DCRConnectorException dce) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.info("DCE: dcID=" + dcID + ": " + dce.getMessage());
			}
			//dce.printStackTrace();
		}
		
		return dc;
    }
    
    /**
     * Refresh the current data category
     */
    protected void refreshCategory(){
    	DCSmall dc;
    	
    	this.setCursor(BUSY_CURSOR);
    	if (catTable.getSelectedRows().length == 1 &&
    			catTable.getSelectedRow() > -1) {
    		dc = (DCSmall) catTable.getValueAt(catTable.getSelectedRow(), 0);
    		dc = getDC(dc.getId());
    		catTable.setValueAt(dc, catTable.getSelectedRow(), 0);
    		
    		replaceDC(dc);
    	}
    	
    	updateDescriptionPanel();
    	
    	this.setCursor(DEFAULT_CURSOR);
    }
    
    
    /**
     * Replaces the given data category in the list
     */
    @Override
	protected void replaceDC(DCSmall dc){
    	DCSmall small2;
		for (int j = 0; j < allDatCats.size(); j++) {
            small2 = allDatCats.get(j);

            if (dc.getIdentifier().equals(small2.getIdentifier())) {
                // already in the list, replace
            	allDatCats.set(j, dc);break;
            }
        }
		
		super.replaceDC(dc);
    }
    
    /**
     * Refresh all data categories in the table
     */
    private void refreshCatergories(){
    	List<DCSmall> dcList = new ArrayList<DCSmall>();
    	DCSmall dc;
    	
    	this.setCursor(BUSY_CURSOR);
    	for(int i= 0 ; i < catTable.getRowCount(); i++){
    		dc = (DCSmall) catTable.getValueAt(i, 0);
    		dc = getDC(dc.getId());
    		if(dc != null){
    			dcList.add(dc);
    			catTable.setValueAt(dc, i, 0);
    		}
    	}
    	
    	if(dcList.size() > 0){
    		allDatCats = dcList;
    		
    		if(connector instanceof LocalDCRConnector){
    			try {
    				((LocalDCRConnector) connector).addDataCategories(dcList);
    			} catch (DCRConnectorException e) {
    				if (LOG.isLoggable(Level.INFO)) {
    					LOG.info("DCE: allDC's: " + e.getMessage());
    				}
    				//e.printStackTrace();
    			}
    		}
    	}
    	
    	updateDescriptionPanel();
    	
    	this.setCursor(DEFAULT_CURSOR);
    }
}

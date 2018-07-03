package mpi.dcr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import mpi.dcr.isocat.Profile;
import mpi.dcr.isocat.RestDCRConnector;
import mpi.eudico.util.EmptyStringComparator;


/**
 * A panel with table of selected data categories and a sub panel
 *  with description details of the selected data category.
 *
 * @author aarsom
 * @version 1.0
 */
@SuppressWarnings("serial")
public abstract class AbstractDCSelectPanel2 extends JPanel 
	implements ActionListener, ListSelectionListener{
    /** the DCR connector */
    protected ILATDCRConnector connector = null;
    
    protected RestDCRConnector remoteConnector;

    /** the data categories panel */
    protected JPanel catPanel;

    /** the data categories table */
    protected JTable catTable;
    
    protected JScrollPane catTableScroll;

    /** the data categories list model */
    protected DefaultTableModel catModel;
    
    /** the language label */
    protected JLabel langLabel;
    
    /** the language combo box */
    protected JComboBox langCombo;
        
    /** data category description panel */
    protected JPanel descPanel;
    
    /** the label for the description of the data category*/
    protected JLabel descLabel;

    /** text area for description of a data category */
    protected JTextArea descArea;

    /** scroll pane for text area for description of a data category */
    protected JScrollPane descScroll;
 
    /** the label for the name key of the data category*/
    protected JLabel nameKeyLabel;
    
    /** the label for the name of the data category*/
    protected JTextArea nameValueLabel;

    /** the empty string */
    protected final String EMPTY = "-";

    /** a resource bundle for localisation */
    protected ResourceBundle resBundle;
    
    protected Insets globalInsets = new Insets(2, 6, 2, 6);
    protected Insets spacerInsets = new Insets(2, 6, 10, 6);
    
    protected Dimension dim = new Dimension(220, 300);
    
    public final Cursor BUSY_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
   	public final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
   	
   	protected String currentLanguage;
   	
    /**
     * Creates a new AbstractDCSelectPanel instance
     *
     * @param connector the connector to connect to the dcr
     */
    public AbstractDCSelectPanel2(ILATDCRConnector connector) {
        super();
        this.connector = connector;
    }

    /**
     * Creates a new AbstractDCSelectPanel instance
     *
     * @param connector the connector to connect to the dcr
     * @param resBundle the resource bundle
     */
    public AbstractDCSelectPanel2(ILATDCRConnector connector,
        ResourceBundle resBundle) {
        super();
        this.connector = connector;
        this.resBundle = resBundle;
    }

    /**
     * Creates a new AbstractDCSelectPanel instance
     */
    public AbstractDCSelectPanel2() {
        super();
    }
    
    protected void initialize(){
    	initializeDCPanel();
    	initializeDescriptionPanel();
    	
    	initComponents();
    }

    /**
     * Initializes the ui components
     */
    protected abstract void initComponents();
    
    protected void initializeDescriptionPanel(){
    	//description panel
    	langLabel = new JLabel();
            
    	langCombo = new JComboBox();
    	langCombo.addItem(EMPTY);
            
    	nameKeyLabel = new JLabel();
//    	nameValueLabel = new JLabel();
//    	nameValueLabel.setText(EMPTY);
            
    	nameValueLabel = new JTextArea();
    	nameValueLabel.setEditable(false);
    	nameValueLabel.setLineWrap(true);
    	nameValueLabel.setWrapStyleWord(true);
    	nameValueLabel.setText(EMPTY);
    	
    	descLabel = new JLabel();
    	     	 
    	descArea = new JTextArea(6,30);
    	descArea.setEditable(false);
    	descArea.setLineWrap(true);
    	descArea.setWrapStyleWord(true);
        descPanel = new JPanel(new GridBagLayout());
        
        descScroll = new JScrollPane(descArea);
        descScroll.setMinimumSize(descArea.getPreferredScrollableViewportSize());
        descScroll.setMaximumSize(descArea.getPreferredScrollableViewportSize());
        descScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    	
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
//    	gbc.gridwidth = 2;
    	gbc.insets = globalInsets;
    	descPanel.add(nameKeyLabel,gbc);
          
    	gbc.gridx = 1;
    	//gbc.gridy = gbc.gridy+1;
    	gbc.insets = spacerInsets;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx =1.0;
        gbc.weighty = 0.0;
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
       
        descPanel.setMinimumSize(dim);
        descPanel.setPreferredSize(dim);
    }
    
    /**
     * Initializes and loads the category panel
     * 
     * @return JPanel, catPanel
     */
    protected void initializeDCPanel(){
    	//catPanel   
    	catModel = new DefaultTableModel(){
    		@Override
			public boolean isCellEditable(int row, int column) {    
    			return false;
    		}	
    	};
    	catModel.setColumnIdentifiers(new Object[]{"DC- IDENTIFIER", "ID", "PROFILES"});
         
    	DCTableCellRenderer catTableRenderer = new DCTableCellRenderer();
    	
    	catTable = new JTable(catModel); 
    	catTable.setDefaultRenderer(Object.class, catTableRenderer);
    	catTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    	catTable.getSelectionModel().addListSelectionListener(this);	
    	
    	TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(catModel);
        EmptyStringComparator emptyComp = new EmptyStringComparator();
        
        rowSorter.setComparator(1, null);
        rowSorter.setComparator(2, emptyComp);
        catTable.setRowSorter(rowSorter);
    	
    	catTableScroll = new JScrollPane(catTable);
    	
    	catPanel = new JPanel(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = globalInsets;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        catPanel.add(catTableScroll, gbc);
    }
    
    protected void setSingleSelection(boolean bool){
    	catTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    }
    
    public void setPreferredLanguage(String lang){
    	currentLanguage = lang; 
    }

    /**
     * Applies the texts of the ui elements.
     */
    protected void updateLocale() {
       
        String descpanel = "Category Description";
        String name = "Name";
        String lang = "Language";
        String desc = "Description";

        if (resBundle != null) {
            try {
            	descpanel = resBundle.getString("DCR.Label.CategoryDescription");
            } catch (Exception ex) {
            }
            
            try {
            	name = resBundle.getString("DCR.Label.Name");
            } catch (Exception ex) {
            }

            try {
                lang = resBundle.getString("DCR.Label.Language");
            } catch (Exception ex) {
            }
        }
        
        descPanel.setBorder(new TitledBorder(descpanel));
        descLabel.setText(desc);
        nameKeyLabel.setText(name);
        langLabel.setText(lang);
    }

    /**
     * Returns the selected categories.
     *
     * @return the selected categories
     */
    public List<DCSmall> getSelectedCategories() {
        List<DCSmall> selected = new ArrayList<DCSmall>();

       int[] vals = catTable.getSelectedRows();

       for (int val : vals) {
    	   selected.add((DCSmall)catTable.getValueAt(val, 0));
       }  

       return selected;
    }

    /**
     * Adds the data categories in the specified list to the list of selected
     * categories.
     *
     * @param datcats a list containing data category representations
     */
    protected void updateCategories(List<DCSmall> datcats) {
    	if (datcats == null) {
    		//return; // or clear the list??
    		datcats = new ArrayList<DCSmall>(0);
    	}

    	descArea.setText("");
    	while(catModel.getRowCount() > 0){
    		catModel.removeRow(0);
    	}

    	DCSmall dc;
    	for (int i = 0; i < datcats.size(); i++) {
    		dc = datcats.get(i);
    		String prof = null;
    		final Profile[] profiles = dc.getProfiles();
    		if (profiles.length > 0) {
    			prof = profiles[0].getName();
    			for (int p = 1; p < profiles.length; p++) {
    				prof += ", " + profiles[p].getName();
    			}
    		}
    		catModel.addRow(new Object[]{dc,dc.getId(), prof});
    	}
    }
    
    protected void updateDescription(){
    	DCSmall dc = (DCSmall) catTable.getValueAt(catTable.getSelectedRow(), 0);
    	
    	String lang = langCombo.getSelectedItem().toString();
    	nameValueLabel.setText(dc.getName(lang));
        descArea.setText(dc.getDesc(lang));
    }
    
    protected void replaceDC(DCSmall dc){
    	try {
			if(connector instanceof LocalDCRConnector){
				((LocalDCRConnector) connector).replaceDC(dc);
			}
		} catch (DCRConnectorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * Updates the information in the description panel.
     *
     * @param row the summary of the selected data category
     */
    protected void updateDescriptionPanel(DCSmall dc) {
    	if (dc != null ) {
			if(!dc.isLoaded()){
				if(connector instanceof RestDCRConnector){
					remoteConnector = (RestDCRConnector) connector;
				} else if( remoteConnector == null){
					remoteConnector = new RestDCRConnector();
				}
				
				try {
					dc = remoteConnector.getDataCategory(dc.getId());
					catTable.setValueAt(dc, catTable.getSelectedRow(), 0);
					replaceDC(dc);
				} catch (DCRConnectorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//String lang = (String) langCombo.getSelectedItem();
			langCombo.removeActionListener(this);
	        langCombo.removeAllItems();
	        
	        List<String> langList = dc.getLanguages();
	        for(int i=0; i < langList.size(); i++){
	        	langCombo.addItem(langList.get(i));
	        }
	        	        
//	        if(lang != null && langList.contains(lang)){
//	        	langCombo.setSelectedItem(lang);
//	        } else {
//	        	if(currentLanguage != null && langList.contains(currentLanguage)){
//	        		langCombo.setSelectedItem(currentLanguage);
//	        	} else{
//	        		langCombo.setSelectedItem(DCSmall.EN);
//	        	}
//	        }
	        
	        if(currentLanguage != null && langList.contains(currentLanguage)){
        		langCombo.setSelectedItem(currentLanguage);
        	} else{
        		langCombo.setSelectedItem(DCSmall.EN);
        	}
	        
	        langCombo.addActionListener(this);
			
	        updateDescription();
        } else {
        	if (catTable.getSelectedRows().length == 1){
        		catTable.getSelectionModel().clearSelection();
        	}
        	
        	langCombo.removeActionListener(this);
            langCombo.removeAllItems();
            langCombo.addItem(EMPTY);
            langCombo.addActionListener(this);
            	
            nameValueLabel.setText(EMPTY);
            descArea.setText("");
        }
    	
    	
    }
    
    /**
     * Updates the information in the description panel.
     *
     * @param row the summary of the selected data category
     */
    protected void updateDescriptionPanel() {
    	if (catTable.getSelectedRows().length == 1 &&
    			catTable.getSelectedRow() > -1) {
    		
    		DCSmall dc = (DCSmall) catTable.getValueAt(catTable.getSelectedRow(), 0);
            
    		if (dc != null ) {
    			if(!dc.isLoaded()){
    				
    				if(connector instanceof RestDCRConnector){
    					remoteConnector = (RestDCRConnector) connector;
    				} else if( remoteConnector == null){
    					remoteConnector = new RestDCRConnector();
    				}
    				
    				try {
						dc = remoteConnector.getDataCategory(dc.getId());
						catTable.setValueAt(dc, catTable.getSelectedRow(), 0);
						if(connector instanceof LocalDCRConnector){
	    					((LocalDCRConnector) connector).replaceDC(dc);
	    				}
					} catch (DCRConnectorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			}
            }
    		
    		updateDescriptionPanel(dc);    		
        } else {
        	updateDescriptionPanel(null);  
        }
    }


//    /**
//     * Updates the information in the description panel.
//     *
//     * @param row the summary of the selected data category
//     */
//    protected void updateDescriptionPanel() {
//    	if (catTable.getSelectedRows().length == 1 &&
//    			catTable.getSelectedRow() > -1) {
//    		
//    		DCSmall dc = (DCSmall) catTable.getValueAt(catTable.getSelectedRow(), 0);
//            
//    		if (dc != null ) {
//    			if(!dc.isLoaded()){
//    				
//    				if(connector instanceof RestDCRConnector){
//    					remoteConnector = (RestDCRConnector) connector;
//    				} else if( remoteConnector == null){
//    					remoteConnector = new RestDCRConnector();
//    				}
//    				
//    				try {
//						dc = remoteConnector.getDataCategory(dc.getId());
//						catTable.setValueAt(dc, catTable.getSelectedRow(), 0);
//						if(connector instanceof LocalDCRConnector){
//	    					((LocalDCRConnector) connector).replaceDC(dc);
//	    				}
//						
////						langCombo.removeActionListener(this);
////		    	        langCombo.removeAllItems();
////		    	        
////		    	        List<String> langList = dc.getLanguages();
////		    	        for(int i=0; i < langList.size(); i++){
////		    	        	langCombo.addItem(langList.get(i));
////		    	        }
////		    	        
////		    	        langCombo.setSelectedIndex(0);
////		    	        
////		    	        langCombo.addActionListener(this);
//					} catch (DCRConnectorException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//    			}
//    			
//    			langCombo.removeActionListener(this);
//    	        langCombo.removeAllItems();
//    	        
//    	        List<String> langList = dc.getLanguages();
//    	        for(int i=0; i < langList.size(); i++){
//    	        	langCombo.addItem(langList.get(i));
//    	        }
//    	        
//    	        langCombo.setSelectedIndex(0);
//    	        
//    	        langCombo.addActionListener(this);
//    			
//    	        updateDescription();
//                return;
//            }
//        }
//    	
//    	langCombo.removeActionListener(this);
//        langCombo.removeAllItems();
//        langCombo.addItem(EMPTY);
//        langCombo.addActionListener(this);
//        	
//        nameValueLabel.setText(EMPTY);
//        descArea.setText("");
//    }

    /**
     * Empty action event handling.
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	if(e.getSource() == langCombo && 
    			!langCombo.getSelectedItem().equals(EMPTY)){
    		updateDescription();
    	} 
    }
    
    /**
     * Handles changes in the category selection
     *
     * @param e list selection event
     */
    @Override
	public void valueChanged(ListSelectionEvent e) {
    	updateDescriptionPanel();
    }
    
    public class DCTableCellRenderer extends JTextArea implements TableCellRenderer {
        
    	HashMap<Integer, List<Integer>> heightMap;
    	
    	public DCTableCellRenderer() {
          setLineWrap(true);
          setWrapStyleWord(true);
          setEditable(false);
          
          heightMap = new HashMap<Integer, List<Integer>>();
        }

        @Override
		public Component getTableCellRendererComponent(JTable table, Object
              value, boolean isSelected, boolean hasFocus, int row, int column) {
         
        	setText(value.toString());
        	setSize(table.getColumnModel().getColumn(column).getWidth(),
                  getPreferredSize().height);
        	setToolTipText(getText());
          
        	setBackground(Color.WHITE);
        	setForeground(Color.BLACK);
          
        	// set selection
        	for(int i = 0; i < table.getSelectedRows().length; i++){
        		if(row == table.getSelectedRows()[i]){
        			setBackground(table.getSelectionBackground());
        			setForeground(table.getSelectionForeground());
        			break;
        		} 
        	}
        	
        	//resizing
        	List<Integer> heightList;
        	if(!heightMap.containsKey(row)){
        		heightList = new ArrayList<Integer>(3);
        		heightList.add(0);
        		heightList.add(0);
        		heightList.add(0);  
        		
        		heightMap.put(row, heightList);
        	}
        	
        	heightList = heightMap.get(row);
    		heightList.set(column, getPreferredSize().height);
        		
        	int height = 0;
        	for(int i= 0; i < 3; i++){
        		if(height < heightList.get(i)){
        			height = heightList.get(i);
        		}
        	}        	
        	table.setRowHeight(row, height);
        	
        	return this;
        }
    } 
 }

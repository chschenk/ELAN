package mpi.eudico.client.annotator.prefs.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.ColorDialog;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;
import mpi.eudico.client.util.ButtonCellEditor;
import mpi.eudico.client.util.ButtonTableCellRenderer;
import mpi.eudico.client.util.RadioButtonCellEditor;
import mpi.eudico.client.util.RadioButtonTableCellRenderer;
import mpi.eudico.client.util.SelectEnableObject;

/**
 * A panel for viewer related preferences.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
@SuppressWarnings("serial")
public class ViewerPanel extends AbstractEditPrefsPanel implements PreferenceEditor, MouseListener, ActionListener {
    private int origNumSubtitles = 4;
    private boolean origActiveAnnBold = false;
    private boolean origReducedTierHeight = false;
  
    private boolean videoInCentre = false; 
    
    private JComboBox numSubCB;
    private JCheckBox aaBoldCB;
    private JCheckBox redTierHeightCB;    
    
	private JCheckBox tierOrderInDropdownCB;
	private boolean origTierOrderInDropdownCB;
    
    private Color origSymAnnColor = Constants.SHAREDCOLOR1; 
    private Color symAnnColor = origSymAnnColor;
    private JPanel colorPreviewPanel; 
    private JButton colorButton;
    private JButton resetColorButton;
    private JLabel colorTextLabel;
    
    public ColorDialog dialog;
     
    private JButton downButton;
    private JButton upButton;   
	
    private JTable viewerTable; 
    private boolean sortOrderChanged = false;

    private final String  GRID_VIEWER = ElanLocale.getString(ELANCommandFactory.GRID_VIEWER);
    private final String  TEXT_VIEWER = ElanLocale.getString(ELANCommandFactory.TEXT_VIEWER);
    private final String  SUBTITLE_VIEWER = ElanLocale.getString(ELANCommandFactory.SUBTITLE_VIEWER);
    private final String  LEXICON_VIEWER = ElanLocale.getString(ELANCommandFactory.LEXICON_VIEWER);
    private final String  COMMENT_VIEWER = ElanLocale.getString(ELANCommandFactory.COMMENT_VIEWER);
    private final String  RECOGNIZER = ElanLocale.getString(ELANCommandFactory.RECOGNIZER);
    private final String  METADATA_VIEWER = ElanLocale.getString(ELANCommandFactory.METADATA_VIEWER);

    private final List<String> viewersList = new ArrayList<String>(
    		Arrays.asList(GRID_VIEWER, TEXT_VIEWER, SUBTITLE_VIEWER, LEXICON_VIEWER, COMMENT_VIEWER, RECOGNIZER, METADATA_VIEWER ));
    
    private List<String> viewerSortOrder;
    
    private JSpinner scrollSpeedSpinner;
    private int origScrollSpeed = 10;
    private final int MIN_SCROLL = 5;
    private final int MAX_SCROLL = 50;
    
    /**
     * Creates a new ViewerPanel instance
     */
    public ViewerPanel() {
        super(ElanLocale.getString("PreferencesDialog.Category.Viewer"));   
        readPrefs();
        initComponents(); 
    }
    
    /**
     * Reads stored preferences.
     *
     */
    private void readPrefs() {
    	
    	Boolean boolPref =  Preferences.getBool("Media.VideosCentre", null);
    		
    	if (boolPref != null) {
    		videoInCentre = boolPref.booleanValue();
    	}        
        
    	Integer intPref = Preferences.getInt("NumberOfSubtitleViewers", null);

    	if (intPref != null) {
    		origNumSubtitles = intPref.intValue();
    	}
        
    	boolPref = Preferences.getBool("TimeLineViewer.ActiveAnnotationBold", null);
    	if (boolPref instanceof Boolean) {
    		origActiveAnnBold = boolPref.booleanValue();
    	}
        
    	boolPref = Preferences.getBool("TimeLineViewer.ReducedTierHeight", null);
    	if (boolPref instanceof Boolean) {
    		origReducedTierHeight = boolPref.booleanValue();
    	}
    	
    	boolPref = Preferences.getBool("SingleTierViewer.TierOrderInDropdown", null);
    	if (boolPref instanceof Boolean) {
    		origTierOrderInDropdownCB = boolPref.booleanValue();
    	}
        
    	List<String> order = Preferences.getListOfString("PreferencesDialog.Viewer.SortOrder", null);
    	if (order != null) {
    		viewerSortOrder = order;
    	} else {
    		viewerSortOrder = viewersList;
    	}   
    	
    	Color colorPref = Preferences.getColor("Preferences.SymAnnColor", null);
    	if (colorPref != null) {
			origSymAnnColor = new Color (
				colorPref.getRed(),colorPref.getGreen(),colorPref.getBlue()) ;
    	}
    	
    	intPref = Preferences.getInt("Preferences.TimeLine.HorScrollSpeed", null);
    	if (intPref != null) {
    		origScrollSpeed = intPref;
    		if (origScrollSpeed < MIN_SCROLL) {
    			origScrollSpeed = MIN_SCROLL;
    		} else if (origScrollSpeed > MAX_SCROLL) {
    			origScrollSpeed = MAX_SCROLL;
    		}
    	}

    }
    
    
    /**
     * Reads stored viewer preferences.
     *
     */
    private void readViewerPref(){
    	for(int x=0; x< viewerTable.getRowCount(); x++){	
    		if( viewerTable.getModel().getValueAt(x, 0) instanceof SelectEnableObject){
    			SelectEnableObject<String> seo = (SelectEnableObject<String>) viewerTable.getModel().getValueAt(x, 1);
    			boolean bool = getPrefValue(seo.getValue());
    			seo.setSelected(bool);
    			((SelectEnableObject) viewerTable.getModel().getValueAt(x, 0)).setSelected(!bool);
    		}
    	}       
    }          
    
    /**
     * Gets the references value to store the preferences of the viewer
     * 
     * @param viewer, the viewer
     * @return string, the reference string
     */
    private String getRefValue(String viewer){
    	String val = null;
    	
    	if(viewer.equals(this.GRID_VIEWER)){
    		val = "PreferencesDialog.Viewer.Grid.Right";
    	}else if(viewer.equals(this.TEXT_VIEWER)){
    		val = "PreferencesDialog.Viewer.Text.Right";
    	} else if(viewer.equals(this.SUBTITLE_VIEWER)){
    		val = "PreferencesDialog.Viewer.Subtitle.Right";
    	} else if(viewer.equals(this.LEXICON_VIEWER)){
    		val = "PreferencesDialog.Viewer.Lexicon.Right";
    	} else if(viewer.equals(this.COMMENT_VIEWER)){
    		val = "PreferencesDialog.Viewer.Comment.Right";
    	} else if(viewer.equals(this.RECOGNIZER)){
    		val = "PreferencesDialog.Viewer.Audio.Right";
    	} else if(viewer.equals(this.METADATA_VIEWER)){
    		val = "PreferencesDialog.Viewer.MetaData.Right";
    	}
    	
    	return val;
    }
    
   /**
    * Gets the preference value of the given viewer
    * 
    * @param viewer the viewer for which the value is required
    * @return boolean if true, the given viewer is in the right pane of the video
    * 				if false , then it is on the left pane of the video
    */
    private boolean getPrefValue(String viewer){
    	
    	boolean bool = true;
    	
    	Boolean val = Preferences.getBool(getRefValue(viewer), null);
		if (val != null) {
			bool = val.booleanValue();  
		} 
		return bool;
    }

    /**
     * Initializes the ui components
     */
    private void initComponents() {
	    
	    Font plainFont;
	    GridBagConstraints gbc;
	    
	    //Subtitles Panel
	    JPanel subtitlePanel = new JPanel(new GridBagLayout());
	    JLabel numLabel = new JLabel(ElanLocale.getString(
                "PreferencesDialog.Viewer.NumSubtitles"));
	    plainFont = numLabel.getFont().deriveFont(Font.PLAIN);
	    numLabel.setFont(plainFont);
	    
	    numSubCB = new JComboBox(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8 });
	    numSubCB.setFont(plainFont);
        numSubCB.setSelectedItem(origNumSubtitles); 
	    
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = topInset;
        subtitlePanel.add(numLabel, gbc);
        
        gbc.gridx = 1;
        gbc.insets = leftInset;
        subtitlePanel.add(numSubCB, gbc);
        
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        subtitlePanel.add(new JPanel(), gbc); //filler        
        
        //Tier Order in Dropdown panel
        tierOrderInDropdownCB = new JCheckBox(ElanLocale.getString(
        		"PreferencesDialog.Viewer.OrderAlphabetically"));
        tierOrderInDropdownCB.setFont(tierOrderInDropdownCB.getFont().deriveFont(Font.PLAIN));
        tierOrderInDropdownCB.setSelected(origTierOrderInDropdownCB);
        
        
        //Timeline Panel        
        aaBoldCB = new JCheckBox(ElanLocale.getString("TimeLineViewer.ActiveAnnotationBold"));
        aaBoldCB.setFont(aaBoldCB.getFont().deriveFont(Font.PLAIN));
        aaBoldCB.setSelected(origActiveAnnBold);
        
        redTierHeightCB = new JCheckBox(ElanLocale.getString("TimeLineViewer.ReducedTierHeight"));
        redTierHeightCB.setFont(plainFont);
        redTierHeightCB.setSelected(origReducedTierHeight);       
        
        //Horizontal Speed
        SpinnerNumberModel spinModel = new SpinnerNumberModel(origScrollSpeed, 5, 50, 5);
        scrollSpeedSpinner = new JSpinner(spinModel);
        
        JLabel scrollLabel = new JLabel(ElanLocale.getString("PreferencesDialog.Viewer.HorizontalScrollSpeed"));
        scrollLabel.setFont(plainFont);
        
        JPanel scrollSpeedPanel = new JPanel(new GridBagLayout());
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST; 
        gbc.insets = topInset;
        scrollSpeedPanel.add(scrollLabel, gbc);
               
        gbc.gridx = 1;   
        gbc.insets = leftInset;
        scrollSpeedPanel.add(scrollSpeedSpinner, gbc);
        
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        scrollSpeedPanel.add(new JPanel(), gbc); //filler   
        
        //Color panel
        colorTextLabel = new JLabel(ElanLocale.getString(
                "PreferencesDialog.Viewer.ColorTextLabel"));
        colorTextLabel.setFont(plainFont);
        colorButton = new JButton(ElanLocale.getString("Button.Browse"));
        colorButton.addActionListener(this);
        resetColorButton = new JButton(ElanLocale.getString("Button.Default"));
        resetColorButton.addActionListener(this);

        colorPreviewPanel = new JPanel();
        colorPreviewPanel.setBorder(new LineBorder(Color.GRAY, 1));
        colorPreviewPanel.setPreferredSize(new Dimension(colorButton.getPreferredSize().height - 5, 
		colorButton.getPreferredSize().height - 5));
        colorPreviewPanel.setMinimumSize(new Dimension(colorButton.getPreferredSize().height - 5, 
		colorButton.getPreferredSize().height - 5));
        colorPreviewPanel.setBackground(origSymAnnColor);
        
        JPanel colorPanel = new JPanel(new GridBagLayout());
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = topInset;
        colorPanel.add(colorTextLabel, gbc);
               
        gbc.gridx = 1;   
        gbc.insets = leftInset;
        colorPanel.add(colorPreviewPanel, gbc);
        
        gbc.gridx = 2;
        colorPanel.add(colorButton, gbc);
        
        gbc.gridx = 3;
        colorPanel.add(resetColorButton, gbc);
        
        gbc.gridx = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;        		
        colorPanel.add(new JPanel(), gbc);
        
        // select viewers panel
        String columnHeader1 = ElanLocale.getString("PreferencesDialog.Viewer.ColumnHeader.LeftofVideo");
    	String columnHeader2 = ElanLocale.getString("PreferencesDialog.Viewer.ColumnHeader.RightofVideo");
    	String columnHeader3 = ElanLocale.getString("PreferencesDialog.Viewer.ColumnHeader.MoveUp");
    	String columnHeader4 = ElanLocale.getString("PreferencesDialog.Viewer.ColumnHeader.MoveDown");
    	
    	DefaultTableModel dm = new DefaultTableModel();
	    dm.setColumnIdentifiers(new String[] { columnHeader1, columnHeader2, columnHeader3, columnHeader4 });
	   
	    viewerTable = new JTable(dm) ;  
	    viewerTable.setFont(viewerTable.getFont().deriveFont(Font.PLAIN));
	    viewerTable.getColumn(columnHeader1).setCellEditor(new RadioButtonCellEditor(new JCheckBox()));               
	    viewerTable.getColumn(columnHeader1).setCellRenderer(new RadioButtonTableCellRenderer());	   
	    
	    viewerTable.getColumn(columnHeader2).setCellEditor(new RadioButtonCellEditor(new JCheckBox()));               
	    viewerTable.getColumn(columnHeader2).setCellRenderer(new RadioButtonTableCellRenderer());	   
	    
	    viewerTable.getColumn(columnHeader3).setCellRenderer(new ButtonTableCellRenderer());
	    viewerTable.getColumn(columnHeader3).setCellEditor(new ButtonCellEditor(new JCheckBox()));
	    viewerTable.getColumn(columnHeader3).setMaxWidth(70);	    
	    
	    viewerTable.getColumn(columnHeader4).setCellRenderer(new ButtonTableCellRenderer());
	    viewerTable.getColumn(columnHeader4).setCellEditor(new ButtonCellEditor(new JCheckBox()));
	    viewerTable.getColumn(columnHeader4).setMaxWidth(70);
	    
	    
	    viewerTable.setGridColor(Color.BLACK);	    
	    viewerTable.setRowHeight(20);
	    viewerTable.addMouseListener(this);
	    
	    ImageIcon upIcon = null;
        ImageIcon downIcon = null;        
        String upButtonLabel = null;
        String downButtonLabel= null;
        
        try {
            upIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Up16.gif"));
            downIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Down16.gif"));            
        } catch (Exception ex) {
        	upButtonLabel ="Up";
        	downButtonLabel = "Down";
        }     
        
	    for (int i=0; i< viewerSortOrder.size(); i++){
	    	SelectEnableObject<String> leftObj = new SelectEnableObject<String>(viewerSortOrder.get(i), false, false);	    	
	    	SelectEnableObject<String> rightObj = new SelectEnableObject<String>(viewerSortOrder.get(i), true, true);
	    	
	    	upButton = new JButton();
	        downButton = new JButton();
	        upButton.setToolTipText(ElanLocale.getString("PreferencesDialog.Viewer.SortButtonToolTip"));
	        downButton.setToolTipText(ElanLocale.getString("PreferencesDialog.Viewer.SortButtonToolTip"));
	        
		    if(upIcon !=null && downIcon !=null){
		    	upButton.setIcon(upIcon);
		    	downButton.setIcon(downIcon);
		    }else{
		    	upButton.setText(upButtonLabel);
		    	downButton.setText(downButtonLabel);
		    }
		    
		    dm.addRow(new Object[] { leftObj, rightObj, upButton, downButton });	
	    }
	    
        //main layout   
        int gy=0;
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;  
        
        gbc.gridy = gy++;
        gbc.insets = globalPanelInset;
        outerPanel.add(scrollSpeedPanel, gbc);
        
        gbc.gridy = gy++;
        outerPanel.add(colorPanel, gbc);
        
        gbc.gridy = gy++;  
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("TimeLineViewer.Name")), gbc);        
        
        gbc.gridy = gy++;  
        gbc.insets = globalInset;        
        outerPanel.add(aaBoldCB, gbc);
        
        gbc.gridy = gy++;      
        outerPanel.add(redTierHeightCB, gbc);  
        
        gbc.insets = catInset;
        gbc.gridy = gy++;  
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Viewer.TierOrderInDropdowns")), gbc);
        
        gbc.gridy = gy++;  
        gbc.insets = globalInset;        
        outerPanel.add(tierOrderInDropdownCB, gbc);
        
        gbc.insets = catInset;
        gbc.gridy = gy++;  
        outerPanel.add(new JLabel(ElanLocale.getString("Tab.Subtitles")), gbc);
        
        gbc.gridy = gy++;  
        gbc.insets = catPanelInset;        
        outerPanel.add(subtitlePanel, gbc);
                
        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Viewer.Label.Video")), gbc);
               
        gbc.gridy = gy++;  
        gbc.insets = globalInset;
        JScrollPane scrollPane = new JScrollPane(viewerTable);
        scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, viewerTable.getPreferredSize().height + 20));
        //viewerTable.setBorder(new LineBorder(Color.BLACK));
        outerPanel.add(scrollPane, gbc);
        
        gbc.gridy = gy++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;       
        outerPanel.add(new JPanel(), gbc); // filler 
        
        updateViewerSelectionInTable();  
    }

    /**
     * Returns a map of changed key-value pairs.
     *
     * @return a map of changed key-value pairs
     */
    @Override
	public Map<String, Object> getChangedPreferences() {
        if (isChanged()) {
            Map<String, Object> chMap = new HashMap<String, Object>(1);

            if (origNumSubtitles != (Integer) numSubCB.getSelectedItem()) {
                chMap.put("NumberOfSubtitleViewers",
                    numSubCB.getSelectedItem());
            }
            
            if (origActiveAnnBold != aaBoldCB.isSelected()) {
            	chMap.put("TimeLineViewer.ActiveAnnotationBold", Boolean.valueOf(aaBoldCB.isSelected()));
            }
            
            if (origTierOrderInDropdownCB != tierOrderInDropdownCB.isSelected()) {
            	chMap.put("SingleTierViewer.TierOrderInDropdown", 
            			Boolean.valueOf(tierOrderInDropdownCB.isSelected()));
            }
            
            if (origReducedTierHeight != redTierHeightCB.isSelected()) {
            	chMap.put("TimeLineViewer.ReducedTierHeight", Boolean.valueOf(redTierHeightCB.isSelected()));
            }
            
            if(videoInCentre){
            	for(int x=0; x< viewerTable.getRowCount(); x++){	
            		if( viewerTable.getModel().getValueAt(x, 0) instanceof SelectEnableObject){
            			SelectEnableObject<String> seo = (SelectEnableObject<String>) viewerTable.getModel().getValueAt(x, 1);
            			String refValue = getRefValue(seo.getValue());
            			chMap.put(refValue, Boolean.valueOf(seo.isSelected()));
            		}
            	}      
            }
            
            if (symAnnColor != origSymAnnColor){
            	chMap.put("Preferences.SymAnnColor", new Color(symAnnColor.getRed(),symAnnColor.getGreen(),symAnnColor.getBlue()));        	
            }
            
            if(sortOrderChanged){            	 
            	 chMap.put("PreferencesDialog.Viewer.SortOrder" , getNewViewerSortOrder());             	
            }
            
            int curScrollSpeed = (Integer) scrollSpeedSpinner.getValue();
            
            if (curScrollSpeed != origScrollSpeed) {
            	chMap.put("Preferences.TimeLine.HorScrollSpeed", Integer.valueOf(curScrollSpeed));
            }
            return chMap;
        }
        return null;
    } 
    
    /**
     * Returns whether any preference item has been changed.
     *
     * @return true if anything has changed.
     */
    @Override
	public boolean isChanged() {     	
    	
    	int count = (Integer) numSubCB.getSelectedItem();
    	
    	List<String> newSortOrder = getNewViewerSortOrder();
        
        for(int i=0; i< viewerSortOrder.size(); i++){
        	if(viewerSortOrder.get(i).equals(newSortOrder.get(i))) {
				continue;
			} else{        		
        		sortOrderChanged = true;
        		return true;
        	}
        }
        
        if (count != origNumSubtitles) {
            return true;
        }
        
        if (origActiveAnnBold != aaBoldCB.isSelected()) {
        	return true;
        }
        
        if (origTierOrderInDropdownCB != tierOrderInDropdownCB.isSelected()) {
        	return true;
        }
        
        if (origReducedTierHeight != redTierHeightCB.isSelected()) {
        	return true;
        }
        
        if(videoInCentre){        	
        	return true;
        }
        
        if(origSymAnnColor != symAnnColor){
            return true;
        }
        
        int curScrollSpeed = (Integer) scrollSpeedSpinner.getValue();
        
        if (curScrollSpeed != origScrollSpeed && (curScrollSpeed >= MIN_SCROLL && curScrollSpeed <= MAX_SCROLL)) {
        	return true;
        }
        
        return false;
    }    
    
    private List<String> getNewViewerSortOrder(){
    	List<String> newSortOrder = new ArrayList<String>();
    	int row = 0;
    	while(row < viewerTable.getRowCount()){     		
    		newSortOrder.add(((SelectEnableObject<String>)viewerTable.getModel().getValueAt(row, 0)).getValue());
    		row++;
    	}
    	
    	return newSortOrder;
    }

    /**
	 * updates the videoInCentre value
	 * 
	 * @param  val the new value
	 */
	public void updateVideoInCentre(Boolean val){
		if(val != videoInCentre){
			videoInCentre = val;
			updateViewerSelectionInTable();		
		}
	}
	
	/**
	 * updates the viewer selection panel with 	  
	 */
	private void updateViewerSelectionInTable(){		
		if(!videoInCentre){ 
			for(int x=0; x< viewerTable.getRowCount(); x++){	
				final Object value = viewerTable.getModel().getValueAt(x, 0);
				if (value instanceof SelectEnableObject){
					SelectEnableObject<String> leftRB= (SelectEnableObject<String>) value;
					SelectEnableObject<String> rightRB= (SelectEnableObject<String>) viewerTable.getModel().getValueAt(x, 1);
					leftRB.setEnabled(false);
					leftRB.setSelected(false);
					rightRB.setEnabled(true);
					rightRB.setSelected(true);
				}
			}
		} else {
			readViewerPref();
			for(int x=0; x< viewerTable.getRowCount(); x++){	
				final Object value = viewerTable.getModel().getValueAt(x, 0);
				if (value instanceof SelectEnableObject) {
					SelectEnableObject<String> rb = (SelectEnableObject<String>) value;
					rb.setEnabled(true);
				}
			}	
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		int selectedRowIndex = viewerTable.getSelectedRow();
		int selectedColumnIndex = viewerTable.getSelectedColumn();
		
		if( viewerTable.getValueAt(selectedRowIndex, selectedColumnIndex) instanceof JButton){	
			final TableModel model = viewerTable.getModel();
			if(selectedColumnIndex == 2 ){
				Object row1 = model.getValueAt(selectedRowIndex, 0);
				Object row11 = model.getValueAt(selectedRowIndex, 1);
				if(selectedRowIndex > 0){
					Object row2 = model.getValueAt(selectedRowIndex-1, 0);
					Object row21 = model.getValueAt(selectedRowIndex-1, 1);
					
					model.setValueAt(row1, selectedRowIndex-1, 0);
					model.setValueAt(row11, selectedRowIndex-1, 1);
					
					model.setValueAt(row2, selectedRowIndex, 0);
					model.setValueAt(row21, selectedRowIndex, 1);
				}
			} else if(selectedColumnIndex == 3 ){
				Object row1 = model.getValueAt(selectedRowIndex, 0);
				Object row11 = model.getValueAt(selectedRowIndex, 1);
				if(selectedRowIndex < viewerTable.getRowCount()-1){
					Object row2 = model.getValueAt(selectedRowIndex+1, 0);
					Object row21 = model.getValueAt(selectedRowIndex+1, 1);
					
					model.setValueAt(row1, selectedRowIndex+1, 0);
					model.setValueAt(row11, selectedRowIndex+1, 1);
					
					model.setValueAt(row2, selectedRowIndex, 0);
					model.setValueAt(row21, selectedRowIndex, 1);
				}
			}
		}
		viewerTable.repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int selectedRowIndex = viewerTable.getSelectedRow();
		int selectedColumnIndex = viewerTable.getSelectedColumn();
		
		if( viewerTable.getValueAt(selectedRowIndex, selectedColumnIndex) instanceof SelectEnableObject){			
			SelectEnableObject<String> seo1 = (SelectEnableObject<String>) viewerTable.getValueAt(selectedRowIndex, selectedColumnIndex);
			if(seo1.isSelected()){
				SelectEnableObject<String> seo2 = null;
				if(selectedColumnIndex==0){
					seo2 = (SelectEnableObject<String>) viewerTable.getValueAt(selectedRowIndex, 1);
				} else if(selectedColumnIndex==1) {
					seo2 = (SelectEnableObject<String>) viewerTable.getValueAt(selectedRowIndex, 0);
				}
				if(seo2 != null && seo2.isEnabled()){
					seo2.setSelected(false);
					//seo2.setSelected(!seo1.isSelected());
				}
			} else {
				seo1.setSelected(true);
			}
		}
		viewerTable.repaint();
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
	    
	    Color newColor = null;

	    if (e.getSource() == colorButton) {
		
		// symAnnColor is suggested in the dialog
		
		dialog = new ColorDialog (this, origSymAnnColor);  
		
		newColor = dialog.chooseColor(); 
		if (newColor == null){
		    // no color selected, keep current
		}
		else {
		    symAnnColor = newColor;
		    colorPreviewPanel.setBackground(symAnnColor);
		}
	    }
	    else if (e.getSource() == resetColorButton) {
	            colorPreviewPanel.setBackground(Constants.SHAREDCOLOR1); 
	    }
	}
}



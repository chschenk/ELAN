package mpi.eudico.client.annotator.export;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.JFontChooser;
import mpi.eudico.client.util.FavoriteColorPanel;



/**
 * A dialog to set the font and color settings of the subtitle.
 *
 * @author Aarthy
 * @version 
 */
@SuppressWarnings("serial")
public class DisplaySettingsPane extends JDialog implements ActionListener, ChangeListener{
	
	    private JPanel backgroundColorPreviewPanel;
	    private JButton backgroundColorButton;
	    private JButton resetBackgroundColorButton;
	    private JLabel backgroundColorLabel;
	    
	    private JCheckBox transparentBackgroundCB;
	    
	    private JPanel textColorPreviewPanel;
	    private JButton textColorButton;
	    private JButton resetTextColorButton;
	  
	    private JTextField fontTextField;
	    private JButton fontButton;
	    private JButton resetFontButton;
	    
	    private JComboBox fontSizeComboBox;
	    private JComboBox textAlignComboBox;
	    
	    private JButton applyButton;
	    private JButton cancelButton; 
	    
	    private int fontSize;
	    private String fontName;
	    private Color backColor;
	    private Color textColor;
	    private String justify;
	    private Map<String, Object> settingHashMap;
	    
	    private String parent;
	   	 
	     /**
	     * Creates a new Instance.
	     *
	     * @param owner the owner window
	     * @param title the dialog title
	     * 
	     */
	    public DisplaySettingsPane(Dialog owner, String title) {
	    	this(owner, title, null); 
	    }
	    
	    /**
	     * Creates a new Instance.
	     *
	     * @param owner the owner window
	     * @param title the dialog title
	     * @param parent the name of the parent dialog
	     * 
	     */
	    public DisplaySettingsPane(Dialog owner, String title, String parent) {
	    	super(owner, title, true); 	  
	    	this.parent = parent;
	        initComponents();
	        postInit();
	    }
	    
	    /**
	     * Initializes the ui components.
	     */
	    private void initComponents() {
	        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	        getContentPane().setLayout(new GridBagLayout());

	        JPanel prefPanel = new JPanel();

	        prefPanel.setBorder(new TitledBorder(ElanLocale.getString("DisplaySettingsPane.Label.AdvancedOptions")));
	        prefPanel.setLayout(new GridBagLayout());	        
	        
	        backgroundColorLabel = new JLabel(ElanLocale.getString("DisplaySettingsPane.Label.BackgroundTextColor"));
	        backgroundColorPreviewPanel = new JPanel();
	        backgroundColorPreviewPanel.setBorder(new LineBorder(Color.GRAY, 1));
	        backgroundColorPreviewPanel.setBackground(Color.BLACK); 
	        backgroundColorButton = new JButton(ElanLocale.getString("Button.Browse"));
	        backgroundColorButton.addActionListener(this);
	        resetBackgroundColorButton = new JButton(ElanLocale.getString("Button.Default"));
	        resetBackgroundColorButton.addActionListener(this);
	        backgroundColorPreviewPanel.setPreferredSize(new Dimension(backgroundColorButton.getPreferredSize().height, 
	        		backgroundColorButton.getPreferredSize().height));
	        backgroundColorPreviewPanel.setMinimumSize(new Dimension(backgroundColorButton.getPreferredSize().height, 
	        		backgroundColorButton.getPreferredSize().height));
	       
			JLabel textColorLabel = new JLabel(ElanLocale.getString(
					"DisplaySettingsPane.Label.TextColor"));
			textColorPreviewPanel = new JPanel();
			textColorPreviewPanel.setBorder(new LineBorder(Color.GRAY, 1));
			textColorPreviewPanel.setBackground(Color.WHITE);
			textColorButton = new JButton(ElanLocale.getString("Button.Browse"));
			textColorButton.addActionListener(this);
	        resetTextColorButton = new JButton(ElanLocale.getString("Button.Default"));
	        resetTextColorButton.addActionListener(this);
	        textColorPreviewPanel.setPreferredSize(new Dimension(  textColorButton.getPreferredSize().height,
	        		 textColorButton.getPreferredSize().height));
	        textColorPreviewPanel.setMinimumSize(new Dimension(	 textColorButton.getPreferredSize().height,
	        		 textColorButton.getPreferredSize().height));	        
	        
	        JLabel fontLabel = new JLabel(ElanLocale.getString(
	                    "DisplaySettingsPane.Label.Font"));
	        fontTextField = new JTextField(20);
	        fontTextField.setEditable(false);
            fontTextField.setText("Arial Unicode MS");	        
	        fontButton = new JButton(ElanLocale.getString("Button.Browse"));
	        fontButton.addActionListener(this);
	        resetFontButton = new JButton(ElanLocale.getString("Button.Default"));
	        resetFontButton.addActionListener(this);	
	        transparentBackgroundCB = new JCheckBox(ElanLocale.getString("DisplaySettingsPane.Label.TransparentBackground")); 
	        transparentBackgroundCB.addChangeListener(this);
	        
	        JLabel fontSizeLabel = new JLabel(ElanLocale.getString(
	        			"DisplaySettingsPane.Label.FontSize"));
	        fontSizeComboBox = new JComboBox(new Object[] {
	        		Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10),
	        		Integer.valueOf(11), Integer.valueOf(12), Integer.valueOf(14),
	        		Integer.valueOf(16), Integer.valueOf(18), Integer.valueOf(24),
	        		Integer.valueOf(28), Integer.valueOf(36), Integer.valueOf(48),
	        		Integer.valueOf(60)});
	        fontSizeComboBox.setEditable(false);
	        fontSizeComboBox.setSelectedItem(12);	 
	        fontSizeComboBox.addActionListener(this);
	        
	        JLabel textAlignLabel = new JLabel(ElanLocale.getString(
				"DisplaySettingsPane.Label.TextAlign"));
			textAlignComboBox = new JComboBox(new Object[] {
					new String("center"), new String("left"), new String("right")  });
			textAlignComboBox.setEditable(false);
			textAlignComboBox.setSelectedItem("left");	
			textAlignComboBox.addActionListener(this);
	    	        
			Insets insets = new Insets(2, 6, 2, 6);
			
	        GridBagConstraints gbc = new GridBagConstraints();
	        
	        gbc.anchor = GridBagConstraints.WEST;	        
	        gbc.gridy = 0;
	        gbc.gridx = 0;
	        gbc.insets = insets;
	        prefPanel.add(transparentBackgroundCB, gbc);
	        gbc.gridy = 1;	
	        prefPanel.add(backgroundColorLabel, gbc);
	        gbc.gridx = 1;
	        prefPanel.add(backgroundColorPreviewPanel, gbc);
	        gbc.gridx = 3;
	        gbc.gridwidth = 1;
	        gbc.fill = GridBagConstraints.NONE;
	        gbc.weightx = 0;
	        gbc.weighty = 0;
	        prefPanel.add(backgroundColorButton, gbc);
	        gbc.gridx = 4;
	        prefPanel.add(resetBackgroundColorButton, gbc);
	        
	        gbc.gridx = 0;
	        gbc.gridy = 2;
	        prefPanel.add(textColorLabel, gbc);
	        gbc.gridx = 1;
	        prefPanel.add(textColorPreviewPanel, gbc);
	        gbc.gridx = 3;
	        gbc.gridwidth = 1;
	        gbc.fill = GridBagConstraints.NONE;
	        gbc.weightx = 0;
	        gbc.weighty = 0;
	        prefPanel.add(textColorButton, gbc);
	        gbc.gridx = 4;
	        prefPanel.add(resetTextColorButton, gbc);
	        	        
	        gbc.gridx = 0;
	        gbc.gridy = 3;	      
	        prefPanel.add(fontLabel, gbc);
	        gbc.gridx = 1;
	        gbc.gridwidth = 2;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.weightx = 1.0;
	        prefPanel.add(fontTextField, gbc);
	        gbc.gridx = 3;
	        gbc.gridwidth = 1;
	        gbc.fill = GridBagConstraints.NONE;
	        gbc.weightx = 0;
	        prefPanel.add(fontButton, gbc);
	        gbc.gridx = 4;
	        prefPanel.add(resetFontButton, gbc);
	        
	        gbc.gridy = 4;
	        gbc.gridx = 0;	        
	        prefPanel.add(fontSizeLabel, gbc);
	        
	        gbc.gridx = 1;
	        prefPanel.add(fontSizeComboBox, gbc);
	        
	        gbc.gridy = 5;
	        gbc.gridx = 0;	   
	        gbc.fill = GridBagConstraints.NONE;
	        prefPanel.add(textAlignLabel, gbc);
	        
	        gbc.gridx = 1;
	        prefPanel.add(textAlignComboBox, gbc);  
	        
	        gbc = new GridBagConstraints();
	        gbc.gridy = 1;
	        gbc.fill = GridBagConstraints.BOTH;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.insets = new Insets(6, 6, 6, 6);
	        gbc.weightx = 1.0;
	        gbc.weighty = 1.0;
	        getContentPane().add(prefPanel, gbc);

	        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
	        applyButton = new JButton();
	        applyButton.setText(ElanLocale.getString("Button.Apply"));
	        applyButton.addActionListener(this);
	        cancelButton = new JButton();
	        cancelButton.setText(ElanLocale.getString("Button.Cancel"));
	        cancelButton.addActionListener(this);
	        buttonPanel.add(applyButton);
	        buttonPanel.add(cancelButton);

	        gbc = new GridBagConstraints();
	        gbc.gridy = 2;
	        gbc.anchor = GridBagConstraints.SOUTH;
	        gbc.insets = insets;
	        getContentPane().add(buttonPanel, gbc); 
	        
	        fontSizeComboBox.addActionListener(this);
	        loadPreferences();
	        initialize();
	        
	        if(parent != null){
	        	if(parent.equalsIgnoreCase("realPlayer")){
	        		transparentBackgroundCB.setEnabled(false);
	        		transparentBackgroundCB.setSelected(false);	        		
	        	}	        		
	        }

	        addWindowListener(new WindowAdapter() {
	                @Override
					public void windowClosing(WindowEvent we) {
	                    doClose();
	                }
	            });
	    }
	    
	    /**
	     * Intializes the local variables 
	     */	    
	    private void initialize()
	    {
	    	fontSize = (Integer)fontSizeComboBox.getSelectedItem();
	    	fontName = fontTextField.getText();
	    	justify = (String)textAlignComboBox.getSelectedItem();
	    	backColor = backgroundColorPreviewPanel.getBackground();
	    	textColor = textColorPreviewPanel.getBackground();  
	    }

	    
	    /**
	     * Enables/disables the back ground Color panel
	     *
	     * @param e the change event
	     */
	    @Override
		public void stateChanged(ChangeEvent e) {
	        if (e.getSource() == transparentBackgroundCB) {
	        	backgroundColorButton.setEnabled(!transparentBackgroundCB.isSelected());
	    	    resetBackgroundColorButton.setEnabled(!transparentBackgroundCB.isSelected());
	    	    backgroundColorLabel.setEnabled(!transparentBackgroundCB.isSelected());
	    	    backgroundColorPreviewPanel.setEnabled(!transparentBackgroundCB.isSelected());
	        }
	    }
	    
	    /**
	     * Pack, size and set location.
	     */
	    private void postInit() {
	        pack();	        
	        setResizable(false);
	        setLocationRelativeTo(getParent());
	    }

	    /**
	     * Creates a customized color chooses, which includes a panel for (persistent) favorite
	     * colors.
	     * 
	     * @param oldColor the color to start with
	     * @return a new color or null
	     */
	    private Color chooseColor(final Color oldColor) {
	    	Color newColor = null;
	    	
	    	final JColorChooser chooser = new JColorChooser(oldColor);
	    	AbstractColorChooserPanel[] panels = chooser.getChooserPanels();
	    	AbstractColorChooserPanel[] panels2 = new AbstractColorChooserPanel[panels.length + 1];
	    	FavoriteColorPanel fcp = new FavoriteColorPanel();
	    	panels2[0] = fcp;
	    	
	    	for (int i = 0; i < panels.length; i++) {
	    		panels2[i + 1] = panels[i];
	    	}
	    	
	    	chooser.setChooserPanels(panels2);
	    	// read stored favorite colors
	    	Map<String, Color> oldColors = Preferences.getMapOfColor("FavoriteColors", null);
	    	if (oldColors != null) {
	    		//Color[] favColors = new Color[fcp.NUM_COLS * fcp.NUM_ROWS];
	    		Color[] favColors = fcp.getColors();// use the array of the panel
	    		for (Map.Entry<String, Color> e : oldColors.entrySet()) {
	    			String key = e.getKey();
	    			Color val = e.getValue();
	    			try {
	    				int index = Integer.valueOf(key);
	        			if (index < favColors.length) {
	        				favColors[index] = val;
	        			}
	    			} catch (NumberFormatException nfe) {
	    				// ignore
	    			}
	    		}	    		
	    	}
	    	
	    	// have to provide an "OK" action listener...
	    	AbstractAction aa = new AbstractAction() {
	    			
				@Override
				public void actionPerformed(ActionEvent e) {
					putValue(Action.DEFAULT, chooser.getColor());				
				}};
				
	        JDialog cd = JColorChooser.createDialog(this, ElanLocale.getString("ColorChooser.Title"), 
	        		true, chooser, aa, null);
	        cd.setVisible(true);
	        
	        // if necessary store the current favorite colors
	        Map<String, Color> colMap = new HashMap<String, Color>();
	        Color[] colors = fcp.getColors();
	        for (int i = 0; i < colors.length; i++) {
	        	if (colors[i] != null) {
	        		colMap.put(String.valueOf(i), colors[i]);
	        	}
	        }
	        
	        if (colMap.size() > 0 || oldColors != null) {
	        	Preferences.set("FavoriteColors", colMap, null);
	        }
	        
	        newColor = (Color) aa.getValue(Action.DEFAULT);
	        
	    	return newColor;
	    }
	    
	     private void selectBackgroundColor() {	    		    	
	    	Color newColor = chooseColor(backgroundColorPreviewPanel.getBackground());	    	
	    	if (newColor != null) {
	            backgroundColorPreviewPanel.setBackground(newColor);
	            backColor = newColor;
	        }
	    }
	     
	    private void selectTextColor() {	    	
	    	Color newColor = chooseColor(textColorPreviewPanel.getBackground()); 	        
	        if (newColor != null) {
	            textColorPreviewPanel.setBackground(newColor);
	            textColor = newColor;
	        }
	    }
	    
	    private void updateFontSize() {
	    	fontSize = (Integer)fontSizeComboBox.getSelectedItem();
		}
	    
	    private void updateTextAlignment() {
	    	justify = (String)textAlignComboBox.getSelectedItem();
		}
			    
	    private void selectFont() {
	    	JFontChooser jfc = new JFontChooser();
	    	Font curFont = new Font(fontTextField.getText(),0, 12);
	    	Font f = jfc.showDialog(this, true, curFont);
	    	if (f != null) {	    		
	    		fontTextField.setText(f.getName()); 
	    		fontName = f.getName();
	    	}	    	
	    }

	    /**
	     *   Stores all the settings in a map
	     */
	    private void applyNewSetting(){	    	
	    	settingHashMap = new HashMap<String, Object>();
	    	settingHashMap.put("size",fontSize);
	    	settingHashMap.put("font",fontName);
	    	settingHashMap.put("backColor",backColor);
	    	settingHashMap.put("textColor", textColor );
	    	settingHashMap.put("justify",justify);	
	    	settingHashMap.put("transparent",transparentBackgroundCB.isSelected());
	    }
	    
	    private void doClose() {
	    	savePreferences();
	        setVisible(false);	            	
	        dispose();
	    }
	    
	    /**
	     * The action event handling
	     *
	     * @param e the event
	     */	    
	    @Override
		public void actionPerformed(ActionEvent e) {
	        if (e.getSource() == applyButton) {		        	
	        	applyNewSetting();		        	
		    	doClose();
	        } else if (e.getSource() == cancelButton) {
	             doClose();
	        } else if (e.getSource() == backgroundColorButton) {
	            selectBackgroundColor();
	        } else if (e.getSource() == resetBackgroundColorButton) {
	            backgroundColorPreviewPanel.setBackground(Color.BLACK);	
	        } else if (e.getSource() == textColorButton) { 
	        	selectTextColor();
	        } else if (e.getSource() == resetTextColorButton) {
	        	textColorPreviewPanel.setBackground(Color.WHITE);	        	
	        } else if (e.getSource() == fontButton) {
	            selectFont();
	        } else if (e.getSource() == resetFontButton) {
	        	    fontTextField.setText("Arial Unicode MS");	    		    
	        }else if (e.getSource() == fontSizeComboBox){
	        	updateFontSize();	        	
	        }else if (e.getSource() == textAlignComboBox){
	        	updateTextAlignment();
	        }
	    }
	    
	    /**
	     *  @param parent the owner window
	     *  @param title the dialog title
	     *  
	     *  return the map with the new display preferences
	     */
	    public static Map<String, Object> getNewFontSetting(JDialog parent, String title) {
	    	DisplaySettingsPane pane = new DisplaySettingsPane(parent,title, parent.getName());
	    	pane.setVisible(true);		    	
	    	
	    	return pane.settingHashMap;
	    }
	    
	    /** 
	     *  Returns a map with the previously stored display preferences, or null in case there
	     *  are no previous settings.
	     */
	    public static Map<String, Object> getLastUsedSetting() {
	    	Map<String, Object> storedMap = new HashMap<String, Object>();
	    	
	    	Color colorPref = Preferences.getColor("DisplaySettingsPane.backColor", null);
	    	if (colorPref != null) {
	    		storedMap.put("backColor", colorPref);
	    	}
	    	
	    	colorPref = Preferences.getColor("DisplaySettingsPane.textColor", null);
	    	if (colorPref != null) {
	    		storedMap.put("textColor", colorPref);
	    	}
	    	
	    	String stringPref = Preferences.getString("DisplaySettingsPane.fontName", null);
	    	if (stringPref != null) {
	    		storedMap.put("font", stringPref);
	    	}
	    	
	    	Integer intPref = Preferences.getInt("DisplaySettingsPane.fontSize", null);
	    	if (intPref != null) {
	    		storedMap.put("size", intPref);
	    	}
	    	
	    	stringPref = Preferences.getString("DisplaySettingsPane.justify", null);
	    	if (stringPref != null) {
	    		storedMap.put("justify", stringPref);
	    	}
	    	
	    	Boolean boolPref = Preferences.getBool("DisplaySettingsPane.transparentBackground", null);
	    	if (boolPref != null) {
	    		storedMap.put("transparent", boolPref);
	    	}

	    	if (storedMap.isEmpty()) {
	    		return null;
	    	}
	    	
	    	return storedMap;
	    }
	    
	    /**
	     *  Save the preferences of the user
	     */
	    private void savePreferences(){
	    	Preferences.set("DisplaySettingsPane.backColor", backColor, null);	
	    	Preferences.set("DisplaySettingsPane.textColor", textColor, null);
	    	Preferences.set("DisplaySettingsPane.fontSize", fontSize, null);
	    	Preferences.set("DisplaySettingsPane.fontName", fontName, null);
	    	Preferences.set("DisplaySettingsPane.justify", justify, null);
	    	Preferences.set("DisplaySettingsPane.transparentBackground", transparentBackgroundCB.isSelected(), null);	
	    }
	    
	    /**
	     *  Loads the User preferences
	     */
	    private void loadPreferences(){
	    	Color colorPref = Preferences.getColor("DisplaySettingsPane.backColor", null);	
	    	if (colorPref != null) {
	    		backgroundColorPreviewPanel.setBackground(colorPref);
	    	}
	    	
	    	colorPref = Preferences.getColor("DisplaySettingsPane.textColor", null);
	    	if (colorPref != null) {
	    		textColorPreviewPanel.setBackground(colorPref);
	    	}
	    	
	    	String stringPref = Preferences.getString("DisplaySettingsPane.fontName", null);
	    	if (stringPref != null) {
	    		fontTextField.setText(stringPref);
	    	}
	    	
	    	stringPref = Preferences.getString("DisplaySettingsPane.justify", null);
	    	if (stringPref != null) {
	    		textAlignComboBox.setSelectedItem(stringPref);	
	    	}
	    	
	    	Integer intPref = Preferences.getInt("DisplaySettingsPane.fontSize", null);
	    	if( intPref != null) {
	    		fontSizeComboBox.setSelectedItem(intPref);	
	    	}
	    	
	    	Boolean boolPref = Preferences.getBool("DisplaySettingsPane.transparentBackground", null);
	    	if (boolPref != null) {
	    		transparentBackgroundCB.setSelected(boolPref);
	    	}
	    }
	}

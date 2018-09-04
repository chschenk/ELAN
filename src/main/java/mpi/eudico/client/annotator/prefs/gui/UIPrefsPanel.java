package mpi.eudico.client.annotator.prefs.gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;


/**
 * A panel for changing UI related settings.
 * 
 * @author Mark Blokpoel
  */
@SuppressWarnings("serial")
public class UIPrefsPanel extends AbstractEditPrefsPanel implements PreferenceEditor {
    private JComboBox nrOfRecentItemsCBox;
    private Integer origNrRecentItems = 5;
    private JCheckBox tooltipCB;
    private boolean origToolTipEnabled = true;
    private JCheckBox showAnnotationCountCB;
    private boolean origShowAnnotationCount = false; 
    private JRadioButton useBufferedPaintingRB;
    private JRadioButton useDirectPaintingRB;
    /**
     * 03-2013 user interface for buffered painting setting that used to be only a 
     * command line parameter. Default is now false.
     */
    private boolean origUseBufferedPainting = false;// default false
    private JSlider fontScaleSlider;
    //private float origFontScale = 1.0f; // CC
    private int origFontScaleInt = 100;

    /**
     * Creates a new PlatformPanel instance
     */
    public UIPrefsPanel() {
        super(ElanLocale.getString("PreferencesDialog.Category.UI"));
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
        Integer intPref = Preferences.getInt("UI.RecentItems", null);

        if (intPref != null) {
            origNrRecentItems = intPref;
        }
        
        Boolean boolPref = Preferences.getBool("UI.ToolTips.Enabled", null);
        
        if (boolPref != null) {
        	origToolTipEnabled = boolPref;
        }
        
        boolPref = Preferences.getBool("UI.MenuItems.ShowAnnotationCount", null);
        if (boolPref != null) {
    		origShowAnnotationCount = boolPref;
        }
    	
    	boolPref = Preferences.getBool("UI.UseBufferedPainting", null);
        if (boolPref != null) {
    		origUseBufferedPainting = boolPref;
    	}
        
        Float scalePref = Preferences.getFloat("UI.FontScaleFactor", null);
        if (scalePref != null) {
        	float origFontScale = scalePref.floatValue();
        	if (origFontScale < 1) {
        		origFontScale = 1.0f;
        	} else if (origFontScale > 2) {
        		origFontScale = 2.0f;
        	}
        	origFontScaleInt = (int) (100 * origFontScale);
        }
    }

    private void initComponents() {  
    	GridBagConstraints gbc;
    	Font plainFont;

    	//recent items panel
    	Integer[] nrOfRecentItemsList = { 5, 10, 15, 20, 25, 30 };
        nrOfRecentItemsCBox = new JComboBox(nrOfRecentItemsList);
        nrOfRecentItemsCBox.setSelectedItem(origNrRecentItems);
        plainFont = nrOfRecentItemsCBox.getFont().deriveFont(Font.PLAIN);
        nrOfRecentItemsCBox.setFont(plainFont);
        nrOfRecentItemsCBox.setToolTipText(ElanLocale.getString(
                "PreferencesDialog.Relaunch.Tooltip"));
        
        JLabel recentItemsLabel = new JLabel(ElanLocale.getString("PreferencesDialog.UI.RecentItems"));
        recentItemsLabel.setFont(plainFont);

        JPanel recentItemsPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;        
        gbc.insets = topInset;
        recentItemsPanel.add(recentItemsLabel,gbc);

        gbc.gridx = 1;
        gbc.insets = leftInset;
        recentItemsPanel.add(nrOfRecentItemsCBox, gbc);
    	
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;  
        gbc.weightx = 1.0;
        recentItemsPanel.add(new JPanel(), gbc); // filler
        
        // main panel    	
    	int gy=0;
    	       
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.insets = catInset;
        gbc.gridy = gy++;
        outerPanel.add(recentItemsPanel, gbc);
        
        
        JLabel uiFontScaleLabel = new JLabel(ElanLocale.getString("PreferencesDialog.UI.FontScaling"));
        fontScaleSlider = new JSlider(SwingConstants.HORIZONTAL, 100, 200, origFontScaleInt);
        fontScaleSlider.setMajorTickSpacing(20);
        fontScaleSlider.setPaintLabels(true);
        fontScaleSlider.setPaintTicks(true);
 
        JPanel fontScalePanel = new JPanel(new GridBagLayout());
        GridBagConstraints fbc = new GridBagConstraints();
        fbc.anchor = GridBagConstraints.NORTHWEST;        
        fbc.insets = topInset;
        fbc.fill = GridBagConstraints.HORIZONTAL;
        fbc.weightx = 0.1;
        fbc.gridwidth = 1;
        fbc.gridheight = 1;
        fontScalePanel.add(uiFontScaleLabel, fbc);
        fbc.gridy = 1;
        gbc.insets = globalInset;
        fontScalePanel.add(fontScaleSlider, fbc);
        
        JLabel relaunchLabel = new JLabel();
        ImageIcon relaunchIcon = null;

        // add relaunch icon
        try {
            relaunchIcon = new ImageIcon(this.getClass()
                                             .getResource("/toolbarButtonGraphics/general/Refresh16.gif"));
            relaunchLabel.setIcon(relaunchIcon);
        } catch (Exception ex) {
            relaunchLabel.setText(ElanLocale.getString(
                    "PreferencesDialog.Relaunch"));
        }

        relaunchLabel.setToolTipText(ElanLocale.getString(
                "PreferencesDialog.Relaunch.Tooltip"));

        fbc.gridx = 1;
        fbc.gridy = 0;
        fbc.gridheight = 2;
        fbc.fill = GridBagConstraints.NONE;
        fbc.anchor = GridBagConstraints.CENTER;      
        fbc.weightx = 0.0;
        fontScalePanel.add(relaunchLabel, fbc);
        
        gbc.gridy = gy++;
        outerPanel.add(fontScalePanel, gbc);
        
        gbc.gridy = gy++;
        gbc.insets = catInset;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.UI.ToolTip")), gbc);
        
        tooltipCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.UI.ToolTip.Enabled"));
        tooltipCB.setSelected(origToolTipEnabled);
        tooltipCB.setFont(tooltipCB.getFont().deriveFont(Font.PLAIN));
       
        gbc.gridy = gy++;
        gbc.insets = globalInset;
        outerPanel.add(tooltipCB, gbc);
        
        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.UI.MenuOptions")), gbc);
        
        showAnnotationCountCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.UI.MenuOptions.ShowAnnotationCount"));
        showAnnotationCountCB.setSelected(origShowAnnotationCount);
        showAnnotationCountCB.setFont(showAnnotationCountCB.getFont().deriveFont(Font.PLAIN));
        gbc.gridy = gy++;
        gbc.insets = globalInset;
        outerPanel.add(showAnnotationCountCB, gbc);
        
        // hier add label?
        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.UI.PaintingStrategy")), gbc);
        
        ButtonGroup bGroup = new ButtonGroup();
        useBufferedPaintingRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.UI.UseBufferedPainting"));
        useBufferedPaintingRB.setSelected(origUseBufferedPainting);
        useBufferedPaintingRB.setFont(tooltipCB.getFont().deriveFont(Font.PLAIN));
        useDirectPaintingRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.UI.UseDirectPainting"));
        useDirectPaintingRB.setSelected(!origUseBufferedPainting);
        useDirectPaintingRB.setFont(tooltipCB.getFont().deriveFont(Font.PLAIN));
        bGroup.add(useBufferedPaintingRB);
        bGroup.add(useDirectPaintingRB);
       
        // add buttons        
        gbc.gridy = gy++;
        gbc.insets = globalInset;
        outerPanel.add(useBufferedPaintingRB, gbc);
      
        gbc.gridy = gy++;
        outerPanel.add(useDirectPaintingRB, gbc);
        
        gbc.gridy = gy++;       
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;        
        outerPanel.add(new JPanel(), gbc); // filler
    }
    
    /**
     * Returns
     *
     * @return a map containing the changes
     */
    @Override
	public Map<String, Object> getChangedPreferences() {
        if (isChanged()) {
            Map<String, Object> chMap = new HashMap<String, Object>(6);

            if (nrOfRecentItemsCBox.getSelectedItem() != origNrRecentItems) {
                chMap.put("UI.RecentItems",
                    nrOfRecentItemsCBox.getSelectedItem());
            }
            if (fontScaleSlider.getValue() != origFontScaleInt) {
            	chMap.put("UI.FontScaleFactor", fontScaleSlider.getValue() / 100f);
            }
            // will be handled by ElanLayoutManager (arbitrary choice)
            if (tooltipCB.isSelected() != origToolTipEnabled) {
            	chMap.put("UI.ToolTips.Enabled",
            			Boolean.valueOf(tooltipCB.isSelected()));
            }
            if(showAnnotationCountCB.isSelected() != origShowAnnotationCount){
        		chMap.put( "UI.MenuItems.ShowAnnotationCount", Boolean.valueOf(showAnnotationCountCB.isSelected()) );
        	}
            
            if (useBufferedPaintingRB.isSelected() != origUseBufferedPainting) {
            	chMap.put("UI.UseBufferedPainting", Boolean.valueOf(useBufferedPaintingRB.isSelected()));
            }
            return chMap;
        }

        return null;
    }

    /**
     * Returns whether any of the settings has changed
     *
     * @return whether anything changed
     */
    @Override
	public boolean isChanged() {
        if (nrOfRecentItemsCBox.getSelectedItem() != origNrRecentItems || 
        		tooltipCB.isSelected() != origToolTipEnabled || 
        		showAnnotationCountCB.isSelected() != origShowAnnotationCount ||
        		origUseBufferedPainting != useBufferedPaintingRB.isSelected() ||
        		origFontScaleInt != fontScaleSlider.getValue()) {
            return true;
        }

        return false;
    }
}

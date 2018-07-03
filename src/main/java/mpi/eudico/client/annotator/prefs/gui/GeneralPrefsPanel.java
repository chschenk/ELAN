package mpi.eudico.client.annotator.prefs.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mpi.eudico.client.annotator.CachedDataManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;
import mpi.eudico.client.annotator.util.FileUtility;


/**
 * A panel for changing settings concerning preferences files.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class GeneralPrefsPanel extends AbstractEditPrefsPanel implements PreferenceEditor,
    ActionListener {
    private String curGenPrefsLocation = "-";
    private JLabel setDirLabel;
    private JLabel curDirLabel;
    private JButton defaultDirButton;
    private JButton resetDirButton;
    
    // Cache directory 
    private String curCacheDirectory = "-";
    private JLabel cacheDirLabel;
    private JLabel curCacheDirLabel;
    private JButton cacheDirButton;
    private JButton resetCacheDirButton;
    
    private JCheckBox tierSetCB;
    private String curTSPath = "-";
    private JLabel setTSFileLabel;
    private JLabel curTSFileLabel;
    private JButton defaultTSFileButton;
    private JButton resetTSFileButton;
    
    private JComboBox nrOfBuFilesCB;
    private Integer origNumBuFiles = 1;
    private JCheckBox checkForUpdatesCB;
    private JCheckBox saveAsOldFormatCB;
    private boolean origCheckUpdates = true;    
    private boolean origSaveAsOldFormatFlag = false;
    private boolean oriWorkWithTierSetFlag = false;

    /**
     * Constructor. Reads the current preferences and creates the ui.
     */
    public GeneralPrefsPanel() {
        super(ElanLocale.getString("PreferencesDialog.Category.Preferences"));
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
        String stringPref = Preferences.getString("DefaultPreferencesLocation", null);

        if (stringPref != null) {
            curGenPrefsLocation = stringPref;
        }
        
        stringPref = Preferences.getString("DefaultTierSetFilePath", null);
        if (stringPref != null) {
        	curTSPath = stringPref;
        }
        
        Boolean boolPref1 = Preferences.getBool("WorkwithTierSets", null);
        if (boolPref1 != null) {
        	oriWorkWithTierSetFlag = boolPref1.booleanValue();
        }
        
        Integer intPref = Preferences.getInt("NumberOfBackUpFiles", null);
        
        if (intPref != null) {
			origNumBuFiles = intPref;
		}
        
        Boolean boolPref = Preferences.getBool("AutomaticUpdate", null);
        
        if (boolPref != null) {
        	origCheckUpdates = boolPref;
		}
        
        boolPref = Preferences.getBool("SaveAsOldEAF2_7", null);
        
        if (boolPref != null) {
        	origSaveAsOldFormatFlag = boolPref.booleanValue();
        }
        
        stringPref = Preferences.getString("CacheLocation", null);
        if (stringPref != null) {
        	curCacheDirectory = stringPref;
        }
    }

    private void initComponents() {
    	
    	Font plainFont;
    	// create panel for set directory
        setDirLabel = new JLabel(ElanLocale.getString(
                "PreferencesDialog.Prefs.DefaultLoc"));
        plainFont = setDirLabel.getFont().deriveFont(Font.PLAIN);
        setDirLabel.setFont(plainFont);        
        
        JPanel dirPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 0;        
        gbc.insets = topInset;      
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;        
        dirPanel.add(setDirLabel, gbc);
        
        curDirLabel = new JLabel(curGenPrefsLocation);
        curDirLabel.setFont(new Font(curDirLabel.getFont().getFontName(), Font.PLAIN, 10));
        gbc.gridy = 1;
        dirPanel.add(curDirLabel, gbc);
        
        defaultDirButton = new JButton(ElanLocale.getString("Button.Browse"));
        gbc.gridy = 0;
        gbc.gridx = 1; 
        gbc.gridheight = 2;
        gbc.insets = leftInset;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        dirPanel.add(defaultDirButton, gbc);
        defaultDirButton.addActionListener(this);
        
        resetDirButton = new JButton();
        ImageIcon resetIcon = null;
        // add reset icon
        try {
            resetIcon = new ImageIcon(this.getClass()
                                          .getResource("/mpi/eudico/client/annotator/resources/Remove.gif"));
            resetDirButton.setIcon(resetIcon);
        } catch (Exception ex) {
            resetDirButton.setText("X");
        }

        resetDirButton.setToolTipText(ElanLocale.getString(
                "PreferencesDialog.Reset"));
        resetDirButton.setPreferredSize(new Dimension(
                resetDirButton.getPreferredSize().width,
                defaultDirButton.getPreferredSize().height));        
        gbc.gridx = 2;
        dirPanel.add(resetDirButton, gbc);       
        resetDirButton.addActionListener(this);
        
        // create panel for cache directory
        cacheDirLabel = new JLabel(ElanLocale.getString(
                "PreferencesDialog.Prefs.CacheDirectory"));
        cacheDirLabel.setFont(plainFont);
        JPanel cacheDirPanel = new JPanel(new GridBagLayout());
        GridBagConstraints cachePanelGbc = new GridBagConstraints();
        cachePanelGbc.anchor = GridBagConstraints.NORTHWEST;
        cachePanelGbc.gridx = 0;
        cachePanelGbc.gridy = 0;        
        cachePanelGbc.insets = topInset;      
        cachePanelGbc.fill = GridBagConstraints.HORIZONTAL;
        cachePanelGbc.weightx = 1.0;        
        cacheDirPanel.add(cacheDirLabel, cachePanelGbc);
        
        curCacheDirLabel = new JLabel(curCacheDirectory);
        curCacheDirLabel.setFont(new Font(curCacheDirLabel.getFont().getFontName(), Font.PLAIN, 10));
        cachePanelGbc.gridy = 1;
        cacheDirPanel.add(curCacheDirLabel, cachePanelGbc);
        
        cacheDirButton = new JButton(ElanLocale.getString("Button.Browse"));
        cachePanelGbc.gridy = 0;
        cachePanelGbc.gridx = 1; 
        cachePanelGbc.gridheight = 2;
        cachePanelGbc.insets = leftInset;
        cachePanelGbc.fill = GridBagConstraints.NONE;
        cachePanelGbc.weightx = 0.0;
        cacheDirPanel.add(cacheDirButton, cachePanelGbc);
        cacheDirButton.addActionListener(this);
        
        resetCacheDirButton = new JButton();
        if(resetIcon != null) {
        	resetCacheDirButton.setIcon(resetIcon);
        } else {
        	resetCacheDirButton.setText("X");
        }
        resetCacheDirButton.setToolTipText(ElanLocale.getString(
                "PreferencesDialog.Reset"));
        resetCacheDirButton.setPreferredSize(new Dimension(
        		resetCacheDirButton.getPreferredSize().width,
                cacheDirButton.getPreferredSize().height));        
        cachePanelGbc.gridx = 2;
        cacheDirPanel.add(resetCacheDirButton, cachePanelGbc);       
        resetCacheDirButton.addActionListener(this);
        
        // create panel for set tier set        
        tierSetCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.Edit.TierSet"),oriWorkWithTierSetFlag);
        
        tierSetCB.setFont(plainFont);
        
        setTSFileLabel = new JLabel(ElanLocale.getString(
                "PreferencesDialog.Prefs.DefaultTSFilePath"));
        setTSFileLabel.setFont(plainFont);   
        
        curTSFileLabel = new JLabel(curTSPath);
        curTSFileLabel.setFont(curDirLabel.getFont());
        
        defaultTSFileButton = new JButton(ElanLocale.getString("Button.Browse"));
        defaultTSFileButton.addActionListener(this);
        
        resetTSFileButton = new JButton();
        resetTSFileButton.addActionListener(this);
        if(resetIcon != null){
        	resetTSFileButton.setIcon(resetIcon);
        } else {
        	resetTSFileButton.setText("X");
        }
        
        resetTSFileButton.setToolTipText(ElanLocale.getString(
                "PreferencesDialog.Reset"));
        resetTSFileButton.setPreferredSize(resetDirButton.getPreferredSize());       
        
        JPanel tsFilePathPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 0;        
        gbc.insets = topInset;      
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; 
        tsFilePathPanel.add(tierSetCB, gbc);   

        gbc.gridy = 1;
        //gbc.insets = globalInset;
        tsFilePathPanel.add(setTSFileLabel, gbc);
        
        gbc.gridy = 2;
        tsFilePathPanel.add(curTSFileLabel, gbc);
        
        gbc.gridy = 1;
        gbc.gridx = 1; 
        gbc.gridheight = 2;
        gbc.insets = leftInset;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        tsFilePathPanel.add(defaultTSFileButton, gbc);  
        
        gbc.gridx = 2;
        tsFilePathPanel.add(resetTSFileButton, gbc);  
        
        //backup panel
        Integer[] nrOfBuItemsList = { 1, 2, 3, 4, 5 };
        nrOfBuFilesCB = new JComboBox(nrOfBuItemsList);
        nrOfBuFilesCB.setSelectedItem(origNumBuFiles);
        nrOfBuFilesCB.setFont(plainFont);        
        
        JLabel backUpLabel = new JLabel(ElanLocale.getString("PreferencesDialog.Prefs.NumBackUp"));
        backUpLabel.setFont(plainFont);
        
        JPanel backupPanel = new JPanel(new GridBagLayout());	
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;  
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.insets = topInset;
        backupPanel.add(backUpLabel, gbc);
    	
        gbc.gridx = 1;
        gbc.insets = leftInset;
        backupPanel.add(nrOfBuFilesCB, gbc);   
       
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.weightx = 1.0;
        backupPanel.add(new JPanel(), gbc); 
    	// main preferences panel 
    	
    	int gy = 0;
    	
    	gbc = new GridBagConstraints();
    	gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = gy++;
        gbc.insets = globalInset;        
        checkForUpdatesCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Prefs.AutoUpdate"), origCheckUpdates);
        checkForUpdatesCB.setFont(plainFont);
        outerPanel.add(checkForUpdatesCB, gbc);
        
        gbc.gridy = gy++;
        gbc.insets = globalPanelInset;
        outerPanel.add(backupPanel, gbc);
        
        saveAsOldFormatCB = new JCheckBox(ElanLocale.getString(
                "PreferencesDialog.Prefs.SaveAsOld"), origSaveAsOldFormatFlag);

        gbc.gridy = gy++;
        gbc.insets = globalInset;
        outerPanel.add(saveAsOldFormatCB, gbc);

        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Prefs.Location")),
            gbc);

        gbc.gridy = gy++;
        gbc.insets = catPanelInset;
        outerPanel.add(dirPanel, gbc);   
        
        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("MultiTierControlPanel.Menu.TierSet")),
            gbc);

        gbc.gridy = gy++;
        gbc.insets = catPanelInset;
        outerPanel.add(tsFilePathPanel, gbc);   
        
        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Prefs.CacheLocation")), gbc);
        
        gbc.gridy = gy++;
        gbc.insets = catPanelInset;
        outerPanel.add(cacheDirPanel, gbc);
        
        gbc.gridy = gy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        outerPanel.add(new JPanel(), gbc); // filler
    }

    /**
     * Returns the changed preferences.  return a map of changed preferences
     *
     * @return a map with changed preferences
     */
    @Override
	public Map<String, Object> getChangedPreferences() {
        if (isChanged()) {
            Map<String, Object> chMap = new HashMap<String, Object>(2);

            if ((curDirLabel.getText() != null) &&
                    !curDirLabel.getText().equals("-")) {
                chMap.put("DefaultPreferencesLocation", curDirLabel.getText());
            } else {
            	chMap.put("DefaultPreferencesLocation", null);
            }
            
            
            if ((curCacheDirLabel.getText() != null) &&
            		!curCacheDirLabel.getText().equals("-")) {
            	chMap.put("CacheLocation", curCacheDirLabel.getText());
            } else {
            	chMap.put("CacheLocation", null);
            }
            if ((curTSFileLabel.getText() != null) &&
                    !curTSFileLabel.getText().equals("-")) {
                chMap.put("DefaultTierSetFilePath", curTSFileLabel.getText());
            } else {
            	chMap.put("DefaultTierSetFilePath", null);
            }
            
            if (tierSetCB.isSelected() != oriWorkWithTierSetFlag) {
            	chMap.put("WorkwithTierSets", 
            		Boolean.valueOf(tierSetCB.isSelected()));
            }
            
            if (origNumBuFiles != nrOfBuFilesCB.getSelectedItem()) {
            	chMap.put("NumberOfBackUpFiles", nrOfBuFilesCB.getSelectedItem());
            }
            
            if (origCheckUpdates != checkForUpdatesCB.isSelected()) {
            	chMap.put("AutomaticUpdate", checkForUpdatesCB.isSelected());
            }

            if (saveAsOldFormatCB.isSelected() != origSaveAsOldFormatFlag) {
            	chMap.put("SaveAsOldEAF2_7", 
            		Boolean.valueOf(saveAsOldFormatCB.isSelected()));
            }
            
            return chMap;
        }

        return null;
    }

    /**
     * Returns whether or not anything changed.
     *
     * @return true if anything changed, false otherwise
     */
    @Override
	public boolean isChanged() {
        return !curGenPrefsLocation.equals(curDirLabel.getText()) ||
        		!curTSPath.equals(curTSFileLabel.getText()) ||
        		origNumBuFiles != nrOfBuFilesCB.getSelectedItem() ||
        		origCheckUpdates != checkForUpdatesCB.isSelected() ||
        		tierSetCB.isSelected() != oriWorkWithTierSetFlag ||
        		origSaveAsOldFormatFlag != saveAsOldFormatCB.isSelected() ||
        		!curCacheDirectory.equals(curCacheDirLabel.getText());
    }

    /**
     * Action event handling
     *
     * @param e the event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == defaultDirButton) {
            File selFile = showDirectoryChooser(ElanLocale.getString("PreferencesDialog.Media.DefaultLoc"), curGenPrefsLocation);
            if (selFile != null) {
                curDirLabel.setText(selFile.getAbsolutePath());
                curDirLabel.setText(FileUtility.pathToURLString(
                        selFile.getAbsolutePath()));
            }
        } else if (e.getSource() == resetDirButton) {
        	curDirLabel.setText("-");
        } else if (e.getSource() == cacheDirButton) {
        	File selFile = showDirectoryChooser(ElanLocale.getString("PreferencesDialog.Prefs.CacheDirectory"), curCacheDirectory);
            if (selFile != null && selFile.isDirectory()) {
                if (!CachedDataManager.containsCacheSubdirs(selFile)) {
					curCacheDirLabel.setText(selFile.getAbsolutePath());
					curCacheDirLabel.setText(FileUtility.pathToURLString(selFile.getAbsolutePath()));
				} else {
					JOptionPane.showMessageDialog(this,
							ElanLocale.getString("PreferencesDialog.Prefs.NewCacheDirContainsSubdirs"),
							ElanLocale.getString("Message.Error"),
						    JOptionPane.ERROR_MESSAGE);
				}
            }
        } else if (e.getSource() == resetCacheDirButton) {
        	curCacheDirLabel.setText("-");
        } else if (e.getSource() == resetTSFileButton) {
        	curTSFileLabel.setText("-");
        } else if (e.getSource() == defaultTSFileButton) {
            // show a folder file chooser, set the current def tier set file path
            FileChooser chooser = new FileChooser(this);

            File startDir = new File(System.getProperty("user.home"));
            String selectedFile = null;
            if (curTSPath.length() > 1) {
            	 File file = new File(FileUtility.urlToAbsPath(curTSPath));
                 if (file.exists() && file.isFile()) {
                	 selectedFile = file.getAbsolutePath();
                 }
            	
                File dir = new File(FileUtility.urlToAbsPath(
                		FileUtility.directoryFromPath(curTSPath)));
                if (dir.exists() && dir.isDirectory()) {
                    startDir = dir;
                }
            }

            chooser.setCurrentDirectory(startDir.getAbsolutePath());            
            chooser.createAndShowFileDialog(ElanLocale.getString("PreferencesDialog.Prefs.TierSetFilePath"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
            		null, null, true, null, FileChooser.FILES_ONLY, selectedFile);
            File selFile = chooser.getSelectedFile();
            if (selFile != null) {
                curTSFileLabel.setText(selFile.getAbsolutePath());
                curTSFileLabel.setText(FileUtility.pathToURLString(
                        selFile.getAbsolutePath()));
            }
        }
    }
    
    private File showDirectoryChooser(String title, String currentDirectory) {
    	// show a folder file chooser, set the current def. location
        FileChooser chooser = new FileChooser(this);

        File startDir = new File(System.getProperty("user.home"));
        if (currentDirectory.length() > 1) {
            File dir = new File(FileUtility.urlToAbsPath(
            		currentDirectory));

            if (dir.exists() && dir.isDirectory()) {
                startDir = dir;
            }
        }

        chooser.setCurrentDirectory(startDir.getAbsolutePath());            
        chooser.createAndShowFileDialog(title, FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
        		null, null, true, null, FileChooser.DIRECTORIES_ONLY, null);
        return chooser.getSelectedFile();
    }
}

/*
 * Options dialog to set all parameters for CHAT Export.
 *
 * @author Hennie Brugman
 */
package mpi.eudico.client.annotator.export;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.chat.CHATEncoderInfo;


/**
 * DOCUMENT ME!
 *
 * @author hennie
 */
@SuppressWarnings("serial")
public class CHATExportDlg extends ClosableDialog implements ActionListener {
    private final int NUM_OF_COLUMNS = 7;
    private final int NUM_OF_DEP_COLUMNS = 3;
    private final String MAIN_TIER = "Main Tier";
    private final String DEPENDENT_TIER = "Dependent Tier";
    private final String LABEL = "Label";
    private final String FULL_NAME = "Full Name";
    private final String ROLE = "Role";
    private final String ID = "ID";
    private final String LANGUAGE = "Language";
    private TranscriptionImpl transcription;
    private TranscriptionStore acmTranscriptionStore;
    private List<TierImpl> visibleTiers;
    private JLabel titleLabel;
    private JPanel titlePanel;
    private JComponent[][] mainTierTable;
    private JComponent[][] dependentTierTable;
    private JPanel mainTiersPanel;
    private JPanel dependentTiersPanel;
    private JPanel optionsPanel;
    private JPanel buttonPanel;
    private JButton exportButton;
    private TitledBorder mainTiersBorder;
    private TitledBorder dependentTiersBorder;
    private TitledBorder optionsBorder;
    private JCheckBox correctTimesCB;
    private JCheckBox timesOnSeparateLineCB;
    private JCheckBox includeLanguageLineCB;

    /**
     * Creates a new CHATExportDlg instance
     *
     * @param frame DOCUMENT ME!
     * @param modal DOCUMENT ME!
     * @param tr DOCUMENT ME!
     * @param acmTranscriptionStore DOCUMENT ME!
     * @param visibleTiers DOCUMENT ME!
     */
    public CHATExportDlg(JFrame frame, boolean modal, Transcription tr,
        TranscriptionStore acmTranscriptionStore, List<TierImpl> visibleTiers) {
        super(frame, modal);

        //this.frame = frame;
        transcription = (TranscriptionImpl) tr;
        this.acmTranscriptionStore = acmTranscriptionStore;
        this.visibleTiers = visibleTiers;

        // create main tier table (num of root tier records, NUM_OF_COLUMNS columns each)
        List<TierImpl> topTiers = transcription.getTopTiers();

        if (topTiers != null) {
            int numOfTiers = transcription.getTiers().size();
            mainTierTable = new JComponent[NUM_OF_COLUMNS][topTiers.size() + 1];
            dependentTierTable = new JComponent[NUM_OF_DEP_COLUMNS][numOfTiers -
                topTiers.size() + 1];
        }

        mainTiersPanel = new JPanel();
        dependentTiersPanel = new JPanel();
        buttonPanel = new JPanel();

        exportButton = new JButton();

        mainTiersBorder = new TitledBorder("Main tiers");
        dependentTiersBorder = new TitledBorder("Dependent tiers");

        createDialog();
        updateForLocale();
        setDefaultValues();
        pack();
        setLocationRelativeTo(getParent());
        //setResizable(false);
    }

    private void createDialog() {
        getContentPane().setLayout(new GridBagLayout());
        Insets insets = new Insets(4, 6, 4, 6);
        titleLabel = new JLabel();
        titlePanel = new JPanel();
        titlePanel.setLayout(new BorderLayout(0, 4));
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel titleLabelPanel = new JPanel();
        titleLabelPanel.add(titleLabel);
        titlePanel.add(titleLabelPanel, BorderLayout.NORTH);
        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(titlePanel, gridBagConstraints);
        
        mainTiersPanel.setLayout(new GridBagLayout());
        dependentTiersPanel.setLayout(new GridBagLayout());
        buttonPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        // main tiers panel
        JComponent tableComponent = null;

        JPanel mtPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(mtPanel);
        scrollPane.setBorder(null);
        mainTiersPanel.setBorder(mainTiersBorder);

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.insets = insets;

        //getContentPane().add(mainTiersPanel, c);
        mtPanel.add(mainTiersPanel, c);

        // header row
        c = new GridBagConstraints();
        tableComponent = new JLabel(MAIN_TIER);
        mainTierTable[1][0] = tableComponent;
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        mainTiersPanel.add(tableComponent, c);

        tableComponent = new JLabel(LABEL);
        mainTierTable[2][0] = tableComponent;
        c.gridx = 2;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        mainTiersPanel.add(tableComponent, c);

        tableComponent = new JLabel(FULL_NAME);
        mainTierTable[3][0] = tableComponent;
        c.gridx = 3;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        mainTiersPanel.add(tableComponent, c);

        tableComponent = new JLabel(ROLE);
        mainTierTable[4][0] = tableComponent;
        c.gridx = 4;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        mainTiersPanel.add(tableComponent, c);

        tableComponent = new JLabel(ID);
        mainTierTable[5][0] = tableComponent;
        c.gridx = 5;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        mainTiersPanel.add(tableComponent, c);

        tableComponent = new JLabel(LANGUAGE);
        mainTierTable[5][0] = tableComponent;
        c.gridx = 6;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        mainTiersPanel.add(tableComponent, c);

        // row for each top level tier
        List<TierImpl> topTiers = transcription.getTopTiers();

        if (topTiers != null) {
            for (int i = 0; i < topTiers.size(); i++) {
                String tName = topTiers.get(i).getName();

                tableComponent = new JCheckBox();
                ((JCheckBox) tableComponent).setSelected(true);
                mainTierTable[0][i + 1] = tableComponent;
                c.gridx = 0;
                c.gridy = i + 1;
                c.anchor = GridBagConstraints.WEST;
                c.insets = insets;
                mainTiersPanel.add(tableComponent, c);

                tableComponent = new JLabel(tName);
                mainTierTable[1][i + 1] = tableComponent;
                c.gridx = 1;
                c.gridy = i + 1;
                c.anchor = GridBagConstraints.WEST;
                c.insets = insets;
                mainTiersPanel.add(tableComponent, c);

                tableComponent = new JTextField(3);

                String defaultName = "*";

                if (tName.startsWith("*") && (tName.length() == 4)) {
                    defaultName = tName;
                }

                ((JTextField) tableComponent).setText(defaultName);
                mainTierTable[2][i + 1] = tableComponent;
                c.gridx = 2;
                c.gridy = i + 1;
                c.anchor = GridBagConstraints.WEST;
                c.insets = insets;
                mainTiersPanel.add(tableComponent, c);

                tableComponent = new JTextField(12);
                mainTierTable[3][i + 1] = tableComponent;
                c.gridx = 3;
                c.gridy = i + 1;
                c.anchor = GridBagConstraints.WEST;
                c.insets = insets;
                mainTiersPanel.add(tableComponent, c);

                tableComponent = new JTextField(8);
                mainTierTable[4][i + 1] = tableComponent;
                c.gridx = 4;
                c.gridy = i + 1;
                c.anchor = GridBagConstraints.WEST;
                c.insets = insets;
                mainTiersPanel.add(tableComponent, c);

                tableComponent = new JTextField(18);
                mainTierTable[5][i + 1] = tableComponent;
                c.gridx = 5;
                c.gridy = i + 1;
                c.anchor = GridBagConstraints.WEST;
                c.insets = insets;
                mainTiersPanel.add(tableComponent, c);

                tableComponent = new JTextField(8);

                Locale defLoc = topTiers.get(i).getDefaultLocale();
                String language = null;
                if (defLoc != null) {
                	language = defLoc.getLanguage();
                }
                                   

                if ((language != null) && !language.equals("")) {
                    ((JTextField) tableComponent).setText(language);
                }

                mainTierTable[6][i + 1] = tableComponent;
                c.gridx = 6;
                c.gridy = i + 1;
                c.anchor = GridBagConstraints.WEST;
                c.insets = insets;
                mainTiersPanel.add(tableComponent, c);
            }
        }
        // filler
        c.gridx = 7;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        mainTiersPanel.add(new JPanel(), c);
        
        // dependent tiers panel
        dependentTiersPanel.setBorder(dependentTiersBorder);

        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        c.weightx = 1.0;

        //getContentPane().add(dependentTiersPanel, c);
        mtPanel.add(dependentTiersPanel, c);

        // header row
        tableComponent = new JLabel(DEPENDENT_TIER);
        dependentTierTable[1][0] = tableComponent;
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        dependentTiersPanel.add(tableComponent, c);

        tableComponent = new JLabel(LABEL);
        dependentTierTable[1][0] = tableComponent;
        c.gridx = 2;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        dependentTiersPanel.add(tableComponent, c);

        // row for each dependent tier
        List<TierImpl> tiers = null;

        int rowIndex = 1;

        tiers = transcription.getTiers();

        if (tiers != null) {
            for (int i = 0; i < tiers.size(); i++) {
                TierImpl t = tiers.get(i);

                if (t.hasParentTier()) {
                    tableComponent = new JCheckBox();
                    ((JCheckBox) tableComponent).setSelected(true);
                    dependentTierTable[0][rowIndex] = tableComponent;
                    c.gridx = 0;
                    c.gridy = rowIndex;
                    c.anchor = GridBagConstraints.WEST;
                    c.insets = insets;
                    dependentTiersPanel.add(tableComponent, c);

                    tableComponent = new JLabel(t.getName());
                    dependentTierTable[1][rowIndex] = tableComponent;
                    c.gridx = 1;
                    c.gridy = rowIndex;
                    c.anchor = GridBagConstraints.WEST;
                    c.insets = insets;
                    dependentTiersPanel.add(tableComponent, c);

                    String defaultName = "%";

                    if (t.getName().startsWith("%")) {
                        int atInd = t.getName().indexOf('@');
                        if (atInd > 1 && atInd <= 8) {
                            defaultName = t.getName().substring(0, atInd);    
                        }
                    }

                    tableComponent = new JTextField(7);
                    ((JTextField) tableComponent).setText(defaultName);
                    dependentTierTable[2][rowIndex] = tableComponent;
                    c.gridx = 2;
                    c.gridy = rowIndex;
                    c.anchor = GridBagConstraints.WEST;
                    c.insets = insets;
                    dependentTiersPanel.add(tableComponent, c);

                    rowIndex++;
                }
            }
        }
        // filler
        c.gridx = 3;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        dependentTiersPanel.add(new JPanel(), c);
        
        optionsPanel = new JPanel(new GridBagLayout());
        optionsBorder = new TitledBorder("");
        optionsPanel.setBorder(optionsBorder);
        correctTimesCB = new JCheckBox();
        correctTimesCB.setSelected(true);
        timesOnSeparateLineCB = new JCheckBox();
        includeLanguageLineCB = new JCheckBox();
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = insets;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        optionsPanel.add(correctTimesCB, c);
       
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = insets;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        optionsPanel.add(timesOnSeparateLineCB, c);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = insets;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        optionsPanel.add(includeLanguageLineCB, c);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = insets;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 100.0;
        getContentPane().add(scrollPane, c);

        c.gridx = 0;
        c.gridy = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.insets = new Insets(2, 12, 2, 12);
        getContentPane().add(optionsPanel, c);
        
        c.gridx = 0;
        c.gridy = 3;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.SOUTH;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = insets;
        getContentPane().add(buttonPanel, c);

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = insets;
        buttonPanel.add(exportButton, c);
        
        setPreferredSetting();
        
    }

    private void setDefaultValues() {
        //		selectionOnlyCheckBox.setSelected(false);
        //		selectionOnlyCheckBox.setEnabled(false);
        //		selectionOnlyLabel.setEnabled(false);
        //		showTierLabelCheckBox.setSelected(interlinearizer.isTierLabelsShown());		
        //		widthTextField.setText(Integer.valueOf(initialWidth).toString());
    }

    /**
     * Update the UI elements according to the current Locale and the current
     * edit mode.
     */
    private void updateForLocale() {
        setTitle(ElanLocale.getString("ExportCHATDialog.Title"));
        titleLabel.setText(ElanLocale.getString("ExportCHATDialog.Title"));
        mainTiersBorder.setTitle(ElanLocale.getString(
                "ExportCHATDialog.MainTiers"));
        dependentTiersBorder.setTitle(ElanLocale.getString(
                "ExportCHATDialog.DependentTiers"));
        optionsBorder.setTitle(ElanLocale.getString("ExportDialog.Label.Options"));
        correctTimesCB.setText(ElanLocale.getString("ExportDialog.CorrectTimes"));
        timesOnSeparateLineCB.setText(ElanLocale.getString("ExportCHATDialog.SeparateLine"));
        includeLanguageLineCB.setText(ElanLocale.getString("ExportCHATDialog.LanguageLine"));
        exportButton.setText(ElanLocale.getString("ExportCHATDialog.Export"));
        exportButton.addActionListener(this);
    }

    //listeners
    @Override
	public void actionPerformed(ActionEvent event) {
        if (event.getSource() == exportButton) {
            if (fieldsOK()) {
                showDialogAndSave();
                
            }
        }
    }

    private boolean fieldsOK() {
        boolean ok = true;
        savePreferences();

        // main tier table: all fields have to start with * and have length 4
        for (int i = 1; i < mainTierTable[2].length; i++) {
            if (((JCheckBox) mainTierTable[0][i]).isSelected()) {
                String text = ((JTextField) mainTierTable[2][i]).getText();

                if ((text.length() != 4) || !text.startsWith("*")) {
                    ok = false;

                    break;
                }
            }
        }

        // dependent tier table: all fields have to start with % and have length 4
        // dec 2006  HS: depending tier names can be 1 to 7 chars long
        for (int i = 1; i < dependentTierTable[2].length; i++) {
            if (((JCheckBox) dependentTierTable[0][i]).isSelected()) {
                String text = ((JTextField) dependentTierTable[2][i]).getText();

                if ((text.length() < 2 || text.length() > 8) || !text.startsWith("%")) {
                    ok = false;

                    break;
                }
            }
        }

        if (!ok) { // give error message
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ExportCHATDlg.Message.WrongLabel"),
                ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
        }

        return ok;
    }

    private void showDialogAndSave() {   
    	// prompt for new file name
        String saveDir = Preferences.getString("LastUsedCHATDir", null);
        
        FileChooser chooser = new FileChooser(this);
        
        // open dialog at directory of original eaf file
        if (saveDir == null) {
        	saveDir = transcription.getFullPath();
        	if (saveDir.startsWith("file:")) {
        		saveDir = saveDir.substring(5);
			}
        	File d = new File(saveDir);
        	
            saveDir = d.getParent();

            if (saveDir != null) {
                chooser.setCurrentDirectory(saveDir);
            }
        }
        chooser.createAndShowFileDialog(ElanLocale.getString("ExportCHATDialog.Title"), FileChooser.SAVE_DIALOG,
        		FileExtension.CHAT_EXT, "LastUsedCHATDir");

        File f = chooser.getSelectedFile();
        if (f != null) {
              // collect encoder information to pass on
        	String[][] mainTierInfo = new String[mainTierTable.length - 1][mainTierTable[0].length -1];
            String[][] dependentTierInfo = new String[dependentTierTable.length -
                    1][dependentTierTable[0].length - 1];
            int index = 0;

            for (int i = 1; i < mainTierTable[1].length; i++) {
                if (((JCheckBox) mainTierTable[0][i]).isSelected()) {
                    mainTierInfo[0][index] = ((JLabel) mainTierTable[1][i]).getText();
                    mainTierInfo[1][index] = ((JTextField) mainTierTable[2][i]).getText();
                    mainTierInfo[2][index] = ((JTextField) mainTierTable[3][i]).getText();
                    mainTierInfo[3][index] = ((JTextField) mainTierTable[4][i]).getText();
                    mainTierInfo[4][index] = ((JTextField) mainTierTable[5][i]).getText();
                    mainTierInfo[5][index] = ((JTextField) mainTierTable[6][i]).getText();

                    index++;
                }
            }
            
            index = 0;

            for (int j = 1; j < dependentTierTable[1].length; j++) {
                if (((JCheckBox) dependentTierTable[0][j]).isSelected()) {
                    dependentTierInfo[0][index] = ((JLabel) dependentTierTable[1][j]).getText();
                    dependentTierInfo[1][index] = ((JTextField) dependentTierTable[2][j]).getText();

                    index++;
                }
            }
            
            CHATEncoderInfo encoderInfo = new CHATEncoderInfo(mainTierInfo,
                    dependentTierInfo);
            encoderInfo.setCorrectAnnotationTimes(correctTimesCB.isSelected());
            encoderInfo.setTimesOnSeparateLine(timesOnSeparateLineCB.isSelected());
            encoderInfo.setIncludeLangLine(includeLanguageLineCB.isSelected());
            
            if (correctTimesCB.isSelected()) {
                List<MediaDescriptor> mediaDescriptors = transcription.getMediaDescriptors();
                if (mediaDescriptors.size() > 0) {
                    encoderInfo.setMediaOffset( mediaDescriptors.get(0).timeOrigin);
                }
            }
            try {
            acmTranscriptionStore.storeTranscriptionIn(transcription,
                encoderInfo, visibleTiers, f.getAbsolutePath(),
                TranscriptionStore.CHAT);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this,
                        ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                        "(" + ioe.getMessage() + ")",
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Intializes the dialogBox with the last preferred/ used settings 
     *
     */
    private void setPreferredSetting()
    {
    	Boolean useTyp = Preferences.getBool("ExportCHATDialog.correctTimesCB", null);
    
    	if(useTyp != null){
    		correctTimesCB.setSelected(useTyp); 
    	}	
     
    	useTyp = Preferences.getBool("ExportCHATDialog.timesOnSeparateLineCB", null);
    	if(useTyp != null){
    		 timesOnSeparateLineCB.setSelected(useTyp); 
    	}
     
    	useTyp = Preferences.getBool("ExportCHATDialog.includeLanguageLineCB", null);
    	if(useTyp != null){
    		includeLanguageLineCB.setSelected(useTyp); 
    	}
    	
    	List<TierImpl> topTiers = transcription.getTopTiers(); 
    	if (topTiers != null) {
            for (int i = 0; i < topTiers.size(); i++) {     
            	String tiername = "ExportCHATDialog.mainTierTable." + topTiers.get(i).getName();
               	List<?> list = Preferences.getList(tiername, transcription);
               	if (list != null && list.size() == 6) {
               		((JCheckBox)mainTierTable[0][i + 1]).setSelected((Boolean) list.get(0));               			
               		((JTextField)mainTierTable[2][i + 1]).setText((String) list.get(1));                   
               		((JTextField)mainTierTable[3][i + 1]).setText((String) list.get(2));
               		((JTextField)mainTierTable[4][i + 1]).setText((String) list.get(3));
               		((JTextField)mainTierTable[5][i + 1]).setText((String) list.get(4)); 
               		((JTextField)mainTierTable[6][i + 1]).setText((String) list.get(5));
               	}
            }
    	}    	
    	
    	int rowIndex = 1;
   	 List<TierImpl> tiers = transcription.getTiers();
      if (tiers != null) {
          for (int i = 0; i < tiers.size(); i++) {
              TierImpl t = tiers.get(i);
              if (t.hasParentTier()) {   
             	 String tiername = "ExportCHATDialog.dependentTierTable." + tiers.get(i).getName();
                  List<?> list = Preferences.getList(tiername, transcription);
                  if (list != null && list.size() == 2) {
                 	((JCheckBox)dependentTierTable[0][rowIndex]).setSelected((Boolean) list.get(0));               			
                 	((JTextField)dependentTierTable[2][rowIndex]).setText((String) list.get(1)); 
                  }
                  rowIndex ++;
              }
      	 }
      }
    }
    
    /**
     * Saves the preferred settings Used. 
     *
     */
    private void savePreferences(){
    	Preferences.set("ExportCHATDialog.correctTimesCB", correctTimesCB.isSelected(), null);    
    	Preferences.set("ExportCHATDialog.timesOnSeparateLineCB",  timesOnSeparateLineCB.isSelected(), null);
    	Preferences.set("ExportCHATDialog.includeLanguageLineCB", includeLanguageLineCB.isSelected(), null);    	    	
    	
    	List<TierImpl> topTiers = transcription.getTopTiers(); 
        if (topTiers != null) {
        	for (int i = 0; i < topTiers.size(); i++) {   
        		String tiername = "ExportCHATDialog.mainTierTable."+ ((JLabel) mainTierTable[1][i + 1]).getText();
        		List<Object> list = new ArrayList<Object>();
        		list.add(((JCheckBox) mainTierTable[0][i + 1]).isSelected());
        		list.add(((JTextField)mainTierTable[2][i + 1]).getText());
        		list.add(((JTextField)mainTierTable[3][i + 1]).getText());
        		list.add(((JTextField)mainTierTable[4][i + 1]).getText());
        		list.add(((JTextField)mainTierTable[5][i + 1]).getText()); 
        		list.add(((JTextField)mainTierTable[6][i + 1]).getText()); 
        		Preferences.set(tiername, list, transcription);               
            }
        }
                
        int rowIndex = 1;
        List<TierImpl> tiers = transcription.getTiers();
        if (tiers != null) {
            for (int i = 0; i < tiers.size(); i++) {
                TierImpl t = tiers.get(i);
                if (t.hasParentTier()) {                	
                	String tiername = "ExportCHATDialog.dependentTierTable."+ ((JLabel) dependentTierTable[1][rowIndex]).getText();
            		ArrayList<Object> list = new ArrayList<Object>();
            		list.add(((JCheckBox) dependentTierTable[0][rowIndex]).isSelected());          
            		list.add(((JTextField)dependentTierTable[2][rowIndex]).getText());
            		Preferences.set(tiername, list, transcription);   
            		rowIndex++;
                }               
            }
        }       
    }
}

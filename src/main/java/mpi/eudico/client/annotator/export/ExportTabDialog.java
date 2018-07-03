package mpi.eudico.client.annotator.export;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.EmptyMediaPlayer;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.util.Transcription2TabDelimitedText;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.delimitedtext.DelimitedTextEncoderInfoFiles;
import mpi.eudico.server.corpora.clomimpl.delimitedtext.DelimitedTextEncoderInfoTrans;
import mpi.eudico.util.TimeFormatter;

/**
 * A dialog for exporting a set of tiers to a tab delimited text file. Provides
 * ui elements to customize the output.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ExportTabDialog extends AbstractExtTierExportDialog
    implements ChangeListener {
    private JCheckBox btCheckBox;
    private JCheckBox correctTimesCB;
    private JCheckBox suppressNamesCB;
    private JCheckBox suppressParticipantsCB;
    private JCheckBox colPerTierCB;
    private JCheckBox repeatValuesCB;
    private JCheckBox repeatOnlyWithinCB;
    private JCheckBox durCheckBox;
    private JCheckBox etCheckBox;
    private JCheckBox hhmmssmsCheckBox;
    private JCheckBox msCheckBox;
    private JCheckBox ssmsCheckBox;
    private JCheckBox timecodeCB;
    private JLabel timeCodesLabel;
    private JLabel timeFormatLabel;
    private JRadioButton ntscTimecodeRB;
    private JRadioButton palTimecodeRB;
    private JRadioButton pal50TimecodeRB;
    private JCheckBox includeFileNameCB;
    private JCheckBox includeFilePathCB;
    private JCheckBox includeCVEntryDesCB;
    // HS added Nov 2015
    private JCheckBox includeMediaHeaderCB;
    private JCheckBox fileNameInRowCB;
    // Aug 2017
    private JCheckBox slicedOutputCB;
    private JCheckBox includeAnnotationIdCB;
    
    private Insets insets = new Insets(2, 4, 2, 4);    

    /**
     * Creates a new ExportTabDialog2 instance
     *
     * @param parent the parent frame
     * @param modal the modal property
     * @param transcription the transcription to export
     * @param selection the selection object
     */
    public ExportTabDialog(Frame parent, boolean modal,
        TranscriptionImpl transcription, Selection selection) {
        super(parent, modal, transcription, selection);       
        makeLayout();
        extractTiers();
        postInit();      
    }

    /**
     * Creates a new ExportTabDialog2 instance
     *
     * @param parent the parent frame
     * @param modal the modal property
     * @param files the eaf files to export
     */
    public ExportTabDialog(Frame parent, boolean modal,
    		List<File> files) {
        super(parent, modal, files);
        makeLayout();
        extractTiersFromFiles();
        postInit();      
    }
    
    /**
     * Enables / disables PAL and NTSC radio buttons.
     * Enables / disables check boxes concerning column-per-tier output or not. 
     *
     * @param ce change event
     */
    @Override
	public void stateChanged(ChangeEvent ce) {
    	if (ce.getSource() == timecodeCB) {
	        palTimecodeRB.setEnabled(timecodeCB.isSelected());
	        pal50TimecodeRB.setEnabled(timecodeCB.isSelected());
	        ntscTimecodeRB.setEnabled(timecodeCB.isSelected());
    	} else if (ce.getSource() == colPerTierCB) {
    		repeatValuesCB.setEnabled(colPerTierCB.isSelected() && 
    				!slicedOutputCB.isSelected());
    		// update include tier name and tier participant checkboxes
    		suppressNamesCB.setEnabled(!colPerTierCB.isSelected());
    		suppressParticipantsCB.setEnabled(!colPerTierCB.isSelected());
    		if (!colPerTierCB.isSelected()) {
    			repeatOnlyWithinCB.setEnabled(false);
    		}
    		includeCVEntryDesCB.setEnabled(!colPerTierCB.isSelected());
    		slicedOutputCB.setEnabled(colPerTierCB.isSelected());
    		includeAnnotationIdCB.setEnabled(colPerTierCB.isSelected());
    	} else if (ce.getSource() == repeatValuesCB) {
    		repeatOnlyWithinCB.setEnabled(repeatValuesCB.isSelected());
    		includeAnnotationIdCB.setEnabled(
    				repeatValuesCB.isSelected() || includeAnnotationIdCB.isSelected());
    	} else if (ce.getSource() == slicedOutputCB) {
    		repeatValuesCB.setEnabled(!slicedOutputCB.isSelected());
    		repeatOnlyWithinCB.setEnabled(!slicedOutputCB.isSelected() && 
    				repeatValuesCB.isSelected());
    		includeAnnotationIdCB.setEnabled(
    				repeatValuesCB.isSelected() || includeAnnotationIdCB.isSelected());
    	}
    }
    
    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
    	List<String> stringsPref = Preferences.getListOfString("ExportTabDialog.TierOrder", transcription);
    	if (stringsPref != null) {
    		setTierOrder(stringsPref);        	
        } 
    	/*else {
        	super.extractTiers(false);
        }*/
    	
        stringsPref = Preferences.getListOfString("ExportTabDialog.selectedTiers", transcription);
        if (stringsPref != null) {
        	setSelectedTiers(stringsPref);
        }

        String stringPref = Preferences.getString("ExportTabDialog.SelectTiersMode", transcription);
        if (stringPref != null) {
        	//List list = (List) Preferences.get("ExportTabDialog.HiddenTiers", transcription);
        	//setSelectedMode((String)useTyp, list);
        	setSelectionMode(stringPref);
        	
        	if (!AbstractTierSortAndSelectPanel.BY_TIER.equals(stringPref) ) {
            	// call this after! the mode has been set
        		List<String> selItems  = Preferences.getListOfString("ExportTabDialog.LastSelectedItems", transcription);
            	
            	if (selItems != null) {
            		setSelectedItems(selItems);
            	}
        	}
         }
    }
    
    /**
     * Restore some global preferences
     */
	@Override
	protected void extractTiersFromFiles() {
		super.extractTiersFromFiles();
		
        // in case of multiple files, transcription is null and  the global preference will be loaded
        String useTyp = Preferences.getString("ExportTabDialog.SelectTiersMode", null);
        if (useTyp != null) {
        	//List list = (List) Preferences.get("ExportTabDialog.HiddenTiers", transcription);
        	//setSelectedMode((String)useTyp, list);
        	setSelectionMode(useTyp);
         }
	}

	/**
     * Initializes UI elements.
     */
    @Override
	protected void makeLayout() {
        super.makeLayout();

        // add more 
        timeCodesLabel = new JLabel();
        timeFormatLabel = new JLabel();
        btCheckBox = new JCheckBox();
        etCheckBox = new JCheckBox();
        durCheckBox = new JCheckBox();
        hhmmssmsCheckBox = new JCheckBox();
        ssmsCheckBox = new JCheckBox();
        msCheckBox = new JCheckBox();
        timecodeCB = new JCheckBox();
        palTimecodeRB = new JRadioButton();
        pal50TimecodeRB = new JRadioButton();
        ntscTimecodeRB = new JRadioButton();
        includeFileNameCB = new JCheckBox();
        includeFilePathCB = new JCheckBox();
        includeCVEntryDesCB = new JCheckBox();
        includeMediaHeaderCB = new JCheckBox();
        fileNameInRowCB = new JCheckBox();
        slicedOutputCB = new JCheckBox();
        slicedOutputCB.addChangeListener(this);
        includeAnnotationIdCB = new JCheckBox();
        
        ButtonGroup group = new ButtonGroup();
        correctTimesCB = new JCheckBox();
        suppressNamesCB = new JCheckBox();
        suppressParticipantsCB = new JCheckBox();
        colPerTierCB = new JCheckBox();
        repeatValuesCB = new JCheckBox();
        colPerTierCB.addChangeListener(this);
        repeatValuesCB.setSelected(true);
        repeatValuesCB.setEnabled(false);
        repeatValuesCB.addChangeListener(this);
        repeatOnlyWithinCB = new JCheckBox();
        repeatOnlyWithinCB.setSelected(false);
        repeatOnlyWithinCB.setEnabled(false);
       
        // options
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(restrictCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(correctTimesCB, gridBagConstraints);
       
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(includeMediaHeaderCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(suppressNamesCB, gridBagConstraints);

        // add suppress participant checkbox
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(suppressParticipantsCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(colPerTierCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 22, 2, 4);              	
        optionsPanel.add(repeatValuesCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 22, 2, 4);
        optionsPanel.add(repeatOnlyWithinCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 22, 2, 4);
        optionsPanel.add(slicedOutputCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 22, 2, 4);
        optionsPanel.add(includeAnnotationIdCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(includeCVEntryDesCB, gridBagConstraints);
        
        if(!multipleFileExport){        
        	JPanel fill = new JPanel();
        	Dimension fillDim = new Dimension(30, 10);
        	fill.setPreferredSize(fillDim);
        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 0;
        	gridBagConstraints.gridy = 11;
        	gridBagConstraints.gridwidth = 3;
        	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(fill, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 0;
        	gridBagConstraints.gridy = 12;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(timeCodesLabel, gridBagConstraints);

        	JPanel filler = new JPanel();
        	filler.setPreferredSize(fillDim);
        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 1;
        	gridBagConstraints.gridy = 12;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(filler, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 2;
        	gridBagConstraints.gridy = 12;
        	gridBagConstraints.gridwidth = 2;
        	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(timeFormatLabel, gridBagConstraints);       

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 0;
        	gridBagConstraints.gridy = 13;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(btCheckBox, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 2;
        	gridBagConstraints.gridy = 13;
        	gridBagConstraints.gridwidth = 2;
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(hhmmssmsCheckBox, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 0;
        	gridBagConstraints.gridy = 14;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(etCheckBox, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 2;
        	gridBagConstraints.gridy = 14;
        	gridBagConstraints.gridwidth = 2;
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(ssmsCheckBox, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 0;
        	gridBagConstraints.gridy = 15;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(durCheckBox, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 2;
        	gridBagConstraints.gridy = 15;
        	gridBagConstraints.gridwidth = 2;
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(msCheckBox, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 2;
        	gridBagConstraints.gridy = 16;
        	gridBagConstraints.gridwidth = 2;
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(timecodeCB, gridBagConstraints);

        	JPanel smptePanel = new JPanel();        	
        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 2;
        	gridBagConstraints.gridy = 17;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.gridheight = 2;
        	gridBagConstraints.insets = new Insets(2, 22, 2, 4);
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.weightx = 0.0;
        	optionsPanel.add(smptePanel, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 0;
        	gridBagConstraints.gridy = 0;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        	gridBagConstraints.weightx = 10.0;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = new Insets(2, 22, 2, 4);
        	smptePanel.add(palTimecodeRB, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 1;
        	gridBagConstraints.gridy = 0;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        	gridBagConstraints.weightx = 10.0;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = new Insets(2, 22, 2, 4);
        	smptePanel.add(pal50TimecodeRB, gridBagConstraints);
        	
        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 2;
        	gridBagConstraints.gridy = 0;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        	gridBagConstraints.weightx = 10.0;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = new Insets(2, 22, 2, 4);
        	smptePanel.add(ntscTimecodeRB, gridBagConstraints);
        }
        else {
        	gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 11;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(includeFileNameCB, gridBagConstraints);
            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 12;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;;
            optionsPanel.add(includeFilePathCB, gridBagConstraints);    
            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 13;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;;
            optionsPanel.add(fileNameInRowCB, gridBagConstraints);
            
            JPanel fill = new JPanel();
            Dimension fillDim = new Dimension(30, 10);
            fill.setPreferredSize(fillDim);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 14;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(fill, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 15;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(timeCodesLabel, gridBagConstraints);

            JPanel filler = new JPanel();
            filler.setPreferredSize(fillDim);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 15;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(filler, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 15;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(timeFormatLabel, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 16;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(btCheckBox, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 16;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(hhmmssmsCheckBox, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 17;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(etCheckBox, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 17;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(ssmsCheckBox, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 18;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(durCheckBox, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 18;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(msCheckBox, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 19;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(timecodeCB, gridBagConstraints);

            JPanel smptePanel = new JPanel();            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 20;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridheight = 2;
            gridBagConstraints.insets = new Insets(2, 22, 2, 4);
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 0.0;
            optionsPanel.add(smptePanel, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 10.0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(2, 22, 2, 4);
            smptePanel.add(palTimecodeRB, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 10.0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(2, 22, 2, 4);
            smptePanel.add(pal50TimecodeRB, gridBagConstraints);
            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 10.0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(2, 22, 2, 4);
            smptePanel.add(ntscTimecodeRB, gridBagConstraints);
        
        }
            
        group.add(palTimecodeRB);
        group.add(pal50TimecodeRB);
        group.add(ntscTimecodeRB);
        timecodeCB.addChangeListener(this);		
        
        setPreferencesOrDefaultSettings();
        
        if (transcription == null) {
        		// Nov 2015 correct times is available in multiple file export as well
        		//correctTimesCB.setEnabled(false);
        		//colPerTierCB.setEnabled(false);
        		restrictCheckBox.setEnabled(false);
        }       
        updateLocale();
    }

    /**
     * Starts the actual exporting process.
     *
     * @return true if export succeeded
     *
     * @throws IOException can occur when writing to the file
     * @throws NullPointerException DOCUMENT ME!
     */
    @Override
	protected boolean startExport() throws IOException {
        List<String> selectedTiers = getSelectedTiers();
        savePreferences();

        if (selectedTiers.size() == 0) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ExportTradTranscript.Message.NoTiers"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);

            return false;
        }
   
        // prompt for file name and location
        List<String[]> formats = new ArrayList<String[]>(2);
        formats.add(FileExtension.TEXT_EXT);
        formats.add(FileExtension.CSV_EXT);
        File exportFile = promptForFile(ElanLocale.getString("ExportTabDialog.Title"), 
        		formats, FileExtension.TEXT_EXT, true);

        if (exportFile == null) {
            return false;
        }
 
        boolean csvExport = exportFile.getName().toLowerCase().endsWith(
        		FileExtension.CSV_EXT[0]);
        // export....
        long selectionBT = 0L;
        long selectionET = Long.MAX_VALUE;

//        long mediaOffset = 0L;

        if (transcription != null) {
        	// this option only applies to single file export currently
            if (restrictCheckBox.isSelected()) {
                selectionBT = selection.getBeginTime();
                selectionET = selection.getEndTime();
            }

            DelimitedTextEncoderInfoTrans encInfoTrans = new DelimitedTextEncoderInfoTrans(transcription);
            encInfoTrans.setTierNames(selectedTiers);
            encInfoTrans.setExportFile(exportFile);
            encInfoTrans.setExportCSVFormat(csvExport);
            encInfoTrans.setCharEncoding(encoding);
            encInfoTrans.setBeginTime(selectionBT);
            encInfoTrans.setEndTime(selectionET);
            encInfoTrans.setAddMasterMediaOffset(correctTimesCB.isSelected());
            encInfoTrans.setIncludeCVDescription(includeCVEntryDesCB.isSelected());
            encInfoTrans.setIncludeBeginTime(btCheckBox.isSelected());
            encInfoTrans.setIncludeEndTime(etCheckBox.isSelected());
            encInfoTrans.setIncludeDuration(durCheckBox.isSelected());
            encInfoTrans.setIncludeHHMM(hhmmssmsCheckBox.isSelected());
            encInfoTrans.setIncludeSSMS(ssmsCheckBox.isSelected());
            encInfoTrans.setIncludeMS(msCheckBox.isSelected());
            encInfoTrans.setIncludeSMPTE(timecodeCB.isSelected());
            encInfoTrans.setPalFormat(palTimecodeRB.isSelected());
            encInfoTrans.setPal50Format(pal50TimecodeRB.isSelected());
            encInfoTrans.setIncludeNames(!suppressNamesCB.isSelected());
            encInfoTrans.setIncludeParticipants(!suppressParticipantsCB.isSelected());
            encInfoTrans.setIncludeAnnotationId(includeAnnotationIdCB.isSelected());
            
            if (includeMediaHeaderCB.isSelected()) {
            	encInfoTrans.setMediaHeaderLines(getMediaHeaderLines(transcription));
            }
            
	        if (colPerTierCB.isSelected()) {
	        	if (slicedOutputCB.isSelected()) {
	        		ExportTabdelimited et = new ExportTabdelimited();
	        		et.exportTiersSliced(encInfoTrans);
	        	} else if (!repeatValuesCB.isSelected()) {
	        		Transcription2TabDelimitedText tdt = new Transcription2TabDelimitedText();
	        		tdt.exportTiersColumnPerTier(encInfoTrans);            
	        	} else {
		            ExportTabdelimited et = new ExportTabdelimited();
		            encInfoTrans.setRepeatValues(true);
		            encInfoTrans.setCombineBlocks(!repeatOnlyWithinCB.isSelected());
		            et.exportTiersColumnPerTier(encInfoTrans);
	        	}            
	        } else { 
	        	Transcription2TabDelimitedText tdt = new Transcription2TabDelimitedText();
	        	tdt.exportTiers(encInfoTrans);
	        }
        } else {
        	DelimitedTextEncoderInfoFiles encInfoFiles = new DelimitedTextEncoderInfoFiles(files);
        	encInfoFiles.setTierNames(selectedTiers);
        	encInfoFiles.setExportFile(exportFile);
        	encInfoFiles.setExportCSVFormat(csvExport);
        	encInfoFiles.setCharEncoding(encoding);
        	encInfoFiles.setIncludeCVDescription(includeCVEntryDesCB.isSelected());
        	encInfoFiles.setIncludeBeginTime(btCheckBox.isSelected());
        	encInfoFiles.setIncludeEndTime(etCheckBox.isSelected());
        	encInfoFiles.setIncludeDuration(durCheckBox.isSelected());
        	encInfoFiles.setIncludeHHMM(hhmmssmsCheckBox.isSelected());
        	encInfoFiles.setIncludeSSMS(ssmsCheckBox.isSelected());
        	encInfoFiles.setIncludeMS(msCheckBox.isSelected());
        	encInfoFiles.setIncludeSMPTE(timecodeCB.isSelected());
        	encInfoFiles.setPalFormat(palTimecodeRB.isSelected());
        	encInfoFiles.setPal50Format(pal50TimecodeRB.isSelected());
        	encInfoFiles.setIncludeNames(!suppressNamesCB.isSelected());
        	encInfoFiles.setIncludeParticipants(!suppressParticipantsCB.isSelected());
        	encInfoFiles.setIncludeFileName(includeFileNameCB.isSelected());
        	encInfoFiles.setIncludeFilePath(includeFilePathCB.isSelected());
        	encInfoFiles.setAddMasterMediaOffset(correctTimesCB.isSelected());
        	encInfoFiles.setFileNameInRow(fileNameInRowCB.isSelected());
        	encInfoFiles.setIncludeMediaHeaders(includeMediaHeaderCB.isSelected());
        	encInfoFiles.setIncludeAnnotationId(includeAnnotationIdCB.isSelected());
        	
        	if (colPerTierCB.isSelected()) {
        		if (slicedOutputCB.isSelected()) {
	        		ExportTabdelimited et = new ExportTabdelimited();
	        		et.exportTiersSlicedForFiles(encInfoFiles);
	        	} else if (!repeatValuesCB.isSelected()) {
	        		Transcription2TabDelimitedText tdt = new Transcription2TabDelimitedText();
	        		tdt.exportTiersColumnPerTierFromFiles(encInfoFiles);
	        	} else {
		            ExportTabdelimited et = new ExportTabdelimited();
		            encInfoFiles.setRepeatValues(true);
		            encInfoFiles.setCombineBlocks(!repeatOnlyWithinCB.isSelected());
		            et.exportTiersColumnPerTierForFiles(encInfoFiles);
	        	}
        	} else {
        		Transcription2TabDelimitedText tdt = new Transcription2TabDelimitedText();
        		tdt.exportTiersFromFiles(encInfoFiles);
        	}
        } 
        
        return true;
    }

    /**
     * Set the localized text on ui elements.
     *
     * @see mpi.eudico.client.annotator.export.AbstractTierExportDialog#updateLocale()
     */
    @Override
	protected void updateLocale() {
    		super.updateLocale();
        setTitle(ElanLocale.getString("ExportTabDialog.Title"));
        titleLabel.setText(ElanLocale.getString("ExportTabDialog.TitleLabel"));
        correctTimesCB.setText(ElanLocale.getString("ExportDialog.CorrectTimes"));
        suppressNamesCB.setText(ElanLocale.getString(
        "ExportTabDialog.Label.SuppressNames"));
        suppressParticipantsCB.setText(ElanLocale.getString("ExportTabDialog.Label.SuppressParticipants"));
        colPerTierCB.setText(ElanLocale.getString("ExportTabDialog.Label.ColPerTier"));
        repeatValuesCB.setText(ElanLocale.getString("ExportTabDialog.Label.RepeatValues"));
        repeatOnlyWithinCB.setText(ElanLocale.getString("ExportTabDialog.Label.RepeatWithinBlock"));
        includeFileNameCB.setText(ElanLocale.getString("ExportTabDialog.Label.IncludeFileName"));
        includeFilePathCB.setText(ElanLocale.getString("ExportTabDialog.Label.IncludeFilePath"));        
        timeCodesLabel.setText(ElanLocale.getString(
                "ExportTabDialog.Label.Columns"));
        timeFormatLabel.setText(ElanLocale.getString(
                "ExportTabDialog.Label.Formats"));
        btCheckBox.setText(ElanLocale.getString(
                "Frame.GridFrame.ColumnBeginTime"));
        etCheckBox.setText(ElanLocale.getString("Frame.GridFrame.ColumnEndTime"));
        durCheckBox.setText(ElanLocale.getString(
                "Frame.GridFrame.ColumnDuration"));
        hhmmssmsCheckBox.setText(ElanLocale.getString("TimeCodeFormat.TimeCode"));
        ssmsCheckBox.setText(ElanLocale.getString("TimeCodeFormat.Seconds"));
        msCheckBox.setText(ElanLocale.getString("TimeCodeFormat.MilliSec"));
        timecodeCB.setText(ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE"));
        ntscTimecodeRB.setText(ElanLocale.getString(
                "TimeCodeFormat.TimeCode.SMPTE.NTSC"));
        palTimecodeRB.setText(ElanLocale.getString(
                "TimeCodeFormat.TimeCode.SMPTE.PAL"));
        pal50TimecodeRB.setText(ElanLocale.getString(
                "TimeCodeFormat.TimeCode.SMPTE.PAL50"));
        includeCVEntryDesCB.setText(ElanLocale.getString("ExportTabDialog.Label.IncludeCVDescription"));
        includeMediaHeaderCB.setText(ElanLocale.getString("ExportTabDialog.Label.IncludeMediaInfoHeader"));
        fileNameInRowCB.setText(ElanLocale.getString("ExportTabDialog.Label.FileNameInRow"));
        slicedOutputCB.setText(ElanLocale.getString("ExportTabDialog.Label.SlicedOutput"));
        includeAnnotationIdCB.setText(ElanLocale.getString("ExportTabDialog.Label.IncludeAnnotationID"));
     }  
    
    private List<String> getMediaHeaderLines(TranscriptionImpl trans) {
    	if (trans.getMediaDescriptors() != null && !trans.getMediaDescriptors().isEmpty()) {
    		List<String> headers = new ArrayList<String>(trans.getMediaDescriptors().size());
    		// get them from the players
			ViewerManager2 vm = ELANCommandFactory.getViewerManager(trans);
			ElanMediaPlayer pl = vm.getMasterMediaPlayer();
			if (!(pl instanceof EmptyMediaPlayer)) {
				headers.add(getMediaHeaderString(pl));

				for (ElanMediaPlayer spl : vm.getSlaveMediaPlayers()) {
					if (spl != null && spl.getMediaDescriptor() != null) {
						headers.add(getMediaHeaderString(spl));
					}
				}
			} else {
				// export from the descriptors
				for (MediaDescriptor md : trans.getMediaDescriptors()) {	
					headers.add("\"#" + md.mediaURL + " -- offset: " + md.timeOrigin + "\"");	    		
				}
			}
			
			return headers;
    	}
    	return null;
    }
    
    /**
     * Construct a media "header" string.
     * @param pl the player, not null
     * @return a descriptive "header"
     */
    private String getMediaHeaderString(ElanMediaPlayer pl) {
    	StringBuilder sb = new StringBuilder("\"#" + pl.getMediaDescriptor().mediaURL);
		sb.append(" -- offset: " + pl.getOffset());
		sb.append(", duration: ");
		sb.append(TimeFormatter.toString( pl.getMediaDuration()));	
		sb.append(" / ");
		sb.append(TimeFormatter.toSSMSString( pl.getMediaDuration()));
		sb.append(" / ");
		sb.append(pl.getMediaDuration());// ms
		if (pl.getVisualComponent() != null) {// video implied
			sb.append(", ms per sample: " + pl.getMilliSecondsPerSample());
		}
		sb.append("\"");
    	return sb.toString();
    }
    
    private void setPreferencesOrDefaultSettings()
    {
    	 Boolean boolPref = Preferences.getBool("ExportTabDialog.restrictCheckBox", null);
         if (boolPref != null) {
         	restrictCheckBox.setSelected(boolPref); 
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.correctTimesCB", null);
         if (boolPref != null) {
        	 correctTimesCB.setSelected(boolPref); 
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.includeMediaHeaderCB", null);
         if (boolPref != null) {
        	 includeMediaHeaderCB.setSelected(boolPref); 
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.suppressNamesCB", null);
         if (boolPref != null) {
        	 suppressNamesCB.setSelected(boolPref); 
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.suppressParticipantsCB", null);
         if (boolPref != null) {
        	 suppressParticipantsCB.setSelected(boolPref); 
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.colPerTierCB", null);
         if (boolPref != null) {
        	 colPerTierCB.setSelected(boolPref); 
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.repeatValuesCB", null);
         if (boolPref != null) {
        	 repeatValuesCB.setSelected(boolPref); 
         } else {
        	 repeatValuesCB.setSelected(true);
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.repeatOnlyWithinCB", null);
         if (boolPref != null) {
        	 repeatOnlyWithinCB.setSelected(boolPref); 
         }
         
         
         boolPref = Preferences.getBool("ExportTabDialog.btCheckBox", null);
         if (boolPref != null) {
        	 btCheckBox.setSelected(boolPref); 
         } else {
        	 btCheckBox.setSelected(true);
         }
                  
         boolPref = Preferences.getBool("ExportTabDialog.etCheckBox", null);
         if (boolPref != null) {
        	 etCheckBox.setSelected(boolPref); 
         } else {
        	 etCheckBox.setSelected(true);
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.durCheckBox", null);
         if (boolPref != null) {
        	 durCheckBox.setSelected(boolPref); 
         } else {
        	 durCheckBox.setSelected(true);
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.hhmmssmsCheckBox", null);
         if (boolPref != null) {
        	 hhmmssmsCheckBox.setSelected(boolPref); 
         } else {
        	 hhmmssmsCheckBox.setSelected(true);
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.ssmsCheckBox", null);
         if (boolPref != null) {
        	 ssmsCheckBox.setSelected(boolPref); 
         } else {
        	 ssmsCheckBox.setSelected(true);
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.msCheckBox", null);
         if (boolPref != null) {
        	 msCheckBox.setSelected(boolPref); 
         } else {
        	 msCheckBox.setSelected(false);
         }       
         
         boolPref = Preferences.getBool("ExportTabDialog.timecodeCB", null);
         if (boolPref != null) {
        	 timecodeCB.setSelected(boolPref); 
         } else {
        	 timecodeCB.setSelected(false);  
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.ntscTimecodeRB", null);
         if (boolPref != null) {
        	 ntscTimecodeRB.setSelected(boolPref); 
         }
          
         boolPref = Preferences.getBool("ExportTabDialog.pal50TimecodeRB", null);
         if (boolPref != null) {
        	 pal50TimecodeRB.setSelected(boolPref); 
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.palTimecodeRB", null);
         if (boolPref != null) {
        	 palTimecodeRB.setSelected(boolPref); 
         } else {
        	 palTimecodeRB.setSelected(true);
       	 }
         
         boolPref = Preferences.getBool("ExportTabDialog.includeFileNameCB", null);
         if (boolPref != null) {
        	 includeFileNameCB.setSelected(boolPref); 
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.IncludeCVDescription", null);
         if (boolPref != null) {
        	 includeCVEntryDesCB.setSelected(boolPref); 
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.includeFilePathCB", null);
         if (boolPref != null) {
        	 includeFilePathCB.setSelected(boolPref); 
         } else {
			includeFilePathCB.setSelected(true);
		 }
         
         boolPref = Preferences.getBool("ExportTabDialog.fileNameInRowCB", null);
         if (boolPref != null) {
        	 fileNameInRowCB.setSelected(boolPref); 
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.slicedOutputCB", null);
         if (boolPref != null) {
        	 slicedOutputCB.setSelected(boolPref);
         }
         
         boolPref = Preferences.getBool("ExportTabDialog.includeAnnotationIDCB", null);
         if (boolPref != null) {
        	 includeAnnotationIdCB.setSelected(boolPref);
         }         
         
         if( timecodeCB.isSelected()) {
        	 palTimecodeRB.setEnabled(true);
        	 pal50TimecodeRB.setEnabled(true);
             ntscTimecodeRB.setEnabled(true); 
         } else {
        	 palTimecodeRB.setEnabled(false);
        	 pal50TimecodeRB.setEnabled(false);
             ntscTimecodeRB.setEnabled(false); 
         }
    }
    
    private void savePreferences() {    		
    	Preferences.set("ExportTabDialog.restrictCheckBox", restrictCheckBox.isSelected(), null);    
    	Preferences.set("ExportTabDialog.correctTimesCB", correctTimesCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.suppressNamesCB", suppressNamesCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.suppressParticipantsCB", suppressParticipantsCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.colPerTierCB", colPerTierCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.repeatValuesCB", repeatValuesCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.repeatOnlyWithinCB", repeatOnlyWithinCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.btCheckBox", btCheckBox.isSelected(), null);
    	Preferences.set("ExportTabDialog.etCheckBox", etCheckBox.isSelected(), null);
    	Preferences.set("ExportTabDialog.durCheckBox", durCheckBox.isSelected(), null);
    	Preferences.set("ExportTabDialog.hhmmssmsCheckBox", hhmmssmsCheckBox.isSelected(), null);
    	Preferences.set("ExportTabDialog.ssmsCheckBox", ssmsCheckBox.isSelected(), null);
    	Preferences.set("ExportTabDialog.msCheckBox", msCheckBox.isSelected(), null);
    	Preferences.set("ExportTabDialog.timecodeCB", timecodeCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.ntscTimecodeRB", ntscTimecodeRB.isSelected(), null);
    	Preferences.set("ExportTabDialog.palTimecodeRB", palTimecodeRB.isSelected(), null);
    	Preferences.set("ExportTabDialog.pal50TimecodeRB", pal50TimecodeRB.isSelected(), null);
    	Preferences.set("ExportTabDialog.includeFileNameCB", includeFileNameCB.isSelected(), null);// multiple files
    	Preferences.set("ExportTabDialog.includeFilePathCB", includeFilePathCB.isSelected(), null);// multiple files
    	Preferences.set("ExportTabDialog.fileNameInRowCB", fileNameInRowCB.isSelected(), null);// multiple files
    	Preferences.set("ExportTabDialog.IncludeCVDescription", includeCVEntryDesCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.includeMediaHeaderCB", includeMediaHeaderCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.slicedOutputCB", slicedOutputCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.includeAnnotationIDCB", includeAnnotationIdCB.isSelected(), null);
    	
    	if(!multipleFileExport){
    		Preferences.set("ExportTabDialog.selectedTiers", getSelectedTiers(), transcription);      		
    		Preferences.set("ExportTabDialog.SelectTiersMode", getSelectionMode(), transcription);
        	// save the selected list in case on non-tier tab
        	if (getSelectionMode() != AbstractTierSortAndSelectPanel.BY_TIER) {
        		Preferences.set("ExportTabDialog.LastSelectedItems", getSelectedItems(), transcription);
        	}
    		Preferences.set("ExportTabDialog.HiddenTiers", getHiddenTiers(), transcription);
    	
    		List<String> tierOrder = getTierOrder();
    		Preferences.set("ExportTabDialog.TierOrder", tierOrder, transcription);
    		/*
        	List currentTierOrder = getCurrentTierOrder();    	    	
        	for(int i=0; i< currentTierOrder.size(); i++){
        		if(currentTierOrder.get(i) != tierOrder.get(i)){
        			Preferences.set("ExportTabDialog.TierOrder", currentTierOrder, transcription);
        			break;
        		}
        	}
        	*/   		
    	} else {
    		// set a global preference
    		Preferences.set("ExportTabDialog.SelectTiersMode", getSelectionMode(), null);
    	}
    }
}

    
    
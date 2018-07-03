package mpi.eudico.client.annotator.interlinear;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.PrintCommand;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.ShowHideMoreTiersDlg;
import mpi.eudico.client.annotator.tier.TierExportTable;
import mpi.eudico.client.annotator.tier.TierExportTableModel;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.util.TierSorter;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A dialog that shows a preview for interlinear printing or text export.
 * Contains numerous ui elements for configuring the output parameters.
 *
 * @author mpi
 */
@SuppressWarnings("serial")
public class InterlinearPreviewDlg extends ClosableDialog implements ActionListener,
    ItemListener {
    private Interlinear interlinearizer;
    private int curSortMode;

    // ui
    private PreviewPanel previewPanel;
    private JPanel optionsPanel;
    private JPanel whatPanel;
    private JPanel tierSelectionPanel;
    private JPanel howPanel;
    private JPanel buttonPanel;
    private JTable tierTable;
    private TierExportTableModel model;

    // tier panel
    private JButton fontSizesButton;
    private JButton upButton;
    private JButton downButton;
    private JButton advancedTierSelButton;

    //private HashMap fontSizes;
    // whatPanel
    private JCheckBox selectionOnlyCheckBox;
    private JCheckBox showTierLabelCheckBox;
    private JCheckBox showTimeCodeCheckBox;
    private JComboBox timeCodeFormatComboBox;
    private JCheckBox playSoundCheckBox;  //CC 26/11/2010
    private JCheckBox showEmptySlotsCheckBox;
    private JRadioButton hideLinesRadioButton;
    private JRadioButton tierTemplateRadioButton;
    private JCheckBox showPageNumberCB;
    private JCheckBox silenceCB;
    private JComboBox silenceDecimalComboBox;
    private JTextField minDurSilTF;
    private JLabel minDurSilLabel;
    private JLabel silDecimalLabel;

    // howPanel
    private JLabel widthLabel;
    private JTextField widthTextField;
    private JLabel heightLabel;
    private JTextField heightTextField;
    private JLabel blockWrapLabel;
    private JComboBox blockWrapComboBox;
    private JLabel lineWrapLabel;

    //private JComboBox lineWrapComboBox;
    private JCheckBox lineWrapCheckBox;
    private JLabel sortingLabel;
    private JComboBox sortingComboBox;
    private JLabel lineSpacingLabel;
    private JTextField lineSpacingTextField;
    private JLabel blockSpacingLabel;
    private JTextField blockSpacingTextField;
    private JCheckBox insertTabCheckBox;
    private JCheckBox tabsInsteadOfCheckBox;

    // button panel
    private JButton applyChangesButton;
    private JButton printButton;
    private JButton pageSetupButton;
    private JButton closeButton;

    // localized option strings
    private String tcStyleHhMmSsMs;
    private String tcStyleSsMs;
    private String tcStyleMs;
    private String blockWrapNone;
    private String blockWrapWithinBlock;
    private String blockWrapBlockBoundary;
    private String blockWrapEachBlock;
    private String sortingAsFile;
    private String sortingTierHierarchy;
    private String sortingTierName;
    private String sortingLinguisticType;
    private String sortingParticipant;
    private String sortingAnnotator;
    private String sortingLanguage;
    /** the character encoding for the text file */
	private String charEncoding = "UTF-8";

    /** column id for the include in export checkbox column, invisible */
    private final String PRINT_COLUMN = "export";

    /** column id for the tier name column, invisible */
    private final String TIER_NAME_COLUMN = "tier";

    /** column id for the font size column, invisible */
    private final String FONT_SIZE_COLUMN = "fontsize";    
 
    /**
     * Creates a new InterlinearPreviewDlg instance
     *
     * @param frame the parent frame
     * @param modal the modal property
     * @param interlinearizer the Interlinear object
     */
    public InterlinearPreviewDlg(Frame frame, boolean modal,
        Interlinear interlinearizer) {
        super(frame, modal);
        this.interlinearizer = interlinearizer;

        initComponents();
        setDefaultValues();
        extractTiers();
        doApplyChanges();
        postInit();
    }

    /**
     * Initializes UI elements.
     */
    protected void initComponents() {
        previewPanel = new PreviewPanel(interlinearizer);
        optionsPanel = new JPanel();
        tierSelectionPanel = new JPanel();
        whatPanel = new JPanel();
        howPanel = new JPanel();
        buttonPanel = new JPanel();
        fontSizesButton = new JButton();
        upButton = new JButton();
        downButton = new JButton();
        advancedTierSelButton = new JButton();

        //fontSizes = new HashMap();
        //	then: other components
        selectionOnlyCheckBox = new JCheckBox();
        showTierLabelCheckBox = new JCheckBox();
        showTimeCodeCheckBox = new JCheckBox();
        timeCodeFormatComboBox = new JComboBox();
        playSoundCheckBox = new JCheckBox(); 		//CC 26/11/2010 
        playSoundCheckBox.setVisible(false);
        showEmptySlotsCheckBox = new JCheckBox();
        showEmptySlotsCheckBox.setEnabled(false);
        hideLinesRadioButton = new JRadioButton();
        tierTemplateRadioButton = new JRadioButton();
        showPageNumberCB = new JCheckBox();
        silenceCB = new JCheckBox();
        silenceDecimalComboBox = new JComboBox();
        silenceDecimalComboBox.addItem(Constants.ONE_DIGIT);
        silenceDecimalComboBox.addItem(Constants.TWO_DIGIT);     
        silenceDecimalComboBox.addItem(Constants.THREE_DIGIT);     
        minDurSilTF = new JTextField(4);
        minDurSilLabel = new JLabel();
        silDecimalLabel = new JLabel();

        // components of howPanel
        widthLabel = new JLabel();
        widthTextField = new JTextField(4);
        heightLabel = new JLabel();
        heightTextField = new JTextField(4);
        blockWrapLabel = new JLabel();
        blockWrapComboBox = new JComboBox();
        lineWrapLabel = new JLabel();

        //lineWrapComboBox = new JComboBox();
        lineWrapCheckBox = new JCheckBox();
        sortingLabel = new JLabel();
        sortingComboBox = new JComboBox();
        lineSpacingLabel = new JLabel();
        lineSpacingTextField = new JTextField(2);
        blockSpacingLabel = new JLabel();
        blockSpacingTextField = new JTextField(2);

        // components of buttonPanel
        applyChangesButton = new JButton();
        printButton = new JButton();
        closeButton = new JButton();

        if (interlinearizer.getOutputMode() == Interlinear.PRINT) {
            pageSetupButton = new JButton();
        }

        // tier table and scrollpane, panel
        try {
            ImageIcon upIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Up16.gif"));
            ImageIcon downIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Down16.gif"));
            upButton.setIcon(upIcon);
            downButton.setIcon(downIcon);
        } catch (Exception ex) {
            upButton.setText("Up");
            downButton.setText("Down");
        }

        model = new TierExportTableModel();
        model.setColumnIdentifiers(new String[] {
                PRINT_COLUMN, TIER_NAME_COLUMN, FONT_SIZE_COLUMN
            });
        tierTable = new TierExportTable(model);

        Dimension tableDim = new Dimension(50, 100);

        tierTable.getColumn(FONT_SIZE_COLUMN).setMaxWidth(30);

        JScrollPane tierScrollPane = new JScrollPane(tierTable);
        tierScrollPane.setPreferredSize(tableDim);

        // layout
        getContentPane().setLayout(new GridBagLayout());
        optionsPanel.setLayout(new GridBagLayout());
        tierSelectionPanel.setLayout(new GridBagLayout());
        whatPanel.setLayout(new GridBagLayout());
        howPanel.setLayout(new GridBagLayout());
        buttonPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc;
        Insets insets = new Insets(2, 2, 2, 2);

        // add the preview panel
        gbc = new GridBagConstraints();
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;
        getContentPane().add(previewPanel, gbc);

        // fill and add the tier selection panel
        gbc = new GridBagConstraints();
        gbc.gridwidth = 4;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        tierSelectionPanel.add(tierScrollPane, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        tierSelectionPanel.add(upButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        tierSelectionPanel.add(downButton, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        tierSelectionPanel.add(advancedTierSelButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.EAST;
        tierSelectionPanel.add(fontSizesButton, gbc);

        gbc = new GridBagConstraints();
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        optionsPanel.add(tierSelectionPanel, gbc);

        // fill and add the "what" panel
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        //gbc.gridwidth = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        whatPanel.add(selectionOnlyCheckBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;

        //gbc.gridwidth = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        whatPanel.add(showTierLabelCheckBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;

        //gbc.gridwidth = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        whatPanel.add(showTimeCodeCheckBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;

        //gbc.gridwidth = 1;
        gbc.insets = new Insets(2,150,2,2);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        whatPanel.add(timeCodeFormatComboBox, gbc);
        
        // CC 26/11/2010
        if (interlinearizer.getOutputMode() == Interlinear.HTML) {
        	playSoundCheckBox.setVisible(true);
        	playSoundCheckBox.addItemListener(this);
        }
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        whatPanel.add(playSoundCheckBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;

        //gbc.gridwidth = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        whatPanel.add(showEmptySlotsCheckBox, gbc);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(hideLinesRadioButton);
        buttonGroup.add(tierTemplateRadioButton);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;

        //gbc.gridwidth = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        whatPanel.add(hideLinesRadioButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;

        //gbc.gridwidth = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        whatPanel.add(tierTemplateRadioButton, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;

        //gbc.gridwidth = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        whatPanel.add(showPageNumberCB, gbc);
        
        silenceCB.addItemListener(this);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;        
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        whatPanel.add(silenceCB, gbc); 
        
        minDurSilTF.setEnabled(false);
        minDurSilTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;        
        gbc.insets = new Insets(2, 30, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        whatPanel.add(minDurSilTF, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        //gbc.gridwidth = 2;
        gbc.insets = new Insets(2, 100, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        whatPanel.add(minDurSilLabel, gbc);       
        
        silenceDecimalComboBox.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;        
        gbc.insets = new Insets(2, 30, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        whatPanel.add(silenceDecimalComboBox, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        //gbc.gridwidth = 2;
        gbc.insets = new Insets(2, 100, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        whatPanel.add(silDecimalLabel, gbc);    

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        optionsPanel.add(whatPanel, gbc);

        // fill and add "how" panel
        gbc = new GridBagConstraints();
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        howPanel.add(widthLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        howPanel.add(widthTextField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        howPanel.add(heightLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        howPanel.add(heightTextField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        howPanel.add(blockWrapLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        howPanel.add(blockWrapComboBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        howPanel.add(lineWrapLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        //gbc.fill = GridBagConstraints.HORIZONTAL;
        //gbc.weightx = 1.0;	
        //howPanel.add(lineWrapComboBox, gbc);
        howPanel.add(lineWrapCheckBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        howPanel.add(sortingLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        howPanel.add(sortingComboBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        howPanel.add(lineSpacingLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        howPanel.add(lineSpacingTextField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        howPanel.add(blockSpacingLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        howPanel.add(blockSpacingTextField, gbc);

        if (interlinearizer.getOutputMode() == Interlinear.INTERLINEAR_TEXT) {
            insertTabCheckBox = new JCheckBox();
            insertTabCheckBox.addItemListener(this);
	        gbc = new GridBagConstraints();
	        gbc.gridx = 0;
	        gbc.gridy = 7;
	        gbc.gridwidth = 2;
	        gbc.insets = insets;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.weightx = 1.0;
	        howPanel.add(insertTabCheckBox, gbc);
	        tabsInsteadOfCheckBox = new JCheckBox();
	        tabsInsteadOfCheckBox.addItemListener(this);
	        tabsInsteadOfCheckBox.setEnabled(false);
	        gbc.gridy = 8;
	        howPanel.add(tabsInsteadOfCheckBox, gbc);
        }
    
        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        optionsPanel.add(howPanel, gbc);

        JScrollPane optionsScroll = new JScrollPane(optionsPanel);
        optionsScroll.setBorder(null);      
        
        // button panel
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        buttonPanel.add(applyChangesButton, gbc);

        if (interlinearizer.getOutputMode() == Interlinear.PRINT) {
            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.insets = insets;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            buttonPanel.add(pageSetupButton, gbc);
        }

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        buttonPanel.add(printButton, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        buttonPanel.add(closeButton, gbc);

//        gbc = new GridBagConstraints();
//        gbc.gridy = 3;
//        gbc.insets = insets;
//        gbc.anchor = GridBagConstraints.NORTHWEST;
//        optionsPanel.add(buttonPanel, gbc);

        // add the options panel
        gbc = new GridBagConstraints();
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        //gbc.fill = GridBagConstraints.VERTICAL;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.05;
        gbc.weighty = 1.0;
//        getContentPane().add(optionsPanel, gbc);
        getContentPane().add(optionsScroll, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        getContentPane().add(buttonPanel, gbc);
        
        // apply localized strings
        updateForLocale();

        // add listeners
        fontSizesButton.addActionListener(this);
        upButton.addActionListener(this);
        downButton.addActionListener(this);
        advancedTierSelButton.addActionListener(this);
        applyChangesButton.addActionListener(this);
        closeButton.addActionListener(this);
        sortingComboBox.addItemListener(this);

        if (interlinearizer.getOutputMode() == Interlinear.PRINT) {
            printButton.addActionListener(this);
            pageSetupButton.addActionListener(this);
        } else if (interlinearizer.getOutputMode() == Interlinear.INTERLINEAR_TEXT) {
            printButton.addActionListener(this);
            fontSizesButton.setEnabled(false);
            lineSpacingTextField.setEnabled(false);
            lineWrapCheckBox.addItemListener(this);
        } else if (interlinearizer.getOutputMode() == Interlinear.HTML) {
            printButton.addActionListener(this);
            lineWrapCheckBox.addItemListener(this);
        }

        addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent we) {
                    savePreferences(); 
                    interlinearizer.savePreferences();
                }
            });
    }

    /**
     * Pack, size and set location.
     */
    protected void postInit() {
        pack();

        int w = 550;
        int h = 400;
        setSize((getSize().width < w) ? w : getSize().width,
            (getSize().height < h) ? h : getSize().height);
        
        Container parent = getParent();
        if (parent != null) {
        	GraphicsConfiguration gc = parent.getGraphicsConfiguration();
        	if (gc != null) {
        		Rectangle rect = gc.getBounds();
        		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        		w = getSize().width;
        		h = getSize().height;
        		int nw = rect.width - insets.left - insets.right;
        		int nh = rect.height - insets.left - insets.right;
        		if (w > nw || h > nh) {
        			setSize((w > nw ? nw : w), (h > nh ? nh : h));
        		}
        	}
        }
        setLocationRelativeTo(parent);

        //setResizable(false);
    }

    /**
     * Initialise some default parameter values.
     */
    private void setDefaultValues() {
        curSortMode = interlinearizer.getSortingStyle();
        selectionOnlyCheckBox.setSelected(interlinearizer.isSelectionOnly());

        showTierLabelCheckBox.setSelected(interlinearizer.isTierLabelsShown());
        showTimeCodeCheckBox.setSelected(interlinearizer.isTimeCodeShown());
        playSoundCheckBox.setSelected(interlinearizer.isPlaySoundSel());	//CC 29/11/2010
        showPageNumberCB.setSelected(interlinearizer.isShowPageNumber());
        
        silenceCB.setSelected(interlinearizer.isShowSilenceDuration());
        silenceDecimalComboBox.setSelectedItem(interlinearizer.getNumOfDecimalDigits());
        minDurSilTF.setText(Integer.toString(interlinearizer.getMinSilenceDuration()));  

        if (interlinearizer.getOutputMode() == Interlinear.PRINT) {
            showEmptySlotsCheckBox.setSelected(false);
            showEmptySlotsCheckBox.setEnabled(true);
        } else if (interlinearizer.getOutputMode() == Interlinear.HTML) {
            showEmptySlotsCheckBox.setSelected(true);
            showEmptySlotsCheckBox.setEnabled(false);
            showPageNumberCB.setSelected(false);
            showPageNumberCB.setEnabled(false);
        } else {
            showEmptySlotsCheckBox.setSelected(false);
            showEmptySlotsCheckBox.setEnabled(false);   
            showPageNumberCB.setSelected(false);
            showPageNumberCB.setEnabled(false);
        }

        if (interlinearizer.getEmptyLineStyle() == Interlinear.HIDE_EMPTY_LINES) {
            hideLinesRadioButton.setSelected(true);
        } else {
            tierTemplateRadioButton.setSelected(true);
        }

        if (interlinearizer.getBlockWrapStyle() == Interlinear.BLOCK_BOUNDARY) {
            blockWrapComboBox.setSelectedItem(blockWrapBlockBoundary);
        } else if (interlinearizer.getBlockWrapStyle() == Interlinear.EACH_BLOCK) {
            blockWrapComboBox.setSelectedItem(blockWrapEachBlock);
        } else if (interlinearizer.getBlockWrapStyle() == Interlinear.NO_WRAP) {
            blockWrapComboBox.setSelectedItem(blockWrapNone);
        } else if (interlinearizer.getBlockWrapStyle() == Interlinear.WITHIN_BLOCKS) {
            blockWrapComboBox.setSelectedItem(blockWrapWithinBlock);
        }

        int initialWidth = interlinearizer.getWidth();
        int initialHeigth = interlinearizer.getHeight();

        if (initialWidth > 0) {
            widthTextField.setText("" + initialWidth);
        } else {
            widthTextField.setText("" + 0);
        }

        if (initialHeigth > 0) {
            heightTextField.setText("" + initialHeigth);
        } else {
            heightTextField.setText("" + 0);
        }
        
        int blockSpacing = interlinearizer.getBlockSpacing();
        
        if(blockSpacing > 0) {
        	blockSpacingTextField.setText(""+ blockSpacing);
        }

        if (interlinearizer.getOutputMode() == Interlinear.PRINT) { // width and height is determined by page setup, so fix it
            widthTextField.setEnabled(false);
            widthTextField.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            heightTextField.setEnabled(false);
            heightTextField.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        } else {
            heightTextField.setText("");
            heightTextField.setEnabled(false);
            heightTextField.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            lineSpacingTextField.setText("");
            lineSpacingTextField.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        }

        lineWrapCheckBox.setSelected(interlinearizer.getLineWrapStyle() == Interlinear.NEXT_LINE);
        if (interlinearizer.getOutputMode() == Interlinear.INTERLINEAR_TEXT || 
        		interlinearizer.getOutputMode() == Interlinear.HTML) {
	        if (interlinearizer.getLineWrapStyle() == Interlinear.NO_WRAP) {
	        	widthTextField.setEnabled(false);
	        }
        }
        if (interlinearizer.getOutputMode() == Interlinear.INTERLINEAR_TEXT) {
            insertTabCheckBox.setSelected(interlinearizer.isInsertTabs());
            tabsInsteadOfCheckBox.setSelected(interlinearizer.isTabsReplaceSpaces());
        }
    }

    /**
     * Extract candidate tiers for export and add them to the table. Take saved
     * preferences into account.
     */
    protected void extractTiers() {
        if (model != null) {
            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                model.removeRow(i);
            }

            if (interlinearizer.getTranscription() != null) {            	
            	List<TierImpl> v ;
            	List<String> tierOrder = ELANCommandFactory.getViewerManager(interlinearizer.getTranscription()).getTierOrder().getTierOrder();
            	if(tierOrder != null && tierOrder.size() > 0){
            		v = new ArrayList<TierImpl>();
            		for (int i = 0; i < tierOrder.size(); i++) {
            			TierImpl t = interlinearizer.getTranscription().getTierWithId(tierOrder.get(i));
            			if (t != null) {
            				v.add(t);
            			}
            		}
            	} else {            		
            		v = interlinearizer.getTranscription().getTiers();
            	}   
               
                List<Tier> visTiers = interlinearizer.getVisibleTiers();
                List<String> sortedTiers = null;
                List<TierImpl> allTiers = new ArrayList<TierImpl>();

                // read preferences
                sortedTiers = Preferences.getListOfString(interlinearizer.prefTierOrder,
                        interlinearizer.getTranscription());

                if (sortedTiers != null) {

                    for (String stierName : sortedTiers) {
                    	TierImpl t = interlinearizer.getTranscription()
                                                    .getTierWithId(stierName);

                        if (t != null) {
                            allTiers.add(t);
                        }
                    }
                }

                for (TierImpl t : v) {
                    if (!allTiers.contains(t)) {
                        allTiers.add(t);
                    }
                }

                for (TierImpl t : allTiers) {
                    String tierName = t.getName();
                    Integer fontSize = Integer.valueOf(interlinearizer.getFontSize(tierName));

                    //fontSizes.put(tierName, fontSize);
                    model.addRow(Boolean.valueOf(visTiers.contains(t)), tierName, fontSize);
                }
            }

            if (model.getRowCount() > 1) {
                upButton.setEnabled(true);
                downButton.setEnabled(true);
            } else {
                upButton.setEnabled(false);
                downButton.setEnabled(false);
            }
        } else {
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        }
    }

    /**
     * Save some user prefs to the preferences file.
     */
    public void savePreferences() {    	
        List<String> allTiers = new ArrayList<String>();

        if (model != null) {
            String name = null;
            int tierCol = model.findColumn(TIER_NAME_COLUMN);

            for (int i = 0; i < model.getRowCount(); i++) {
                name = (String) model.getValueAt(i, tierCol);
                allTiers.add(name);
            }

            Preferences.set(interlinearizer.prefTierOrder, allTiers,
                interlinearizer.getTranscription());
        }
    }

    /**
     * Update the UI elements according to the current Locale and the current
     * edit mode.
     */
    private void updateForLocale() {
        setTitle(ElanLocale.getString("InterlinearizerOptionsDlg.Title"));

        tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "InterlinearizerOptionsDlg.Tiers")));
        whatPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "InterlinearizerOptionsDlg.What")));
        howPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "InterlinearizerOptionsDlg.How")));

        fontSizesButton.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.FontSizes"));
        
        advancedTierSelButton.setText(ElanLocale.getString(
        		"ExportDialog.AdvacedSelectionOptions"));

        selectionOnlyCheckBox.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.SelectionOnly"));
        showTierLabelCheckBox.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.ShowTierLabels"));
        showTimeCodeCheckBox.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.ShowTimeCode"));
        //CC 26/11/2010
        playSoundCheckBox.setText(ElanLocale.getString(
        		"InterlinearizerOptionsDlg.PlaySound"));
        showEmptySlotsCheckBox.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.ShowEmptySlots"));

        tcStyleHhMmSsMs = ElanLocale.getString(
                "TimeCodeFormat.TimeCode");
        tcStyleSsMs = ElanLocale.getString(
                "TimeCodeFormat.Seconds");
        tcStyleMs = ElanLocale.getString(
                "TimeCodeFormat.MilliSec");
        timeCodeFormatComboBox.addItem(tcStyleHhMmSsMs);
        timeCodeFormatComboBox.addItem(tcStyleSsMs);
        timeCodeFormatComboBox.addItem(tcStyleMs);

        hideLinesRadioButton.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.HideLines"));
        tierTemplateRadioButton.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.TierTemplate"));
        showPageNumberCB.setText(ElanLocale.getString("InterlinearizerOptionsDlg.ShowPageNumbers"));
        silenceCB.setText(ElanLocale.getString(
        	"InterlinearizerOptionsDlg.IncludeSilence"));
        minDurSilLabel.setText(ElanLocale.getString(
        		"InterlinearizerOptionsDlg.MinSilenceDuration"));   
        silDecimalLabel.setText(ElanLocale.getString(
        		"InterlinearizerOptionsDlg.NumberofDigits"));
        
        // "how" panel
        widthLabel.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.Width"));
        heightLabel.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.Height"));
        blockWrapLabel.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.BlockWrap"));

        blockWrapNone = ElanLocale.getString(
                "InterlinearizerOptionsDlg.BlockWrap.None");
        blockWrapWithinBlock = ElanLocale.getString(
                "InterlinearizerOptionsDlg.BlockWrap.WithinBlock");
        blockWrapBlockBoundary = ElanLocale.getString(
                "InterlinearizerOptionsDlg.BlockWrap.BlockBoundary");
        blockWrapEachBlock = ElanLocale.getString(
                "InterlinearizerOptionsDlg.BlockWrap.EachBlock");

        blockWrapComboBox.addItem(blockWrapNone);
        blockWrapComboBox.addItem(blockWrapWithinBlock);
        blockWrapComboBox.addItem(blockWrapBlockBoundary);
        blockWrapComboBox.addItem(blockWrapEachBlock);

        lineWrapLabel.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.LineWrap"));

        sortingLabel.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.Sorting"));
        sortingAsFile = ElanLocale.getString(
                "InterlinearizerOptionsDlg.Sorting.AsFile");
        sortingComboBox.addItem(sortingAsFile);

        sortingTierHierarchy = ElanLocale.getString(
                "InterlinearizerOptionsDlg.Sorting.TierHierarchy");
        sortingComboBox.addItem(sortingTierHierarchy);

        sortingTierName = ElanLocale.getString(
                "InterlinearizerOptionsDlg.Sorting.ByTierName");
        sortingComboBox.addItem(sortingTierName);

        sortingLinguisticType = ElanLocale.getString(
                "InterlinearizerOptionsDlg.Sorting.ByType");
        sortingComboBox.addItem(sortingLinguisticType);

        sortingParticipant = ElanLocale.getString(
                "InterlinearizerOptionsDlg.Sorting.ByParticipant");
        sortingComboBox.addItem(sortingParticipant);

        sortingAnnotator = ElanLocale.getString(
                "InterlinearizerOptionsDlg.Sorting.ByAnnotator");
        sortingComboBox.addItem(sortingAnnotator);

        sortingLanguage = ElanLocale.getString(
                "InterlinearizerOptionsDlg.Sorting.ByLanguage");
        sortingComboBox.addItem(sortingLanguage);
        
        lineSpacingLabel.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.LineSpacing"));
        blockSpacingLabel.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.BlockSpacing"));
        if (insertTabCheckBox != null) {
	        insertTabCheckBox.setText(ElanLocale.getString(
	                "InterlinearizerOptionsDlg.InsertTab"));
        }
        if (tabsInsteadOfCheckBox != null) {
        	tabsInsteadOfCheckBox.setText(ElanLocale.getString(
        		"InterlinearizerOptionsDlg.TabsInsteadOfSpaces"));
        }
        // components of buttonPanel
        applyChangesButton.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.ApplyChanges"));
        closeButton.setText(ElanLocale.getString("Button.Close"));

        if (interlinearizer.getOutputMode() == Interlinear.PRINT) {
            pageSetupButton.setText(ElanLocale.getString("Menu.File.PageSetup"));
            printButton.setText(ElanLocale.getString("Menu.File.Print"));
        } else {
            printButton.setText(ElanLocale.getString("Menu.File.SaveAs"));
        }
    }

    /**
     * Moves selected tiers up in the list of tiers.
     */
    private void moveUp() {
        if ((tierTable == null) || (model == null) ||
                (model.getRowCount() < 2)) {
            return;
        }

        int[] selected = tierTable.getSelectedRows();

        for (int element : selected) {
            int row = element;

            if ((row > 0) && !tierTable.isRowSelected(row - 1)) {
                model.moveRow(row, row, row - 1);
                tierTable.changeSelection(row, 0, true, false);
                tierTable.changeSelection(row - 1, 0, true, false);
            }
        }
    }

    /**
     * Moves selected tiers up in the list of tiers.
     */
    private void moveDown() {
        if ((tierTable == null) || (model == null) ||
                (model.getRowCount() < 2)) {
            return;
        }

        int[] selected = tierTable.getSelectedRows();

        for (int i = selected.length - 1; i >= 0; i--) {
            int row = selected[i];

            if ((row < (model.getRowCount() - 1)) &&
                    !tierTable.isRowSelected(row + 1)) {
                model.moveRow(row, row, row + 1);
                tierTable.changeSelection(row, 0, true, false);
                tierTable.changeSelection(row + 1, 0, true, false);
            }
        }
    }

    /**
     * Sorting of the tiers in the table.
     */
    private void sortTiersTable() {
        if ((tierTable == null) || (model == null) ||
                (model.getRowCount() < 2)) {
            return;
        }

        // first store the current order
        List<TierImpl> curOrder = new ArrayList<TierImpl>();
        String name = null;
        TierImpl t = null;
        int col = model.findColumn(TIER_NAME_COLUMN);

        for (int i = 0; i < model.getRowCount(); i++) {
            name = (String) model.getValueAt(i, col);

            t = interlinearizer.getTranscription().getTierWithId(name);
            curOrder.add(t);
        }

        TierSorter sorter = new TierSorter(interlinearizer.getTranscription());
        List<TierImpl> newSort = null;
        int order = -1;

        switch (curSortMode) {
        case Interlinear.EXTERNALLY_SPECIFIED:
            newSort = sorter.sortTiers(sorter.UNSORTED);
            break;
        case Interlinear.TIER_HIERARCHY:
            order = sorter.BY_HIERARCHY;
            break;
        case Interlinear.BY_NAME:
            order = sorter.BY_NAME;
            break;
        case Interlinear.BY_LINGUISTIC_TYPE:
            order = sorter.BY_LINGUISTIC_TYPE;
            break;
        case Interlinear.BY_PARTICIPANT:
            order = sorter.BY_PARTICIPANT;
            break;
        case Interlinear.BY_ANNOTATOR:
            order = sorter.BY_ANNOTATOR;
            break;
        case Interlinear.BY_LANGUAGE:
            order = sorter.BY_LANGUAGE;
            break;
        }
        
        if (order >= 0) {
            newSort = sorter.sortTiers(order, curOrder);        	
        } else {
        	return; // should not happen
        }

        // apply new order to the table
        String cellName;

        for (int i = 0; i < newSort.size(); i++) {
            t = newSort.get(i);

            name = t.getName();

            for (int j = 0; j < model.getRowCount(); j++) {
                cellName = (String) model.getValueAt(j, col);

                if ((cellName != null) && cellName.equals(name)) {
                    model.moveRow(j, j, i);

                    break;
                }
            }
        }

        tierTable.clearSelection();
    }

    /**
     * Returns the tiers that are marked visible in the tier table.
     *
     * @return the current selected tiers in the table, a list of TierImpl
     *         objects
     */
    private List<Tier> getSelectedTierList() {
        List<Tier> vt = new ArrayList<Tier>();

        if (model != null) {
        	final TranscriptionImpl transcription = interlinearizer.getTranscription();
        	List<String> names = model.getSelectedTiers();
        	
        	for (String name : names) {
				TierImpl t = transcription.getTierWithId(name);
            	vt.add(t);
            }
        }

        return vt;
    }
    
    /**
     * Returns the current tierOrder of this export
     * 
     * @ return  tierOrder
     */
    protected List<String> getCurrentTierOrder() {  
    	List<String> tierOrder = new ArrayList<String>();
    	
    	int tierCol = model.findColumn(TIER_NAME_COLUMN);
    	
        for (int i = 0; i < model.getRowCount(); i++) { 
        	tierOrder.add((String)model.getValueAt(i, tierCol));            
        }
        
        return tierOrder;
    }
   
    /**
     * Select the given list of tiers in the table
     * 
     * @param visibleTiers, list of tiers to be selected
     */
    private void selectTiers(List<String> visibleTiers) { 
    	if (model != null && visibleTiers != null) {          
    		int tierCol = model.findColumn(TIER_NAME_COLUMN);
    		int visCol = model.findColumn(PRINT_COLUMN);
    		Object value;
    		for (int i = 0; i < model.getRowCount(); i++) {
    			value = model.getValueAt(i, tierCol);
    			if(visibleTiers.contains(value)){
    				model.setValueAt(Boolean.TRUE, i, visCol);
    			} else{
    				model.setValueAt(Boolean.FALSE, i, visCol);	
    			}
    		}
    	}  
    }

    /**
     * Update the Interlinear object with the current font sizes.
     */
    private void applyFontSizes() {
        if (model != null) {
            String name = null;
            Integer size = null;
            int tierCol = model.findColumn(TIER_NAME_COLUMN);
            int sizeCol = model.findColumn(FONT_SIZE_COLUMN);

            for (int i = 0; i < model.getRowCount(); i++) {
                name = (String) model.getValueAt(i, tierCol);
                size = (Integer) model.getValueAt(i, sizeCol);

                if ((name != null) && (size != null)) {
                    if (size.intValue() != interlinearizer.getFontSize(name)) {
                        interlinearizer.setFontSize(name, size.intValue());
                    }
                }
            }
        }
    }

    /**
     * Change the font sizes. Pass a HashMap with tier names and font sizes to
     * the dialog.
     */
    private void doSetFontSizes() {
        List<String> fontNames = new ArrayList<String>();
        Map<String, Integer> fontMap = new HashMap<String, Integer>();

        if (model != null) {
            int tierCol = model.findColumn(TIER_NAME_COLUMN);
            int fontCol = model.findColumn(FONT_SIZE_COLUMN);
            String name = null;
            Integer size = null;

            for (int i = 0; i < model.getRowCount(); i++) {
                name = (String) model.getValueAt(i, tierCol);
                size = (Integer) model.getValueAt(i, fontCol);
                fontNames.add(name);
                fontMap.put(name, size);
            }

            JDialog dlg = new TierFontSizeDlg(this, true, fontMap, fontNames);
            dlg.setLocationRelativeTo(tierTable);
            dlg.setVisible(true);

            // update the table
            Iterator<String> tierIt = fontMap.keySet().iterator();
            name = null;
            size = null;

            while (tierIt.hasNext()) {
                name = tierIt.next();

                if (name != null) {
                    size = fontMap.get(name);

                    if (size != null) {
                        Object ttName;

                        for (int i = 0; i < model.getRowCount(); i++) {
                            ttName = model.getValueAt(i, tierCol);

                            if (name.equals(ttName)) {
                                model.setValueAt(size, i, fontCol);

                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates the interlinear layout and the preview after (possible) changes
     * in parameters.
     */
    private void doApplyChanges() {
        // set parameters	
    	String tcStyleString = (String) timeCodeFormatComboBox.getSelectedItem();
    	
        interlinearizer.setSelectionOnly(selectionOnlyCheckBox.isSelected());
        interlinearizer.setTierLabelsShown(showTierLabelCheckBox.isSelected());
        interlinearizer.setTimeCodeShown(showTimeCodeCheckBox.isSelected());
        interlinearizer.setShowPageNumber(showPageNumberCB.isSelected());
        // CC 26/11/2010
        interlinearizer.setPlaySoundSel(playSoundCheckBox.isSelected());
        if (interlinearizer.isPlaySoundSel() && interlinearizer.getOutputMode() == Interlinear.HTML){
        	tcStyleString=tcStyleSsMs;
        	//interlinearizer.setTimeCodeShown(true);
        }
        interlinearizer.setShowSilenceDuration(silenceCB.isSelected());
        
     // check the minimal silence duration
        int minSilence = Interlinear.MIN_SILENCE;
        if (silenceCB.isSelected()) {
        	
            String textValue = minDurSilTF.getText().trim();
            
            try {
                minSilence = Integer.parseInt(textValue);
                int  numOfDigits = Integer.parseInt(silenceDecimalComboBox.getSelectedItem().toString());
                interlinearizer.setMinSilenceDuration(minSilence);
                interlinearizer.setNumOfDecimalDigits(numOfDigits);

            } catch (NumberFormatException nfe) {            	
            	JOptionPane.showMessageDialog(this,
                        ElanLocale.getString("InterlinearizerOptionsDlg.Message.InvalidNumber"),
                        ElanLocale.getString("Message.Warning"),
                        JOptionPane.WARNING_MESSAGE);                
                minDurSilTF.selectAll();
                minDurSilTF.requestFocus();
                return ;                
            }
        }

        if (tcStyleString.equals(tcStyleHhMmSsMs)) {
            interlinearizer.setTimeCodeType(Interlinear.HHMMSSMS);
        } else if (tcStyleString.equals(tcStyleSsMs)) {
            interlinearizer.setTimeCodeType(Interlinear.SSMS);
        } else if (tcStyleString.equals(tcStyleMs)) {
            interlinearizer.setTimeCodeType(Interlinear.MS);
        }

        interlinearizer.setEmptySlotsShown(showEmptySlotsCheckBox.isSelected());
        
        if (hideLinesRadioButton.isSelected()) {
            interlinearizer.setEmptyLineStyle(Interlinear.HIDE_EMPTY_LINES);
        } else {
            interlinearizer.setEmptyLineStyle(Interlinear.TEMPLATE);
        }

        int imageWidth = 0;

        //int imageHeight = 0;
        if (interlinearizer.getOutputMode() == Interlinear.PRINT) {
            int pageWidth = new Double(PrintCommand.pageFormat.getImageableWidth()).intValue(); //width of printer page	
            widthTextField.setText(Integer.toString(pageWidth));
            interlinearizer.setWidth(pageWidth);

            int pageHeight = new Double(PrintCommand.pageFormat.getImageableHeight()).intValue();
            heightTextField.setText("" + pageHeight);
            interlinearizer.setPageHeight(pageHeight);
        } else {
            String widthText = widthTextField.getText();

            try {
                imageWidth = Integer.parseInt(widthText);
                interlinearizer.setWidth(imageWidth);
            } catch (NumberFormatException nfe) {
                imageWidth = interlinearizer.getWidth();
                widthTextField.setText("" + imageWidth);
            }
        }

        if (lineWrapCheckBox.isSelected()) {
            interlinearizer.setLineWrapStyle(Interlinear.NEXT_LINE);
        } else {
            interlinearizer.setLineWrapStyle(Interlinear.NO_WRAP);
            if (interlinearizer.getOutputMode() == Interlinear.INTERLINEAR_TEXT || 
            		interlinearizer.getOutputMode() == Interlinear.HTML) {
            	// set the width to the maximum
            	interlinearizer.setWidth(Integer.MAX_VALUE);
            }
        }

        String blockWrapString = (String) blockWrapComboBox.getSelectedItem();

        if (blockWrapString.equals(blockWrapNone)) {
            interlinearizer.setBlockWrapStyle(Interlinear.NO_WRAP);
        } else if (blockWrapString.equals(blockWrapWithinBlock)) {
            interlinearizer.setBlockWrapStyle(Interlinear.WITHIN_BLOCKS);
        } else if (blockWrapString.equals(blockWrapBlockBoundary)) {
            interlinearizer.setBlockWrapStyle(Interlinear.BLOCK_BOUNDARY);
        } else if (blockWrapString.equals(blockWrapEachBlock)) {
            interlinearizer.setBlockWrapStyle(Interlinear.EACH_BLOCK);
        }

        String sortingString = (String) sortingComboBox.getSelectedItem();

        {
        	int style = stringToSortingStyle(sortingString);
        	if (style >= 0) {
        		interlinearizer.setSortingStyle(style);
        	}
        }

        if (interlinearizer.getOutputMode() == Interlinear.PRINT) {
            int lineSpacing = 0;

            try {
                lineSpacing = Integer.parseInt(lineSpacingTextField.getText());
            } catch (NumberFormatException nfe) {
                lineSpacingTextField.setText("" + lineSpacing);
            }

            interlinearizer.setLineSpacing(lineSpacing);
        } else {
            lineSpacingTextField.setText("");
        }

        int blockSpacing = -1; // default: derived from line spacing

        try {
            blockSpacing = Integer.parseInt(blockSpacingTextField.getText());
        } catch (NumberFormatException nfe) {
            blockSpacingTextField.setText("" +
                interlinearizer.getBlockSpacing());
        }

        interlinearizer.setBlockSpacing(blockSpacing);

        // visible tiers
        interlinearizer.setVisibleTiers(getSelectedTierList());
        applyFontSizes();

        if (interlinearizer.getOutputMode() == Interlinear.PRINT) {
            interlinearizer.renderView(previewPanel.getBufferedImage());

            // rendering (calculating) the view causes a new total height to be calculated 
            previewPanel.updateView();
        } else if (interlinearizer.getOutputMode() == Interlinear.HTML) {
            interlinearizer.renderView(previewPanel.getBufferedImage());
            
            previewPanel.updateView();
        } else {
            interlinearizer.renderView();

            int pageWidth = 600;
            FontMetrics metrics = previewPanel.getBufferedImage().getGraphics()
                                              .getFontMetrics(Interlinear.MONOSPACED_FONT);

            if (metrics != null) {
                int cw = metrics.charWidth('w'); //any char
                pageWidth = cw * interlinearizer.getWidth();
            }
            if (interlinearizer.getOutputMode() == Interlinear.INTERLINEAR_TEXT && 
            		interlinearizer.getWidth() == Integer.MAX_VALUE) {// indicates NO_WRAP
            	// set a random, large enough image width in number of chars
            	pageWidth = 3000;
            }

            previewPanel.setImageableSize(new Dimension(pageWidth,
                    interlinearizer.getHeight()));
            interlinearizer.drawViewOnImage(previewPanel.getBufferedImage(),
                previewPanel.getOffset());
            previewPanel.repaint();
        }
    }
    
    private int stringToSortingStyle(String sortingString) {
    	int style = -1;
        if (sortingString.equals(sortingAsFile)) {
        	style = Interlinear.EXTERNALLY_SPECIFIED;            
        } else if (sortingString.equals(sortingTierHierarchy)) {
        	style = Interlinear.TIER_HIERARCHY;           
        } else if (sortingString.equals(sortingTierName)) {
        	style = Interlinear.BY_NAME;            
        } else if (sortingString.equals(sortingLinguisticType)) {
        	style = Interlinear.BY_LINGUISTIC_TYPE;            
        } else if (sortingString.equals(sortingParticipant)) {
        	style = Interlinear.BY_PARTICIPANT;
        } else if (sortingString.equals(sortingAnnotator)) {
        	style = Interlinear.BY_ANNOTATOR;
        } else if (sortingString.equals(sortingLanguage)) {
        	style = Interlinear.BY_LANGUAGE;
        }
        
        return style;
    }
    /**
     * Checks for any non applied changes in the parameters before exporting
     * 
     * @return true if some changes are yet to be applied, else false.
     */
    private boolean isSettingsChanged() {         	
    	if( interlinearizer.isSelectionOnly() != selectionOnlyCheckBox.isSelected() ||
    			interlinearizer.isTierLabelsShown() != showTierLabelCheckBox.isSelected() ||
    			interlinearizer.isEmptySlotsShown() != showTimeCodeCheckBox.isSelected() ||
    			interlinearizer.isShowPageNumber() != showPageNumberCB.isSelected() ||
    			interlinearizer.isPlaySoundSel() != playSoundCheckBox.isSelected() ||
    			interlinearizer.isShowSilenceDuration() != silenceCB.isSelected() ||
    			interlinearizer.isEmptySlotsShown() != showEmptySlotsCheckBox.isSelected()){
    		return true;
    	}  
    	
    	String tcStyleString = (String) timeCodeFormatComboBox.getSelectedItem();    	      
        if (playSoundCheckBox.isSelected()){
        	tcStyleString=tcStyleSsMs;        	
        }       
        
        // time code style
        int style = -1;
        if (tcStyleString.equals(tcStyleHhMmSsMs)) {
        	style = Interlinear.HHMMSSMS; 
        } else if (tcStyleString.equals(tcStyleSsMs)) {
        	style = Interlinear.SSMS;              
        } else if (tcStyleString.equals(tcStyleMs)){
        	style = Interlinear.MS;              
        }
        
        if(style != interlinearizer.getTimeCodeType()){
        	return true;
        }
        
        // check the minimal silence duration
        int minSilence = Interlinear.MIN_SILENCE;
        if (silenceCB.isSelected()) {
        	
            String textValue = minDurSilTF.getText().trim();
            
            try {
                minSilence = Integer.parseInt(textValue);
                int  numOfDigits = Integer.parseInt(silenceDecimalComboBox.getSelectedItem().toString());
                if(minSilence != interlinearizer.getMinSilenceDuration() ||
                		numOfDigits != interlinearizer.getNumOfDecimalDigits()){
                	return true;
                }
            } catch (NumberFormatException nfe) {            	
            	JOptionPane.showMessageDialog(this,
                        ElanLocale.getString("InterlinearizerOptionsDlg.Message.InvalidNumber"),
                        ElanLocale.getString("Message.Warning"),
                        JOptionPane.WARNING_MESSAGE);                
                minDurSilTF.selectAll();
                minDurSilTF.requestFocus();
            }
        }
    	
        // hideLinesRadioButton
        if (hideLinesRadioButton.isSelected()) {
        	style = Interlinear.HIDE_EMPTY_LINES;        	
        } else {
        	style = Interlinear.TEMPLATE;      
        }
        
        if(style != interlinearizer.getEmptyLineStyle()){
        	return true;
        }
        
        
        int imageWidth = 0;
        //int imageHeight = 0;
        if (interlinearizer.getOutputMode() != Interlinear.PRINT) {
            String widthText = widthTextField.getText();
            try {
                imageWidth = Integer.parseInt(widthText);
                if(imageWidth != interlinearizer.getWidth()){
                	return true;
                }               
            } catch (NumberFormatException nfe) {
                imageWidth = interlinearizer.getWidth();
                widthTextField.setText("" + imageWidth);
            }
        }

        // lineWrapCheckBox
        if (lineWrapCheckBox.isSelected()) {
        	style = Interlinear.NEXT_LINE;            
        } else {
        	style = Interlinear.NO_WRAP;
        }
        
        if(style != interlinearizer.getLineWrapStyle()){
        	return true;
        }

        String blockWrapString = (String) blockWrapComboBox.getSelectedItem();
        if (blockWrapString.equals(blockWrapNone)) {
        	style = Interlinear.NO_WRAP;
        } else if (blockWrapString.equals(blockWrapWithinBlock)) {
        	style = Interlinear.WITHIN_BLOCKS;
        } else if (blockWrapString.equals(blockWrapBlockBoundary)) {
        	style = Interlinear.BLOCK_BOUNDARY;           
        } else if (blockWrapString.equals(blockWrapEachBlock)) {
        	style = Interlinear.EACH_BLOCK;           
        }
        
        if(style != interlinearizer.getBlockWrapStyle()){
        	return true;
        }

        String sortingString = (String) sortingComboBox.getSelectedItem();

        style = stringToSortingStyle(sortingString);

        if(style != interlinearizer.getSortingStyle()){
        	return true;
        }

        if (interlinearizer.getOutputMode() == Interlinear.PRINT) {
            int lineSpacing = 0;

            try {
                lineSpacing = Integer.parseInt(lineSpacingTextField.getText());
            } catch (NumberFormatException nfe) {
                lineSpacingTextField.setText("" + lineSpacing);
            }

            if(lineSpacing != interlinearizer.getLineSpacing()){
            	return true;
            }            
        } 

        int blockSpacing = -1; // default: derived from line spacing

        try {
            blockSpacing = Integer.parseInt(blockSpacingTextField.getText());
        } catch (NumberFormatException nfe) {
            blockSpacingTextField.setText("" +
                interlinearizer.getBlockSpacing());
        }
        
        if(blockSpacing != interlinearizer.getBlockSpacing()){
        	return true;
        }
        
        List<Tier> tiers = getSelectedTierList();
        //  ordered, visible tiers
        List<String> tierNames = new ArrayList<String>();

        for (int i = 0; i < interlinearizer.getVisibleTiers().size(); i++) {
        	tierNames.add(interlinearizer.getVisibleTiers().get(i).getName());
        }
        
        if(tierNames.size() != tiers.size()){
        	return true;
        }else{
        	for (int i=0; i< tiers.size(); i++ ){
        		if(!tierNames.contains(tiers.get(i))){
        			return true;
        		}
        	}
        }        
//        applyFontSizes();

       return false;
    }

    /**
     * Creates and executes a PrintCommand.
     */
    private void doPrint() {
        Command c = ELANCommandFactory.createCommand(null,
                ELANCommandFactory.PRINT);
        c.execute(interlinearizer.getTranscription(),
            new Object[] { interlinearizer });

        /*
           PrintService[] services = PrintServiceLookup.lookupPrintServices(
                                      DocFlavor.INPUT_STREAM.JPEG, null);
           PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
           PrintService service =  ServiceUI.printDialog(null, 50, 50,
                                                         services, null, null,
                                                         attributes);
         */
    }

    /**
     * Shows a save as dialog and starts rendering to the text file.
     */
    private void doSaveText() {
        String fileName = promptForFileName();

        if (fileName != null) {
            try {
                File exportFile = new File(fileName);
                CharacterRenderer render = new CharacterRenderer(interlinearizer,
                        exportFile, charEncoding);
                render.renderText();

                // success message?

                /*
                   JOptionPane.showMessageDialog(this,
                       ElanLocale.getString("ExportDialog.Message.Error"),
                       ElanLocale.getString("Message.Warning"),
                       JOptionPane.INFORMATION_MESSAGE);
                 */
            } catch (Exception e) {
                // FileNotFound, IO, Security, Null etc
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString(
                        "InterlinearizerOptionsDlg.Error.TextOut") + " \n" +
                    "(" + e.getMessage() + ")",
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    /**
     * Shows a save as dialog and starts rendering to the html file.
     */
    private void doSaveHTML() {
        String fileName = promptForHTMLFileName();

        if (fileName != null) {
            try {
                File exportFile = new File(fileName);
                HTMLRenderer render = new HTMLRenderer(interlinearizer);
                render.renderToFile(exportFile);

                // success message?

                /*
                   JOptionPane.showMessageDialog(this,
                       ElanLocale.getString("ExportDialog.Message.Error"),
                       ElanLocale.getString("Message.Warning"),
                       JOptionPane.INFORMATION_MESSAGE);
                 */
            } catch (Exception e) {
                // FileNotFound, IO, Security, Null etc
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString(
                        "InterlinearizerOptionsDlg.Error.TextOut") + " \n" +
                    "(" + e.getMessage() + ")",
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.WARNING_MESSAGE);
            }
        }    
    }

    /**
     * Invokes a standard, platform specific page setup dialog
     */
    private void doPageSetup() {
        Command c = ELANCommandFactory.createCommand(null,
                ELANCommandFactory.PAGESETUP);
        c.execute(interlinearizer.getTranscription(), null);

        doApplyChanges(); // possibly width, height are changed
    }

    /**
     * Prompts the user for a file name and location.
     *
     * @return a file (unique) path
     */
    private String promptForFileName() {
       
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileAndEncodingDialog(ElanLocale.getString("ExportDialog.ExportToFile"), FileChooser.SAVE_DIALOG, FileExtension.TEXT_EXT, "LastUsedExportDir",null);
        File exportFile = chooser.getSelectedFile();
        
        if (exportFile != null) {
        	charEncoding = chooser.getSelectedEncoding();
        	 return exportFile.getAbsolutePath();               
        } else {          
            return null;
        }
    }

    /**
     * Prompts the user for a file name and location.
     *
     * @return a file (unique) path
     */
    private String promptForHTMLFileName() {

		FileChooser chooser = new FileChooser(this);
		chooser.createAndShowFileDialog(ElanLocale.getString("ExportDialog.ExportToFile"), FileChooser.SAVE_DIALOG, FileExtension.HTML_EXT, "LastUsedExportDir");
		
        File exportFile = chooser.getSelectedFile();
        if (exportFile != null) {
           return exportFile.getAbsolutePath();
        } else {          
            return null;
        }
    }
    
    /**
     * The action performed method.
     *
     * @param event the action event
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        if (event.getSource() == applyChangesButton) {
            doApplyChanges();
        } else if (event.getSource() == printButton) {
        	if(isSettingsChanged()){
        		doApplyChanges();        		
        	}
            if (interlinearizer.getOutputMode() == Interlinear.PRINT) {
                doPrint();
            } else if (interlinearizer.getOutputMode() == Interlinear.HTML) {
                doSaveHTML();  
            } else {
                doSaveText();
            }
        } else if (event.getSource() == pageSetupButton) {
            doPageSetup();
        } else if (event.getSource() == fontSizesButton) {
            doSetFontSizes();
        } else if (event.getSource() == upButton) {
            moveUp();
        } else if (event.getSource() == downButton) {
            moveDown();
        } else if (event.getSource() == closeButton) {
            savePreferences();
            interlinearizer.savePreferences();
        	setVisible(false);
        	dispose();
        } else if(event.getSource() ==  advancedTierSelButton){  
        	List<Tier> selectedTiers = new ArrayList<Tier>(getSelectedTierList());       	
        	ShowHideMoreTiersDlg dialog = new ShowHideMoreTiersDlg(interlinearizer.getTranscription(), getCurrentTierOrder(), selectedTiers, this);    
        	// read prefs
        	List<String> hiddenTiers = Preferences.getListOfString(interlinearizer.prefHiddenTiers, interlinearizer.getTranscription());
        	String selectionMode = Preferences.getString(interlinearizer.prefTierSelectionMode, interlinearizer.getTranscription());
        	
        	if(hiddenTiers != null){
        		int i=0;
        		TierImpl tier;
        		while(i < hiddenTiers.size()){
        			tier = interlinearizer.getTranscription().getTierWithId(hiddenTiers.get(i));
        			if(tier == null || selectedTiers.contains(tier)){
        				hiddenTiers.remove(i);
        			} else {
        				i++;
        			}
        		}
        	}
        	dialog.setSelectionMode(selectionMode, hiddenTiers);        	
        	dialog.setVisible(true);
    		
        	// store prefs only if there are changes
        	if(dialog.isValueChanged()){
        		selectTiers(dialog.getVisibleTierNames());
            	Preferences.set(interlinearizer.prefHiddenTiers, dialog.getHiddenTiers(),
                        interlinearizer.getTranscription());
            	
            	Preferences.set(interlinearizer.prefTierSelectionMode, dialog.getSelectionMode(),
                        interlinearizer.getTranscription());
        	}        	
        }
    }

    /**
     * The item state changed handling.
     *
     * @param ie the ItemEvent
     */
    @Override
	public void itemStateChanged(ItemEvent ie) {
        if (ie.getSource() == sortingComboBox) {
            if (ie.getStateChange() == ItemEvent.SELECTED) {
                int nextSort = curSortMode;
                String sortMode = (String) sortingComboBox.getSelectedItem();

                nextSort = stringToSortingStyle(sortMode);

                curSortMode = nextSort;
                sortTiersTable();
            }
        } else if (ie.getSource() == insertTabCheckBox) {
            interlinearizer.setInsertTabs(insertTabCheckBox.isSelected());
            tabsInsteadOfCheckBox.setEnabled(insertTabCheckBox.isSelected());
        } else if (ie.getSource() == tabsInsteadOfCheckBox) {
            interlinearizer.setTabsReplaceSpaces(tabsInsteadOfCheckBox.isSelected());
        } else if (ie.getSource() == silenceCB) {
            if (silenceCB.isSelected()) {
                minDurSilTF.setEnabled(true);
                minDurSilTF.setBackground(Constants.SHAREDCOLOR4);
                if (minDurSilTF.getText() == null || minDurSilTF.getText().length() == 0) {
                    minDurSilTF.setText("" + Interlinear.MIN_SILENCE);
                }
                silenceDecimalComboBox.setEnabled(true);
                silenceDecimalComboBox.setSelectedItem(2);
                
                minDurSilTF.requestFocus();
            } else {
                minDurSilTF.setEnabled(false);
                minDurSilTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
                
                silenceDecimalComboBox.setEnabled(false);
            }
        } else if (ie.getSource() == playSoundCheckBox) {
        	interlinearizer.setPlaySoundSel(playSoundCheckBox.isSelected());
        } else if (ie.getSource() == lineWrapCheckBox) {
        	// in interlinear text mode or html mode
        	widthTextField.setEnabled(lineWrapCheckBox.isSelected());
        	// if not enabled could remove the value, but cache it in case it is enabled again?
        }
    }
}

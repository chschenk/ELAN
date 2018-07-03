package mpi.eudico.client.annotator.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.client.annotator.viewer.SegmentationViewer2;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.server.corpora.clom.Transcription;

public class SegmentationPanel extends JPanel implements ControllerListener,
		ActionListener {

    private JLabel keyLabel;
    private JRadioButton oneClickRB;
    private JRadioButton twoClicksRB;
    private ButtonGroup optionButtonGroup;
    private JPanel tierSelectionPanel;
    private JButton applyButton;
    private Transcription transcription;

    // jan 2008 new ui elements
    private JPanel fixedOptPanel;
    private JRadioButton oneClickFixedRB;
    private JLabel durLabel;
    private JTextField durTF;
    private JRadioButton beginStrokeRB;
    private JRadioButton endStrokeRB;
    private ButtonGroup boundGroup;
    private JCheckBox delayCB;
    private JTextField delayTF;

    // by default the single stroke of a fixed annotation marks the begin
    private boolean singleStrokeIsBegin = true;
    private long fixedDuration = 1000;

    // administration
    //private ArrayList timeSegments;
    //private String curTier;

    // default mode is the two-times-segmentation mode
    private int mode = SegmentationViewer2.TWO_TIMES_SEGMENTATION;
//    private long lastSegmentTime = -1;
//    private int timeCount = 0;
    //private SegmentationViewer previewer;
//	private InputMap mainInputMap;
//	private InputMap cvInputMap;
//	private ActionMap mainActionMap;
//	private ActionMap cvActionMap;

	private SegmentationViewer2 viewer;
	private JRadioButton keyDownKeyUpRB;

	/**
	 * 
	 */
	public SegmentationPanel(SegmentationViewer2 viewer) {
		super();
		this.viewer = viewer;
		initComponents();
	}
	
	private void initComponents() {
        keyLabel = new JLabel();
        oneClickRB = new JRadioButton();
        twoClicksRB = new JRadioButton();
        optionButtonGroup = new ButtonGroup();
        tierSelectionPanel = new JPanel();
        applyButton = new JButton();
        fixedOptPanel = new JPanel(new GridBagLayout());
        oneClickFixedRB = new JRadioButton();
        durLabel = new JLabel();
        durTF = new JTextField(6);
        beginStrokeRB = new JRadioButton();
        endStrokeRB = new JRadioButton();
        boundGroup = new ButtonGroup();
        delayCB = new JCheckBox();
        delayTF = new JTextField(4);
        delayTF.setEnabled(false);
        keyDownKeyUpRB = new JRadioButton();
        updateLocale();

        GridBagConstraints gridBagConstraints;
        setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);
        Insets optInsets = new Insets(1, 6, 0, 6);

        tierSelectionPanel.setLayout(new GridBagLayout());
        twoClicksRB.setSelected(true);
        optionButtonGroup.add(twoClicksRB);
        twoClicksRB.addActionListener(this);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        //gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        tierSelectionPanel.add(twoClicksRB, gridBagConstraints);

        optionButtonGroup.add(oneClickRB);
        oneClickRB.addActionListener(this);
        gridBagConstraints.gridy = 1;
        tierSelectionPanel.add(oneClickRB, gridBagConstraints);

        optionButtonGroup.add(oneClickFixedRB);
        oneClickFixedRB.addActionListener(this);
        gridBagConstraints.gridy = 2;
        tierSelectionPanel.add(oneClickFixedRB, gridBagConstraints);

        // the fixed size annotation options panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = optInsets;
        fixedOptPanel.add(durLabel, gbc);

        durTF.setText(String.valueOf(fixedDuration));
        durTF.addActionListener(this);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        fixedOptPanel.add(durTF, gbc);

        beginStrokeRB.setSelected(true);
        boundGroup.add(beginStrokeRB);
        beginStrokeRB.addActionListener(this);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 1;
        fixedOptPanel.add(beginStrokeRB, gbc);

        endStrokeRB.addActionListener(this);
        boundGroup.add(endStrokeRB);
        gbc.gridy = 2;
        fixedOptPanel.add(endStrokeRB, gbc);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(1, 28, 2, 6);
        tierSelectionPanel.add(fixedOptPanel, gridBagConstraints);
        
        optionButtonGroup.add(keyDownKeyUpRB);
        keyDownKeyUpRB.addActionListener(this);
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        //gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        tierSelectionPanel.add(keyDownKeyUpRB, gridBagConstraints);
        
        JPanel delayPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        delayPanel.add(delayCB, gbc);
        delayCB.addActionListener(this);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 6, 0, 0);
        delayPanel.add(delayTF, gbc);
        delayTF.addActionListener(this);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        delayPanel.add(new JPanel(), gbc);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
//        gridBagConstraints.insets = optInsets;
        tierSelectionPanel.add(delayPanel, gridBagConstraints);        

        keyLabel.setFont(Constants.DEFAULTFONT);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = optInsets;
        gridBagConstraints.weighty = 0.1;
        tierSelectionPanel.add(keyLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tierSelectionPanel, gridBagConstraints);

        enableFixedDurUI(false);
        
        readPreferences();
	}
	

    /**
     * Applies localized strings to the ui elements.
     */
    public void updateLocale() {
    	updateSegmentkeyLabel();
//        tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString(
//                    "SegmentationDialog.Title")));
        oneClickRB.setText(ElanLocale.getString(
                "SegmentationDialog.Mode.SingleStroke"));
        twoClicksRB.setText(ElanLocale.getString(
                "SegmentationDialog.Mode.DoubleStroke"));
        applyButton.setText(ElanLocale.getString("Button.Apply"));
        oneClickFixedRB.setText(ElanLocale.getString(
                "SegmentationDialog.Mode.SingleStrokeFixed"));
        durLabel.setText(ElanLocale.getString(
                "SegmentationDialog.Label.Duration"));
        beginStrokeRB.setText(ElanLocale.getString(
                "SegmentationDialog.Label.BeginStroke"));
        endStrokeRB.setText(ElanLocale.getString(
                "SegmentationDialog.Label.EndStroke"));
        delayCB.setText(ElanLocale.getString("SegmentationDialog.Label.Delay"));
        keyDownKeyUpRB.setText(ElanLocale.getString("SegmentationDialog.Mode.KeyDownKeyUp"));
    }
    
    public void updateSegmentkeyLabel(){
    	keyLabel.setText(ElanLocale.getString("SegmentationDialog.Label.Key") +
    	            "  " + 
    	            ELANCommandFactory.convertAccKey(ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.SEGMENT, ELANCommandFactory.SEGMENTATION_MODE)));
    }
    
    /**
     * Enables or disables ui elements when the player is started or stopped.
     *
     * @param enable if true most option elements are enabled, some depend on
     * the selected state of others
     */
    private void enableUI(boolean enable) {
        twoClicksRB.setEnabled(enable);
        oneClickRB.setEnabled(enable);
        oneClickFixedRB.setEnabled(enable);

        if (oneClickFixedRB.isSelected() && enable) {
            enableFixedDurUI(enable);
        } else {
            enableFixedDurUI(false);
        }
    }

    /**
     * Enables/disables the ui elements for the fixed duration annotation mode.
     *
     * @param enable if true the fixed duration ui elements are enabled
     */
    private void enableFixedDurUI(boolean enable) {
        fixedOptPanel.setEnabled(enable);
        durLabel.setEnabled(enable);
        durTF.setEditable(enable);
        beginStrokeRB.setEnabled(enable);
        endStrokeRB.setEnabled(enable);
    }
	
    private void readPreferences() {
    	String modeString = Preferences.getString("SegmentationMode.Mode", null);
    	if (modeString != null) {
    		if (modeString.equals("adjacent")) {
    			mode = SegmentationViewer2.ONE_TIME_SEGMENTATION;
    			oneClickRB.setSelected(true);
    			viewer.setSegmentationMode(mode);
    		} else if (modeString.equals("fixed-duration")) {
    			mode = SegmentationViewer2.ONE_TIME_FIXED_SEGMENTATION;
    			oneClickFixedRB.setSelected(true);
    			enableFixedDurUI(true);
    			viewer.setSegmentationMode(mode);
    		} else if (modeString.equals("press-release")) {
    			mode = SegmentationViewer2.TWO_TIMES_PRESS_RELEASE_SEGMENTATION;
    			keyDownKeyUpRB.setSelected(true);
    			viewer.setSegmentationMode(mode);
    		}
    		// otherwise keep the default TWO_TIMES_SEGMENTATION 
    	}
    	
    	Boolean singleStrokeIsBegin = Preferences.getBool("SegmentationMode.MarkBegin", null);
    	if (singleStrokeIsBegin != null) {
    		beginStrokeRB.setSelected(singleStrokeIsBegin);
    		endStrokeRB.setSelected(!singleStrokeIsBegin);
    		viewer.setSingleStrokeIsBegin(singleStrokeIsBegin);
    	}
    	
    	Long duration = Preferences.getLong("SegmentationMode.FixedDuration", null);
    	if (duration != null) {
			durTF.setText(String.valueOf(duration));
			viewer.setFixedSegmentDuration(duration);
    	}
    	
    	Boolean boolPref = Preferences.getBool("SegmentationMode.DelayMode", null);
    	if (boolPref != null) {
    		delayCB.setSelected(boolPref);
    		delayTF.setEnabled(boolPref);
    	}
    	
    	Long longPref = Preferences.getLong("SegmentationMode.DelayDuration", null);
    	if (longPref != null) {
    		delayTF.setText(String.valueOf(longPref));
    		if (delayCB.isSelected()) {
    			viewer.setDelayDuration(longPref);
    		}
    	}
    }
    
    /**
     * All preferences are stored as global preferences.
     * @param key
     * @param value
     */
    private void setPreference(String key, Object value) {
    	//SegmentationMode.Mode : Non-adjacent, Adjacent, Fixed-duration
    	//SegmentationMode.FixedDuration : ms
    	//SegmentationMode.BeginMark : true/false
    	Preferences.set(key, value, null, false, false);
    }
    
	@Override
	public void controllerUpdate(ControllerEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		 Object source = ae.getSource();

	        /*if (source == applyButton) {
	            Command c = ELANCommandFactory.createCommand(transcription,
	                    ELANCommandFactory.ADD_SEGMENTATION);

	            c.execute(transcription, new Object[] { curTier, timeSegments });

	        } else */
	        if (source == oneClickRB) {
	            if (mode != SegmentationViewer2.ONE_TIME_SEGMENTATION) {
//	                timeCount = 0;
//	                lastSegmentTime = -1;
	                mode = SegmentationViewer2.ONE_TIME_SEGMENTATION;
	                viewer.setSegmentationMode(SegmentationViewer2.ONE_TIME_SEGMENTATION);
	                setPreference("SegmentationMode.Mode", "adjacent");
	            }
	            
	            enableFixedDurUI(false);
	            oneClickRB.transferFocusUpCycle();
	        } else if (source == twoClicksRB) {
	            if (mode != SegmentationViewer2.TWO_TIMES_SEGMENTATION) {
	                mode = SegmentationViewer2.TWO_TIMES_SEGMENTATION;
	                viewer.setSegmentationMode(SegmentationViewer2.TWO_TIMES_SEGMENTATION);
	                setPreference("SegmentationMode.Mode", "non-adjacent");
	            }
	            
	            enableFixedDurUI(false);
	            twoClicksRB.transferFocusUpCycle();
	        } else if (source == oneClickFixedRB) {
	            if (mode != SegmentationViewer2.ONE_TIME_FIXED_SEGMENTATION) {
	                mode = SegmentationViewer2.ONE_TIME_FIXED_SEGMENTATION;
	                viewer.setSegmentationMode(SegmentationViewer2.ONE_TIME_FIXED_SEGMENTATION);
	                setPreference("SegmentationMode.Mode", "fixed-duration");
	            }
	            
	            enableFixedDurUI(true);
	            oneClickFixedRB.transferFocusUpCycle();
	        } else if (source == keyDownKeyUpRB) {
	        	// here add key down , key up
	        	if (mode != SegmentationViewer2.TWO_TIMES_PRESS_RELEASE_SEGMENTATION) {
	        		mode = SegmentationViewer2.TWO_TIMES_PRESS_RELEASE_SEGMENTATION;
	        		viewer.setSegmentationMode(SegmentationViewer2.TWO_TIMES_PRESS_RELEASE_SEGMENTATION);
	        		setPreference("SegmentationMode.Mode", "press-release");
	        	}
	        	enableFixedDurUI(false);
	        	keyDownKeyUpRB.transferFocusUpCycle();
	        } else if (source == beginStrokeRB) {
	            singleStrokeIsBegin = true;
	            viewer.setSingleStrokeIsBegin(true);
	            beginStrokeRB.transferFocusUpCycle();
	            setPreference("SegmentationMode.MarkBegin", true);
	        } else if (source == endStrokeRB) {
	            singleStrokeIsBegin = false;
	            viewer.setSingleStrokeIsBegin(false);
	            endStrokeRB.transferFocusUpCycle();
	            setPreference("SegmentationMode.MarkBegin", false);
	        } else if (source == durTF) {
	        	try {
	        		long dur = Long.parseLong(durTF.getText());
	        		durTF.transferFocusUpCycle();
	        		viewer.setFixedSegmentDuration(dur);
	        		setPreference("SegmentationMode.FixedDuration", dur);
	        	} catch (NumberFormatException nfe) {
	        		durTF.selectAll();
	        		durTF.requestFocus();
	        	}
	        	durTF.transferFocusUpCycle();
	        } else if (source == delayCB) {
	        	delayTF.setEnabled(delayCB.isSelected());
	        	if (delayCB.isSelected()) {
	        		try {
		        		long delay = Long.parseLong(delayTF.getText());
		        		viewer.setDelayDuration(delay);
	        		} catch (NumberFormatException nfe) {
	        		}
	        	} else {
	        		viewer.setDelayDuration(0);
	        	}
	        	delayCB.transferFocusUpCycle();
	        	setPreference("SegmentationMode.DelayMode", delayCB.isSelected());
	        } else if (source == delayTF) {
	        	try {
	        		long delay = Long.parseLong(delayTF.getText());
	        		delayTF.transferFocusUpCycle();
	        		viewer.setDelayDuration(delay);
	        		setPreference("SegmentationMode.DelayDuration", delay);
	        	} catch (NumberFormatException nfe) {
	        		delayTF.selectAll();
	        		delayTF.requestFocus();
	        	}
	        }

	}

}

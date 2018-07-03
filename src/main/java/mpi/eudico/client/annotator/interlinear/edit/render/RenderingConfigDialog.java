package mpi.eudico.client.annotator.interlinear.edit.render;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.ColorDialog;
import mpi.eudico.client.annotator.interlinear.edit.IGTConstants;

/**
 * A dialog to customize the visualization settings of the 
 * interlinearization viewer (colors, margins etc.).
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class RenderingConfigDialog extends ClosableDialog {	
	// gui elements
	// row background color even 
	private JLabel rowBgEvenLB;
	private JPanel rowBgEvenColorP;
	private JButton rowBgEvenColorB;
	private JButton rowBgEvenResetB;
	
	// row background color odd
	private JLabel rowBgOddLB;
	private JPanel rowBgOddColorP;
	private JButton rowBgOddColorB;
	private JButton rowBgOddResetB;
	
	// annotation border flag and color
	private JCheckBox showAnnBorderCB;
	private JLabel annBorderColorLB;
	private JPanel annBorderColorP;
	private JButton annBorderColorB;
	private JButton annBorderResetB;
	
	// annotation background flag and color
	private JCheckBox showAnnBgCB;
	private JLabel annBgColorLB;
	private JPanel annBgColorP;
	private JButton annBgColorB;
	private JButton annBgResetB;
	
	// annotation bounding box margins
	private JLabel annBboxMarginsLB;
	private JLabel annBboxTopBotLB;
	private JSpinner annBboxTopBotSP;
	private JButton annBboxTopBotResetB;
	private JLabel annBboxLeRiLB;
	private JSpinner annBboxLeRiSP;
	private JButton annBboxLeRiResetB;		
	
	// width of white spaces, space between annotations bounding boxes 
	private JLabel whitespaceWidthLB;
	private JSpinner whitespaceWidthSP;
	private JButton whitespaceResetB;		
	
	// button panel
	private JButton applyB;
	private JButton cancelB;
	private JButton resetAllB;
	// main panels
	private JPanel settingsPanel;
	private JPanel buttonPanel;
	private UIEventListener eventListener;
	
	private Color annBorderEnabledColor;
	private Color annBgEnabledColor;
	
	// the settings are returned in a Map
	private Map<String, Object> settings = null;
	
	/**
	 * Constructor.
	 * @param owner a frame as the parent of this dialog
	 * @param title the title
	 * @param modal whether or not the dialog is modal
	 * @throws HeadlessException
	 */
	public RenderingConfigDialog(Frame owner, String title, boolean modal)
			throws HeadlessException {
		super(owner, title, modal);
		
		initComponents();
		loadSettings();
		pack();
		setLocationRelativeTo(owner);
	}

	/**
	 * Constructor.
	 * @param owner a dialog as owner or parent
	 * @param title the title of the dialog
	 * @param modal whether or not the dialog is modal
	 */
	public RenderingConfigDialog(Dialog owner, String title, boolean modal)
			throws HeadlessException {
		super(owner, title, modal);
		
		initComponents();
		loadSettings();
		pack();
		setLocationRelativeTo(owner);
	}

	/**
	 * Initializes UI components and listeners etc.
	 */
	private void initComponents() {
		getContentPane().setLayout(new GridBagLayout());
		settingsPanel = new JPanel(new GridBagLayout());
		buttonPanel = new JPanel();
		BoxLayout buttonLayout = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
		buttonPanel.setLayout(buttonLayout);
				
		// row background color even 
		rowBgEvenLB = new JLabel();
		rowBgEvenColorP = new JPanel();
		rowBgEvenColorP.setBorder(new LineBorder(Color.GRAY, 1));
		rowBgEvenColorP.setPreferredSize(new Dimension(32, 12));
		rowBgEvenColorB = new JButton();
		rowBgEvenResetB = new JButton();
		
		// row background color odd
		rowBgOddLB = new JLabel();
		rowBgOddColorP = new JPanel();
		rowBgOddColorP.setBorder(new LineBorder(Color.GRAY, 1));
		rowBgOddColorB = new JButton();
		rowBgOddResetB = new JButton();
		
		// annotation border flag and color
		showAnnBorderCB = new JCheckBox();
		annBorderColorLB = new JLabel();
		annBorderColorP = new JPanel();
		annBorderColorP.setBorder(new LineBorder(Color.GRAY, 1));
		annBorderColorB = new JButton();
		annBorderResetB = new JButton();
		
		// annotation background flag and color
		showAnnBgCB = new JCheckBox();
		annBgColorLB = new JLabel();
		annBgColorP = new JPanel();
		annBgColorP.setBorder(new LineBorder(Color.GRAY, 1));
		annBgColorB = new JButton();
		annBgResetB = new JButton();
		
		// annotation bounding box margins
		annBboxMarginsLB = new JLabel();
		annBboxTopBotLB = new JLabel();
		annBboxTopBotSP = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
		annBboxTopBotResetB = new JButton();
		annBboxLeRiLB = new JLabel();
		annBboxLeRiSP = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
		annBboxLeRiResetB = new JButton();		
		
		// width of white spaces, space between annotations bounding boxes 
		whitespaceWidthLB = new JLabel();
		whitespaceWidthSP = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
		whitespaceResetB = new JButton();		
		
		// button panel
		applyB = new JButton();
		cancelB = new JButton();
		resetAllB = new JButton();
		
		// add to layout
		int margin = 4;
		int margin2 = margin + margin;
		int indent = margin2 * margin;
		Insets genInsets = new Insets(margin, margin, margin, margin); 
		Insets leftButInsets = new Insets(margin, margin2 + margin, margin, margin);
		// ...
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = genInsets;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		settingsPanel.add(rowBgEvenLB, gbc);
		gbc.gridy = 1;
		settingsPanel.add(rowBgOddLB, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = leftButInsets;
		settingsPanel.add(rowBgEvenColorP, gbc);
		gbc.gridx = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = genInsets;
		settingsPanel.add(rowBgEvenColorB, gbc);
		gbc.gridx = 3;
		settingsPanel.add(rowBgEvenResetB, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = leftButInsets;
		settingsPanel.add(rowBgOddColorP, gbc);
		gbc.gridx = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = genInsets;
		settingsPanel.add(rowBgOddColorB, gbc);
		gbc.gridx = 3;
		settingsPanel.add(rowBgOddResetB, gbc);
		
		gbc.gridwidth = 4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(margin + margin2, margin, margin, margin);
		settingsPanel.add(showAnnBorderCB, gbc);
		
		gbc.gridwidth = 1;
		gbc.gridy = 3;
		gbc.insets = new Insets(margin, indent, margin, margin);
		settingsPanel.add(annBorderColorLB, gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx = 1;
		gbc.insets = leftButInsets;
		settingsPanel.add(annBorderColorP, gbc);
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.gridx = 2;
		gbc.insets = genInsets;
		settingsPanel.add(annBorderColorB, gbc);
		gbc.gridx = 3;
		settingsPanel.add(annBorderResetB, gbc);
		
		gbc.gridwidth = 4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 4;
		settingsPanel.add(showAnnBgCB, gbc);
		
		gbc.gridwidth = 1;
		gbc.gridy = 5;
		gbc.insets = new Insets(margin, indent, margin, margin);
		settingsPanel.add(annBgColorLB, gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx = 1;
		gbc.insets = leftButInsets;
		settingsPanel.add(annBgColorP, gbc);
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.gridx = 2;
		gbc.insets = genInsets;
		settingsPanel.add(annBgColorB, gbc);
		gbc.gridx = 3;
		settingsPanel.add(annBgResetB, gbc);
		
		gbc.gridwidth = 4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.insets = new Insets(margin + margin2, margin, margin, margin);
		settingsPanel.add(annBboxMarginsLB, gbc);
		gbc.gridwidth = 2;
		gbc.gridy = 7;
		gbc.insets = new Insets(margin, indent, margin, margin);
		settingsPanel.add(annBboxTopBotLB, gbc);
		gbc.gridx = 2;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weightx = 0.0;
		gbc.insets = genInsets;
		settingsPanel.add(annBboxTopBotSP, gbc);
		gbc.gridx = 3;
		settingsPanel.add(annBboxTopBotResetB, gbc);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 8;
		gbc.insets = new Insets(margin, indent, margin, margin);
		settingsPanel.add(annBboxLeRiLB, gbc);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 2;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.insets = genInsets;
		settingsPanel.add(annBboxLeRiSP, gbc);
		gbc.gridx = 3;
		settingsPanel.add(annBboxLeRiResetB, gbc);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 9;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(margin + margin2, margin, margin, margin);
		settingsPanel.add(whitespaceWidthLB, gbc);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridwidth = 1;
		gbc.gridx = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.insets = genInsets;
		settingsPanel.add(whitespaceWidthSP, gbc);
		gbc.gridx = 3;
		settingsPanel.add(whitespaceResetB, gbc);
		
		// end of settings panel
		buttonPanel.setBorder(new EmptyBorder(margin, margin, margin, margin));
		buttonPanel.add(resetAllB);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(applyB);
		buttonPanel.add(Box.createRigidArea(new Dimension(margin2, 0)));
		buttonPanel.add(cancelB);
		
		GridBagConstraints gpc = new GridBagConstraints();
		gpc.fill = GridBagConstraints.BOTH;
		gpc.weightx = 1.0;
		gpc.weighty = 1.0;
		gpc.insets = new Insets(4, 6, 4, 6);
		getContentPane().add(settingsPanel, gpc);
		gpc.fill = GridBagConstraints.HORIZONTAL;
		gpc.weighty = 0.0;
		gpc.gridy = 1;
		getContentPane().add(buttonPanel, gpc);
		
		updateLocale();
		
		eventListener = new UIEventListener();
		showAnnBorderCB.addItemListener(eventListener);
		showAnnBgCB.addItemListener(eventListener);
		
		rowBgEvenColorB.addActionListener(eventListener);
		rowBgEvenResetB.addActionListener(eventListener);
		rowBgOddColorB.addActionListener(eventListener);
		rowBgOddResetB.addActionListener(eventListener);
		annBorderColorB.addActionListener(eventListener);
		annBorderResetB.addActionListener(eventListener);
		annBgColorB.addActionListener(eventListener);
		annBgResetB.addActionListener(eventListener);
		annBboxLeRiResetB.addActionListener(eventListener);
		annBboxTopBotResetB.addActionListener(eventListener);
		whitespaceResetB.addActionListener(eventListener);
		
		resetAllB.addActionListener(eventListener);
		cancelB.addActionListener(eventListener);
		applyB.addActionListener(eventListener);
	}	
	
	/**
	 * Sets the locale aware text of labels, buttons, checkboxes etc.
	 */
	private void updateLocale() {
		settingsPanel.setBorder(new TitledBorder(ElanLocale.getString("InterlinearEditor.RenderDialog.Settings")));
		// row background color even 
		rowBgEvenLB.setText(ElanLocale.getString("InterlinearEditor.RenderDialog.EvenRowsBgLabel"));
		rowBgEvenColorB.setText(ElanLocale.getString("Button.Browse"));
		rowBgEvenResetB.setText(ElanLocale.getString("Button.Reset"));
		
		// row background color odd
		rowBgOddLB.setText(ElanLocale.getString("InterlinearEditor.RenderDialog.OddRowsBgLabel"));
		rowBgOddColorB.setText(ElanLocale.getString("Button.Browse"));
		rowBgOddResetB.setText(ElanLocale.getString("Button.Reset"));
		
		// annotation border flag and color
		showAnnBorderCB.setText(ElanLocale.getString("InterlinearEditor.RenderDialog.BorderPaintLabel"));
		annBorderColorLB.setText(ElanLocale.getString("InterlinearEditor.RenderDialog.BorderColorLabel"));
		annBorderColorB.setText(ElanLocale.getString("Button.Browse"));
		annBorderResetB.setText(ElanLocale.getString("Button.Reset"));
		
		// annotation background flag and color
		showAnnBgCB.setText(ElanLocale.getString("InterlinearEditor.RenderDialog.BackgroundPaintLabel"));
		annBgColorLB.setText(ElanLocale.getString("InterlinearEditor.RenderDialog.BackgroundColorLabel"));
		annBgColorB.setText(ElanLocale.getString("Button.Browse"));
		annBgResetB.setText(ElanLocale.getString("Button.Reset"));
		
		// annotation bounding box margins
		annBboxMarginsLB.setText(ElanLocale.getString("InterlinearEditor.RenderDialog.BBMarginsLabel"));
		annBboxTopBotLB.setText(ElanLocale.getString("InterlinearEditor.RenderDialog.BBMarginTopLabel"));
		annBboxTopBotResetB.setText(ElanLocale.getString("Button.Reset"));
		annBboxLeRiLB.setText(ElanLocale.getString("InterlinearEditor.RenderDialog.BBMarginLeftLabel"));
		annBboxLeRiResetB.setText(ElanLocale.getString("Button.Reset"));		
		
		// width of white spaces, space between annotations bounding boxes 
		whitespaceWidthLB.setText(ElanLocale.getString("InterlinearEditor.RenderDialog.SpaceWidthLabel"));
		whitespaceResetB.setText(ElanLocale.getString("Button.Reset"));		
		
		// button panel
		applyB.setText(ElanLocale.getString("Button.Apply"));
		cancelB.setText(ElanLocale.getString("Button.Cancel"));
		resetAllB.setText(ElanLocale.getString("InterlinearEditor.RenderDialog.RestoreDefaultsLabel"));
	}
	
	/**
	 * Loads and applies stored settings or default settings in case settings have not
	 * been stored before.  
	 */
	private void loadSettings() {
		Color evenRowColor = Preferences.getColor(IGTConstants.KEY_BACKGROUND_COLOR_EVEN, null);
		if (evenRowColor != null) {
			rowBgEvenColorP.setBackground(evenRowColor);
		} else {
			rowBgEvenColorP.setBackground(IGTConstants.TABLE_BACKGROUND_COLOR1);
		}
		
		Color oddRowColor = Preferences.getColor(IGTConstants.KEY_BACKGROUND_COLOR_ODD, null);
		if (oddRowColor != null) {
			rowBgOddColorP.setBackground(oddRowColor);
		} else {
			rowBgOddColorP.setBackground(IGTConstants.TABLE_BACKGROUND_COLOR2);
		}
		
		Color annBorderColor = Preferences.getColor(IGTConstants.KEY_ANN_BORDER_COLOR, null);
		if (annBorderColor != null) {
			annBorderColorP.setBackground(annBorderColor);
			annBorderEnabledColor = annBorderColor;
		} else {
			annBorderColorP.setBackground(IGTConstants.ANNO_BORDER_COLOR);
			annBorderEnabledColor = IGTConstants.ANNO_BORDER_COLOR;
		}
		
		Color annBgColor = Preferences.getColor(IGTConstants.KEY_ANN_BACKGROUND_COLOR, null);
		if (annBgColor != null) {
			annBgColorP.setBackground(annBgColor);
			annBgEnabledColor = annBgColor;
		} else {
			annBgColorP.setBackground(IGTConstants.ANNO_BACKGROUND_COLOR);
			annBgEnabledColor = IGTConstants.ANNO_BACKGROUND_COLOR;
		}
		
		Integer leftMargin = Preferences.getInt(IGTConstants.KEY_BBOX_LEFT_MARGIN, null);
		if (leftMargin != null) {
			annBboxLeRiSP.setValue(leftMargin);
		} else {
			annBboxLeRiSP.setValue(IGTConstants.TEXT_MARGIN_LEFT);
		}
		
		Integer topMargin = Preferences.getInt(IGTConstants.KEY_BBOX_TOP_MARGIN, null);
		if (topMargin != null) {
			annBboxTopBotSP.setValue(topMargin);
		} else {
			annBboxTopBotSP.setValue(IGTConstants.TEXT_MARGIN_TOP);
		}
		
		Integer spaceWidth = Preferences.getInt(IGTConstants.KEY_WHITESPACE_WIDTH, null);
		if (spaceWidth != null) {
			whitespaceWidthSP.setValue(spaceWidth);
		} else {
			whitespaceWidthSP.setValue(IGTConstants.WHITESPACE_PIXEL_WIDTH);
		}
		
		// two settings that have implications for multiple UI components, load last
		Boolean showAnnoBorders = Preferences.getBool(IGTConstants.KEY_ANN_BORDER_VIS_FLAG, null);
		boolean annBorderVisible = IGTConstants.SHOW_ANNOTATION_BORDER; // default
		if (showAnnoBorders != null) {
			annBorderVisible = ((Boolean) showAnnoBorders).booleanValue();
		}
		showAnnBorderCB.setSelected(annBorderVisible);
		
		Boolean showAnnoBG = Preferences.getBool(IGTConstants.KEY_ANN_BACKGROUND_VIS_FLAG, null);
		boolean annBGPainted = IGTConstants.SHOW_ANNOTATION_BACKGROUND; // default
		if (showAnnoBG != null) {
			annBGPainted = ((Boolean) showAnnoBG).booleanValue();
		}
		showAnnBgCB.setSelected(annBGPainted);
		
		updateAnnBorderItems(annBorderVisible);
		updateAnnBgItems(annBGPainted);
	}
	
	/**
	 * Applies the current settings by storing non-default settings
	 * in a map which is accessible to the caller of the dialog.  
	 */
	private void applySettings() {
		settings = new HashMap<String, Object>();
		
		Color color = rowBgEvenColorP.getBackground();
		if (color != IGTConstants.TABLE_BACKGROUND_COLOR1) {
			settings.put(IGTConstants.KEY_BACKGROUND_COLOR_EVEN, color);
		}
		
		color = rowBgOddColorP.getBackground();
		if (color != IGTConstants.TABLE_BACKGROUND_COLOR2) {
			settings.put(IGTConstants.KEY_BACKGROUND_COLOR_ODD, color);
		}
		
		boolean showAnnoBorderSetting = showAnnBorderCB.isSelected();
		if (showAnnoBorderSetting != IGTConstants.SHOW_ANNOTATION_BORDER) {
			settings.put(IGTConstants.KEY_ANN_BORDER_VIS_FLAG, 
					Boolean.valueOf(showAnnoBorderSetting));
		}
		color = annBorderColorP.getBackground();
		if (!showAnnoBorderSetting) {
			color = annBorderEnabledColor;
		}
		if (color != IGTConstants.ANNO_BORDER_COLOR) {
			settings.put(IGTConstants.KEY_ANN_BORDER_COLOR, color);
		}
		
		boolean showAnnoBgSetting = showAnnBgCB.isSelected();
		if (showAnnoBgSetting != IGTConstants.SHOW_ANNOTATION_BACKGROUND) {
			settings.put(IGTConstants.KEY_ANN_BACKGROUND_VIS_FLAG, 
					Boolean.valueOf(showAnnoBgSetting));
		}
		color = annBgColorP.getBackground();
		if (!showAnnoBgSetting) {
			color = annBgEnabledColor;
		}
		if (color != IGTConstants.ANNO_BACKGROUND_COLOR) {
			settings.put(IGTConstants.KEY_ANN_BACKGROUND_COLOR, color);
		}
		
		Integer topBottomMarg = (Integer) annBboxTopBotSP.getValue();
		if (topBottomMarg != IGTConstants.TEXT_MARGIN_TOP) {
			settings.put(IGTConstants.KEY_BBOX_TOP_MARGIN, topBottomMarg);
		}
		
		Integer leftRightMarg = (Integer) annBboxLeRiSP.getValue();
		if (leftRightMarg != IGTConstants.TEXT_MARGIN_LEFT) {
			settings.put(IGTConstants.KEY_BBOX_LEFT_MARGIN, leftRightMarg);
		}
		
		Integer wSpaceWidth = (Integer) whitespaceWidthSP.getValue();
		if (wSpaceWidth != IGTConstants.WHITESPACE_PIXEL_WIDTH) {
			settings.put(IGTConstants.KEY_WHITESPACE_WIDTH, wSpaceWidth);
		}

	}
	
	/**
	 * Restores all components and relevant fields to default values.
	 */
	private void restoreDefaultSettings() {
		rowBgEvenColorP.setBackground(IGTConstants.TABLE_BACKGROUND_COLOR1);
		rowBgOddColorP.setBackground(IGTConstants.TABLE_BACKGROUND_COLOR2);
		annBorderEnabledColor = IGTConstants.ANNO_BORDER_COLOR;
		showAnnBorderCB.setSelected(IGTConstants.SHOW_ANNOTATION_BORDER);
		updateAnnBorderItems(IGTConstants.SHOW_ANNOTATION_BORDER);
		annBorderColorP.setBackground(IGTConstants.ANNO_BORDER_COLOR);
		annBgEnabledColor = IGTConstants.ANNO_BACKGROUND_COLOR;
		showAnnBgCB.setSelected(IGTConstants.SHOW_ANNOTATION_BACKGROUND);
		updateAnnBgItems(IGTConstants.SHOW_ANNOTATION_BACKGROUND);
		annBgColorP.setBackground(IGTConstants.ANNO_BACKGROUND_COLOR);
		annBboxLeRiSP.setValue(IGTConstants.TEXT_MARGIN_LEFT);
		annBboxTopBotSP.setValue(IGTConstants.TEXT_MARGIN_TOP);
		whitespaceWidthSP.setValue(IGTConstants.WHITESPACE_PIXEL_WIDTH);
	}
	
	/**
	 * Updates several UI elements that are related to the annotation border property.
	 * 
	 * @param selected if true, the elements are enabled, otherwise disabled
	 */
	private void updateAnnBorderItems(boolean selected) {
		annBorderColorLB.setEnabled(selected);
		annBorderColorB.setEnabled(selected);
		annBorderResetB.setEnabled(selected);
		if (!selected) {
			annBorderColorP.setBackground(annBorderColorLB.getBackground());
		} else {
			annBorderColorP.setBackground(annBorderEnabledColor);
		}
	}
	
	/**
	 * Updates several UI elements that are related to the annotation 
	 * background property.
	 * @param selected if true the elements are enabled, otherwise they are disabled
	 */
	private void updateAnnBgItems(boolean selected) {
		annBgColorLB.setEnabled(selected);
		annBgColorB.setEnabled(selected);
		annBgResetB.setEnabled(selected);
		if (!selected) {
			annBgColorP.setBackground(annBgColorLB.getBackground());
		} else {
			annBgColorP.setBackground(annBgEnabledColor);
		}
	}
	
	/**
	 * Shows the color chooser.
	 * @param curColor the current color
	 * @return the newly selected color or null
	 */
	private Color getColor(Color curColor) {
		ColorDialog colDialog = new ColorDialog(this, curColor);	
		return colDialog.chooseColor();
	}
	
	/**
	 * @return the current settings or null
	 */
	public Map<String, Object> getSettings() {		
		return settings;
	}
	
	/**
	 * A class combining action listener and item listener
	 */
	private class UIEventListener implements ActionListener, ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getSource() == showAnnBorderCB) {
				updateAnnBorderItems(showAnnBorderCB.isSelected());
			} else if (e.getSource() == showAnnBgCB) {
				updateAnnBgItems(showAnnBgCB.isSelected());
			}			
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source == resetAllB) {
				restoreDefaultSettings();
			} else if (source == cancelB){
				// return value is null
				RenderingConfigDialog.this.setVisible(false);
				RenderingConfigDialog.this.dispose();
			}  else if (source == applyB){
				// fill settings map
				applySettings();
				RenderingConfigDialog.this.setVisible(false);
				RenderingConfigDialog.this.dispose();
			}
			else if (source == rowBgEvenColorB) {
				Color cl = getColor(rowBgEvenColorP.getBackground());
				if (cl != null && !cl.equals(rowBgEvenColorP.getBackground())) {
					rowBgEvenColorP.setBackground(cl);
				}
			} else if (source == rowBgEvenResetB) {
				rowBgEvenColorP.setBackground(IGTConstants.TABLE_BACKGROUND_COLOR1);
			} else if (source == rowBgOddColorB) {
				Color cl = getColor(rowBgOddColorP.getBackground());
				if (cl != null && !cl.equals(rowBgOddColorP.getBackground())) {
					rowBgOddColorP.setBackground(cl);
				}
			} else if (source == rowBgOddResetB) {
				rowBgOddColorP.setBackground(IGTConstants.TABLE_BACKGROUND_COLOR2);
			} else if (source == annBorderColorB) {
				Color cl = getColor(annBorderColorP.getBackground());
				if (cl != null && !cl.equals(annBorderColorP.getBackground())) {
					annBorderEnabledColor = cl;
					annBorderColorP.setBackground(cl);
				}
			} else if (source == annBorderResetB) {
				annBorderColorP.setBackground(IGTConstants.ANNO_BORDER_COLOR);
			} else if (source == annBgColorB) {
				Color cl = getColor(annBgColorP.getBackground());
				if (cl != null && !cl.equals(annBgColorP.getBackground())) {
					annBgEnabledColor = cl;
					annBgColorP.setBackground(cl);
				}
			} else if (source == annBgResetB) {
				annBgColorP.setBackground(IGTConstants.ANNO_BACKGROUND_COLOR);
			} else if (source == annBboxTopBotResetB) {
				annBboxTopBotSP.setValue(IGTConstants.TEXT_MARGIN_TOP);
			} else if (source == annBboxLeRiResetB) {
				annBboxLeRiSP.setValue(IGTConstants.TEXT_MARGIN_LEFT);
			} else if (source == whitespaceResetB) {
				whitespaceWidthSP.setValue(IGTConstants.WHITESPACE_PIXEL_WIDTH);
			}	
		}	
	}
	
}

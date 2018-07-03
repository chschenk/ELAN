package mpi.eudico.client.annotator.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FontSizer;
import mpi.eudico.client.annotator.Zoomable;

/**
 * A small panel for changing the font size of a viewer or panel.
 * Maybe to be replaced by a more extensive configuration panel/dialog.
 * It currently has Plus and Minus buttons to increase or decrease the font size.
 */
@SuppressWarnings("serial")
public class FontSizePanel extends JPanel implements ActionListener, Zoomable {
	private FontSizer fontSizer;
	private JLabel fontLabel;
	private JButton plusButton;
	private JButton minusButton;
	//private int defaultFontSize = 12;
	private int defaultFontSize = Constants.DEFAULTFONT.getSize();
	
	public FontSizePanel(FontSizer fontSizer) {
		this.fontSizer = fontSizer;
		initComponents();
	}

	private void initComponents() {
		setLayout(new GridBagLayout());
		fontLabel = new JLabel();
		plusButton = new JButton();
		minusButton = new JButton();
		
		try {
			ImageIcon icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Plus16.gif"));
			plusButton.setIcon(icon);
		} catch (Throwable t) {
			plusButton.setText("+");
		}
		try {
			ImageIcon icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Minus16.gif"));
			minusButton.setIcon(icon);
		} catch (Throwable t) {
			minusButton.setText("-");
		}
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(2, 2, 2, 2);
		add(fontLabel, gbc);
		
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		add(minusButton, gbc);
		gbc.gridx = 2;
		add(plusButton, gbc);
		
		updateLocale();
		plusButton.addActionListener(this);
		minusButton.addActionListener(this);
	}
	
	public void updateLocale() {
		updateFontLabel(defaultFontSize);
	}

	@Override
	public void actionPerformed(ActionEvent e) {	
		if (e.getSource() == plusButton) {
			zoomInStep();
		} else if (e.getSource() == minusButton) {
			zoomOutStep();
		}
	}
	
	/**
	 * The font sizes are taken from an array in {@link Constants}
	 * @param fontSize the size to retrieve the index for
	 * @return the index
	 */
	private int getFontIndex(int fontSize) {
		int index = -1;
		for (int i = 0; i < Constants.FONT_SIZES.length; i++) {
			if (fontSize == Constants.FONT_SIZES[i]) {
				index = i;
				break;
			}
		}
		
		if (index == -1) {// if the font size was not one of the predefined sizes
			for (int i = 0; i < Constants.FONT_SIZES.length - 1; i++) {
				if (fontSize > Constants.FONT_SIZES[i] && fontSize < Constants.FONT_SIZES[i + 1]) {
					int dif1 = fontSize - Constants.FONT_SIZES[i];
					int dif2 = Constants.FONT_SIZES[i + 1] - fontSize;
					
					if (i <= Constants.FONT_SIZES.length / 2) {
						index = i + 1;// "round up"
					} else {
						if (dif1 <= dif2) {
							index = i;
						} else {
							index = i + 1;
						}
					}
					break;
				}
			}
		}
		
		return index;
	}
	
	/**
	 * Updates the font label including the font size
	 * @param size the current font size
	 */
	private void updateFontLabel(int size) {
		fontLabel.setText(ElanLocale.getString("Menu.View.FontSize") + ": " + size);
	}
	
	/**
	 * Sets the font size used e.g. to restore a font size from preferences.  
	 * 
	 * @param fontSize the new font size
	 */
	public void setFontSize(int fontSize) {
		fontSizer.setFontSize(fontSize);
		updateFontLabel(fontSize);
		
		if (fontSize == Constants.FONT_SIZES[Constants.FONT_SIZES.length - 1]) {
			plusButton.setEnabled(false);
		}
		if (fontSize == Constants.FONT_SIZES[0]) {
			minusButton.setEnabled(false);
		}
	}

	/**
	 * Increases the size of the font based on the array of predefined font sizes.
	 */
	@Override
	public void zoomInStep() {
		int index = getFontIndex(fontSizer.getFontSize());
		
		index++;
		if (index < Constants.FONT_SIZES.length) {
			fontSizer.setFontSize(Constants.FONT_SIZES[index]);
			updateFontLabel(Constants.FONT_SIZES[index]);
		}
		if (index == Constants.FONT_SIZES.length - 1) {
			plusButton.setEnabled(false);
		}
		minusButton.setEnabled(true);
	}

	/**
	 * Decreases the size of the font based on the array of predefined font sizes.
	 */
	@Override
	public void zoomOutStep() {
		int index = getFontIndex(fontSizer.getFontSize());
		
		index--;
		if (index >= 0) {
			fontSizer.setFontSize(Constants.FONT_SIZES[index]);
			updateFontLabel(Constants.FONT_SIZES[index]);
		}
		if (index == 0) {
			minusButton.setEnabled(false);
		}
		plusButton.setEnabled(true);
	}

	/**
	 * Sets the size of the font to the default value (12).
	 */
	@Override
	public void zoomToDefault() {
		// default font size is 12 
		fontSizer.setFontSize(defaultFontSize);
		updateFontLabel(defaultFontSize);
		plusButton.setEnabled(true);
		minusButton.setEnabled(true);
	}

}

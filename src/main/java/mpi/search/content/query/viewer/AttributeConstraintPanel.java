package mpi.search.content.query.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import mpi.search.content.model.CorpusType;
import mpi.search.gui.SteppedComboBox;

/**
 * Panel for specifying attributes of a token
 *
 * @author Alexander Klassmann
 * @version January 2004
 */
public class AttributeConstraintPanel extends JPanel {
	/** Holds value of property DOCUMENT ME! */
	final private JPanel inputPanel = new JPanel(new GridBagLayout());

	/** Holds value of property DOCUMENT ME! */
	final private CorpusType type;

	/** Holds value of property DOCUMENT ME! */
	final private HashMap comboBoxHash = new HashMap();

	/** Holds value of property DOCUMENT ME! */
	final private HashMap textFieldHash = new HashMap();

	/** Holds value of property DOCUMENT ME! */
	final private String any = "";
	private String tierName;
	private String[] attributeNames;

	/**
	 * Creates a new AttributeConstraintPanel instance
	 *
	 * @param type DOCUMENT ME!
	 */
	public AttributeConstraintPanel(CorpusType type) {
		this.type = type;
		add(inputPanel, BorderLayout.CENTER);
	}

	@Override
	public Dimension getPreferredSize(){
		return new Dimension(320,50);
	}
	
	private void makeLayout() {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		for (int i = 0; i < attributeNames.length; i++) {
			c.gridx = (i / 2) * 2;
			c.gridy = i % 2;

			JLabel attributeNameLabel = new JLabel(attributeNames[i] + " ", SwingConstants.LEFT);
			attributeNameLabel.setToolTipText(type.getToolTipTextForAttribute(attributeNames[i]));
			inputPanel.add(attributeNameLabel, c);
			c.gridx = ((i / 2) * 2) + 1;

			String[] attributeValues =
				(String[]) type.getPossibleAttributeValues(tierName, attributeNames[i]);
			int maxLength = 0;

			for (int j = 0; j < attributeValues.length; j++)
				if (maxLength < attributeValues[j].length()) {
					maxLength = attributeValues[j].length();
				}

			JComboBox comboBox = new SteppedComboBox(attributeValues);
			comboBox.setName(attributeNames[i]);
			comboBox.insertItemAt(any, 0);
		    comboBox.setFont(comboBox.getFont().deriveFont(11));
		      
			comboBox.setPreferredSize(
				new Dimension(35 + (maxLength * 5), comboBox.getPreferredSize().height));
			comboBoxHash.put(attributeNames[i], comboBox);
			inputPanel.add(comboBox, c);

			if (tierName.equals("PHO")) {
				comboBox.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							JTextField textField =
								(JTextField) textFieldHash.get(
									((JComboBox) e.getSource()).getName());

							if (textField == null) {
								return;
							}

							if (any.equals((String) e.getItem())) {
								textField.setText("");
								textField.setEnabled(false);
							}
							else if ("SEP".equals((String) e.getItem())) {
								textField.setText("()");
								textField.setEnabled(false);
							}
							else {
								textField.setText("(.+)");
								textField.setEnabled(true);
							}
						}
					}
				});

				JTextField textField = new JTextField("(.+)", 4);
				textFieldHash.put(attributeNames[i], textField);
				c.gridx = ((i / 2) * 2) + 2;
				inputPanel.add(textField, c);
			}

			comboBox.setSelectedIndex(0);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param attributeName attributeName
	 *
	 * @return String attributeValue
	 */
	public String getAttributeValue(String attributeName) {
		String value = "";
		value =
			comboBoxHash.containsKey(attributeName)
				? (String) ((JComboBox) comboBoxHash.get(attributeName)).getSelectedItem()
				: "";

		if (textFieldHash.containsKey(attributeName)) {
			value += ((JTextField) textFieldHash.get(attributeName)).getText();
		}

		return value;
	}

	private void reset() {
		inputPanel.removeAll();
		comboBoxHash.clear();
		textFieldHash.clear();
		revalidate();
	}

	/**
	 * Sets the attributes corresponding to a tier
	 *
	 * @param tierName tier name
	 */
	public void setTier(String tierName) {
		this.tierName = tierName;
		attributeNames = type.getAttributeNames(tierName);
		reset();

		if (attributeNames.length > 0) {
			makeLayout();
			inputPanel.setVisible(true);
		}
		else {
			inputPanel.setVisible(false);
		}
	}
}

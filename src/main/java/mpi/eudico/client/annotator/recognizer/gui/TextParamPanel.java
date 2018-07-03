/**
 * 
 */
package mpi.eudico.client.annotator.recognizer.gui;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import mpi.eudico.client.annotator.recognizer.data.TextParam;

/**
 * A panel for configuring a textual parameter.
 * 
 * @author Han Sloetjes
 */
public class TextParamPanel extends AbstractParamPanel {
	private JTextField tf;
	private JComboBox cb;
	private String initial;
	private List<String> convoc;

	/**
	 * Constructor. If there is no controlled vocabulary a textfield will be used for text entry
	 * otherwise a combo box will be filled with the values.
	 * 
	 * @param paramName the name of the parameter
	 */
	public TextParamPanel(String paramName, String description, String initial, List<String> convoc) {
		super(paramName, description);
		this.initial = initial;
		this.convoc = convoc;
		initComponents();
	}

	/**
	 * Constructor taking a TextParam as an argument.
	 * 
	 * @param param the text parameter object
	 */
	public TextParamPanel(TextParam param) {
		super(param);
		if (param != null) {
			if (param.curValue != null) {
				initial = param.curValue;
			} else {
				initial = param.defValue;
			}
			if (param.conVoc != null) {
				convoc = param.conVoc;
			}
			initComponents();
		}
	}

	@Override
	protected void initComponents() {
		super.initComponents();
		StringBuilder builder = new StringBuilder("<html><p>");
		builder.append(description);
		if(showParamNames){
			builder.append(" <i>[" + paramName + "]</i>");
		}	
		builder.append("</p></html>");
		descLabel.setText(builder.toString());
		
		// add a textfield or a combobox
		if (convoc != null && convoc.size() > 0) {
			cb = new JComboBox(convoc.toArray());
			if (initial != null) {
				cb.setSelectedItem(initial);
			}
		} else {
			tf = new JTextField();
			if (initial != null) {
				tf.setText(initial);
			}
		}
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 3;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;		
		gbc.insets = new Insets(1, 1, 0, 1);
		
		if (tf != null) {
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			add(tf, gbc);
		} else if (cb != null) {
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0.0;
			add(cb, gbc);
		}		
	}


	/**
	 * Depending on the used ui element, the value of the textfield or the selected value
	 * of the combobox is returned.
	 * 
	 * @see mpi.eudico.client.annotator.recognizer.gui.AbstractParamPanel#getParamValue()
	 * @return the text value
	 */
	@Override
	protected Object getParamValue() {
		if (tf != null) {
			return tf.getText();// can be null or empty
		} else if (cb != null) {
			return cb.getSelectedItem();
		}
		
		return null;
	}

	/**
	 * Sets the (initial) value, e.g. after loading of preferences.
	 * 
	 * @param value the current value
	 * @see mpi.eudico.client.annotator.recognizer.gui.AbstractParamPanel#setParamValue(java.lang.Object)
	 */
	@Override
	protected void setParamValue(Object value) {
		if (value instanceof String) {
			initial = (String) value;
		} else if (value != null) {
			initial = value.toString();
		}
		if (tf != null) {
			tf.setText(initial);
		} else if (cb != null) {
			cb.setSelectedItem(initial);
		}
	}

}

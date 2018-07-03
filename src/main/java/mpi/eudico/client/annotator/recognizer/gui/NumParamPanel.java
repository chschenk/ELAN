/**
 * 
 */
package mpi.eudico.client.annotator.recognizer.gui;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.recognizer.data.NumParam;

/**
 * A parameter panel for numerical parameters.
 * 
 * @author Han Sloetjes
 */
public class NumParamPanel extends AbstractParamPanel implements ChangeListener, 
    ActionListener, FocusListener {
	private float min;
	private float max;
	private float initial;
	private int decPrecision = 1;
	private JSlider slider; //a slider only supports integers
	private JTextField valueField;
	private int scale = 100000;
    private DecimalFormat decFormat;
    
    private String type;
	
	/**
	 * Constructor with parameter identifier, min. value, max. and the default value.
	 * @param paramName the identifier
	 * @param min the minimal value
	 * @param max the maximal value
	 * @param initial initial value 
	 */
	public NumParamPanel(String paramName, String description, float min, float max, float initial, int precision) {
		this(paramName, description, min, max, initial, precision, NumParam.FLOAT);
	}
	
	/**
	 * Constructor with parameter identifier, min. value, max. and the default value.
	 * @param paramName the identifier
	 * @param min the minimal value
	 * @param max the maximal value
	 * @param initial initial value 
	 * @param type 
	 */
	public NumParamPanel(String paramName, String description, float min, float max, float initial, int precision, String type) {
		super(paramName, description);
		this.min = min;
		this.max = max;
		if (this.min > this.max) {
			float tmp = this.max;
			this.max = this.min;
			this.min = tmp;
		}
		this.initial = initial;
		this.type = type;
		decPrecision = precision;
		this.type = type;
		initComponents();
	}
	
	/**
	 * Constructor taking a NumParam as an argument.
	 * 
	 * @param param the num param object
	 */
	public NumParamPanel(NumParam param) {
		super(param);
		if (param != null) {
			min = param.min;
			max = param.max;
			if (min > max) {
				float tmp = max;
				max = min;
				min = tmp;
			}
			
			initial = param.def;// or current?
			decPrecision = param.precision;
			type = param.type;
			initComponents();
		}
	}

	/**
	 * @see mpi.eudico.client.annotator.recognizer.gui.AbstractParamPanel#initComponents()
	 */
	@Override
	protected void initComponents() {
		super.initComponents();
		StringBuilder sb = new StringBuilder("#0.");
		for (int i = 0; i < decPrecision; i++) {
			sb.append("0");
		}
		decFormat = new DecimalFormat(sb.toString(),
		            new DecimalFormatSymbols(Locale.US));
		StringBuilder builder = new StringBuilder("<html><p>");
		builder.append(description);
		if(showParamNames){
			builder.append(" <i>[" + paramName + "]</i>");
		}	
		builder.append(" <i><small>");
		if(type.equals(NumParam.INT)){
			builder.append(" (" + Math.round(min) + " - " + Math.round(max) + "; " + Math.round(initial) + ")");
		} else {
			builder.append(" (" + min + " - " + max + "; " + initial + ")");
		}		
		builder.append("</small></i></p></html>");
		descLabel.setText(builder.toString());
		//add a slider or slider
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(1, 1, 0 , 1);
		
		if(type.equals(NumParam.INT)){
			slider = new JSlider(Math.round(min), Math.round(max), Math.round(initial));// a slider only supports integers
		} else {
			slider = new JSlider((int) (min * scale), (int) (max *scale), (int) (initial * scale));// a slider only supports integers
		}		
		
		
		add(slider, gbc);
		slider.addChangeListener(this);
		
		valueField = new JTextField(8);
		if(type.equals(NumParam.INT)){
			valueField.setText(String.valueOf(Math.round(initial)));
		} else {
			valueField.setText(String.valueOf(initial));
		}	
		
		valueField.addActionListener(this);
		valueField.addFocusListener(this);
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.insets = new Insets(1, 1, 0, 5);
		add(valueField, gbc);
	}
	/**
	 * @see mpi.eudico.client.annotator.recognizer.gui.AbstractParamPanel#getParamValue()
	 */
	@Override
	protected Object getParamValue() {
		// check whether the textfield has been "committed"?
		if (slider != null) {
			if(type.equals(NumParam.INT)){	
				return new Float(slider.getValue());
			} else {
				return new Float(slider.getValue() / (float) scale);
			}
		}
		
		return null;
	}

	/**
	 * Sets the initial value for the parameter.
	 * 
	 * @param value the initial value for this parameter
	 */
	@Override
	protected void setParamValue(Object value) {
		if (value instanceof Float) {
			float nv = (Float) value;
			if (nv >= min && nv <= max) {
				initial = nv;

				if (slider != null) {
					
					if(type.equals(NumParam.INT)){					
						slider.setValue(Math.round(initial));
					} else {
						slider.setValue((int)(initial * scale));
					}
				}
			}
		} else if (value instanceof String) {
			try {
				float nv = Float.parseFloat((String) value);
				
				if (nv >= min && nv <= max) {
					initial = nv;
					// update ui
					if (slider != null){
						if(type.equals(NumParam.INT)){					
							slider.setValue(Math.round(initial));
						} else {
							slider.setValue((int)(initial * scale));
						}
					}
				}
			} catch (NumberFormatException nfe) {
				// ignore
			}
		}
		
	}

	/**
	 * Updates the textfield when the slider is dragged.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == slider) {
			if(type.equals(NumParam.FLOAT)){
				valueField.setText(decFormat.format(slider.getValue() / (float) scale));
			} else {
				valueField.setText(String.valueOf(slider.getValue()));
			}
		}
	}

	/**
	 * When the enter button is hit while the textfield has focus the 
	 * current value is applied.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == valueField) {
			String v = valueField.getText();
			try {
				float fv = Float.parseFloat(v);
				if (fv < min) {
					fv = min;
				}
				if (fv > max) {
					fv = max;
				}
				
				if(type.equals(NumParam.INT)){					
					slider.setValue(Math.round(fv));
				} else {
					slider.setValue((int) (fv * scale));
				}
				
			} catch (NumberFormatException nfe) {
				if(type.equals(NumParam.FLOAT)){					
					valueField.setText(decFormat.format(slider.getValue() / (float) scale));
				} else {
					valueField.setText(String.valueOf(slider.getValue()));
				}
			}
		}
	}

	/**
	 * Ignored
	 */
	@Override
	public void focusGained(FocusEvent e) {
		// method stub
	}

	/**
	 * If the textfield loses focus the current value is applied.
	 * 
	 * @param e focus event
	 */
	@Override
	public void focusLost(FocusEvent e) {	
		if (e.getSource() == valueField) {
			String v = valueField.getText();
			try {
				float fv = Float.parseFloat(v);
				if (fv < min) {
					fv = min;
				}
				if (fv > max) {
					fv = max;
				}
				if(type.equals(NumParam.INT)){					
					slider.setValue(Math.round(fv));
				} else {
					slider.setValue((int) (fv * scale));
				}
			} catch (NumberFormatException nfe) {
				if(type.equals(NumParam.FLOAT)){					
					valueField.setText(decFormat.format(slider.getValue() / (float) scale));
				} else {
					valueField.setText(String.valueOf(slider.getValue()));
				}
			}
		}		
	}

}

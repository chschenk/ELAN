/**
 * 
 */
package mpi.eudico.client.annotator.recognizer.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import mpi.eudico.client.annotator.recognizer.data.Param;

/**
 * Abstract base class for parameter panels.
 * 
 * @author Han Sloetjes
 */
public abstract class AbstractParamPanel extends JPanel {
	/**
	 * The identifier of the parameter
	 */
	protected String paramName;
	protected String description;
	protected JLabel descLabel;
	
	// flag to show the param names to the developers
	protected boolean showParamNames = false;

	/**
	 * Constructor.
	 * 
	 * @param paramName name of the parameter
	 */
	public AbstractParamPanel(String paramName, String description) {
		super();
		
	}
	
	/**
	 * Constructor taking a Param object as argument.
	 * 
	 * @param param the parameter object
	 */
	public AbstractParamPanel(Param param) {
		super();
		if (param != null) {
			this.paramName = param.id;
			this.description = param.info;
			
			String test = System.getProperty("ShowRecognizerParamNames");
			if(test != null && test.toLowerCase().equals("true")){
				showParamNames = true;
			} 
		}
	}
	
	/**
	 * Returns the name of the parameter.
	 * 
	 * @return the name of the parameter
	 */
	public String getParamName() {
		return paramName;
	}

	/**
	 * Initializes the user interface elements.
	 */
	protected void initComponents() {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 1, 0, 1);
		descLabel = new JLabel(description);
		add(descLabel, gbc);
	}
	
	/**
	 * Returns the current value of the parameter.
	 * 
	 * @return the current value
	 */
	protected abstract Object getParamValue();
	
	/**
	 * Sets the current value of the parameter.
	 * 
	 * @return the current value
	 */
	protected abstract void setParamValue(Object value);

}

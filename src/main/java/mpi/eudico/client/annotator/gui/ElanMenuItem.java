package mpi.eudico.client.annotator.gui;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;


/**
 * This class extends <code>JMenuItem</code> by getting the  accelerator key
 * from the key-value list and applying this to the menu item.
 *
 * @author Han Sloetjes
 */
public class ElanMenuItem extends JMenuItem {
    /**
     * Constructor for ElanMenuItem. When there is an accelerator key defined
     * it is applied to the menu item.
     *
     * @param a the Action for this menu item
     */
    public ElanMenuItem(Action a) {
        super(a);

        if (a != null) {
            KeyStroke ks = (KeyStroke) a.getValue(Action.ACCELERATOR_KEY);

            if (ks != null) {
                setAccelerator(ks);
            }
        }
    }
    
    /**
     * Constructor for ElanMenuItem. When there is an accelerator key defined
     * it is applied to the menu item. Accepts an additional parameter 
     * to set the enabled state. 
     *
     * @param a the Action for this menu item
     * @param enabled the enabled flag
     */
    public ElanMenuItem(Action a, boolean enabled) {
        this(a);
        setEnabled(enabled);
    }

    /**
     * Creates a menu item and sets it enabled or disabled.
     * @param s the label
     * @param enabled enabled flag
     */
    public ElanMenuItem(String s, boolean enabled) {
    	super(s);
    	setEnabled(enabled);
    }
    
	/**
	 * @param text
	 */
	public ElanMenuItem(String text) {
		super(text);
	}

	/**
	 * @see javax.swing.AbstractButton#setAction(javax.swing.Action)
	 */
	@Override
	public void setAction(Action a) {
		super.setAction(a);
		
        if (a != null) {
            KeyStroke ks = (KeyStroke) a.getValue(Action.ACCELERATOR_KEY);

            if (ks != null) {
                setAccelerator(ks);
            }
        }
	}
    
    /**
     * Sets the action and the enabled state.
     * 
     * @param a the action
     * @param enabled the enabled flag
     */
	public void setAction(Action a, boolean enabled) {
		this.setAction(a);
		setEnabled(enabled);
	}
}

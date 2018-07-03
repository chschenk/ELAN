package mpi.eudico.client.annotator.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.util.FavoriteColorPanel;

/**
 * A dialog for setting color preferences. This class is based on code duplicated 
 * in AdvancedTierOptionsDialog and DisplaySettingsPanel. The new class can be used in
 * those two, and it is already used in the ViewerPanel class.
 *
 * @author kj 
 * @version 
 */

public class ColorDialog /* implements ActionListener */{
    
    private Component parent;	
    private Color oldColor;
    final JColorChooser chooser;
    private AbstractAction aa;
    private Map<String, Color> oldColors = null;
    private FavoriteColorPanel fcp;
	
    /**
     * Creates a customized color chooser, which includes a panel for (persistent) favorite
     * colors.
     * 
     * @param oldColor the color to start with
     * @param parent the parent of this dialog
     * @return a new color or null
     */

    public ColorDialog(Component parent, final Color oldColor){

	this.parent = parent;
	this.oldColor = oldColor;

	chooser = new JColorChooser(oldColor);
	AbstractColorChooserPanel[] panels = chooser.getChooserPanels();
	AbstractColorChooserPanel[] panels2 = new AbstractColorChooserPanel[panels.length + 1];
        fcp = new FavoriteColorPanel();
	panels2[0] = fcp;
	
	for (int i = 0; i < panels.length; i++) {
		panels2[i + 1] = panels[i];
	}
	
	chooser.setChooserPanels(panels2);
	// read stored favorite colors
	oldColors = Preferences.getMapOfColor("FavoriteColors", null);
	
 	if (oldColors != null) {
		//Color[] favColors = new Color[fcp.NUM_COLS * fcp.NUM_ROWS];
		Color[] favColors = fcp.getColors();// use the array of the panel

		for (Map.Entry<String, Color> e : oldColors.entrySet()) {
			String key = e.getKey();
			Color val = e.getValue();
			
			try {
				int index = Integer.valueOf(key);
				if (index < favColors.length) {
					favColors[index] = val;
				}
			} catch (NumberFormatException nfe) {
				// ignore
			}
		}
		//fcp.setColors(favColors);	
	}
	// have to provide an "OK" action listener...
	aa = new AbstractAction() {
				
	    @Override
		public void actionPerformed(ActionEvent e) {
		putValue(Action.DEFAULT, chooser.getColor());				
		}
	};
    }
    
    /**
     * Lets the user choose colors.
     * 
     * @return a new color or null
     */

    public Color chooseColor() {
	
	Color newColor;
	
	JDialog cd = JColorChooser.createDialog(parent, ElanLocale.getString("ColorChooser.Title"), 
	    		true, chooser, aa, null); 
	cd.setVisible(true);
	
	// if necessary store the current favorite colors
	HashMap<String, Color> colMap = new HashMap<String, Color>();
	Color[] colors = fcp.getColors();
	for (int i = 0; i < colors.length; i++) {
	    if (colors[i] != null) {
		colMap.put(String.valueOf(i), colors[i]);
	    }
	}

	if (colMap.size() > 0 || oldColors != null) {
	    Preferences.set("FavoriteColors", colMap, null);
	}

	newColor = (Color) aa.getValue(Action.DEFAULT);
	return newColor; // substitute default here
    }

}
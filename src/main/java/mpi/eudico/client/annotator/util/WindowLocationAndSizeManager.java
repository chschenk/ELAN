package mpi.eudico.client.annotator.util;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;

import mpi.eudico.client.annotator.Preferences;

/**
 * Class to store & restore the location and size
 * of the window
 * 
 * @author aarsom
 *
 */
public class WindowLocationAndSizeManager {
	
	/**
     * Pack, size and set location.
     * 
     * @param dialog, the dialog for which the location and size has to be set
     * @param prefPrefix, the prefPrefix to read the preferences for the dialog
     */
	public static void postInit(Window window, String prefPrefix){
		postInit(window, prefPrefix, 0, 0);
	}
	
	/**
     * Pack, size and set location.
     * 
     * @param dialog, the dialog for which the location and size has to be set
     * @param prefPrefix, the prefPrefix to read the preferences for the dialog
     * @param minimalWidth, minimal width of the dialog
     * @param minimalHeight, minimal height of the dialog
     */
	public static void postInit(Window window, String prefPrefix, int minimalWidth, int minimalHeight ){
		window.pack();
        
        // set initial location and size, take insets into account
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        Insets ins = Toolkit.getDefaultToolkit().getScreenInsets(
      		ge.getDefaultScreenDevice().getDefaultConfiguration());
        
        int maxW = dim.width - ins.left - ins.right;
        int maxH = dim.height - ins.top - ins.bottom;
        
        Point location = null;
        Dimension size = null;
        
        if(prefPrefix != null){
        	location = Preferences.getPoint(prefPrefix+".Location", null);
        	size = Preferences.getDimension(prefPrefix+".Size", null);
        }
        
		if (location != null) {
			Point p = location;
			int x = p.x <= maxW - 50 ? p.x : maxW - 50;
			int y = p.y <= maxH - 50 ? p.y : maxH - 50;
			window.setLocation(x, y);
		} else {
			window.setLocationRelativeTo(window.getParent());
		}
		
		int targetW;
		int targetH;
		if (size != null) {
			Dimension d = size;
			targetW = d.width < maxW ? d.width : maxW;
	        targetH = d.height < maxH ? d.height : maxH;
		} else {
			targetW = window.getSize().width < minimalWidth ? minimalWidth : window.getSize().width;
	        targetW = targetW > maxW ? maxW : targetW;
	        
	        targetH = window.getSize().height < minimalHeight ? minimalHeight : window.getSize().height;
	        targetH = targetH > maxH ? maxH : targetH;
		}
		window.setSize(targetW, targetH);        
	}
	
	/**
	 * Stores the location and size preferences of the dialog 
	 * 
	 * @param dialog, the dialog for which the preference has to be stored
	 * @param prefPrefix, prefix for storing the dialog preferences
	 */
	public static void storeLocationAndSizePreferences(Window window, String prefPrefix){
		if(window != null && prefPrefix != null){
			Point p = window.getLocation();
	        Dimension d = window.getSize();
	        Preferences.set(prefPrefix+".Location", p, null, false, false);
	        Preferences.set(prefPrefix+".Size", d, null, false, false);
		}
	}
}

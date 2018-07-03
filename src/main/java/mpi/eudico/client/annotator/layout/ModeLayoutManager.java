package mpi.eudico.client.annotator.layout;

import java.util.List;

import mpi.eudico.client.annotator.Zoomable;

public interface ModeLayoutManager {
	
	/**
	 * Adds the object to the active layout
	 * 
	 * @param object, object to be added
	 */
	public void add(Object object);
	
	/**
	 * Remove an object from the active layout
	 * 
	 * @param object, object to be removed
	 */
	public void remove(Object object);
	
	/**
	 * Makes the layout
	 */
	public void doLayout();
	
	/**
	 * Updates all the values
	 */
	public void updateLocale();	
	
	/**
	 * Clears everthing in the layout
	 */
	public void clearLayout();	
	
	/**
	 * Initialize all the components required
	 * for the layout
	 */
	public void initComponents();
	
	/**
	 * Enables or disables certain menu's.
	 * Note HS 08-2016: when a mode is deactivated "true" is passed as the enabled parameter,
	 * when the mode becomes active false is passed. So a mode should take care of 
	 * actions and menus that are not active/enabled in this mode.
	 * 
	 * @param enabled, if true enables the menus else
	 * 					disables the menus
	 */
	public void enableOrDisableMenus(boolean enabled);
	
	/**
	 * If shortcut values are changes this method
	 * called
	 * 
	 */
	public void shortcutsChanged();
	
	/**
	 * Creates the new instance of the viewer
	 * and adds it to the current layout
	 * 
	 * @param viewerName, name of the viewer to be added
	 */
	public void createAndAddViewer(String viewerName);
	
	/**
	 * Destroys the viewerand also removes
	 * it from the current layout
	 * 
	 * @param viewerName, name of the viewer to be destroyed
	 */
	public boolean destroyAndRemoveViewer(String viewerName);
	
	/**
	 * Returns a list of viewers that implement the Zoomable interface.
	 * The viewers should be returned in the order of preference; in principle
	 * a method of the first zoomable in the list will be called.
	 * 
	 * @return a list of zoomable viewers or null
	 */
	public List<Zoomable> getZoomableViewers();

	/**
	 * Detaches the specified viewer or player.
	 * 
	 * @param object the viewer or player to remove from the main application frame
	 */
	public void detach(Object object);

	/**
	 * Attaches the specified viewer or player. 
	 *
	 * @param object the viewer or player to attach
	 */
	public void attach(Object object);	

	/**
	 * Notification of any change in the preferences.
	 * Can be ignored if the manager doesn't need it.
	 */
	public void preferencesChanged();

	/**
	 * Called when a file is closed
	 */
	public void cleanUpOnClose();
	
	/**
	 * Called when the transcription is closed or 
	 * when switched btw different modes
	 */
	public void isClosing();
}

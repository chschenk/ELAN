package mpi.eudico.client.mac;

/**
 * Defines Mac OS X specific methods for handling the main screen menu 
 * Application events.
 * 
 * @version Feb 2009 added delegate methods for all methods in com.apple.eawt.ApplicationListener
 */
public interface MacApplicationListener {
	
	/**
	 * Handle the standard main/screen menu bar Quit action.
	 */
	public void macHandleQuit();
	
	/**
	 * Handle the standard main/screen menu bar About action.
	 *
	 */
	public void macHandleAbout();
	
	/**
	 * Handle the standard main/screen menu bar Preferences action.
	 *
	 */
	public void  macHandlePreferences();

	/**
	 * Any initialization on application launch. If a file path is specified then
	 * open it.
	 * 
	 * @param fileName the path to a file, or null
	 */
	public void macHandleOpenApplication(String fileName);
	
	/**
	 * Open the specified file.
	 * 
	 * @param fileName the path to a file
	 */
	public void macHandleOpenFile(String fileName);
	
	/**
	 * Request to print a file. Could be a print with default settings?
	 * Or show a preview window?
	 * 
	 * @param fileName the path to a file
	 */
	public void macHandlePrintFile(String fileName);
	
	/**
	 * Any initialization on application (re)launch. If a file path is specified then
	 * open it.
	 * 
	 * @param fileName the path to a file, or null 
	 */
	public void macHandleReOpenApplication(String fileName);
}

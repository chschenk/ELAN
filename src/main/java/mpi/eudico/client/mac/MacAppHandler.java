package mpi.eudico.client.mac;


import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * A class to handle the Mac OS X default application menu items, like "About" 
 * and "Quit" (Command Q). This adapter passes the handling to the (one)
 * MacApplicationListener. 
 * 
 * @version 1.0 nov 04
 * @version 2.0 jan 09 Added wrappers for all methods in com.apple.eawt.ApplicationListener
 * @author han sloetjes
 */
public class MacAppHandler extends ApplicationAdapter {
	private Application app;
	private MacApplicationListener macApp;
	
	/**
	 * Constructs the MacAppHandler. Only one listener allowed.
	 * 
	 * @param macApp the listener to be notified
	 */
	public MacAppHandler(MacApplicationListener macApp){
		this.macApp = macApp;
		app = Application.getApplication();
		app.setEnabledPreferencesMenu(true);
		app.addApplicationListener(this);
	}

	/**
	 * Forwards the handling of the main screen menu "Quit" event to the listener.
	 * 
	 * @param ae the Application event from the OS
	 */
	@Override
	public void handleQuit(ApplicationEvent ae) {
		if (ae.isHandled()) {
			return;
		}
		
		macApp.macHandleQuit();
		ae.setHandled(false);
	}
	
	/**
	 * Forwards the handling of the main screen menu "About" event to the listener.
	 * 
	 * @param ae the Application event from the OS
	 */
	@Override
	public void handleAbout(ApplicationEvent ae) {
		if (ae.isHandled()) {
			return;
		}
		
		macApp.macHandleAbout();
		ae.setHandled(true);
	}

	/**
	 * Forwards the handling of the main screen menu "Preferences" event to the listener.
	 * 
	 * @param ae the Application event
	 * @see com.apple.eawt.ApplicationAdapter#handlePreferences(com.apple.eawt.ApplicationEvent)
	 */
	@Override
	public void handlePreferences(ApplicationEvent ae) {
		if (ae.isHandled()) {
			return;
		}
		//System.out.println("Handle Preferences...");
		macApp.macHandlePreferences();
		ae.setHandled(true);
	}

	/**
	 * Called from the Finder. The Application Event can contain a file.
	 * 
	 * @see com.apple.eawt.ApplicationAdapter#handleOpenApplication(com.apple.eawt.ApplicationEvent)
	 */
	@Override
	public void handleOpenApplication(ApplicationEvent ae) {
		if (ae.isHandled()) {
			return;
		}
		//System.out.println("Handle Open Application: " + ae.getFilename());
		macApp.macHandleOpenApplication(ae.getFilename());
		ae.setHandled(true);
	}

	/**
	 * Called from the Finder. The event contains the file name.
	 * 
	 * @see com.apple.eawt.ApplicationAdapter#handleOpenFile(com.apple.eawt.ApplicationEvent)
	 */
	@Override
	public void handleOpenFile(ApplicationEvent ae) {
		if (ae.isHandled()) {
			return;
		}
		
		//System.out.println("Handle Open File: " + ae.getFilename());
		macApp.macHandleOpenFile(ae.getFilename());
		ae.setHandled(true);
	}

	/**
	 * Called from Finder or other application. Implement or warn that this is not supported?
	 * 
	 * @see com.apple.eawt.ApplicationAdapter#handlePrintFile(com.apple.eawt.ApplicationEvent)
	 */
	@Override
	public void handlePrintFile(ApplicationEvent ae) {
		if (ae.isHandled()) {
			return;
		}
		
		//System.out.println("Handle Print File: " + ae.getFilename());
		macApp.macHandlePrintFile(ae.getFilename());
		ae.setHandled(true);
	}

	/**
	 * Called from the Finder. Do what??
	 * @see com.apple.eawt.ApplicationAdapter#handleReOpenApplication(com.apple.eawt.ApplicationEvent)
	 */
	@Override
	public void handleReOpenApplication(ApplicationEvent ae) {
		if (ae.isHandled()) {
			return;
		}
		
		//System.out.println("Handle ReOpen Application: " + ae.getFilename());
		// file name usually/always null
		macApp.macHandleReOpenApplication(ae.getFilename());
		ae.setHandled(true);
	}

	
}

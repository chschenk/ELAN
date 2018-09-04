package mpi.eudico.client.mac;

import java.io.File;
import java.util.List;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.Application;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.PrintFilesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.OpenFilesEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.PrintFilesEvent;
import com.apple.eawt.AppEvent.QuitEvent;

/**
 * Implementation of Apple's new integration mechanism, needs 
 * Java for Mac OS X 10.6 Update 3 or Java for Mac OS X 10.5 Update 8 
 * 
 * This class implements a couple of the new handler interfaces but delegates 
 * the actual implementation to a MacApplicationListener instance.
 * 
 * @author Han Sloetjes
 */
public class MacAppHandler2 implements AboutHandler, OpenFilesHandler, PrintFilesHandler,
		PreferencesHandler, QuitHandler {
	private MacApplicationListener macApp;
	
	/**
	 * Constructor.
	 * 
	 * @param macApp the listener containing the real implementation 
	 */
	public MacAppHandler2(MacApplicationListener macApp) {
		super();
		this.macApp = macApp;
		Application.getApplication().setQuitHandler(this);
		Application.getApplication().setAboutHandler(this);
		Application.getApplication().setOpenFileHandler(this);
		Application.getApplication().setPreferencesHandler(this);
		Application.getApplication().setPrintFileHandler(this);
	}

	/**
	 * AboutHandler
	 */
	@Override
	public void handleAbout(AboutEvent arg0) {
		macApp.macHandleAbout();
	}

	/**
	 * OpenFilesHandler
	 */
	@Override
	public void openFiles(OpenFilesEvent event) {
		List<File> files = event.getFiles();
		if (files != null && files.size() > 0) {
			for (File f : files) {
				macApp.macHandleOpenFile(f.getAbsolutePath());
			}
		}
	}

	/**
	 * PreferencesHandler
	 */
	@Override
	public void handlePreferences(PreferencesEvent arg0) {
		macApp.macHandlePreferences();
	}

	/**
	 * QuitHandler
	 */
	@Override
	public void handleQuitRequestWith(QuitEvent arg0, QuitResponse arg1) {
		arg1.cancelQuit();// cancel the system quit action
		macApp.macHandleQuit();

	}

	/**
	 * PrintFilesHandler
	 */
	@Override
	public void printFiles(PrintFilesEvent event) {
		List<File> files = event.getFiles();
		
		if (files != null && files.size() > 0) {
			for (File f : files) {
				macApp.macHandlePrintFile(f.getAbsolutePath());
			}
		}
	}

}

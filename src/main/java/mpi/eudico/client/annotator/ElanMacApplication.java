package mpi.eudico.client.annotator;

import mpi.eudico.client.annotator.commands.global.AboutMA;
import mpi.eudico.client.annotator.commands.global.EditPreferencesMA;
import mpi.eudico.client.annotator.commands.global.MenuAction;

import mpi.eudico.client.annotator.util.ClientLogger;

import mpi.eudico.client.mac.MacApplicationListener;

import java.io.File;


/**
 * A class for handling Mac OS specific events.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ElanMacApplication implements MacApplicationListener {
    /**
     * The "About" function from the main (screen) menu bar.
     */
    @Override
	public void macHandleAbout() {
        MenuAction ma = new AboutMA("Menu.Help.About", (ElanFrame2) FrameManager.getInstance().getActiveFrame());
        ma.actionPerformed(null);
    }

    /**
     * Called when the application is launched. A file can be passed if  the
     * application is launched e.g. after a double click on a document.
     *
     * @param fileName the path to a file, or null
     */
    @Override
	public void macHandleOpenApplication(String fileName) {
        if (fileName != null) {
            macHandleOpenFile(fileName);
        }
    }

    /**
     * Opens the specified file, the application is already running.
     *
     * @param fileName the path to a file or null
     */
    @Override
	public void macHandleOpenFile(String fileName) {
        if (fileName != null) {
            File f = new File(fileName);

            try {
                if (!f.exists() || f.isDirectory()) {
                    ClientLogger.LOG.info("Cannot open file: " + fileName);

                    return;
                }

                FrameManager.getInstance().createFrame(f.getAbsolutePath());
            } catch (Exception ex) {
                ClientLogger.LOG.info("Cannot open file: " + ex.getMessage());
            }
        } else {
            ClientLogger.LOG.info("No file specified.");
        }
    }

    /**
     * The Preferences item from the main (screen) menu.
     */
    @Override
	public void macHandlePreferences() {
        MenuAction ma2 = new EditPreferencesMA("Menu.Edit.Preferences.Edit",
        		(ElanFrame2) FrameManager.getInstance().getActiveFrame());
        ma2.actionPerformed(null);
    }

    /**
     * Request to print a certain file. Not implemented yet.
     *
     * @param fileName the path to the file
     */
    @Override
	public void macHandlePrintFile(String fileName) {
        ClientLogger.LOG.info(
            "Printing from Finder or other application not yet implemented.");
    }

    /**
     * Mac OS X specific handling of the main (screen) menu Quit application
     * event. Implementation of MacApplicationListener.
     */
    @Override
	public void macHandleQuit() {
        FrameManager.getInstance().exit();
    }

    /**
     * Called when the running application is made the active application
     * (receives focus).  Do nothing.
     *
     * @param fileName the path to a file or null
     */
    @Override
	public void macHandleReOpenApplication(String fileName) {
        //macHandleOpenFile (fileName);
    }
}

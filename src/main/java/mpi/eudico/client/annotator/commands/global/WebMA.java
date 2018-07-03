package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.util.UrlOpener;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;


/**
 * Attempts to open an url in the default browser.
 * 
 * @author Han Sloetjes
 * @version 1.0
 * @version 2.0 Dec 2012 Updated to use the Java 1.6 Desktop integration
  */
public class WebMA extends FrameMenuAction {
    private String url;

    /**
     * Creates a new WebMA instance
     *
     * @param name name of the action
     * @param frame the parent frame
     * @param webpageURL the url to jump to
     */
    public WebMA(String name, ElanFrame2 frame, String webpageURL) {
        super(name, frame);
        url = webpageURL;
    }

    /**
     * Opens the web page in the default web browser of the system.
     *
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    	try {
			UrlOpener.openUrl(url);
		} catch(Exception exc) {
			errorMessage(exc.getMessage());
		}
		/* old implementation, pre Java 1.6
        if (SystemReporting.isMacOS()) {
        	String[] command = new String[] { "open", url };
            
            String error = execCommand(command);
            if (error != null) {
            	errorMessage(error);
            }
        } else if (SystemReporting.isWindows()) {
        	//String[] command = new String[] { "cmd.exe", "/c", "start", url };
        	String[] command = new String[] { "rundll32", "url.dll", "FileProtocolHandler", url };
            String error = execCommand(command);
            if (error != null) {
            	errorMessage(error);
            }
        } else {// linux, try multiple variants
        	String[][] commands = new String[][] {
        		new String[] { "xdg-open", url },
        		new String[] { "gnome-open", url },
        		new String[] { "kde-open", url },
                new String[] { "firefox", url }
        	};
        	String error = "";
        	for (int i = 0; i < commands.length; i++) {
        		String nextError = execCommand(commands[i]);
        		if (nextError == null) {
        			return;
        		} else {
        			error = error + ", " + nextError;
        		}
        	}
        	errorMessage(error);
        }
        */
    }

    private void errorMessage(String message) {
        JOptionPane.showMessageDialog(frame,
            (ElanLocale.getString("Message.Web.NoConnection") + ": " + message),
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Executes the command.
     * 
     * @param command the command string array
     * @return null if the command was executed successfully, an error message otherwise
     */
    /*  old implementation, pre Java 1.6
    private String execCommand(String[] command) {
        try {
            Process proc = Runtime.getRuntime().exec(command);
            
            try {
            	Thread.sleep(100);
            } catch (InterruptedException ie) {
            	// ignore
            }
            //proc.destroy();//doesn't work on Windows
            return null;
        } catch (SecurityException se) {
            ClientLogger.LOG.warning("No connection: " + se.getMessage());
            return se.getMessage();
        } catch (IOException ioe) {
            ClientLogger.LOG.warning("No connection: " + ioe.getMessage());
            return ioe.getMessage();
        }
    }
    */
}

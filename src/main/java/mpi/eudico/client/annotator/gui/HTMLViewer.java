package mpi.eudico.client.annotator.gui;

import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Frame;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.util.ClientLogger;

/**
 * Utility class for creating a Frame or a Dialog with a simple html viewer in a scrollpane.
 * Based on the SimpleHTMLViewer in the old nl.mpi.util package
 * 
 * @author Han Sloetjes
 */
public class HTMLViewer implements HyperlinkListener {
	private URL url;
	private String title;
	private boolean modal;
	private final JEditorPane htmlPane;
	
	/**
	 * Constructor, throws an exception in case of an io error
	 * @param urlString the url as string
	 * @param modal modal flag
	 * @param title the title for from or dialog
	 * @throws IOException when the file cannot be found
	 */
	public HTMLViewer(String urlString, boolean modal, String title) throws IOException {
		super();
		// first try if this is a local resource
		url = HTMLViewer.class.getResource(urlString);
		if (url == null) {
			try {
				url = new URL(urlString);
			} catch (MalformedURLException mue) {
				throw new IOException("Cannot load file: " + mue.getMessage());
			}
		}
		
		this.title = title;
		this.modal = modal;
		
		htmlPane = new JEditorPane(url);
		htmlPane.setEditable(false);
		htmlPane.addHyperlinkListener(this);
	}

	/**
	 * Creates a dialog with the specified frame as parent.
	 * 
	 * @param parent the parent frame
	 * @return a dialog
	 */
	public JDialog createHTMLDialog(Frame parent) {
		JDialog dialog = new ClosableDialog(parent, title, modal);
		JScrollPane scrollPane = new JScrollPane(htmlPane);
		dialog.getContentPane().add(scrollPane);
		
		return dialog;
	}
	
	
	/**
	 * Creates a dialog with the specified dialog as parent.
	 * 
	 * @param parent the parent dialog
	 * @return a dialog
	 */
	public JDialog createHTMLDialog(Dialog parent) {
		JDialog dialog = new ClosableDialog(parent, title, modal);
		JScrollPane scrollPane = new JScrollPane(htmlPane);
		dialog.getContentPane().add(scrollPane);
		
		return dialog;
	}
	
	/**
	 * For a frame the modal flag is used to determine whether the frame 
	 * should always be on top.
	 * 
	 * @return a Frame for the html page
	 */
	public JFrame createHTMLFrame() {
		JFrame frame = new JFrame(title);
		JScrollPane scrollPane = new JScrollPane(htmlPane);
		frame.setAlwaysOnTop(modal);
		frame.getContentPane().add(scrollPane);
		
		return frame;
	}
	
	/**
	 * Handles hyperlink events. 
	 *
	 */
	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			//only care if link is to same document
			if (e.getURL().sameFile(url)) {
				try {
					htmlPane.scrollToReference(e.getURL().getRef());
				} catch (Throwable t) {
					//t.printStackTrace();
				}
			} else {
				// try to load the external link?
				// Makes no sense if it is not possible to go back
				// Open in browser instead?
				/*
				try {
					htmlPane.setPage(e.getURL());
				} catch (IOException ioe) {
					// warn?
				}
				*/
				// in Java 1.6 or higher: use Desktop to browse the file in a browser
		        URI uri;
				try {
					uri = new URI(e.getURL().toExternalForm());
					Desktop.getDesktop().browse(uri);				
				} catch (URISyntaxException use) {
					ClientLogger.LOG.warning("Error opening webpage: " + use.getMessage());
					errorMessage(use.getMessage());
					//use.printStackTrace();
				} catch (IOException ioe) {
					ClientLogger.LOG.warning("Error opening webpage: " + ioe.getMessage());
					errorMessage(ioe.getMessage());
					//ioe.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Gives access to the actual html/editor pane so that clients can change 
	 * the configuration of the viewer.
	 * 
	 * @return the HTML pane
	 */
	public JEditorPane getHTMLPane() {
		return htmlPane;
	}
	
    private void errorMessage(String message) {
        JOptionPane.showMessageDialog(htmlPane,
            (ElanLocale.getString("Message.Web.NoConnection") + ": " + message),
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

}

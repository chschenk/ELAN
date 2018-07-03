package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.LogTextFrame;
import mpi.eudico.client.annotator.util.ClientLogger;
//import mpi.eudico.client.annotator.util.LogBufferHandler;
import mpi.eudico.util.ErrOutLogFileHandler;

/**
 * An action for creating a simple viewer for ELAN log messages.
 */
@SuppressWarnings("serial")
public class ShowLogMA extends FrameMenuAction {

	/**
	 * @param name name of the action
	 * @param frame the parent frame
	 */
	public ShowLogMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}

	/**
	 * Tries to get the log from the log buffer.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			// test
			Enumeration<String> en = LogManager.getLogManager().getLoggerNames();
			boolean handlerFound = false;
			
			while(en.hasMoreElements()) {
				Logger l = LogManager.getLogManager().getLogger(en.nextElement());
				Handler[] h = l.getHandlers();
				
				for (int i = 0; i < h.length; i++) {
					if (h[i] instanceof ErrOutLogFileHandler) {
						ErrOutLogFileHandler target = (ErrOutLogFileHandler) h[i];
						String fileName = "";
						handlerFound = true;
						try {
							String s = target.getCurrentContent();
							showLog(s);

						} catch (IOException ioe) {
							ClientLogger.LOG.warning("Cannot read from the log file: " + 
									fileName + " : " + ioe.getMessage());
							showMessage(ElanLocale.getString("ExportDialog.LogView.Message") + "\n" +
									fileName + " : " + ioe.getMessage());
							break;
						} catch (Exception ex) {
							ClientLogger.LOG.warning("Cannot read from the log file: " + 
									fileName + " : " + ex.getMessage());
							showMessage(ElanLocale.getString("ExportDialog.LogView.Message") + "\n" + 
									fileName + " : " + ex.getMessage());
							break;
						}
						break;
					}
				}
			}
			
			if (!handlerFound) {
				showMessage(ElanLocale.getString("ExportDialog.LogView.Message") + "\n" + 
						ElanLocale.getString("ExportDialog.LogView.Message2"));
			}

		} catch (Exception exc) {
			// popup message
			showMessage(ElanLocale.getString("ExportDialog.LogView.Message") + "\n" + exc.getMessage());
		}
	}

	/**
	 * Creates a frame for displaying the log.
	 * @param log the current log contents.
	 */
	private void showLog(String log) {
		if (log != null) {
			LogTextFrame ltframe = new LogTextFrame(ElanLocale.getString("ExportDialog.LogView.Title"), 
					log);
			ltframe.pack();
			ltframe.setLocationRelativeTo(frame);
			ltframe.setVisible(true);
		}
	}
	
	/** 
	 * Displays a warning message.
	 * 
	 * @param message
	 */
	private void showMessage(String message) {
		JOptionPane.showMessageDialog(frame, 
				message, 
				ElanLocale.getString("Warning"), 
				JOptionPane.WARNING_MESSAGE);
	}
	
}

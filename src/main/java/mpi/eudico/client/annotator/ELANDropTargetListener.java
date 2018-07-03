package mpi.eudico.client.annotator;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.commands.global.NewMA;
import mpi.eudico.client.annotator.commands.global.OpenMA;
import mpi.eudico.client.annotator.turnsandscenemode.TaSFrame;
import mpi.eudico.client.annotator.turnsandscenemode.commands.NewTaSMA;
import mpi.eudico.client.annotator.turnsandscenemode.commands.OpenTaSMA;
import mpi.eudico.client.annotator.util.ClientLogger;

/**
 * A listener for drag and drop events. 
 * Initial purpose is to allow to start a new transcription by dropping one 
 * or more media files on an ELAN window. And to allow to open an eaf file
 * by drag and drop.
 * 
 * Importing files could be considered as well, for file types that don't need
 * configuration or can use some default configuration.
 * 
 * @author Han Sloetjes
 */
public class ELANDropTargetListener implements DropTargetListener {

	/**
	 * Constructor.
	 */
	public ELANDropTargetListener() {
		super();
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		// stub, for now

	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		// Some (very basic) checking on the data to indicate accept/reject
		if (!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			dtde.rejectDrag();
		} else {
			dtde.acceptDrag(DnDConstants.ACTION_REFERENCE);
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// stub

	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		// stub, nothing to do
	}

	/**
	 * Depending on what has been dropped on the window a new transcription can be
	 * created or an existing transcription can be opened.
	 * A check is made on the type of frame that is the source of the event so that
	 * the appropriate action can be invoked. 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent dtde) {

		if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			for (DataFlavor df : dtde.getCurrentDataFlavors()) {
				if (df.isFlavorJavaFileListType()) {
					dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
					try {
						Transferable transferable = dtde.getTransferable();
						List<File> allFiles = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
						// if there are .eaf files in the list, open the first one (or all)?
						File eafFile = null;
						for (File f : allFiles) {
							if (f.getName().toLowerCase().endsWith(".eaf")) {
								eafFile = f;
								break;
							}
						}
						
						// the following assumes the source is a DropTarget instance
						Window frame = SwingUtilities.getWindowAncestor(((DropTarget) dtde.getSource()).getComponent());
						// open an eaf file if it exists, forget about the rest
						if (eafFile != null) {
							if (frame instanceof TaSFrame) {
								OpenTaSMA openTasAction = new OpenTaSMA("Open TaS Frame", (TaSFrame) frame);
								//openTasAction.createFrameForPath(eafFile.getAbsolutePath());
								openFrameLater(openTasAction, eafFile.getAbsolutePath());
							} else if (frame instanceof ElanFrame2) {
								OpenMA openTransAction = new OpenMA("Open ElanFrame", (ElanFrame2) frame);
								//openTransAction.createFrameForPath(eafFile.getAbsolutePath());
								openFrameLater(openTransAction, eafFile.getAbsolutePath());
							}
							dtde.dropComplete(true);
							return;
						}
						
						// if there are other files in the list treat them as selected via File->New:
						// try to create a new transcription assuming the files are either media files or a template
						final List<String> fileNames = new ArrayList<String>(allFiles.size());
						for (File f : allFiles) {
							fileNames.add(f.getAbsolutePath());
						}

						if (frame instanceof TaSFrame) {
							final NewTaSMA tasAction = new NewTaSMA("New TaS Frame", (TaSFrame) frame);
							//tasAction.createNewFile(fileNames);
							createNewFrameLater(tasAction, fileNames);
						} else if (frame instanceof ElanFrame2) {
							final NewMA nextTransAction = new NewMA("New ElanFrame", (ElanFrame2) frame);
							//nextTransAction.createNewFile(fileNames);
							createNewFrameLater(nextTransAction, fileNames);
						}
						
						dtde.dropComplete(true);
					} catch (UnsupportedFlavorException ufe) {
						// error message?
						ClientLogger.LOG.warning("Unable to handle the dropped data: " + ufe.getMessage());
						dtde.dropComplete(false);
					} catch (IOException ioe) {
						ClientLogger.LOG.warning("Unable to handle the dropped data: " + ioe.getMessage());
						dtde.dropComplete(false);
					} catch (Throwable t) {
						ClientLogger.LOG.warning("Unable to handle the dropped data: " + t.getMessage());
						dtde.dropComplete(false);
					}
				}
			}
		}

	}

	/**
	 * Creates a new transcription and frame on the AWT event queue.
	 * 
	 * @param createAction the action to execute
	 * @param mediaFiles the media files to link
	 */
	private void createNewFrameLater(final NewMA createAction, final List<String> mediaFiles) {
		EventQueue.invokeLater(new Runnable() {			
			@Override
			public void run() {
				createAction.createNewFile(mediaFiles);									
			}
		});
	}
	
	/**
	 * Opens an eaf on the AWT event queue.
	 * @param openAction the open action
	 * @param filePath the file to open
	 */
	private void openFrameLater(final OpenMA openAction, final String filePath) {
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				openAction.createFrameForPath(filePath);
				
			}
		});
	}
}

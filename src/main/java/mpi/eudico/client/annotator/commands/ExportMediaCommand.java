package mpi.eudico.client.annotator.commands;


import java.io.IOException;
import java.util.List;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;

/**
 * 
 */
public class ExportMediaCommand implements Command {
	private String commandName;

	/**
	 * Creates a new ExportSmilCommand instance
	 *
	 * @param theName DOCUMENT ME!
	 */
	public ExportMediaCommand(String theName) {
		commandName = theName;
	}

	/**
	 * <b>Note: </b>it is assumed the types and order of the arguments are
	 * correct.
	 * 
	 * For the time being only the master media is exported
	 * M2-edeit-cl only supports mpeg1 and mpeg2 files, 
	 * for wav clips use java code based on wav reader code
	 *
	 * @param receiver null
	 * @param arguments the arguments:  <ul><li>arg[0] = the Viewer Manager
	 *        (ViewerManager2)</li> </ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		ViewerManager2 viewerManager = (ViewerManager2) arguments[0];
		
		if (viewerManager != null) {
			String executable = "M2-edit-cl";
			if (arguments.length > 1) {
				executable = (String) arguments[1];
			}
			Selection selection = viewerManager.getSelection();
			if (selection != null) {
				// do something to point to source media and target media file name
				// and maybe also give the option to define a time interval independent of the current selection
				// for the time being the clip is saved in the fiel pointed to by a file chooser	
				
				FileChooser chooser = new FileChooser(ELANCommandFactory.getRootFrame(viewerManager.getTranscription()));
				chooser.createAndShowFileDialog(null, FileChooser.SAVE_DIALOG, null, "MediaDir");

	            if (chooser.getSelectedFile() != null) {
					try {
						String saveFileName = chooser.getSelectedFile().getAbsolutePath();
					
						// check for extension
						if (!saveFileName.toLowerCase().endsWith(".mpg") && !saveFileName.toLowerCase().endsWith(".mpeg")) {
							saveFileName += ".mpg";
						}						
						
						new ClipThread(viewerManager, saveFileName, executable).start();
						
					} catch (Exception e) {
						e.printStackTrace();
					}
	            }
			} 
		}
	}

	/**
	 * Returns the name
	 *
	 * @return the name
	 */
	@Override
	public String getName() {
		return commandName;
	}
	
	/**
	 * Start clipping by M2-edit in a separate thread. Waiting for the process to end 
	 * might halt the application.
	 */
	private class ClipThread extends Thread {
		ViewerManager2 viewerManager;
		String saveFileName;
		String executable = "M2-edit-cl";
		
		public ClipThread(ViewerManager2 viewerManager, String saveFileName, String exName) {
			super();
			this.viewerManager = viewerManager;
			this.saveFileName = saveFileName;
			if (exName != null) {
				executable = exName;
			}
		}

		@Override
		public void run() {
			if (viewerManager == null || saveFileName == null) {
				return;
			}
			ElanMediaPlayer player = viewerManager.getMasterMediaPlayer();
			long offset = player.getOffset();
			Selection selection = viewerManager.getSelection();
			double frameDuration = player.getMilliSecondsPerSample();
			long beginFrame = (long)((offset + selection.getBeginTime()) / frameDuration);
			long endFrame = (long) Math.ceil(((double) offset + selection.getEndTime()) / frameDuration);
			
			// M2-edit-class can handle unix type path delimiters, but when the file is on 
			// a network share it should start with "\\" instead of "//"
			String sourceFileName = player.getMediaDescriptor().mediaURL.substring(5);
			if (sourceFileName.startsWith("///")) {
				sourceFileName = sourceFileName.substring(3);
			} else {
				// network share, starting with //share/folder...
				sourceFileName.replace('/', '\\');
			}
			
			String[] command = new String[5];
			//command[0] = "M2-edit-cl";
			command[0] = executable;
			command[1] = "/in:" + beginFrame;
			command[2] = "/out:" + endFrame;
			command[3] = sourceFileName;
			command[4] = saveFileName;
			ClientLogger.LOG.info("Clip command: " + command[0] + " " + command[1] 
			      + " " + command[2] + " " + command[3] + " " + command[4]);
			
			try {
				Process pr = Runtime.getRuntime().exec(command);				
				int exit = pr.waitFor();
				if (exit != 0) {
					ClientLogger.LOG.warning("Clipping exited abnormally: " + exit);
				}
				ClientLogger.LOG.info("exported clip:" + saveFileName);
			} catch (InterruptedException ie) {
				ClientLogger.LOG.warning("Clip error: " + ie.getMessage());
			} catch (IOException ioe) {
				ClientLogger.LOG.warning("Clip error: " + ioe.getMessage());
			}
			
			// apr 2008 HS: also export other videos
			List<ElanMediaPlayer> slavePlayers = viewerManager.getSlaveMediaPlayers();
			MediaDescriptor md;
			ElanMediaPlayer pl;
			for (int i = 0; i < slavePlayers.size(); i++) {
				pl = slavePlayers.get(i);
				md = pl.getMediaDescriptor();
				offset = pl.getOffset();
				if (md == null) {
					continue;
				}
				//System.out.println("Player: " + md.mediaURL);
				String sourceName = md.mediaURL.substring(5);
				if (sourceName.startsWith("///")) {
					sourceName = sourceName.substring(3);
				} else {
					// network share, starting with //share/folder...
					sourceName.replace('/', '\\');
				}
				int stopIndex = saveFileName.lastIndexOf('.');
				String saveName = saveFileName.substring(0, stopIndex) + "_" + (i) + 
				    saveFileName.substring(stopIndex);
				
				double frameDur = player.getMilliSecondsPerSample();
				long beginFr = (long)((offset + selection.getBeginTime()) / frameDur);
				long endFr = (long) Math.ceil(((double) offset + selection.getEndTime()) / frameDur);
				
				String[] comm = new String[5];
				//comm[0] = "M2-edit-cl";
				comm[0] = executable;
				comm[1] = "/in:" + beginFr;
				comm[2] = "/out:" + endFr;
				comm[3] = sourceName;
				comm[4] = saveName;
				//System.out.println(comm[0] + " " + comm[1] + " " + comm[2] + " " + comm[3] + " " + comm[4]);
				try {
					Process pr = Runtime.getRuntime().exec(comm);
					
					int exit = pr.waitFor();
					if (exit != 0) {
						ClientLogger.LOG.warning("Clipping exited abnormally: " + exit);
					}
					ClientLogger.LOG.info("exported clip:" + saveName);
				} catch (InterruptedException ie) {
					ClientLogger.LOG.warning("Clip export interrupted: " + ie.getMessage());
				} catch (IOException ioe) {
					ClientLogger.LOG.warning("Clip export failed: " + ioe.getMessage());
				}
			}
		}
		
		
	}
}
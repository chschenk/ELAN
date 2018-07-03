package mpi.eudico.client.annotator.commands;

import java.awt.event.ActionEvent;
import java.io.File;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.SelectionListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.ClipWithScriptUtil;

/**
 * A CommandAction for creating a clip from media files by means of calling a script.
 */
@SuppressWarnings("serial")
public class ClipMediaCA extends CommandAction implements SelectionListener {
	private File scriptFile;
	// the executable part of the script
	private String executable;
	// the parameter part of the script
	private String paramLine;
	private final String scriptFileName = "clip-media.txt";
	private long lastModified = 0L;
	
	private String outFilePath = null;
	private ClipWithScriptUtil scriptUtil;
	
	/**
	  * Creates a new ClipMediaCA instance
	  *
	  * @param viewerManager vm
	  */
	public ClipMediaCA(ViewerManager2 viewerManager) {
		super(viewerManager, ELANCommandFactory.CLIP_MEDIA);
		scriptUtil = new ClipWithScriptUtil();
		
		try {
			scriptFile = scriptUtil.getScriptFile(scriptFileName);
			if (scriptFile != null) {
				//lastModified = scriptFile.lastModified();
				ClientLogger.LOG.info("Found clipping script: " + scriptFile.getName());
			} else {
				ClientLogger.LOG.info("No clipping script found!");
			}
			
		} catch (Exception e) {
			ClientLogger.LOG.info("No clipping script found");
		}	 
		 	 
	     viewerManager.connectListener(this);
	 }
	 
	 /**
	  * Creates a new ClipMedia command
	  */
	 @Override
	protected void newCommand() {
		 command = ELANCommandFactory.createCommand(vm.getTranscription(),
				 ELANCommandFactory.CLIP_MEDIA);
	 }
	 
	 /**
	  * There's no logical receiver for this CommandAction.
	  *
	  * @return null
	  */
	 public void setPath(String outFilePath) {
		 this.outFilePath = outFilePath;
	 }

	 /**
	  * There's no logical receiver for this CommandAction.
	  *
	  * @return null
	  */
	 @Override
	protected Object getReceiver() {
		 return null;
	 }

	 /**
	  * Returns an array of arguments.
	  *
	  * @return the arguments
	  */
	 @Override
	protected Object[] getArguments() {
		 if (scriptFile == null) {
			 return new Object[] { vm, new Exception(ElanLocale.getString("ExportClipDialog.Message.NoScript") + 
					 "\n" + ElanLocale.getString("ExportClipDialog.Message.LookingFor") +
					 "\n" + (System.getProperty("user.dir") + File.separator + scriptFileName) +
					 "\n" + (Constants.ELAN_DATA_DIR + File.separator + scriptFileName)) };
		 } else if (executable == null) {
			 return new Object[] { vm, new Exception(ElanLocale.getString("ExportClipDialog.Message.InvalidScript")) };
		 }
		 
		 if(outFilePath != null){
			 return new Object[] { vm, executable, paramLine, outFilePath };
		 }else{
			 return new Object[] { vm, executable, paramLine };
		 }
			 
		 
	 }

	 // only activate menu item when selection is made
	 // if the user may define a time interval in a dialog this is obsolete
	 @Override
	public void updateSelection() {
 		if (vm.getSelection().getEndTime() > vm.getSelection().getBeginTime()) {
 			// could check the media players and/or the media descriptors
 			setEnabled(true);
 		} else {
 			setEnabled(false);
 		}
	 }

	/**
	 * Checks if a script has already been found and if so checks the last modified value.
	 * If needed the script is read again.
	 * 
	 * @see mpi.eudico.client.annotator.commands.CommandAction#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		// (re) check if there is a script file and check the last modified field
		if (scriptFile == null) {
			scriptFile = scriptUtil.getScriptFile(scriptFileName);
			if (scriptFile != null) {
				lastModified = scriptFile.lastModified();
				String[] scriptParts = scriptUtil.parseScriptLine(scriptFile);
				if (scriptParts != null && scriptParts.length >= 2) {
					executable = scriptParts[0];
					paramLine = scriptParts[1];
				}
			}
		} else {
			long lm = scriptFile.lastModified();
			if (lm > lastModified) {
				String[] scriptParts = scriptUtil.parseScriptLine(scriptFile);
				if (scriptParts != null && scriptParts.length >= 2) {
					executable = scriptParts[0];
					paramLine = scriptParts[1];
				}
				lastModified = lm;
			}
		}
		super.actionPerformed(event);
	}
	 
	 
}

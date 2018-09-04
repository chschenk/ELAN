package mpi.eudico.client.annotator.commands;

import java.io.IOException;

import mpi.eudico.client.annotator.SelectionListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.util.ClientLogger;

/**
 *
 */
public class ExportMediaCA extends CommandAction implements SelectionListener {
	private boolean available;
	private String executable = "M2-edit-cl";
	
	/**
	  * Creates a new ExportMediaCA instance
	  *
	  * @param viewerManager DOCUMENT ME!
	  */
	public ExportMediaCA(ViewerManager2 viewerManager) {
		super(viewerManager, ELANCommandFactory.EXPORT_MEDIA);

		try {
			// check if possible, for the time being only looks for one tool
			Runtime.getRuntime().exec("M2-edit-cl");
			ClientLogger.LOG.info("Found executable version of M2-edit-cl");
			available = true;
		} catch (IOException e) {
			//e.printStackTrace();
			// exception if exec was impossible
			try {
				// check a newer version of M2-edit
				Runtime.getRuntime().exec("M2-xcode-cl");
				executable = "M2-xcode-cl";
				ClientLogger.LOG.info("Found executable version of M2-xcode-cl");
				available = true;
			} catch(IOException ee) {
				available = false;
				ClientLogger.LOG.info("No version of M2-edit-cl or M2-xcode-cl found");
			} catch (Exception ex) {
				available = false;
				ClientLogger.LOG.info("No version of M2-edit-cl or M2-xcode-cl found");
			}
		}	 
		 	 
	     viewerManager.connectListener(this);
	     
		 setEnabled(available);
	 }

	public boolean isAvailable() {
		return available;
	}
	 
	 /**
	  * DOCUMENT ME!
	  */
	 @Override
	protected void newCommand() {
		 command = ELANCommandFactory.createCommand(vm.getTranscription(),
				 ELANCommandFactory.EXPORT_MEDIA);
	 }

	 /**
	  * There's no logical receiver for this CommandAction.
	  *
	  * @return DOCUMENT ME!
	  */
	 @Override
	protected Object getReceiver() {
		 return null;
	 }

	 /**
	  * DOCUMENT ME!
	  *
	  * @return DOCUMENT ME!
	  */
	 @Override
	protected Object[] getArguments() {
		 return new Object[] { vm, executable };
	 }

	 // only activate menu item when selection is made
	 // if the user may define a time interval in a dialog this is obsolete
	 @Override
	public void updateSelection() {
	 	if (available) {
	 		if (vm.getSelection().getEndTime() > vm.getSelection().getBeginTime()) {
	 			setEnabled(true);
	 		} else {
	 			setEnabled(false);
	 		}
	 	}
	 }
}

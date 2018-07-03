package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Created on Apr 15, 2004
 * @author Alexander Klassmann
 * @version Apr 15, 2004
 */
public class ExportSmilCA extends CommandAction {
	/**
	  * Creates a new ExportSmilCA instance
	  *
	  * @param viewerManager DOCUMENT ME!
	  */
	 public ExportSmilCA(ViewerManager2 viewerManager) {
		 super(viewerManager, ELANCommandFactory.EXPORT_SMIL_RT);
	 }

	 /**
	  * DOCUMENT ME!
	  */
	 @Override
	protected void newCommand() {
		 command = ELANCommandFactory.createCommand(vm.getTranscription(),
				 ELANCommandFactory.EXPORT_SMIL_RT);
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
		 return new Object[] { vm.getTranscription(), vm.getSelection() };
	 }

}

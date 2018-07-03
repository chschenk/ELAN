package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Created on Oct 8, 2010
 * @author Aarthy
 * @version  Oct 8, 2010
 */
public class ExportSmilQTCA extends CommandAction {
	/**
	  * Creates a new ExportSmilQTCA instance
	  *
	  * @param viewerManager DOCUMENT ME!
	  */
	 public ExportSmilQTCA(ViewerManager2 viewerManager) {
		 super(viewerManager, ELANCommandFactory.EXPORT_SMIL_QT);
	 }

	 /**
	  * DOCUMENT ME!
	  */
	 @Override
	protected void newCommand() {
		 command = ELANCommandFactory.createCommand(vm.getTranscription(),
				 ELANCommandFactory.EXPORT_SMIL_QT);
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

package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Created on Jul 2, 2004
 * @author Alexander Klassmann
 * @version Jul 2, 2004
 */
public class ExportQtSubCA extends CommandAction {
	/**
	 * Creates a new ExportTabDelDlgCA instance
	 *
	 * @param viewerManager DOCUMENT ME!
	 */
	public ExportQtSubCA(ViewerManager2 viewerManager) {
		super(viewerManager, ELANCommandFactory.EXPORT_QT_SUB);
	}

	/**
	 * DOCUMENT ME!
	 */
	@Override
	protected void newCommand() {
		command = ELANCommandFactory.createCommand(vm.getTranscription(),
				ELANCommandFactory.EXPORT_QT_SUB);
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

package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import java.awt.event.ActionEvent;


/**
 * DOCUMENT ME! $Id: RedoCA.java 43915 2015-06-10 09:02:42Z olasei $
 *
 * @author $Author$
 * @version $Revision$
 */
public class RedoCA extends CommandAction {
    private CommandHistory commandHistory;

    /**
     * Creates a new RedoCA instance
     *
     * @param theVM DOCUMENT ME!
     * @param theHistory DOCUMENT ME!
     */
    public RedoCA(ViewerManager2 theVM, CommandHistory theHistory) {
        super(theVM, ELANCommandFactory.REDO);
        commandHistory = theHistory;

        setEnabled(false); // initially disable
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param event DOCUMENT ME!
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        commandHistory.redo();
    }
}

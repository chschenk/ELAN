package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clom.Tier;


/**
 * DOCUMENT ME!
 * $Id: SetTierNameCommand.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class SetTierNameCommand implements UndoableCommand {
    private String commandName;

    // store state for undo and redo
    private Tier t;
    private String newTierName;
    private String oldTierName;

    /**
     * Creates a new SetTierNameCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public SetTierNameCommand(String theName) {
        commandName = theName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param receiver DOCUMENT ME!
     * @param arguments DOCUMENT ME!
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void undo() {
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void redo() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getName() {
        return commandName;
    }
}

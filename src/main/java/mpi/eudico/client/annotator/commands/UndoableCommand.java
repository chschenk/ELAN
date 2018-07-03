package mpi.eudico.client.annotator.commands;

/**
 * DOCUMENT ME!
 * $Id: UndoableCommand.java 4129 2005-08-03 15:01:06Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public interface UndoableCommand extends Command {
    /**
     * DOCUMENT ME!
     */
    public void undo();

    /**
     * DOCUMENT ME!
     */
    public void redo();
}

package mpi.eudico.client.annotator.commands;

/**
 * DOCUMENT ME!
 * $Id: Command.java 4129 2005-08-03 15:01:06Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public interface Command {
    /**
     * DOCUMENT ME!
     *
     * @param receiver DOCUMENT ME!
     * @param arguments DOCUMENT ME!
     */
    public void execute(Object receiver, Object[] arguments);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName();
}

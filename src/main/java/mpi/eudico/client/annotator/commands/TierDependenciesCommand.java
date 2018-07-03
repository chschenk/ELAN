package mpi.eudico.client.annotator.commands;

import javax.swing.JFrame;


/**
 *
 */
public class TierDependenciesCommand implements Command {
    private String commandName;
    private JFrame dependencyFrame;

    /**
     * Creates a new TierDependenciesCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public TierDependenciesCommand(String name) {
        commandName = name;
    }

    /**
     *
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        dependencyFrame = (JFrame) (arguments[0]);

        showTierDependencies();
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

    private void showTierDependencies() {
        if (dependencyFrame != null) {
            //Next line useable as of JDK 1.4
            //dependencyFrame.setExtendedState(Frame.NORMAL);
            dependencyFrame.setVisible(true);
            dependencyFrame.toFront();
        }
    }
}

package mpi.eudico.client.annotator.commands;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.commands.CommandAction;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;

/**
 * 
 * A CommandAction that creates AddCommenCommands.
 * 
 * @author Olaf Seibert
 *
 */
@SuppressWarnings("serial")
public class AddCommentCA extends CommandAction 
						  implements ActiveAnnotationListener {
    /**
	 * The Annotation that may get a comment.
	 */
	private Annotation activeAnnotation;
    
	public AddCommentCA(ViewerManager2 viewerManager) {
		super(viewerManager, ELANCommandFactory.ADD_COMMENT);

        viewerManager.connectListener(this);
        setEnabled(false);

		// TODO Auto-generated constructor stub
	}

	@Override
	protected void newCommand() {
		command =  ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.ADD_COMMENT);
	}

	/**
     * The receiver of this CommandAction is the ??? object on which the
     * new comment should be created.
     *
     * @return the receiver for the related command
     */
	@Override
    protected Object getReceiver() {
        return null;
    }

    /**
     * Returns the arguments for the related Command.
     *
     * @return the arguments for the related Command
     */
	@Override
    protected Object[] getArguments() {
        Object[] args = new Object[1];
        args[0] = activeAnnotation;

        return args;
    }

    /**
     * Check if there is an active annotation first.
     *
     * @param event the action event
     */
	@Override
    public void actionPerformed(ActionEvent event) {
		if (activeAnnotation != null) {
			super.actionPerformed(event);
		}
    }

    /**
     * On a change of ActiveAnnotation perform a check to determine whether
     * this action should be enabled or disabled.<br>
     * This depends on the type of the annotation and the type of the Tier it
     * belongs to.
     *
     * @see ActiveAnnotationListener#updateActiveAnnotation()
     */
    @Override
	public void updateActiveAnnotation() {
        activeAnnotation = vm.getActiveAnnotation().getAnnotation();
        checkState();
    }

    /**
     * Enables or disables this <code>Action</code> depending on the characteristics
     * of the active annotation (and therefore the tier it is on), if any.
     * In this case it only checks if there is an active annotation at all.
     */
    protected void checkState() {
        setEnabled(activeAnnotation != null);
    }
}

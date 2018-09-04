package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.comments.CommentEnvelope;
import mpi.eudico.client.annotator.comments.CommentManager;

/**
 * 
 * A command that initiates the process of adding a comment.
 * 
 * @author Olaf Seibert
 *
 */
public class DeleteCommentCommand implements UndoableCommand {

	 private String commandName;
	 private CommentManager commentManager;
     private CommentEnvelope oldComment;     

	/**
 	 * A command to add a comment to an annotation or time slice.
	 *
	 * @param name the name of the command
	 */
	 public DeleteCommentCommand(String name) {
	     commandName = name;	       
	 }
	 
	@Override
	public void execute(Object receiver, Object[] arguments) {
        if (receiver instanceof CommentManager &&
                arguments != null &&
                arguments.length >= 1 &&
                arguments[0] instanceof Integer) {

            commentManager = (CommentManager)receiver;
            int removeIndex = (Integer)arguments[0];

            oldComment = commentManager.get(removeIndex);
        	commentManager.remove(removeIndex);
        }
	}

	 /**
	   * Returns the name of the command.
	   *
	   * @return the name of the command
	   */
	@Override
	 public String getName() {
		 return commandName;
	 }

    /**
     * The undo action: re-insert the comment
     */
	@Override
	public void undo() {
        if (oldComment != null) {
            int insertPosition = commentManager.insert(oldComment);
        }
	}

    /**
     * The redo action. Find the comment again, based on its ID.
     */
	@Override
	public void redo() {
        if (oldComment != null) {
        	int index = commentManager.findCommentById(oldComment.getMessageID());
        	if (index >= 0) {
        		commentManager.remove(index);
        	}
        }
	}
}

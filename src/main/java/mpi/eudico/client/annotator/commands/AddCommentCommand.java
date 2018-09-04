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
public class AddCommentCommand implements UndoableCommand  {

	 private String commandName;
	 private CommentManager commentManager;
     private CommentEnvelope newComment;
     private int insertPosition;
     

	/**
 	 * A command to add a comment to an annotation or time slice.
	 *
	 * @param name the name of the command
	 */
	 public AddCommentCommand(String name) {
	     commandName = name;	       
	 }
	 
	@Override
	public void execute(Object receiver, Object[] arguments) {
        if (receiver instanceof CommentManager &&
                arguments != null &&
                arguments.length >= 1 &&
                arguments[0] instanceof CommentEnvelope) {

            commentManager = (CommentManager)receiver;
            newComment = (CommentEnvelope)arguments[0];

            redo();
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
     * The undo action: find the comment by its ID and remove it.
     */
	@Override
	public void undo() {
        if (newComment != null) {
        	int index = commentManager.findCommentById(newComment.getMessageID());
        	commentManager.remove(index);
        }
	}

    /**
     * The redo action. Insert the comment again.
     */
	@Override
	public void redo() {
        if (newComment != null) {
            insertPosition = commentManager.insert(newComment);
        }
	}
	
	public int getInsertPosition() {
		return insertPosition;
	}
}

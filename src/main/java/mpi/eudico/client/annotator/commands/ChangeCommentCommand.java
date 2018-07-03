package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.comments.CommentEnvelope;
import mpi.eudico.client.annotator.comments.CommentManager;

/**
 * 
 * A command that replaces one comment by another modified version of it.
 * 
 * To change a comment's identity, even the MessageID may be changed.
 * Since commentManager.replace() is called, even the time (and thereby
 * the position) may be different.
 * 
 * @author Olaf Seibert
 *
 */
public class ChangeCommentCommand implements UndoableCommand {

	 private String commandName;
	 private CommentManager commentManager;
     private CommentEnvelope oldComment;
     private CommentEnvelope newComment;
     private int insertPosition;
     
	public ChangeCommentCommand(String name) {
	    commandName = name;	       
	}
	
	/**
	 * Change a comment.
	 * <p>
	 * Receiver: the CommentManager.
	 * <p>
	 * Arguments:<br/>
	 * [0] Integer: position of the comment <br/>
	 * [1] CommentEnvelope: new version of it
	 */
	 
	@Override
	public void execute(Object receiver, Object[] arguments) {
        if (receiver instanceof CommentManager &&
                arguments != null &&
                arguments.length >= 2 &&
                arguments[0] instanceof Integer &&
                arguments[1] instanceof CommentEnvelope) {

            commentManager = (CommentManager)receiver;
            int changeIndex = (Integer)arguments[0];
            newComment = (CommentEnvelope)arguments[1];
            
            oldComment = commentManager.get(changeIndex);
           	insertPosition = commentManager.replace(changeIndex, newComment);
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
     * The undo action: put the old comment back in the place of the new comment
     */
	@Override
	public void undo() {
        if (oldComment != null && newComment != null) {
        	int index = commentManager.findCommentById(newComment.getMessageID());
        	if (index >= 0) {
        		commentManager.replace(index, oldComment);
        	}
        }
	}

    /**
     * The redo action. Find the old comment again, based on its ID.
     */
	@Override
	public void redo() {
        if (newComment != null && newComment != null) {
        	int index = commentManager.findCommentById(oldComment.getMessageID());
        	if (index >= 0) {
        		commentManager.replace(index, newComment);
        	}
        }
	}
	
	public int getInsertPosition() {
		return insertPosition;
	}
}

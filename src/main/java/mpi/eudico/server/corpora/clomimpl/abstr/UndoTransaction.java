package mpi.eudico.server.corpora.clomimpl.abstr;

/**
 * <h1>Server-side undo.</h1>
 * <p>
 * The main undo action is still done by UndoableCommands,
 * which are a client-side concept.
 * <p>
 * But these Transactions are built as a side-effect of
 * (for example) deletion of Annotations, where the caller has
 * no idea that other changes are also made.
 * <p>
 * Calling UndoTransaction.undo() will reverse whatever side-effect
 * there was.
 * <p>
 * It is assumed that when some Command.redo() method redoes
 * some undone actions, the side-effects are intrinsically
 * re-occuring, so that there is no need for a RedoTransaction
 * or the like.
 * 
 * @author olasei
 */
public abstract class UndoTransaction {
	private UndoTransaction next;
	
	public UndoTransaction getNext() {
		return next;
	}
	public void setNext(UndoTransaction u) {
		next = u;
	}
	
	public abstract void undo();
}
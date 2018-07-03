package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Stores the command history, for undo and redo actions.
 * Executed commands are added at the end of the history.
 * When executing an undo, a pointer is moved back from the end, indicating which
 * command can now be re-done, and which is the next command to undo.
 * 
 * Once some commands are undone, and a new command is executed, all remaining
 * to-redo commands are discarded.
 */
public class CommandHistory {
    /** The maximum history size to remember */
    public final static int historySize = 25;
    private List<UndoableCommand> history;
    private int nextCommand;		// lowest numbered free or redoable entry
    private UndoCA undoCA;
    private RedoCA redoCA;
    private TranscriptionImpl trans;

    /**
     * Creates a new CommandHistory instance
     *
     * @param size The expected size
     */
    public CommandHistory(int size, Transcription trans) {
        history = new ArrayList<UndoableCommand>(size);
        nextCommand = 0;
        this.trans = (TranscriptionImpl)trans;
    }

    /**
     * Adds the command to the history.
     *
     * @param theCommand DOCUMENT ME!
     */
    public void addCommand(UndoableCommand theCommand) {
        // algorithm:
        // - commands that go over the history size are discarded
        // - insert new command at the end
        // - discard all commands that are more recent than current command
        // - adjust undo and redo command actions

    	// If the history is too full, make space by removing oldest.
    	// 0 <= currentCommand <= history.size()
        if (nextCommand >= historySize) {
        	int surplus = nextCommand - historySize + 1; // usually only 1
        	while (surplus > 0) {
            	history.remove(0);
            	nextCommand--;
            	surplus--;
        	}
        	trans.forgetOldUndoTransactions(historySize - 1);
        }
        
        // Extend the history if needed
        if (nextCommand == history.size()) {
        	history.add(theCommand);
        } else {
        	history.set(nextCommand, theCommand);
        }
        nextCommand++;
        
        // Discard all commands that are newer
        for (int i = nextCommand; i < history.size(); i++) {
        	history.set(i, null);
        }

        // adjust current command
        adjustCurrentCommand();
        
        trans.pushNewUndoTransaction();
    }

    private void adjustCurrentCommand() {
        if (nextCommand > 0) {
            String undoString = ElanLocale.getString("Menu.Edit.Undo");
            undoString += " ";
            undoString += ElanLocale.getString((history.get(
                    nextCommand - 1)).getName());

            undoCA.putValue(Action.NAME, undoString);
            undoCA.setEnabled(true);
        } else {
            undoCA.putValue(Action.NAME, ElanLocale.getString("Menu.Edit.Undo"));
            undoCA.setEnabled(false);
        }

        if ((nextCommand < history.size()) &&
                (history.get(nextCommand) != null)) {
            String redoString = ElanLocale.getString("Menu.Edit.Redo");
            redoString += " ";
            redoString += ElanLocale.getString((history.get(nextCommand)).getName());

            redoCA.putValue(Action.NAME, redoString);
            redoCA.setEnabled(true);
        } else {
            redoCA.putValue(Action.NAME, ElanLocale.getString("Menu.Edit.Redo"));
            redoCA.setEnabled(false);
        }
    }

    /**
     * Runs the undo method of a single command, and adjusts the current command position.
     */
    public void undo() {
        // undo current command
        if (nextCommand > 0) {
            // point to previous
            nextCommand--;

            /*
             * We need to start a new transaction here, otherwise the
             * effects of performing the undo get merged into the
             * current transaction, and will also be undone.
             */
            trans.pushNewUndoTransaction();
            
            history.get(nextCommand).undo();
            
            // Don't undo the undo
            trans.popAndForgetTransaction();
            trans.popAndUndoTransaction();

            // adjust undo and redo command actions
            adjustCurrentCommand();
        }
    }

    /**
     * Runs the redo method of a single command, and adjusts the current command position.
     */
    public void redo() {
        if (nextCommand < history.size() && history.get(nextCommand) != null) {

            trans.pushNewUndoTransaction();
            // redo command
            history.get(nextCommand).redo();

            // point to next
            nextCommand++;

            // adjust undo and redo command actions
            adjustCurrentCommand();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param theUndoCA DOCUMENT ME!
     */
    public void setUndoCA(UndoCA theUndoCA) {
        undoCA = theUndoCA;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theRedoCA DOCUMENT ME!
     */
    public void setRedoCA(RedoCA theRedoCA) {
        redoCA = theRedoCA;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String toString() {
        String s = "\n";

        for (int i = 0; i < history.size(); i++) {
            if (history.get(i) != null) {
                if (i == nextCommand) {
                    s += "-> ";
                } else {
                    s += "   ";
                }

                s += (history.get(i).getName() + "\n");
            }
        }

        return s;
    }
}

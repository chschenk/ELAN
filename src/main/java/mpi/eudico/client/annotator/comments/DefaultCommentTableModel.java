package mpi.eudico.client.annotator.comments;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.util.TimeFormatter;

/**
 * The default implementation of the CommentTableModel.
 * 
 * @see CommentTableModel
 * 
 * @author olasei
 */
@SuppressWarnings("serial")
public class DefaultCommentTableModel extends AbstractTableModel
                                      implements CommentTableModel {

    private List<CommentEnvelope> comments;	// a possibly read-only instance of the underlying comment list
    private CommentManager commentManager = null;
    private static CommentEnvelope noComment = new CommentEnvelope();

    /**
     * Create a model for a comment table (which is a table that shows comments).
     * It needs a reference to the CommentManager, so it can tell it when
     * editing has taken place. If you don't want the comments to be editable,
     * you can pass null.
     */
    public DefaultCommentTableModel(CommentManager cm) {
        super();
        this.comments = new ArrayList<CommentEnvelope>(0);
        this.commentManager = cm;
    }

    /**
     * Tell the Model about the List which contains the comments.
     * It does not make a copy of all comments (or references to comments),
     * the model will work on the main list itself
     * (or likely a read-only instance of it).
     * It fires a TableDataChanged event so that the GUI will update itself.
     * 
     * @param comments
     */
    @Override // CommentTableModel
    public void setComments(List<CommentEnvelope> comments) {
        this.comments = comments;
        fireTableDataChanged();
    }

    /**
     * Get a comment from the backing array.
     * Since this method can be called while the table is getting updated,
     * we need to be careful to check we're using an index which is in range.
     * The backing array can have an element removed, and then the rest of
     * the table will get updated, but parts may still use the removed
     * index.
     * 
     * @param index
     * @return
     */
    @Override // CommentTableModel
    public CommentEnvelope getComment(int index) {
    	if (index >= 0 && index < comments.size()) {
    		return comments.get(index);
    	}
    	return noComment;
    }

    public static final int START_TIME = 0;
    public static final int END_TIME = 1;
    public static final int TIER_NAME = 2;
    public static final int INITIALS = 3;
    public static final int COMMENT = 4;
    public static final int THREAD_ID = 5;
    public static final int SENDER = 6;
    public static final int RECIPIENT = 7;
    public static final int CREATION_DATE = 8;
    public static final int MODIFICATION_DATE = 9;

    // Note: this doesn't update itself when the locale changes.
    private static final String columnNames[] = {
        /* 0 */ ElanLocale.getString("CommentTable.StartTime"),
        /* 1 */ ElanLocale.getString("CommentTable.EndTime"),
        /* 2 */ ElanLocale.getString("CommentTable.Tier"),
        /* 3 */ ElanLocale.getString("CommentTable.Initials"),
        /* 4 */ ElanLocale.getString("CommentTable.Comment"),
        /* 5 */ ElanLocale.getString("CommentTable.Thread"),
        /* 6 */ ElanLocale.getString("CommentTable.Sender"),
        /* 7 */ ElanLocale.getString("CommentTable.Recipient"),
        /* 8 */ ElanLocale.getString("CommentTable.CreationDate"),
        /* 9 */ ElanLocale.getString("CommentTable.ModificationDate"),
    };

    @Override // TableModel
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override // TableModel
    public int getRowCount() {
        return comments.size();
    }

    @Override // TableModel
    public String getColumnName(int col) {
        return columnNames[col];
    }

    /**
     * Returns <code>String.class</code> regardless of column index.
     */
    @Override // TableModel
    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    @Override // TableModel
    public Object getValueAt(int row, int col) {
        CommentEnvelope e = comments.get(row);

        return getValueAtColumn(e, col);
    }

    public static String getValueAtColumn(CommentEnvelope e, int col) {
        switch (col) {
        case START_TIME:
            return TimeFormatter.toString(e.getStartTime());
        case END_TIME:
            return TimeFormatter.toString(e.getEndTime());
        case TIER_NAME:
            return e.getTierName();
        case INITIALS:
            return e.getInitials();
        case COMMENT:
            return e.getMessage();
        case THREAD_ID:
            return e.getThreadID();
        case SENDER:
            return e.getSender();
        case RECIPIENT:
            return e.getRecipient();
        case CREATION_DATE:
            return readableTime(e.getCreationDateString());
        case MODIFICATION_DATE:
            return readableTime(e.getModificationDateString());
        default:
            return "-";
        }
    }
    /**
     * Make a string of the form 1234-56-78T12:34:56Z a bit more human-friendly.
     * @param formal
     * @return
     */
    public static String readableTime(String formal) {
        return formal.replace('T', ' ').replace("Z", " UTC");
    }

    /**
     * Columns INITIALS, COMMENT, THREAD_ID and SENDER are editable,
     * but only if we have a CommentManager.
     */
    @Override // TableModel
    public boolean isCellEditable(int row, int col) {
    	if (commentManager == null) {
    		return false;
    	}
    	
        CommentEnvelope e = comments.get(row);
        if (e.isReadOnly()) {
        	return false;
        }
        
        switch (col) {
        case INITIALS:
        case COMMENT:
        case THREAD_ID:
        case SENDER:
        case RECIPIENT:
            return true;
        }
        
        return false;
    }

    @Override // TableModel
    public void setValueAt(Object object, int row, int col) {
        if (!(object instanceof String)) {
            return;
        }
        if (commentManager == null) {
        	return;
        }
        String value = (String)object;
        CommentEnvelope e = commentManager.undoableGet(row);

        switch (col) {
        case INITIALS:
            value = value.trim();
            e.setInitials(value);
            break;
        case COMMENT:
            e.setMessage(value);
            break;
        case THREAD_ID:
            value = value.trim();
            e.setThreadID(value);
            break;
        case SENDER:
            value = value.trim();
            e.setSender(value);
            break;
        case RECIPIENT:
            value = value.trim();
            e.setRecipient(value);
            break;
        }
        e.setModificationDate();
        fireTableCellUpdated(row, col);
        fireTableCellUpdated(row, MODIFICATION_DATE);
        
        commentManager.undoableRelease(row, e);
    }
}

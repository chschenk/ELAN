package mpi.eudico.client.annotator.gui;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * This extension of JTable is more user-friendly when it is placed in a
 * scroll pane. It is scrollable (horizontally) and the columns are
 * resizable, while filling up the whole available width of the scroll
 * pane in all cases. 

 * 
 * Use it in a scroll pane as usual:<br/>
 * <code>
 *       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); <br/>
 *       JScrollPane scrollPane = new JScrollPane(table); <br/>
 *       frame.getContentPane.add(scrollPane);
 * </code>
 * <p>
 * This was inspired by http://stackoverflow.com/questions/6104916/how-to-make-jtable-both-autoresize-and-horizontall-scrollable
 * and
 * http://flyingjxswithjava.blogspot.nl/2014/01/make-jtable-show-horizontal-scroll-bar.html
 * 
 * @author olasei
 */
@SuppressWarnings("serial")
public class ScrollFriendlyTable extends JTable {
	private boolean doingLayout;

	public ScrollFriendlyTable(TableModel dm) {
		super(dm);
	}

	/**
	 * Idea from
	 * http://stackoverflow.com/questions/6104916/how-to-make-jtable-both-autoresize-and-horizontall-scrollable.
	 * Effectively it sets
	 * AUTO_RESIZE_OFF when there is a scrollbar and otherwise uses the
	 * AUTO_RESIZE_xyz mode that was set. Added the check for
	 * getResizingColumn() to allow any resizing even without scrollbar
	 * present. (but that may give a very jumpy behaviour, and end up
	 * with a too-narrow table)
     *
	 * @see javax.swing.JTable#getScrollableTracksViewportWidth()
	 */
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
	    return getPreferredSize().width < getParent().getWidth();
	}

	/**
	 * Override from
	 * http://flyingjxswithjava.blogspot.nl/2014/01/make-jtable-show-horizontal-scroll-bar.html
	 * which in turn refers to
	 * http://stackoverflow.com/questions/15499255/jtable-with-autoresize-horizontal-scrolling-and-shrinkable-first-column.html
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public void doLayout() {
	    if (getScrollableTracksViewportWidth()) { // a.k.a. isNotWideEnough()	   
	        autoResizeMode = AUTO_RESIZE_SUBSEQUENT_COLUMNS;
	    }
	    doingLayout = true;
	    super.doLayout();
	    doingLayout = false;
	    autoResizeMode = AUTO_RESIZE_OFF;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This override adds the check for <code>!doingLayout</code> but is
	 * otherwise identical to its super version.
	 */
	@Override
	public void columnMarginChanged(ChangeEvent e) {
	    if (isEditing()) {
	        removeEditor();
	    }
	    TableColumn resizingColumn = getTableHeader().getResizingColumn();
	    // Need to do this here, before the parent's
	    // layout manager calls getPreferredSize().
	    if (resizingColumn != null && autoResizeMode == AUTO_RESIZE_OFF
	            && !doingLayout) {
	        resizingColumn.setPreferredWidth(resizingColumn.getWidth());
	    }
	    resizeAndRepaint();
	}
}
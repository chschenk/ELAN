package mpi.search.gui;

import java.util.List;

import javax.swing.AbstractListModel;

import mpi.search.content.result.model.ContentMatch;

/**
 * model that allows paging through list data 
 * corresponds to the Paging(Table)Model of O'Reilly book JavaSwing
 * $Id: PagingListModel.java 20115 2010-09-29 12:34:59Z wilelb $
 *
 * @author $author$
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class PagingListModel extends AbstractListModel {
    public static final int DEFAULT_PAGE_SIZE = 50;
    protected List<ContentMatch> data;
    protected int pageOffset = 0;
    protected int pageSize = DEFAULT_PAGE_SIZE;

    // Work only on the visible part of the table.
    @Override
	public Object getElementAt(int index) {
    		//dummy exception if attempt to get data beyond viewable part
        if (index >= pageSize) {
            throw new IndexOutOfBoundsException("Index "+ index + ", viewable Size "+ pageSize);
        }

        int realIndex = index + (pageOffset * pageSize);

        return data.get(realIndex);
    }

    /**
     *
     * @return real index of first entry in viewable page
     */
    public int getFirstShownRealIndex() {
        return (data.size() == 0) ? (-1) : (pageOffset * pageSize);
    }

    /**
     *
     * @return real index of last entry in viewable page
     */
    public int getLastShownRealIndex() {
        return ((pageOffset * pageSize) + getSize()) - 1;
    }

    /**
     *
     * @return number of pages needed to cover the data
     */
    public int getPageCount() {
        return (int) Math.ceil((double) getRealSize() / pageSize);
    }

    /**
     * @return actual page number that is accesible
     */
    public int getPageOffset() {
        return pageOffset;
    }

    /**
     * sets the size of the viewable part
     *
     * @param s page size
     */
    public void setPageSize(int s) {
        if (s == pageSize) {
            return;
        }

        int oldPageSize = pageSize;
        pageSize = s;
        pageOffset = (oldPageSize * pageOffset) / pageSize;

        fireContentsChanged(this, 0, getSize());
    }

    /**
     * 
     *
     * @return page size (maximum size of viewable data)
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 
     * @return size of total data
     */
    // Use this method if you want to know how big the real list is . . . we
    // could also write "getRealValueAt()" if needed.
    public int getRealSize() {
        return data.size();
    }

    /**
     * @return size of viewable part
     */
    @Override
	public int getSize() {
        return Math.min(pageSize, data.size() - (pageOffset * pageSize));
    }

    /**
     * Update the page offset and fire a data changed.
     */
    public void pageDown() {
        if (pageOffset > 0) {
            pageOffset--;
            fireContentsChanged(this, 0, getSize());
        }
    }

    /**
     * Update the page offset and fire a data changed.
     */
    public void pageUp() {
        if (pageOffset < (getPageCount() - 1)) {
            pageOffset++;
            fireContentsChanged(this, 0, getSize());
        }
    }
}

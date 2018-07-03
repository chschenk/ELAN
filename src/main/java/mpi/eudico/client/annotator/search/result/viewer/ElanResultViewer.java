/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.eudico.client.annotator.search.result.viewer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.TableCellRenderer;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.grid.AbstractEditableGridViewer;
import mpi.eudico.client.annotator.grid.AnnotationTable;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.search.content.result.model.ContentMatch;
import mpi.search.content.result.model.ContentResult;
import mpi.search.result.model.Result;
import mpi.search.result.model.ResultEvent;
import mpi.search.result.viewer.ResultViewer;


/**
 * $Id: ElanResultViewer.java 44948 2016-02-02 13:34:33Z hasloe $
 *
 * @author $Author$
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class ElanResultViewer extends AbstractEditableGridViewer implements ResultViewer, ListDataListener {
    private ContentResult result;

    /**
     * Creates a new ElanResultViewer object.
     */
    public ElanResultViewer() {
        super(new AnnotationTable(new EAFResultViewerTableModel()));
    }

    /**
     * After setting the viewer manager, create the mapping from Tier name and CveId to Color,
     * which is used in the (EAFResultViewer)GridRenderer. This process needs the viewer manager.
     * When drawing,
     * {@link GridRenderer#setComponentLayout} calls
     * {@link AnnotationTable#getColorForAnnotation}.
     */
    @Override // AbstractViewer
    public void setViewerManager(ViewerManager2 viewerManager) {
    	super.setViewerManager(viewerManager);
    	if (viewerManager != null) {
    		preferencesChanged();
    	}
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected TableCellRenderer createTableCellRenderer() {
        return new EAFResultViewerGridRenderer(this, dataModel);
    }

    /**
     * DOCUMENT ME!
     *
     * @param columnName DOCUMENT ME!
     * @param visible DOCUMENT ME!
     */
    public void setColumnVisible(String columnName, boolean visible) {
        table.setColumnVisible(columnName, visible);
    }

    /**
     * DOCUMENT ME!
     *
     * @param list DOCUMENT ME!
     */
    public void setData(List<? extends AnnotationCore> list) {
        updateDataModel(list);
        updateSelection();
        doUpdate();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
    public void resultChanged(ResultEvent e) {
        result = (ContentResult) e.getSource();

        if (result.getRealSize() == 0) {
            reset();
        }
        if ((e.getType() == ResultEvent.STATUS_CHANGED) &&
                (result.getStatus() == Result.INIT)) {
        	
            result.addListDataListener(this);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param result DOCUMENT ME!
     */
    @Override // ResultViewer
    public void showResult(Result result) {
        this.result = (ContentResult) result;
        setData(result.getMatches());
    }

    /**
     * DOCUMENT ME!
     */
    @Override // ResultViewer
    public void reset() {
        setData(new ArrayList<AnnotationCore>(0));
    }
    /**
    *
    *
    * @param e DOCUMENT ME!
    */
   @Override // ListDataListener
   public void contentsChanged(ListDataEvent e) {	
  		dataModel.updateAnnotations(result.getSubList());
   }

   /**
    *
    *
    * @param e DOCUMENT ME!
    */
   @Override // ListDataListener
   public void intervalAdded(ListDataEvent e) {
       for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
           dataModel.addAnnotation((ContentMatch) result.getElementAt(i));
       }
   }

   /**
    *
    *
    * @param e DOCUMENT ME!
    */
   @Override // ListDataListener
   public void intervalRemoved(ListDataEvent e) {
   }

	/**
	 * method from ElanLocaleListener not implemented in AbstractViewer
	 */
   @Override
	public void updateLocale() {
		super.updateLocale();
		popup = new EAFResultViewerPopupMenu(table);
	}
	
    /**
     * Checks the kind of edit that has happened and updates the table when necessary.
     *
     * @param e the ACMEditEvent
     */
    @Override
    public void ACMEdited(ACMEditEvent e) {
        if ((result == null) || (result.getTierNames().length == 0)) {
            return;
        }

        TierImpl changedTier;

        switch (e.getOperation()) {
        case ACMEditEvent.ADD_TIER:
            break;

        case ACMEditEvent.ADD_ANNOTATION_BEFORE:
            break;

        case ACMEditEvent.ADD_ANNOTATION_AFTER:
            break;

        case ACMEditEvent.CHANGE_ANNOTATION_TIME:
        case ACMEditEvent.CHANGE_ANNOTATIONS:

        // fallthrough
        case ACMEditEvent.REMOVE_ANNOTATION:
            repaint();

            break;

        case ACMEditEvent.CHANGE_TIER:

            // a tier is invalidated the kind of change is unknown
            changedTier = (TierImpl) e.getInvalidatedObject();

        case ACMEditEvent.REMOVE_TIER:
            changedTier = (TierImpl) e.getModification();

            for (int i = 0; i < result.getTierNames().length; i++) {
                try {
                    if (result.getTierNames()[i].equals(changedTier.getName())) {
                        result.reset();

                        break;
                    }
                }
                catch (Exception er) {
                }
            }

            break;

        default:
            super.ACMEdited(e);
        }
    }
}

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
package mpi.search.result.viewer;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mpi.search.result.model.Result;
import mpi.search.result.model.ResultEvent;


/**
 * $Id: AbstractResultViewer.java 8348 2007-03-09 09:43:13Z klasal $
 *
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractResultViewer extends JPanel
    implements ResultViewer {
    /** DOCUMENT ME! */
    protected final Action nextAction;

    /** DOCUMENT ME! */
    protected final Action previousAction;

    /** DOCUMENT ME! */
    protected final JLabel currentLabel;

    /** DOCUMENT ME! */
    protected final JPanel controlPanel;

    /** DOCUMENT ME! */
    protected Result result;

    /**
     * Creates a new AbstractResultViewer object.
     */
    public AbstractResultViewer() {
        controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        currentLabel = new JLabel();
        nextAction = new AbstractAction() {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        result.pageUp();
                        updateButtons();
                    }
                };
        nextAction.putValue(Action.NAME, ">");
        previousAction = new AbstractAction() {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        result.pageDown();
                        updateButtons();
                    }
                };
        previousAction.putValue(Action.NAME, "<");
        nextAction.setEnabled(false);
        previousAction.setEnabled(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JPanel getControlPanel() {
        return controlPanel;
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void reset() {
        nextAction.setEnabled(false);
        previousAction.setEnabled(false);
        controlPanel.setVisible(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param result DOCUMENT ME!
     */
    @Override
	public abstract void showResult(Result result);

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void resultChanged(ResultEvent e) {
        result = (Result) e.getSource();

        if (result.getRealSize() == 0) {
            reset();
        }
        else {
            	if ((e.getType() == ResultEvent.PAGE_COUNT_INCREASED) || ((e.getType() == ResultEvent.STATUS_CHANGED) &&
                    ((result.getStatus() == Result.COMPLETE) ||
                    (result.getStatus() == Result.INTERRUPTED)))) {
                updateButtons();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected int[] getNextInterval() {
        if (result.getPageOffset() < result.getPageCount()) {
            return new int[] {
                ((result.getPageOffset() + 1) * result.getPageSize()) + 1,
                Math.min(((result.getPageOffset() + 2) * result.getPageSize()) +
                    1, result.getRealSize())
            };
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected int[] getPreviousInterval() {
        if (result.getPageOffset() > 0) {
            return new int[] {
                ((result.getPageOffset() - 1) * result.getPageSize()) + 1,
                result.getPageOffset() * result.getPageSize()
            };
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param interval DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected String intervalToString(int[] interval) {
        return (interval != null)
        ? (interval[0] +
        ((interval[0] != interval[1]) ? ("-" + interval[1]) : "")) : null;
    }

    /**
     * DOCUMENT ME!
     */
    protected void updateButtons() {
        previousAction.setEnabled(result.getPageOffset() > 0);
        nextAction.setEnabled(result.getPageOffset() < (result.getPageCount() -
            1));

        currentLabel.setText(intervalToString(
                new int[] {
                    result.getFirstShownRealIndex() + 1,
                    result.getLastShownRealIndex() + 1
                }));

        previousAction.putValue(Action.SHORT_DESCRIPTION,
            intervalToString(getPreviousInterval()));
        nextAction.putValue(Action.SHORT_DESCRIPTION,
            intervalToString(getNextInterval()));
    }
}

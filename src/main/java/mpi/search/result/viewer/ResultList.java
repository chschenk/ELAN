package mpi.search.result.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;

import mpi.search.result.model.Match;
import mpi.search.result.model.ResultHandler;


/**
 * $Id: ResultList.java 8348 2007-03-09 09:43:13Z klasal $
 *
 * @author $author$
 * @version $Revision$
 */
public class ResultList extends JList implements MouseListener {
    /** to choose an action which the resulthandler should perform */
    private JPopupMenu actionsMenu;

    /** handler that does something with selected match number */
    private ResultHandler resultHandler;

    /**
     * Creates a new ResultList object.
     */
    public ResultList() {
        this(null);
    }

    /**
     * Creates a new ResultList object.
     *
     * @param resultHandler DOCUMENT ME!
     */
    public ResultList(ResultHandler resultHandler) {
        super();
        this.resultHandler = resultHandler;
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addMouseListener(this);
     }

    /**
     * DOCUMENT ME!
     *
     * @param popupChoices DOCUMENT ME!
     */
    public void setPopupChoices(String[] popupChoices) {
        if (popupChoices.length > 0) {
            actionsMenu = new JPopupMenu();

            ActionListener actionListener = new ActionListener() {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        if (1 >= 0) {
                            resultHandler.handleMatch((Match) getSelectedValue(),
                                e.getActionCommand());
                        }
                    }
                };

            JMenuItem menuItem;

            for (int i = 0; i < popupChoices.length; i++) {
                menuItem = new JMenuItem(popupChoices[i]);
                menuItem.addActionListener(actionListener);
                actionsMenu.add(menuItem);
            }
        } else {
            actionsMenu = null;
        }
    }

    /**
     *
     *
     * @param resultHandler DOCUMENT ME!
     */
    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() > 1) {
            // first registered action is considered default action
            resultHandler.handleMatch((Match) getSelectedValue(),
                ((JMenuItem) actionsMenu.getSubElements()[0]).getActionCommand());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void mouseEntered(MouseEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void mouseExited(MouseEvent e) {
    }

    // on a pc this is the popup trigger method
    @Override
	public void mousePressed(MouseEvent e) {
        if ((actionsMenu != null) && e.isPopupTrigger() &&
                (getSelectedValue() != null)) {
            showPopup(e);
        }
    }

    // On the sun this is the popup trigger method
    @Override
	public void mouseReleased(MouseEvent e) {
        if ((actionsMenu != null) && e.isPopupTrigger() &&
                (getSelectedValue() != null)) {
            showPopup(e);
        }
    }

    private void showPopup(MouseEvent e) {
        actionsMenu.show(ResultList.this, e.getX(), e.getY());
        actionsMenu.setVisible(true);
        requestFocus();
    }
}

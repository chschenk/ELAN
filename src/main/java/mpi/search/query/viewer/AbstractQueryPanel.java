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
package mpi.search.query.viewer;

import mpi.search.SearchLocale;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

/**
 * $Id: AbstractQueryPanel.java 16154 2009-08-25 11:46:52Z patdui $
 * 
 * @author $author$
 * @version $revision$
 */
public abstract class AbstractQueryPanel extends JPanel {
    /** adding a constraint(panel). */
    private final Action addConstraintAction;

    /** deleting constraint(panel). */
    private final Action deleteConstraintAction;

    /** max number of constraints. */
    private final static int maxConstraintCount = 10;

    /** Holds all constraint panels. */
    private final JPanel constraintGridPanel = new JPanel(new GridLayout(0, 1));

    /**
     * Creates a new AbstractQueryPanel object.
     */
    public AbstractQueryPanel() {
        addConstraintAction = new AbstractAction(SearchLocale.getString("Search.Query.Add")) {
            @Override
			public void actionPerformed(ActionEvent e) {
                addConstraint();
            }
        };

        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK);
        addConstraintAction.putValue(Action.ACCELERATOR_KEY, ks);
        deleteConstraintAction = new AbstractAction(SearchLocale.getString("Search.Query.Delete")) {
            @Override
			public void actionPerformed(ActionEvent e) {
                deleteConstraint();
            }
        };

        ks = KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK);
        deleteConstraintAction.putValue(Action.ACCELERATOR_KEY, ks);

        makeLayout();
    }

    private void makeLayout() {
        setLayout(new BorderLayout());
        addConstraintGridPanel();
        addConstraintGridControlPanel();
    }

    protected void addConstraintGridPanel() {
        add(constraintGridPanel, BorderLayout.CENTER);
    }

    protected void addConstraintGridControlPanel() {
        JButton addButton = new JButton(addConstraintAction);
        JButton delButton = new JButton(deleteConstraintAction);
        if (maxConstraintCount > 1) {
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
            buttonPanel.setOpaque(false);
            buttonPanel.add(addButton);
            buttonPanel.add(delButton);

            JPanel insetPanel = new JPanel();
            insetPanel.setOpaque(false);
            insetPanel.add(buttonPanel);
            insetPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
            add(insetPanel, BorderLayout.SOUTH);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void updateLayout() {
        if (getTopLevelAncestor() != null) {
            int preferredWidth = getPreferredSize().width;
            int currentWidth = getSize().width;

            if (preferredWidth > currentWidth) {
                getTopLevelAncestor().setSize(preferredWidth + 20, getTopLevelAncestor().getHeight());
            } else {
                getTopLevelAncestor().validate();
                repaint(); // necessary for applet
            }
        }

        updateActions();
    }

    /**
     * enables/disables actions
     */
    protected void updateActions() {
        deleteConstraintAction.setEnabled(constraintGridPanel.getComponentCount() > 1);
        addConstraintAction.setEnabled(constraintGridPanel.getComponentCount() < maxConstraintCount);
    }

    /**
     * reset to empty.
     */
    public void reset() {
        constraintGridPanel.removeAll();
        addConstraint();
    }

    /**
     * adds a constraint panel.
     */
    protected void addConstraint() {
        constraintGridPanel.add(createConstraintPanel());
        updateLayout();
    }

    /**
     * deletes last constraint panel.
     */
    protected void deleteConstraint() {
        deleteConstraints(1);
    }

    /**
     * deletes last #nrOfPanelsToDelete constraint panels.
     */
    protected void deleteConstraints(int nrOfPanelsToDelete) {
        int nrOfComponents = constraintGridPanel.getComponentCount();
        for (int i = nrOfComponents - 1; i > 0 && nrOfPanelsToDelete > 0; i--, nrOfPanelsToDelete--) {
            constraintGridPanel.remove(i);
        }
        updateLayout();
    }
    
    protected int getNrOfConstraints() {
        return constraintGridPanel.getComponentCount();
    }

    /**
     * returns the panel which collects all constraint panels.
     * @return constraint grid panel
     */
    protected JPanel getConstraintGridPanel() {
        return constraintGridPanel;
    }

    /**
     * returns an anchor or dependent constraint panel.
     * @return constraint panel
     */
    abstract protected JPanel createConstraintPanel();
}

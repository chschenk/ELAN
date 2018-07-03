package mpi.search.content.query.viewer;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;

import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.DependentConstraint;


/**
 * $Id: ConstraintEditor.java 13086 2008-08-20 15:44:25Z hasloe $
 *
 * @author $author$
 * @version $Revision$
 */
public class ConstraintEditor extends AbstractCellEditor
    implements TreeCellEditor {
    protected AbstractConstraintPanel constraintPanel;
    protected final Action startAction;
    protected final CorpusType type;
    protected final DefaultTreeModel treeModel;

    /**
     * Creates a new ConstraintEditor object.
     *
     * @param treeModel DOCUMENT ME!
     * @param type DOCUMENT ME!
     * @param startAction DOCUMENT ME!
     */
    public ConstraintEditor(DefaultTreeModel treeModel, CorpusType type,
        Action startAction) {
        this.treeModel = treeModel;
        this.type = type;
        this.startAction = startAction;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Object getCellEditorValue() {
        return constraintPanel.getConstraint();
    }

    /**
     * DOCUMENT ME!
     *
     * @param tree DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @param selected DOCUMENT ME!
     * @param expanded DOCUMENT ME!
     * @param leaf DOCUMENT ME!
     * @param row DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Component getTreeCellEditorComponent(JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row) {
    	
        if (value instanceof AnchorConstraint) {
            constraintPanel = new AnchorConstraintPanel((AnchorConstraint) value,
                    treeModel, type, startAction);
        } else if (value instanceof DependentConstraint) {
            constraintPanel = new DependentConstraintPanel((DependentConstraint) value,
                    treeModel, type, startAction);
        }

        return constraintPanel;
    }
}

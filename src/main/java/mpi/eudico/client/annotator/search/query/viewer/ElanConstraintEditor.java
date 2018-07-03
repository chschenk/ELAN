package mpi.eudico.client.annotator.search.query.viewer;

import mpi.search.content.model.CorpusType;

import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.DependentConstraint;

import mpi.search.content.query.viewer.ConstraintEditor;

import java.awt.Component;

import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;


/**
 * Font related subclass for ELAN context.
 * 
 * @author HS
 * @version Aug 2008
  */
public class ElanConstraintEditor extends ConstraintEditor {
    /**
     * Creates a new ElanConstraintEditor instance
     *
     * @param treeModel 
     * @param type 
     * @param startAction 
     */
    public ElanConstraintEditor(DefaultTreeModel treeModel, CorpusType type,
        Action startAction) {
        super(treeModel, type, startAction);
    }

    /**
     * Returns the appropriate component.
     *
     * @param tree 
     * @param value 
     * @param selected 
     * @param expanded 
     * @param leaf 
     * @param row 
     *
     * @return the editor component
     */
    @Override
	public Component getTreeCellEditorComponent(JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row) {
        if (value instanceof AnchorConstraint) {
            constraintPanel = new ElanAnchorConstraintPanel((AnchorConstraint) value,
                    treeModel, type, startAction);
        } else if (value instanceof DependentConstraint) {
            constraintPanel = new ElanDependentConstraintPanel((DependentConstraint) value,
                    treeModel, type, startAction);
        }
        // try to set the focus to the text entry field of the constraint panel
        SwingUtilities.invokeLater(new Runnable(){
        	@Override
			public void run() {
        		constraintPanel.grabFocus();
        	}
        });

        return constraintPanel;
    }
}

package mpi.search.content.query.viewer;

import java.awt.Component;


import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;


import mpi.search.content.query.model.Constraint;


/**
 * $Id: ConstraintRenderer.java 13087 2008-08-20 15:45:22Z hasloe $
 *
 * @author $author$
 * @version $Revision$
 */
public class ConstraintRenderer implements TreeCellRenderer {

    /**
     * DOCUMENT ME!
     *
     * @param tree DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @param selected DOCUMENT ME!
     * @param expanded DOCUMENT ME!
     * @param leaf DOCUMENT ME!
     * @param row DOCUMENT ME!
     * @param hasFocus DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row,
        boolean hasFocus) {
        JLabel label = new JLabel();

        if (value instanceof Constraint) {
        	label.setFont(tree.getFont());
            label.setText(Query2HTML.translate((Constraint) value)); 
        }

        label.setOpaque(false);

        return label;
    }
}

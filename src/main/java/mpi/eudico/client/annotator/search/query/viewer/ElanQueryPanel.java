package mpi.eudico.client.annotator.search.query.viewer;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.Preferences;
import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.AbstractConstraint;
import mpi.search.content.query.model.Constraint;
import mpi.search.content.query.viewer.ConstraintRenderer;
import mpi.search.content.query.viewer.QueryPanel;


/**
 * Subclass for ELAN so that ELAN's fonts etc. can be applied.
 * 
 * @author HS
 * @version Aug 2008
  */
@SuppressWarnings("serial")
public class ElanQueryPanel extends QueryPanel {
    /**
     * Creates a new ElanQueryPanel instance
     *
     * @param type corpus type, is EAFType
     * @param startAction the start action
     */
    public ElanQueryPanel(CorpusType type, Action startAction) {
        super(type, startAction);
    }

    /**
     * Creates the constraints tree, using ELAN specific components.
     *
     * @param startAction the start action
     */
    @Override
	protected void createTree(Action startAction) {  
        // get the first anchor constraint and apply some preferences to it
        MutableTreeNode rootNode = (MutableTreeNode) treeModel.getRoot();
        if (rootNode instanceof AbstractConstraint) {
        	AbstractConstraint rootCon = (AbstractConstraint) rootNode;
        	
            Boolean regExPref = Preferences.getBool("Search.RegularExpression", null);
            if (regExPref != null) {
            	rootCon.setRegEx((Boolean) regExPref);
            }
            Boolean casePref = Preferences.getBool("Search.CaseSensitive", null);
            if (casePref != null) {
            	rootCon.setCaseSensitive((Boolean) casePref);
            }
        }
        
    	//setFont(Constants.DEFAULTFONT);
        jTree = new JTree(treeModel) {
                    @Override
					public boolean isPathEditable(TreePath path) {
                        return ((Constraint) path.getLastPathComponent()).isEditable();
                    }
                };
               
        //jTree.setFont(getFont());
        jTree.setEditable(true);
        jTree.setCellRenderer(new ConstraintRenderer());
        jTree.setCellEditor(new ElanConstraintEditor(treeModel, type, startAction));

        //hack to kill mouse event (otherwise they would activate subcomponents of ConstraintPanel)
//        jTree.setUI(new BasicTreeUI() {
//                protected boolean startEditing(TreePath path, MouseEvent event) {
//                    return super.startEditing(path, null);
//                }
//            });

        //explicitly overwriting default height defined by Mac
        jTree.setRowHeight(0);

        jTree.setBorder(new EmptyBorder(5, 5, 5, 5));
        jTree.setOpaque(false);

        //setFont(getFont().deriveFont(Font.PLAIN));
        setLayout(new BorderLayout());
        add(jTree, BorderLayout.CENTER);

        jTree.startEditingAtPath(jTree.getPathForRow(0));

        treeModel.addTreeModelListener(new TreeModelListener() {
                @Override
				public void treeNodesInserted(final TreeModelEvent e) {
                    try {
                        //editing has to start after JTree has updated itself. Otherwise one gets a bad layout. 
                        javax.swing.SwingUtilities.invokeLater(new java.lang.Runnable() {
                                @Override
								public void run() {
                                    jTree.startEditingAtPath(e.getTreePath()
                                                              .pathByAddingChild(e.getChildren()[0]));
                                }
                            });
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }

                /**
                 * DOCUMENT ME!
                 *
                 * @param e DOCUMENT ME!
                 */
                @Override
				public void treeNodesChanged(TreeModelEvent e) {
                }

                /**
                 * DOCUMENT ME!
                 *
                 * @param e DOCUMENT ME!
                 */
                @Override
				public void treeStructureChanged(TreeModelEvent e) {
                }

                /**
                 * DOCUMENT ME!
                 *
                 * @param e DOCUMENT ME!
                 */
                @Override
				public void treeNodesRemoved(TreeModelEvent e) {
                    jTree.startEditingAtPath(e.getTreePath());
                }
            });
    }
}

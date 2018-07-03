package mpi.eudico.client.annotator.md.imdi;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

public class MDTree extends JTree {
	private int displayWidth;
	
	
	/**
	 * @param newModel
	 */
	public MDTree(TreeModel newModel) {
		super(newModel);
	}

	/**
	 * @param root
	 */
	public MDTree(TreeNode root) {
		super(root);
	}

	/**
	 * Returns the current display width available for the tree.
	 * 
	 * @return the diisplay width
	 */
	public int getDisplayWidth() {
		return displayWidth;
	}

	/**
	 * Sets the display width for the tree, which in most cases will be the width of the 
	 * viewport of a scrollpane. The width can be used in the calculation of line breaks
	 * for long metadata (keys and) values. 
	 * 
	 * @param displayWidth the new width
	 */
	public void setDisplayWidth(int displayWidth) {
		this.displayWidth = displayWidth;
	}

	/**
	 * When the size of the tree or the scrollpane containing the tree changes,
	 * the old renderer components seem to be still there, in a cache.
	 * The size of the components is not or not correctly updated.
	 * Ways to force an update on the sub components:
	 * updateUI(), ugly
	 * tree.setCellRenderer(new MDTreeCellRenderer(), also ugly
	 * and changing the row heights value twice, not nice either but slightly better.
	 * There might be better ways?
	 */
	public void forceUIUpdate() {
		setRowHeight(20);
		revalidate();
		setRowHeight(0);
		revalidate();
	}
}

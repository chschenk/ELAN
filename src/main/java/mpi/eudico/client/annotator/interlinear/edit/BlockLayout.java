package mpi.eudico.client.annotator.interlinear.edit;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.interlinear.AnnotationBlockCreator;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

public class BlockLayout {
	private AbstractAnnotation aa;
	private int width;
	private int height;
	private int collapsedHeight;
	
	private boolean collapsed;
	private DefaultMutableTreeNode node;
	
	public BlockLayout(AbstractAnnotation aa) {
		super();
		this.aa = aa;
		collapsed = true;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
		AnnotationBlockCreator creator = new AnnotationBlockCreator();
		node = creator.createBlockForAnnotation(aa, null);
		// hier... wait for call to a "calculate" method that accepts a Graphics object as the context
	}

	public int getHeight() {
		return height;
	}

	public int getCollapsedHeight() {
		return collapsedHeight;
	}


	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}
	
	
}

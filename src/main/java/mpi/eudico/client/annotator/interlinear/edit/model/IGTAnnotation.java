package mpi.eudico.client.annotator.interlinear.edit.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.interlinear.edit.render.IGTNodeRenderInfo;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

/**
 * Represents a node containing either an annotation or text.
 * 
 * TODO we might need different classes for nodes containing annotation or text.
 * A problem to solve then is how to replace a text only node by an annotation node
 * once an annotation has been created for a "placeholder", text only node.
 * If one object can handle both than the annotation can just be set later on. 
 *  
 * @author Han Sloetjes
 *
 */
public class IGTAnnotation implements IGTNode {
	private IGTNode parent;
	private IGTTier igtTier;

    /** this is a flat list of child annotations, not grouped per child tier
     * (which makes the ordering arbitrary). Could introduce a map instead or additional? */
	private List<IGTNode> children;
	private IGTNodeRenderInfo renderInfo;
	// for the time being both textual content and annotation content are allowed
	private String text;
	private AbstractAnnotation annotation;
	
	/**
	 * Constructor for annotations that are part of interlinear glossed text group
	 * of annotations. The root of such a group is a word level annotation or a root
	 * annotation.
	 * 
	 * @param aa the annotation
	 */
	public IGTAnnotation(AbstractAnnotation aa) {
		super();
		this.annotation = aa;
		renderInfo = new IGTNodeRenderInfo();
	}
	
	/**
	 * Constructor for placeholder or text annotations. 
	 * The annotation member should be null. 
	 * 
	 * @param text the text
	 */
	public IGTAnnotation(String text) {
		super();
		this.annotation = null;
		this.text = text;
		renderInfo = new IGTNodeRenderInfo();
	}

	/**
	 * Returns the annotation or null if this is a text node.
	 * @return the annotation or null if this is a text node
	 */
	public AbstractAnnotation getAnnotation() {
		return annotation;
	}
	
	/**
	 * Sets the annotation for this node. The text member will be reset to null.
	 * 
	 * @param aa the annotation for this node
	 */
	public void setAnnotation(AbstractAnnotation aa) {
		this.annotation = aa;
		text = null;//??
		hashCodeOfText = 0;
	}
	
	/**
	 * Returns the text value of the annotation or the text string of this node.
	 * 
	 * @return the text value of the annotation or the text string of this node
	 */
	public String getTextValue() {
		if (annotation != null) {
			return annotation.getValue();
		} else if (text != null) {
			return text;
		} else {
			return "";
		}
	}
	
	/**
	 * Sets the text for this node. If this node was containing an annotation
	 * the annotation field will be set to null.
	 * <p>
	 * For now this method seem unused.
	 * 
	 * @param text the new text
	 */
	public void setTextForNode(String text) {
		this.text = text;
		annotation = null;
		hashCodeOfText = 0;
	}
	
	/**
	 * Returns the parent.
	 * 
	 * @return the parent node
	 */
	@Override
	public IGTNode getParent() {
		return parent;
	}
	
	/**
	 * Sets the parent node (without adding this node as a child node, that should be done separately).
	 * 
	 * Note: this method might be made private
	 * 
	 *  @param node the parent node
	 */
	@Override
	public void setParent(IGTNode node) {
		this.parent = node;
	}

	/**
	 * Returns the list containing the children, can be null.
	 * <p>
	 * (It seems that) The children are all direct children;
	 * but since the tier of the annotation may have multiple direct dependent tiers,
	 * the direct children may still be on different tiers.
	 * 
	 * @return the list containing the children, can be null!
	 */
	@Override
	public List<IGTNode> getChildren() {
		return children;
	}

	/** 
	 * @return a mapping of tier to node lists or null if there are no children
	 * 
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTNode#getChildrenPerTier()
	 */
	@Override
	public Map<IGTTier, List<IGTNode>> getChildrenPerTier() {
		if (children != null) {
			Map<IGTTier, List<IGTNode>> nodeMap = new HashMap<IGTTier, List<IGTNode>>();
			for (IGTNode n : children) {
				if (!nodeMap.containsKey(n.getIGTTier())) {
					nodeMap.put(n.getIGTTier(), new ArrayList<IGTNode>());
				}
				nodeMap.get(n.getIGTTier()).add(n);
			}
			return nodeMap;
		}
		
		return null;
	}

	/**
	 * @return the number of child nodes, 0 if there are no children
	 */
	@Override
	public int getChildCount() {
		if (children == null) {
			return 0;
		} else {
			return children.size();
		}
	}

	/**
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTNode#getIndex(IGTNode)
	 */
	@Override
	public int getIndex(IGTNode node) {
		if (children == null) {
			return -1;
		} else {
			return children.indexOf(node);
		}
	}

	/**
	 * 
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTNode#getChildAfter(IGTNode)
	 */
	@Override
	public IGTNode getChildAfter(IGTNode node) {
		if (children != null) {
			int index = children.indexOf(node);
			if (index == -1) {
				return null;
			} else if (index < children.size() - 1){
				return children.get(index + 1);
			}
		}
		
		return null;
	}
	
	/**
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTNode#getChildBefore(IGTNode)
	 */
	@Override
	public IGTNode getChildBefore(IGTNode node) {
		if (children != null) {
			int index = children.indexOf(node);
			if (index == -1) {
				return null;
			} else if (index > 0){
				return children.get(index - 1);
			}
		}
		
		return null;
	}

	/**
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTNode#addChild(IGTNode)
	 */
	@Override
	public boolean addChild(IGTNode node) {
		if (node == null) {
			return false;
		}
		
		if (children == null) {
			children = new ArrayList<IGTNode>();
		}

		if (children.contains(node)) {
			return false;
		} else {
			children.add(node);
			node.setParent(this);
			hashCodeOfText = 0;
			return true;
		}
	}

	/**
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTNode#removeChild(IGTNode)
	 */
	@Override
	public boolean removeChild(IGTNode node) {
		if (children == null) {
			return false;
		} else {
			node.setParent(null);
			hashCodeOfText = 0;
			return children.remove(node);
		}
	}

	/**
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTNode#insertChild(IGTNode, int)
	 */
	@Override
	public boolean insertChild(IGTNode node, int index) {
		if (node == null || index < 0) {
			return false;
		}
		
		if (children == null) {
			children = new ArrayList<IGTNode>();
		}

		if (index > children.size()) {
			return false;
		} else {
			node.setParent(this);
			children.add(index, node);
			hashCodeOfText = 0;
			return true;
		}
	}

	/**
	 * Returns a reference to the IGT tier this IGT annotation is part of.
	 * 
	 * @return the IGT tier
	 */
	@Override
	public IGTTier getIGTTier() {
		return igtTier;
	}

	/**
	 * Sets the IGT tier this annotation is part of.
	 * 
	 * @param igtTier
	 */
	@Override
	public void setIGTTier(IGTTier igtTier) {
		this.igtTier = igtTier;
	}

	/**
	 * Sets the render info object for this node.
	 * 
	 * @param renderInfo the new render info object
	 */
	@Override
	public void setRenderInfo(IGTNodeRenderInfo renderInfo) {
		this.renderInfo = renderInfo;
	}

	/**
	 * Returns the render info object for this node.
	 * 
	 * @return the render info object
	 */
	@Override
	public IGTNodeRenderInfo getRenderInfo() {
		return renderInfo;
	}
	
	private int hashCodeOfText;
	
    /**
     * A sort-of hashCode which is only based on the suggested strings,
     * not on the Positions that may be associated with them, or
     * other data.
     */
	@Override
	public int hashCodeOfText() {
		if (hashCodeOfText == 0) {
			int result = getTextValue().hashCode();
			
			if (children != null) {
				for (IGTNode child : children) {
					long m = (long)result * 37;
					result = (int)(m & 0xFFFFFFFF) + child.hashCodeOfText() + (int)(m >>> 32);
				}
			}
			
			hashCodeOfText = result;
		}
		
		return hashCodeOfText;
	}

	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("IGTAnnotation:['");
		buf.append(getTextValue());
		//buf.append("' type=");
		//buf.append(String.valueOf(type));
		IGTNodeRenderInfo ri = getRenderInfo();
		//buf.append(String.valueOf(ri));
		buf.append("' x=");
		buf.append(String.valueOf(ri.x));
		buf.append(" y=");
		buf.append(String.valueOf(ri.y));
		buf.append(" w=");
		buf.append(String.valueOf(ri.width));
		buf.append(" h=");
		buf.append(String.valueOf(ri.height));
		buf.append(" realW=");
		buf.append(String.valueOf(ri.realWidth));
		buf.append(" calcW=");
		buf.append(String.valueOf(ri.calcWidth));
		buf.append(" children=");
		buf.append(String.valueOf(children));
		buf.append("]");
		
		return buf.toString();
	}
}

package mpi.eudico.client.annotator.interlinear.edit.model;

import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.interlinear.edit.render.IGTNodeRenderInfo;

public interface IGTNode {
	/**
	 * Returns the parent node, or null if this element is the root.
	 * 
	 * @return the parent node, or null if this element is the root
	 */
	public IGTNode getParent();
	
	/**
	 * Sets the parent node of this node.
	 * 
	 * @param node the new parent
	 */
	public void setParent(IGTNode node);
	
	/**
	 * Returns the list of child nodes. 
	 * NB: provides access to the actual list.
	 * 
	 * @return the list of child nodes
	 */
	public List<IGTNode> getChildren();
	
	/**
	 * Returns the child nodes, grouped per tier.
	 * <p>
	 * The children of the tier are direct children, but since the tier can
	 * have multiple dependent tiers, the children can still be spread
	 * over multiple tiers. 
	 * 
	 * @return a mapping of tier to node lists
	 */
	public  Map<IGTTier, List<IGTNode>> getChildrenPerTier();
	
	/**
	 * Returns the number of child nodes.
	 * 
	 * @return the number of child nodes
	 */
	public int getChildCount();
	
	/**
	 * Returns the index of the specified node in the list of child nodes, 
	 * or -1 if the node is not in the list.
	 * 
	 * @param node the node
	 * 
	 * @return the index of the specified node in the list of child nodes, or -1
	 */
	public int getIndex(IGTNode node);
	
	/**
	 * Returns the next child in the list, the next sibling.
	 * 
	 * @param node the current node
	 * 
	 * @return the next node, or null
	 */
	public IGTNode getChildAfter(IGTNode node);
	
	/**
	 * Returns the node before the specified node.
	 * 
	 * @param node the current node
	 * 
	 * @return the previous node, or null
	 */
	public IGTNode getChildBefore(IGTNode node);
	
	/**
	 * Adds the node to the list of children.
	 * 
	 * @param node the node to add
	 * 
	 * @return true if the node has successfully been added, false otherwise
	 */
	public boolean addChild(IGTNode node);
	
	/**
	 * Removes the node from the list of children.
	 * 
	 * @param node the node to remove
	 * 
	 * @return true if the node was in the list of children and removed, false otherwise
	 */
	public boolean removeChild(IGTNode node);
	
	/**
	 * Inserts the specified node into the list of children at the specified index
	 * 
	 * @param node the node to insert
	 * @param index the intended position in the list of children
	 * 
	 * @return true if the node has successfully been inserted
	 */
	public boolean insertChild(IGTNode node, int index);
	

	/**
	 * Returns a reference to the IGT tier this IGT annotation is part of.
	 * 
	 * @return the IGT tier
	 */
	public IGTTier getIGTTier();

	/**
	 * Sets the IGT tier this annotation is part of.
	 * 
	 * @param igtTier
	 */
	public void setIGTTier(IGTTier igtTier);
	
	/**
	 * Sets the render info object for this node.
	 * 
	 * @param renderInfo the new render info object
	 */
	public void setRenderInfo(IGTNodeRenderInfo renderInfo);
	
	/**
	 * Returns the render info object for this node.
	 * 
	 * @return the render info object
	 */
	public IGTNodeRenderInfo getRenderInfo();

	/**
	 * Returns a hash code, similar to the normal hashCode(),
	 * but only based on the text of this node and its children.
	 */
	int hashCodeOfText();
	
}

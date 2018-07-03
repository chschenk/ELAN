package mpi.eudico.client.annotator.tiersets;

/**
 * Defines a tier set change listener.
 *
 * @author Aarthy Somasundaram
 */
public interface TierSetListener {    
    /**
     * Notifies the listener of a change multiple tier sets
     */
    public void tierSetChanged();
    
    /**
     * Notifies the listener of a change in a single tier set
     */
    public void tierSetVisibilityChanged(TierSet set);
    
    /**
     * Notifies the listener of a change in visibility of a single tier.
     * 
     * @param tierName
     * @param isVisible
     */
	public void tierVisibilityChanged(String tierName, boolean isVisible);
}
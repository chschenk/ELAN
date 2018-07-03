package mpi.eudico.server.corpora.clomimpl.reflink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.RefLink;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.UndoTransaction;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;

/**
 * The class corresponding to a REF_LINK_SET element in the EAF file.
 * <p>
 * It is also an ACMEditListener to keep track of Annotations
 * being deleted. If the set contains any RefLinks pertaining to
 * those Annotations, they will have to be removed.
 * 
 * @author olasei
 */

public class RefLinkSet implements ACMEditListener {
	// Attributes
	private String linksID;
	private String linksName;
	private ExternalReference extRef;
	private String langRef;
	private String cvRef;
	
	private TranscriptionImpl trans;
	
	// Content
	private List<RefLink> refs;

	public RefLinkSet(TranscriptionImpl trans) {
		this.trans = trans;
		trans.addACMEditListener(this);
	}
	
	/**
	 * @return the linksID
	 */
	public String getLinksID() {
		return linksID;
	}

	/**
	 * @param linksID the linksID to set
	 */
	public void setLinksID(String linksID) {
		this.linksID = linksID;
	}

	/**
	 * @return the linksName
	 */
	public String getLinksName() {
		return linksName;
	}

	/**
	 * @param linksName the linksName to set
	 */
	public void setLinksName(String linksName) {
		this.linksName = linksName;
	}

	/**
	 * @return the extRef
	 */
	public ExternalReference getExtRef() {
		return extRef;
	}

	/**
	 * @param extRef the extRef to set
	 */
	public void setExtRef(ExternalReference extRef) {
		this.extRef = extRef;
	}

	/**
	 * @return the langRef
	 */
	public String getLangRef() {
		return langRef;
	}

	/**
	 * @param langRef the langRef to set
	 */
	public void setLangRef(String langRef) {
		this.langRef = langRef;
	}

	/**
	 * @return the cvRef
	 */
	public String getCvRef() {
		return cvRef;
	}

	/**
	 * @param cvRef the cvRef to set
	 */
	public void setCvRef(String cvRef) {
		this.cvRef = cvRef;
	}

	/**
	 * @return the refs
	 */
	public List<RefLink> getRefs() {
		return refs;
	}

	/**
	 * @param refs the refs to set
	 */
	public void setRefLinks(List<RefLink> refs) {
		this.refs = refs;
	}

	private static final int GROUP_MEMBERS_MINIMUM = 2;
	
	/**
	 * If an annotation is removed, or anything else that a reflink can link to,
	 * the link itself should be removed too.
	 * Any groups that refer to these links are removed too, transitively. 
	 *
	 * @param id could be the id of an annotation or of a RefLink
	 * @return the RefLinks that were removed
	 */
	Set<RefLink> removeLinksTo(String id) {
		// Collection of ids that should not be referred to any more.
		NavigableSet<String> toClean = new TreeSet<String>();
		
		toClean.add(id);	// initial work
		
		return removeLinksTo(toClean);
	}
	
	/**
	 * Clean up all RefLinks that refer to one of the ids in the given set.
	 * 
	 * @param toClean set of ids. The ids can be of Annotations or of other RefLinks.
	 * @return the RefLinks that were removed
	 */
	Set<RefLink> removeLinksTo(NavigableSet<String> toClean) {
		Set<RefLink> removed = new TreeSet<RefLink>();
		
		while (!toClean.isEmpty()) {
			NavigableSet<String> cleanNext = new TreeSet<String>();

			Iterator<RefLink> refIter = refs.iterator();

			while (refIter.hasNext()) {
				RefLink rl = refIter.next();
				if (rl.references(toClean)) {
					boolean removeRL = true;
					
					if (rl instanceof GroupRefLink) {
						GroupRefLink grl = (GroupRefLink)rl;
						// If the group keeps more than 1 member, don't remove it.
						removeRL = maybeShrinkRefs(grl, toClean);
					}
					if (removeRL) {
						removed.add(rl);
						refIter.remove();
						// Now that this link is removed,
						// nobody should refer to it any more.
						cleanNext.add(rl.getId());
					}
				}
			}
			
			toClean = cleanNext;
		}
		
		return removed;
	}

	/**
	 * Checks if all Reference Links still refer to valid annotations.
	 * <p>
	 * After references to deleted annotations are deleted,
	 * all references from other RefLinks to the removed RefLinks are cleaned up as well.
	 * <p>
	 * Question: do CrossRefLink only refer to Annotations or can they
	 * refer to other ...RefLinks as well?
	 * Currently, checkId() assumes that they can.
	 * 
	 * @return all RefLinks that have been removed.
	 */
	Set<RefLink> checkForRemovedAnnotations() {
		Set<RefLink> removed = new HashSet<RefLink>();
		Map<String, ?> annotations = null;
		
		// If there is a sufficient number of references,
		// it is worth it to cache the ids.
		if (refs.size() > 0) {
			annotations = trans.getAnnotationsByIdMap();
		}

		for (RefLink rl : refs) {
			if (rl instanceof CrossRefLink) {
				CrossRefLink crl = (CrossRefLink)rl;
				
				if (!checkId(crl.getRef1(), trans, annotations) ||
					!checkId(crl.getRef2(), trans, annotations)) {
					removed.add(rl);
				}
			} else if (rl instanceof GroupRefLink) {
				GroupRefLink grl = (GroupRefLink)rl;
				Set<String> grefs = grl.getRefs();
				List<String> removeGrefs = new ArrayList<String>(grefs.size());
				
				/*
				 * If a group refers to some non-existing members, at first just make
				 * the group smaller. Only if it has less than 2 members, remove it.
				 * To know which, we need to count ids first.
				 */
				for (String id : grefs) {
					if (!checkId(id, trans, annotations) ) {
						removeGrefs.add(id);
					}
				}
				
				if (!removeGrefs.isEmpty()) {
					if (grefs.size() - removeGrefs.size() >= GROUP_MEMBERS_MINIMUM) {
						// Keep the group, but with fewer members.
						shrinkRefs(grl, removeGrefs);
					} else {
						// Remove the group as a whole
						removed.add(rl);
					}
				}
			}
		}
			
		if (!removed.isEmpty()) {
			refs.removeAll(removed);

			// Now that some RefLinks are removed, clean up the references to those.
			NavigableSet<String> toClean = new TreeSet<String>();
			for (RefLink link : removed) {
				toClean.add(link.getId());
			}
			Set<RefLink> moreRemoved = removeLinksTo(toClean);
			removed.addAll(moreRemoved);
		}
		
		return removed;
	}

	/**
	 * Take a GroupRefLink, store an UndoTransaction for it, remove some refs
	 * from a copy of its group members, and set the shrunk copy as the group.
	 * <p>
	 * Use this method if you are sure that
	 * <ul>
	 * <li>removeGrefs is not empty
	 * <li>all members of removeGrefs are indeed group members, and
	 * <li>that the remaining group members are satisfactory to keep the group
	 *     in existence.
	 * </ul>
	 * Otherwise, call maybeShrinkRefs().
	 * 
	 * @param grl
	 *            a GroupRefLink
	 * @param removeGrefs
	 *            refs to remove from the group
	 */
	private void shrinkRefs(GroupRefLink grl, Collection<String> removeGrefs) {
		Set<String> grefs = grl.getRefs();
		appendUndoTransaction(grl, grefs);
		
		HashSet<String> newgrefs = new HashSet<String>(grefs);
		newgrefs.removeAll(removeGrefs);
		grl.setRefs(newgrefs);
	}
	
	/**
	 * Much like shrinkRefs(), except we are not sure yet if there will
	 * be a change at all, or if it will be so big that we'll just
	 * remove the whole group.
	 * <p>
	 * If there is no change at all (because none of the members of removeGrefs
	 * are part of the group), no change is made and no undo info recorded.
	 * 
	 * @param grl
	 * @param removeGrefs
	 * @return true if the whole group should be removed
	 *         (in that case the group is not modified and no undo info recorded).
	 */
	private boolean maybeShrinkRefs(GroupRefLink grl, Collection<String> removeGrefs) {
		Set<String> grefs = grl.getRefs();
		HashSet<String> newgrefs = new HashSet<String>(grefs);
		newgrefs.removeAll(removeGrefs);
		
		if (newgrefs.size() < GROUP_MEMBERS_MINIMUM) {
			return true;
		}

		if (newgrefs.size() < grefs.size()) {
			appendUndoTransaction(grl, grefs);
			grl.setRefs(newgrefs);
		}
		
		return false;
	}
	
	/**
	 * Check if the id is that of an Annotation
	 * or of another RefLink.
	 *  
	 * @param id	the id to look up
	 * @param trans the TranscriptionImpl
	 * @param map 	quick lookup for Annotation ids.
	 * 
	 * @return whether the id was found
	 */
	private boolean checkId(String id, TranscriptionImpl trans, Map<String, ?> map) {
		if (map != null) {
			return map.containsKey(id) || checkLinkId(id);
		} else {
			return checkLinkId(id) || trans.getAnnotationById(id) != null;
		}
	}
	
	/**
	 * Linear scan to check if a certain id matches that of a RefLink.
	 * @param id
	 */
	private boolean checkLinkId(String id) {
		for (RefLink rl : refs) {
			if (id.equals(rl.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * React to removed Annotations.
	 * <p>
	 * If there is a link to or from a removed annotation, the RefLink is
	 * removed.
	 * <p>
	 * Implementation notes: When deleting a single annotation,
	 * REMOVE_ANNOTATION notifications are not always sent for each child
	 * annotation that gets removed as well. <br/>
	 * On the other hand, this often occurs when setNotifying == false, which
	 * causes all notifications to be suppressed anyway. When re-enabling it, a
	 * CHANGE_ANNOTATIONS is sent, which causes a full check.
	 * <p>
	 * As a result, at times there may be links left over that should have been
	 * purged already, but will (probably) be done soon.
	 * <p>
	 * HS note Dec 2016 REMOVE_ANNOTATION of a single annotation generates only one
	 * event (only in the Undo the setNotifying() is used), at least, so it seems
	 * to be now. Therefore changed to check for any removed annotation.
	 * Links can refer to links in other sets, proper removal of all cross-set links still has to be implemented  
	 */
	@Override
	public void ACMEdited(ACMEditEvent e) {
		// Fast path if there is nothing to do anyway.
		if (refs.isEmpty()) {
			return;
		}
		
		switch (e.getOperation()) {
			case ACMEditEvent.REMOVE_ANNOTATION:
				Object o = e.getModification();
				if (o instanceof AbstractAnnotation) {
					String id = ((AbstractAnnotation)o).getIdLazily();
					if (id != null) {
						//Set<RefLink> removed = removeLinksTo(id);						
						// for now check for any removed annotation in the non-optimized way
						Set<RefLink> removed = checkForRemovedAnnotations();
						appendUndoTransaction(removed);						
					}
					break;
				}
				// fall through: specific removed annotation is unknown
			case ACMEditEvent.CHANGE_ANNOTATIONS:
				// it is unknown which annotation(s) suddenly are gone.
				// Maybe this is not needed anymore if proper REMOVE_ANNOTATION
				// events are sent at all times.
				Set<RefLink> removed = checkForRemovedAnnotations();
				appendUndoTransaction(removed);
				break;
			case ACMEditEvent.CHANGE_CONTROLLED_VOCABULARY:
				// TODO: add check for removal of CVs and languages too
				break;
		}
	}

	/**
	 * Get the current UndoTransaction of the Transcription to be amended.
	 * <p>
	 * If the UndoTransaction at the top happens to be our type, 
	 * it is fine to just add some more data to it.
	 * Otherwise, we make a new one. 
	 *  
	 * @param removed
	 */
	private RefLinksRemovedUndoTransaction getUndoTransaction() {
		UndoTransaction ut = trans.getCurrentUndoTransaction();
		if (ut instanceof RefLinksRemovedUndoTransaction) {
			return (RefLinksRemovedUndoTransaction)ut;
		} else if (ut != null) {
			RefLinksRemovedUndoTransaction rut = new RefLinksRemovedUndoTransaction();
			trans.addToCurrentUndoTransaction(rut);
			return rut;
		}
		return null;	// can't happen
	}
	
	/**
	 * Amend the current UndoTransaction of the Transcription by adding more RefLinks.
	 * We have removed some links, so they should be added to stack.
	 * <p>
	 * If the UndoTransaction at the top happens to be our type, just add some more data to it.
	 * Otherwise, make a new one. 
	 *  
	 * @param removed
	 */
	private void appendUndoTransaction(Set<RefLink> removed) {
		if (!removed.isEmpty()) {
			RefLinksRemovedUndoTransaction rut = getUndoTransaction();
			if (rut != null) {
				rut.add(this, removed);
			}
		}
	}
	
	/**
	 * Amend the current UndoTransaction of the Transcription by adding more
	 * GroupRefLinks.
	 * We have removed some if its members, so they should be added to stack.
	 * <p>
	 * If the UndoTransaction at the top happens to be our type, just add some more data to it.
	 * Otherwise, make a new one. 
	 *  
	 * @param removed
	 */
	private void appendUndoTransaction(GroupRefLink group, Set<String> oldMembers) {
		RefLinksRemovedUndoTransaction rut = getUndoTransaction();
		if (rut != null) {
			rut.add(this, group, oldMembers);
		}
	}
	
	/**
	 * An UndoTransaction object to remember which RefLinks were removed.
	 * The set can be extended on demand, there is no need to create
	 * a new object every time some new deletions have to be undone.
	 * Dec 2016 HS adaptation to support multiple RefLinkSets
	 *  
	 * @author olasei
	 */
	class RefLinksRemovedUndoTransaction extends UndoTransaction {
		LinkedHashMap<RefLinkSet, Set<RefLink>> removedLinksMap;
		LinkedHashMap<RefLinkSet, Map<String, Set<String>>> shrunkGroupLinksMap;
		
		RefLinksRemovedUndoTransaction() {
			super();
		}
		
		/**
		 * Add a set of removed RefLinks for restoration later at undo time.
		 */
		public void add(RefLinkSet set, Set<RefLink>removed) {
			if (removedLinksMap == null) {
				removedLinksMap = new LinkedHashMap<RefLinkSet, Set<RefLink>>();
			}
			Set<RefLink> removedLinks = removedLinksMap.get(set);
			if (removedLinks == null) {
				removedLinks = new HashSet<RefLink>();
				removedLinksMap.put(set, removedLinks);
			}
			
			removedLinks.addAll(removed);
		}	
		
		/**
		 * Record that a group had a certain set of members,
		 * which is going to be shrunk,
		 * for restoration later at undo time.
		 */
		public void add(RefLinkSet set, GroupRefLink group, Set<String> oldMembers) {
			if (shrunkGroupLinksMap == null) {
				shrunkGroupLinksMap = new LinkedHashMap<RefLinkSet, Map<String,Set<String>>>();
			}
			Map<String, Set<String>> shrunkGroupLinks = shrunkGroupLinksMap.get(set);
			if (shrunkGroupLinks == null) {
				shrunkGroupLinks = new HashMap<String, Set<String>>();
				shrunkGroupLinksMap.put(set, shrunkGroupLinks);
			}
			if (shrunkGroupLinks.containsKey(group.getId())) {
				// This group was added earlier,
				// by definition with a larger set of members,
				// so don't add it again.
			} else {
				shrunkGroupLinks.put(group.getId(), oldMembers);
			}
		}
		
		@Override
		public void undo() {
			// Groups can be first shrunk and then removed later anyway,
			// so first add back all links and then go and see if
			// any groups need to be restored.
			if (removedLinksMap != null) {
				Iterator<RefLinkSet> setIter = removedLinksMap.keySet().iterator();
				while (setIter.hasNext()) {
					RefLinkSet set = setIter.next();
					set.getRefs().addAll(removedLinksMap.get(set));
				}
			}
			
			if (shrunkGroupLinksMap != null) {
				Iterator<RefLinkSet> setIter = shrunkGroupLinksMap.keySet().iterator();
				while (setIter.hasNext()) {
					RefLinkSet set = setIter.next();
					Map<String, Set<String>> shrunkGroupLinks = shrunkGroupLinksMap.get(set);
					
					if (shrunkGroupLinks != null && !shrunkGroupLinks.isEmpty()) {
						for (RefLink link : set.getRefs()) {
							if (link instanceof GroupRefLink) {
								Set<String> oldMembers = shrunkGroupLinks.get(link.getId());
								if (oldMembers != null) {
									((GroupRefLink)link).setRefs(oldMembers);
								}
							}
						}
					}
				}
			}
		}
	}
}

package mpi.eudico.server.corpora.clomimpl.abstr;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ParentAnnotationListener;
import mpi.eudico.server.corpora.util.ACMEditableObject;


/**
 * DOCUMENT ME!
 * $Id: AbstractAnnotation.java 44655 2015-11-10 12:58:28Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractAnnotation implements Annotation {
    private Tier tier;
    private String value = "";
    private String id = null;
    private boolean markedDeleted = false;
    private List<Annotation> parentAnnotListeners; // see addParentAnnotationListener()
    /** field used to, potentially, store different types of external references from an annotation
     * e.g. a reference to a (ISO DCR) Data Category 
     * */
    private ExternalReference extRef;
	/**
	 *  field used to store the id of the value in the CV
	 */
	private String cvEntryId;

    /**
     * Creates a new AbstractAnnotation instance
     */
    public AbstractAnnotation() {
        parentAnnotListeners = new ArrayList<Annotation>();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public abstract long getBeginTimeBoundary();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public abstract long getEndTimeBoundary();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getValue() {
        return value;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theValue DOCUMENT ME!
     */
    @Override
	public void setValue(String theValue) {
        value = theValue;

        modified(ACMEditEvent.CHANGE_ANNOTATION_VALUE, null);
    }

    /**
     * Returns the annotation id.
     * If it does not have one yet, it is invented.
     * 
     * @return the annotation id
     */
    @Override
	public String getId() {
    	if (id == null || id.isEmpty()) {
    		TierImpl ti = (TierImpl)tier;
    		TranscriptionImpl tr = ti.getTranscription();
    		// This Property's value is an Integer.
    		Property p = tr.getDocProperty("lastUsedAnnotationId");
    		Integer lastUsedAnnId = 0;
			if (p == null) {
				p = new PropertyImpl("lastUsedAnnotationId", null);
				tr.addDocProperty(p);
			} else if (p.getValue() != null) {
				try {
					lastUsedAnnId = (Integer)p.getValue();
				} catch (ClassCastException nfe) {
					System.out.println("Could not retrieve the last used annotation id.");
				}
			}
			id = "a" + ++lastUsedAnnId;
			p.setValue(lastUsedAnnId);
    	}
    		
    	return id;
    }

    /**
     * Returns the annotation id.
     * If it does not have one yet, null is returned.
     * 
     * @return the annotation id, or null
     */
	public String getIdLazily() {
		return id;
	}

	/**
     * Sets the annotation id.
     * 
     * @param the annotation id
     */
    @Override
	public void setId(String s){
    	id = s;
    }
    
    /**
     * Returns the external reference object. This can be a compound object containing 
     * multiple reference objects.
     * 
     * @return the external reference object
     */
    public ExternalReference getExtRef() {
    	return extRef;
    }
    
    /**
     * Sets the external reference object. This can be a compound object containing multiple 
     * external references.
     * 
     * @param extRef the external reference object
     */
    public void setExtRef(ExternalReference extRef) {
    	this.extRef = extRef;
    	// is this event necessary?
    	modified(ACMEditEvent.CHANGE_ANNOTATION_EXTERNAL_REFERENCE, extRef);// or null as the modification?
    }
    
	/**
	 * @return the cVEntryId
	 */
	@Override
	public String getCVEntryId() {
		return cvEntryId;
	}

	/**
	 * @param cVEntryId the cVEntryId to set
	 */
	@Override
	public void setCVEntryId(String cVEntryId) {
		cvEntryId = cVEntryId;
	}
    
	/**
	 * Returns the value of the external reference of a type
	 * 
	 * @param type
	 * @return
	 * @author Micha Hulsbosch
	 */
	public String getExtRefValue(int type) {
		if (extRef != null) {
			String value = null; 
			ExternalReference ef = extRef;
			ArrayList<ExternalReference> extRefList = new ArrayList<ExternalReference>();
			extRefList.add(ef);
			int i = 0;
			while (i < extRefList.size()) {
				if (extRefList.get(i).getReferenceType() == type) {
					value = extRefList.get(i).getValue();
					break;
				} else if (extRefList.get(i).getReferenceType() == ExternalReference.REFERENCE_GROUP) {
					extRefList.addAll(((ExternalReferenceGroup) extRefList.get(i)).getAllReferences());
				}
				i++;
			}
			return value;
		}
		return null;
	}

	/**
	 * Removes a certain ExternalReference
	 * @param externalReferenceImpl
	 */
	public void removeExtRef(ExternalReferenceImpl er) {
		if (extRef == null) {
			// No need to do anything
		} else if (extRef.equals(er)) {
			extRef = null;
		} else if (extRef instanceof ExternalReferenceGroup) {
			removeExtRefFromGroup((ExternalReferenceGroup) extRef, er);
		}
		modified(ACMEditEvent.CHANGE_ANNOTATION_EXTERNAL_REFERENCE, extRef);
	}
	
	/**
	 * 
	 * @param extRefGrp
	 * @param er
	 */
	private void removeExtRefFromGroup(ExternalReferenceGroup extRefGrp, ExternalReference er) {
		List<ExternalReference> extRefList = extRefGrp.getAllReferences();
		for(int i = 0; i < extRefList.size();  i++) {
			final ExternalReference externalReference = extRefList.get(i);
			if (externalReference instanceof ExternalReferenceGroup) {
				removeExtRefFromGroup((ExternalReferenceGroup) externalReference, er);
			} else if (externalReference != null
					&& externalReference.equals(er)) {
				extRefGrp.removeReference(er);
			}
		}
	}

	/**
	 * Adds a certain ExternalReferences
	 * @param externalReferenceImpl
	 */
	public void addExtRef(ExternalReference er) {
		if (extRef == null) {
			extRef = er;
		} else if (extRef instanceof ExternalReferenceGroup) {
				((ExternalReferenceGroup) extRef).addReference(er);
		} else {
			extRef = ExternalReferenceGroup.create(extRef, er);
		}
		modified(ACMEditEvent.CHANGE_ANNOTATION_EXTERNAL_REFERENCE, extRef);
	}

	/**
	 * Returns a list containing the external references (not ext. ref. groups!)
	 * @return
	 */
	public List<ExternalReference> getExtRefs() {
		List<ExternalReference> extRefs = new ArrayList<ExternalReference>();

		if (extRef != null) {
			if (extRef instanceof ExternalReferenceGroup) {
				addExtRefToList((ExternalReferenceGroup) extRef, extRefs);
			} else {
				extRefs.add(extRef);
			}
		}

		return extRefs;
	}

	private void addExtRefToList(ExternalReferenceGroup extRefGrp,
			List<ExternalReference> extRefs) {
		for (ExternalReference er : extRefGrp.getAllReferences()) {
			if (er != null) {
				if (er instanceof ExternalReferenceGroup) {
					addExtRefToList((ExternalReferenceGroup) er, extRefs);
				} else {
					extRefs.add(er);
				}
			}
		}
	}
	
    /**
     * DOCUMENT ME!
     *
     * @param theValue DOCUMENT ME!
     */
    @Override
	public void updateValue(String theValue) {
        value = theValue;

        modified(ACMEditEvent.CHANGE_ANNOTATION_VALUE, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Tier getTier() {
        return tier;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theTier DOCUMENT ME!
     */
    protected void setTier(Tier theTier) {
        tier = theTier;
    }

    /**
     * DOCUMENT ME!
     *
     * @param deleted DOCUMENT ME!
     */
    @Override
	public void markDeleted(boolean deleted) {
        // System.out.println("ann: " + getValue() + " marked deleted");
        markedDeleted = deleted;

        notifyParentListeners();
        unregisterWithParent();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean isMarkedDeleted() {
        return markedDeleted;
    }

    /**
     * DOCUMENT ME!
     */
    public void unregisterWithParent() {
        if (hasParentAnnotation()) {
            Annotation p = getParentAnnotation();

            if (p != null) {
                p.removeParentAnnotationListener(this);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param operation DOCUMENT ME!
     * @param modification DOCUMENT ME!
     *
     */
    @Override
	public void modified(int operation, Object modification) {
        handleModification(this, operation, modification);
    }

    /**
     * DOCUMENT ME!
     *
     * @param source DOCUMENT ME!
     * @param operation DOCUMENT ME!
     * @param modification DOCUMENT ME!
     */
    @Override
	public void handleModification(ACMEditableObject source, int operation,
        Object modification) {
        if (tier != null) {
            tier.handleModification(source, operation, modification);
        }
    }

    /**
     * MK:02/07/03 <br>
     * Each Annotation has 0..n  ACMListeners. Children of this annotation do
     * not account as ACMListeners.
	 * <p>
     * FIXME: The result of getParentListeners() is known to be Annotations (or even more specific!)
     * and is used as such without checking.
     * Even the callback RefAnnotation.parentAnnotationChanged() "knows" this.
     * So enforce that here already.
     * <br/>
     * A better way would be to fix the ParentAnnotation interface, its use, and/or
     * the users of getParentListeners() (which is not part of the interface).
     * 
     * @param listener the child Annotation
     */

    @Override
	public void addParentAnnotationListener(ParentAnnotationListener listener) {
        addParentAnnotationListener((Annotation)listener);
    }

	protected void addParentAnnotationListener(Annotation child) {
        if (!parentAnnotListeners.contains(child)) {
            parentAnnotListeners.add(child);
        }
    }

	/**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    @Override
	public void removeParentAnnotationListener(ParentAnnotationListener l) {
        parentAnnotListeners.remove(l);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void notifyParentListeners() {
        List<ParentAnnotationListener> copiedList = new ArrayList<ParentAnnotationListener>(parentAnnotListeners);
        Iterator<ParentAnnotationListener> i = copiedList.iterator();

        while (i.hasNext()) {
            ParentAnnotationListener listener = i.next();
            listener.parentAnnotationChanged(new EventObject(this));
        }
    }

    /**
     * MK:02/07/03 <br>
     * Each Annotation has 0..n  children-Annotations, for Transcriptions, a
     * morpheme brake (MB) may have several part of speeches (PS).  The PS are
     * children annotations of MB.<br>
     * (Name hint: children listen to their parent, so they are
     * parent-listeners.)
     * <p>
     * FIXME: this returns a list of ParentAnnotationListeners, but in fact
     * is used (and known to contain) Annotations or even AbstractAnnotations.
     * This fact is even abused in this very file, see for example in 
     * {@link #getChildrenOnTier(Tier)}.
     * <p>
     * According to the interface, this knowledge should not be used.
     * 
     * @return children-Annotations of 'this' parent, or empty ArrayList.
     */
    public List<Annotation> getParentListeners() {
        return parentAnnotListeners;
    }

    /**
     * AK:05/07/2002 <br>
     * gets a List with all dependent children on the specified tier If
     * there are no children, an empty vector is returned
     *
     * @param tier
     *
     * @return List of Annotations
     */
    @Override
	public final List<Annotation> getChildrenOnTier(Tier tier) {
        List<Annotation> children = new ArrayList<Annotation>();
        Annotation annotation = null;

        for (Annotation annotation2 : parentAnnotListeners) {
            annotation = annotation2;

            if (annotation.getTier() == tier) {
                children.add(annotation);
            }
        }

        return children;
    }

    // Comparable<Annotation> interface method
    @Override
	public int compareTo(Annotation obj) {
        // if not both RefAnnotations, delegate comparison to first alignable root.
        // if both RefAnnotations, call compareRefAnnotations()
        int ret = 0;

        Annotation a1 = this;
        Annotation a2 = obj;

        // comparison is on basis of embedding in 'network' of Annotations. When an Annotation
        // is marked deleted, it is already detached from the network.
        if (a1.isMarkedDeleted() || a2.isMarkedDeleted()) {
            return compareOtherwise(a1, a2);
        }

        int numOfRefAnnotations = 0;

        if (this instanceof RefAnnotation) {
            numOfRefAnnotations += 1;
            a1 = ((RefAnnotation) this).getFirstAlignableRoot();
        }

        if (obj instanceof RefAnnotation) {
            numOfRefAnnotations += 1;
            a2 = ((RefAnnotation) obj).getFirstAlignableRoot();
        }

        if (a1 != a2) { // two different RefAnnotations can have same alignable parent         
            ret = ((AlignableAnnotation) a1).getBegin().compareTo(((AlignableAnnotation) a2).getBegin());

            if (ret == 0) {
                ret = -1;
            }
             // otherwise shared begin timeslot would lead to equality
        } else { // same alignable parent

            // if one Alignable and one RefAnnotation, the Alignable one comes before the Ref one
            if (numOfRefAnnotations == 1) {
                if (this instanceof AlignableAnnotation) {
                    ret = -1;
                } else {
                    ret = 1;
                }
            } else if (numOfRefAnnotations == 2) { // two RefAnnotations with the same parent. 
                ret = compareRefAnnotations((RefAnnotation) this,
                        (RefAnnotation) obj);
            }
        }

        return ret;
    }

    private int compareOtherwise(Annotation a1, Annotation a2) {
        if (a1.equals(a2)) {
            return 0;
        } else {
            // no refs to parents, fall back on index on Tier
            Tier t1 = a1.getTier();
            Tier t2 = a2.getTier();

            if (t1 == t2) {
                List<? extends Annotation> v = null;

                v = ((TierImpl) t1).getAnnotations();

                if (v.indexOf(a1) < v.indexOf(a2)) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return -1; // FIXME: fails symmetry requirement for compareTo()
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param a1 DOCUMENT ME!
     * @param a2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int compareRefAnnotations(RefAnnotation a1, RefAnnotation a2) {
        // if on same tier, compare via "chain of reference".
        // else, take first and go up reference tree until on same tier or until alignable parent,
        // take second and repeat
        int ret = 0;

        if ((a1.getTier() == a2.getTier()) &&
                (a1.getReferences().get(0) == a2.getReferences()
                                                            .get(0))) { // and have same parent
            ret = compareUsingRefChain(a1, a2);
        } else {
            Annotation parent = (a1.getReferences().get(0));

            if (parent instanceof RefAnnotation) {
                ret = compareRefAnnotations((RefAnnotation) parent, a2);
            }

            if (ret == 0) { // still undecided
                parent = (a2.getReferences().get(0));

                if (parent instanceof RefAnnotation) {
                    ret = compareRefAnnotations(a1, (RefAnnotation) parent);
                }
            }
        }

        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @param a1 DOCUMENT ME!
     * @param a2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int compareUsingRefChain(RefAnnotation a1, RefAnnotation a2) {
        // a1 and a2 are on the same tier, and have the same alignable parent
        int ret = 1; // default: a1 comes before a2
        RefAnnotation nextR = a1.getNext();

        if (a1 == a2) {
            ret = 0;
        } else {
            while (nextR != null) {
                if (nextR == a2) { // a2 after a1
                    ret = -1;

                    break;
                }

                nextR = nextR.getNext();
            }
        }

        return ret;
    }
}

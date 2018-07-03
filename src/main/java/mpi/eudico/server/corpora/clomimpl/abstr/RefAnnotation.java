package mpi.eudico.server.corpora.clomimpl.abstr;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;


/**
 * DOCUMENT ME!
 * $Id: RefAnnotation.java 43483 2015-03-05 10:11:50Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class RefAnnotation extends AbstractAnnotation {
    private List<Annotation> references; // should contain minimally 1 reference
    private RefAnnotation next; // for chains of RefAnnots that have same parent
    private RefAnnotation previous; // for chains of RefAnnots that have same parent

    /**
     * <p>
     * MK:02/06/24<br> A RefAnnotation refers to at least one "parent
     * Annotation", called "theReference".  The current implementation allows
     * the "parent Annotation" to be null.  Be aware, that you have to set the
     * parent before you can use the returned object.<br>
     * The returned RefAnnotation has no value. You have to set it with
     * setValue().
     * </p>
     *
     * @param theReference "parent Annotation". vital, but currently nullable.
     * @param theTier tier of the returned RefAnnotation.
     */
    public RefAnnotation(Annotation theReference, Tier theTier) {
        super(); //MK:02/06/28 this super constructor is good for nothing. Should handle at least tier.

        references = new ArrayList<Annotation>();

        if (theReference != null) {
            addReference(theReference);
        } else {
            //			MK:02/09/17 happens too often to have any use.
            //			System.out.println("creating RefAnnotation without reference for tier " + theTier.getName());
        }

        this.setTier(theTier);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public long getBeginTimeBoundary() {
        long beginTimeBoundary = Long.MAX_VALUE;
        Constraint c = ((TierImpl) getTier()).getLinguisticType()
                        .getConstraints();

        if (c != null) {
            beginTimeBoundary = c.getBeginTimeForRefAnnotation(this);
        } else {
            long beginB = 0;

            Iterator<Annotation> refIter = references.iterator();

            while (refIter.hasNext()) {
                beginB = refIter.next().getBeginTimeBoundary();

                beginTimeBoundary = Math.min(beginTimeBoundary, beginB);
            }
        }

        return beginTimeBoundary;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public long getEndTimeBoundary() {
        long endTimeBoundary = 0;

        Constraint c = ((TierImpl) getTier()).getLinguisticType()
                        .getConstraints();

        if (c != null) {
            endTimeBoundary = c.getEndTimeForRefAnnotation(this);
        } else {
            long endB = 0;

            Iterator<Annotation> refIter = references.iterator();

            while (refIter.hasNext()) {
                endB = refIter.next().getEndTimeBoundary();

                endTimeBoundary = Math.max(endTimeBoundary, endB);
            }
        }

        return endTimeBoundary;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public AlignableAnnotation getFirstAlignableRoot() {
        Annotation parent = references.get(0);

        if (parent instanceof AlignableAnnotation) {
            return (AlignableAnnotation) parent;
        } else {
            return ((RefAnnotation) parent).getFirstAlignableRoot();
        }
    }

    /**
     * MK:02/06/07
     *
     * @param theReference "parent Annotation" of 'this'. Not nullable.
     */
    public void addReference(Annotation theReference) {
        //System.out.println("add ref: " + theReference.getValue() + " to: " + getValue());
        references.add(theReference);

        // register as listener with reference
        theReference.addParentAnnotationListener(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param theReference DOCUMENT ME!
     */
    public void removeReference(Annotation theReference) {
        //System.out.println("remove ref: " + theReference.getValue() + " from: " + getValue());
        // unregister as listener with reference
        theReference.removeParentAnnotationListener(this);

        references.remove(theReference);

        if (references.size() == 0) { // not refering to any annotation anymore
            markDeleted(true);
        }
    }

    /**
     * <p>
     * MK:02/06/24<br> The returned Vector contatains Elements of type
     * Annotation,  which are the "parent Annotation" of 'this' RefAnnotation.
     * It is a runtime error if 'this' RefAnnotation has no "parent
     * Annotation".
     * </p>
     *
     * @return all "parent Annotation" of 'this' RefAnnotation.
     */
    public List<Annotation> getReferences() {
        return references;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public RefAnnotation getNext() {
        return next;
    }

    /**
     * DOCUMENT ME!
     *
     * @param a DOCUMENT ME!
     */
    public void setNext(RefAnnotation a) {
        //	System.out.println(getValue() + " has as next: " + a.getValue());
        next = a;

        if (a != null) {
            a.setPrevious(this);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean hasNext() {
        if (next != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public RefAnnotation getPrevious() {
        return previous;
    }

    /**
     * WATCH OUT: call this carefully !!!!! Always via setNext().
     *
     * @param a DOCUMENT ME!
     */
    public void setPrevious(RefAnnotation a) {
        previous = a;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean hasPrevious() {
        if (previous != null) {
            return true;
        } else {
            return false;
        }
    }

    // ParentAnnotationListener implementation
    @Override
	public void parentAnnotationChanged(EventObject e) {
        if (e.getSource() instanceof Annotation) {
            if (((Annotation) e.getSource()).isMarkedDeleted()) {
                removeReference(((Annotation) e.getSource()));
            }
        }
    }

    /**
     * Checks if this RefAnnotation has a parent Annotation.
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean hasParentAnnotation() {
        boolean hasParent = false;

        if (references.size() > 0) {
            hasParent = true;
        }

        return hasParent;
    }

    /**
     * Parent-child relationship for RefAnnotations is defined by an explicit
     * reference.
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Annotation getParentAnnotation() {
        Annotation p = null;

        if (hasParentAnnotation()) {
            p = references.get(0);
        }

        return p;
    }
}

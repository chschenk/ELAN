package mpi.eudico.server.corpora.clomimpl.abstr;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.server.corpora.clom.ExternalReference;


/**
 * A class for grouping a number of external reference objects.  Note:  this
 * class extend ExternalReferenceImpl for reasons of convenience (and it
 * allows that a group can be added to a group).
 *
 * @author Han Sloetjes
 */
public class ExternalReferenceGroup extends ExternalReferenceImpl {
	private static final long serialVersionUID = 2228267290242872070L;
	private List<ExternalReference> group;

    /**
     * Creates a new instance.
     */
    public ExternalReferenceGroup() {
        super(null, ExternalReference.REFERENCE_GROUP);
    }

    /**
     * Creates a new instance.
     *
     * @param value the value, can be null in case of a group
     */
    public ExternalReferenceGroup(String value) {
        super(value, ExternalReference.REFERENCE_GROUP);
    }

    /**
     * Adds an external reference to the group.
     *
     * @param extRef the ExternalReference to add
     */
    public void addReference(ExternalReference extRef) {
        if (extRef == null) {
            // ignore
            return;
        }

        if (group == null) {
            group = new ArrayList<ExternalReference>(4);
        }

        group.add(extRef);
    }

    /**
     * Removes the specified external reference from the list.
     *
     * @param extRef the external reference
     *
     * @return true if the reference has been removed and thus the group has
     *         been changed,  false if the group has not been changed
     */
    public boolean removeReference(ExternalReference extRef) {
        if ((extRef == null) || (group == null)) {
            // ignore
            return false;
        }

        return group.remove(extRef);
    }

    /**
     * Returns the list of references.  Note: grants direct access to the list.
     *
     * @return the list of references, can be null
     */
    public List<ExternalReference> getAllReferences() {
        return group;
    }

    /**
     * Type is always the group type.
     *
     * @see mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl#setReferenceType(int)
     */
    @Override
	public void setReferenceType(int refType) {
        // ignore
    }

    /**
     * Returns a param. string of all references in the group.
     *
     * @see mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl#paramString()
     */
    @Override
	public String paramString() {
        if (group != null) {
        	StringBuilder buf = new StringBuilder();
            ExternalReference er = null;

            for (int i = 0; i < group.size(); i++) {
                er = group.get(i);
                buf.append(i + " - " + er.paramString() + "; ");
            }

            return buf.toString();
        }

        return super.paramString();
    }

    /**
     * Creates a copy (deep clone) of this group.
     *
     * @see java.lang.Object#clone()
     */
    @Override
	public ExternalReferenceGroup clone() throws CloneNotSupportedException {
        ExternalReferenceGroup groupCopy = new ExternalReferenceGroup();

        if (group != null) {
            for (int i = 0; i < group.size(); i++) {
            	ExternalReference er = group.get(i);

                if (er != null) {
                    groupCopy.addReference(er.clone());
                }
            }
        }

        return groupCopy;
    }

    /**
     * First check the list, then call super.equals(). The elements in the list
     * must be equal and in the same order. (The order could be ignored?
     *
     * @see mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object obj) {
        if (!(obj instanceof ExternalReferenceGroup)) {
            return false;
        }

        ExternalReferenceGroup other = (ExternalReferenceGroup) obj;

        final List<ExternalReference> allReferences = other.getAllReferences();
		if ((allReferences == null && group != null) ||
                (allReferences != null && group == null)) {
            return false;
        }

        if (group != null) {
            if (group.size() != allReferences.size()) {
                return false;
            }

            // check the objects, ignore the order?
            ExternalReference o1;

            // check the objects, ignore the order?
            ExternalReference o2;

            for (int i = 0; i < group.size(); i++) {
                o1 = group.get(i);
                o2 = allReferences.get(i);

                if ((o1 == null) && (o2 != null)) {
                    return false;
                }

                if ((o1 != null) && (o2 == null)) {
                    return false;
                }

                if ((o1 != null) && !o1.equals(o2)) {
                    return false;
                }
            }
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
    	int h = 12;
    	if (group != null) {
    		h += group.hashCode();
    	}

    	return h;
    }


    /**
     * Takes an existing ExternalReference(Group) and a freshly created ExternalReference.
     * <p>
     * If the group is null, just returns the fresh reference.
     * There is no need to make it into a group.
     * <p>
     * If the group is indeed a group, clone it and add the fresh reference to the clone.
     * <p>
     * Otherwise, creates a new group with the existing reference, plus the new one.
     * <p>
     * Note that this never changes existing references (or groups).
     * @param existing
     * @param fresh
     * @return
     */
    public static ExternalReference create(ExternalReference existing, ExternalReference fresh) {
    	if (existing == null) {
    		return fresh;
    	} else if (existing instanceof ExternalReferenceGroup) {
    		ExternalReferenceGroup group;
			try {
				group = ((ExternalReferenceGroup)existing).clone();
	    		group.addReference(fresh);
	    		return group;    		
			} catch (CloneNotSupportedException e) {
				// can't happen: we know that this type supports clone().
				e.printStackTrace();
				return fresh;
			}
    	} else {
    		ExternalReferenceGroup group = new ExternalReferenceGroup();
    		
			group.addReference(existing);
    		group.addReference(fresh);
    		
    		return group;    		
    	}
    }
    
	@Override
	public String getTypeString() {
		return "reference_group";
	}

}

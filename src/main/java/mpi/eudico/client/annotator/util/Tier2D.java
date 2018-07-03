package mpi.eudico.client.annotator.util;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Stores a Tier and has some additional display specific fields.
 *
 * @author Han Sloetjes
 * @version 0.1 10/7/2003
 */
public class Tier2D {
    private TierImpl tier;
    private ArrayList<Tag2D> tags;
    private String name;
    private boolean isActiveTier;
    //private boolean isVisible; //may not be needed

    /**
     * Creates a new Tier2D instance
     *
     * @param tier DOCUMENT ME!
     */
    public Tier2D(TierImpl tier) {
        this.tier = tier;
        name = tier.getName();

        isActiveTier = false;
        //isVisible = true;
        tags = new ArrayList<Tag2D>(20);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public TierImpl getTier() {
        return tier;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tag DOCUMENT ME!
     */
    public void addTag(Tag2D tag) {
        tags.add(tag);
        tag.setTier2D(this);
    }

    /**
     * Insert the Tag2D into the list.<br>
     * Determine the right index by means of the x position.
     *
     * @param tag the Tag2D to insert
     */
    public void insertTag(Tag2D tag) {
        tag.setTier2D(this);

        Tag2D t1;
        Tag2D t2;

        for (int i = 0; i < tags.size(); i++) {
            t1 = tags.get(i);

            if (((i == 0) || (i == (tags.size() - 1))) &&
                    (tag.getX() < t1.getX())) {
                tags.add(i, tag);

                return;
            }

            if (i < (tags.size() - 1)) {
                t2 = tags.get(i + 1);

                if ((tag.getX() > t1.getX()) && (tag.getX() < t2.getX())) {
                    tags.add(i + 1, tag);

                    return;
                }
            }
        }

        tags.add(tag);
    }

    /**
     * DOCUMENT ME!
     *
     * @param tag DOCUMENT ME!
     */
    public void removeTag(Tag2D tag) {
        tags.remove(tag);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Iterator<Tag2D> getTags() {
        return tags.iterator();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<Tag2D> getTagsList() {
        return tags;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return name;
    }

    /**
     * Update the name of this Tier2D object after a change in the name of the
     * Tier.
     */
    public void updateName() {
        String old = name;

        if (!old.equals(tier.getName())) {
            name = tier.getName();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param active DOCUMENT ME!
     */
    public void setActive(boolean active) {
        isActiveTier = active;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isActive() {
        return isActiveTier;
    }
}

package mpi.eudico.server.corpora.clomimpl.flex;

/**
 * A record for an item element, which will be converted to an annotation. All
 * members are public.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class Item {
    /** the type of item, the value of the "type" attribute */
    public String type;

    /** the "lang" attribute */
    public String lang;

    /** the value of the item */
    public String value;
    /** the tier name is the combination of element name, item type and item language */
    public String tierName;
}

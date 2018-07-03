package mpi.dcr;

import java.util.List;

import mpi.dcr.isocat.DCSelection;
import mpi.dcr.isocat.Profile;


/**
 * A new interface from which all GMT based classes are removed.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public interface IDCRConnector2 {
    /**
     * Returns the name of the DCR.
     *
     * @return the name
     */
    public String getName();

    /**
     * Currently returns a list of Data Category objects containing a summary
     * of the information on a Data Category.
     *
     * @param profileId the profile identifier
     *
     * @return a Data Category Selection containing the Data Categories of
     * one profile
     *
     * @throws DCRConnectorException if a connection could not be made
     */
    public DCSelection getDataCategories(String profileId)
        throws DCRConnectorException;

    /**
     * Returns a summary of a Data Category
     *
     * @param a_urid the identifier
     *
     * @return a summary of a Data Category
     *
     * @throws DCRConnectorException if a connection could not be made
     */
    public DCSmall getDataCategory(String a_urid) throws DCRConnectorException;

    /**
     * Returns a list of Profile objects; mappings of profile id to profile name
     *
     * @return a list of Profile objects (mappings from profile id to profile name)
     *
     * @throws DCRConnectorException if a connection could not be made
     */
    public List<Profile> getProfiles() throws DCRConnectorException;
}

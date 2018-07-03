package mpi.dcr;

import java.util.List;


/**
 * Interface for a DCR connector that extends IDCRConnector by adding a method
 * that returns a list of  "small" data category objects (containing a summary
 * of the information of a category).
 * 
 *
 * @author Han Sloetjes
 * @version 1.0
 * @version 2.0 July 2009 the GMT based webservice does not exist anymore,
 * neither in France, nor in Nijmegen
 */
public interface ILATDCRConnector extends IDCRConnector2 {
    /**
     * Returns a list of objects containing only part of the information of
     * each  Data Category of the specified profile.
     *
     * @param a_profile the profile
     * @param a_registrationStatus the registration status
     *
     * @return a list with DCSmall objects
     *
     * @throws DCRConnectorException a connector exception
     */
    public List<DCSmall> getDCSmallList(String a_profile, String a_registrationStatus)
        throws DCRConnectorException;
}

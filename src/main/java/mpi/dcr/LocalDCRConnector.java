package mpi.dcr;

import java.util.ArrayList;
import java.util.List;

import mpi.dcr.isocat.DCSelection;
import mpi.dcr.isocat.Profile;
import mpi.dcr.isocat.RestDCRConnector;


/**
 * A connector to a locally stored Data Category Registry or Selection.
 *
 * @author Han Sloetjes
 */
public class LocalDCRConnector implements ILATDCRConnector {
    /** the name of the local dcs connector */
    protected String name = "Local DCR Connector";

    /** the list of data categories! */
    protected List<DCSmall> catList = null;
    
    /** current prefered language*/
    protected String currentLanguage = null;

    /**
     * Creates a new LocalDCRConnector instance
     */
    public LocalDCRConnector() {
        super();
        catList = new ArrayList<DCSmall>();
    }

    /**
     * Returns the name of the connector
     *
     * @return the name
     */
    @Override
	public String getName() {
        return name;
    }

    /**
     * Returns a list of dat categories in the form of DCSmall objects,
     * containing only a selection  of the information of each category.
     *
     * @param a_profile the profile
     * @param a_registrationStatus the registration status, ignored
     *
     * @return a list with DCSmall objects
     *
     * @throws DCRConnectorException a connector exception
     */
    @Override
	public List<DCSmall> getDCSmallList(String a_profile, String a_registrationStatus)
        throws DCRConnectorException {
        if (a_profile == null) {
            // return all
            return catList;
        }

        List<DCSmall> profCats = new ArrayList<DCSmall>();
        DCSmall dc = null;

        for (int i = 0; i < catList.size(); i++) {
            dc = catList.get(i);

            if ((dc.getProfiles() != null) && (dc.getProfiles().length > 0)) {
                for (int j = 0; j < dc.getProfiles().length; j++) {
                    if (dc.getProfiles()[j].getId().equals(a_profile)) {// hier... of getName?
                        profCats.add(dc);

                        break;
                    }
                }
            }
        }

        return profCats;
    }

    /**
     * Saves the current dc selection to a local xml/rng file. To be
     * implemented by subclasses.
     */
    protected void saveDCS() {
        /*
           if (catList == null) {
               return;
           }
           //DCR dcr = new DCR();
           // hier... maybe don't need summaries and selection??
           // or don't use data categories?
           DataCategorySelection sel = new DataCategorySelection();
           DataCategorySummary sum = null;
           //DataCategory dc = null;
           Identifier identifier = null;
           Description desc = null;
           Definition def = null;
           Profile[] profs = null;
           BroaderConceptGeneric bcg = null;
           DCSmall small = null;
        
           for (int i = 0; i < catList.size(); i++) {
               small = (DCSmall) catList.get(i);
               sum = new DataCategorySummary();
               identifier = new Identifier();
               identifier.setContentByString(small.getIdentifier());
               sum.setIdentifier(identifier);
               sum.setIdByString(small.getId());
               //dc = new DataCategory();//??
               //dc.setIdByString(small.getId());
               def = new Definition();
               if (small.getDesc() != null) {
                   def.setContentByString(small.getDesc());
               }
               desc = new Description();
               desc.addDefinition(def);
               if (small.getBroaderDCId() != null) {
                   bcg = new BroaderConceptGeneric();
                   bcg.setContentByString(small.getBroaderDCId());
                   desc.setBroaderConceptGeneric(bcg);
               }
               if (small.getProfiles() != null && small.getProfiles().length > 0) {
                   profs = new Profile[small.getProfiles().length];
        
                   for (int j = 0; j < small.getProfiles().length; j++) {
                       profs[j] = new Profile();
                       profs[j].setContentByString(small.getProfiles()[j]);
                   }
                   desc.setProfiles(profs);
               }
               sel.addDataCategorySummaries(sum);
           }
           try {
               Document doc = sel.makeDocument();
        
               // write document
           } catch (ParserConfigurationException pce) {
               // log error
           }
         */
    }

    /**
     * Reads the current dc selection from a local xml/rng file. To be
     * implemented by subclasses.
     */
    protected void readDCS() {
    }

    // *** Local connector specific methods *** //
    /**
     * Adds the data categories from the specified list to the local cache.
     *
     * @param datCats the DC's to add to the list and local dc selection
     *
     * @throws DCRConnectorException a connector exception
     */
    public void addDataCategories(List<DCSmall> datCats) throws DCRConnectorException {
    	final int size1 = datCats.size();
dcloop: 
        for (int i = 0; i < size1; i++) {
        	DCSmall small1 = datCats.get(i);

            final int size2 = catList.size();
			for (int j = 0; j < size2; j++) {
            	DCSmall small2 = catList.get(j);

                if (small1.getId().equals(small2.getId())) {
                   
                	// already in the list, replace
                	catList.set(j, small1);
                    continue dcloop;
                }
            }

            // not in the list
            catList.add(small1);
        }

        saveDCS();
    }
    
    /**
     * Update the data category in the specified list and local cache.
     *
     * @param dc the DC to be updated in the list and local dc selection
     * @param updateLocalCache, if true, saves the local dc selection file
     *
     * @throws DCRConnectorException a connector exception
     */
    public void replaceDC(DCSmall dc) throws DCRConnectorException {
    	List<DCSmall> dcList = new ArrayList<DCSmall>();
    	dcList.add(dc);
    	
    	addDataCategories(dcList);
//        DCSmall small1 = null;
// 
//       for (int j = 0; j < catList.size(); j++) {
//    	   small1 = (DCSmall) catList.get(j);
//
//    	   if (small1.getId().equals(dc.getId())) {
//    		   catList.set(j, dc);
//    		   break;
//    	   }
//       }      
//    
//       saveDCS();
    }
    


    /**
     * Removes the data categories from the specified list from the local
     * cache.
     *
     * @param datCats the list of data categories to remove
     *
     * @throws DCRConnectorException a connector exception
     */
    public void removeCategories(List<DCSmall> datCats) throws DCRConnectorException {
    	final int size1 = datCats.size();
dcloop: 
        for (int i = 0; i < size1; i++) {
        	DCSmall small1 = datCats.get(i);

            final int size2 = catList.size();
            
			for (int j = 0; j < size2; j++) {
            	DCSmall small2 = catList.get(j);

                if (small1.getId().equals(small2.getId())) {
                    // in the list
                    catList.remove(j);

                    continue dcloop;
                }
            }
        }

        saveDCS();
    }

    /**
     * Returns the dc summary object for the given id.
     *
     * @param dcId the id as a string
     *
     * @return a DCSmall object
     */
    public DCSmall getDCSmall(String dcId) {
        if (dcId == null) {
            return null;
        }

        DCSmall small = null;

        final int size = catList.size();
		for (int i = 0; i < size; i++) {
            small = catList.get(i);

            if (small.getId().equals(dcId)) {
                return small;
            }
        }

        return null;
    }
    
    /**
     * Returns the dc summary object for the given id.
     * Forcefully loads the dc from registry in the
     * following cases when
     * 
     * 1. dc is not available locally
     * 2. dc is not loaded fully
     *
     * @param dcId the id as a string
     *
     * @return a DCSmall object, can be null
     */
    public DCSmall getDCSmallLoaded(String dcId) {
        if (dcId == null) {
            return null;
        }

        DCSmall small = null;

        final int size = catList.size();
		for (int i = 0; i < size; i++) {
            small = catList.get(i);

            if (small.getId().equals(dcId)) {
            	break;
            } else {
            	small = null;
            }
        }
        
        //loads the dc from the registry
        if (small == null || !small.isLoaded()) {
    		try {
    			RestDCRConnector remoteConnector = new RestDCRConnector();
    			small = remoteConnector.getDataCategory(dcId);
    			replaceDC(small);
    			
    			return small;
    		} catch (DCRConnectorException e) {
    			//e.printStackTrace();
    			System.err.println("DCRConnectorException: " + e.getMessage());
    		}
        } 
        
        if(small != null){
        	return small;
        }

        return null;
    }
    
    /**
     * Returns the dc name in the current preferred language.
     * If name is not available in the given language,
     * then returns the name in English
     *
     * @param dcId the id as a string
     *
     * @return String, the name of the dataCategory
     */
    public String getNameForDC(String dcId) {
        DCSmall dc = getDCSmall(dcId);
       
        String name = null;
        if(dc != null){
        	if(currentLanguage != null){
        		name = dc.getName(currentLanguage);
        	}
        	
        	if(name == null){
        		name = dc.getName();
        	}
        }
        return name;
    }
  
    /**
	 * Returns the preferred language by the user, cannot be null
	 * Return "en" if no preferred language set
     *
     * @return String, cannot be null
     */
    public String getPreferedLanguage() { 
    	if(currentLanguage == null){
    		return DCSmall.EN;
    	}
    	
        return currentLanguage;
    }

	@Override
	public DCSelection getDataCategories(String profileId) throws DCRConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DCSmall getDataCategory(String a_urid) throws DCRConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Profile> getProfiles() throws DCRConnectorException {
		// TODO Auto-generated method stub
		return null;
	}
}

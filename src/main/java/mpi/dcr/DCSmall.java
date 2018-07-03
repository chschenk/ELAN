package mpi.dcr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import mpi.dcr.isocat.ISOCatConstants;
import mpi.dcr.isocat.Profile;

/**
 * An object to store a summary of the information available for a data
 * category. The id, (camelcase) identifier, a description, the profiles and
 * the broader concept generic are stored.
 *
 * @author Han Sloetjes
 * @version 1.0
 * @version 2.0 July 2009 some changes to be up-to-date with the changes 
 * in the dcr model. The signature of getProfiles() and setProfiles() have 
 * changed; they now use Profile objects instead of Strings
 * @author aarsom
 * @version 3.0 May 2014, name and description values are multilingual
 */
public class DCSmall {
    private String id;
    private Integer idAsInteger;
    private String identifier;
    
    //HashMap<String, String> : <lang, value>
    private HashMap<String, String> nameMap;
    private HashMap<String, String> descMap;
    
    private DCSmall broaderDC; // or just the id?
    private String broaderDCId;
    
    private Profile[] profiles;
    private boolean loaded = false;
    
    public final static String EN ="en";
    
    private List<String> languages;
    
    private long lastUpdated = 0;
    
    

    /**
     * Creates a new DCSmall instance
     *
     * @param profile the profile
     * @param id the unique id
     * @param identifier the textual identifier
     */
    public DCSmall(Profile profile, String id, String identifier) {
        super();
        // throw exception if any param is null
        profiles = new Profile[] { profile };
        this.id = id;
        this.identifier = identifier;
        createIdAsInt(id);
        
        nameMap = new HashMap<String, String>();
        descMap = new HashMap<String, String>();
        
        languages = new ArrayList<String>();
    }

    /**
     * Creates a new DCSmall instance without profile.
     *
     * @param id the unique id
     * @param identifier the textual identifier
     */
    public DCSmall(String id, String identifier) {
        super();
        // throw exception if any param is null
        profiles = new Profile[] { };
        this.id = id;
        this.identifier = identifier;
        createIdAsInt(id);
        
        nameMap = new HashMap<String, String>();
        descMap = new HashMap<String, String>();
        
        languages = new ArrayList<String>();
    }
    
    /**
     * Returns the identifier field, a unique string representation.  This way
     * DCSmall can be used as user object in trees etc.
     *
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return identifier;
    }

    private void createIdAsInt(String id) {
        if (id == null) {
            idAsInteger = Integer.valueOf(-1);
        }

        try {
        	if (id.startsWith(ISOCatConstants.PID_PREFIX)) {
        		idAsInteger = Integer.valueOf(id.substring(ISOCatConstants.PID_PREFIX.length()));
        	} else {
        		idAsInteger = Integer.valueOf(id);
        	}
        } catch (NumberFormatException nfe) {
        }
    }

    /**
     * Returns the broader concept generic as a DCSmall object.
     *
     * @return Returns the broaderDC, can be null
     *
     * @see #getBroaderDCId()
     */
    public DCSmall getBroaderDC() {
        return broaderDC;
    }

    /**
     * Sets the broader concept generic as a DCSmall object.
     *
     * @param broaderDC the broaderDC
     */
    public void setBroaderDC(DCSmall broaderDC) {
        this.broaderDC = broaderDC;
    }

    /**
     * Returns the id of the broader concept generic data category.
     *
     * @return Returns the id of the broader DC as a String, or null
     */
    public String getBroaderDCId() {
        return broaderDCId;
    }

    /**
     * Set the id of the broader concept generic data category.
     *
     * @param broaderDCId the id of the broader DC as a String
     */
    public void setBroaderDCId(String broaderDCId) {
        this.broaderDCId = broaderDCId;
    }
    
    /**
     * Returns the list of languages
     * 
     * @return
     */
    public List<String> getAvailableLanguages(){
    	List<String> langList = new ArrayList<String>();
    	Iterator<String> it =  descMap.keySet().iterator();
    	while(it.hasNext()){
    		langList.add(it.next());
    	}
    	
    	return langList;
    }
    
    /**
     * Returns a description of the category (in English)
     *
     * @return Returns the description
     */
    public String getDesc() {
        return descMap.get(EN);
    }

    /**
     * Sets the description of the category (in English).
     *
     * @param desc the description
     */
    public void setDesc(String desc) {
    	setDesc(EN, desc);
        
    }
    
    /**
     * Returns a description of the category 
     * in the given language
     * 
     * @param lang, the language
     *
     * @return Returns the description, can be null
     */
    public String getDesc(String lang) {
        return descMap.get(lang);
    }

    /**
     * Sets the description of the category
     * in the given language
     *
     * @param lang, the language
     * @param desc the description
     */
    public void setDesc(String lang, String desc) {
        descMap.put(lang, desc);
        if(!languages.contains(lang)){
			languages.add(lang);
		}
    }
    
    /**
    * Sets the description of the category
    * in the available languages
    *
    * @param map<string, string> : <language, description>
    */
   public void setDescMap(HashMap<String, String> map) {
	   Iterator<Entry<String, String>> it = map.entrySet().iterator();
	   Entry<String, String> entry;
	   
	   while(it.hasNext()){
		  entry =  it.next();
		  descMap.put(entry.getKey(), entry.getValue());
	      if(!languages.contains(entry.getKey())){
			languages.add(entry.getKey());
	      }
	   }
   }
   
   /**
    * Sets the name of the category
    * in the available languages
    *
    * @param map<string, string> : <language, description>
    */
   public void setNameMap(HashMap<String, String> map) {
      
	   Iterator<Entry<String, String>> it = map.entrySet().iterator();
	   Entry<String, String> entry;
	   
	   while(it.hasNext()){
		  entry =  it.next();
		  nameMap.put(entry.getKey(), entry.getValue());
	      if(!languages.contains(entry.getKey())){
			languages.add(entry.getKey());
	      }
	   }
   }
    
    public List<String> getLanguages(){
    	return languages;
    }
    
    /**
     * Returns the name of the data category (in English).
     * 
     * @return the name
     */
	public String getName() {
		return nameMap.get(EN);
	}

	/**
	 * Sets the name of the the data category (in English).
	 * 
	 * @param name the name of the dc(in English)
	 */
	public void setName(String name) {
		setName(EN, name);
	}
	
	/**
     * Returns the name of the dc
     * in the given language
     * 
     * @param lang, the language
     * @return the name, can be null
     */
	public String getName(String lang) {
		return nameMap.get(lang);
	}

	/**
	 * Sets the name of the dc
	 * 
	 * @param lang, the language
	 * @param name the name of the dc
	 */
	public void setName(String lang, String name) {
		nameMap.put(lang, name);
		if(!languages.contains(lang)){
			languages.add(lang);
		}
	}

    /**
     * Returns the unique id as a String
     *
     * @return Returns the id as a String
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id as a String
     *
     * @param id the id as a String
     */
    public void setId(String id) {
        this.id = id;
        createIdAsInt(id);
    }

    /**
     * Returns the (camelcase) textual identifier of the category, e.g.
     * "commonNoun"
     *
     * @return Returns the textual identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the (camelcase) textual identifier of the category.
     *
     * @param identifier the identifier.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns an array of profiles this category belongs to.
     *
     * @return Returns the profiles
     */
    public Profile[] getProfiles() {
        return profiles;
    }

    /**
     * Sets the array of profiles this category belongs to.
     *
     * @param profiles the profiles
     */
    public void setProfiles(Profile[] profiles) {
        this.profiles = profiles;
    }

    /**
     * Returns the unique id as an Integer.
     *
     * @return Returns the id as an Integer
     */
    public Integer getIdAsInteger() {
        return idAsInteger;
    }

    /**
     * Sets the id as an Integer, the String representation of the id is
     * updated as well.
     *
     * @param idAsInteger the id as an Integer
     */
    public void setIdAsInteger(Integer idAsInteger) {
        this.idAsInteger = idAsInteger;

        if (idAsInteger != null) {
            id = idAsInteger.toString();
        }
    }

    /**
     * Returns whether all information of this summary is loaded.  By default
     * only the id and identifier are loaded (for performance reasons).
     *
     * @return Returns whether this record is fully loaded or only the id and
     *         identifier.
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Sets the loaded flag.
     *
     * @param loaded set to true when additional information has been loaded
     *        like description , broader concept generic, profiles.
     *
     * @see #isLoaded()
     */
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
    
    /**
     * Sets the last updated date
     * 
     * @param lastUpdate - date in milliseconds
     * 
     */
    public void setLastUpdate(long lastUpdate){
    	this.lastUpdated = lastUpdate;
    }
    
    
    /**
     * Gets the last updated date
     * 
     * @return long - date in milliseconds
     * 
     */
    public long getLastUpdated(){
    	return lastUpdated;
    }
    
   public int compareTo(DCSmall dc) {
		return String.CASE_INSENSITIVE_ORDER.compare
				(this.identifier,dc.identifier); 
	}
}

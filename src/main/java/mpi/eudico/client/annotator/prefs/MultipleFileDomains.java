package mpi.eudico.client.annotator.prefs;

import mpi.eudico.client.annotator.Constants;

import mpi.eudico.client.annotator.util.ClientLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A singleton class for loading and storing multiple file domains.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class MultipleFileDomains {
    /** a suffix for a domains directories */
    public static final String DIR_SUF = ".Dirs";

    /** a suffix for a domains paths */
    public static final String PATH_SUF = ".Paths";
    private final static String DOMAINS_FILEPATH = Constants.ELAN_DATA_DIR +
        System.getProperty("file.separator") + "mf_domains.pfsx";
    private static MultipleFileDomains domains;
    private static PreferencesReader xmlPrefsReader;
    private static PreferencesWriter xmlPrefsWriter;
    private static List<String> domainNames;
    private static Map<String, List<String>> domainMap;

    /**
     * Creates a new MultipleFileDomains instance
     */
    private MultipleFileDomains() {
        initDomains();
    }

    private void initDomains() {
        xmlPrefsReader = new PreferencesReader();
        xmlPrefsWriter = new PreferencesWriter();
        // read stored
        readDomain();
    }

    private static void readDomain() {
        try {
            domainMap = (Map<String,List<String>>)(Map)xmlPrefsReader.parse(DOMAINS_FILEPATH);
            
            if (domainMap != null) {
	            Object val = domainMap.get("DomainNames");
	
	            if (val instanceof List) {
	                domainNames = (List<String>) val;
	            }
            }
        } catch (Exception ex) {
            ClientLogger.LOG.warning(
                "Could not load the domains preferences file");
        }

        if (domainMap == null) {
            domainMap = new HashMap<String, List<String>>(12);
        }

        if (domainNames == null) {
            domainNames = new ArrayList<String>();
            domainMap.put("DomainNames", domainNames);
        }
    }

    private static void writeDomain() {
        try {
            xmlPrefsWriter.encodeAndSave(domainMap, DOMAINS_FILEPATH);
        } catch (Exception ex) {
            ClientLogger.LOG.warning(
                "Could not save the domains preferences file");
        }
    }

    /**
     * Returns the single instance of this class.
     *
     * @return the single instance
     */
    public static MultipleFileDomains getInstance() {
        if (domains == null) {
            domains = new MultipleFileDomains();
        }

        return domains;
    }

    /**
     * Returns the list of stored domain names.
     *
     * @return list of domain names
     */
    public List<String> getDomainList() {
        return domainNames;
    }

    /**
     * Adds the domain to the map. The name is added to list of names, the 
     * dir list and path list are added to the map using suffixes.
     *
     * @param key the domain name
     * @param dirs the list of directories in the domain
     * @param paths the list of filepaths in the domain
     */
    public void addDomain(String key, List<String> dirs, List<String> paths) {
        if (!domainNames.contains(key)) {
            domainNames.add(key);
        }

        domainMap.put(key + DIR_SUF, dirs);
        domainMap.put(key + PATH_SUF, paths);
        
        writeDomain();
    }

    /**
     * Removes the name from the list of domains and the dir and path list
     * belonging to the domain from the domain map.
     *
     * @param name the name of the domain
     */
    public void removeDomain(String name) {
        domainNames.remove(name);
        domainMap.remove(name + DIR_SUF);
        domainMap.remove(name + PATH_SUF);
        
        writeDomain();
    }

    /**
     * Returns a Map containing maximal 2 entries (String - List pairs):  1
     * list for file paths, 1 list for file dirs.
     *
     * @param key the name of the domain
     *
     * @return the mapping or null
     */
    public Map<String, List<String>> getDomain(String key) {
        if (domainNames.contains(key)) {
            HashMap<String, List<String>> result = new HashMap<String, List<String>>(2);
            String name = key + DIR_SUF;
            result.put(name, (List<String>) domainMap.get(name));
            name = key + PATH_SUF;
            result.put(name, (List<String>) domainMap.get(name));

            return result;
        }

        return null;
    }
}

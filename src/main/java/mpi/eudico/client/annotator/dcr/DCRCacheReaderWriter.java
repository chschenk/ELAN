package mpi.eudico.client.annotator.dcr;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mpi.dcr.DCRConnectorException;
import mpi.dcr.DCSmall;
import mpi.dcr.isocat.Profile;
import mpi.dcr.isocat.RestDCRConnector;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.util.IoUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * A reader and writer of the locally stored cache. In this first version the
 * GMT format is used.
 *
 * @author Han Sloetjes
 * @version 1.0
 * @version 2.0 profiles are now stored as a struct with an id and a feature 
 * of type "name"
 */
public class DCRCacheReaderWriter {
    private final String struct = "struct";
    private final String feat = "feat";
    //private final String dcr = "DCR"; //or DCS
    private final String dcs = "DCS";
    private final String dc = "DC";
    private final String ai = "AI";
    private final String ar = "AR";
    private final String desc = "Desc";
    private final String def = "definition";
    private final String ident = "identifier";
    private final String prof = "profile";
    private final String broad = "broaderConceptGeneric";
    private final String type = "type";
    private final String id = "id";
    private final String lang = "lang";
    private final String loaded = "loaded";
    private final String lastUpdated = "lastUpdated";
    private final String name = "name";
    private final String urlPref = "http://www.isocat.org/datcat/DC-";
    private DocumentBuilder db;
    private XMLReader reader;
    //private XMLReaderAdapter xmlAdapter;
    private String filePath;

    // indicates whether stored data categories were found without
    // an id value
    private boolean profileIdsNeeded = false;
    
    private boolean newVersion = false;
    
    /**
     * Creates a new DCRCacheReaderWriter instance
     *
     * @throws ParserConfigurationException parser config exception
     */
    public DCRCacheReaderWriter() throws ParserConfigurationException {
        super();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        db = dbf.newDocumentBuilder();

        try {
            reader = XMLReaderFactory.createXMLReader(
                    "org.apache.xerces.parsers.SAXParser");
            reader.setFeature("http://xml.org/sax/features/namespaces", true);
            reader.setFeature("http://xml.org/sax/features/validation", false);
            reader.setFeature("http://apache.org/xml/features/validation/schema",
                false);
            reader.setFeature("http://apache.org/xml/features/validation/dynamic",
                false);
        } catch (SAXException se) {
        	ClientLogger.LOG.warning("Could not create a parser for the DCR cache: " + se.getMessage());
        } /*catch (IOException ioe) {
           ioe.printStackTrace();
           }*/}

    /**
     * Returns the path to the local file.
     *
     * @return Returns the filePath.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Sets the path to the cache file
     *
     * @param filePath The filePath to set.
     */
    public void setFilePath(String filePath) {
    	setFilePath(filePath, false);
    }  
    
    /**
     * Sets the path to the cache file
     *
     * @param filePath The filePath to set.
     * @param newVersion true if DCSelection2.xml is found
     */
    public void setFilePath(String filePath, boolean newVersion) {
        this.filePath = filePath;
        this.newVersion = newVersion;
    } 

    /**
     * Creates a GMT format DOM with DCR or DCS as root struct element.
     *
     * @param categories list of DCSmall objects
     *
     * @throws IOException if there is no filepath specified
     * @throws ParserConfigurationException if no Document could be created
     * @throws NullPointerException if the category list is null
     */
    public synchronized void save(List<DCSmall> categories)
        throws IOException, ParserConfigurationException {
        if (filePath == null) {
            throw new IOException("No filepath specified.");
        }

        if (categories == null) {
            throw new NullPointerException("The list of categories is null");
        }

        if (db != null) {
            Document doc = db.newDocument();
            Element dcsElem = doc.createElement(struct);
            doc.appendChild(dcsElem);
            dcsElem.setAttribute(type, dcs);

            DCSmall dcsmall = null;

            for (int i = 0; i < categories.size(); i++) {
                dcsmall = categories.get(i);

                Element dcEl = doc.createElement(struct);
                dcEl.setAttribute(type, dc);
                dcEl.setAttribute(id, dcsmall.getId());
                dcsElem.appendChild(dcEl);

                if ((dcsmall.getIdentifier() != null) &&
                        (dcsmall.getIdentifier().length() > 0)) {
                    Element aiEl = doc.createElement(struct);
                    aiEl.setAttribute(type, ai);
                    dcEl.appendChild(aiEl);

                    Element arEl = doc.createElement(struct);
                    arEl.setAttribute(type, ar);
                    aiEl.appendChild(arEl);

                    Element idEl = doc.createElement(feat);
                    idEl.setAttribute(type, ident);
                    idEl.appendChild(doc.createTextNode(dcsmall.getIdentifier()));
                    arEl.appendChild(idEl);
                }

                Element descEl = doc.createElement(struct);
                descEl.setAttribute(type, desc);
                dcEl.appendChild(descEl);

                if(dcsmall.getLanguages().size() > 0){
                	String language;
                	for(int l=0; l < dcsmall.getLanguages().size(); l++){
                		language = dcsmall.getLanguages().get(l);
                		Element langEl = doc.createElement(struct);
                		langEl.setAttribute(type, lang);
                        
                        Element defEl = doc.createElement(feat);
                        defEl.setAttribute(type, def);
                        defEl.setAttribute(lang, language);
                        defEl.appendChild(doc.createTextNode(dcsmall.getDesc(language)));
                        langEl.appendChild(defEl);
                        
                        Element nameEl = doc.createElement(feat);
                        nameEl.setAttribute(type, name);
                        nameEl.setAttribute(lang, language);
                        nameEl.appendChild(doc.createTextNode(dcsmall.getName(language)));
                        langEl.appendChild(nameEl);
                        
                        descEl.appendChild(langEl);
                	}
                }
                
                
                if ((dcsmall.getProfiles() != null) &&
                        (dcsmall.getProfiles().length > 0)) {
                    for (int j = 0; j < dcsmall.getProfiles().length; j++) {
                        if (dcsmall.getProfiles()[j].getName().length() == 0) {
                            continue;
                        }

                        Element prEl = doc.createElement(struct);
                        prEl.setAttribute(type, prof);
                        prEl.setAttribute(id, dcsmall.getProfiles()[j].getId());
                        Element nameEl = doc.createElement(feat);
                        nameEl.setAttribute(type, name);
                        nameEl.appendChild(doc.createTextNode(
                                dcsmall.getProfiles()[j].getName()));
                        prEl.appendChild(nameEl);
                        descEl.appendChild(prEl);
                    }
                }

                if ((dcsmall.getBroaderDCId() != null) &&
                        (dcsmall.getBroaderDCId().length() > 0)) {
                    Element brEl = doc.createElement(feat);
                    brEl.setAttribute(type, broad);
                    brEl.appendChild(doc.createTextNode(
                            dcsmall.getBroaderDCId()));
                    descEl.appendChild(brEl);
                }
                
                Element loadEl = doc.createElement(feat);
                loadEl.setAttribute(type, "loaded");
                loadEl.appendChild(doc.createTextNode(Boolean.toString(dcsmall.isLoaded())));
                descEl.appendChild(loadEl);
                
                if(dcsmall.isLoaded()){
                	 Element lastUpdateEl = doc.createElement(feat);
                     lastUpdateEl.setAttribute(type, lastUpdated);
                     lastUpdateEl.appendChild(doc.createTextNode(new Timestamp(dcsmall.getLastUpdated()).toString()));
                     dcEl.appendChild(lastUpdateEl);
                }
            }

            // write
            try {
                IoUtil.writeEncodedFile("UTF-8", filePath,
                    doc.getDocumentElement());
            } catch (Exception ioe) {
                throw new IOException(ioe.getMessage());
            }
        }
    }

    /**
     * Reads the data categories from the cache.
     *
     * @return the list of categories from the cache
     *
     * @throws IOException any io exception
     */
    public synchronized List<DCSmall> read() throws IOException {
        if (reader != null) {
            try {
                DcrGmtAdapter adapter; 
                if(newVersion){
                	adapter = new DcrGmtAdapter2();
                } else {
                	adapter= new DcrGmtAdapter();
                }
                
                reader.setContentHandler(adapter);
                reader.parse(filePath);
                List<DCSmall> curDCList = adapter.getDCS();
                
                if (profileIdsNeeded) {
                	try {
                		RestDCRConnector rconn = new RestDCRConnector();
                		List<Profile> profs = rconn.getProfiles();
                		
                		DCSmall small1;
                		Profile[] curProfs;
                		Profile pr;
                		for (int i = 0; i < curDCList.size(); i++) {
                			small1 = curDCList.get(i);
                			curProfs = small1.getProfiles();
                			for (Profile curProf : curProfs) {
                				if (curProf.getId().length() == 0) {
                					for (int k = 0; k < profs.size(); k++) {
                						pr = profs.get(k);
                						if (pr.getName().equals(curProf.getName())) {
                							curProf.setId(pr.getId());
                							break;
                						}
                					}
                				}
                			}
                		}
                	} catch (DCRConnectorException dce) {
                		ClientLogger.LOG.warning("Could not retrieve additional information from ISOCat");
                	}               	
                }
                
                return curDCList;
            } catch (SAXException se) {
            	ClientLogger.LOG.warning("Could not read the local data categories cache: " + se.getMessage());
            }
        }

        return new ArrayList<DCSmall> ();
    }

    /**
     * A parser adapter for parsing GMT style dc selection file.
     *
     * @author Han Sloetjes
     */
    private class DcrGmtAdapter extends DefaultHandler {
        protected List<DCSmall> datcats;

        // parse time objects
        protected String idAttr;
        protected String identifierAttr;
        protected String descAttr;
        protected DCSmall dcsmall;
        protected String broaderDCIdAttr;
        protected List<Profile> profiles;
        protected String curProfId;
        protected String curStruct;
        protected String curFeat;
        protected String content = "";
        protected int structLevel = 0;

        /** Holds value of property DOCUMENT ME! */
        boolean inFeat = false;

        /**
         * Creates a new DcrGmtAdapter instance
         */
        public DcrGmtAdapter() {
            super();
            //super(xmlReader);
            datcats = new ArrayList<DCSmall>();
            profiles = new ArrayList<Profile>();
        }

        /**
         * Return the list of small Data Category objects.
         *
         * @return the list of data categories
         */
        public List<DCSmall> getDCS() {
            return datcats;
        }

        /**
         * @see org.xml.sax.helpers.XMLReaderAdapter#characters(char[], int,
         *      int)
         */
        @Override
		public void characters(char[] ch, int start, int length)
            throws SAXException {
            //System.out.println("characters..." + start + " " + length);
            // for some reason all kinds of in between element characters (spaces, newlines) are passed
            // to this method ??
            if (inFeat) {
                content = new String(ch, start, length);

                if (curFeat == ident) {
                    identifierAttr = content;
                } else if (curFeat == def) {
                    descAttr = content;
                } else if (curFeat == broad) {
                    broaderDCIdAttr = content;
                } else if (curFeat == prof) {//old profile feature
                    //profiles.add(content);
                	profiles.add(new Profile("", content));
                } else if (curFeat == name) {//new profile feature
                	profiles.add(new Profile(curProfId, content));
                }
            }
        }

        /**
         * @see org.xml.sax.helpers.XMLReaderAdapter#endElement(java.lang.String,
         *      java.lang.String, java.lang.String)
         */
        @Override
		public void endElement(String uri, String localName, String qName)
            throws SAXException {
            //System.out.println("end..." + localName);
        	inFeat = false;

            if (localName.equals(struct)) {
                structLevel--;
            }

            if (structLevel == 1) { // dc level, dcs is 0

                if ((idAttr != null) && (profiles.size() > 0)) {
                    dcsmall = new DCSmall(null, idAttr, identifierAttr);
                    dcsmall.setBroaderDCId(broaderDCIdAttr);
                    dcsmall.setProfiles(profiles.toArray(new Profile[profiles.size()]));
                    dcsmall.setDesc(descAttr);
                    dcsmall.setLoaded(false);
                    datcats.add(dcsmall);
                }
            }
        }

        /**
         * @see org.xml.sax.helpers.XMLReaderAdapter#parse(java.lang.String)
         */

        /*
           public void parse(String systemId) throws IOException, SAXException {
               super.parse(systemId);
           }
         */
        /**
         * @see org.xml.sax.helpers.XMLReaderAdapter#startElement(java.lang.String,
         *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
		public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
            //System.out.println("start..." + localName);
            if (localName.equals(struct)) {
                structLevel++;

                String ty = atts.getValue(type);

                if (dc.equals(ty)) {
                    curStruct = dc;
                    // reset
                    idAttr = atts.getValue(id);
                    // HS Dec 2010 if the ID from the cache is a simple numeric value (3057) 
                    // prefix the isocat url to change it into an official PID
                    if (idAttr.length() > 0) { // test other assumptions, like start with http://...?
	                    try {
	                    	Integer.parseInt(idAttr);
	                    	// no error, then add the prefix
	                    	idAttr = urlPref + idAttr; 
	                    } catch (NumberFormatException nfe) {
	                    	// assume the id is in the right format (or check the affix?)
	                    }
                    }
                    identifierAttr = null;
                    descAttr = null;
                    broaderDCIdAttr = null;
                    profiles.clear();
                    content = "";
                } else if (ar.equals(ty)) {
                    curStruct = ar;
                } else if (desc.equals(ty)) {
                    curStruct = desc;
                } else if (prof.equals(ty)) {
                	curStruct = prof;// the new profile struct
                	curProfId = atts.getValue(id);
                }
            } else if (localName.equals(feat)) {
            	inFeat = true;

                String fty = atts.getValue(type);

                if (ident.equals(fty)) {
                    curFeat = ident;
                } else if (def.equals(fty)) {
                    curFeat = def;
                } else if (prof.equals(fty)) {
                    curFeat = prof;// the old profile feature, replaced by a struct
                    profileIdsNeeded = true;
                } else if (broad.equals(fty)) {
                    curFeat = broad;
                } else if (name.equals(fty)) {
                    curFeat = name; // the new profile name feature
                }
            }
        }
    }
    
    /**
     * A parser adapter for parsing GMT style dc selection file.
     *
     * @author aarsom
     */
    private class DcrGmtAdapter2 extends DcrGmtAdapter {

        // parse time objects
        protected HashMap<String,String> descMap;
        protected HashMap<String,String> nameMap;

        private boolean inProfile;
        private boolean load;
        private long lastUpdate = 0;
        
        private String language;
    	
    	/**
         * Creates a new DcrGmtAdapter instance
         */
        public DcrGmtAdapter2() {
            super();
        }

        /**
         * @see org.xml.sax.helpers.XMLReaderAdapter#characters(char[], int,
         *      int)
         */
        @Override
		public void characters(char[] ch, int start, int length)
            throws SAXException {
            if (inFeat) {
                content = new String(ch, start, length);

                if (curFeat == ident) {
                    identifierAttr = content;
                } else if (curFeat == def) {
                    descMap.put(language, content);
                } else if (curFeat == broad) {
                    broaderDCIdAttr = content;
                } else if (curFeat == prof) {//old profile feature
                    //profiles.add(content);
                	profiles.add(new Profile("", content));
                } else if (curFeat == name) {//new profile feature
                	if(inProfile){
                		profiles.add(new Profile(curProfId, content));
                	} else {
                		nameMap.put(language, content);
                	}
                } else if(curFeat == loaded){
                	load = Boolean.valueOf(content);
                } else if(curFeat == lastUpdated){
                	lastUpdate = Timestamp.valueOf(content).getTime();
                }
            }
        }

        /**
         * @see org.xml.sax.helpers.XMLReaderAdapter#endElement(java.lang.String,
         *      java.lang.String, java.lang.String)
         */
        @Override
		public void endElement(String uri, String localName, String qName)
            throws SAXException {
            //System.out.println("end..." + localName);
        	inFeat = false;
        	inProfile = false;
        	
            if (localName.equals(struct)) {
                structLevel--;
            }

            if (structLevel == 1) { // dc level, dcs is 0

                if ((idAttr != null) && (profiles.size() > 0)) {
                    dcsmall = new DCSmall(null, idAttr, identifierAttr);
                    dcsmall.setBroaderDCId(broaderDCIdAttr);
                    dcsmall.setProfiles(profiles.toArray(new Profile[profiles.size()]));
                    dcsmall.setLoaded(load);
                    dcsmall.setDescMap(descMap);
                    dcsmall.setNameMap(nameMap);
                    
                    dcsmall.setLastUpdate(lastUpdate);
                    
                    datcats.add(dcsmall);
                }
            }
        }

        /**
         * @see org.xml.sax.helpers.XMLReaderAdapter#parse(java.lang.String)
         */

        /*
           public void parse(String systemId) throws IOException, SAXException {
               super.parse(systemId);
           }
         */
        /**
         * @see org.xml.sax.helpers.XMLReaderAdapter#startElement(java.lang.String,
         *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
		public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
            //System.out.println("start..." + localName);
            if (localName.equals(struct)) {
                structLevel++;

                String ty = atts.getValue(type);

                if (dc.equals(ty)) {
                    curStruct = dc;
                    // reset
                    idAttr = atts.getValue(id);
                    // HS Dec 2010 if the ID from the cache is a simple numeric value (3057) 
                    // prefix the isocat url to change it into an official PID
                    if (idAttr.length() > 0) { // test other assumptions, like start with http://...?
	                    try {
	                    	Integer.parseInt(idAttr);
	                    	// no error, then add the prefix
	                    	idAttr = urlPref + idAttr; 
	                    } catch (NumberFormatException nfe) {
	                    	// assume the id is in the right format (or check the affix?)
	                    }
                    }
                    identifierAttr = null;
                    descAttr = null;
                    broaderDCIdAttr = null;
                    profiles.clear();
                    content = "";
                    
                    descMap = new HashMap<String, String>();
                    nameMap = new HashMap<String, String>();
                    inProfile  =false;
                    load = false;
                    language = "";
                } else if (ar.equals(ty)) {
                    curStruct = ar;
                } else if (desc.equals(ty)) {
                    curStruct = desc;
                } else if (prof.equals(ty)) {
                	curStruct = prof;// the new profile struct
                	inProfile = true;
                	curProfId = atts.getValue(id);
                }
            } else if (localName.equals(feat)) {
            	inFeat = true;

                String fty = atts.getValue(type);

                if (ident.equals(fty)) {
                    curFeat = ident;
                } else if (def.equals(fty)) {
                    curFeat = def;
                    language = atts.getValue(lang);
                } else if (prof.equals(fty)) {
                	curFeat = prof;// the old profile feature, replaced by a struct
                    profileIdsNeeded = true;
                } else if (broad.equals(fty)) {
                    curFeat = broad;
                } else if (name.equals(fty)) {
                    curFeat = name; 
                    if(!inProfile){
                    	language = atts.getValue(lang);
                    }
                } else if (loaded.equals(fty)) {
                    curFeat = loaded;
                } else if (lastUpdated.equals(fty)) {
                    curFeat = lastUpdated;
                }
            }
        }
    }
}

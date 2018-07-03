package mpi.dcr.isocat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mpi.dcr.DCRConnectorException;
import mpi.dcr.DCSmall;
import mpi.dcr.ILATDCRConnector;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Connects to the ISOCat Rest service and extracts minimal information.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class RestDCRConnector implements ILATDCRConnector {
    private static boolean ISOCatUseFullPID = false;

    static {
        String val = System.getProperty("ISOCatUseFullPID");

        if ((val != null) && val.toLowerCase().equals("true")) {
            ISOCatUseFullPID = true;
        }
    }

    private String user = "guest";
    private String isoName = "ISOCat REST DCR Connector";

    /** Refers to the URL for the DCR server */
    private String DCRLocation = ISOCatConstants.BASE_URL;
    private String dcrName = isoName;
    // cache the profiles until a new call to getProfiles
    // use the cache to file in profile id's that are lacking
    // in the information on data categories or selections
    private List<Profile> curRemoteProfiles;
    private XMLReader wsReader;
    private Object wsLock = new Object();
    private XMLReader dcifReader;
    private Object dcifLock = new Object();
    private XMLReader dcReader;
    private Object dcLock = new Object();
    private final Logger LOG = Logger.getLogger("DCR");
    private String acceptAll = "application/x-dcif+xml, text/xml, text/html, application/x-httpd-php";
    /**
     * Creates a new RestDCRConnector instance
     */
    public RestDCRConnector() {
        super();
        curRemoteProfiles = new ArrayList<Profile>(20);
        String locProperty = System.getProperty("DCRLocation");

        if (locProperty != null) {
            DCRLocation = locProperty;

            String propName = System.getProperty("DCRName");

            if (propName != null) {
                dcrName = propName;
            }
        }
    }

    /**
     * Returns the name of the connector
     *
     * @return the name of the connector
     */
    @Override
	public String getName() {
        return dcrName;
    }

    /**
     * Returns the location of the connector
     *
     * @return the location of the connector
     */
    public String getDCRLocation() {
        return DCRLocation;
    }

    /**
     * Returns the profiles' id's and names from the "guest" user workspace.
     *
     * @return a list of Profile objects, containing mappings of profile id - profile name
     *
     * @throws DCRConnectorException
     */
    @Override
	public List<Profile> getProfiles() throws DCRConnectorException {
        return getProfiles(user);
    }

    /**
     * Returns the profiles' id's and names from the workspace of the specified
     * user. Note: As is, this will only work for the guest account.
     *
     * @param userName the name of the user
     *
     * @return a list of Profile objects; mappings of profile id - profile name
     *
     * @throws DCRConnectorException
     */
    public List<Profile> getProfiles(String userName)
        throws DCRConnectorException {
        URL url = null;

        try {
            url = new URL(DCRLocation + "user/" + userName + "/workspace");

            //url = new URL("http://www.isocat.org/rest/profile/5");
        } catch (MalformedURLException mue) {
            //mue.printStackTrace();
        	if (LOG.isLoggable(Level.INFO)) {
        		LOG.info("MUE: " + mue.getMessage());
        	}
            throw new DCRConnectorException("Unable to connect to DCR: " +
                mue.getMessage());
        }

        if (url != null) {
            HttpURLConnection conn = getConnection(url, false);
            
            InputStream is = null;
            try {
                // check response code throw exception if ! OK
                int respCode = conn.getResponseCode();

                if (respCode != 200) {
                	if (LOG.isLoggable(Level.INFO)) {
                		LOG.info("Unexpected response code: " + respCode);
                	}
                    throw new DCRConnectorException(
                        "Unable to connect to DCR: " + respCode + " " +
                        conn.getResponseMessage());
                }

                Object cont = conn.getContent();

                if (cont instanceof InputStream) {
                    is = (InputStream) cont;

                    InputSource source = new InputSource(is);

                    synchronized (wsLock) {
                        getWSReader().parse(source);
                        curRemoteProfiles.clear();
                        List<Profile> profs = ((WSHandler) getWSReader().getContentHandler()).getProfiles();
                        curRemoteProfiles.addAll(profs);
                        
                        is.close();
                        
                        return profs;
                    }
                }
            } catch (SAXException sax) {
                throw new DCRConnectorException(sax);
            } catch (IOException ioee) {
                throw new DCRConnectorException(ioee);
            } finally {
                //System.out.println("Close connection...");
            	if (is != null) {
            		try {
						is.close();
					} catch (IOException e) {
					}
            	}
                conn.disconnect();
            }
        }

        return null;
    }

    /**
     * Retrieves the data categories of the specified profile.
     *
     * @param profileId the (short) id of the profile
     *
     * @return a selection of profiles
     *
     * @throws DCRConnectorException any dcr connection exception
     */
    @Override
	public DCSelection getDataCategories(String profileId)
        throws DCRConnectorException {
        URL url = null;
        
        try {
        	// is there a full id for profiles?
            url = new URL(DCRLocation + "profile/" +
                    profileId /* + "?dcif-mode=domain"*/ );// or profileId + ".dcif"
        } catch (MalformedURLException mue) {
            // mue.printStackTrace();
        	if (LOG.isLoggable(Level.INFO)) {
        		LOG.info("MUE: " + mue.getMessage());
        	}
            throw new DCRConnectorException("Unable to connect to DCR: " +
                mue.getMessage());
        }

        if (url != null) {
            HttpURLConnection conn = getConnection(url, true);
            InputStream is = null;

            try {            	
                // check response code throw exception if ! OK
                int respCode = conn.getResponseCode();

                if (respCode != 200) {
                	if (LOG.isLoggable(Level.INFO)) {
                		LOG.info("Unexpected response code: " + respCode);
                	}
                    throw new DCRConnectorException(
                        "Unable to connect to DCR: " + respCode + " " +
                        conn.getResponseMessage());
                }

                Object cont = conn.getContent();

                if (cont instanceof InputStream) {
                    is = (InputStream) cont;

                    /*
                       InputStreamReader isr = new InputStreamReader(is);
                       char[] buf = new char[256];
                       int r;
                       while ((r = isr.read(buf)) > 0) {
                           System.out.print(new String(buf, 0, r));
                       }
                       System.out.println();
                       try {
                           isr.close();
                       } catch (IOException iio) {
                           iio.printStackTrace();
                       }                      
                     */
                    InputSource source = new InputSource(is);

                    synchronized (dcifLock) {
                        getDCIFReader().parse(source);

                        DCSelection dcs = ((DCIF_DCS_Handler) getDCIFReader()
                                                                  .getContentHandler()).getDCSelection();
                        // fill in profile information, only the name is likely available
                        Profile mainProf = null;
                        Profile loopProf = null;
                        if (curRemoteProfiles.size() == 0) {
                        	try {
                        		getProfiles();
                        	} catch (DCRConnectorException dce) {
                        		if (LOG.isLoggable(Level.INFO)) {
                            		LOG.info("DCR connection exception: " + dce.getMessage());
                            	}
                        	}
                        }

                    	for (int i = 0; i < curRemoteProfiles.size(); i++) {
                    		loopProf = curRemoteProfiles.get(i);
                    		if (loopProf.getName().equals(profileId)) {
                    			mainProf = loopProf;
                    			break;
                    		}
                    	}
                    	if (mainProf != null) {
	                    	DCSmall loopDC;
	                    	loopProf = null;
	                    	for (int i = 0; i < dcs.getDataCategories().size(); i++) {
	                    		loopDC = dcs.getDataCategories().get(i);
	                    		if (loopDC.getProfiles().length == 0) {
	                    			loopDC.setProfiles(new Profile[]{new Profile(mainProf)});
	                    		} else {
	                    			for (int j = 0; j < loopDC.getProfiles().length; j++) {
	                    				loopProf = loopDC.getProfiles()[j];
	                    				if (loopProf.getId().length() == 0) {
	                    					if (loopProf.getName().equals(mainProf.getName())) {
	                    						loopProf.setId(mainProf.getId());
	                    					} else {
	                    						// check other profile names
	                    						Profile lp2;
	                    						for (int k = 0; k < curRemoteProfiles.size(); k++) {
	                    							lp2 = curRemoteProfiles.get(k);
	                    							if (loopProf.getName().equals(lp2.getName())) {
	                    								loopProf.setId(lp2.getId());
	                    							}
	                    						}
	                    					}
	                    				}
	                    			}
	                    		}
	                    		//update broader generic data categories??
	                    	}
                    	}

                        //System.out.println("Selection: " + dcs);
                        return dcs;
                    }
                }
            } catch (SAXException sax) {
                throw new DCRConnectorException(sax);
            } catch (IOException ioee) {
                throw new DCRConnectorException(ioee);
            } finally {
                //System.out.println("Close connection...");
            	if (is != null) {
            		try {
						is.close();
					} catch (IOException e) {
					}
            	}
                conn.disconnect();
            }
        }

        return null;
    }

    /**
     * Returns a list of  data categories for the specified profile id. 
     * The registration status is ignored.
     * 
     * @param profileId the pid of the profile 
     * @param registrationStatus ignored
     * @return a list of DCSmall objects
     */
	@Override
	public List<DCSmall> getDCSmallList(String profileId, String registrationStatus) throws DCRConnectorException {
		DCSelection dcSelection = getDataCategories(profileId);
		
		return dcSelection.getDataCategories();
	}
	
    /**
     * Returns a data category object with limited information extracted
     * from the stream.
     *
     * @param fullPID the full pid (including prefix) can be provided (for future use)
     *
     * @return a data category object with limited information, or null
     *
     * @throws DCRConnectorException any dcr connection exception
     */
    @Override
	public DCSmall getDataCategory(String fullPID) throws DCRConnectorException {
        URL url = null;

        try {
            if (!ISOCatUseFullPID) {
                if (fullPID.startsWith(ISOCatConstants.PID_PREFIX)) {
                    String dcId = fullPID.substring(ISOCatConstants.PID_PREFIX.length());
                    url = new URL(DCRLocation + "dc/" + dcId); // or "dc/" + dcId + ".dcif"
                } else {
                    url = new URL(DCRLocation + "dc/" + fullPID);
                }
            } else {
                url = new URL(DCRLocation + "dc/" + fullPID);
            }
        } catch (MalformedURLException mue) {
            //mue.printStackTrace();
        	if (LOG.isLoggable(Level.INFO)) {
        		LOG.info("MUE: " + mue.getMessage());
        	}
            throw new DCRConnectorException("Unable to connect to DCR: " +
                mue.getMessage());
        }

        //System.out.println("URL: " + url);

        if (url != null) {
            HttpURLConnection conn = getConnection(url, true);
            InputStream is = null;

            try {
                // check response code throw exception if ! OK
                int respCode = conn.getResponseCode();
            	
                if (respCode != 200) {
                	if (LOG.isLoggable(Level.INFO)) {
                		LOG.info("Unexpected response code: " + respCode);
                	}
                    throw new DCRConnectorException(
                        "Unable to connect to DCR: " + respCode + " " +
                        conn.getResponseMessage());
                } else {
                	if (LOG.isLoggable(Level.INFO)) {
                		LOG.info("Response Content Type: " + conn.getContentType() +
                				" Content Length: " + conn.getContentLength());
                	}
                }

                Object cont = conn.getContent();

                if (cont instanceof InputStream) {
                    is = (InputStream) cont;

                    /*
                       InputStreamReader isr = new InputStreamReader(is);
                       char[] buf = new char[256];
                       int r;
                       while ((r = isr.read(buf)) > 0) {
                    
                           System.out.print(new String(buf, 0, r));
                       }
                       System.out.println();
                       try {
                           isr.close();
                       } catch (IOException iio) {
                           iio.printStackTrace();
                       }                      
                     */
                    InputSource source = new InputSource(is);

                    synchronized (dcLock) {
                        getDCReader().parse(source);

                        DCSmall dcSmall = ((DCIF_DC_Handler) getDCReader()
                                                                 .getContentHandler()).getDC();
                        
                        if (dcSmall != null) {
	                        dcSmall.setLastUpdate(Calendar.getInstance().getTimeInMillis());
	                        //System.out.println("Selection: " + dcSmall);
	                        if (dcSmall.getProfiles() != null) {
	                        	Profile lp1, lp2;
	                        	for (int i = 0; i < dcSmall.getProfiles().length; i++) {
	                        		lp1 = dcSmall.getProfiles()[i];
	                        		if (lp1.getId().length() == 0) {
	                        			for (int j = 0; j < curRemoteProfiles.size(); j++) {
	                        				lp2 = curRemoteProfiles.get(j);
	                        				if (lp1.getName().equals(lp2.getName())) {
	                        					lp1.setId(lp2.getId());
	                        				}
	                        			}
	                        		}
	                        	}
	                        }    
                        }
                        // update broader generic data categories??
                        return dcSmall;
                    }
                }
            } catch (SAXException sax) {
                throw new DCRConnectorException(sax);
            } catch (IOException ioee) {
                throw new DCRConnectorException(ioee);
            } finally {
                //System.out.println("Close connection...");
            	if (is != null) {
            		try {
						is.close();
					} catch (IOException e) {
					}
            	}
                conn.disconnect();
            }
        }

        return null;
    }

    /**
     * Creates and configures a HTTP Url connection.
     *
     * @version Dec 2014 after the transition of ISOcat to a new hosting provider the Accept header 
     * for dcif has to be "application/x-dcif+xml" otherwise a 406 Not Acceptable will be returned
     * 
     * @version Oct 2017 it seems there have been changes at TermWeb, causing many 406 response codes.
     * It seems that the returned content type depends on which DC or Profile etc. is requested (?)
     * 
     * @param url the url (with parameters)
     * @param dcifMode if true the accept header is set to "application/dcif+xml" 
     * (from Dec 2014 on "application/x-dcif+xml"), otherwise to "application/xml"
     *
     * @return a HttpURLConnection
     *
     * @throws DCRConnectorException if an IOException or any other exception
     *         occurs
     */
    private HttpURLConnection getConnection(URL url, boolean dcifMode)
        throws DCRConnectorException {
        if (url == null) {
            return null;
        }

        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDefaultUseCaches(true);

            if (dcifMode) {
                //conn.setRequestProperty("Accept", "application/x-dcif+xml");
                conn.setRequestProperty("Accept", acceptAll);
            } else {
                //conn.setRequestProperty("Accept", "application/xml");
            	// HS Febr 2015 it now seems to require "text/xml"  
                //conn.setRequestProperty("Accept", "text/xml");
                conn.setRequestProperty("Accept", acceptAll);
            }
            
            
            conn.setRequestMethod("GET");
            conn.connect();

            return conn;
        } catch (IOException ioe) {
            throw new DCRConnectorException("Unable to connect to DCR" +
                ioe.getMessage());
        } catch (Exception e) {
            throw new DCRConnectorException("Unable to connect to DCR" +
                e.getMessage());
        }
    }

    private XMLReader getWSReader() throws SAXException {
        if (wsReader == null) {
            wsReader = XMLReaderFactory.createXMLReader(
                    "org.apache.xerces.parsers.SAXParser");
            wsReader.setFeature("http://xml.org/sax/features/namespaces", false);
            wsReader.setFeature("http://xml.org/sax/features/validation", false);
            wsReader.setContentHandler(new WSHandler());
        }

        return wsReader;
    }

    private XMLReader getDCIFReader() throws SAXException {
        if (dcifReader == null) {
            dcifReader = XMLReaderFactory.createXMLReader(
                    "org.apache.xerces.parsers.SAXParser");
            dcifReader.setFeature("http://xml.org/sax/features/namespaces",
                false);
            dcifReader.setFeature("http://xml.org/sax/features/validation",
                false);
            dcifReader.setContentHandler(new DCIF_DCS_Handler());
        }

        return dcifReader;
    }

    private XMLReader getDCReader() throws SAXException {
        if (dcReader == null) {
            dcReader = XMLReaderFactory.createXMLReader(
                    "org.apache.xerces.parsers.SAXParser");
            dcReader.setFeature("http://xml.org/sax/features/namespaces", false);
            dcReader.setFeature("http://xml.org/sax/features/validation", false);
            dcReader.setContentHandler(new DCIF_DC_Handler());
        }

        return dcReader;
    }

    /**
     * testing
     *
     * @param args
     */
    public static void main(String[] args) {
        RestDCRConnector connector = new RestDCRConnector();

        try {
            //connector.loadWorkspace();
            System.out.println(connector.getProfiles());
            //connector.getDataCategories(""+6);
            connector.getDataCategory("" + 1988);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}

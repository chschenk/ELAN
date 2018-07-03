package mpi.eudico.webserviceclient.weblicht;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

import mpi.eudico.webserviceclient.WsClientRest;

/**
 * Harvest WebLicht services or loads them from cache.
 * 
 * @author Han Sloetjes
 */
public class WebLichtHarvester {
	private List<WLServiceDescriptor> defaultDescriptors;
	final Logger LOG;
	public long cacheReloadInterval = 24 * 60 * 60 * 1000; // one day in milliseconds
	public final String CACHE_FILENAME = "WebLichtServices.xml";
	private final String NS_WC = "*";
	
	public WebLichtHarvester() {
		LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	}


	/**
	 * A list of preselected known services (currently tokenizers, pos-taggers
	 * and lemmatizers.
	 * 
	 * @param inputType the type of input for the webservice, either plain text or
	 * tcf+xml
	 * 
	 * @return a list of services
	 */
	public List<WLServiceDescriptor> getServicesDescriptors() {
		defaultDescriptors = new ArrayList<WLServiceDescriptor>();
		return defaultDescriptors;
	}
	
	/**
	 * Tries to harvest web services remotely, as published by Weblicht.
	 * 
	 * @return the (OAI-PMH) xml string as returned by the harvest service
	 */
	public String harvestServicesRemote() throws IOException {
		WsClientRest wsClient = new WsClientRest();
		
		String urlString = "http://weblicht.sfs.uni-tuebingen.de/apps/harvester/resources/services";
		Map<String, String> requestHead = new HashMap<String, String>(3);
		requestHead.put("Connection", "Keep-Alive");
		// can't ask the user to provide name + pwd
		String base64Encoded = "Y2xhcmluZGRldmVsb3Blcjp3ZWJzZXJ2aWNlaGFydmVzdGluZw==";
		requestHead.put("Authorization", "Basic " + base64Encoded);
		try {
			String result = wsClient.callServiceGetMethod(urlString, null, requestHead, null, null, 0, 0);
			if (result == null) {
				LOG.warning("An error occurred while harvesting available web services...");
			} else {
				//System.out.println(result);
				return result;
			}
		} catch (IOException ioe) {
			LOG.warning("IO error while harvesting: " + ioe.getMessage());
			throw(ioe);
		}
		
		return null;
	}
	
	/**
	 * Parses relevant web service from the specified xml string, stores them in the local list and 
	 * returns them.
	 * 
	 * @param xmlString the harvested or cached (OAI-PMH) xml string
	 * @return a list of service descriptors
	 * 
	 * @throws IOException as a wrapper round any possible exception
	 */
	public  List<WLServiceDescriptor>  parseRelevantServices(String xmlString) throws IOException {
		if (xmlString == null || xmlString.isEmpty()) {
			return null;
		}
		List<WLServiceDescriptor> wlDescriptors = null;
		
		try {
		    DOMImplementationLS domImplementation = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
		    LSInput lsInput = domImplementation.createLSInput();
		    lsInput.setEncoding("UTF-8");
		    lsInput.setStringData(xmlString);
		    
		    LSParser parser = domImplementation.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
		    Document wlDoc = parser.parse(lsInput);
		    
		    wlDescriptors = parseRelevantServices(wlDoc);
		   
		} catch (IllegalAccessException iae) {
			//System.out.println("Illegal Access: " + iae.getMessage());
			throw new IOException(iae.getMessage());
		} catch (ClassCastException cce) {
			throw new IOException(cce.getMessage());
		} catch (ClassNotFoundException cnfe) {
			throw new IOException(cnfe.getMessage());
		} catch (InstantiationException ie) {
			throw new IOException(ie.getMessage());
		} catch (DOMException de) {
			throw new IOException(de.getMessage());
		} catch (LSException lse) {
			throw new IOException(lse.getMessage());
		}
		return wlDescriptors;
	}
	
	
	/**
	 * Parses relevant web service from the specified document, stores them in the local list and 
	 * returns them.
	 * 
	 * @param weblichtDoc the loaded (OAI-PMH) xml Document
	 * @return a list of service descriptors
	 * 
	 * @throws IOException as a wrapper round any possible exception
	 */
	private List<WLServiceDescriptor> parseRelevantServices(Document weblichtDoc) {
		if (weblichtDoc == null) {
			return null;
		}
		
		List<WLServiceDescriptor> wlDescriptors = new ArrayList<WLServiceDescriptor>();
		WLServiceDescriptor curWLDescriptor;
		
	    Element docElem = weblichtDoc.getDocumentElement();

	    NodeList serviceList = docElem.getElementsByTagNameNS(NS_WC, "Service");
	    LOG.info("Number of WebLicht web services: " + serviceList.getLength());
	    for (int i = 0; i < serviceList.getLength(); i++) {
	    	curWLDescriptor = null;
	    	Element serviceEl = (Element) serviceList.item(i);
	    	String serviceName = null;
	    	NodeList serviceNameList = serviceEl.getElementsByTagNameNS(NS_WC, "Name");
	    	if (serviceNameList.getLength() > 0) {
	    		serviceName = serviceNameList.item(0).getTextContent();
	    	}
	    	if (serviceName == null || serviceName.isEmpty()) {
	    		// log
	    		continue;
	    	}		    	
	    	
	    	NodeList inputElems = serviceEl.getElementsByTagNameNS(NS_WC, "Input");
	    	if (inputElems.getLength() == 0) {
	    		continue;
	    	}
	    	// get the first one
	    	Element inputEl = (Element) inputElems.item(0);
	    	NodeList inParams = inputEl.getElementsByTagNameNS(NS_WC, "Parameter");
	    	curWLDescriptor = checkInputParameters(serviceName, inParams);
	    	
    		if (curWLDescriptor == null) {
    			// no suitable input found, go to next service
    			continue;
    		}
    		// check output parameters
	    	NodeList outputElems = serviceEl.getElementsByTagNameNS(NS_WC, "Output");
	    	if (outputElems.getLength() == 0) {
	    		continue;
	    	}
	    	// get the first one
	    	Element outputEl = (Element) outputElems.item(0);
	    	NodeList outParams = outputEl.getElementsByTagNameNS(NS_WC, "Parameter");
	    	
    		boolean validOutput = checkOutputParameters(outParams, curWLDescriptor);
    		
    		if (!validOutput) {
    			// no suitable output found, go to next service
    			continue;
    		}
    		// encountered a usable service descriptor, retrieve description and url
    		NodeList descriptionList = serviceEl.getElementsByTagNameNS(NS_WC, "Description");
    		if (descriptionList.getLength() > 0) {
    			// take the first
    			Node descEl = descriptionList.item(0);
    			curWLDescriptor.description = descEl.getTextContent().replaceAll("\n", " ");
    		}
    		// creator
    		NodeList orgList = serviceEl.getElementsByTagNameNS(NS_WC, "Organisation");
    		if (orgList.getLength() > 0) {
    			// take the first
    			curWLDescriptor.creator = orgList.item(0).getTextContent(); 
    		}
    		// the url, critical
    		NodeList urlList = serviceEl.getElementsByTagNameNS(NS_WC, "url");
    		if (urlList.getLength() > 0) {
    			curWLDescriptor.fullURL = urlList.item(0).getTextContent();
    			wlDescriptors.add(curWLDescriptor);
    		}// otherwise it will not be in the list
	    }
	    
		return wlDescriptors;
	}
	
	/**
	 * Checks the input parameter "type" and creates a descriptor object if a suitable type
	 * is found.
	 * 
	 * @param serviceName the name of the service
	 * @param inputParams the input parameter elements
	 * 
	 * @return null if no suitable input type declared, a descriptor instance otherwise
	 */
	private WLServiceDescriptor checkInputParameters( String serviceName, NodeList inputParams) {
		if (inputParams == null || inputParams.getLength() == 0) {
			return null;
		}
		WLServiceDescriptor curWLDescriptor = new WLServiceDescriptor(serviceName);
		try {
	    	// find the "type" input parameter and check if it is text/plain or text/tcf+xml
	    	for (int j = 0; j < inputParams.getLength(); j++) {
	    		Element param = (Element) inputParams.item(j);// an input parameter
	    		// or loop over child nodes
	    		NodeList nameList = param.getElementsByTagNameNS(NS_WC, "Name");
	    		if (nameList.getLength() > 0) {
	    			Element nameEl = (Element) nameList.item(0);
	    			String name = nameEl.getTextContent();
	    			if (name != null) {
	    				if (name.equals("sentences")) {
	    					curWLDescriptor.sentenceInput = true;
	    				} else if (name.equals("tokens")) {
	    					curWLDescriptor.tokensInput = true;
	    				} 
	    				//else if (name.equals("lemmas")) {
	    				//	curWLDescriptor.lemmaSupport = true;
	    				//} 
	    				else if (name.equals("type")) {
	    		    		// we're in the "type" Parameter 
	    		    		NodeList valList = param.getElementsByTagNameNS(NS_WC, "Value");
	    		    		for (int k = 0; k < valList.getLength(); k++) {
	    		    			Element valEl = (Element) valList.item(k);
	    		    			String value = valEl.getTextContent();
	    		    			if ("text/tcf+xml".equals(value)) {
	    		    				curWLDescriptor.tcfInput = true;
	    		    			} else if ("text/plain".equals(value)) {
	    		    				curWLDescriptor.plainTextInput = true;
	    		    			}
	    		    		}
		    			}
	    			}
	    		}
	    	}
		} catch (DOMException dex) {
			// print message?
		}
		
		if (curWLDescriptor.plainTextInput || curWLDescriptor.tcfInput) {
			return curWLDescriptor;
		}
		
		return null;
	}
	
	/**
	 * Checks the declared output parameters and updates the descriptor.
	 * 
	 * @param outputParams the output parameters of the service
	 * @param curWLDescriptor the current descriptor to be updated
	 * 
	 * @return true if tcf+xml is declared as output, or if sentences, tokens, lemmas or postags are part 
	 * of the output
	 */
	private boolean checkOutputParameters(NodeList outputParams, WLServiceDescriptor curWLDescriptor) {
		if (outputParams == null || curWLDescriptor == null || outputParams.getLength() == 0) {
			return false;
		}		

		try {
	    	// find the "type" output parameter and check if it is text/tcf+xml
	    	for (int j = 0; j < outputParams.getLength(); j++) {
	    		Element param = (Element) outputParams.item(j);// an input parameter
	    		// or loop over child nodes
	    		// TODO if output is only sentences and tokens it is probably a tokenizer
	    		NodeList nameList = param.getElementsByTagNameNS(NS_WC, "Name");
	    		if (nameList.getLength() > 0) {
	    			Element nameEl = (Element) nameList.item(0);
	    			String name = nameEl.getTextContent();
	    			if (name != null) {
	    				if (name.equals("sentences")) {
	    					curWLDescriptor.sentenceOutput = true;
	    				} else if (name.equals("tokens")) {
	    					curWLDescriptor.tokensOutput = true;
	    				} else if (name.equals("lemmas")) {
	    					curWLDescriptor.lemmaSupport = true;
	    				} else if (name.indexOf("postags") > -1) {
	    					curWLDescriptor.posTagSupport = true;
	    				} else
		    			if (name.equals("type")) {
		    	    		// we're in the "type" Parameter 
		    	    		NodeList valList = param.getElementsByTagNameNS(NS_WC, "Value");
		    	    		for (int k = 0; k < valList.getLength(); k++) {
		    	    			Element valEl = (Element) valList.item(k);
		    	    			String value = valEl.getTextContent();
		    	    			if ("text/tcf+xml".equals(value)) {
		    	    				curWLDescriptor.tcfOutput = true;
		    	    			}
		    	    		}
		    			}
	    			}
	    		}
	    	}
	    	
		} catch (DOMException dex) {
			// print message?
		}
		
		return curWLDescriptor.tcfOutput || curWLDescriptor.lemmaSupport || curWLDescriptor.sentenceOutput 
				|| curWLDescriptor.tokensOutput || curWLDescriptor.posTagSupport;
	}
	
	/**
	 * Returns the last modified property of the file containing the cached version of the harvested
	 * services.
	 * 
	 * @param cachePath the path to the cache file
	 * @return a last modified time or 0L in case there is no cache file yet (or anymore) 
	 */
	public long getLastCachingTime(String cachePath) {
		if (cachePath == null) {
			return 0L;
		}
		
		try {
			File cf = new File(cachePath);
			if (cf.exists() && cf.canRead() && !cf.isDirectory()) {
				return cf.lastModified();
			}
		} catch (Throwable t) {
			// log error?
		}
		
		return 0L;
	}
	
	
	public  List<WLServiceDescriptor> loadRelevantServicesFromCache(String cachePath) throws IOException {
		if (cachePath == null) {
			return null;
		}
		
		List<WLServiceDescriptor> wlDescriptors = null;
		
		InputStreamReader reader = null;
		try {
		    DOMImplementationLS domImplementation = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
		    LSInput lsInput = domImplementation.createLSInput();
		    lsInput.setEncoding("UTF-8");
			lsInput.setCharacterStream(reader = new InputStreamReader(new FileInputStream(cachePath)));
		    
		    LSParser parser = domImplementation.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
		    Document wlDoc = parser.parse(lsInput);
		    wlDescriptors = parseRelevantServices(wlDoc);
		} catch (IllegalAccessException iae) {
			//System.out.println("Illegal Access: " + iae.getMessage());
			throw new IOException(iae.getMessage());
		} catch (ClassCastException cce) {
			throw new IOException(cce.getMessage());
		} catch (ClassNotFoundException cnfe) {
			throw new IOException(cnfe.getMessage());
		} catch (InstantiationException ie) {
			throw new IOException(ie.getMessage());
		} catch (DOMException de) {
			throw new IOException(de.getMessage());
		} catch (LSException lse) {
			throw new IOException(lse.getMessage());
        } finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
		
		return wlDescriptors;
	}
	
	/**
	 * Stores the xml content in the specified file location. 
	 * 
	 * @param cachePath the absolute file path
	 * @param xmlString the OAI XML as returned by the harvester
	 * 
	 * @throws IOException any IO related error
	 */
	public void storeCachedVersion(String cachePath, String xmlString) throws IOException {
		BufferedWriter bufWriter = null;
		
		try {
//			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
//			DOMImplementation domImpl = registry.getDOMImplementation("XML LS");
//			if (domImpl instanceof DOMImplementationLS) {
//				DOMImplementationLS domLS = (DOMImplementationLS) domImpl;
//				LSSerializer serializer = domLS.createLSSerializer();
//				LSOutput lsOut = domLS.createLSOutput();
//				lsOut.setEncoding("UTF-8");
//				StringWriter writer = new StringWriter();
//				lsOut.setCharacterStream(writer);
//				//serializer.write(doc.getDocumentElement(), lsOut);
//				//String result = serializer.writeToString(doc.getDocumentElement());// on Mac this defaults to writing UTF-16
//				//String result = writer.toString();
//				//System.out.println(result);
//				//return result;
//				//return writer.toString();
//			} 
			
			File outFile = new File(cachePath);
			bufWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
			bufWriter.write(xmlString);
			bufWriter.flush();
		} catch (Throwable t) {
			LOG.warning("Cannot write WebLicht cache file:" + t.getMessage());
			throw new IOException("Cannot write WebLicht cache file:" + t.getMessage());
		} finally {
			if (bufWriter != null) {
				try {
					bufWriter.close();
				} catch (Throwable th){}
			}
		}
	}
	
	
}

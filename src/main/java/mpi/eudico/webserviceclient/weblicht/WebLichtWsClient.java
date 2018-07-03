package mpi.eudico.webserviceclient.weblicht;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mpi.eudico.server.corpora.util.ServerLogger;
import mpi.eudico.webserviceclient.WsClientRest;

/**
 * A class to access the WebLicht services.
 *  
 * @author Han Sloetjes
 */
public class WebLichtWsClient {
	public static String baseUrl = "http://weblicht.sfs.uni-tuebingen.de/rws/";
	// hard coded url for converting plain text to tcf. There is an alternative one from BBAW
	private String convertUrl = "http://weblicht.sfs.uni-tuebingen.de/rws/service-converter/convert/qp";
	
	private WsClientRest wsClient;

	/**
	 * No arg constructor
	 */
	public WebLichtWsClient() {
		super();
		wsClient = new WsClientRest();
	}
	
	/**
	 * Converts plain text to TCF.
	 * 
	 * @param text the plain text
	 * @return the TCF as string
	 */
	public String convertPlainText(String text) throws IOException {
		if (text == null || text.length() == 0) {
			return text;
		}
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("informat", "plaintext");
		params.put("outformat", "tcf04");
		params.put("language", "unknown");
		Map<String, String> properties = new HashMap<String, String>();
		// any Accept header tried so far return a 406, Not Acceptable
		//properties.put("Accept", "text/plain;charset=utf-8, text/tcf+xml;version=0.4");
		properties.put("User-Agent", "ELAN");
		properties.put("Connection", "Keep-Alive");
		//properties.put("Content-Type", "text/tcf+xml");
		properties.put("Content-Type", "text/tcf+xml;charset=utf-8");
		
		try {
			String result = wsClient.callServicePostMethodWithString(convertUrl, params, properties, 
				text, null, null, 0, 0);
			//System.out.println(result);
			return result;
		} catch (IOException ioe) {
			// log 
			ServerLogger.LOG.warning("Call failed: " + ioe.getMessage());
			throw ioe;
		} catch (Throwable t) {
			// log 
			ServerLogger.LOG.warning("Call failed: " + t.getMessage());
			throw new IOException(t.getMessage());
		}

	}
	
	/**
	 * Calls WebLicht components that take TCF as input and returns TCF. 
	 * 
	 * @param toolUrl the tool specific web service url
	 * @param tcfString the input tcf string
	 * 
	 * @return the returned content
	 */
	public String callWithTCF(String toolUrl, String tcfString) throws IOException {
		if (toolUrl == null) {
			ServerLogger.LOG.warning("No web service url specified.");
			throw new NullPointerException("No web service url specified.");
		}
		
		if (tcfString == null) {
			ServerLogger.LOG.warning("No input specified.");
			throw new NullPointerException("No input TCF specified.");
		}
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("Accept", "text/tcf+xml");
		properties.put("User-Agent", "ELAN");
		properties.put("Connection", "Keep-Alive");
		properties.put("Content-Type", "text/tcf+xml;charset=utf-8");
		
		try {
			String result = wsClient.callServicePostMethodWithString(toolUrl, 
					null, properties, 
				tcfString, null, null, 0, 0);
			//System.out.println(result);
			return result;
		} catch (IOException ioe) {
			// log 
			ServerLogger.LOG.warning("Call failed: " + ioe.getMessage());
			throw ioe;
		} catch (Throwable t) {
			ServerLogger.LOG.warning("Call failed: " + t.getMessage());
			throw new IOException(t.getMessage());
		}
	}
	
}

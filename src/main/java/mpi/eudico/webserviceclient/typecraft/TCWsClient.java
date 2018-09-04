package mpi.eudico.webserviceclient.typecraft;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.util.ServerLogger;
import mpi.eudico.webserviceclient.WsClientRest;

public class TCWsClient {
	public static String storedSessionId = null;
	// could make this static?
	private String sessionId = null;
	private String userName;
	private WsClientRest wsClient;
	private String loginUrl = "http://typecraft.org/w/api.php";
	private String downloadUrl = "http://typecraft.org/tc2/ELAN";
	
	
	public TCWsClient() {
		wsClient = new WsClientRest();
	}
	
	public TCWsClient(String name, String pwd) {
		wsClient = new WsClientRest();
		sessionId = login(name, pwd);
		System.out.println(sessionId);
		if (sessionId != null) {
			listTexts();
			String xmlText = downloadText("1891");
			if (xmlText != null) {
				TCParser parser = new TCParser(xmlText);
				List<PhraseRecord> records = parser.getPhraseRecords();
				System.out.println("Number of records: " + records.size());
			}
		}
	}
	
	public static void main(String[] args) {
		if (args.length >= 2) {
			TCWsClient tcws = new TCWsClient(args[0], args[1]);
		} else {
			TCWsClient tcws = new TCWsClient();
		}
		
	}
	
	
	public String login(String username, String pwd) {
//		if (sessionId != null) {
//			return sessionId;//?? already logged in
//		}
		userName = username;
		Map<String, String> params = new HashMap<String, String>(4);
		params.put("action", "login");
		try {
			params.put("lgname", URLEncoder.encode(username, "UTF-8"));
			params.put("lgpassword", URLEncoder.encode(pwd, "UTF-8"));
		} catch (UnsupportedEncodingException uee){
			System.out.println("Cannot encode user name: " + uee.getMessage());
		}
		
		params.put("format", "xml");
		
		try {
			String result = wsClient.callServicePostMethod(loginUrl, params, null, null, 
				null, 0, 0);
			//System.out.println("Result: " + result);
			return parseLoginMessage(result);
		} catch (IOException ioe) {
			System.out.println("Cannot connect to server: " + ioe.getMessage());
		}
		return null;
	}
	
	/**
	 * Contacts the web service with the specified sessionid and downloads a list 
	 * of texts for the user.
	 * @param sessionId the session
	 * @return a list of Text objects
	 */
	public List<TCTextId> listTexts(String sessionId) {
		if (sessionId == null) {
			return null;
		}
		
		Map<String, String> params = new HashMap<String, String>(4);
		params.put("sessionid", sessionId);
		params.put("command", "listtexts");
		
		try {
			String result = wsClient.callServicePostMethod(downloadUrl, params, null, null, 
				null, 0, 0);
			//System.out.println("Texts: " + result);
			if (result != null) {
				return parseTextList(result);
			}

			return null;
		} catch (IOException ioe) {
			System.out.println("Cannot list the texts of user: " + ioe.getMessage());
		}
		
		return null;
	}
	
	/**
	 * Downloads the text with the specified id. Returns the unparsed xml.
	 * @param textId the id of the text
	 * @param sessionId the session of the user
	 * @return the xml
	 */
	public String downloadText(String textId, String sessionId) {
		if (textId == null || sessionId == null) {
			return null;
		}
		
		Map<String, String> params = new HashMap<String, String>(4);
		params.put("sessionid", sessionId);
		params.put("command", "export");
		params.put("text", textId);
		
		try {
			String result = wsClient.callServicePostMethod(downloadUrl, params, null, null, 
				null, 0, 0);
			//System.out.println("Text " + textId + ": " + result);
			return result;
		} catch (IOException ioe) {
			ServerLogger.LOG.warning("Cannot export the text: " + ioe.getMessage());
		}
		
		return null;
	}
	/**
	 * Extracts the name from the xml message return from the server.
	 * 
	 * @param result the xml result
	 * @return sessionId
	 */
	private String parseLoginMessage(String result) {
		if (result == null) {
			return null;
		}
		
		if (result.startsWith("<?xml")) {
			int index = result.indexOf("sessionid");
			if (index > -1) {
				int index2 = result.indexOf('\"', index);
				if (index2 > -1 && index2 < result.length() - 1) {
					int index3 = result.indexOf('\"', index2 + 1);
					if (index3 > -1 && index3 > index2 + 1) {
						return result.substring(index2 + 1, index3);
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Preliminary implementation of extracting "texts" information from returned "listtexts" results.
	 * 
	 * @param textListXml the returned result
	 * @return a list of "Text" objects
	 */
	private List<TCTextId> parseTextList(String textListXml) {
		if (textListXml == null) {
			return null;
		}
		List<TCTextId> texts = new ArrayList<TCTextId>();
		
		if (textListXml.startsWith("<?xml")) {
			int lastIndex = 0;
			int index = -1;
			do {
				index = textListXml.indexOf("<text", lastIndex);
				if (index == -1) {// no more text elements
					break;
				}
				String textId = null;
				int idIndex = textListXml.indexOf("id", index);
				if (idIndex > -1) {
					int quot1 = textListXml.indexOf('\"', idIndex);
					if (quot1 > -1 && quot1 < textListXml.length() - 1) {
						int quot2 = textListXml.indexOf('\"', quot1 + 1);
						if (quot2 > -1)  {
							textId = textListXml.substring(quot1 + 1, quot2);
						}
					}
				} else {
					ServerLogger.LOG.warning("No id attribute for text.");
					break;//??
				}			
				
				int title1 = textListXml.indexOf("<title>", index);
				if (title1 > -1) {
					int title2 = textListXml.indexOf("</title>", title1);
					lastIndex = title2;
					if (lastIndex == -1) {
						ServerLogger.LOG.info("No end tag found for a <title> element");
						break;
					}
					String title = textListXml.substring(title1 + 7, title2);
					if (textId != null && title != null) {
						TCTextId tid = new TCTextId();
						tid.id = textId;
						tid.title = title;
						texts.add(tid);	
					}
					
				} else {
					ServerLogger.LOG.info("No <title> element found inside the <text> element");
					break;
				}
				
			} while (index > -1);
			
		}
		return texts;
	}
	
	private String listTexts() {
		if (sessionId == null) {
			System.out.println("Not logged in.");
			return null;
		}
		
		Map<String, String> params = new HashMap<String, String>(4);
		params.put("sessionid", sessionId);
		params.put("command", "listtexts");
		
		try {
			String result = wsClient.callServicePostMethod(downloadUrl, params, null, null, 
				null, 0, 0);
			System.out.println("Texts: " + result);
			return result;
		} catch (IOException ioe) {
			System.out.println("Cannot list the texts of user: " + ioe.getMessage());
		}
		return null;
	}
	
	private String downloadText(String textId) {
		if (sessionId == null) {
			System.out.println("Not logged in.");
			return null;
		}
		
		Map<String, String> params = new HashMap<String, String>(4);
		params.put("sessionid", sessionId);
		params.put("command", "export");
		params.put("text", textId);
		
		try {
			String result = wsClient.callServicePostMethod(downloadUrl, params, null, null, 
				null, 0, 0);
			System.out.println("Text " + textId + ": " + result);
			return result;
		} catch (IOException ioe) {
			System.out.println("Cannot export the text: " + ioe.getMessage());
		}
		return null;
	}
	
}

package mpi.eudico.client.annotator.recognizer.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.recognizer.data.FileParam;
import mpi.eudico.client.annotator.recognizer.data.NumParam;
import mpi.eudico.client.annotator.recognizer.data.Param;
import mpi.eudico.client.annotator.recognizer.data.TextParam;
import mpi.eudico.client.annotator.recognizer.gui.TierSelectionPanel;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.util.XMLEscape;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A class for reading and writing of (AVATecH project specific) parameter in XML format.
 *  
 * @author Han Sloetjes
 * @updated Sep 2012, Aarthy Somasundaram
 *
 */
public class ParamIO {
	
	private final String TIER_NAME ="TierName:";	
	private final String SELECTIONS ="Selections";

	public ParamIO() {
	}

	/**
	 * Reads parameters from the specified file.
	 * 
	 * @param f the parameter file
	 * @return a Map of parameter name-value pairs or null if no parameters were found
	 * 
	 * @throws IOException if reading or parsing fails
	 */
	public Map<String, Object> read(File f) throws IOException {
		if (f == null) {
			new IOException("Cannot read from file: file is null");
		}
		
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader(
	    		"org.apache.xerces.parsers.SAXParser");
			ParamHandler ph = new ParamHandler(); 
			reader.setContentHandler(ph);

			reader.parse(f.getAbsolutePath());
			
			return ph.getParamMap();
		} catch (SAXException sax) {
			ClientLogger.LOG.warning("Parsing failed: " + sax.getMessage());
			throw new IOException(sax.getMessage());
		}
	}
	
	/**
	 * Writes the parameters from the specified list to the specified XML file.
	 * 
	 * @param recognizerID the name or identifier of the recognizer
	 * @param paramList a list of parameters
	 * @param f the destination file
	 * 
	 * @throws IOException if writing fails 
	 */
	public void write(String recognizerID, List<Param> paramList, File f) throws IOException {
		if (f == null) {
			new IOException("Cannot write to file: file is null");
		}
		XMLEscape xmlEscape = new XMLEscape();
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(f), "UTF-8")));// utf-8 is always supported, I guess
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<PARAM xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"file:avatech-call.xsd\">");
		// HS July 2014 write a InvocationContext param 
		// accept the formatted (Java 1.6) string as is: 2014-07-31 14:23:09+0200
		String timestamp = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ssZ").format(new Date());
		writer.print("<param name=\"InvocationContext\">");
		writer.print( (recognizerID != null ? xmlEscape.escape(recognizerID) : "Unknown") );
		writer.print(" " + timestamp);
		writer.println("</param>");
		
		if (paramList != null && paramList.size() > 0) {

			for (Param p : paramList) {
				writer.print("<param name=\"" + p.id + "\">");
				if (p instanceof NumParam) {
					writer.print(((NumParam) p).current);
				} else if (p instanceof TextParam) {
					if (((TextParam) p).curValue != null) {
						String value = ((TextParam) p).curValue;
						if (value != null && value.length() > 0) {
							writer.print(xmlEscape.escape(value));
						}
					}
				} else if (p instanceof FileParam) {
					String path = ((FileParam) p).filePath;
					if (path != null && path.length() > 0) {
						// in case of file protocol strip the protocol part
						if (path.startsWith("file:")) {
							path = path.substring(5);
						}
						writer.print(xmlEscape.escape(path));
					}
				}					
				writer.println("</param>");
			}
		}
		
		writer.println("</PARAM>");
		
		writer.close();
	}
	
//	/**
//	 * Writes the parameters from the specified map to the specified XML file.
//	 * 
//	 * @param paramMap a mapping of parameter key-value pairs
//	 * @param f the destination file
//	 * 
//	 * @throws IOException if writing fails 
//	 */
//	public void write(Map<String, Object> paramMap, File f) throws IOException {
//		if (f == null) {
//			new IOException("Cannot write to file: file is null");
//		}
//		
//		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
//				new FileOutputStream(f), "UTF-8")));// utf-8 is always supported, I guess
//		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//		writer.println("<PARAM xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"file:avatech-call.xsd\">");
//		
//		if (paramMap != null && paramMap.size() > 0) {
//			String key = null;
//			Object val = null;
//			
//			Iterator<String> parIt = paramMap.keySet().iterator();
//			while (parIt.hasNext()) {
//				key = parIt.next();
//				val = paramMap.get(key);
//				
//				writer.print("<param name=\"" + key + "\">");
//				if (val instanceof String) {
//					writer.print((String) val);
//				} else if (val instanceof Float) {
//					writer.print((Float) val);
//				} else if (val instanceof Double) {
//					writer.print((Double) val);
//				} 
//				
//				writer.println("</param>");
//			}
//		}
//		
//		writer.println("</PARAM>");
//		
//		writer.close();
//	}
	
	/**
	 * Writes the parameters from the specified map to the specified 
	 * XML file.
	 * 
	 * Used for saving and loading the preferences file.
	 * 
	 * @param recognizerID the name or identifier of the recognizer
	 * @param paramMap a mapping of parameter key-value pairs
	 * @param f the destination file
	 * 
	 * @throws IOException if writing fails 
	 */
	public void writeParamFile(String recognizerID, Map<String, Object> paramMap, File f) throws IOException {
		if (f == null) {
			new IOException("Cannot write to file: file is null");
		}
		XMLEscape xmlEscape = new XMLEscape();
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(f), "UTF-8")));// utf-8 is always supported, I guess
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");		
		writer.println("<PARAM xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"file:avatech-call.xsd\">");
		// HS July 2014 write a InvocationContext param 
		// accept the formatted (Java 1.6) string as is: 2014-07-31 14:23:09+0200
		String timestamp = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ssZ").format(new Date());
		writer.print("<param name=\"InvocationContext\">");
		writer.print( (recognizerID != null ? xmlEscape.escape(recognizerID) : "Unknown") );
		writer.print(" " + timestamp);
		writer.println("</param>");
		
		if (paramMap != null && paramMap.size() > 0) {
			String key = null;
			Object val = null;
			
			Iterator<String> parIt = paramMap.keySet().iterator();
			while (parIt.hasNext()) {
				key = parIt.next();
				val = paramMap.get(key);				
				
				writer.print("<param name=\"" + key + "\">");
				if (val instanceof String) {
					writer.print(xmlEscape.escape((String) val));
				} else if (val instanceof Float) {
					writer.print((Float) val);
				} else if (val instanceof Double) {
					writer.print((Double) val);
				} else if (val instanceof HashMap){		
					Iterator keyIt = ((HashMap) val).keySet().iterator();	          
				    Object obj;
				    String k;
				    while (keyIt.hasNext()) {
				    	k = (String) keyIt.next();
				    	obj = ((HashMap) val).get(k);
				       	if(obj instanceof String){
				       		 if(k.equals(TierSelectionPanel.TIER_NAME)){
				       			writer.print(TIER_NAME + xmlEscape.escape(obj.toString()));
				       		 }else{
				       			writer.print(xmlEscape.escape(obj.toString()));
				       		 }		       		 
				       	 } 
				       	//do not write selections to the param file
				       	else {
				       		writer.print(SELECTIONS);
				       	}
//				       	else if (obj instanceof HashMap){
//				       		Iterator selIt = ((HashMap) obj).keySet().iterator();	
//				       		Object selObj;
//				       		while (selIt.hasNext()) {
//				       			selObj = ((HashMap) obj).get((String) selIt.next());
//				       			if(selObj instanceof List && ((List)selObj).size() == 2){
//				       				writer.print("<selection begin-time =\""+ ((List)selObj).get(0)+ 
//						       				 "\" end-time =\""+ ((List)selObj).get(1)+"\" />");
//				       			}
//				       		}
//				       	 }
					} 
				}
				writer.println("</param>");
			}
		}
		
		writer.println("</PARAM>");
		
		writer.close();
		
	}
	
	// ###############  Parser content handler  #############################################
	class ParamHandler implements ContentHandler {
		private String curContent = "";
		private String curName = null;
		
		private HashMap<String, Object> curMap = null;
		//private HashMap<String, Object> selMap = null;
		//private List<Long> selList = null;
		
		private final String PARAM = "param";
		private final String NAME = "name";		
		private Map<String, Object> params;
		
		/**
		 * Constructor.
		 */
		public ParamHandler() {
			super();
			params= new HashMap<String, Object>();
		}

		/**
		 * Returns the map of parameters.
		 * 
		 * @return the map of parameters
		 */
		public Map<String, Object> getParamMap() {
			return params;
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			curContent += new String(ch, start, length);			
		}

		@Override
		public void startElement(String nameSpaceURI, String name,
	            String rawName, Attributes attributes) throws SAXException {
			if (name.equals(PARAM)) {
				curName = attributes.getValue(NAME);
			} 
//			else if (name.equals("tier")){
//				curMap = new HashMap<String, Object>();
//				curMap.put(TierSelectionPanel.TIER_NAME, attributes.getValue(NAME));
//			} else if(name.equals("file")){
//				curMap = new HashMap<String, Object>();
//				curMap.put(TierSelectionPanel.FILE_NAME, attributes.getValue(NAME));
//			} else if(name.equals("selection")){
//				if(curMap == null){
//					curMap = new HashMap<String, Object>();
//					selMap = new HashMap<String, Object>();
//					curMap.put(TierSelectionPanel.SELECTIONS, selMap);
//				}					
//				selList = new ArrayList<Long>();
//				selList.add(Long.valueOf(attributes.getValue("begin-time")));
//				selList.add(Long.valueOf(attributes.getValue("end-time")));				
//				selMap.put(Integer.toString(selMap.size()+1), selList);
//			}
		}

		@Override
		public void endElement(String nameSpaceURI, String name, String rawName)
				throws SAXException {
//			if (name.equals(PARAM)) {
//				if (curName != null) {
//					if(curMap != null ){
//						params.put(curName, curMap);
//					} else{
//						curContent = curContent.trim();						
//						params.put(curName, curContent);
//					}
//				}
//				curContent = "";
//				curMap = null;
//			} 			
			if (name.equals(PARAM)) {
				curContent = curContent.trim();		
				if(curContent.startsWith(TIER_NAME)){
					curMap = new HashMap<String, Object>();					
					curMap.put(TierSelectionPanel.TIER_NAME, curContent.substring(TIER_NAME.length()));
				} else if(curContent.startsWith(SELECTIONS)){
					curMap = new HashMap<String, Object>();
					curMap.put(TierSelectionPanel.SELECTIONS,null);
				}
				
				if(curMap != null ){
					params.put(curName, curMap);
				} else{
					curContent = curContent.trim();						
					params.put(curName, curContent);
				}				
				curContent = "";
				curMap = null;
			}
			
		}

		@Override
		public void endDocument() throws SAXException {			
		}
		
		@Override
		public void endPrefixMapping(String arg0) throws SAXException {
		}

		@Override
		public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
				throws SAXException {	
		}

		@Override
		public void processingInstruction(String arg0, String arg1)
				throws SAXException {
		}

		@Override
		public void setDocumentLocator(Locator arg0) {
		}

		@Override
		public void skippedEntity(String arg0) throws SAXException {
		}

		@Override
		public void startDocument() throws SAXException {
		}

		@Override
		public void startPrefixMapping(String arg0, String arg1)
				throws SAXException {
		}
		
	}
}

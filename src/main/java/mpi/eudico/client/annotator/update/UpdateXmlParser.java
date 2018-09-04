package mpi.eudico.client.annotator.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;

/**
 * A parser which parses the update elan xml file
 * 
 */
public class UpdateXmlParser {

	/** the sax parser */
    //private final SAXParser saxParser;
    private XMLReader reader;
    
    /** the major version value */
    public String major;

    /** the minor version value */
    public String minor;

    /** the micro (bug fix) version value */
    public String micro;
    
    private String fileName;
    
    private String summary;

	private InputSource source;

	private String websiteURL;
    
    /**
     * Creates a new EAFSkeletonParser instance
     *
     * @param fileName the file to be parsed
     *
     * @throws ParseException any exception that can occur when creating 
     * a parser    
     */
    public UpdateXmlParser(String fileName) throws ParseException {
    	this(fileName, false);
    }
    
    /**
     * Creates a new EAFSkeletonParser instance
     *
     * @param source the InputSource to be parsed
     *
     * @throws ParseException any exception that can occur when creating 
     * a parser    
     */
    public UpdateXmlParser(InputSource source) throws ParseException {
    	this(source, false);
    }
    
    /**
     * Creates a new EAFSkeletonParser instance
     *
     * @param source the InputSource to be parsed
     *
     * @throws ParseException any exception that can occur when creating 
     * a parser
     * @throws NullPointerException thrown when the filename is null
     */
    public UpdateXmlParser(InputSource source, boolean strict) throws ParseException {
        if (source == null) {
            throw new NullPointerException();
        }

        this.source = source;
        initParser(strict);    
    }
    
    /**
     * Creates a new EAFSkeletonParser instance
     *
     * @param fileName the file to be parsed
     *
     * @throws ParseException any exception that can occur when creating 
     * a parser
     * @throws NullPointerException thrown when the filename is null
     */
    public UpdateXmlParser(String fileName, boolean strict) throws ParseException {
        if (fileName == null) {
            throw new NullPointerException();
        }
        this.fileName = fileName;
        initParser(strict);    	
    }
    
    /**
     * Initializes the parser
     */
    private void initParser(boolean strict) throws ParseException{
    	try {
	        reader = XMLReaderFactory.createXMLReader(
	        	"org.apache.xerces.parsers.SAXParser");
	        reader.setFeature("http://xml.org/sax/features/namespaces", true);
	        reader.setFeature("http://xml.org/sax/features/validation", true);
	        reader.setFeature("http://apache.org/xml/features/validation/schema", true);
	        reader.setFeature("http://apache.org/xml/features/validation/dynamic", true);
	        reader.setContentHandler(new UpdateSkeletonHandler());
	        if (strict) {
	        //	reader.setErrorHandler(new EAFErrorHandler());
	        }
    	} catch (SAXException se) {
    		se.printStackTrace();
    		throw new ParseException(se.getMessage());
    	} 
//    	catch (IOException ioe) {
//    		ioe.printStackTrace();
//    		throw new ParseException(ioe.getMessage());
//    	}
    }
    
    /**
     * Starts the actual parsing.
     *
     * @throws ParseException any parse exception
     */
    public void parse() throws ParseException {
        // init maps and lists
        try {           
        	if(fileName != null){
        		reader.parse(fileName);
        	} else if(source != null){
        		reader.parse(source);
        	}
        } catch (SAXException sax) {
            System.out.println("Parsing error: " + sax.getMessage());
             // the SAX parser can have difficulties with certain characters in 
            // the filepath: try to create an InputSource for the parser
            // HS Mar 2007: depending on Xerces version a SAXException or an IOException 
            // is thrown in such case
            if (fileName != null) {
            	retryParsing();
            } else {
            	throw new ParseException(sax.getMessage(), sax.getCause());
            }
        } catch (IOException ioe) {
            System.out.println("IO error: " + ioe.getMessage());

            // the SAX parser can have difficulties with certain characters in 
            // the filepath: try to create an InputSource for the parser
            // HS Mar 2007: depending on Xerces version a SAXException or an IOException 
            // is thrown in such case
            if (fileName != null) {
            	retryParsing();
            } else {
            	throw new ParseException(ioe.getMessage(), ioe.getCause());
            }
        } catch (Exception e) {
        	throw new ParseException(e.getMessage(), e.getCause());
        } catch (Throwable t) {
        	throw new ParseException(t.getMessage(), t.getCause());
        }
    }
    
    /**
     * Retry parsing
     */
    private void retryParsing() throws ParseException{
   	 	File f = new File(fileName);
        if (f.exists()) {
        	FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);
                InputSource source = new InputSource(fis);
                //saxParser.parse(source);
                reader.parse(source);
                // just catch any exception
            } catch (Exception ee) {
                System.out.println("Parsing retry error: " +
                    ee.getMessage());
                throw new ParseException(ee.getMessage(), ee.getCause());
            } finally {
    			try {
    				if (fis != null) {
    					fis.close();
    				}
    			} catch (IOException e) {
    			}
            }
        }
   }
    
    /**
     * Returns the major version number of the new update
     * 
     * @return major version number
     * 
     */
    public int getMajorVersion(){
    	int n = -1;
    	if(major != null){
    		try {
    			n = Integer.parseInt(major);
    		} catch (NumberFormatException e) {
    			n = -1;
    		}
    	}    	
    	return n;
    }
    
    /**
     * Returns the minor version number of the new update
     * 
     * @return minor version number
     * 
     */
    public int getMinorVersion(){
    	int n = -1;
    	if(minor != null){
    		try {
    			n = Integer.parseInt(minor);
    		} catch (NumberFormatException e) {
    			n = -1;
    		}
    	}  
    	return n;
    }
    
    /**
     * Returns the micro version number of the new update
     * 
     * @return micro version number
     * 
     */
    public int getMicroVersion(){
    	int n = -1;
    	if(micro != null){
    		try {
    			n = Integer.parseInt(micro);
    		} catch (NumberFormatException e) {
    			n = -1;
    		}
    	}  
    	return n;
    }
    
    public String getWebsiteURL() {		
		return websiteURL;
	}
    
    /**
     * Returns the summary of the new update
     * 
     * @return summary
     * 
     */
    public String getSummary(){    
    	return summary;
    }
    
 // Content handler
    class UpdateSkeletonHandler implements ContentHandler {
    	String content;
		
		@Override
		public void setDocumentLocator(Locator locator) {			
		}

		
		@Override
		public void startDocument() throws SAXException {
		}

	
		@Override
		public void endDocument() throws SAXException {			
		}

	
		@Override
		public void startPrefixMapping(String prefix, String uri)
				throws SAXException {
		}

	
		@Override
		public void endPrefixMapping(String prefix) throws SAXException {			
		}

		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {	
			content = "";
		}

		
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {	
			if (localName.equals("major")) {
				 major = content;				 
			} else if(localName.equals("minor")) {
				 minor = content;
			}else if(localName.equals("micro")) {
				 micro = content;
			} else if(localName.equals("summary")){
				summary = content;
			} else if(localName.equals("website")){
				websiteURL = content;
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {			
			 content += new String(ch, start, length);
		}

	
		@Override
		public void ignorableWhitespace(char[] ch, int start, int length)
				throws SAXException {
		}

		
		@Override
		public void processingInstruction(String target, String data)
				throws SAXException {
		}

	
		@Override
		public void skippedEntity(String name) throws SAXException {
			
			
		}
    }
    
//  /**
//  * An error handler for the eaf parser.<br>
//  * The exception thrown (by Xerces 2.6.2) contains apart from file name,
//  * line and column number, only a description of the problem in it's message.
//  * To really deal with a problem a handler would need to parse the message
//  * for certain strings (defined in a Xerces resource .properties file) and/or
//  * read the file to the specified problem line.
//  *
//  * @author Han Sloetjes, MPI
//  */
// class EAFErrorHandler implements ErrorHandler {
//
//		public void error(SAXParseException exception) throws SAXException {
//			System.out.println("Error: " + exception.getMessage());
//			// system id is the file path
//			System.out.println("System id: " + exception.getSystemId());
//			System.out.println("Public id: " + exception.getPublicId());
//			System.out.println("Line: " + exception.getLineNumber());
//			System.out.println("Column: " + exception.getColumnNumber());
//			throw exception;
//		}
//
//		public void fatalError(SAXParseException exception) throws SAXException {
//			System.out.println("FatalError: " + exception.getMessage());
//			throw exception;
//		}
//
//		public void warning(SAXParseException exception) throws SAXException {
//			System.out.println("Warning: " + exception.getMessage());
//		}
//
// }
}





    

   
    

   
   
    
   
    

    




   
   


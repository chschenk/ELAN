/*
 * Created on Mar 30, 2004
 * $Id: IoUtil.java 40495 2014-04-09 11:07:27Z olasei $
 */
package mpi.eudico.util;

// These classes are deprecated:
import org.apache.xml.serialize.ElementState;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
// but the replacement uses this new version of them:
//import com.sun.org.apache.xml.internal.serialize.ElementState;
//import com.sun.org.apache.xml.internal.serialize.OutputFormat;
//import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
// Just puzzling what the best thing to do is...

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;


/**
 * Convenience methods for IO operations.
 *
 * @author Wouter Huijnink
 */
public final class IoUtil {
    /**
     * Open a file using the given encoding.
     *
     * @param encoding example: UTF-8
     * @param filename the name of the file
     *
     * @return handle to the file
     *
     * @throws Exception DOCUMENT ME!
     */
    public final static BufferedReader openEncodedFile(String encoding,
        String filename) throws Exception {
        /*
           A file is opened from the operating system.
           This file is a stream of bytes.
           This could be a UTF-8 encoded unicode stream.
           The decision, if to read UTF-8 is done by the reader:
         */
        File file = new File(filename);

        // Convert the File into an input stream
        InputStream fis = new FileInputStream(file);

        // interpret the input stream as an UTF-8 stream
        // convert it to a reader
        Reader filereader = new InputStreamReader(fis, encoding);

        // explicit performance care: buffering the filereader
        return new BufferedReader(filereader);
    }

    /**
     * Write the content to the new file using the given encoding.
     *
     * @param encoding example: UTF-8
     * @param filename the name of the file
     * @param content the content of the file to be written
     *
     * @throws Exception DOCUMENT ME!
     */
    public final static void writeEncodedFile(String encoding, String filename,
        Element content) throws Exception {
    	
		FileOutputStream outputstream = new FileOutputStream(filename);

		Document document = content.getOwnerDocument();
		DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		serializer.getDomConfig().setParameter("format-pretty-print", true);
		LSOutput destination = domImplLS.createLSOutput();
		destination.setEncoding(encoding);
		destination.setByteStream(outputstream);

		serializer.write(content, destination);
		outputstream.close();

		// This previous version uses deprecated classes.
		// (The new version uses something surprisingly similar internally, though).
        
//		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
//											filename), encoding);
//        OutputFormat format = new OutputFormat(content.getOwnerDocument(), encoding, true);
//		format.setLineWidth(0);
//
//        XMLSerializer ser = new XMLSerializer(out, format);
//        ser.asDOMSerializer();
//        ser.serialize(content);
//        out.close();
        /*
		FileOutputStream outputstream = new FileOutputStream(filename);
		OutputFormat format = new OutputFormat(Method.XML, encoding, true);
		format.setPreserveSpace(true);

		XMLSerializer ser = new XMLSerializer(outputstream, format);
		ser.startNonEscaping();
		ser.startDocument();
		ser.serialize(content);
		ser.endDocument();
		outputstream.close();
		*/
        
    } 
    
	/**
	 * Write the content to the new file using the given encoding.
	 * Specialised version for eaf. Tabs and newline characters should be 
	 * maintained and the result's indentation should still be pretty.
	 * There doesn't seem to be a method to setPreserveSpace to true only for
	 * certain Elements or Nodes.?
	 * Yes there is actually: the attribute xml:space="preserve".
	 * Apart from that, whatever the issue was that seemed to not preserve whitespace
     * I don't see it with current serializers.
	 * Probably the issue simply doesn't arise because we don't have mixed-content nodes.
	 * However, current implementation code comments that it may introduce line breaks etc,
	 * but I haven't seen any actual code to do it.
	 * To be really sure, one can add attributes xml:space="preserve" : those are respected
	 * inside the various serializers. That is what is used for EAF 2.8 and it uses writeEncodedFile().
	 * There is a layout difference though: the old code used to keep a start-tag plus
	 * all its attributes on a single line. The new code inserts a newline
	 * between attributes if the line gets too long.
	 *
	 * @param encoding example: UTF-8
	 * @param filename the name of the file
	 * @param content the content of the file to be written
	 *
	 * @throws Exception any io or SAX exception that can occur
	 */
	public final static void writeEncodedEAFFile(String encoding, String filename,
		   Element content) throws Exception {
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
												filename), encoding);
		OutputFormat format = new OutputFormat(content.getOwnerDocument(), encoding, true);
		format.setLineWidth(0);
		//format.setNonEscapingElements(new String[]{"ANNOTATION_VALUE"}); //??

		// override a single XMLSerializer method...
		XMLSerializer ser = new XMLSerializer(out, format) {
			/**
			 * Sets preserveSpace to true for "ANNOTATION_VALUE" elements, CVEntry too??
			 * The same effect can be had if there is the attribute xml:space="preserve".
			 */
			@Override
			protected ElementState enterElementState( String namespaceURI, String localName,
					String rawName, boolean preserveSpace ) {

				if (rawName.equals("ANNOTATION_VALUE")) {
					return super.enterElementState(namespaceURI, localName, rawName, true);								  	
				}
				return super.enterElementState(namespaceURI, localName, rawName, preserveSpace);
			}
		};
		ser.asDOMSerializer();
		ser.serialize(content);
		out.close();	
    		   	
	}
   
}

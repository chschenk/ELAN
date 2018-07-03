package mpi.search.content.query.xml;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import mpi.search.content.query.model.ContentQuery;

/**
 * Evokes parser to read matches from xml-file
 * Created on Sep 30, 2004
 * @author Alexander Klassmann
 * @version Sep 30, 2004
 */
public class Xml2Query {
	private static XMLReader parser;

	public static void translate(String file, ContentQuery query) throws Exception {
		String filePath = null;
		
		if (file != null) {
			filePath = file.replace('\\','/');
		}

		if (filePath != null) {
			if (parser == null) {
				parser = new SAXParser();
			}
			parser.setContentHandler(new QueryContentHandler(query));
			FileInputStream stream = null;
			try {
				parser.parse(new InputSource(stream = new FileInputStream(filePath)));
	        } finally {
				try {
					if (stream != null) {
						stream.close();
					}
				} catch (IOException e) {
				}
			}
		}
		    
	}

}

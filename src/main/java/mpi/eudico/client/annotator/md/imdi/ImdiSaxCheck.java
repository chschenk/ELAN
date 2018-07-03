package mpi.eudico.client.annotator.md.imdi;

import mpi.eudico.client.annotator.util.ClientLogger;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Performs a quick trivial test on a metadata file.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ImdiSaxCheck {
    /**
     * Creates a new ImdiSaxCheck instance
     */
    public ImdiSaxCheck() {
        super();
    }

    /**
     * Starts parsing the file, only 2 elements
     * 
     * @version July 2013 added check to support CMDI (in fact IMDI within CMDI)
     * 
     * @param file the metadata file
     *
     * @return true if it is a "METATRANSCRIPT" file of type "SESSION" and the
     *         first element is "Session", or if it is a CMDI file with a Session component
     */
    public boolean isSessionFile(File file) {
        if ((file == null) || !file.exists()) {
            return false;
        }

    	FileInputStream fis = null;
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader(
                    "org.apache.xerces.parsers.SAXParser");
            reader.setFeature("http://xml.org/sax/features/validation", false);

            ImdiCheckHandler handler = new ImdiCheckHandler();
            reader.setContentHandler(handler);

            fis = new FileInputStream(file);
            InputSource source = new InputSource(fis);

            try {
                reader.parse(source);
            } catch (SAXException sax) {
            	ClientLogger.LOG.info("Is CMDI/IMDI Session file: " + handler.isSessionFile());
                return handler.isSessionFile();
            } catch (IOException ioe) {
                ClientLogger.LOG.warning("Cannot read file: " +
                    ioe.getMessage());
            }
        } catch (SAXException sex) {
            ClientLogger.LOG.warning("Cannot parse file: " + sex.getMessage());
        } catch (FileNotFoundException fnfe) {
            ClientLogger.LOG.warning("Cannot find file: " + fnfe.getMessage());
        } finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
			}
        }

        return false;
    }
}

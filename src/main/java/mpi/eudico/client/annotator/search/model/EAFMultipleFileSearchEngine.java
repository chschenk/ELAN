package mpi.eudico.client.annotator.search.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ConcurrentModificationException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.model.ProgressListener;
import mpi.search.model.SearchEngine;
import mpi.search.query.model.Query;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * $Id: EAFMultipleFileSearchEngine.java 46379 2018-01-31 16:04:03Z hasloe $
 * 
 * @version Jan 2018 replaced File as the input for SAXParser by FileInputSource 
 * (because of problems with diacritical marks in file paths)
 */
public class EAFMultipleFileSearchEngine implements SearchEngine {
    private final ProgressListener progressListener;

    /**
     * Creates a new EAFMultipleFileSearchEngine object.
     *
     * @param progressListener DOCUMENT ME!
     */
    public EAFMultipleFileSearchEngine(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    /**
     *
     *
     * @param regex DOCUMENT ME!
     * @param files DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static ContentQuery createQuery(String regex, File[] files)
        throws Exception {
        AnchorConstraint ac = new AnchorConstraint("", regex, 0L, 0L, "", true,
                false, null);
        ContentQuery query = new ContentQuery(ac, new EAFType(), files);

        return query;
    }

    /**
     * DOCUMENT ME!
     *
     * @param query
     *
     * @throws Exception DOCUMENT ME!
     */
    public void executeThread(ContentQuery query) throws Exception {
        EAFMultipleFileSearchHandler handler = new EAFMultipleFileSearchHandler(query);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);

        File[] files = query.getFiles();

        try {
        	SAXParser saxParser = factory.newSAXParser();
        	
            // iterate over the EAF Files to do the searching stuff
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
            	FileInputStream fis = null;
                handler.newFile(file);
                
                try {
                	fis = new FileInputStream(file);
    				InputSource source = new InputSource(fis);
    				saxParser.parse(source, handler);
                } catch (SAXException e) {
                    throw new SAXException(file.toString() + ":\n" +
                        e.getMessage());
                } catch (IOException e) {
    	            System.out.println("IO error: " + e.getMessage());
    	            throw new SAXException(file.toString() + ":\n" +
                            e.getMessage());
    	        } finally {
    				try {
    					if (fis != null) {
    						fis.close();
    					}
    				} catch (IOException e) {
    				}
    	        }

                if (progressListener != null) {
                    progressListener.setProgress((int) (((i + 1) * 100.0) / files.length));
                }
            }
        }
        // stop of thread can cause ConcurrentModificationException
        // (will be ignored since it has no further consequences)
        catch (ConcurrentModificationException e) {
        }
    }


    /**
     *
     *
     * @param query DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    @Override
	public void performSearch(Query query) throws Exception {
        executeThread((ContentQuery) query);
    }
}

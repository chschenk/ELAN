package mpi.eudico.client.annotator.search.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import mpi.eudico.client.annotator.ElanLocale;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.model.ProgressListener;
import mpi.search.model.SearchEngine;
import mpi.search.query.model.Query;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * $Id: EAFMultipleFileSearchEngine.java 46442 2018-04-19 09:27:35Z hasloe $
 * 
 * @version Jan 2018 replaced File as the input for SAXParser by FileInputSource 
 * (because of problems with diacritical marks in file paths)
 */
public class EAFMultipleFileSearchEngine implements SearchEngine {
    private final ProgressListener progressListener;

    /**
     * Creates a new EAFMultipleFileSearchEngine object.
     *
     * @param progressListener monitor of progress
     */
    public EAFMultipleFileSearchEngine(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    /**
     *
     *
     * @param regex the regular exception
     * @param files the files to search
     *
     * @return a ContentQuery object containing query string and the files
     *
     * @throws Exception any exception
     */
    public static ContentQuery createQuery(String regex, File[] files)
        throws Exception {
        AnchorConstraint ac = new AnchorConstraint("", regex, 0L, 0L, "", true,
                false, null);
        ContentQuery query = new ContentQuery(ac, new EAFType(), files);

        return query;
    }

    /**
     * Executes the query in all files that are part of the ContentQuery
     *
     * @param query contains the content query and the files to search
     *
     * @throws Exception exceptions are caught and packed in one ParseException
     */
    public void executeThread(ContentQuery query) throws Exception {
        EAFMultipleFileSearchHandler handler = new EAFMultipleFileSearchHandler(query);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);

        File[] files = query.getFiles();
        List<File> failedFiles = new ArrayList<File>();
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
//                    throw new SAXException(file.toString() + ":\n" +
//                        e.getMessage());
                	failedFiles.add(file);
                } catch (IOException e) {
//    	            System.out.println("IO error: " + e.getMessage());
//    	            throw new SAXException(file.toString() + ":\n" +
//                            e.getMessage());
                	failedFiles.add(file);
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
        
        if (!failedFiles.isEmpty()) {
        	StringBuilder sb = new StringBuilder(ElanLocale.getString("MultipleFileSearch.Message.ParseErrors"));
        	sb.append("\n");
        	int max = 6;
        	for (int i = 0; i < failedFiles.size() && i < max; i++) {
        		sb.append(failedFiles.get(i).getName());
        		sb.append("\n");
        	}
        	if (failedFiles.size() > max) {
        		sb.append("... + " + (failedFiles.size() - max));
        	}
        	if (LOG.isLoggable(Level.WARNING)) {
        		LOG.warning(sb.toString());
        	}
        	throw new SAXException(sb.toString());
        }
    }


    /**
     *
     *
     * @param query the content query
     *
     * @throws Exception any exception
     */
    @Override
	public void performSearch(Query query) throws Exception {
        executeThread((ContentQuery) query);
    }
}

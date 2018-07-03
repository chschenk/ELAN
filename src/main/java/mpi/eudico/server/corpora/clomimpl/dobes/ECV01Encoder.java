package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.util.ServerLogger;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.ExternalCVEntry;
import mpi.eudico.util.IoUtil;

import org.w3c.dom.Element;

/**
 * Creates an saves a External CV in ECV (XML) format
 * 
 * @author Micha Hulsbosch
 * @version jul 2010
 */
public class ECV01Encoder {

	public static final String VERSION = "0.1";
    
	/**
	 * Creates a DOM and saves it
	 * 
	 * @param cv (the External CV to be saved)
	 * @param path (the path of the EAF)
	 * @param extRefId (the id of the EXT_REF in the EAF, to be used in the file name)
	 */
	public void encodeAndSave(ExternalCV cv, String path, String extRefId) throws IOException {
		Element documentElement = createDOM(cv);
		//save(documentElement, path, extRefId);
		ExternalReference extRef = cv.getExternalRef();
		if (extRef != null) {
			//System.out.println(((ExternalReference) extRef).getValue());
			save(documentElement, path, extRef.getValue());
		}
	}
	
	/**
	 * Creates a DOM and saves it
	 * 
	 * @param cvs a list of the External CV's to be saved
	 * @param path the base path of the cache folder
	 * @param extRefId (the id of the EXT_REF in the EAF, to be used in the file name)
	 */
	public void encodeAndSave(List<ExternalCV> cvs, String path, ExternalReference extRef) 
	throws IOException {
		Element documentElement = createDOM(cvs);
		save(documentElement, path, extRef.getValue());
	}
	
	/**
	 * Creates a DOM and saves it
	 * 
	 * @param cvs a list of the External CV's to be saved
	 * @param path, file path for the ecv 
	 */
	public void encodeAndSave(List<ExternalCV> cvs, String path) 
	throws IOException {
		Element documentElement = createDOM(cvs);
		save(documentElement, path);
	}
	
	/**
	 * Saves the DOM to a file 
	 * (used to export the cv as external cv)
	 * 
	 * @param documentElement (the DOM)
	 * @param path the file path	 * 
	 */
	private void save(Element documentElement, String path) throws IOException {
		try {
		// test for errors
		if (("" + documentElement).length() == 0) {
				throw new IOException("Unable to save this file (zero length).");
				}
				//long beginTime = System.currentTimeMillis();
				//IoUtil.writeEncodedFile("UTF-8", path, documentElement);
				IoUtil.writeEncodedFile("UTF-8", path, documentElement);
			} catch (Exception eee) {
				//throw new IOException("Unable to save this file: " + eee.getMessage());
				ServerLogger.LOG.severe("Could not save the cache file: " + eee.getMessage());
			}
	}

	/**
	 * Saves the DOM to a file consisting of 
	 *   the name of EAF 
	 * + EXT_REF of the External CV
	 * + .ecv
	 * 
	 * @param documentElement (the DOM)
	 * @param path the cache directory
	 * @p
	 * aram extRefId (the EXT_REF of the External CV)
	 */
	/*
	
	private void save(Element documentElement, String path, String extRefId) {
		String savePath = path.substring(0, path.length() - 3) + extRefId + ".ecv";
		
		try {
            // test for errors
            if (("" + documentElement).length() == 0) {
                throw new IOException("Unable to save this file (zero length).");
            }
            long beginTime = System.currentTimeMillis();
            //IoUtil.writeEncodedFile("UTF-8", path, documentElement);
            IoUtil.writeEncodedFile("UTF-8", savePath, documentElement);
        } catch (Exception eee) {
            //throw new IOException("Unable to save this file: " + eee.getMessage());
        }
		
	}
	*/
	/**
	 * Saves the DOM to a file the location of which is derived from the cache base
	 * folder and the elements of the source url (EXT_REF of the External CV
	 * + .ecv)
	 * 
	 * @param documentElement (the DOM)
	 * @param path the cache directory
	 * @param extRefId (the EXT_REF of the External CV)
	 */
	private void save(Element documentElement, String cachePath, String urlString) throws IOException {
		String savePath = cachePath;
			//Constants.ELAN_DATA_DIR + Constants.FILESEPARATOR + "CVCACHE";
		String fileName = "";
		try {
			URL url = new URL(urlString);
			String urlHost = url.getHost();
			if(urlHost.equals("")) {
				urlHost = "localfile_system";
			}

			savePath += File.separator + urlHost;
			String pathElements[] = url.getPath().substring(1).split("/");
			for(int i = 0; i < (pathElements.length - 1); i++) {
				savePath += File.separator + pathElements[i];
			}
			fileName = pathElements[pathElements.length - 1];
		} catch (MalformedURLException e) {
			//e.printStackTrace();
			ServerLogger.LOG.warning("Could not create a cache directory structure: " + e.getMessage());
			int index = urlString.lastIndexOf(File.separator);
			if (index > -1 && index < urlString.length() - 2) {
				fileName = urlString.substring(index + 1);
			} else {
				index = urlString.lastIndexOf("/");
				if (index > -1 && index < urlString.length() - 2) {
					fileName = urlString.substring(index + 1);
				} else {
					fileName = urlString;
				}
			}
		}
		//System.out.println(savePath);

		File dir = new File(savePath);
		
		if(dir.exists() || dir.mkdirs()) {
			savePath += File.separator + fileName;
			try {
				// test for errors
				if (("" + documentElement).length() == 0) {
					throw new IOException("Unable to save this file (zero length).");
				}
				//long beginTime = System.currentTimeMillis();
				//IoUtil.writeEncodedFile("UTF-8", path, documentElement);
				IoUtil.writeEncodedFile("UTF-8", savePath, documentElement);
			} catch (Exception eee) {
				//throw new IOException("Unable to save this file: " + eee.getMessage());
				ServerLogger.LOG.severe("Could not save the cache file: " + eee.getMessage());
			}
		}
	}

	/**
	 * Creates a DOM of the External CV
	 * 
	 * @param cv (the External CV)
	 * @return the ECV document element
	 */
	private Element createDOM(ExternalCV cv) {
		ECV01 ecvFactory = null;
		Element cvElement;
		Element entryElement;
        CVEntry entry;
        String extRefId = null;
        
		try {
            ecvFactory = new ECV01();
        } catch (Exception ex) {
            ServerLogger.LOG.severe("Could not create a document builder: " + ex.getMessage());
        }
        
        Map<String, ExternalReference> extRefIds = new HashMap<String, ExternalReference>();
        List<String> extRefList = new ArrayList<String>();
        int extRefIndex = 1;
        
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String dateString = dateFmt.format(Calendar.getInstance().getTime());
        dateString = correctDate(dateString);

        Element ecvDocument = ecvFactory.newExternalCVDocument(dateString,
                "", VERSION);
        ecvFactory.appendChild(ecvDocument);
        
        cvElement = ecvFactory.newControlledVocabulary(cv.getName(), cv.getDescription());
        
        CVEntry[] entries = cv.getEntries();
        for (int j = 0; j < entries.length; j++) {
    		entry = entries[j];
    		if (entry.getExternalRef() != null) {
    			if (!extRefIds.containsValue(entry.getExternalRef())) {
    				extRefId = "er" + extRefIndex++;
    				extRefIds.put(extRefId, entry.getExternalRef());
    				extRefList.add(extRefId);
    			} else {
    				for (int k = 0; k < extRefList.size(); k++) {
    					if (entry.getExternalRef().equals(extRefIds.get(extRefList.get(k)))) {
    						extRefId = extRefList.get(k);
    						break;
    					}
    				}
    			}
    		} else {
    			extRefId = null;
    		}
    		String entryId = null;
    		if (entry instanceof ExternalCVEntry) {
    			entryId = ((ExternalCVEntry) entry).getId();
    		}

    		entryElement = ecvFactory.newCVEntry(entryId, entry.getValue(),
    				entry.getDescription(), extRefId);
    		cvElement.appendChild(entryElement);
    	}
        
        ecvDocument.appendChild(cvElement);
        
     // EXTERNAL REFERENCES
        Element erElement;
        ExternalReferenceImpl eri;
        String id;
        
        for (int i = 0; i < extRefList.size(); i++) {
        	id = extRefList.get(i);
        	eri = (ExternalReferenceImpl) extRefIds.get(id);
        	if (id != null && eri != null) {
        		erElement = ecvFactory.newExternalReference(id, eri.getTypeString(), eri.getValue());
        		ecvDocument.appendChild(erElement);
        	}
        }
        
		return ecvFactory.getDocumentElement();
	}
	
	/**
	 * Creates a DOM of the list of External CV's
	 * 
	 * @param cv the External CV's
	 * @return the ECV document element
	 */
	private Element createDOM(List<ExternalCV> cvs) {
		ECV01 ecvFactory = null;
		ExternalCV cv;
		Element cvElement;
		Element entryElement;
        CVEntry entry;
        String extRefId = null;
        
		try {
            ecvFactory = new ECV01();
        } catch (Exception ex) {
        	ServerLogger.LOG.severe("Could not create a document builder: " + ex.getMessage());
        }
        
        HashMap<String, ExternalReference> extRefIds = new HashMap<String, ExternalReference>();
        ArrayList<String> extRefList = new ArrayList<String>();
        int extRefIndex = 1;
        
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String dateString = dateFmt.format(Calendar.getInstance().getTime());
        dateString = correctDate(dateString);

        Element ecvDocument = ecvFactory.newExternalCVDocument(dateString,
                "", VERSION);
        ecvFactory.appendChild(ecvDocument);
        
        // iterate over cv's
        if (cvs != null && cvs.size() > 0) {
        	for (int i = 0; i < cvs.size(); i++) {
        		cv = cvs.get(i);
		        cvElement = ecvFactory.newControlledVocabulary(cv.getName(), cv.getDescription());
		        
		        CVEntry[] entries = cv.getEntries();
		        for (int j = 0; j < entries.length; j++) {
		    		entry = entries[j];
		    		if (entry.getExternalRef() != null) {
		    			if (!extRefIds.containsValue(entry.getExternalRef())) {
		    				extRefId = "er" + extRefIndex++;
		    				extRefIds.put(extRefId, entry.getExternalRef());
		    				extRefList.add(extRefId);
		    			} else {
		    				for (int k = 0; k < extRefList.size(); k++) {
		    					if (entry.getExternalRef().equals(extRefIds.get(extRefList.get(k)))) {
		    						extRefId = extRefList.get(k);
		    						break;
		    					}
		    				}
		    			}
		    		} else {
		    			extRefId = null;
		    		}
		    		String entryId = null;
		    		if (entry instanceof ExternalCVEntry) {
		    			entryId = ((ExternalCVEntry) entry).getId();
		    		}
		
		    		entryElement = ecvFactory.newCVEntry(entryId, entry.getValue(),
		    				entry.getDescription(), extRefId);
		    		cvElement.appendChild(entryElement);
		    	}
		        
		        ecvDocument.appendChild(cvElement);
        	}
        }
        
     // EXTERNAL REFERENCES
        Element erElement;
        ExternalReferenceImpl eri;
        String id;
        ArrayList<String> types = new ArrayList<String>();
        types.add("undefined");
        types.add("reference_group");
        types.add("iso12620");
        types.add("resource_url");
        types.add("external_cv");
        
        for (int i = 0; i < extRefList.size(); i++) {
        	id = extRefList.get(i);
        	eri = (ExternalReferenceImpl) extRefIds.get(id);
        	if (id != null && eri != null) {
        		erElement = ecvFactory.newExternalReference(id, types.get(eri.getReferenceType()), eri.getValue());
        		ecvDocument.appendChild(erElement);
        	}
        }
        
        return ecvFactory.getDocumentElement();
	}

	/**
     * Creates a validating date string.
     *
     * @param strIn the date string to correct
     *
     * @return a validating date string
     */
	private static String correctDate(String strIn) {
		String strResult = new String(strIn);

        try {
            int offsetGMT = Calendar.getInstance().getTimeZone().getRawOffset() / (60 * 60 * 1000);

            String strOffset = "+";

            if (offsetGMT < 0) {
                strOffset = "-";
            }

            offsetGMT = Math.abs(offsetGMT);

            if (offsetGMT < 10) {
                strOffset += "0";
            }

            strOffset += (offsetGMT + ":00");

            strResult += strOffset;

            int indexSpace = strResult.indexOf(" ");

            if (indexSpace != -1) {
                String strEnd = strResult.substring(indexSpace + 1);
                strResult = strResult.substring(0, indexSpace);
                strResult += "T";
                strResult += strEnd;
            }

            strResult = strResult.replace('.', '-');
        } catch (Exception ex) {
            return strIn;
        }

        return strResult;
	}

}

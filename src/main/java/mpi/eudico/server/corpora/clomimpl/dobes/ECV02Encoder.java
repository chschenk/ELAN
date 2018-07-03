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
import java.util.Map.Entry;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.util.ServerLogger;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.IoUtil;

import org.w3c.dom.Element;

/**
 * Creates an saves a External CV in ECV (XML) format
 * 
 * @author Micha Hulsbosch
 * @author Olaf Seibert
 * @version jan 2014
 */
public class ECV02Encoder {

	public static final String VERSION = "0.2";
    
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
			//System.out.println(extRef.getValue());
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
				IoUtil.writeEncodedFile("UTF-8", path, documentElement);
			} catch (Exception eee) {
				//throw new IOException("Unable to save this file: " + eee.getMessage());
				ServerLogger.LOG.severe("Could not save the cache file: " + eee.getMessage());
			}
	}

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
		ArrayList<ExternalCV> list = new ArrayList<ExternalCV>(1);
		list.add(cv);
		return createDOM(list);
	}
	
	/**
	 * Creates a DOM of the list of External CV's
	 * 
	 * @param cv the External CV's
	 * @return the ECV document element
	 */
	private Element createDOM(List<ExternalCV> cvs) {
		ECV02 ecvFactory = null;
        
		try {
            ecvFactory = new ECV02();
        } catch (Exception ex) {
        	ServerLogger.LOG.severe("Could not create a document builder: " + ex.getMessage());
        }
        
        GetExtRefIdParams getExtRefIdParams = new GetExtRefIdParams();
        
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String dateString = dateFmt.format(Calendar.getInstance().getTime());
        dateString = correctDate(dateString);

        Element ecvDocument = ecvFactory.newExternalCVDocument(dateString,
                "", VERSION);
        ecvFactory.appendChild(ecvDocument);
        
        
        // iterate over cv's
        if (cvs != null) {
            // Find the combined set of languages that is used.
        	// The longIds (usually URIs) are leading;
        	// the short ids are just arbitrary shorthands.
            Map<String, LanguageRecord> langs = new HashMap<String, LanguageRecord>();

            for (ControlledVocabulary cv: cvs) {
            	int nLangs = cv.getNumberOfLanguages();
            	for (int i = 0; i < nLangs; i++) {
            		String id = cv.getLanguageId(i);
            		String def = cv.getLongLanguageId(i);
            		String label = cv.getLanguageLabel(i);
            		langs.put(def, new LanguageRecord(id, def, label));
            	}
            }
            
            for (String def : langs.keySet()) {
            	LanguageRecord lr = langs.get(def);
            	Element languageElement = ecvFactory.newLanguage(lr.getId(), lr.getDef(), lr.getLabel());
	        	ecvDocument.appendChild(languageElement);
            }
            
        	for (ExternalCV cv : cvs) {
        		Element cvElement = ecvFactory.newControlledVocabulary(cv.getName());
		        
		        int nLangs = cv.getNumberOfLanguages();
		        for (int i = 0; i < nLangs; i++) {
		        	// add <DESCRIPTION>
		        	Element descriptionElement = ecvFactory.newDescription(cv.getLanguageId(i), cv.getDescription(i));
		        	cvElement.appendChild(descriptionElement);
		        }
		        
		        String extRefId = null;
		        
		        for (CVEntry entry : cv) {
	                extRefId = getExtRefId(getExtRefIdParams, entry.getExternalRef());

		    		Element entryElement = ecvFactory.newCVEntryML(entry.getId(), extRefId);
					
					// <CVE_VALUE>s inside <CV_ENTRY_ML>
					for (int i = 0; i < nLangs; i++) {
						String languageId = cv.getLanguageId(i);
						String description = entry.getDescription(i);
						String value = entry.getValue(i);
						if (!value.isEmpty()) {
							Element valueElement = ecvFactory.newCVEntryValue(languageId, value, description);            				
							entryElement.appendChild(valueElement);
						}
					}
					      				
					cvElement.appendChild(entryElement);
		    	}
		        ecvDocument.appendChild(cvElement);
        	}
        }
        
     // EXTERNAL REFERENCES

        for (Entry<ExternalReference, String> pair : getExtRefIdParams.map.entrySet()) {
            ExternalReference er = pair.getKey();
            String id = pair.getValue();

            if (id != null && er != null) {
            	Element erElement = ecvFactory.newExternalReference(id, er.getTypeString(), er.getValue());
        		ecvDocument.appendChild(erElement);
        	}
        }
        
        return ecvFactory.getDocumentElement();
	}

	/**
	 * A bit of state information for mapping ExternalReferences to ids for use in the
	 * saved file.
	 * <p>
	 * The Map could be a LinkedHashMap, if we care about printing the ExternalReferences
	 * in numerical order.
	 * 
	 * @author olasei
	 */
	private static class GetExtRefIdParams {
		private int extRefIndex;
		Map<ExternalReference, String> map;
		
		GetExtRefIdParams() {
			extRefIndex = 1;
			map = new HashMap<ExternalReference, String>();
		}
	}
	
	/**
	 * Gets an ID for a given ExternalReference. If identical ExternalReferences
	 * are passed more than once, it will produce the same ID every time.
	 * If passed null, it will return null.
	 * 
	 * @param params state object that keeps the context
	 * @param extRef
	 * @return
	 */
	private static String getExtRefId(GetExtRefIdParams params, ExternalReference extRef) {
		if (extRef == null || params == null) {
			return null;
		}
		
		String id = params.map.get(extRef);
		if (id == null) {
			id = "er" + params.extRefIndex++;
			params.map.put(extRef, id);
		}
		return id;
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

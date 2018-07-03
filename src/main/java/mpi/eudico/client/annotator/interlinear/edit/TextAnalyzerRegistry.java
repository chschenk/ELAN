package mpi.eudico.client.annotator.interlinear.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import mpi.eudico.util.ExtClassLoader;
import nl.mpi.lexan.analyzers.TextAnalyzer;
import nl.mpi.lexan.analyzers.helpers.Information;

public class TextAnalyzerRegistry {
    private static TextAnalyzerRegistry analyzerRegistry;
    private final Logger LOG = Logger.getLogger("ClientLogger");
    
	private List<TextAnalyzer> instantiatedAnalyzers;// analyzers as discovered on the class path
	private List<Class<? extends TextAnalyzer>> analyzersClassList;
	private List<Information> informationList;
	private Map<Information, Class<? extends TextAnalyzer>> info2analyzerClass;
	private Map<Information, TextAnalyzer> info2Analyzer;
	/** maintain a map per document */
	private Map<String, Map<Information, TextAnalyzer>> doc2AnalyzerMap;
    
	/**
	 * 
	 */
	private TextAnalyzerRegistry() {
		super();
		instantiatedAnalyzers = new ArrayList<TextAnalyzer>();
		analyzersClassList = new ArrayList<Class<? extends TextAnalyzer>>();
		informationList = new ArrayList<Information>();
		info2analyzerClass = new HashMap<Information, Class<? extends TextAnalyzer>>();
		info2Analyzer = new HashMap<Information, TextAnalyzer>();
		doc2AnalyzerMap = new HashMap<String, Map<Information, TextAnalyzer>>();
		initLoad();
	}
    
    public static TextAnalyzerRegistry getInstance() {
    	if (analyzerRegistry == null) {
    		analyzerRegistry = new TextAnalyzerRegistry();
    	}
    	
    	return analyzerRegistry;
    }
    
    private void initLoad() {
     	List<TextAnalyzer> instList = ExtClassLoader.getInstance().getInstanceList(TextAnalyzer.class);
    	if (instList != null) {
    		
    		for (TextAnalyzer ta : instList) {
    			instantiatedAnalyzers.add(ta);
    			analyzersClassList.add(ta.getClass());
    			// share, instead of duplicate
    			final Information information = ta.getInformation();
				informationList.add(information);
    			info2analyzerClass.put(information, ta.getClass());
    			info2Analyzer.put(information, ta);
    		}
    	}
    }
    
    public List<TextAnalyzer> getTextAnalyzers() {
    	return instantiatedAnalyzers;
    }
    
    public List<Information> getAnalyzersInfo() {
    	return informationList;
    }
    
    /**
     * Creates and returns a new instance of the analyzers identified by the 
     * specified Information object.
     * 
     * @param info the information object
     * 
     * @return a new instance of the analyzer
     */
    public TextAnalyzer getAnalyzerInstance(Information info) {
    	TextAnalyzer ta = info2Analyzer.get(info);
    	if (ta != null) {
    		return ta;
    	}

    	// Should not get here, since all classes are already instantiated...
        Class<? extends TextAnalyzer> taClass = info2analyzerClass.get(info);
        if (taClass != null) {
        	ta = ExtClassLoader.getInstance().createInstance(taClass);
        	info2Analyzer.put(info, ta);
        }
        
    	return ta;
    }

    /**
     * Create and or retrieve an analyzer for a specific document(window).
     * This way each document has its own instance of an analyzer. An alternative could be 
     * to have only one instance per analyzer and have multiple settings panels etc. 
     * communicate with that one instance.
     * 
     * @param documentId the URN of an annotation document
     * @param info the information object of an analyzer
     * 
     * @return the instance of the analyzer for that document or a new instance
     */
    public TextAnalyzer getAnalyzerForDoc(String documentId, Information info) {
    	if (documentId == null || info == null) {
    		return null;
    	}
    	TextAnalyzer ta = null;
    	Map<Information, TextAnalyzer> mapForDoc = doc2AnalyzerMap.get(documentId);
    	
    	if (mapForDoc == null) {
    		mapForDoc = new HashMap<Information, TextAnalyzer>();
    		doc2AnalyzerMap.put(documentId, mapForDoc);
    	}
    	
    	ta = mapForDoc.get(info);
		
    	if (ta == null) {
            Class<? extends TextAnalyzer> taClass = info2analyzerClass.get(info);
            if (taClass != null) {
            	ta = ExtClassLoader.getInstance().createInstance(taClass);
            	mapForDoc.put(info, ta);
            }
		}
  	
    	return ta;
    }
    
    /**
     * Removes the specified analyzer from the map for the specified document.
     * 
     * @param documentId the URN of the document to unload
     * @param info the Information object of the analyzer
     */
    public void removeAnalyzerForDoc(String documentId, Information info) {
    	Map<Information, TextAnalyzer> mapForDoc = doc2AnalyzerMap.remove(documentId);
    	if (mapForDoc != null) {
    		TextAnalyzer ta = mapForDoc.remove(info);
    		if (ta != null) {
    			// could log...
    			// unloading of the analyzer is done elsewhere
    		}
    	}
    }
    
    /**
     * Removes the analyzer map for the specified document, e.g. when the document is closed.
     * 
     * @param documentId the URN of the document to unload
     */
    public void removeAnalyzersForDoc (String documentId) {
    	Map<Information, TextAnalyzer> mapForDoc = doc2AnalyzerMap.remove(documentId);
    	if (mapForDoc != null) {
    		mapForDoc.clear();
    	}
    }
    
    // etc...??
}

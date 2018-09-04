package mpi.eudico.client.annotator.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.util.TranscriptionECVLoader;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.util.ProcessReporter;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.Pair;

/**
 * A class/script that updates all transcriptions in a specified folder by 
 * checking external controlled vocabularies and updating, if needed, annotations
 * that are linked to a CV entry. The files can either be overwritten or
 * can be saved in a different folder. There is support for specifying which
 * language to use from the CV; this overrides the normal checks on the tier's
 * language and on stored language preferences.
 * TODO: this might be useful for internal CV's as well?
 */
public class CorpusECVUpdater {
	private File inputFolder;
	private String inputFolderString;
	private File outputFolder;
	private String outputFolderString;
	/** select the entry variant of this language from the CV, 
	 *  the value should correspond to the LANG_REF attribute 
	 *  in most cases a 3-letter code */
	private String forcedLanguage;
	
	/** a map of ecv url's to lists of (loaded) ECV's */
	private HashMap<String, List<ExternalCV>> urlMap;
	private static final String FLANGKEY = "fLang";
	private static final String FOLDERSKEY = "folders";
	
	/** the object to report to */
	private ProcessReporter reporter = null;
	
	/** whether walk recursively through the input folder.
	 * Default is true for backward compatibility. */
	private Boolean recursive = true;
	
	/** the object to update progress to */
	private ProgressListener progressListerner;
	
	/** */
	private Boolean canceled = false;
	private Map<String, Integer> unknownAnnotations;
	
	/** a flag that determines whether an annotation value has to be updated based on 
	 * the annotation's reference to a CVEntry (the default behavior) or the 
	 * reference has to be removed or updated based on the annotation value */
	private boolean annotationValuePrecedence = false;

	/**
	 * Constructor.
	 */
	private CorpusECVUpdater() {
	}
	public CorpusECVUpdater(ProcessReporter reporter) {
		this.reporter = reporter;
	}
	public CorpusECVUpdater(ProcessReporter reporter, ProgressListener progressListener) {
		this.reporter = reporter;
		this.progressListerner = progressListener;
	}

	/**
	 * For command-line usage:<br> 
	 * >java ... CorpusECVUpdater [-L lng] inputfolder [outputfolder]
	 * 
	 * Starts the updating process if at least an input folder is 
	 * provided. 
	 * 
	 * @param args the arguments should include the input folder, 
	 * can include a -L language parameter and an output folder
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("No folder containing eaf files specified");
			System.out.println("Usage: java (...) CorpusECVUpdater [-L lng] inputfolder [outputfolder]");
			System.exit(1);
		}
		
		try {
			Map<String, Object> parsedArgs = parseArgs(args);
			String fLang = (String) parsedArgs.get(FLANGKEY);
			String[] folders = (String[]) parsedArgs.get(FOLDERSKEY);
			
			System.out.println("Input folder is: " + folders[0]);
			if (folders[1] != null) {
				System.out.println("Output folder is: " + folders[1]);
			} else {
				System.out.println("No output folder specified, overwriting files");
			}
			// other tests?
			

			CorpusECVUpdater cECV = new CorpusECVUpdater();
			int success = cECV.updateFiles(folders, fLang);
			System.out.println("Exit Program: " + success);
			System.exit(success);
		} catch(Exception e) {
			System.out.println("The command line arguments are not correct");
			System.out.println("Usage: java (...) CorpusECVUpdater [-L lng] inputfolder [outputfolder]");
			System.exit(1);
		}
	}

	/**
	 * Parses the arguments of the command line
	 * TODO Rework it to accept -l flag anywhere
	 * @param args the arguments from the command line
	 * @return A map containing the parsed arguments
	 */
	private static Map<String, Object> parseArgs(String[] args) {
		Map<String, Object> parsedArgs = new HashMap<String, Object>();
		
		String fLang = null;
		String[] folders = new String[2];
		if (args.length >= 3) {
			if (args[0].toLowerCase().equals("-l")) {
				fLang = args[1];
				System.out.println("Language to apply is: " + fLang);
				folders[0] = args[2];
				if (args.length > 3) {
					folders[1] = args[3];
				}
			} else {// simply take the first 2 as input & output folder 
				folders[0] = args[0];
				folders[1] = args[1];
			}
		} else {// args.length = 1 or 2
			folders[0] = args[0];
			if (args.length > 1) {
				folders[1] = args[1];
			}
		}
		System.out.println("Input folder is: " + folders[0]);
		if (folders[1] != null) {
			System.out.println("Output folder is: " + folders[1]);
		} else {
			System.out.println("No output folder specified, overwriting files");
		}
		// other tests?
		
		parsedArgs.put(FLANGKEY, fLang);
		parsedArgs.put(FOLDERSKEY, folders);
		return parsedArgs;
	}

	/**
	 * Updates all .eaf files in the specified input folder (recursively).
	 * 
	 * @param folders one input folders and possibly an output folder
	 * @param forceLang the language to select from the CV
	 * 
	 * @return a non-zero exit value in case there is an error, 0 otherwise
	 */
	public int updateFiles(String[] folders, String forceLang) {
		forcedLanguage = forceLang;
		unknownAnnotations = new HashMap<String, Integer>();
		
		try {
			inputFolder = new File(folders[0]);
			
			if (!inputFolder.exists() || !inputFolder.isDirectory()) {
				System.out.println("The specified path is not a folder containing eaf files: " + folders[0]);
				return 2;
			}
			inputFolderString = inputFolder.getAbsolutePath().replace('\\', '/');
			
			outputFolder = null;
			if (folders.length > 1 && folders[1] != null) {
				outputFolder = new File(folders[1]);
				if (!outputFolder.exists()){
					boolean created = outputFolder.mkdirs();
					if (!created) {
						System.out.println("Could not create the output folder: " + folders[1]);
						return 4;
					}
				}
				outputFolderString = outputFolder.getAbsolutePath().replace('\\', '/');
			}
			
			processFileOrDirectory(inputFolder, true);
		} catch (Throwable t) {
			if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
            	ClientLogger.LOG.warning("Error while processing files (" + t.getMessage() + ")");
            }
		}
		
		logMessage("\n");
		for(String unknownAnnotation : unknownAnnotations.keySet()) {
			logMessage("UNKNOWN ANNOTATION: " + unknownAnnotation + " " + unknownAnnotations.get(unknownAnnotation));
		}
		
		return 0;
	}
	
	/**
	 * A method that checks if the File object represents a folder or a file
	 * and then delegates to an appropriate method.
	 * 
	 * @param file a folder or a file
	 * @param rootFolder whether this is the root folder for processing. Necessary if no recursion is wanted.
	 */
	private void processFileOrDirectory(File file, Boolean rootFolder) {
		if (file.isDirectory() && (recursive || rootFolder)) {
			File[] subList = file.listFiles();
			for (File f : subList) {
				if(!canceled) {
					processFileOrDirectory(f, false);
				} else {
					break;
				}
			}
		} else {
			processFile(file);
		}
	}
	
	/**
	 * Processes the specified file. If it is not an eaf file it is 
	 * currently ignored.
	 * 
	 * @param file the file to update, folders are ignored
	 */
	private void processFile(File file) {
		if (file.isDirectory()) {
			System.out.println("Error: method called with folder as argument");
		} else {
			try {
				if (file.getName().toLowerCase().endsWith(".eaf")) {
					if(progressListerner != null) {
						progressListerner.progressUpdated(this, -1, file.getAbsolutePath());
					}
					logMessage("\nFILE: " + file.getAbsolutePath());
					TranscriptionImpl t = new TranscriptionImpl(file.getAbsolutePath());
					t.setNotifying(false);
					t.setUnchanged();
					
					if (t.getControlledVocabularies().size() > 0) {
						boolean allCVSReplaced = true;
						if (urlMap != null) {
							allCVSReplaced = updateAlreadyLoadedCVS(urlMap, t);
						} 
						if (urlMap == null || !allCVSReplaced) {
							TranscriptionECVLoader tl = new TranscriptionECVLoader();
							tl.loadExternalCVs(t, null, false);
							updateAnnotations(t);
							
							urlMap = new HashMap<String, List<ExternalCV>>(tl.getLoadedECVMap());
							removeACMEditableObjects(urlMap);
						}
					}
					
					if (outputFolder == null) {
						if (t.isChanged()) {
							System.out.println("Updated file: " + file.getName());
							ACMTranscriptionStore.getCurrentTranscriptionStore().
							storeTranscription(t, null, null, TranscriptionStore.EAF);
						}
					} else {
						// calculate the new location
						String nextPath = file.getAbsolutePath().replace('\\', '/');
						nextPath = nextPath.replaceFirst(inputFolderString, outputFolderString);
						
						String outputSubFolder = nextPath.substring(0, nextPath.lastIndexOf('/'));
						if (!outputSubFolder.equals(outputFolderString)) {
							File osFile = new File(outputSubFolder);
							if (!osFile.exists()) {
								osFile.mkdirs();
							}
						}
						
						if(t.isChanged()) {
							ACMTranscriptionStore.getCurrentTranscriptionStore().
							storeTranscription(t, null, null, nextPath, TranscriptionStore.EAF);
						} 
//						else {
//							// Just copy to prevent change DATE setting in the .eaf file
//							try {
//								FileUtility.copyToFile(file, new File(nextPath));
//							} catch (Exception e) {
//								System.out.println("Error while processing file: " + e.getMessage());
//							}
//						}
					}
					if(t.isChanged()) {
						System.out.println("Processed file: " + file.getName());
					}
					
					// Next bit is for avoiding a 'out of memory' error
                    Preferences.removeDocument(t); 
				}
			} catch (IOException ioe) {
				System.out.println("Error while processing file: " + ioe.getMessage());
			}
		}
	}
	
	/**
	 * Removes <b>ACMEditableObject</b>s from ECVs
	 * 
	 *  @param loadedMap a map of url's to lists of loaded ECV
	 */
	private static void removeACMEditableObjects(HashMap<String, List<ExternalCV>> loadedMap) {
		Iterator<List<ExternalCV>> ecvIter = loadedMap.values().iterator();
		while (ecvIter.hasNext()) {
			List<ExternalCV> ecvList = loadedMap.get(ecvIter.next());
			if (ecvList != null) {
				for (ExternalCV ecv : ecvList) {
					if (ecv != null) {
						ecv.removeACMEditableObject();
					}
				}
			}
		}
	}
	
	/**
	 * Replaces ECV's that have already been loaded. 
	 * 
	 * @param loadedMap a map of url's to lists of loaded ECV
	 * @param t the transcription to update
	 * @return false if not all ECV's have been replaced
	 */
	private boolean updateAlreadyLoadedCVS(HashMap<String, List<ExternalCV>> loadedMap, TranscriptionImpl t) {
		int numReplacedECV = 0;
		boolean allCVSInMap = true;
		for (int i = 0; i < t.getControlledVocabularies().size(); i++) {
			ControlledVocabulary cv = t.getControlledVocabularies().get(i);
			if (cv instanceof ExternalCV) {
				ExternalCV ecv = (ExternalCV) cv;
				if (!ecv.isLoadedFromURL()) {
					ExternalReference er = ecv.getExternalRef();
					if (er.getReferenceType() == ExternalReference.EXTERNAL_CV) {
						String urlString = er.getValue();
						if (urlString != null) {
							// Get all already loaded ECVs from the file referred to by urlString
							List<ExternalCV> loadedECV = loadedMap.get(urlString);
							if (loadedECV != null) {
								for (ExternalCV lECV : loadedECV) {
									if (lECV.getName().equals(ecv.getName())) {
										// already loaded, replace
										// t.replaceControlledVocabulary() changes the order of the CV's
										
										t.getControlledVocabularies().remove(i);
										t.getControlledVocabularies().add(i, lECV);
										numReplacedECV++;
										t.setUnchanged();
									}
								}
							} else {
								allCVSInMap = false;
							}
						}
					}
				}
			}
		}
		
		if (numReplacedECV > 0) {
			updateAnnotations(t);
		}
		
		return allCVSInMap;
	}
	
	/**
	 * The actual updating of the annotations. If no language is enforced it simply calls
	 * the existing checkAnnotECVConsistency() method. Otherwise it will loop over all tiers etc.
	 * 
	 * @param t the transcription to check
	 */
	private void updateAnnotations(TranscriptionImpl t) {
		// The following code is partially copied from TranscriptionImpl.
		for (TierImpl tier : t.getTiers()) {
			// Get effective language of tier
			Pair<ControlledVocabulary, Integer> pair = tier.getEffectiveLanguage();
			if (pair == null) {
				continue;
			}
			int tierLangIndex = pair.getSecond();
			
			// Is there a CV? The following code is already performed in tier.getEffectiveLanguage()
			// so could do ControlledVocabulary cv = pair.getFirst();
			LinguisticType lt = tier.getLinguisticType();
			String cvname = lt.getControlledVocabularyName();
			if (cvname == null || cvname.isEmpty()) {
				continue;
			}
			
			// Can we find that CV?
			ControlledVocabulary cv = t.getControlledVocabulary(cvname);
			if (cv == null) {
				continue;
			}
			
			if (cv instanceof ExternalCV) {// maybe a check on internal CV's would make sense as well?
				ExternalCV ecv = (ExternalCV) cv;
				
				if (!ecv.isLoadedFromURL() && !ecv.isLoadedFromCache()) {
					continue;
				}
				
				int langIndex = cv.getIndexOfLanguage(forcedLanguage);
				
				if (langIndex < 0) {
					if(tierLangIndex >= 0) {
						langIndex = tierLangIndex;
					} else {
						continue;// not there
					}
				}
				
				for (AbstractAnnotation currentAnn : tier.getAnnotations()) {
					String cvEntryRefId = currentAnn.getCVEntryId();
					if (cvEntryRefId == null) {
						// if there is no ref to a cv entry but there is an annotation value
						// check if the value is in the CV and add a reference to that entry
						if (!currentAnn.getValue().isEmpty()) {
							CVEntry cvEntry = ecv.getEntryWithValue(langIndex, currentAnn.getValue());
							if (cvEntry != null) {
								logMessage("TIER: " + tier.getName()
									+ " - ANNOTATION_ID: " + currentAnn.getId()
									+ " - ANNOTATION_VALUE: " + currentAnn.getValue()
									+ " !! CVE_REF added: " + cvEntry.getId()
									+ " - " + cvEntry.getValue(langIndex));
								currentAnn.setCVEntryId(cvEntry.getId());
								t.setChanged();
							} else {
								logMessage("TIER: " + tier.getName()
								+ " - ANNOTATION_ID: " + currentAnn.getId()
								+ " - ANNOTATION_VALUE: " + currentAnn.getValue()
								+ " !! Annotation not in ECV");
								registerUnknownAnnotation(currentAnn.getValue());
							}
						}
					} else if (annotationValuePrecedence) {
						if (!currentAnn.getValue().isEmpty()) {
							CVEntry cvEntry = ecv.getEntryWithValue(langIndex, currentAnn.getValue());
							if (cvEntry != null) {
								if (!cvEntryRefId.equals(cvEntry.getId())) {
									logMessage("TIER: " + tier.getName()
										+ " - ANNOTATION_ID: " + currentAnn.getId()
										+ " - ANNOTATION_VALUE: " + currentAnn.getValue()
										+ " !! CVE_REF updated: " + cvEntryRefId
										+ " => " + cvEntry.getId());
									currentAnn.setCVEntryId(cvEntry.getId());
									t.setChanged();
								} // else annotation value and cv entry idref are consistent
							} else {
								// There's no entry with the same value as the annotation,
								// so remove the CV Entry ID from the annotation
								logMessage("TIER: " + tier.getName()
									+ " - ANNOTATION_ID: " + currentAnn.getId()
									+ " - ANNOTATION_VALUE: " + currentAnn.getValue()
									+ " !! CVE_REF removed: " + cvEntryRefId);
								currentAnn.setCVEntryId(null);
								t.setChanged();
								// and register as "unknown annotation"
								logMessage("TIER: " + tier.getName()
								+ " - ANNOTATION_ID: " + currentAnn.getId()
								+ " - ANNOTATION_VALUE: " + currentAnn.getValue()
								+ " !! Annotation not in ECV");
								registerUnknownAnnotation(currentAnn.getValue());
							}
						}
					} else {				
						CVEntry entry = ecv.getEntrybyId(cvEntryRefId);
						String value;
						if (entry != null && 
								(value = entry.getValue(langIndex)) != null &&
								!value.equals(currentAnn.getValue())) {
							logMessage("TIER: " + tier.getName()
								+ " - ANNOTATION_ID: " + currentAnn.getId()
								+ " !! ANNOTATION_VALUE changed: " + currentAnn.getValue()
								+ " => " + value);
							currentAnn.setValue(value);
							t.setChanged();
						} else if (entry == null) {
							// The entry the External Ref. refers to is no longer
							// there, so remove the CV Entry ID from the annotation
							logMessage("TIER: " + tier.getName()
								+ " - ANNOTATION_ID: " + currentAnn.getId()
								+ " - ANNOTATION_VALUE: " + currentAnn.getValue()
								+ " !! CVE_REF removed: " + currentAnn.getCVEntryId());
							currentAnn.setCVEntryId(null);
							t.setChanged();
						}
					}
				}
			}
		}
	}

	/**
	 * Logs messages to appropriate channel
	 * @param msg a string describing the change
	 */
	private void logMessage(String msg) {
		// TODO Do something more clever with this information
		if(reporter == null) {
			System.out.println(msg);
		} else {
			reporter.report(msg);
		}
	}
	
	/**
	 * Registers an unknown annotation
	 * @param ann
	 */
	private void registerUnknownAnnotation(String ann) {
		if(unknownAnnotations.containsKey(ann)) {
			unknownAnnotations.put(ann, unknownAnnotations.get(ann) + 1);
		} else {
			unknownAnnotations.put(ann, 1);
		}
	}
	
	public void setRecursive(Boolean recursive) {
		this.recursive = recursive;
	}
	
	public void cancel() {
		canceled = true;
	}
	
	/**
	 * @return true if annotation values are never to be changed but only
	 * the reference to a CVEntry will be updated or removed
	 */
	public boolean isAnnotationValuePrecedence() {
		return annotationValuePrecedence;
	}
	
	/**
	 * Sets the flag that determines whether the current annotation value should
	 * take precedence in case of conflicting annotation value and CVEntry idref.
	 * 
	 * The default behavior is that an annotation value is updated if there is
	 * a reference to an CVEntry and that entry has a different value.
	 *  
	 * @param annotationValuePrecedence the new value for this flag
	 */
	public void setAnnotationValuePrecedence(boolean annotationValuePrecedence) {
		this.annotationValuePrecedence = annotationValuePrecedence;
	}
	
	/**
	 * Runs this class in a new JVM (similar to a call from the command line).
	 * This method was used for solving a memory leak. This now prevented by
	 * a call to Preferences.removeDocument(t).
	 * @param lang
	 * @param inputdir
	 * @param outputdir
	 * @param reportfile
	 * @throws Exception
	 */
	@Deprecated
	public static void runInNewJVM(String lang, String inputdir, String outputdir, String reportfile) throws Exception {
		String separator = System.getProperty("file.separator");
		String classpath = System.getProperty("java.class.path");
		String path = System.getProperty("java.home")
	                + separator + "bin" + separator + "java";
		ProcessBuilder processBuilder = 
	                new ProcessBuilder(path, "-Xmx2048m", "-cp", 
	                classpath, 
	                CorpusECVUpdater.class.getName(),
	                "-L" , lang,
	                inputdir,
	                outputdir);
		processBuilder.toString();
		Process process = processBuilder.start();
		
		// Pipe std out
		InputStream in = process.getInputStream();
		int size = 0;
		byte[] buffer = new byte[1024];
		while ((size = in.read(buffer)) != -1) System.out.write(buffer, 0, size);
		
		// Pipe err 
		InputStream error = process.getErrorStream();
		int sizeErr = 0;
		byte[] bufferErr = new byte[1024];
		while ((sizeErr = error.read(bufferErr)) != -1) System.out.write(bufferErr, 0, sizeErr);

		process.waitFor();
		
	}
}

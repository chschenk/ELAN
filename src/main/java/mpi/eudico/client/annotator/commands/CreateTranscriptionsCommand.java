package mpi.eudico.client.annotator.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.ProcessReporter;
import mpi.eudico.server.corpora.util.SimpleReport;

/**
 * A command that creates annotation documents for all media files in a folder 
 * and optionally its sub-folders.
 * 
 * @author Han Sloetjes
 */
public class CreateTranscriptionsCommand implements Command, ProcessReporter {
	private String name;
	private List<Character> delimiters;
	private ProcessReport report;
	private int count = 0;
	
	/**
	 * Constructor.
	 * 
	 * @param name the name of the command
	 */
	public CreateTranscriptionsCommand(String name) {
		this.name = name;
		delimiters = new ArrayList<Character>(4);
		delimiters.add('-');
		delimiters.add('_');
	}

	/**
	 * @param receiver null
	 * @param arguments the arguments:
	 * <ul>
	 * <li>arg[0] = the path of the folder where media files are (String)</li>
	 * <li>arg[1] = the path to a template file or null (String)</li>
	 * <li>arg[2] = the path to an output folder, if null the source folder is the destination folder (String)</li>
	 * <li>arg[3] = flag for recursive processing (Boolean)</li>
	 * <li>arg[4] = flag for specifying to combine videos in a single transcription (Boolean)</li>
	 * <li>arg[5] = flag for specifying whether to combine based on prefixes (true) or suffixes (true) (Boolean)</li>
	 * <li>arg[6] = a character that separates media name and affix (String)</li>
	 * </ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		if (report == null) {
			report = new SimpleReport();
		}
		String sourceFol = (String) arguments[0];
		if (sourceFol == null || sourceFol.length() == 0) {
			String message = "No source folder containing media files specified";
			ClientLogger.LOG.severe(message);
			report(message);
			report("Stopping, no transcriptions created.");
			return;
		}
		String templateFile = (String) arguments[1];
		String destFol = (String) arguments[2];
		Boolean recursive = (Boolean) arguments[3];
		Boolean combineVideos = (Boolean) arguments[4];
		Boolean prefixBased = (Boolean) arguments[5];
		String separator = (String) arguments[6];
		if (separator != null &&separator.length() > 0) {
			delimiters.add(0, separator.charAt(0));
			// add all?
			// remove the default delimiters?
		}
		
		File sourceFolFile = new File(sourceFol);
		try {
			if (!sourceFolFile.exists() || !sourceFolFile.isDirectory()) {
				String message = "The specified source folder does not exist";
				ClientLogger.LOG.severe(message);
				report(message);
				report("Stopping, no transcriptions created.");
				return;
			}
		} catch (Exception ex) {
			String message = "Cannot access the source folder";
			ClientLogger.LOG.severe(message);
			report(message);
			report("Stopping, no transcriptions created.");
			return;
		}
		createTranscriptions(sourceFolFile, templateFile, destFol, recursive, combineVideos, prefixBased);
		report("\nFinished processing: " + count + " EAF files have been created.\n");
	}

	/**
	 * Returns the name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Creates the new transcriptions.
	 * 
	 * @param source the source directory
	 * @param templatePath the path to a template file that is the basis for new files, can be null
	 * @param destinationPath the path to store the transcriptions in, can be null
	 * @param recursive if true, process sub folders as well
	 * @param combineVideos if true video files will be matched on the basis of prefix or suffix.
	 * E.g. AAA_1 and AAA_2 might be in one and the same transcription. If false for each video 
	 * a transcription file will be made.
	 * @param prefixBased if true videos will be combined on the basis of different prefixes,
	 * otherwise on the basis of suffixes. This parameter is only considered if combineVideos is true
	 */
	private void createTranscriptions(File source, String templatePath, String destinationPath, 
			Boolean recursive, Boolean combineVideos, Boolean prefixBased) {
		if (source == null || source.getAbsolutePath().equals(destinationPath)) {
			return;
		}
		File[] files = source.listFiles();
		if (files.length == 0) {
			ClientLogger.LOG.warning("There are no media files in the source folder");
			return;
		}
		
		String templatePrefsPath = null;
		if (templatePath != null) {
			if (templatePath.endsWith(".etf")) {
				templatePrefsPath = templatePath.substring(0, templatePath.length() - 3) + "pfsx";
				File templatePrefsFile = new File (templatePrefsPath);
				if (!templatePrefsFile.exists()) {
					templatePrefsPath = null;
				}
			}
		}
		
		List<File> fileList = new ArrayList<File>();
		List<File> waveList = new ArrayList<File>();
		List<String> exts = new ArrayList<String>(5);
		exts.add(".txt");
		exts.add(".eaf");
		exts.add(".etf");
		exts.add(".pfsx");
		exts.add(".db");
		
		outerloop:
		for (File f : files) {
			if (f.isDirectory()) {
				if (recursive) {
					createTranscriptions(f, templatePath, destinationPath, recursive, combineVideos, prefixBased);
				}
				continue;
			}
			String path = f.getName().toLowerCase();
			if (path.startsWith(".")) {
				// consider as hidden file, don't process
				continue outerloop;
			}
			for (String s : exts) {
				if (path.endsWith(s)) {
					continue outerloop;
				}
			}
			
			if (path.endsWith(".wav")) {
				waveList.add(f);
			} else {
				fileList.add(f);
			}
		}
		// add all wav files to the end of the file list to make sure they are processed in case of audio only transcriptions
		fileList.addAll(waveList);
		
		TranscriptionImpl trans;
		if (templatePath != null) {
			trans = new TranscriptionImpl(templatePath);
		} else {
			trans = new TranscriptionImpl();
            LinguisticType type = new LinguisticType("default-lt");
            TierImpl tier = new TierImpl("default", "", trans, type);
            trans.addLinguisticType(type);
            trans.addTier(tier);
            tier.setDefaultLocale(null);
		}
		
		List<File> processedFiles = new ArrayList<File>();
		
		for (File f : fileList) {
			if (processedFiles.contains(f)) {
				continue;
			}
			
			String path = f.getAbsolutePath();
			int index = path.lastIndexOf('.');
			String withoutExt = null;
			
			if (index > -1) {
				withoutExt = path.substring(0, index);
				
				if ( !waveList.contains(f) ) {
					File wavFile = findWavFile(waveList, withoutExt);
					List<File> otherVideos = null;
					if (combineVideos) {
						otherVideos = findCorVideo(fileList, f, prefixBased);
						if (otherVideos != null && otherVideos.size() > 0) {
							processedFiles.addAll(otherVideos);
						}
					}
					// create media descriptors for original file, wav file and extra video files
					List<MediaDescriptor> mds = new ArrayList<MediaDescriptor>(6);
					MediaDescriptor md1 = MediaDescriptorUtil.createMediaDescriptor(path);
					mds.add(md1);
					MediaDescriptor md = null;
					if (wavFile != null) {
						processedFiles.add(wavFile);
						md = MediaDescriptorUtil.createMediaDescriptor(wavFile.getAbsolutePath());
						md.extractedFrom = md1.mediaURL;
						mds.add(md);
					}
					if (otherVideos != null) {
						for (File ov : otherVideos) {
							if (ov != null) {
								md = MediaDescriptorUtil.createMediaDescriptor(ov.getAbsolutePath());
								mds.add(md);
							}
						}
					}
					trans.setMediaDescriptors(mds);
				} else {// create file for wav only scenario
					//processedFiles.add(f);
					List<MediaDescriptor> mds = new ArrayList<MediaDescriptor>(1);
					MediaDescriptor md = MediaDescriptorUtil.createMediaDescriptor(f.getAbsolutePath());
					mds.add(md);
					trans.setMediaDescriptors(mds);
				}
			} else {
				withoutExt = path;
				// no matching possible, create one media descriptor
				MediaDescriptor md = MediaDescriptorUtil.createMediaDescriptor(path);
				List<MediaDescriptor> mds = new ArrayList<MediaDescriptor>(1);
				mds.add(md);
				trans.setMediaDescriptors(mds);
			}
			// save the transcription, construct path
			boolean saveToSave = false;
			String nextEafPath = null;
			
			if (destinationPath == null) {
				nextEafPath = withoutExt + ".eaf";
				File eafFile = new File(nextEafPath);
				int count = 0;
				
				do {
					try {
						if (!eafFile.exists()) {
							saveToSave = true;
							break;
						} else {
							count++;
							nextEafPath = withoutExt + "-" + count + ".eaf";
							eafFile = new File(nextEafPath);
						}
					} catch (SecurityException se) {
						// can probably break here, next try will also fail?
						count++;
						nextEafPath = withoutExt + "-" + count + ".eaf";
						eafFile = new File(nextEafPath);
					}
				} while (count < 20);
				

			} else {
				String fileName = withoutExt;
				int index2 = fileName.lastIndexOf(File.separatorChar);
				if (index2 > 0) {
					fileName = fileName.substring(index2);
				} else {
					fileName = File.separator + fileName;
				}
				nextEafPath = destinationPath + fileName + ".eaf";
				File eafFile = new File(nextEafPath);
				int count = 0;
				
				do {
					try {
						if (!eafFile.exists()) {
							saveToSave = true;
							break;
						} else {
							count++;
							nextEafPath = destinationPath + fileName + "-" + count + ".eaf";
							eafFile = new File(nextEafPath);
						}
					} catch (SecurityException se) {
						// can probably break here, next try will also fail?
						count++;
						nextEafPath = destinationPath + fileName + "-" + count + ".eaf";
						eafFile = new File(nextEafPath);
					}
				} while (count < 20);

			}
			
			if (saveToSave ) {
				trans.setPathName(nextEafPath);
				// set relative paths
	            String fullEAFURL = FileUtility.pathToURLString(trans.getFullPath());
	            List<MediaDescriptor> mediaDescriptors = trans.getMediaDescriptors();
	            MediaDescriptor md;
	            String relUrl;
	            for (int i = 0; i < mediaDescriptors.size(); i++) {
	                md = mediaDescriptors.get(i);
	                relUrl = FileUtility.getRelativePath(fullEAFURL, md.mediaURL);
	                md.relativeMediaURL = relUrl;
	            }
	            
	            // Make sure this Transcription gets a unique URN
	            trans.createNewURN();
	            
				int saveAsType = SaveAs27Preferences.saveAsType(trans);
	            
				TranscriptionStore store = ACMTranscriptionStore.getCurrentTranscriptionStore();
				try {
					store.storeTranscription(trans, null, null, saveAsType);
					
					if (templatePrefsPath != null) {
						String nextPfsxPath = nextEafPath.substring(0, nextEafPath.length() - 3) + "pfsx";
						try {
							copyPreferences(templatePrefsPath, nextPfsxPath);
						} catch (IOException ioe) {
							
						} catch (Throwable t) {
							ClientLogger.LOG.warning("Cannot copy the preferences file: " + nextPfsxPath + " : " + t.getMessage());
						}
					}
					
					count++;
					report("Created: " + nextEafPath);
				} catch (IOException ioe){
					ClientLogger.LOG.warning("Cannot save a new transcription file: " + nextEafPath);
				}
			} else {
				ClientLogger.LOG.warning("Cannot save a new transcription for file: " + path);
			}
		}
	}

	/**
	 * Finds a wav file for the given video file in the list of wav files.
	 * 
	 * @param waveList list of wav files
	 * @param withoutExt the name of the video without extension
	 * 
	 * @return the wav file or null
	 */
	private File findWavFile(List<File> waveList, String withoutExt) {
		if (waveList == null || withoutExt == null) {
			return null;
		}
		
		for (File f : waveList) {
			String wavFile = f.getAbsolutePath();
			int index = wavFile.lastIndexOf('.');
			if (index > -1) {
				wavFile = wavFile.substring(0, index);
			}
			if (withoutExt.equals(wavFile)) {
				return f;
			}
		}
		
		return null;
	}
	
	/**
	 * Tries to find matching video files for the given first video file.
	 * Based on built in delimiters "-" and "_" or on a character specified by the user.
	 * 
	 * @param fileList the list of remaining video files
	 * @param first the first video file
	 * @param prefixBased if true the comparison is done on the basis of different prefixes, 
	 * otherwise on different suffixes
	 * @return a list of corresponding files or null
	 */
	private List<File> findCorVideo(List<File> fileList, File first, boolean prefixBased) {
		if (first == null || fileList == null) {
			return null;
		}
		
		String trunk = first.getName();
		int index = trunk.lastIndexOf('.');
		if (index > -1) {
			trunk = trunk.substring(0, index);
		}
		
		List<File> result = null;
		String trunk2;
		int index2;
		for (File f : fileList) {
			if (f == first) {
				continue;
			}
			trunk2 = f.getName();
			index2 = trunk2.lastIndexOf('.');
			if (index2 > -1) {
				trunk2 = trunk2.substring(0, index2);
			}
			if (prefixBased) {
				// loop over the delimiters list
				for (Character ch : delimiters) {
					int hindex = trunk.indexOf(ch);
					int hindex2 = trunk2.indexOf(ch);
					if (hindex > -1 && hindex2 > -1) {				
						String sub1 = trunk.substring(hindex);
						String sub2 = trunk2.substring(hindex2);
						if (sub1.length() != 0 && sub2.length() != 0 && sub1.equals(sub2)) {
							if (result == null) {
								result = new ArrayList<File>(6);
							}
							result.add(f);
							break;
						}
					}
				}
			} else {// suffix based, different suffix
				for (Character ch : delimiters) {
					int hindex = trunk.lastIndexOf(ch);
					int hindex2 = trunk2.lastIndexOf(ch);
					if (hindex > -1 && hindex2 > -1) {					
						String sub1 = trunk.substring(0, hindex);
						String sub2 = trunk2.substring(0, hindex2);
						if (sub1.length() != 0 && sub2.length() != 0 && sub1.equals(sub2)) {
							if (result == null) {
								result = new ArrayList<File>(6);
							}
							result.add(f);
							continue;
						}
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Copies the preferences file of a template to the destination folder, one for each created eaf
	 * file.
	 * @param fromPath the path to the preferences file
	 * @param toPath the path to copy the preferences to
	 * @throws IOException if the destination file can not be created or the source is not found
	 * @throws Exception can throw other exceptions when copying  
	 */
	private void copyPreferences(String fromPath, String toPath) throws IOException, Exception {
		if (fromPath == null) {
			throw new NullPointerException("Input file is null");
		}
		if (toPath == null) {
			throw new NullPointerException("Output file is null");
		}
		
		File fromFile = new File(fromPath);
		File toFile = new File(toPath);
		
		if (!fromFile.exists()) {
			throw new FileNotFoundException("The preferences file cannot be found.");
		}
		
		if (!toFile.exists()) {
			boolean create = toFile.createNewFile();
			if (!create) {
				throw new IOException("Cannot create the destination preference file.");
			}
		}// else simply overwrite
		
		FileUtility.copyToFile(fromFile, toFile);
	}

	/**
	 * Returns the report, can be null.
	 */
	@Override
	public ProcessReport getProcessReport() {
		return report;
	}
	
	/**
	 * Adds a string to the report if it exists.
	 */
	@Override
	public void report(String message) {
		if (report != null) {
			report.append(message);
		}		
	}

	/**
	 * Sets the report object.
	 * @param report the new report, can be null
	 */
	@Override
	public void setProcessReport(ProcessReport report) {
		this.report = report;
	}
}

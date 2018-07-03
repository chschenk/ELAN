package mpi.eudico.client.annotator.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.util.ProcessReport;

/**
 * A command to clip several segments from one or more media files.
 * 
 * @author Han Sloetjes
 *
 */
public class ClipMediaMultiCommand extends ClipMediaCommand {
	/**
	 * Constructor
	 * @param theName the name of the command
	 */
	public ClipMediaMultiCommand(String theName) {
		super(theName);
	}

	/**
	 * @param receiver the transcription
	 * @param arguments the arguments for the command
	 * <ul>
	 * <li>arguments[0] = the executable (String)
	 * <li>arguments[1] = the command part of the script (String)
	 * <li>arguments[2] = either a List of segments (arrays of long of size 2 (begin time, end time))
	 * or a List of tier names (String) from which to extract segments<br>
	 * <li>arguments[3] = output directory (String)
	 * <li>arguments[4] = report object, optional (Report)
	 * </ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		// receiver Transcription
		TranscriptionImpl transcription = (TranscriptionImpl) receiver;
		if (arguments.length >= 5) {
			report = (ProcessReport) arguments[4];
		}
		
		if (transcription.getMediaDescriptors().size() == 0) {
			ClientLogger.LOG.warning("No media descriptors in the transcription, nothing to clip");
			if (report != null) {
				report.append("No media descriptors in the transcription, nothing to clip");
			}
			return;
		}

		String executable = (String) arguments[0];
		if (executable == null) {
			// log? has been checked before
			return;
		}
		String command = (String) arguments[1];
		if (command == null) {
			// log? has been checked before
			return;
		}
		
		// arguments[2] either list of tier names or a list of long arrays, each array [bt, et]
		List<long[]> segmentList = null;
		
		if (arguments[2] instanceof List) {
			List<?> arg2 = (List<?>) arguments[2];
			if (arg2.size() > 0) {
				Object first = arg2.get(0);
				if (first instanceof long[]) {
					// segments, use the List directly
					segmentList = (List<long[]>) arg2;
				} else if (first instanceof String) {
					// tier names, extract segments using the names in the list
					segmentList = extractSegments(transcription, (List<String>) arg2);
				}
			}
		}
		
		if (segmentList == null || segmentList.size() == 0) {
			// log? has been checked before
			return;
		}
		
		String outputFolder = (String) arguments[3];
		
		unattendedMode = true;
		// use some global settings
		boolean masterMediaOnly = false;
		
		Boolean mmOnlyObj = Preferences.getBool("Media.OnlyClipFirstMediaFile", transcription);
		if (mmOnlyObj != null) {
			masterMediaOnly = mmOnlyObj;
		}
		// clipping in parallel only in case there are multiple media descriptors in the transcription
		Boolean runAsync = Preferences.getBool("Media.ClipInParallel", transcription);
		if (runAsync != null) {
			asynchronous = runAsync; 
		}

		Map<String, Long> medMap = new HashMap<String, Long>(transcription.getMediaDescriptors().size());
		
		for (int i = 0; i < transcription.getMediaDescriptors().size(); i++) {
			if (i > 0 && masterMediaOnly) {
				break;
			}
			MediaDescriptor md = transcription.getMediaDescriptors().get(i);
			String medUrl = processSourceFileName(md.mediaURL);
			medMap.put(medUrl, md.timeOrigin);
		}
		// all clips as one bunch, or make and process a list per media descriptor?
		List<MediaClipper> clippingList = new ArrayList<MediaClipper>();
		
		for (Map.Entry<String, Long> e : medMap.entrySet()) {
			String mediaSource = e.getKey();
			long offset = e.getValue();

			if (report != null) {
				report.append("Clipping " + segmentList.size() + " segments from " + mediaSource);
			}
			
			String rawOutputName = outputFolder + File.separator + FileUtility.fileNameFromPath(mediaSource);
			
			String completeOutputName;
			long[] segment;
			
			for (int i = 0; i < segmentList.size(); i++) {
				segment = segmentList.get(i);
				completeOutputName = createDestinationName(rawOutputName, segment[0] + offset, segment[1] + offset);
				
				// begin and end time should be known, fill in the relevant parts of the script
				List<String> defCommand = processCommand(command, mediaSource, completeOutputName, segment[0] + offset, segment[1] + offset);
				defCommand.add(0, executable);
		
				clippingList.add(new MediaClipper(defCommand));
			}			
		}
		
		ClipRunner cRun = new ClipRunner(clippingList);
		cRun.start();
		
		try {
			cRun.join();
			if (cRun.getErrorMessage() != null) {
				if (unattendedMode) {
					ClientLogger.LOG.warning(cRun.getErrorMessage() + "(" + cRun.numErrors + ")");
				}
				if (report != null) {
					report.append("Errors occurred: " + cRun.getErrorMessage() + "(" + cRun.numErrors + ")");
				}
			} 
		} catch (InterruptedException ie) {
			ClientLogger.LOG.warning(ElanLocale.getString("Message.Error") + ": " + ie.getMessage());
		}

	}

	/**
	 * Extracts time segments from (the annotations of) the given set of tiers.
	 * 
	 * @param transcription the transcription
	 * @param tierNames the set of tiers to process
	 * @return a list of long arrays
	 */
	private List<long[]> extractSegments(TranscriptionImpl transcription, List<String> tierNames) {
		if (transcription != null && tierNames != null && tierNames.size() > 0) {
			List<long[]> segments = new ArrayList<long[]>();
			TierImpl t;
			Annotation a;
			
			for (String name : tierNames) {
				t = transcription.getTierWithId(name);
				if (t != null) {
					List<AbstractAnnotation> annotations = t.getAnnotations();
					final int size = annotations.size();
					for (int i = 0; i < size; i++) {
						a = annotations.get(i);
						// could check for uniqueness, see if the same segment is already in the list?
						segments.add( new long[]{a.getBeginTimeBoundary(), a.getEndTimeBoundary()});
					}
				}
			}
			
			return segments;
		}
		
		return null;
	}
	
	
}

/*
 * Created on Oct 15, 2004
 */
package mpi.eudico.server.corpora.clomimpl.shoebox;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clom.AnnotationDocEncoder;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.shoebox.interlinear.Interlinearizer;
import mpi.eudico.server.corpora.clomimpl.shoebox.interlinear.TimeCodedTranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.shoebox.utr22.SimpleConverter;


/**
 * Encodes information from a Transcription to Shoebox/Toolbox format and stores it.
 * 
 * @author hennie
 */
public class ShoeboxEncoder implements AnnotationDocEncoder {
	public static final String defaultDBType = "ElanExport";
	
	public static final String elanELANLabel = "\\ELANExport";
	public static final String elanBlockStart = "\\block";
	public static final String elanBeginLabel = "\\ELANBegin";
	public static final String elanEndLabel = "\\ELANEnd";
	public static final String elanParticipantLabel = "\\ELANParticipant";
	public static final String elanMediaURLLabel = "\\ELANMediaURL";
	public static final String elanMediaExtractedLabel = "\\ELANMediaExtracted";
	public static final String elanMediaMIMELabel = "\\ELANMediaMIME";
	public static final String elanMediaOriginLabel = "\\ELANMediaOrigin";
	
	private OutputStreamWriter isoLatinWriter;
	private OutputStreamWriter utf8Writer;
	
	private SimpleConverter simpleConverter;

	public ShoeboxEncoder(String path) {
		try {
			FileOutputStream out = new FileOutputStream(path);
		
			isoLatinWriter = new OutputStreamWriter(out, "ISO-8859-1");
			utf8Writer = new OutputStreamWriter(out, "UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * @see mpi.eudico.server.corpora.clom.AnnotationDocEncoder#encodeAndSave(mpi.eudico.server.corpora.clom.Transcription, mpi.eudico.server.corpora.clom.EncoderInfo, java.util.List, java.lang.String)
	 */
	@Override
	public void encodeAndSave (
		Transcription theTranscription,
		EncoderInfo encoderInfo,
		List<TierImpl> tierOrder,
		String path) throws IOException {
			
		//try {
			writeHeader(theTranscription, encoderInfo);
			writeBlocks(theTranscription, tierOrder, encoderInfo);
			
			// media descriptors are written at the end of the Toolbox file. When written at begin/in header
			// Toolbox throws them away without any notification.
			writeMediaDescriptors(theTranscription);	
			closeFile();
							
		//} catch (Exception e) {
		//	String txt = "Sorry: unable to export this file to Shoebox." +
	     //	e.getMessage() + ")";
		//	JOptionPane.showMessageDialog(null, txt, txt,
		//		JOptionPane.ERROR_MESSAGE);
		//	e.printStackTrace();
		//}				
	}
	
	private void writeHeader(Transcription theTranscription, EncoderInfo encoderInfo) {
		String dbType = ((ToolboxEncoderInfo) encoderInfo).getDatabaseType();
		if (	dbType == null || 
				dbType.equals("") /*||
				((ToolboxEncoderInfo) encoderInfo).getMarkerSource() == ToolboxEncoderInfo.TIERNAMES*/) {

			dbType = defaultDBType;
		}
		
		write("\\_sh v3.0  400  " + dbType + "\n");
		write("\\_DateStampHasFourDigitYear\n");				
		
		if (((ToolboxEncoderInfo) encoderInfo).getMarkerSource() == ToolboxEncoderInfo.TIERNAMES) {
			write(("\n" + elanELANLabel + "\n"));
		}
	}
	
	private void writeMediaDescriptors(Transcription theTranscription) {
		// media descriptors
		List<MediaDescriptor> mediaDescriptors = null;
		
		try {
			mediaDescriptors = theTranscription.getMediaDescriptors();
		} catch (Exception rex) {
			rex.printStackTrace();
		}
		
		for (int i = 0; i < mediaDescriptors.size(); i++) {
			write("\n");
				
			MediaDescriptor md = mediaDescriptors.get(i);
			if (md.mediaURL != null && !md.mediaURL.equals("")) {
				write((elanMediaURLLabel + " " + md.mediaURL + "\n"));
			}
			if (md.mimeType != null && !md.mimeType.equals("")) {
				write((elanMediaMIMELabel + " " + md.mimeType + "\n"));
			}
			if (md.timeOrigin != 0) {
				write((elanMediaOriginLabel + " " + md.timeOrigin + "\n"));
			}
			if (md.extractedFrom != null && !md.extractedFrom.equals("")) {
				write((elanMediaExtractedLabel + " " + md.extractedFrom + "\n"));
			}
		}
	}
	
	/**
	 * Jul 2005: added a test on the toplevel tiers:<br>
	 *  - if there is only one tier use it as the RecordMarker
	 *  - if all top level tiernames start with "xxxx@" use "xxxx" as the RecordMarker
	 * 
	 * in both cases don't add the ELAN "block" marker as RecordMarker
	 * 
	 * @param theTranscription the transcription to export
	 * @param tierOrder the order of the tiers
	 * @param encoderInfo info for the encoder
	 */
	private void writeBlocks(Transcription theTranscription, List<TierImpl> tierOrder, EncoderInfo encoderInfo) {
		boolean lineForRootAnnot = false;
		boolean justOneRoot = false;
		int blockCounter = 1;
		
		Interlinearizer interlinearizer = new Interlinearizer(
							new TimeCodedTranscriptionImpl((TranscriptionImpl) theTranscription));
							
		setShoeboxArguments(theTranscription, interlinearizer, tierOrder, encoderInfo);
		
		String[] outputLines = interlinearizer.renderAsText();
		
		// find set of root tier names, for each root tier store participant
		String participantString = "";
		Map<String, String> rootTierNames = new HashMap<String, String>();		
		try {
			List<TierImpl> topTiers = ((TranscriptionImpl)theTranscription).getTopTiers();
			
			Iterator<TierImpl> tierIter = topTiers.iterator();
			while (tierIter.hasNext()) {
				TierImpl t = tierIter.next();
				rootTierNames.put(t.getName(), t.getParticipant());
			}
			
			if (topTiers.size() == 1) {
				justOneRoot = true;
			} else {
				// loop over toptiers; if all have a '@' in their name and the prefix
				// is always the same, use this prefix as the record marker
				justOneRoot = true;
				String name;
				String prefix = null;
				int atIndex = -1;
				Iterator<String> it = rootTierNames.keySet().iterator();
				while (it.hasNext()) {
					name = it.next();
					atIndex = name.indexOf('@');
					if (atIndex < 1) {
						justOneRoot = false;
						break;
					} else {
						String curPref = name.substring(0, atIndex);
						//System.out.println("Pref: " + curPref);
						if (prefix == null) {
							prefix = curPref;
						} else if (!prefix.equals(curPref)) {
							justOneRoot = false;
							break;
						}
					}
				}
			}

		
			for (int i = 0; i < outputLines.length; i++) {
				// add tier labels
				String tierLabel = interlinearizer.getMetrics().getTierLabelAt(i);
				if (tierLabel == null) {
					tierLabel = "";
				}
				if (rootTierNames.get(tierLabel) != null) {
					participantString = rootTierNames.get(tierLabel);
					lineForRootAnnot = true;
				}
				if (!tierLabel.startsWith("\\") && !tierLabel.equals("")) {
					tierLabel = "\\" + tierLabel;		
				}
				if (tierLabel.indexOf("@") > 0) {
					tierLabel = tierLabel.substring(0, tierLabel.indexOf("@"));
				}
				if (tierLabel.indexOf(" ") >= 0) {	// substitute spaces
					tierLabel = tierLabel.replace(' ', '_');
				}
				if (tierLabel.startsWith("\\TC")) {		// time code, subsitute with 2 lines
					String beginString = outputLines[i].substring(0, outputLines[i].indexOf("-")).trim();
					String endString = outputLines[i].substring(outputLines[i].indexOf("-") + 1).trim();
					
					// only write aligned time slots
					if (!beginString.startsWith(Interlinearizer.UNALIGNED_SSMS)) {
						write((elanBeginLabel + " " + beginString + "\n"));
					}
					if (!endString.startsWith(Interlinearizer.UNALIGNED_SSMS)) {
						write((elanEndLabel + " " + endString + "\n"));
					}					
					if (participantString != null && !participantString.equals("")) {
						write((elanParticipantLabel + " " + participantString + "\n"));
					}
				}
				else {
					// get character encoding for outputLine i
					int charEncoding = interlinearizer.getCharEncoding(
								interlinearizer.getMetrics().getTierLabelAt(i));
								
					String encodingString = "ISO-8859-1";
					if (charEncoding == Interlinearizer.UTF8) {
						encodingString = "UTF-8";
					}
					else if (charEncoding == Interlinearizer.SIL) {
						encodingString = "SIL-IPA93";
					}
					
					if (lineForRootAnnot) {
						if (!justOneRoot) {
							DecimalFormat df = new DecimalFormat("#000");
							String cntString = df.format(blockCounter++);
	
							write(elanBlockStart + " " + cntString +"\n");
						}
						lineForRootAnnot = false;
					}
					write(tierLabel + " " + outputLines[i].replace('\n', ' ') + "\n", encodingString);
				}		
			}
		} catch (Exception rex) {
			rex.printStackTrace();					
		}
	}
	
	private void setShoeboxArguments(	Transcription transcription, 
										Interlinearizer interlinearizer,
										List<TierImpl> tierOrder,
										EncoderInfo encoderInfo) {
		//String[] visTiers = new String[tierOrder.size()];
		//tierOrder.copyInto(visTiers);
		//String[] visTiers = (String[]) tierOrder.toArray(new String[]{});
		String[] visTiers = new String[tierOrder.size()];
		for (int i = 0; i < visTiers.length; i++) {
			visTiers[i] = tierOrder.get(i).getName();
		}
		
		int width = 80;
		int timeFormat = Interlinearizer.SSMS;
		if (encoderInfo != null) {
			width = ((ToolboxEncoderInfo) encoderInfo).getPageWidth();
			timeFormat = ((ToolboxEncoderInfo) encoderInfo).getTimeFormat();
		}
																						
		interlinearizer.setVisibleTiers(visTiers);
		interlinearizer.setAlignmentUnit(Interlinearizer.BYTES);
		interlinearizer.setBlockWrapStyle(Interlinearizer.EACH_BLOCK);
		interlinearizer.setWidth(width);
		interlinearizer.setBlockSpacing(1);
		interlinearizer.setTierLabelsShown(false);
		interlinearizer.setTimeCodeShown(true);
		interlinearizer.setTimeCodeType(timeFormat);
		interlinearizer.setCorrectAnnotationTimes(((ToolboxEncoderInfo) encoderInfo).getCorrectAnnotationTimes());
		//interlinearizer.setEmptyLineStyle(Interlinearizer.TEMPLATE);
		
		// set default char encodings to ISO-Latin
		if (((ToolboxEncoderInfo) encoderInfo).getMarkerSource() == ToolboxEncoderInfo.TIERNAMES) {
			setDefaultCharEncodings(transcription, interlinearizer, Interlinearizer.UTF8);	
		} else if (((ToolboxEncoderInfo) encoderInfo).getMarkerSource() == ToolboxEncoderInfo.TYPFILE) {
		    // typfile 
		    if (((ToolboxEncoderInfo) encoderInfo).isAllUnicode()) {
		        setDefaultCharEncodings(transcription, interlinearizer, Interlinearizer.UTF8);
		    } else {
		        setDefaultCharEncodings(transcription, interlinearizer, Interlinearizer.ISOLATIN);
		    }
		}
		else {	// markers
			setDefaultCharEncodings(transcription, interlinearizer, Interlinearizer.ISOLATIN);
		
			// iterate over markers, set to UTF-8 or SIL where necessary
			List<MarkerRecord> markers = ((ToolboxEncoderInfo) encoderInfo).getMarkers();
			if (markers != null) {
				for (int i = 0; i < markers.size(); i++) {
					MarkerRecord mkrRec = (MarkerRecord) markers.get(i);
					
					// find all tiers starting with the same prefix; crucial in case there 
					// is a tiergroup per participant
					List<String> matchingTiers = getMatchingTiers(transcription, mkrRec.getMarker());
					String name;
					for (int j = 0; j < matchingTiers.size(); j++) {
						name = matchingTiers.get(j);
						
						if (!"".equals(name)) {
							if (mkrRec.getCharset() == MarkerRecord.ISOLATIN) {
								interlinearizer.setCharEncoding(name, Interlinearizer.ISOLATIN);
							}
							else if (mkrRec.getCharset() == MarkerRecord.UTF8) {
								interlinearizer.setCharEncoding(name, Interlinearizer.UTF8);
							}
							else if (mkrRec.getCharset() == MarkerRecord.SILIPA) {
								interlinearizer.setCharEncoding(name, Interlinearizer.SIL);
							}
						}
					}
				}
			}
		}
	}
	
	private void setDefaultCharEncodings(Transcription transcription, Interlinearizer interlinearizer, int encoding) {

		List<? extends Tier> tiers = transcription.getTiers();
		for (int i = 0; i < tiers.size(); i++) {
			String tierName = tiers.get(i).getName();
			interlinearizer.setCharEncoding(tierName, encoding);
		}
		
	}

	/**
	 * Returns all tiers that start with this marker and that have a "@" character 
	 * following the marker.
	 * 
	 * @param transcription the transcription
	 * @param marker the marker name or label
	 * 
	 * @return a list of tiers
	 */
	private List<String> getMatchingTiers(Transcription transcription, String marker) {
		List<String> tierList = new ArrayList<String>(5);
		
		List<? extends Tier> tiers = transcription.getTiers();
		for (int i = 0; i < tiers.size(); i++) {
			String tierName = tiers.get(i).getName();
			if (tierName.startsWith(marker)) {
				if (tierName.length() == marker.length() || 
						tierName.indexOf('@') == marker.length())
				tierList.add(tierName);
			}
		}
		
		return tierList;
	}
	
	/** 
	 * Write using ISO-8859-1 (ISO-LATIN) char set
	 * @param string
	 */
	private void write(String string) {
		write(string, "ISO-8859-1");
	}
	
	private void write(String string, String charsetName) {
		OutputStreamWriter osw = isoLatinWriter;
		try {
			if (charsetName.equals("SIL-IPA93")) {
				if (simpleConverter == null) {
					simpleConverter = new SimpleConverter(null);
				}
				string = simpleConverter.toBinary(string);
			}
			else if (charsetName.equals("UTF-8")) {
				osw = utf8Writer;
			}

			osw.write(string);
			osw.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Try to close the file writers, catching and ignoring any exception.
	 */
	private void closeFile() {
		try {
			isoLatinWriter.flush();
			utf8Writer.flush();
			isoLatinWriter.close();
			utf8Writer.close();
		} catch (Exception ex) {
			
		}
	}
}

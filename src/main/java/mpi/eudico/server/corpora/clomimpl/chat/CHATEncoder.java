/*
 * Created on Oct 15, 2004
 */
package mpi.eudico.server.corpora.clomimpl.chat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationDocEncoder;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Encodes information from a Transcription to CHAT (UTF-8) format and stores it.
 * 
 * @author hennie
 */
public class CHATEncoder implements AnnotationDocEncoder {
	private static String BEGIN_LABEL = "@Begin";
	private static String END_LABEL = "@End";
	private static String LANGUAGE_LABEL = "@Languages:\t";
	private static String PARTICIPANTS_LABEL = "@Participants:\t";
	private static String ID_LABEL = "@ID:\t";
	private static String SOUND_LABEL = "%snd:";
	private static String VIDEO_LABEL = "%mov:";
	private static String LANG_LABEL = "%lan:";
	private final char BULLET = '\u0015';
	
	private long lastAlignedBeginTime = 0;
	private String[][] mainTierInfo;
	private String[][] dependentTierInfo;

	/*
	 * @see mpi.eudico.server.corpora.clom.AnnotationDocEncoder#encodeAndSave(mpi.eudico.server.corpora.clom.Transcription, mpi.eudico.server.corpora.clom.EncoderInfo, java.util.List, java.lang.String)
	 */
	@Override
	public void encodeAndSave(
		Transcription theTranscription,
		EncoderInfo encoderInfo,
		List<TierImpl> tierOrder,
		String path) throws IOException{
		
		mainTierInfo = ((CHATEncoderInfo) encoderInfo).getMainTierInfo();
		dependentTierInfo = ((CHATEncoderInfo) encoderInfo).getDependentTierInfo();
		
		//try {
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");

			out.write("@UTF8\n");
			out.write(BEGIN_LABEL + "\n");
			
			writeHeader(theTranscription, encoderInfo, out);
			writeBlocks(theTranscription, encoderInfo, out);
			
			out.write(END_LABEL + "\n");
			
			out.close();
				
		/*} catch (Exception e) {
			String txt = "Sorry: unable to export this file to CHAT. (" +
				e.getMessage() + ")";
			JOptionPane.showMessageDialog(null, txt, txt,
				JOptionPane.ERROR_MESSAGE);				
		}*/				
	}
	
	private void writeHeader(Transcription theTranscription, EncoderInfo encoderInfo, OutputStreamWriter out) {
		// @Languages
		try {
			String langString = LANGUAGE_LABEL;
			Set<String> languages = new HashSet<String>();
			// HS 04-2008: this list should also contain what has been entered in the Language fields
			// of the export UI?
			List<? extends Tier> tiers = theTranscription.getTiers();
			if (tiers != null) {
				for (Tier t : tiers) {
					if (t instanceof TierImpl) {
						Locale loc = ((TierImpl)t).getDefaultLocale();
						if (loc != null) {
							languages.add(loc.getLanguage());
						}
					}
				}
			}
			// add languages from the input fields
			for (int i = 0; i < mainTierInfo[5].length; i++) {
				if (mainTierInfo[5][i] != null && mainTierInfo[5][i].length() > 0) {
					languages.add(mainTierInfo[5][i]);
				}
			}
			
			int j = 0;
			Iterator<String> langIter = languages.iterator();
			while (langIter.hasNext()) {
				if (j > 0) {
					langString += ", ";
				}
				langString += langIter.next();
				j++;
			}
			
			if (langString != LANGUAGE_LABEL) {
				out.write(langString + "\n");
			}
					
			// @Participants
			String participantsString = PARTICIPANTS_LABEL;
			
			//String[][] mainTierInfo = ((CHATEncoderInfo) encoderInfo).getMainTierInfo();
			
			for (int i = 0; i < mainTierInfo[1].length; i++) {
				if (mainTierInfo[1][i] == null) {
					continue;
				}
								
				if (i > 0) {
					participantsString += ", ";
				}

				participantsString += (mainTierInfo[1][i]).substring(1);
										
				if (!mainTierInfo[2][i].equals("")) {
					participantsString += " " + mainTierInfo[2][i];
				}
				if (!mainTierInfo[3][i].equals("")) {
					participantsString += " " + mainTierInfo[3][i];
				}
				else {
					participantsString += " " + "Unidentified";
				}
			}
			
			if (participantsString != PARTICIPANTS_LABEL) {
				out.write(participantsString + "\n");
			}
			
			// @ID lines, one for each participant
			for (int i = 0; i < mainTierInfo[4].length; i++) {
				if (mainTierInfo[4][i] == null) {
					continue;
				}
				
				if (!mainTierInfo[4][i].equals("")) {
					String idString = ID_LABEL + mainTierInfo[4][i];
					out.write(idString + "\n");
				}
				else {
					String idString = ID_LABEL + mainTierInfo[5][i] + "|" 
										+ mainTierInfo[1][i].substring(1) + "|||||" 
										+ "Unidentified" + "||";	
					out.write(idString + "\n");			
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
			
	}
	
	private void writeBlocks(Transcription theTranscription, EncoderInfo encoderInfo, OutputStreamWriter out) {
		List<Annotation> rootAnnotations = new ArrayList<Annotation>();
		
		// iterate over top tiers, over annotations
		try {
			List<TierImpl> topTiers = ((TranscriptionImpl) theTranscription).getTopTiers();
			
			Iterator<TierImpl> tierIter = topTiers.iterator();
			while (tierIter.hasNext()) {
				TierImpl t = tierIter.next();
				rootAnnotations.addAll(t.getAnnotations());
			}			
		
			Collections.sort(rootAnnotations);
		
			Iterator<Annotation> annotIter = rootAnnotations.iterator();
			while (annotIter.hasNext()) {
				Annotation ann = annotIter.next();	
				String blockString = getBlock(theTranscription, encoderInfo, ann);	
				
				out.write(blockString);
			}
		} catch (Exception rex) {
			rex.printStackTrace();
		}		
	}
	
	private String getBlock(Transcription tr, EncoderInfo encoderInfo, Annotation ann) {
		boolean exportBlock = false;
		
		StringBuilder blockString = new StringBuilder("");		
		
		// create main tier string
		// find label from mainTierInfo
		String tierName = ann.getTier().getName();
		int tierIndex = -1;
		for (int i = 0; i < mainTierInfo[0].length; i++) {
			if (mainTierInfo[0][i] == null) {
				continue;
			}
			
			if (mainTierInfo[0][i].equals(tierName)) {
				//blockString += mainTierInfo[1][i] + ":\t" + ann.getValue().replaceAll("\t", "\n" + "\t") + "\n";
			    blockString.append(mainTierInfo[1][i] + ":\t" + ann.getValue().replaceAll("\t", "\n" + "\t"));
				exportBlock = true;
				tierIndex = i;
				break;
			}
		}
		
		if (!exportBlock) {
			return blockString.toString();
		}
		
		// HS may 2006: add the media filename and begin and end times either on the same line (special formatting)
		// or at the next line following the root annotation
		// take unaligned slots of root annots together. One time link line can refer to more than
		// one preceding block
		// HS may 2006: we could still assume there are no unaligned slots/annotations on a root tier
		if (((AlignableAnnotation) ann).getBegin().isTimeAligned()) {
			lastAlignedBeginTime = ((AlignableAnnotation) ann).getBegin().getTime();
			if (((CHATEncoderInfo) encoderInfo).getCorrectAnnotationTimes()) {
			    lastAlignedBeginTime += ((CHATEncoderInfo) encoderInfo).getMediaOffset();
			}
		}
		
		if (((AlignableAnnotation) ann).getEnd().isTimeAligned()) {
			long endTime = ann.getEndTimeBoundary();
			
			if(((CHATEncoderInfo) encoderInfo).getCorrectAnnotationTimes()) {
			    endTime += ((CHATEncoderInfo) encoderInfo).getMediaOffset();
			}
			String mediaFileName = "";
			
			List<MediaDescriptor> mediaDescriptors = tr.getMediaDescriptors();
			String mediaLabel = SOUND_LABEL;
			
			if (mediaDescriptors != null && mediaDescriptors.size() > 0) {
				mediaFileName = mediaDescriptors.get(0).mediaURL;
				mediaFileName = mediaFileName.substring(mediaFileName.lastIndexOf("/") + 1);
				
				if (!((CHATEncoderInfo) encoderInfo).isTimesOnSeparateLine()) {
				    int index = mediaFileName.indexOf('.'); 
				    if (index > 0) {
				        mediaFileName = mediaFileName.substring(0, index);
				    }
				}
				
				String mimeType = mediaDescriptors.get(0).mimeType;
				if (mimeType.startsWith("video")) {
					mediaLabel = VIDEO_LABEL;
				}
				
				if (((CHATEncoderInfo) encoderInfo).isTimesOnSeparateLine()) {
				    blockString.append("\n" + BULLET + mediaLabel + "\t" + "\"" + mediaFileName + "\" " 
											+ lastAlignedBeginTime + " " 
											+ endTime + BULLET + "\n");
				} else {
				    blockString.append(" " + BULLET + mediaLabel + "\"" + mediaFileName + "\"" +
				            "_" + lastAlignedBeginTime + "_" + endTime + BULLET + "\n");
				}
			}
		}
		// HS Apr 2008: add a %lan line if necessary
		if (((CHATEncoderInfo) encoderInfo).isIncludeLangLine() && tierIndex > -1 
				&& mainTierInfo[5][tierIndex] != null && mainTierInfo[5][tierIndex].length() > 0) {
			blockString.append(LANG_LABEL + "\t$");
			blockString.append(mainTierInfo[5][tierIndex] + "\n");
		}
		
		// recursively add annotations for dependent tiers
		Map<Tier, List<Annotation>> annotsPerTier = new HashMap<Tier, List<Annotation>>();
		List<Tier> tierOrder = new ArrayList<Tier>();
		
		getDependentLines(ann, tr, annotsPerTier, tierOrder);
		
		// compose output string for dependent annotations
		//String dependentString = "";
		
		for (int i = 0; i < tierOrder.size(); i++) {
			Tier t = tierOrder.get(i);
			List<Annotation> annots = annotsPerTier.get(t);
			
			boolean exportTier = false;
			
			if (annots != null) {
				Collections.sort(annots);
				
				// label
				for (int j = 0; j < dependentTierInfo[0].length; j++) {	
					if (dependentTierInfo[0][j] == null) {
						continue;
					}
									

					if (dependentTierInfo[0][j].equals(t.getName())) {
						blockString.append(dependentTierInfo[1][j] + ":\t");
						exportTier = true;
						break;
					}

				}
				
				// concatenate annotation values
				if (exportTier) {
					for (int k = 0; k < annots.size(); k++) {
						if (k != 0) {
							blockString.append(" ");
						}
						
						blockString.append( annots.get(k).getValue().replaceAll("\t", "\n\t") );
					}
					blockString.append("\n");
				}
			}
		}
		
		// add time information.
		// take unaligned slots of root annots together. One time link line can refer to more than
		// one preceding block
		/*
		if (((AlignableAnnotation) ann).getBegin().isTimeAligned()) {
			lastAlignedBeginTime = ((AlignableAnnotation) ann).getBegin().getTime();
		}
		
		if (((AlignableAnnotation) ann).getEnd().isTimeAligned()) {
			
			String mediaFileName = "";
			
			Vector mediaDescriptors = tr.getMediaDescriptors();

			
			String mediaLabel = SOUND_LABEL;
			
			if (mediaDescriptors != null && mediaDescriptors.size() > 0) {
				mediaFileName = ((MediaDescriptor) mediaDescriptors.firstElement()).mediaURL;
				mediaFileName = mediaFileName.substring(mediaFileName.lastIndexOf("/") + 1);
				
				String mimeType = ((MediaDescriptor) mediaDescriptors.firstElement()).mimeType;
				if (mimeType.startsWith("video")) {
					mediaLabel = VIDEO_LABEL;
				}
				
				blockString.append( BULLET + mediaLabel + "\t" + "\"" + mediaFileName + "\" " 
											+ lastAlignedBeginTime + " " 
											+ ann.getEndTimeBoundary() + BULLET + "\n");
			}		
		}
		*/
		return blockString.toString();
	}
	
	private void getDependentLines(Annotation ann, Transcription tr, 
								Map<Tier, List<Annotation>> annotsPerTier, List<Tier> tierOrder) {
		
		List<? extends Annotation> childAnnots = tr.getChildAnnotationsOf(ann);
		
		// collect dependent annots per tier
		List<Annotation> annots = null;
		
		for (Annotation child  : childAnnots) {
			annots = annotsPerTier.get(child.getTier());
			if (annots == null) {
				annots = new ArrayList<Annotation>();
				annotsPerTier.put(child.getTier(), annots);
				
				tierOrder.add(child.getTier());			
			}
			annots.add(child);
			
			getDependentLines(child, tr, annotsPerTier, tierOrder);
		}	
	}
}

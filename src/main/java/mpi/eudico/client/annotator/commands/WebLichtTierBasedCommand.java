package mpi.eudico.client.annotator.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.webserviceclient.weblicht.TCFConstants;
import mpi.eudico.webserviceclient.weblicht.TCFtoTranscription;
import mpi.eudico.webserviceclient.weblicht.TiersToTCF;
import mpi.eudico.webserviceclient.weblicht.WLServiceDescriptor;
import mpi.eudico.webserviceclient.weblicht.WebLichtWsClient;
/**
 * A command that calls a WebLicht service with sentence and (therefore) token input. 
 */
public class WebLichtTierBasedCommand extends AbstractProgressCommand implements
		UndoableCommand {
	private MergeTranscriptionsByAddingCommand mergeCommand;
	private TranscriptionImpl transcription;
	private WLServiceDescriptor wlDescriptor;
	private TiersToTCF tiersToTCF;
	private String tierName;
	private String contentType; // sentence or token (word) level of input
	
	/**
	 * Constructor.
	 * @param theName
	 */
	public WebLichtTierBasedCommand(String theName) {
		super(theName);
	}

	@Override
	public void undo() {
		if (mergeCommand != null) {
			mergeCommand.undo();
		}
	}

	@Override
	public void redo() {
		if (mergeCommand != null) {
			mergeCommand.redo();
		}
	}

	/**
	 * @param receiver the TranscriptionImpl
	 * @param arguments arg[0] = a web service descriptor (WLServiceDescriptor), 
	 * arg[1] = the name of the input tier (String), 
	 * arg[2] = the type of input content, Sentence or Token (String)
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		transcription = (TranscriptionImpl) receiver;
		
		wlDescriptor = (WLServiceDescriptor) arguments[0];
		tierName = (String) arguments[1];
		contentType = (String) arguments[2];
		if (contentType == null) {
			// assume sentence level
			contentType = TCFConstants.SENT;
		}
		
		// start thread
		if (!cancelled) {
			new WLTThread().start(); 
		} else {
			progressInterrupt("The process was cancelled");
		}
	}
	

	private class WLTThread extends Thread {

		/**
		 * The actual processing: gets the input tier and calls a method for either 
		 * sentence level input or token level input.
		 */
		@Override
		public void run() {

			if (transcription != null) {
				TierImpl tier = (TierImpl) transcription.getTierWithId(tierName);
				// check ??
				if (tier == null) {
					progressInterrupt("A tier of that name has not been found: " + tierName);
					return;
				}
								
				if (contentType.equals(TCFConstants.SENT)) {
					processSentenceLevel(tier);
				} else if (contentType.equals(TCFConstants.TOKEN)) {
					processWordLevel(tier);
				} else {
					ClientLogger.LOG.warning("Unknown content type of the tier, cannot proceed");
					progressInterrupt("Unknown content type of the tier, cannot proceed");					
				}
				
			} else {// no transcription, we shouldn't get here at all probably
				ClientLogger.LOG.warning("There is no transcription, cannot do anything");
				progressInterrupt("There is no transcription, cannot do anything");
			}
		}
		
		/**
		 * The input tier is considered to contain sentence level annotations.
		 * 
		 * @param tier the input tier
		 */
		private void processSentenceLevel(TierImpl tier) {
			if (tier == null) {
				ClientLogger.LOG.warning("There is no tier to process.");
				return;
			}
			/*
			 * The actual processing:
			 * - (10%) convert annotations to tcf
			 * - (40%) upload to selected service(s)
			 * - (10%) convert result tcf to transcription
			 * - (10%) update times to match the original annotation times 
			 * - (25%) merge transcription
			 */
			tiersToTCF = new TiersToTCF();
			WebLichtWsClient wsClient = new WebLichtWsClient();
			String tcfString = null;
			// Three possible approaches: 
			// 1. Put all annotations together to one text and upload. Match what is returned with 
			//    the original annotation. This does not work, the sentences can be different from the original 
			//    input, the annotations. 
			// 2. Tokenize the annotations and create tcf ourselves.
			//    
			// 3. Process one annotation at a time in either of the 2 ways

			tcfString = tiersToTCF.toTCFString(tier, "Sentence");
			
			//System.out.println("TCF Sentence:");
			//System.out.println(tcfString);// cache result?
			
			if (tcfString != null) {
				progressUpdate(10, "Created TCF format from input tier");
				
				String tcfString2 = null;
				//String posTagUrl = "service-opennlp/annotate/postag";
				//http://weblicht.sfs.uni-tuebingen.de/rws/service-opennlp/annotate/postag
				
				try {
					tcfString2 = wsClient.callWithTCF(wlDescriptor.fullURL, tcfString);
					progressUpdate(50, "Uploaded the input to the service");
				} catch (IOException ioe) {
					ClientLogger.LOG.warning("An error occurred while calling a web service: " + ioe.getMessage());
					progressInterrupt("An error occurred while calling a web service: " + ioe.getMessage());
					return;
				}
				// convert to transcription
				if (tcfString2 != null) {
					progressUpdate(52, "Uploaded the input to the service, received output");
					//System.out.println("TCF 2:");
					//System.out.println(tcfString2); // cache result?
					
					TCFtoTranscription tctt = new TCFtoTranscription();
					tctt.setDefaultDuration(1000);
					try {
						TranscriptionImpl nextTrans = tctt.createTranscription(tcfString2);
						//update times based on existing transcription
						progressUpdate(60, "Converted returned content to tiers");
						
						TierImpl refTier = (TierImpl) nextTrans.getTierWithId(TCFConstants.SENT);
						if (refTier == null) {
							// message
							progressInterrupt("Could not find the Sentence level tier in the output");
							return;
						}

						if (refTier.getAnnotations().size() == 0) {
							progressInterrupt("There are no annotations on the Sentence tier");
							return;
						}

						// can check number of annotations and / or mappings of orig annotations to sentence id's?						
						List<AbstractAnnotation> origAnns = tier.getAnnotations();
						int numOrigAnns = origAnns.size();
						List<AbstractAnnotation> procAnns = refTier.getAnnotations();
						int numProcAnns = procAnns.size();
						// shift all new annotations beyond the end of the original annotations
						long origEndTime = origAnns.get(numOrigAnns - 1).getEndTimeBoundary();
						nextTrans.shiftAllAnnotations(origEndTime);
						
						AlignableAnnotation procAnn; // the created annotation is time aligned
						AbstractAnnotation origAnn; // the original can be symbolic association
						
						for (int i = 0; i < numProcAnns; i++) {
							if (i < numOrigAnns) {
								origAnn = origAnns.get(i);
								procAnn = (AlignableAnnotation) procAnns.get(i);

								procAnn.updateTimeInterval(origAnn.getBeginTimeBoundary(), origAnn.getEndTimeBoundary());
							}
						}
						// rename the reference tier
						refTier.setName(tier.getName());
						progressUpdate(70, "Realigned the new annotations with the existing annotations, adding new annotations");
						
						// now merge tiers that are on a lower level then the sentence tier
						List<String> tiersToAdd = new ArrayList<String>();
						List<TierImpl> depTiers = refTier.getDependentTiers();
						for(int i = 0; i < depTiers.size(); i++) {
							TierImpl depTier = depTiers.get(i);
							if (depTier.getAnnotations().size() > 0) {
								tiersToAdd.add(depTier.getName());
							}
						}
						// pop up a message if there are no non-empty tiers?
						if (tiersToAdd.size() == 0) {
							progressInterrupt("There were no annotation produced under the sentence level");
							return;
						}
						// check dependent tier names and rename where necessary so that tiers/annotations are not overwritten
						updateTierNames(transcription, nextTrans, tiersToAdd);
						
						// April 2015 don't add the reference tier otherwise the existing source annotations will be overwritten.
						//tiersToAdd.add(0, refTier.getName());
						
						MergeTranscriptionsByAddingCommand command = (MergeTranscriptionsByAddingCommand) 
								ELANCommandFactory.createCommand(transcription, ELANCommandFactory.WEBLICHT_MERGE_TRANSCRIPTIONS);
						// don't overwrite the input annotations, don't add linked files and don't perform a tier compatibility test 
						command.execute(transcription, new Object[]{nextTrans, tiersToAdd, Boolean.TRUE, Boolean.FALSE, 
								Boolean.FALSE, Boolean.FALSE});

						
						ClientLogger.LOG.info("Merged in the produced tiers and annotations.");
						progressComplete("Merged in the produced tiers and annotations.");
					} catch (SAXException sax) {
						ClientLogger.LOG.warning("An error occurred while parsing the returned TCF file: " + sax.getMessage());
						progressInterrupt("An error occurred while parsing the returned TCF file: " + sax.getMessage());
					} catch (IOException ioe) {// is this still possible here?
						ClientLogger.LOG.warning("An error occurred while calling a web service: " + ioe.getMessage());
						progressInterrupt("An error occurred while calling a web service: " + ioe.getMessage());
					}
				} else {
					progressInterrupt("Calling the web service did not succeed, no content returned.");
				}
			} else {
				progressInterrupt("Unable to create meaningful input for the web service");
			}
		}
		
		/**
		 * The input tier is considered to contain word or token level annotations.
		 * 
		 * @param tier the input tier
		 */
		private void processWordLevel(TierImpl tier) {
			if (tier == null) {// should never happen
				ClientLogger.LOG.warning("There is no tier to process.");
				return;
			}
			/*
			 * The actual processing:
			 * - (10%) convert annotations to tcf
			 * - (40%) upload to selected service(s)
			 * - (10%) convert result tcf to transcription
			 * - (10%) update times to match the original annotation times 
			 * - (25%) merge transcription
			 */
			TierImpl parentTier;// sentence level probably
			String tcfString = null;
			// get the parent tier
			if (tier.hasParentTier()) {
				parentTier = tier.getParentTier();
				if (parentTier.hasParentTier()) {
					// this is considered an error. Assume that the parent tier is a sentence tier and is a top level tier
					// or a symbolically associated child of a top level tier ?
					// now return
//					progressInterrupt("The parent tier is not a top level tier, this setup is currently not supported.");
//					return;
				}
				tiersToTCF = new TiersToTCF();
				tcfString = tiersToTCF.toTCFString(tier, "Token");
				//System.out.println("TCF Token:");                                                                                
				//System.out.println(tcfString); // maybe cache the result?
				progressUpdate(10, "Created TCF format from input tier");
			} else {
				// this is an error. Treat all annotations as words in one sentence?
				// for now return
				progressInterrupt("The tier has no parent; token type of input is expected to be on a child tier: " + tierName);
				return;
			}
			
			if (tcfString != null) {
				WebLichtWsClient wsClient = new WebLichtWsClient();
				String tcfString2 = null;

				//String posTagUrl = "service-opennlp/annotate/postag";
				//http://weblicht.sfs.uni-tuebingen.de/rws/service-opennlp/annotate/postag
				try {
					tcfString2 = wsClient.callWithTCF(wlDescriptor.fullURL, tcfString);
					progressUpdate(50, "Uploaded the input to the service");
				} catch (IOException ioe) {
					ClientLogger.LOG.warning("An error occurred while calling a web service: " + ioe.getMessage());
					progressInterrupt("An error occurred while calling a web service: " + ioe.getMessage());
					return;
				}
				
				// convert to transcription
				if (tcfString2 != null) {
					progressUpdate(52, "Uploaded the input to the service, received output");
					TCFtoTranscription tctt = new TCFtoTranscription();
					tctt.setDefaultDuration(100);// safe assumption that this way all new annotations will be left
					// of the original counterpart and will have to be shifted to the right.
					try {
						TranscriptionImpl nextTrans = tctt.createTranscription(tcfString2);
						progressUpdate(60, "Converted returned content to tiers");
						//update times based on existing transcription
						TierImpl refSentTier = nextTrans.getTierWithId(TCFConstants.SENT);
						
						if (refSentTier == null) {
							// message
							ClientLogger.LOG.warning("Could not find the Sentence level tier in the output");
							progressInterrupt("Could not find the Sentence level tier in the output");
							return;
						}
						// if there are no annotations return
						if (refSentTier.getAnnotations().size() == 0) {
							ClientLogger.LOG.warning("There are no annotations on the Sentence tier");
							progressInterrupt("There are no annotations on the Sentence tier");
							return;
						}
						
						TierImpl refTokTier = nextTrans.getTierWithId(TCFConstants.TOKEN);
						if (refTokTier == null) {
							// message
							ClientLogger.LOG.warning("Could not find the Token level tier in the output");
							progressInterrupt("Could not find the Token level tier in the output");
							return;
						}
						// if there are no annotations return
						if (refTokTier.getAnnotations().size() == 0) {
							ClientLogger.LOG.warning("There are no annotations on the Token tier");
							progressInterrupt("There are no annotations on the Token tier");
							return;
						}
						// relinking has to be done via the sentence level tier
						// there is no guarantee that the tokens are returned unchanged
						// maybe just create and add a copy of the word/token tier//??
						List<AbstractAnnotation> origAnns = new ArrayList<AbstractAnnotation>();
						// filter out sentences with word/token children
						AbstractAnnotation parAnn;
						for (AbstractAnnotation tokAnn : tier.getAnnotations()) {
							parAnn = (AbstractAnnotation) tokAnn.getParentAnnotation();
							if (!origAnns.contains(parAnn)) {
								origAnns.add(parAnn);
							}
						}
						
						int numOrigAnns = origAnns.size();
						List<AbstractAnnotation> procAnns = refSentTier.getAnnotations();
						int numProcAnns = procAnns.size();
						
						AbstractAnnotation origAnn;
						AlignableAnnotation procAnn;
						
						if (numOrigAnns == numProcAnns) {// assume that the original and resulting sentences match
							for (int i = numOrigAnns - 1; i >= 0; i--) {
								origAnn = origAnns.get(i);
								procAnn = (AlignableAnnotation) procAnns.get(i);
								// how to effectively update annotation times? Bulldozer mode, direct time slot manipulation?
								if (procAnn.getBeginTimeBoundary() <= origAnn.getBeginTimeBoundary()) {
									procAnn.updateTimeInterval(origAnn.getBeginTimeBoundary(), origAnn.getEndTimeBoundary());
								} else {
									// something wrong, wrong estimation when the returned tcf is converted to a transcription
									ClientLogger.LOG.warning("A new annotation has been positioned to the right of the original, cannot realign");
									// it will likely be overwritten by other annotations
								}
							}
						} else {// different number of sentences, maybe create copies of tiers?
							for (int i = numOrigAnns - 1; i >= 0; i--) {
								if (i < numProcAnns) {
									origAnn = origAnns.get(i);
									procAnn = (AlignableAnnotation) procAnns.get(i);
									// how to effectively update annotation times? Bulldozer mode, direct time slot manipulation?
									if (procAnn.getBeginTimeBoundary() <= origAnn.getBeginTimeBoundary()) {
										if (procAnn.getValue().equals(origAnn.getValue())) {// there might always be slight differences, how to deal with that?
											procAnn.updateTimeInterval(origAnn.getBeginTimeBoundary(), origAnn.getEndTimeBoundary());
										} else {
											// continue the loop? 
										}
									}
								}
							}
						}
						// match the token tiers, rename the token tier in the new transcription, 
						// create unique tier names for lower level tiers
						refTokTier.setName(tier.getName());
						// now merge tiers that are on a lower level then the word tier
						List<String> tiersToAdd = new ArrayList<String>();
						List<TierImpl> depTiers = refTokTier.getDependentTiers();
						for(int i = 0; i < depTiers.size(); i++) {
							TierImpl depTier = depTiers.get(i);
							// skip empty tiers?
							if (depTier.getAnnotations().size() > 0) {
								tiersToAdd.add(depTier.getName());
							}
						}
						// pop up a message if there are no non-empty tiers, interrupt the process
						if (tiersToAdd.size() == 0) {
							progressInterrupt("There were no annotations produced under the sentence level");
							return;
						}
						
						updateTierNames(transcription, nextTrans, tiersToAdd);
						//tiersToAdd.add(0, tier.getName());
						
						mergeCommand = (MergeTranscriptionsByAddingCommand) 
								ELANCommandFactory.createCommand(transcription, ELANCommandFactory.WEBLICHT_MERGE_TRANSCRIPTIONS);
						mergeCommand.execute(transcription, new Object[]{nextTrans, tiersToAdd, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE});
	
						
						ClientLogger.LOG.info("Added the produced tiers and annotations.");
						progressComplete("Added in the produced tiers and annotations.");
					} catch (SAXException sax) {
						ClientLogger.LOG.warning("An error occurred while parsing the returned TCF file: " + sax.getMessage());
						progressInterrupt("An error occurred while parsing the returned TCF file: " + sax.getMessage());
					} catch (IOException ioe) {// is this still possible here?
						ClientLogger.LOG.warning("An error occurred while calling a web service: " + ioe.getMessage());
						progressInterrupt("An error occurred while calling a web service: " + ioe.getMessage());
					}
				}
			}
		}
		
		/**
		 * 
		 * @param refTrans the transcription the new tiers have to be added to
		 * @param nextTrans the transcription resulting from the returned content by the service
		 * 
		 * @param tiersToAdd effectively all tiers in the next transcription except for the (equivalent of) the source tier
		 */
		private void updateTierNames(Transcription refTrans, Transcription nextTrans, List<String> tiersToAdd) {
			String tierName;
			final int MAX_COUNT = 50; // arbitrary number of attempts
			
			for (Tier t : nextTrans.getTiers()) {
				tierName = t.getName();
				
				if (tiersToAdd.contains(tierName) && refTrans.getTierWithId(tierName) != null) {
					// add or update counter suffix
					int count = 1;
					String newName = tierName;
					do {
						newName = tierName + "-" + count++;
					} while (refTrans.getTierWithId(newName) != null && count < MAX_COUNT);
					
					if (count < MAX_COUNT) {
						t.setName(newName);
						tiersToAdd.remove(tierName);
						tiersToAdd.add(newName);
					}
				}
			}
			
		}
	
	}

}

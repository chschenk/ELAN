package mpi.eudico.client.annotator.commands;

import java.io.IOException;

import org.xml.sax.SAXException;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.webserviceclient.weblicht.TCFtoTranscription;
import mpi.eudico.webserviceclient.weblicht.WLServiceDescriptor;
import mpi.eudico.webserviceclient.weblicht.WebLichtWsClient;

/**
 * A command that sends text to a WebLicht web service and adds results to the transcription.
 * The undoable part is delegated to a merging command 
 * 
 * @author Han Sloetjes
 */
public class WebLichtTextBasedCommand extends AbstractProgressCommand implements UndoableCommand {
	private MergeTranscriptionsByAddingCommand mergeCommand;
	private Transcription transcription;
	private Transcription transcription2;
	private String inputText;
	private int sentenceDuration;
	private WLServiceDescriptor wlDescriptor;
	
	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public WebLichtTextBasedCommand(String name) {
		super(name);
	}

	/**
	 * @param receiver the TranscriptionImpl
	 * @param arguments arg[0] = the input text (String),
	 * arg[1] = custom duration per sentence (Integer), 
	 * arg[2] = whether or not to tokenize (WLServiceDescriptor), 
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		transcription = (Transcription) receiver;
		
		inputText = (String) arguments[0];
		sentenceDuration = (Integer) arguments[1];
		wlDescriptor = (WLServiceDescriptor) arguments[2];
		
		// start thread
		if (!cancelled) {
			new WLThread().start(); 
		} else {
			progressInterrupt("The process was cancelled");
		}
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
	 * A thread that performs the calling of the web services.
	 * 
	 * @author Han Sloetjes
	 *
	 */
	private class WLThread extends Thread {

		/**
		 * Starts uploading text and possibly calls additional services.
		 */
		@Override
		public void run() {
			/*
			 * The actual processing:
			 * - (60%) uploading the text (and subsequently tcf) to predefined service(s)
			 * - (10%) convert result tcf to transcription
			 * - (25%) merge transcriptions
			 */
			int perCycle = 30;
			
			progressUpdate(5, "Uploading text to WebLicht...");
			if (cancelled) {
				return;
			}
			WebLichtWsClient wsClient = new WebLichtWsClient();
			String tcfToParse = null;
			// configure
			String tcf = null;
			try {
				tcf = wsClient.convertPlainText(inputText);
				progressUpdate(perCycle, "Converted input text to TCF format");
			} catch (IOException ioe) {
				ClientLogger.LOG.warning("Error converting text to TCF: " + ioe.getMessage());
				//progressInterrupt("Error converting text to TCF: " + ioe.getMessage());
				// programmatically convert to TCF
				tcf = textInTCF(inputText);
				progressUpdate(perCycle, "An error occurred, converted input text to TCF format locally");
				//return;
			}
			// immediately upload the contents to a sentence splitter
			if (tcf != null) {
				if (cancelled) {
					return;
				}
				//System.out.println("TCF 1:");
				//System.out.println(tcf);
				tcfToParse = tcf;
				//String sentenceTokenUrl = "service-opennlp-1_5/tcf/detect-sentences/tokenize";
				//String sentenceTokenUrl = "service-opennlp/annotate/tok-sentences";
					
				String tcf2 = null;
				try {
					tcf2 = wsClient.callWithTCF(wlDescriptor.fullURL, tcf);
					progressUpdate(2 * perCycle, "Uploaded for tokenization of the sentences");
				} catch (IOException ioe) {
					ClientLogger.LOG.warning("Error while calling the tokenizer service: " + ioe.getMessage());
					progressUpdate(2 * perCycle, "Error while calling the tokenizer service: " + ioe.getMessage());
				}
				
				if (tcf2 != null) {
					//System.out.println("TCF 2:");
					//System.out.println(tcf2);
					if (cancelled) {
						return;
					}
					tcfToParse = tcf2;
				} else {
					progressUpdate(60, "Unable to get tokens, creating tiers...");
				}
				
				if (tcfToParse != null) {
					if (cancelled) {
						return;
					}
					progressUpdate(62, "Converting returned content to a transcription.");
					TCFtoTranscription tctt = new TCFtoTranscription();
					tctt.setDefaultDuration(sentenceDuration);
					tctt.setTiersToInclude(true, false, false);
					
					try {
						transcription2 = tctt.createTranscription(tcfToParse);
						
						// check tier names and rename where necessary so that tiers/annotations are not overwritten
						updateTierNames(transcription, transcription2);
						
						progressUpdate(70, "Created a transcription, starting to merge");
						mergeCommand = (MergeTranscriptionsByAddingCommand) 
								ELANCommandFactory.createCommand(transcription, ELANCommandFactory.WEBLICHT_MERGE_TRANSCRIPTIONS);
						mergeCommand.execute(transcription, new Object[]{transcription2, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE});
						
						progressComplete("New tiers were added to the transcription");
					} catch (SAXException sax) {
						ClientLogger.LOG.warning("Error parsing tcf file: " + sax.getMessage());
						progressInterrupt("Error parsing tcf file: " + sax.getMessage());
					} catch (IOException ioe) {//??
						ClientLogger.LOG.warning("Error parsing tcf file: " + ioe.getMessage());
						progressInterrupt("Error parsing tcf file: " + ioe.getMessage());
					}
				}
			} else {
				ClientLogger.LOG.warning("Unknown error converting text to TCF");
				progressInterrupt("Unknown error converting text to TCF");				
			}
			
		}
		
		/**
		 * Wraps the input text in a TCF body with lang set to unknown.
		 * 
		 * @param inputText the input text, should be escaped probably
		 * @return tcf
		 */
		private String textInTCF(String inputText) {
			StringBuilder builder = new StringBuilder();

			builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			builder.append("<D-Spin xmlns=\"http://www.dspin.de/data\" version=\"0.4\">");
			builder.append("<MetaData xmlns=\"http://www.dspin.de/data/metadata\">");
			builder.append("<source></source>");
			builder.append("</MetaData>");
			builder.append("<TextCorpus xmlns=\"http://www.dspin.de/data/textcorpus\" lang=\"unknown\">");		
			builder.append("<text>");
			builder.append(inputText);
    		builder.append("</text>");
    		builder.append("</TextCorpus>");
    		builder.append("</D-Spin>");
    		
			return builder.toString();
		}
		
		/**
		 * 
		 * @param refTrans the transcription the new tiers have to be added to
		 * @param nextTrans the transcription resulting from the returned content by the service
		 */
		private void updateTierNames(Transcription refTrans, Transcription nextTrans) {
			String tierName;
			final int MAX_COUNT = 50; // arbitrary number of attempts
			
			for (Tier t : nextTrans.getTiers()) {
				tierName = t.getName();
				
				if (refTrans.getTierWithId(tierName) != null) {
					// add or update counter suffix
					int count = 1;
					String newName = tierName;
					do {
						newName = tierName + "-" + count++;
					} while (refTrans.getTierWithId(newName) != null && count < MAX_COUNT);
					
					if (count < MAX_COUNT) {
						t.setName(newName);
					}
				}
			}
		}
	}

}

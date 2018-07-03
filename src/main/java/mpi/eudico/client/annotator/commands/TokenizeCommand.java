package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.IndeterminateProgressMonitor;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * A Command that tokenizes the contents of the annotations a source tier 
 * new annotations on a destination tier.
 * 
 * @author Han Sloetjes
 */
public class TokenizeCommand implements UndoableCommand {
	private String commandName;
	private static final Logger LOG = Logger.getLogger(TokenizeCommand.class.getName());
	
	// store state 
	private TranscriptionImpl transcription;
	private TierImpl sourceTier;
	private TierImpl destTier;
	private String delimiter;
	private String tokens;
	private String splitTokens;
	private boolean preserve;
	private boolean createEmpty; 
	private boolean returnDelims;
	
	// store for undo
	/** backup data of existing annotations on the destination tier */
	private List<DefaultMutableTreeNode> existAnnotations;
	/** store the data of the newly created annotations */
	private List<List<AnnotationDataRecord>> newAnnotationsNodes;
	// store for redo
	/** a list of source annotations that have been tokenized */
	private List<AnnotationDataRecord> completedTokenizations;

	/**
	 * Creates a new TokenizeCommand instance.
	 *
	 * @param name the name of the command
	 */
	public TokenizeCommand(String name) {
		commandName = name;
	}
	
	/**
	 * Undo the changes made by this command.
	 */
	@Override
	public void undo() {
		if (transcription == null || sourceTier == null || destTier == null ||
				completedTokenizations == null) {
			return;
		}
		int curPropMode = 0;

		curPropMode = transcription.getTimeChangePropagationMode();

		if (curPropMode != Transcription.NORMAL) {
			transcription.setTimeChangePropagationMode(Transcription.NORMAL);
		}

		transcription.setNotifying(false);
		
		setWaitCursor(true);
		
		// delete created annotations
		if (completedTokenizations.size() > 0) {
			wipeTargetAnnotations();
		}
		// recreate annotations that have been overwritten 
		if (!preserve && existAnnotations.size() > 0) {
			AnnotationRecreator.createAnnotationsSequentially(transcription, existAnnotations, true);
		}
		transcription.setNotifying(true);
		
		setWaitCursor(false);
		
		// restore the time propagation mode
		transcription.setTimeChangePropagationMode(curPropMode);
	}

	/**
	 * Wipe annotations from the destination tier in some time periods, because
	 * we're going to put different annotations there. Used for both undo (to
	 * remove the token-annotations and  make room for the originals if any) and
	 * redo (to make room for the token-annotations).
	 */
	private void wipeTargetAnnotations() {
		AnnotationDataRecord srcRecord;
		AbstractAnnotation srcAnn;
		AbstractAnnotation destAnn;
		List<Annotation> childrenOnDest;
		boolean destTierIsRootTier = !destTier.hasParentTier();
		
		for (int i = 0; i < completedTokenizations.size(); i++) {
			srcRecord = completedTokenizations.get(i);
			if (destTierIsRootTier) {
				childrenOnDest = destTier.getOverlappingAnnotations(srcRecord.getBeginTime(), srcRecord.getEndTime());
			} else {
				srcAnn = (AbstractAnnotation)sourceTier.getAnnotationAtTime(srcRecord.getBeginTime());
				childrenOnDest = srcAnn.getChildrenOnTier(destTier);
			}
			for (int j = 0; j < childrenOnDest.size(); j++) {
				destAnn = (AbstractAnnotation)childrenOnDest.get(j);

				destTier.removeAnnotation(destAnn);					
			}
		}
	}

	/**
	 * Redo the changes made by this command.
	 */
	@Override
	public void redo() {
		if (transcription == null || sourceTier == null || destTier == null ||
				completedTokenizations == null) {
			return;
		}
		int curPropMode = 0;

		curPropMode = transcription.getTimeChangePropagationMode();

		if (curPropMode != Transcription.NORMAL) {
			transcription.setTimeChangePropagationMode(Transcription.NORMAL);
		}

		transcription.setNotifying(false);
		
		setWaitCursor(true);
		
		if (completedTokenizations.size() > 0) {
			if (!preserve) {
				wipeTargetAnnotations();
			}
			
			if (newAnnotationsNodes.size() > 0) {
				AnnotationRecreator.createAnnotationsSequentiallyDepthless(transcription, newAnnotationsNodes, true);
				//AnnotationRecreator.createAnnotationsSequentially(transcription, newAnnotationsNodes);
			}
		}		
		transcription.setNotifying(true);
		
		setWaitCursor(false);
		
		// restore the time propagation mode
		transcription.setTimeChangePropagationMode(curPropMode);
	}

	/**
	 * <b>Note: </b>it is assumed the types and order of the arguments are
	 * correct.
	 *
	 * @param receiver
	 *            the TranscriptionImpl
	 * @param arguments
	 *            the arguments:
	 *            <ul>
	 *            <li>arg[0] = the name of the source tier (String)
	 *            <li>arg[1] = the name of the destination tier (String)</li>
	 *            <li>arg[2] = the token delimiter(s) (String)</li>
	 *            <li>arg[3] = a flag denoting whether or not to preserve
	 *            existing annotations on the destination tier (Boolean)</li>
	 *            <li>arg[4] = a flag denoting whether or not to create new
	 *            annotations for empty source annotations
	 *            <li>arg[5] = a String containing punctuation characters that
	 *            must each become a separate token.</li>
	 *            (Boolean)</li>
	 *            </ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		transcription = (TranscriptionImpl)receiver;
		String sourceName = (String)arguments[0];
		String destName = (String)arguments[1];

		delimiter = (String) arguments[2]; //can be null
		preserve = ((Boolean)arguments[3]).booleanValue();
		createEmpty = ((Boolean)arguments[4]).booleanValue();
		tokens = (String) arguments[5];	// can be null
		
		if (delimiter == null) {
			delimiter = " \t\n\r\f";	// default delimiters for StringTokenizer.
		}
		
		if (tokens == null || tokens.isEmpty()) {
			splitTokens = delimiter;
			returnDelims = false;
		} else {
			// If we want separate tokens, run the StringTokenizer in the mode where
			// it returns the delimiters.
			// In that case, we need to check if we get back any delimiter (not token)
			// and discard it.
			splitTokens = uniqueCharacters(delimiter, tokens);
			returnDelims = true;
		}

		sourceTier = transcription.getTierWithId(sourceName);
		destTier = transcription.getTierWithId(destName);

		if (transcription == null || sourceTier == null || destTier == null) {
			LOG.severe("Error in retrieving the transcription or one of the tiers.");
			return;
		}
		// Check if the destination tier is a parent of the source: that is not allowed.
		if (isParentOf(destTier, sourceTier)) {
			LOG.severe("Destination Tier is parent of source Tier.");
			return;
		}
		// create a blocking progress monitor and start tokenizing
		existAnnotations = new ArrayList<DefaultMutableTreeNode>();
		newAnnotationsNodes = new ArrayList<List<AnnotationDataRecord>>();
		completedTokenizations = new ArrayList<AnnotationDataRecord>();
	
		new TokenizeThread().start();

	}
    private String uniqueCharacters(String first, String second) {
        StringBuilder buffer = new StringBuilder(first);

        for (int i = 0; i < second.length(); i++) {
            if (first.indexOf(second.charAt(i)) < 0) {
                buffer.append(second.charAt(i));
            }
        }

        return buffer.toString();
    }


	/**
	 * Returns the name of the command.
	 *
	 * @return the name of the command
	 */
	@Override
	public String getName() {
		return commandName;
	}
	
	/**
	 * Changes the cursor to either a 'busy' cursor or the default cursor.
	 *
	 * @param showWaitCursor when <code>true</code> show the 'busy' cursor
	 */
	private void setWaitCursor(boolean showWaitCursor) {
		if (showWaitCursor) {
			ELANCommandFactory.getRootFrame(transcription).getRootPane()
							  .setCursor(Cursor.getPredefinedCursor(
					Cursor.WAIT_CURSOR));
		} else {
			ELANCommandFactory.getRootFrame(transcription).getRootPane()
							  .setCursor(Cursor.getDefaultCursor());
		}
	}
	
    /**
     * Determine if {@code parent} is a parent tier of {@code child}.
     * @param parent potential parent
     * @param child potential child
     * @return whether that is so.
     */
    private boolean isParentOf(Tier parent, TierImpl child) {
    	while ((child = child.getParentTier()) != null) {
    		if (child == parent) {
    			return true;
    		}
    	}
    	return false;
    }

	///////////////////////////////////////////
	// inner class: execution thread
	///////////////////////////////////////////
	/**
	 * Class that handles the tokenization in a separate thread.
	 * @author Han Sloetjes
	 */
	private class TokenizeThread extends Thread {
		
		/**
		 * Before using this inner class we must ensure none of the relevant fields
		 * (transcription, sourcetier and destination tier) are null!
		 */
		TokenizeThread() {
			super("Tokenize");
		}
		
		@Override
		public void run() {
			int curPropMode = 0;

			curPropMode = transcription.getTimeChangePropagationMode();

			if (curPropMode != Transcription.NORMAL) {
				transcription.setTimeChangePropagationMode(Transcription.NORMAL);
			}
			
			final IndeterminateProgressMonitor monitor = new IndeterminateProgressMonitor(
					ELANCommandFactory.getRootFrame(transcription),
					true, 
					ElanLocale.getString("TokenizeDialog.Message.Tokenizing"),
					true, 
					ElanLocale.getString("Button.Cancel"));
				// if we are blocking (modal) call show from a separate thread
			new Thread(new Runnable(){
				@Override
				public void run() {
					monitor.show();				
				}
			}, "Tokenize Monitor").start();
				
			List<AbstractAnnotation> sourceAnnos = sourceTier.getAnnotations();
			if (sourceAnnos.size() <= 0) {
				monitor.close();
				return;
			}
			TokenizeCommand.this.transcription.setNotifying(false);
				
			//start iterating over source annotations
			StringTokenizer tokenizer;
			List<Annotation> newAnnos = new ArrayList<Annotation>();
			List<AnnotationDataRecord> siblings;
			boolean destTierIsRootTier = !destTier.hasParentTier();
			
			for (AbstractAnnotation srcAnn : sourceAnnos) {
				List<Annotation> childrenOnDest = srcAnn.getChildrenOnTier(destTier);
				@SuppressWarnings("unused")
				Annotation parentAnn;
				if (destTierIsRootTier) {
					parentAnn = null;
					childrenOnDest = destTier.getOverlappingAnnotations(srcAnn.getBeginTimeBoundary(), srcAnn.getEndTimeBoundary());
				} else {
					parentAnn = srcAnn;
					childrenOnDest = srcAnn.getChildrenOnTier(destTier);
				}
				if (childrenOnDest.size() > 0 && !preserve) {
					// store old annotations (assume they are AbstractAnnotations)
					Iterator<Annotation> childIt = childrenOnDest.iterator();
					while (childIt.hasNext()) {
						AbstractAnnotation destAnn = (AbstractAnnotation)childIt.next();
						existAnnotations.add(AnnotationRecreator.createTreeForAnnotation(destAnn));
					}
					// next remove them
					childIt = childrenOnDest.iterator();
					while (childIt.hasNext()) {
						destTier.removeAnnotation(childIt.next());
					}
				}
				// if existing anns need to be preserved, do nothing
				if (childrenOnDest.size() == 0 || !preserve) {
					String srcValue = srcAnn.getValue();
					tokenizer = new StringTokenizer(srcValue, splitTokens, returnDelims);
					newAnnos.clear();

					List<String> tokens = new ArrayList<String>();
					
					// Count all tokens, since for some destination tier stereotypes we need
					// to know how to subdivide the time.
					while (tokenizer.hasMoreTokens()) {
						String nextToken = tokenizer.nextToken();
						// Discard delimiter tokens if it is possible to get them
						if (returnDelims && nextToken.length() == 1) {
							if (delimiter.indexOf(nextToken.charAt(0)) >= 0) {
								continue;
							}
						}
						tokens.add(nextToken);
					}
					
					Annotation prevAnn = null;
					int seq = 0;
					for (String nextToken : tokens) {
						prevAnn = createAnnotation(destTier, prevAnn,
								srcAnn.getBeginTimeBoundary(), srcAnn.getEndTimeBoundary(), 
								tokens.size(), seq);
						seq++;
						if (prevAnn != null) {
							prevAnn.setValue(nextToken);
							newAnnos.add(prevAnn);
						}
					}

					if (newAnnos.isEmpty()) {
						// if the source annotation is empty and the create destination
						// for empty source is selected create one empty annotation
						if (createEmpty) {
							Annotation ann;
							ann = createAnnotation(destTier, null,
									srcAnn.getBeginTimeBoundary(), srcAnn.getEndTimeBoundary(), 
									1, 0);
							if (ann != null) {
								newAnnos.add(ann);
							}
	
						}
					}

					// now create datarecords of the created annotations...
					if (!newAnnos.isEmpty()) {
						final int size = newAnnos.size();
						siblings = new ArrayList<AnnotationDataRecord>(size);
						for (int i = 0; i < size; i++) {
							//newAnnotationsNodes.add(new DefaultMutableTreeNode(
							//	new AnnotationDataRecord((Annotation)newAnnos.get(i))));
							siblings.add(new AnnotationDataRecord(newAnnos.get(i)));
						}
						newAnnotationsNodes.add(siblings);
					}
					completedTokenizations.add(new AnnotationDataRecord(srcAnn));
				}
				// after completion of a whole source annotation, check the cancelled value of the monitor
				if (monitor.isCancelled()) {
					//monitor.close();
					//return;
					break;
				}
			}
			TokenizeCommand.this.transcription.setNotifying(true);
			
			// restore the time propagation mode
			transcription.setTimeChangePropagationMode(curPropMode);

			monitor.close();
		}
	}
	
	/**
	 * Creates one of several annotations in a time period.
	 * Works for all 4 of the stereotypes.
	 * (SYMBOLIC_ASSOCIATION allows only the first annotation to be made)
	 * 
	 * @param t Tier where the annotation should be made
	 * @param prevAnn Annotation that was made before it (or null if seq == 0)
	 * @param bt begin time of the period in which the annotations should be made
	 * @param et end time
	 * @param numAnns the total number of annotations to make in the time period.
	 * @param seq sequence number: starts at 0 in the first call, increasing by 1
	 */
	static Annotation createAnnotation(TierImpl t, Annotation prevAnn,
			long bt, long et, int numAnns, int seq) {
		LinguisticType lt = t.getLinguisticType();
		Constraint con = lt.getConstraints();
		int st = con == null ? -1 : con.getStereoType();
		Annotation curAnn = null;
		
		if (con == null || st == Constraint.INCLUDED_IN) {
			curAnn = createSubperiodAnnotation(t, bt, et, numAnns, seq);
		} else if (st == Constraint.TIME_SUBDIVISION) {
			if (prevAnn != null) {
				curAnn = t.createAnnotationAfter(prevAnn);
			} else {
				curAnn = createSubperiodAnnotation(t, bt, et, numAnns, seq);
			}
		} else if (st == Constraint.SYMBOLIC_SUBDIVISION) {
			if (prevAnn != null) {
				curAnn = t.createAnnotationAfter(prevAnn);
			} else {
				long mid = (bt + et) / 2;
				curAnn = t.createAnnotation(mid, mid);
			}
		} else if (st == Constraint.SYMBOLIC_ASSOCIATION) {
			if (prevAnn == null) {
				long mid = (bt + et) / 2;
				curAnn = t.createAnnotation(mid, mid);
			}
		}
	
		return curAnn;
	}

	/**
	 * Create a new annotation in a subdivided time period.
	 * <p>
	 * The whole time period is from {@code bt} to {@code et}.
	 * <p>
	 * The period is subdivided in {@code numAnns} sub-periods, and the desired new
	 * annotation occupies the {@code seq}th of that (0-based).
	 * <p>
	 * If the time period cannot be evenly divided into numAnns parts (the resolution
	 * is in whole milliseconds) then some created annotations will be longer than others,
	 * to compensate for the accumulated truncation loss.
	 * <p>
	 * Each annotation will start at the same moment that the previous one ends.
	 * The first one will always start at moment {@code bt}.
	 * The last one will always end at moment {@code et}.
	 * 
	 * @param t TierImpl where to create the annotation
	 * @param bt begin time
	 * @param et end time
	 * @param numAnns total annotations to be created
	 * @param seq sequence number of this one, from the sequence of numAnns.
	 * 
	 * @return the created new Annotation
	 */
	private static Annotation createSubperiodAnnotation(TierImpl t, long bt,
			long et, int numAnns, int seq) {
		float perAnn = (float)(et - bt) / numAnns;
		long start = bt + (long)(seq * perAnn);
		long end = (seq + 1 == numAnns) ? et	// avoid rounding error
				                        : bt + (long)((seq+1) * perAnn);
		/* If we try to make a time-aligned child of a parent annotation that is alignable
		 * but not aligned (wholly or partially), this would fail.
		 * We could call
		if (parentAnn instanceof AlignableAnnotation) {
			((AlignableAnnotation) parentAnn).makeTimeAligned();
		}
		 * but that would change other tiers than the target.
		 * Not to mention the extra work for undo.
		 */
		
		//System.out.printf("createSubperiodAnnotation: seq=%d numAnns=%d perAnn=%f bt=%d et=%d start=%d end=%d, (%f, %f, %f)\n",
		//		seq, numAnns, perAnn, bt, et, start, end,
		//		seq * perAnn, seq * perAnn + perAnn, (seq+1) * perAnn);

		return t.createAnnotation(start, end);
	}
}

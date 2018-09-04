package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.util.TimeRelation;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * An undo-able command for copying annotations of one tier to another tier.
 * The destination tier must either be an independent, top-level tier or a 
 * direct child tier of the source tier.
 * The implementation currently only supports copying within a single transcription,
 * there are preparations for supporting this kind of copying in multiple files.  
 *  
 * @author Han Sloetjes
 */
public class CopyAnnotationsOfTierCommand extends AbstractProgressCommand implements UndoableCommand {
	private TranscriptionImpl transcription;
	private String sourceTierName;
	private String sourceCVName = "";
	private String destinationTierName;
	private String destinationCVName = "";
	
	private boolean copyAllAnnotations;
	private String queryValue;
	private Boolean isRegexObj;
	private boolean isRegex = false;
	private Pattern pattern;
	private Boolean overwriteObj;
	private boolean overwrite = false;
	private List<String> filesToProcess;
	
	private List<DefaultMutableTreeNode> deletedAnnotations;
	private List<List<DefaultMutableTreeNode>> deletedChildAnnotations;
	private List<DefaultMutableTreeNode> modifiedAnnotations;
	private List<AnnotationDataRecord> copyAnnotations;
	
	/**
	 * Constructor
	 * @param name name of the command
	 */
	public CopyAnnotationsOfTierCommand(String name) {
		super(name);
	}

	/**
	 * @param receiver the Transcription in case of single file mode, 
	 * 			null in case of multiple file mode
	 * @param arguments the arguments: <ul>
	 * <li>arguments[0]: the source tier name (String)</li>
	 * <li>arguments[1]: the destination tier name (String)</li>
	 * <li>arguments[2]: the copy mode - ALL or WithValue (String)</li>
	 * <li>arguments[3]: the annotation query constraint (or null) (String)</li>
	 * <li>arguments[4]: the regular expression flag (Boolean)</li>
	 * <li>arguments[5]: the overwrite flag (Boolean)</li>
	 * <li>arguments[6]: the list of files to process (in case of multiple file mode) (List of String)</li>
	 * </ul>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void execute(Object receiver, Object[] arguments) {
		if (receiver instanceof TranscriptionImpl) {
			transcription = (TranscriptionImpl) receiver;
		}
		if (arguments != null && arguments.length >= 6) {
			try {
				sourceTierName = (String) arguments[0];
				destinationTierName = (String) arguments[1];
				String cpMode = (String) arguments[2];
				if ("WithValue".equals(cpMode)) {
					copyAllAnnotations = false;
				} else {// by default copy all
					copyAllAnnotations = true;
				}
				queryValue = (String) arguments[3];
				if (queryValue == null) {
					copyAllAnnotations = true;
				}
				isRegexObj = (Boolean) arguments[4];
				if (isRegexObj != null) {
					isRegex = isRegexObj.booleanValue();
				}
				if (isRegex) {
					// check if queryValue is a valid regular expression
					try {
						pattern = Pattern.compile(queryValue);// add some flags to change default behavior? UNICODE_CASE, CANON_EQ,...
					} catch(PatternSyntaxException pse) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.warning(String.format("Cannot execute the Copy Annotations command, invalid regular expression: %s", 
									pse.getMessage()));
						}
						progressInterrupt(String.format("Cannot execute the Copy Annotations command because of an invalid regular expression: %s", 
								pse.getMessage()));
						return;
					}
				}
				overwriteObj = (Boolean) arguments[5];
				if (overwriteObj != null) {
					overwrite = overwriteObj.booleanValue();
				}
			} catch (Throwable t) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.warning(String.format("Cannot execute the Copy Annotations command: %s", 
							t.getMessage()));
				}
				progressInterrupt(String.format("Cannot execute the Copy Annotations command because of a problem with the arguments: %s", 
						t.getMessage()));
				return;
			}
		} else {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning(String.format("Cannot execute the Copy Annotations command: %d arguments (<6)", 
						(arguments == null ? 0 : arguments.length)));
			}
			progressInterrupt("No or too few arguments passed to the Copy Annotations command");
			return;
		}
		
		if (arguments.length >= 7) {
			try {
				filesToProcess = (List<String>) arguments[6];
			} catch (Throwable t) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.warning("Copy Annotations command, unexpected argument: " + 
							"the last argument is not a list of file names");
				}
			}
		}
		
		if (transcription == null && (filesToProcess == null || filesToProcess.isEmpty())) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Cannot execute the Copy Annotations command: " + 
						"no transcription and no list of files to process");
			}
			progressInterrupt("Cannot execute the Copy Annotations command: " + 
					"no transcription and no list of files to process specified");
			return;
		}
		// start the processing
		new CopyThread().start();
	}

	/**
	 * Removes the created annotations, recreates the deleted or modified annotations.
	 */
	@Override
	public void undo() {
		if (transcription != null) {
			if (copyAnnotations != null && !copyAnnotations.isEmpty()) {
				TierImpl t2 = transcription.getTierWithId(destinationTierName);
				if (t2 == null) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.warning("Cannot undo the Copy Annotations command: " + 
								"the destination tier cannot be found anymore");
					}
					return;
				}
				
				
				int curMode = transcription.getTimeChangePropagationMode();
				if (curMode != Transcription.NORMAL) {
					transcription.setTimeChangePropagationMode(Transcription.NORMAL);
				}
				transcription.setNotifying(false);
				
				// first delete created annotations
				for (AnnotationDataRecord record : copyAnnotations) {
					long mid = (record.getBeginTime() + record.getEndTime()) / 2;
					Annotation a = t2.getAnnotationAtTime(mid);
					if (a != null) {
						t2.removeAnnotation(a);
					}
				}
				
				// then re-create the original annotations
				if (deletedChildAnnotations != null && !deletedChildAnnotations.isEmpty()) {
					// t2 is a child tier of t1
					for (List<DefaultMutableTreeNode> chList : deletedChildAnnotations) {
						AnnotationRecreator.createAnnotationsSequentially(transcription, chList, true);
					}
				} else if ((deletedAnnotations != null && !deletedAnnotations.isEmpty()) || 
						(modifiedAnnotations != null && !modifiedAnnotations.isEmpty())) {
					// t2 is a top level tier
					for (DefaultMutableTreeNode node : deletedAnnotations) {
						AnnotationRecreator.createAnnotationFromTree(transcription, node, true);
					}
					
					for (DefaultMutableTreeNode node : modifiedAnnotations) {
						// delete the modified remainder of the annotation first or just overwrite
						// and rely on correct re-creation? Currently the latter is implemented.
						AnnotationRecreator.createAnnotationFromTree(transcription, node, true);
					}
				}
				
				transcription.setTimeChangePropagationMode(curMode);
				transcription.setNotifying(true);
			}
		}
	}

	/**
	 * Creates the copies again.
	 */
	@Override
	public void redo() {
		if (transcription != null) {
			if (copyAnnotations != null && !copyAnnotations.isEmpty()) {
				TierImpl t2 = transcription.getTierWithId(destinationTierName);
				if (t2 == null) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.warning("Cannot redo the Copy Annotations command: " + 
								"the destination tier cannot be found anymore");
					}
					return;
				}
				
				
				int curMode = transcription.getTimeChangePropagationMode();
				if (curMode != Transcription.NORMAL) {
					transcription.setTimeChangePropagationMode(Transcription.NORMAL);
				}
				transcription.setNotifying(false);
				// first delete again annotations on the destination tier if it is 
				// a symbolic dependent tier
				if (deletedChildAnnotations != null && !deletedChildAnnotations.isEmpty()) {
					for (List<DefaultMutableTreeNode> chList : deletedChildAnnotations) {
						if (!chList.isEmpty()) {
							AnnotationDataRecord r1 = (AnnotationDataRecord)chList.get(0).getUserObject();
							AnnotationDataRecord r2 = (AnnotationDataRecord)chList.get(chList.size() - 1).getUserObject();
							long bt = r1.getBeginTime();
							long et = r2.getEndTime();
							
							List<Annotation> overAnns = t2.getOverlappingAnnotations(bt, et);
							for (Annotation a : overAnns) {
								t2.removeAnnotation(a);
							}
						}
					}
				}
				// check deleted and modified annotations? or just create the annotations again
				// create the copies of annotations again
				createCopiesOfAnnotations(t2, copyAnnotations);
				
				transcription.setTimeChangePropagationMode(curMode);
				transcription.setNotifying(true);
			}
		}
	}

	/**
	 * Collects the annotations that are going to be copied. This depends on several settings:
	 * whether all annotations have to be copied or just those with specific values and 
	 * whether or not existing annotations on the destination tier can be overwritten.
	 *  
	 * @param t1 the source tier
	 * @param t2 the destination tier
	 * @return a list containing data records of the annotations to be copied
	 */
	private List<AbstractAnnotation> collectCandidateAnnotations (TierImpl t1, TierImpl t2) {
		List<AbstractAnnotation> copyAnnList = new ArrayList<AbstractAnnotation>();
		
		List<AbstractAnnotation> srcAnns = t1.getAnnotations();
		for (AbstractAnnotation aa : srcAnns) {
			// first condition for copying, either overwrite or there is no annotation there 
			if (overwrite || t2.getOverlappingAnnotations(aa.getBeginTimeBoundary(), aa.getEndTimeBoundary()).isEmpty()) {
				if (copyAllAnnotations) {
					copyAnnList.add(aa);
				} else { // check contents
					if (isRegex) {
						Matcher m = pattern.matcher(aa.getValue());
						if (m.matches()) {// matches the whole contents
							copyAnnList.add(aa);
						}
					} else {// exact match
						if (queryValue.equals(aa.getValue())) {
							copyAnnList.add(aa);
						}
					}
				}
			}
		}
		
		return copyAnnList;			
	}
	
	/**
	 * Stores, for undo, records of annotations that are going to be removed or modified by the copy operation. 
	 * annotations on depending tiers are also immediately removed here.
	 * 
	 * @param t1 the source tier
	 * @param t2 the destination tier
	 * @param collectedAnnotations records of the annotations that are going to be copied
	 */
	private void processCollectedAnnotation(TierImpl t1, TierImpl t2, List<AbstractAnnotation> collectedAnnotations) {
		boolean isChildTier = t2.hasAncestor(t1);// has parent?
		
		for (AbstractAnnotation aa : collectedAnnotations) {
			long bt = aa.getBeginTimeBoundary();
			long et = aa.getEndTimeBoundary();
			List<Annotation> overlappingAnn = t2.getOverlappingAnnotations(bt, et);
			if (!overlappingAnn.isEmpty()) {
				if (isChildTier) {
					// store
					List<DefaultMutableTreeNode> chGroup = new ArrayList<DefaultMutableTreeNode>(overlappingAnn.size());
					for (Annotation a : overlappingAnn) {
						chGroup.add(AnnotationRecreator.createTreeForAnnotation((AbstractAnnotation)a));
					}
					deletedChildAnnotations.add(chGroup);
					// and delete
					for (Annotation a : overlappingAnn) {
						t2.removeAnnotation(a);
					}
				} else {
					for (Annotation a : overlappingAnn) {
						AbstractAnnotation ovAnn = (AbstractAnnotation) a;
						if (TimeRelation.isInside(ovAnn, bt, et)) {
							// target annotation will be deleted when the source annotation is copied
							deletedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(ovAnn));
						} else if (TimeRelation.overlapsOnLeftSide(ovAnn, bt, et) || 
								TimeRelation.overlapsOnRightSide(ovAnn, bt, et) || 
								TimeRelation.surrounds(ovAnn, bt, et)) {
							// target annotation will be modified when the source annotation is copied
							modifiedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(ovAnn));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Checks the data records to be copied and removes CV entry references if the destination tier is not 
	 * linked to a controlled vocabulary or not to the same CV as the source tier. 
	 * This should probably be extended to other types of external references (e.g. lexicon fields).
	 * 
	 * @param copyAnnotations the records of the annotations to be copied 
	 * @param sourceCVName the name of the CV linked to the source tier, can be null
	 * @param destinationTier  the name of the CV linked to the destination tier, can be null
	 */
	private void updateAnnotationRecords(List<AnnotationDataRecord>copyAnnotations, String sourceCVName, String destinationCVName) {
		if (sourceCVName != null && !sourceCVName.equals(destinationCVName)) {
			for (AnnotationDataRecord adr : copyAnnotations) {
				adr.setCvEntryId(null);
			}
		}
	}
	
	/**
	 * Actually creates the copies of the selected annotations.
	 * 
	 * @param t2 the destination tier
	 * @param copyAnnotations a list of records to be used for the creation of new annotations on
	 * the destination tier
	 */
	private void createCopiesOfAnnotations(TierImpl t2, List<AnnotationDataRecord> copyAnnotations) {
		// on symbolic tiers the average of begin and end time is used to specify where 
		// to create the annotation 
		boolean symbolicTier = false;
		Constraint c = t2.getLinguisticType().getConstraints();
		if (c != null && (c.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION ||
				c.getStereoType() == Constraint.SYMBOLIC_SUBDIVISION)) {
			symbolicTier = true;
		}
		
		for (AnnotationDataRecord record : copyAnnotations) {
			long bt = record.getBeginTime();
			long et = record.getEndTime();
			if (symbolicTier) {
				bt = (bt + et) / 2;
				et = bt;
			}
			AbstractAnnotation na = (AbstractAnnotation) t2.createAnnotation(bt, et);
			
			if (na != null) {
				AnnotationRecreator.restoreValueEtc(na, record, false);
			}
		}
	}

	/**
	 * The steps involved in the copy operation are performed on a separate thread so that progress
	 * can be monitored. This will be more useful in the multiple file situation than in the 
	 * single transcription situation.  
	 */
	private class CopyThread extends Thread {
	
		@Override
		public void run() {
			if (transcription != null) {
				progressUpdate(5, "Starting to copy annotations");
				
				TierImpl sourceTier = transcription.getTierWithId(sourceTierName);
				if (sourceTier == null) {
					progressInterrupt("The source tier is not specified or not found");
					return;
				}
				
				TierImpl destinationTier = transcription.getTierWithId(destinationTierName);
				if (destinationTier == null) {
					progressInterrupt("The destination tier is not specified or not found");
					return;
				}
				
				if (sourceTier.getLinguisticType().getControlledVocabularyName() != null) {
					sourceCVName = sourceTier.getLinguisticType().getControlledVocabularyName();
				}
				if (destinationTier.getLinguisticType().getControlledVocabularyName() != null) {
					destinationCVName = destinationTier.getLinguisticType().getControlledVocabularyName();
				}
				
				progressUpdate(10, "Collecting annotations to copy");
				List<AbstractAnnotation> collectedAnnotations = collectCandidateAnnotations(sourceTier, destinationTier);
				if (collectedAnnotations.isEmpty()) {
					progressInterrupt("There are no matching annotations to be copied");
				}
				// store records before delete or modification
				deletedAnnotations = new ArrayList<DefaultMutableTreeNode>();
				deletedChildAnnotations = new ArrayList<List<DefaultMutableTreeNode>>();
				modifiedAnnotations = new ArrayList<DefaultMutableTreeNode>();
				copyAnnotations = new ArrayList<AnnotationDataRecord>();
				
				// create records of the original annotations
				for (AbstractAnnotation aa : collectedAnnotations) {
					copyAnnotations.add(new AnnotationDataRecord(aa));
				}
				progressUpdate(20, String.format("Collected annotation information, %d annotations to copy", 
						collectedAnnotations.size()));
				
				int curMode = transcription.getTimeChangePropagationMode();
				if (curMode != Transcription.NORMAL) {
					transcription.setTimeChangePropagationMode(Transcription.NORMAL);
				}
				transcription.setNotifying(false);
				processCollectedAnnotation(sourceTier, destinationTier, collectedAnnotations);
				
				progressUpdate(40, "Stored annotations to be deleted or modified"); 
				
				// update data records
				updateAnnotationRecords(copyAnnotations, sourceCVName, destinationCVName);
				progressUpdate(50, "Checked CV entry references of the source annotations"); 
				// create new annotations
				createCopiesOfAnnotations(destinationTier, copyAnnotations);
				progressUpdate(90, "Created the new annotations"); 
				
				transcription.setTimeChangePropagationMode(curMode);
				transcription.setNotifying(true);
				progressComplete("Finished copying annotations");
			}
		}
	}

}

package mpi.eudico.client.annotator.interlinear.edit.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.interlinear.edit.TextAnalyzerLexiconHostContext;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAnnotation;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;

/**
 * An action that triggers an Add New Entry dialog based on the 
 * contents of the specified annotation and the association of the tier
 * with a lexicon field.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class IGTAddToLexiconAction extends IGTEditAction {// could implement and register as ActiveAnnotationListener via the ViewerManager
	private TextAnalyzerLexiconHostContext lexHostContext;

	/**
	 * Constructor.
	 * @param igtAnnotation the annotation providing the initial value, can be null
	 * @param lexHostContext the lexicon context to call
	 * @param label the text for the action
	 */
	public IGTAddToLexiconAction(IGTAnnotation igtAnnotation, 
			TextAnalyzerLexiconHostContext lexHostContext, String label) {
		super(igtAnnotation, label);
		this.lexHostContext = lexHostContext;
	}

	/**
	 * If this action is created with a non-null IGTAnnotation, this one is used to
	 * find the actual annotation and the link to a lexicon field. If the IGTAnnotation
	 * is null, the "global" active annotation is used to get the required information.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (lexHostContext != null) {
			AbstractAnnotation annotation = null;
			
			if (igtAnnotation != null) {
				annotation = igtAnnotation.getAnnotation();
			} else {
				Transcription tr = lexHostContext.getTranscription();
				ViewerManager2 vm = ELANCommandFactory.getViewerManager(tr);
				annotation = (AbstractAnnotation) vm.getActiveAnnotation().getAnnotation();
			}
			// lexiconBundle contains the name of the lexicon and the name of the field, the bundle can be null
			LexiconQueryBundle2 lexiconBundle = null;
			if (annotation != null) {
				lexiconBundle = annotation.getTier().getLinguisticType().getLexiconQueryBundle();
			} else {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.info("Add to Lexicon action called without a selected or active annotation");
				}
				return;
			}
			
			if (lexiconBundle != null) {
				Map<String, String> initValues = new HashMap<String, String>(6);
				initValues.put(lexiconBundle.getFldId().getName(), annotation.getValue());
				// if the annotation has a parent or children that are also connected to a lexicon
				// field add these fields too?
				addAncestorFields(annotation, initValues);
				addDescendantFields(annotation, initValues);
				
				lexHostContext.initiateCreateNewEntry(lexiconBundle, initValues);
			} else {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.info("Cannot add to lexicon: the tier of this annotation is not linked to a lexicon field");
				}
			}
		} else {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.info("Cannot add to lexicon: lexicon host context is missing");
			}
		}
	}

	/**
	 * Adds values of ancestor annotations if linked to a lexicon field too, recursive.
	 * 
	 * @param annotation the annotation to check
	 * @param fieldValues the map to add fields and values to
	 */
	private void addAncestorFields(AbstractAnnotation annotation, Map<String, String> fieldValues) {
		AbstractAnnotation parentAnn = (AbstractAnnotation) annotation.getParentAnnotation();
		
		if (parentAnn != null) {
			LexiconQueryBundle2 lexiconBundle = parentAnn.getTier().getLinguisticType().getLexiconQueryBundle();
			if (lexiconBundle != null) {
				fieldValues.put(lexiconBundle.getFldId().getName(), parentAnn.getValue());
			}
			
			addAncestorFields(parentAnn, fieldValues);
		}
	}
	
	/**
	 * Adds values of descendant annotations if these are linked to a lexicon field too, recursive.
	 * 
	 * @param annotation the annotation to check
	 * @param fieldValues the map to add fields and values to
	 */
	private void addDescendantFields(AbstractAnnotation annotation, Map<String, String> fieldValues) {
		Tier t = annotation.getTier();
		List<? extends Tier> childList = t.getChildTiers();
		
		for (Tier cht : childList) {
			if (cht.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
				if (cht.getLinguisticType().getLexiconQueryBundle() != null) {
					AbstractAnnotation chAnn = (AbstractAnnotation) ((TierImpl) cht).getAnnotationAtTime(
							(annotation.getBeginTimeBoundary() + annotation.getEndTimeBoundary()) / 2);
					if (chAnn != null) {
						if (!chAnn.getValue().isEmpty()) {
							fieldValues.put(cht.getLinguisticType().getLexiconQueryBundle().getFldId().getName(), chAnn.getValue());
						}
						addDescendantFields(chAnn, fieldValues);
					} 
				}
			}
		}
	}
	
}

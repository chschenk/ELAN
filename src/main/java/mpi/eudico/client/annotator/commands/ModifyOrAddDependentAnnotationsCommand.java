package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * An undoable command that takes the active annotation as input, and creates
 * new annotations on all the given tiers, using the same start/end time as the
 * active annotation. This is then one undoable command in the undo/redo list.
 * 
 * @author Olaf Seibert
 */

public class ModifyOrAddDependentAnnotationsCommand
					implements UndoableCommand, ClientLogger {

	private String commandName;
    private TranscriptionImpl transcription;   
    private long begin;
    private long end;
    private Object[] arguments;
    private List<String> prevTexts;			/** for undo */
    private List<ExternalReference> prevExtRefs;		/** for undo */
    private List<String> prevCveIds;		/** for undo */
    
    /**
     * Creates a new CreateDependentAnnotationsCommand instance
     * 
     * @param name the name of the command
     */
    public ModifyOrAddDependentAnnotationsCommand(String name) {
    	commandName = name;
    }
    

	/**
	 * The execute method receives a variable number of arguments that
	 * must be a multiple of 4.
	 * arguments[4 * i + 0]: tier name
	 * arguments[4 * i + 1]: desired annotation text
 	 * arguments[4 * i + 2]: if not null: extRef
 	 * arguments[4 * i + 3]: if not null: cveId
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		Annotation ann = (Annotation)receiver;
		
		begin = ann.getBeginTimeBoundary();
		end = ann.getEndTimeBoundary();
		this.arguments = arguments;
		TierImpl tier = (TierImpl) ann.getTier();
		transcription = (tier.getTranscription());
		
		redo();
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
	 * Restore the old situation before adding/changing the annotations.
	 * This is done by means of the tier names which were remembered,
	 * and the previous texts and external references of the annotations
	 * that were there. They are null if the annotation was created. 
	 */
	@Override
	public void undo() {
        int t = 0;

        transcription.setNotifying(false);

        for (int x = 0; x < this.arguments.length; x += 4) {
        	String tiername = (String)this.arguments[x];
        	
        	String oldAnnotationText = prevTexts.get(t);
        	ExternalReference oldExtRef = prevExtRefs.get(t);
        	String oldCveId = prevCveIds.get(t);
        	t++;

			TierImpl tier = transcription.getTierWithId(tiername);
			Annotation ann;

			if (tier.isTimeAlignable()) {
				ann = tier.getAnnotationAtTime(begin);
			} else {
				long time = (begin + end) / 2;
				ann = tier.getAnnotationAtTime(time);
			}

			if (ann != null) {
				if (oldAnnotationText == null) {
					tier.removeAnnotation(ann);
				} else {
	                ((AbstractAnnotation) ann).setExtRef(oldExtRef);
	                ann.setCVEntryId(oldCveId);
					ann.setValue(oldAnnotationText);
				}
			}
        }
        
        transcription.setNotifying(true);
	}

	/**
	 * redo() actually also does most of the work of execute().
	 */
	@Override
	public void redo() {
    	prevTexts = new ArrayList<String>();
    	prevExtRefs = new ArrayList<ExternalReference>();
    	prevCveIds = new ArrayList<String>();
    	
    	int curPropMode = 0;
        curPropMode = transcription.getTimeChangePropagationMode();
        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }
                
        Tier firstTier = null;

        for (int x = 0; x < this.arguments.length; x += 4) {
        	String tiername = (String)this.arguments[x];
        	String newAnnotationText = (String)this.arguments[x + 1];
        	ExternalReference extRef = (ExternalReference)this.arguments[x + 2];
        	String cveId = (String)this.arguments[x + 3];

        	TierImpl tier = transcription.getTierWithId(tiername);

        	Annotation ann = tier.getAnnotationAtTime(begin);
        	if (ann == null) {
        		if (firstTier == null) {
        			firstTier = tier;
        		}
        		if (tier.isTimeAlignable()) {
        			ann = tier.createAnnotation(begin, end);
        		} else {
        			long time = (begin + end) / 2;
        			ann = tier.createAnnotation(time, time);	            		
        		} 
        		prevTexts.add(null);
        		prevExtRefs.add(null);
        		prevCveIds.add(null);
        	} else {
        		prevTexts.add(ann.getValue());
        		ExternalReference oldExtRef = null;

        		try {
            		AbstractAnnotation aa = (AbstractAnnotation)ann;
           			oldExtRef = aa.getExtRef();
           			if (oldExtRef != null) {
           				oldExtRef = oldExtRef.clone();
           			}
    			} catch (CloneNotSupportedException e) {
    				e.printStackTrace();
    			}
        		
        		prevExtRefs.add(oldExtRef);
        		prevCveIds.add(ann.getCVEntryId());
        	}
        	ann.setCVEntryId(cveId);
        	ann.setValue(newAnnotationText);
			ModifyAnnotationCommand.replaceExternalReference((AbstractAnnotation)ann, extRef);
        }

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);       
	}	
}

package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.imports.MergeUtil;
import mpi.eudico.client.annotator.imports.UndoableTranscriptionMerger;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
/**
 * This class merges two transcriptions without creating a third one, so the 
 * merging should be undoable. 
 * Note May 2015: this is all only partially implemented. Several scenario's are not supported yet. 
 */
public class MergeTranscriptionsByAddingCommand implements UndoableCommand {
	private String name;
    private TranscriptionImpl firstTrans;
    private TranscriptionImpl secondTrans;
    private UndoableTranscriptionMerger merger;
    
    private List<String> tierNames;
    // not yet implemented
    private boolean processSameNameTiers = false;
    private boolean overwrite = false;
    // not yet implemented
    private boolean addLinkedFiles = false;
    // by default a compatibility check is performed on the tiers to add, this can be circumvented
    private boolean checkTierCompatibility = true;
    
	/**
	 * A command to add the tiers and annotations from one transcription to another.
	 * This command does not create a copy of the first transcription, but adds directly 
	 * to the first transcription. This means that the action must be undoable.
	 * 
	 * @param name the name of the command
	 */
	public MergeTranscriptionsByAddingCommand(String name) {
		super();
		this.name = name;
	}

	@Override
	public void redo() {
		if (merger != null) {
			merger.redo();
		}

	}

	/**
	 * This method delegates the implementation of undo to the UndoableTranscriptionMerger instance
	 * that did the merging.
	 */
	@Override
	public void undo() {
		if (merger != null) {
			merger.undo();
		}

	}

	/**
	 * The actual action. The arguments are expected to be in the right order.
	 * 
	 * @param receiver the first, receiving transcription
	 * @param arguments the arguments
	 * <ul>
	 * <li>
	 * arguments[0] = the second transcription (TranscriptionImpl)
	 * <li>
	 * arguments[1] = a list of names of tiers to merge/import, can be null in which case all tiers are processed
	 * <li>
	 * arguments[2] = a flag to indicate whether tiers with the same name 
	 * in both transcriptions need to be processed as well (Boolean, optional, default: false)
	 * <li>
	 * arguments[3] = a flag to indicate whether annotations on tiers with the same name 
	 * may be overwritten or not (Boolean, optional, default: false)
	 * <li>
	 * arguments[4] = a flag to indicate whether the linked files of the second transcription 
	 * must be added as well (Boolean, optional, default: false)
	 * <li>
	 * arguments[5] = a flag to indicate whether tier compatibility should be checked 
	 *  (Boolean, optional, default: true)
	 * </ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		firstTrans = (TranscriptionImpl) receiver;
		secondTrans = (TranscriptionImpl) arguments[0];
		tierNames = (List<String>) arguments[1];
		
		if (arguments.length > 2) {
			processSameNameTiers = (Boolean) arguments[2];
		}
		if (arguments.length > 3) {
			overwrite = (Boolean) arguments[3];
		}		
		if (arguments.length > 4) {
			addLinkedFiles = (Boolean) arguments[4];
		}
		if (arguments.length > 5) {
			checkTierCompatibility = (Boolean) arguments[5];
		}
		
		startMerge();
		
	}

	/**
	 * Starts the merging process.
	 */
	private void startMerge() {
		if (checkTierCompatibility) {
			MergeUtil mergeUtil = new MergeUtil();
			// contains tier objects
			List<TierImpl> addableTiers = mergeUtil.getAddableTiers(secondTrans, firstTrans, tierNames);
			List<String> addableTierNames = new ArrayList<String>(addableTiers.size());
			
			Tier t;
			for (int i = 0; i < addableTiers.size(); i++) {
				t = addableTiers.get(i);
				addableTierNames.add(t.getName());
			}
			// call methods on the UndoableTranscriptionMerger
			merger = new UndoableTranscriptionMerger();
			merger.mergeWith(firstTrans, secondTrans, overwrite, addableTierNames);
		} else {
			// call methods on the UndoableTranscriptionMerger
			merger = new UndoableTranscriptionMerger();
			merger.mergeWith(firstTrans, secondTrans, overwrite, tierNames);
		}
	}
	
	/**
	 * Returns the name of the command
	 */
	@Override
	public String getName() {
		return name;
	}

}

package mpi.eudico.client.annotator.imports;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.ConstraintImpl;
import mpi.eudico.server.corpora.clomimpl.type.IncludedIn;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicSubdivision;
import mpi.eudico.server.corpora.clomimpl.type.TimeSubdivision;
/**
 * A class that merges elements from one transcription to another transcription such 
 * that the merging can be undone.  
 * There are several options for the merging; if a selection of tiers is provided
 * those tiers and everything connected to them is copied else everything is copied
 * (types, controlled vocabularies, external refs, doc properties)
 * Copying of linked files etc is not (yet) provided for.
 *  
 * @author Han Sloetjes
 */
public class UndoableTranscriptionMerger {
	private TranscriptionImpl transA;
	private TranscriptionImpl transB;
	
    private Map<AbstractAnnotation, DefaultMutableTreeNode> createdAnnotations;
    private Map<AbstractAnnotation, DefaultMutableTreeNode> deletedAnnotations;
    private Map<AbstractAnnotation, DefaultMutableTreeNode> modifiedAnnotations;
    private List<ArrayList<DefaultMutableTreeNode>> createdAnnotationSequences;
    
    private Map<String, TierImpl> createdTierMap;
    private Map<String, LinguisticType> createdTypeMap;
    //private Map<String, ControlledVocabulary> createdCVMap;
    
    private List<String> tiersToAddList;
    private boolean addEverything;
    private MergeUtil mergeUtil;
	
	/**
	 * Constructor.
	 */
	public UndoableTranscriptionMerger() {
		super();
		mergeUtil = new MergeUtil();
		createdAnnotations = new HashMap<AbstractAnnotation, DefaultMutableTreeNode>();
		deletedAnnotations = new HashMap<AbstractAnnotation, DefaultMutableTreeNode>();
		modifiedAnnotations = new HashMap<AbstractAnnotation, DefaultMutableTreeNode>();
		createdAnnotationSequences = new ArrayList<ArrayList<DefaultMutableTreeNode>>();
		createdTierMap = new LinkedHashMap<String, TierImpl>();
		createdTypeMap = new LinkedHashMap<String, LinguisticType>();
		//createdCVMap = new LinkedHashMap<String, ControlledVocabulary>();
	}
	
	/**
	 * Merges the content of the second transcription into the first. Such that it is undoable.
	 *  
	 * @param first the first transcription, the destination or receiver
	 * @param second the second transcription, the supplier
	 */
	public void mergeWith(TranscriptionImpl first, TranscriptionImpl second) {
		if (transA != null) {
			//throw Illegal state exception or something
		}
		
		if (first == null) {
			throw new NullPointerException("The first transcription is null");
		}
		if (second == null) {
			throw new NullPointerException("The second transcription is null");
		}
		
		mergeWith(first, second, false);
	}

	/**
	 * Merges the content of the second transcription into the first. Such that it is undoable.
	 *  
	 * @param first the first transcription, the destination or receiver
	 * @param second the second transcription, the supplier
	 * @param overwrite a flag to indicate whether annotations maybe overwritten
	 */
	public void mergeWith(TranscriptionImpl first, TranscriptionImpl second, boolean overwrite) {
		if (transA != null) {
			//throw Illegal state exception or something
		}
		
		if (first == null) {
			throw new NullPointerException("The first transcription is null");
		}
		if (second == null) {
			throw new NullPointerException("The second transcription is null");
		}
		
		mergeWith(first, second, overwrite, null);
	}
	
	/**
	 * Merges the content of the second transcription into the first. Such that it is undoable.
	 *  
	 * @param first the first transcription, the destination or receiver
	 * @param second the second transcription, the supplier
	 * @param overwrite a flag to indicate whether annotations maybe overwritten
	 * @param tiersToAdd a list of names of tiers that should be added to the first transcription,
	 * if null all tiers will be added
	 */
	public void mergeWith(TranscriptionImpl first, TranscriptionImpl second, 
			boolean overwrite, List<String> tiersToAdd) {
		if (transA != null) {
			//throw Illegal state exception or something
		}
		
		if (first == null) {
			throw new NullPointerException("The first transcription is null");
		}
		if (second == null) {
			throw new NullPointerException("The second transcription is null");
		}
		
		transA = first;
		transB = second;
		List<String> addedTiers = new ArrayList<String>();
		tiersToAddList = new ArrayList<String>();

		// merge annotations/tiers/types/cv's/external refs/doc properties?
		if (tiersToAdd == null) {// add all tiers
			addEverything = true;
			getTierNames2(transB, tiersToAddList);
		} else {
			addEverything = false;
			tiersToAddList.addAll(tiersToAdd);
		}
		// sort tier names, so that parent tiers are processed before any descendant tiers
		tiersToAddList = mergeUtil.sortTiers(transB, tiersToAddList);
		List<TierImpl> sortedTierObj = mergeUtil.getSortedTiers(transB, tiersToAddList);
		
		if (sortedTierObj == null || sortedTierObj.size() == 0) {
			// the specified tiers weren't actually in the second transcription
			// log or throw exception?
			ClientLogger.LOG.warning("The requested tiers are not in the transcription.");
			return;
		}
	
		TierImpl tier;
		String tierName;
		for (int i = 0; i < sortedTierObj.size(); i++) {
			tier = (TierImpl) sortedTierObj.get(i);
			tierName = tier.getName();
			if (transA.getTierWithId(tierName) == null) {
				// add new tier...
				if (tier.getParentTier() == null) {
					addNewTopLevelTier(tier, sortedTierObj);
				} else {
					addNewDependingTier(tier, sortedTierObj);
				}
				addedTiers.add(tierName);
			}
		}
		
		// if add everything is true all tiers are in the sorted tier list and have been added?
		if (addEverything) {
			// add the
			for (int i = 0; i < sortedTierObj.size(); i++) {
				tier = (TierImpl) sortedTierObj.get(i);
				tierName = tier.getName();
				
				if (addedTiers.contains(tierName)) {
					continue;
				}
				
				if (transA.getTierWithId(tierName) == null) {
					ClientLogger.LOG.info("Adding a tier in a second iteration: " + tierName);
					// add new tier...
					if (tier.getParentTier() == null) {
						addNewTopLevelTier(tier, sortedTierObj);
					} else {
						addNewDependingTier(tier, sortedTierObj);
					}
				}
			}
		}
		
		transA.setNotifying(false);
		int origPropMode = transA.getTimeChangePropagationMode();
		
		if (origPropMode != Transcription.NORMAL) {
			transA.setTimeChangePropagationMode(Transcription.NORMAL);
		}
		
		// after adding the tiers create annotations
		// of the created tiers and the already existing tiers
		List<String> tiersWithoutParentInGroup = mergeUtil.getTiersWithoutParentInGroup(transA, tiersToAddList);
		
		for (int i = 0; i < tiersWithoutParentInGroup.size(); i++) {
			tier = (TierImpl) transB.getTierWithId(tiersWithoutParentInGroup.get(i));
			if (tier.hasParentTier()) {
				addAnnotationsToDependentTier(tier, tiersToAddList);
			} else {
				addAnnotations(tier, tiersToAddList);
			}
		}
		
		if (origPropMode != Transcription.NORMAL) {
			transA.setTimeChangePropagationMode(origPropMode);
		}
		transA.setNotifying(true);
	}
	
	/**
	 * Reverts the changes made to the destination transcription. 
	 */
	public void undo() {		
		// delete the created tiers, reverse order
		String tierName;
		TierImpl t;
		
		String[] tierKeys = createdTierMap.keySet().toArray(new String[0]);
		for (int i = tierKeys.length - 1; i >= 0; i--) {
			tierName = tierKeys[i];
			t = (TierImpl) transA.getTierWithId(tierName);
			if (t != null) {
				t.removeAllAnnotations();
			}
			transA.removeTier(t);
		}
		// TODO delete the created CV's
		
		// delete the created types
		String typeName;
		LinguisticType lt;
		String[] typeKeys = createdTypeMap.keySet().toArray(new String[0]);
		for (int i = typeKeys.length - 1; i >= 0; i--) {
			typeName = typeKeys[i];
			lt = transA.getLinguisticTypeByName(typeName);
			
			if (lt != null) {
				transA.removeLinguisticType(lt);
			}
		}
		
		transA.setNotifying(false);
		int origPropMode = transA.getTimeChangePropagationMode();
		
		if (origPropMode != Transcription.NORMAL) {
			transA.setTimeChangePropagationMode(Transcription.NORMAL);
		}
		
		// delete the created annotations (if they haven't been deleted yet)
		Iterator<AbstractAnnotation> caIter = createdAnnotations.keySet().iterator();
		AbstractAnnotation createdAnn, curAnn;

		while (caIter.hasNext()) {
			createdAnn = caIter.next();
			t = (TierImpl) createdAnn.getTier();
			if (t == null) {
				continue;
			}
			if (transA.getTierWithId(t.getName()) == null) {
				continue;// the tier has been deleted
			}
			curAnn = (AbstractAnnotation) t.getAnnotationAtTime(
					(createdAnn.getBeginTimeBoundary() + createdAnn.getEndTimeBoundary())  / 2);
			if (curAnn != null) {
				t.removeAnnotation(curAnn);
			}
		}
		
		// recreate the modified annotations
		undoModifiedAnnotations();
		
		Iterator<AbstractAnnotation> modIter = modifiedAnnotations.keySet().iterator();
		AbstractAnnotation modAnn, recreatedModAnn;
		DefaultMutableTreeNode modNode;
		
		while (modIter.hasNext()) {
			modAnn = modIter.next();
			modNode = modifiedAnnotations.get(modAnn);
			recreatedModAnn = AnnotationRecreator.createAnnotationFromTree(transA, modNode, true);
			// check and log
			if (recreatedModAnn == null) {
				ClientLogger.LOG.warning("Could not recreate a modified annotation: " + modAnn.getTier().getName() + 
						" BT: " + modAnn.getBeginTimeBoundary());
			}
		}
		
		// recreate the deleted annotations
		Iterator<AbstractAnnotation> delIter = deletedAnnotations.keySet().iterator();
		AbstractAnnotation deletedAnn, recreatedAnn;
		DefaultMutableTreeNode delNode;
		
		while (delIter.hasNext()) {
			deletedAnn = delIter.next();
			delNode = deletedAnnotations.get(deletedAnn);
			recreatedAnn = AnnotationRecreator.createAnnotationFromTree(transA, delNode, true);
			// check and log
			if (recreatedAnn == null) {
				ClientLogger.LOG.warning("Could not recreate a deleted annotation: " + deletedAnn.getTier().getName() + 
						" BT: " + deletedAnn.getBeginTimeBoundary());
			}
		}
		
		if (origPropMode != Transcription.NORMAL) {
			transA.setTimeChangePropagationMode(origPropMode);
		}
		transA.setNotifying(true);
	}
	
	private void undoModifiedAnnotations() {
		Iterator<AbstractAnnotation> modIter = modifiedAnnotations.keySet().iterator();
		AbstractAnnotation modAnn;
		DefaultMutableTreeNode modNode;
		AnnotationDataRecord annRecord;
		
		while (modIter.hasNext()) {
			modAnn = modIter.next();
			modNode = modifiedAnnotations.get(modAnn);
			annRecord = (AnnotationDataRecord) modNode.getUserObject();
			String tierName = annRecord.getTierName();
			TierImpl tier = (TierImpl) transA.getTierWithId(tierName);
			// the tier is always a top level tier since for depending annotations the root annotation is stored?
			// first try for depending tiers
			long mid = (annRecord.getBeginTime() + annRecord.getEndTime()) / 2;
			modAnn = (AbstractAnnotation) tier.getAnnotationAtTime(mid);
			
			if (modAnn == null) {
				modAnn = (AbstractAnnotation) tier.getAnnotationAtTime(annRecord.getBeginTime());
				if (modAnn == null) {
					modAnn = (AbstractAnnotation) tier.getAnnotationAtTime(annRecord.getEndTime() - 1);
				}
			}
			
			if (modAnn != null) {
				tier.removeAnnotation(modAnn);
			}
		}
	}
	
	/**
	 * Merges again.
	 */
	public void redo() {
		// TODO recreate CV's
		// TODO recreate EXT_REF's ?
		
		// recreate/add types
		String typeName;
		LinguisticType lt;
		String[] typeKeys = createdTypeMap.keySet().toArray(new String[0]);
		for (int i = 0; i < typeKeys.length; i++) {
			typeName = typeKeys[i];
			lt = transA.getLinguisticTypeByName(typeName);
			
			if (lt == null) {
				transA.addLinguisticType(createdTypeMap.get(typeName));
			}
		}
		
		// recreate tiers
		String tierName;
		TierImpl t;
		TierImpl nextTier;
		
		String[] tierKeys = createdTierMap.keySet().toArray(new String[0]);
		for (int i = 0; i < tierKeys.length; i++) {
			tierName = tierKeys[i];
			t = (TierImpl) transA.getTierWithId(tierName);
			if (t == null) {
				// create new tier and add
				TierImpl oldTier = createdTierMap.get(tierName);
				lt = transA.getLinguisticTypeByName(oldTier.getLinguisticType().getLinguisticTypeName());
				if (lt == null) {
					// something wrong, log
					continue;
				}
				TierImpl parentTier = null;
				if (oldTier.getParentTier() != null) {
					parentTier = (TierImpl) transA.getTierWithId(oldTier.getParentTier().getName());
					// assert that parentTier != null?
				}
				if (parentTier == null) {
					nextTier = new TierImpl(tierName, oldTier.getParticipant(), transA, lt);
				} else {
					nextTier = new TierImpl(parentTier, tierName, oldTier.getParticipant(), transA, lt);
				}
				nextTier.setAnnotator(oldTier.getAnnotator());
				nextTier.setDefaultLocale(oldTier.getDefaultLocale());
				nextTier.setLangRef(oldTier.getLangRef());
				transA.addTier(nextTier);
			}
		}
		
		transA.setNotifying(false);
		int origPropMode = transA.getTimeChangePropagationMode();
		
		if (origPropMode != Transcription.NORMAL) {
			transA.setTimeChangePropagationMode(Transcription.NORMAL);
		}
		
		Iterator<AbstractAnnotation> annIter = createdAnnotations.keySet().iterator();
		AbstractAnnotation aa, nextAa;
		DefaultMutableTreeNode node;
		
		while (annIter.hasNext()) {
			aa = annIter.next();
			node = createdAnnotations.get(aa);
			nextAa = AnnotationRecreator.createAnnotationFromTree(transA, node);
			// test whether recreation was successful 
			// check and log
			if (nextAa == null) {
				ClientLogger.LOG.warning("Could not create the annotation again in redo: " + aa.getTier().getName() + 
						" BT: " + aa.getBeginTimeBoundary());
			}
		}
		
		// annotation sequences
		Iterator<ArrayList<DefaultMutableTreeNode>> listIter = createdAnnotationSequences.iterator();
		ArrayList<DefaultMutableTreeNode> nextList;
		
		while (listIter.hasNext()) {
			nextList = listIter.next();
			AnnotationRecreator.createAnnotationsSequentially(transA, nextList, false);
		}
		
		if (origPropMode != Transcription.NORMAL) {
			transA.setTimeChangePropagationMode(origPropMode);
		}
		transA.setNotifying(true);
	}
	
	/**
	 * Adds the names of all tiers in the transcription to the specified list.  
	 * 
	 * @param trans the transcription
	 * @param listToAddTo to this list the tier names are added.
	 */
	private void getTierNames2(TranscriptionImpl trans, List<String> listToAddTo) {
		if (trans == null || listToAddTo == null) {
			return;
		}
		
		List<TierImpl> tiers = trans.getTiers();
		Tier t;
		for (int i = 0; i < tiers.size(); i++) {
			t = tiers.get(i);
			listToAddTo.add(t.getName());
		}		
	}
	
	/**
	 * Creates a new top level tier and possibly sub-tiers. 
	 * Note: the private methods assume that both transcription objects are not null. 
	 * 
	 * @param tier the tier to add
	 * @param allTiersToAdd the list of all tiers to add // TODO currently not used
	 */
	private void addNewTopLevelTier(TierImpl tier, List<? extends Tier> allTiersToAdd) {
		if (tier == null || tier.getParentTier() != null) {
			return;
		}
		
		TierImpl nextTier = null;
		LinguisticType origType = tier.getLinguisticType();
//		String origCVName = origType.getControlledVocabularyName();
		
		// check if the linguistic type is already there
		String ltName = origType.getLinguisticTypeName();		
		LinguisticType curType = transA.getLinguisticTypeByName(ltName);
		if (curType != null) {
			boolean compatible = mergeUtil.lingTypeCompatible(curType, tier.getLinguisticType());
			if (compatible) {
				// use it for the new copy of the tier
				nextTier = new TierImpl(tier.getName(), tier.getParticipant(), transA, curType);
				nextTier.setAnnotator(tier.getAnnotator());
				nextTier.setDefaultLocale(tier.getDefaultLocale());
				nextTier.setLangRef(tier.getLangRef());
				createdTierMap.put(nextTier.getName(), nextTier);
			} else {
				// add a new linguistic type, adjust the name
				String nextLTName = getUniqueLTName(transA, ltName);
				if (nextLTName != null) {
					// create and add the new linguistic type
					LinguisticType nextType = new LinguisticType(nextLTName);
					nextType.setTimeAlignable(origType.isTimeAlignable());// true for a top level tier
					// for a top level tier the constraints = null
					// TODO check CV and data category, external references etc.
					transA.addLinguisticType(nextType);
					createdTypeMap.put(nextLTName, nextType);
					
					nextTier = new TierImpl(tier.getName(), tier.getParticipant(), transA, nextType);
					nextTier.setAnnotator(tier.getAnnotator());
					nextTier.setDefaultLocale(tier.getDefaultLocale());
					nextTier.setLangRef(tier.getLangRef());
					createdTierMap.put(nextTier.getName(), nextTier);
				} // else don't create a type and don't create the tier
			}
		} else {// add the linguistic type with the same name
			LinguisticType nextType = new LinguisticType(ltName);
			nextType.setTimeAlignable(origType.isTimeAlignable());// true for a top level tier
			// for a top level tier the constraints = null
			// TODO check CV and data category, external references etc.
			transA.addLinguisticType(nextType);
			createdTypeMap.put(ltName, nextType);
			
			nextTier = new TierImpl(tier.getName(), tier.getParticipant(), transA, nextType);
			nextTier.setAnnotator(tier.getAnnotator());
			nextTier.setDefaultLocale(tier.getDefaultLocale());
			nextTier.setLangRef(tier.getLangRef());
			createdTierMap.put(nextTier.getName(), nextTier);
		}
		// add the tier if it has been created
		if (nextTier != null) {
			transA.addTier(nextTier);
		}
	}
	
	/**
	 * Creates a new depending tier and possibly sub-tiers. 
	 * Note: the private methods assume that both transcription objects are not null. 
	 * 
	 * @param tier the depending tier to add
	 * @param allTiersToAdd the list of all tiers to add // TODO currently not used
	 */
	private void addNewDependingTier(TierImpl tier, List<? extends Tier> allTiersToAdd) {
		if (tier == null) {
			return;
		}
		
		TierImpl nextTier = null;
		LinguisticType origType = tier.getLinguisticType();
//		String origCVName = origType.getControlledVocabularyName();
		
		// dependent tier: find target parent and add
		String parentName = tier.getParentTier().getName();
		TierImpl parentTier = (TierImpl) transA.getTierWithId(parentName);
		if (parentTier != null) {
			boolean compatible = mergeUtil.parentChildTypeCompatible(parentTier.getLinguisticType(), origType);
			if (compatible) { // add the tier and if necessary the type
				// check the type first
				LinguisticType curType = transA.getLinguisticTypeByName(origType.getLinguisticTypeName());
				if (curType != null) {// check if it is the same or compatible
					boolean typeComp = mergeUtil.lingTypeCompatible(origType, curType);
					if (typeComp) {// use it
						nextTier = new TierImpl(parentTier, tier.getName(), tier.getParticipant(), transA, curType);
						nextTier.setAnnotator(tier.getAnnotator());
						nextTier.setDefaultLocale(tier.getDefaultLocale());
						nextTier.setLangRef(tier.getLangRef());
						createdTierMap.put(nextTier.getName(), nextTier);
					} else {
						// add a new linguistic type, adjust the name
						String nextLTName = getUniqueLTName(transA, curType.getLinguisticTypeName());
						if (nextLTName != null) {
							// create and add the new linguistic type
							LinguisticType nextType = new LinguisticType(nextLTName);
							nextType.setTimeAlignable(origType.isTimeAlignable());
							ConstraintImpl nextConstraint = null;
							switch(origType.getConstraints().getStereoType()) {
							case Constraint.INCLUDED_IN:
								nextConstraint = new IncludedIn();
								break;
							case Constraint.TIME_SUBDIVISION:
								nextConstraint = new TimeSubdivision();
								break;
							case Constraint.SYMBOLIC_ASSOCIATION:
								nextConstraint = new SymbolicAssociation();
								break;
							case Constraint.SYMBOLIC_SUBDIVISION:
								nextConstraint = new SymbolicSubdivision();
								break;
							}
							if (nextConstraint != null) {
								nextType.addConstraint(nextConstraint);
								// TODO check CV and data category, external references etc.
								transA.addLinguisticType(nextType);
								createdTypeMap.put(nextLTName, nextType);
								
								nextTier = new TierImpl(parentTier, tier.getName(), tier.getParticipant(), 
										transA, nextType);
								nextTier.setAnnotator(tier.getAnnotator());
								nextTier.setDefaultLocale(tier.getDefaultLocale());
								nextTier.setLangRef(tier.getLangRef());
								createdTierMap.put(nextTier.getName(), nextTier);
							}// else log
						} // else don't create a type and don't create the tier
					}
				} else { // add the type with the original name
					// create and add the new linguistic type
					LinguisticType nextType = new LinguisticType(origType.getLinguisticTypeName());
					nextType.setTimeAlignable(origType.isTimeAlignable());
					ConstraintImpl nextConstraint = null;
					switch(origType.getConstraints().getStereoType()) {
					case Constraint.INCLUDED_IN:
						nextConstraint = new IncludedIn();
						break;
					case Constraint.TIME_SUBDIVISION:
						nextConstraint = new TimeSubdivision();
						break;
					case Constraint.SYMBOLIC_ASSOCIATION:
						nextConstraint = new SymbolicAssociation();
						break;
					case Constraint.SYMBOLIC_SUBDIVISION:
						nextConstraint = new SymbolicSubdivision();
						break;
					}
					if (nextConstraint != null) {
						nextType.addConstraint(nextConstraint);
						// TODO check CV and data category, external references etc.
						transA.addLinguisticType(nextType);
						createdTypeMap.put(nextType.getLinguisticTypeName(), nextType);
						
						nextTier = new TierImpl(parentTier, tier.getName(), tier.getParticipant(), 
								transA, nextType);
						nextTier.setAnnotator(tier.getAnnotator());
						nextTier.setDefaultLocale(tier.getDefaultLocale());
						nextTier.setLangRef(tier.getLangRef());
						createdTierMap.put(nextTier.getName(), nextTier);
					}// else log
				}
			}
		}// else no parent, don't add the tier, nowhere to put it	
		// add the tier if it is not null
		if (nextTier != null) {
			transA.addTier(nextTier);
		}
	}
	
	/**
	 * Adds the annotations of the source tier to the corresponding tier in destination
	 * transcription. This assumes that, if necessary, the destination tier(s) etc
	 * have been set up beforehand.
	 * 
	 * Note: the private methods assume that both transcription objects are not null. 
	 * HS April 2015 at the moment this method is only called for top level tiers, 
	 * but there are still some remaining checks on root/parent tier
	 * 
	 * @param sourceTier a tier from the second transcription
	 * @param tierToInclude a list of all tiers to include in the copy process
	 */
	private void addAnnotations(TierImpl sourceTier, List<String> tierToInclude) {
		if (sourceTier == null || tierToInclude == null) {
			return;
		}
		TierImpl destTier = (TierImpl) transA.getTierWithId(sourceTier.getName());
		if (destTier == null) {
			ClientLogger.LOG.warning("The tier to add annotations to is not found in the target transcription.");
			return; // log
		}
		TierImpl rootTier = destTier.getRootTier();
		
		List<AbstractAnnotation> annotations = sourceTier.getAnnotations();
		AbstractAnnotation srcAnn;
		AbstractAnnotation destAnn;
		AbstractAnnotation changeAnn;
		DefaultMutableTreeNode recordNode = null;
		long bt, et;
		
		for (int i = 0; i < annotations.size(); i++) {
			srcAnn = annotations.get(i);
			bt = srcAnn.getBeginTimeBoundary();
			et = srcAnn.getEndTimeBoundary();
			recordNode = AnnotationRecreator.createTreeForAnnotation(srcAnn);
			List<Annotation> existAnns; 
			if (rootTier != destTier) {// for depending annotations store the root annotation (should not happen now)
				existAnns =	rootTier.getOverlappingAnnotations(bt, et);
			} else {
				existAnns =	destTier.getOverlappingAnnotations(bt, et);
			}
		
			// store for undo
			if (destTier.hasParentTier()) {// depending tier (should not happen now)
				for (int j = 0; j < existAnns.size(); j++) {
					changeAnn = (AbstractAnnotation) existAnns.get(j);
					modifiedAnnotations.put(changeAnn, AnnotationRecreator.createTreeForAnnotation(changeAnn));
				} 
			} else { // root tier

	            for (int j = 0; j < existAnns.size(); j++) {
	            	changeAnn = (AbstractAnnotation) existAnns.get(j);

	                if (changeAnn.getBeginTimeBoundary() < bt) {
	                	modifiedAnnotations.put(changeAnn, AnnotationRecreator.createTreeForAnnotation(changeAnn));
	                } else if (changeAnn.getEndTimeBoundary() > et) {
	                	modifiedAnnotations.put(changeAnn, AnnotationRecreator.createTreeForAnnotation(changeAnn));
	                } else {
	                	deletedAnnotations.put(changeAnn, AnnotationRecreator.createTreeForAnnotation(changeAnn));
	                }
	            }
			}

	        Enumeration<?> en = recordNode.breadthFirstEnumeration();
	        List<DefaultMutableTreeNode> processedNodes = new ArrayList<DefaultMutableTreeNode>();
	        DefaultMutableTreeNode node;
	        AnnotationDataRecord annData = null;
	        //TierImpl tier = null;


	        while (en.hasMoreElements()) {
	            node = (DefaultMutableTreeNode) en.nextElement();
	            if (processedNodes.contains(node)) {
	            	continue;
	            }
	            annData = (AnnotationDataRecord) node.getUserObject();
	            
	            if (tierToInclude.contains(annData.getTierName())) {
	            	destAnn = AnnotationRecreator.createAnnotationFromTree(transA, node);
	            	
	            	if (destAnn != null) {
	            		createdAnnotations.put(destAnn, node);
	            	}
	            	Enumeration<?> nextEn = node.breadthFirstEnumeration();

	            	while (nextEn.hasMoreElements()) {
	            		processedNodes.add((DefaultMutableTreeNode)nextEn.nextElement());
	            	}
	            }
	        }

		}
	}
	
	/**
	 * Adds the annotations of the source dependent tier to the corresponding dependent tier
	 * in the destination transcription. This assumes that, if necessary, the destination 
	 * tier(s) etc have been set up beforehand.
	 * 
	 * Note: the private methods assume that both transcription objects are not null. 
	 * 
	 * @param sourceTier a tier from the second transcription
	 * @param tierToInclude a list of all tiers to include in the copy process
	 */
	private void addAnnotationsToDependentTier(TierImpl sourceTier, List<String> tierToInclude) {
		if (sourceTier == null || tierToInclude == null) {
			return;
		}
		TierImpl destTier = (TierImpl) transA.getTierWithId(sourceTier.getName());
		if (destTier == null) {
			ClientLogger.LOG.warning("The tier to add annotations to is not found in the target transcription.");
			return; // log
		}
		//TierImpl rootTier = destTier.getRootTier();
		TierImpl sourceParentTier = sourceTier.getParentTier();
		
		if (destTier.getLinguisticType().getConstraints().getStereoType() != Constraint.SYMBOLIC_ASSOCIATION) {
			List<AbstractAnnotation> parAnnotations = sourceParentTier.getAnnotations();
			AbstractAnnotation rootAnnotation;
			for (AbstractAnnotation parAnn : parAnnotations) {
				List <Annotation> curChildren = parAnn.getChildrenOnTier(sourceTier);
				
				if (curChildren.isEmpty()) {
					continue;
				}
				// store root for undo
	//			rootAnnotation = (AbstractAnnotation) getRootAnnotation(parAnn);
	//			if (!modifiedAnnotations.containsKey(rootAnnotation)) {
	//				modifiedAnnotations.put(rootAnnotation, AnnotationRecreator.createTreeForAnnotation( rootAnnotation));
	//			}
				
				ArrayList<DefaultMutableTreeNode> curNodeGroup = new ArrayList<DefaultMutableTreeNode>();
				for (Annotation aa : curChildren) {
					curNodeGroup.add(AnnotationRecreator.createTreeForAnnotation((AbstractAnnotation) aa));
				}
		
				createdAnnotationSequences.add(curNodeGroup);
				AnnotationRecreator.createAnnotationsSequentially(transA, curNodeGroup, false);
			}
		} else { //create annotations on symbolic annotation tiers
//			TierImpl destParentTier = destTier.getParentTier();
			List<AbstractAnnotation> sourceAnnotations = sourceTier.getAnnotations();
			
			for (AbstractAnnotation srcAnn : sourceAnnotations) {
				long refTime = (long) (srcAnn.getBeginTimeBoundary() + srcAnn.getEndTimeBoundary()) /2;
				// store current annotations on this tier for undo
				AbstractAnnotation curDestAnn = (AbstractAnnotation) destTier.getAnnotationAtTime(refTime);
				if (curDestAnn != null) {
					deletedAnnotations.put(curDestAnn, new DefaultMutableTreeNode(AnnotationRecreator.createTreeForAnnotation(curDestAnn)));
					destTier.removeAnnotation(curDestAnn);
				}
				DefaultMutableTreeNode recNode = AnnotationRecreator.createTreeForAnnotation(srcAnn);
				
				AbstractAnnotation nextAnn = AnnotationRecreator.createAnnotationFromTree(transA, recNode);
				if (nextAnn != null) {
					createdAnnotations.put(nextAnn, recNode);
				} //else {//logging has probably been taken care of in the AnnotationRecreator
					//ClientLogger.LOG.warning("Could not create annotation");
				//}
			}
			
		}
	}
	
	/**
	 * Produces a new, unique name for a linguistic type based on the name of an existing type.
	 * 
	 * @param trans the transcription
	 * @param ltName the existing type's name
	 * 
	 * @return a new name based on the existing one or null
	 */
	private String getUniqueLTName(TranscriptionImpl trans, String ltName) {
		if (trans == null || ltName == null) {
			return null;
		}
		
		String newName;
		for (int i = 1; i < 20; i++) {//  arbitrary number of retries.
			newName = ltName + "-" + i;
			if (trans.getLinguisticTypeByName(newName) == null) {
				return newName;
			}
		}
		
		return null;
	}
	
	/**
	 * Iterates up to the root level annotation from the specified annotation.
	 * 
	 * @param annotation the annotation to find the root annotation for
	 * @return the root annotation or the input annotation in case that is a root annotation
	 */
	private Annotation getRootAnnotation(Annotation annotation) {
		if (annotation != null && annotation.hasParentAnnotation()) {
			Annotation iterAnn = annotation.getParentAnnotation();
			while (iterAnn.hasParentAnnotation()) {
				iterAnn = iterAnn.getParentAnnotation();
			}
			
			return iterAnn;
		}
		
		return annotation;
	}
}

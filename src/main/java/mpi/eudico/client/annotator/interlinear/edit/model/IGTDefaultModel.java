package mpi.eudico.client.annotator.interlinear.edit.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.interlinear.IGTTierType;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTDataModelEvent;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTDataModelListener;
import mpi.eudico.client.annotator.interlinear.edit.event.ModelEventType;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTBlockRenderInfo;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.util.TimeFormatter;

/**
 * Implementation of an IGT data model based on a group of annotations,
 * with a single top level annotation as the root.
 * 
 * @author Han Sloetjes
 *
 */
public class IGTDefaultModel extends IGTAbstractDataModel {
	private int numInitializedSpecialTiers = 0;
	
	/**
	 * The original beginTimeBoundary of the rootAnnotation.
	 * If the annotation's time gets changed, this value represents the
	 * original time. This helps finding modified annotations.
	 */
	private long beginTime;
	/**
	 * The original endTimeBoundary of the rootAnnotation.
	 * If the annotation's time gets changed, this value represents the
	 * original time. This helps finding modified annotations.
	 */
	private long endTime;
	
	/**
	 * Constructor.
	 * 
	 * @param rootAnnotation the top level alignable annotation
	 * @param hiddenTiers keep these tiers out of the model
	 */
	public IGTDefaultModel(AlignableAnnotation rootAnnotation, Collection<String> hiddenTiers) {
		this(rootAnnotation, hiddenTiers, null);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param rootAnnotation the top level alignable annotation
	 * @param hiddenTiers keep these tiers out of the model
	 * @param hiddenSpecialTiers the special tier types not to show e.g. time code, speaker, silence duration(?)
	 */
	public IGTDefaultModel(AlignableAnnotation rootAnnotation, Collection<String> hiddenTiers, 
			Collection<IGTTierType> hiddenSpecialTiers) {
		rowData = new ArrayList<IGTTier>();
		rowHeader = new IGTRowHeader();
		renderInfo = new IGTBlockRenderInfo();
		listeners = new ArrayList<IGTDataModelListener>(4);
		fillTable(rootAnnotation, hiddenTiers, hiddenSpecialTiers);
	}

	private void fillTable(AlignableAnnotation rootAnnotation, Collection<String> hiddenTiers, 
			Collection<IGTTierType> hiddenSpecialTiers) {
		beginTime = rootAnnotation.getBeginTimeBoundary();
		endTime = rootAnnotation.getEndTimeBoundary();
		
		IGTAnnotation rootIA = new IGTAnnotation(rootAnnotation);
		IGTTier rootTier = new IGTTier(((TierImpl) rootAnnotation.getTier()).getName(), IGTTierType.ROOT);
		rowData.add(rootTier);
		rowHeader.addHeader(rootTier.getTierName());
		rootTier.addAnnotation(rootIA);
		// add place holder tiers and/or fill "empty" positions with empty place holder nodes
		addDependingTiersNodes(rootTier, (TierImpl) rootAnnotation.getTier(), hiddenTiers);
		
		// add children
		addChildrenToNode(rootAnnotation.getParentListeners(), rootIA, false);
		
		// add nodes for speaker, time info, silence duration etc?
		addSpecialTiers(rootIA, hiddenSpecialTiers);
	}
	
	/**
	 * Recursively creates and adds tier nodes for child tiers and adds them to the model. 
	 * Currently all tiers are just added, no support yet for a selection of tiers.
	 * 
	 * @param parentTierNode the parent node
	 * @param parentTier the parent tier
	 */
	private void addDependingTiersNodes(IGTTier parentTierNode, TierImpl parentTier, Collection<String> hiddenTiers) {
		List<TierImpl> depTiers = parentTier.getChildTiers();
		
		if (depTiers != null) {
			
			for (TierImpl dt : depTiers) {
				//System.out.println("Parent: " + parentTier.getName() + " - Child " + i + ": "+ dt.getName());
				if (hiddenTiers != null && hiddenTiers.contains(dt.getName())) {
					continue;
				}
				
				IGTTier igtTier = createIGTTier(dt.getName(), parentTierNode, dt);

				if (numInitializedSpecialTiers > 0) {//add before the special tiers
					int insertIndex = rowData.size() - numInitializedSpecialTiers;
					rowData.add(insertIndex, igtTier);
					rowHeader.addHeader(insertIndex, igtTier.getTierName());
				} else {
					rowData.add(igtTier);
					rowHeader.addHeader(igtTier.getTierName());
				}
				// recursive
				addDependingTiersNodes(igtTier, dt, hiddenTiers);
			}
		}
	}	
	
	/**
	 * Given a parent IGT tier and a TierImpl to be transformed into its child tier,
	 * create a new IGTTier of the right type.
	 */
	private IGTTier createIGTTier(String tierName, IGTTier parentTier, TierImpl tier) {
		IGTTierType type; // = IGTTierType.ASSOCIATION;
		boolean inWLBlock; // = false;
		Constraint c = tier.getLinguisticType().getConstraints();
		
		switch (c.getStereoType()) {
		case Constraint.SYMBOLIC_ASSOCIATION:
			if (parentTier.getType() == IGTTierType.ROOT) {
				type = IGTTierType.FIRST_LEVEL_ASSOCIATION;
				inWLBlock = false;
			} else {
				type = IGTTierType.DEEPER_LEVEL_ASSOCIATION;
				inWLBlock = parentTier.isInWordLevelBlock();
			}
			break;
		case Constraint.SYMBOLIC_SUBDIVISION:
		case Constraint.TIME_SUBDIVISION:
		case Constraint.INCLUDED_IN:
			if (!parentTier.isInWordLevelBlock()
				/*was parentTier.getType() == IGTTierType.ROOT
					|| parentTier.getType() == IGTTierType.FIRST_LEVEL_ASSOCIATION*/) {
				// also when parent is IGTTierType.DEEPER_LEVEL_ASSOCIATION ?
				// Or simply when it is not inWordLevelBlock() ?
				type = IGTTierType.WORD_LEVEL_ROOT;// ?? can't assume this,
				// could be phrase level as well.
				// On the other hand, no code has been written to handle PHRASE_LEVEL_ROOTs.
				inWLBlock = true;
			} else {
				type = IGTTierType.SUBDIVISION;
				inWLBlock = true;
			}
			break;
		default:	// cannot happen!?
			type = IGTTierType.ASSOCIATION;
			inWLBlock = parentTier.isInWordLevelBlock(); //was false;
		}
		
//old version:
//		if (c.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
//			if (parentTier.getType() == IGTTierType.ROOT) {
//				type = IGTTierType.FIRST_LEVEL_ASSOCIATION;
//				inWLBlock = false;
//			} else {
//				type = IGTTierType.DEEPER_LEVEL_ASSOCIATION;
//				inWLBlock = parentTier.isInWordLevelBlock();
//			}
//		} else if (c.getStereoType() == Constraint.SYMBOLIC_SUBDIVISION || 
//				c.getStereoType() == Constraint.TIME_SUBDIVISION ||
//				c.getStereoType() == Constraint.INCLUDED_IN) {
//			if (parentTier.getType() == IGTTierType.ROOT || 
//					parentTier.getType() == IGTTierType.FIRST_LEVEL_ASSOCIATION) {
//				type = IGTTierType.WORD_LEVEL_ROOT;//?? can't assume this, could be phrase level as well
//				inWLBlock = true;
//			} else {
//				type = IGTTierType.SUBDIVISION;	
//				inWLBlock = true;
//			}
//		}

		IGTTier igtTier = new IGTTier(tierName, type, inWLBlock);

		parentTier.addChildTier(igtTier);
		igtTier.setParentTier(parentTier);
		
		return igtTier;
	}
	
	/**
	 * Adds nodes for child annotations of a parent annotation to the appropriate tier node while constructing
	 * tree relations between the annotations themselves.
	 * 
	 * @param childAnnos the child annotations
	 * @param parentNode the node of the parent annotation
	 */
	private void addChildrenToNode(List<Annotation> childAnnos, IGTAnnotation parentNode,
			boolean createTiersWhenMissing) {
		if (childAnnos == null || parentNode == null) {
			return;			
		}
		IGTTier parentTier = parentNode.getIGTTier();
		Map<String, List<AbstractAnnotation>> amap = new HashMap<String, List<AbstractAnnotation>>();
		
		// Sort children into buckets by tier
		for (int i = 0; i < childAnnos.size(); i++) {
			AbstractAnnotation aa = (AbstractAnnotation) childAnnos.get(i);
			String tierName = aa.getTier().getName();
			List<AbstractAnnotation> group = amap.get(tierName);
			if (group == null) {
				group = new ArrayList<AbstractAnnotation>();
				amap.put(tierName, group);
			}
			group.add(aa);
		}
		
		for (Map.Entry<String, List<AbstractAnnotation>> e : amap.entrySet()) {
			String tierName = e.getKey();
			List<AbstractAnnotation> group = e.getValue();
			
			IGTTier igtTier;
			int tierIndex = getRowIndexForTier(tierName);
			
			if (tierIndex == -1) {
				if (!createTiersWhenMissing) {
					continue;
				}
				igtTier = null; // delay creation until finding an annotation on the tier
			} else {
				igtTier = rowData.get(tierIndex);
			}
			
			if (group.size() > 1) {
				Collections.sort(group, new AAComparator());
			}
			
			for (AbstractAnnotation aa : group) {
				
				if (igtTier == null) { // this shouldn't occur if the tiers have already been added
					igtTier = createIGTTier(tierName, parentTier, (TierImpl) aa.getTier());
					rowData.add(igtTier);
					rowHeader.addHeader(tierName);
				}
				IGTAnnotation ia = new IGTAnnotation(aa);
				igtTier.addAnnotation(ia);
				parentNode.addChild(ia);
				
				List<Annotation> nextChildren = aa.getParentListeners();
				if (nextChildren.size() > 0) {
					addChildrenToNode(nextChildren, ia, createTiersWhenMissing);
				}
			}
		}
	}
	
	/**
	 * Adds "placeholder tiers" for time and speaker information. 
	 * 
	 * @param root the root annotation
	 */
	private void addSpecialTiers(IGTAnnotation root, Collection<IGTTierType> hiddenSpecialTiers) {
		long bt = getBeginTime();
		long et = getEndTime();
		// speaker label
		if (hiddenSpecialTiers == null  || !hiddenSpecialTiers.contains(IGTTierType.SPEAKER_LABEL)) {
			String speaker = ((TierImpl) root.getAnnotation().getTier()).getParticipant();
			if (speaker == null) {
				speaker = "   ";// or Undefined? Unknown
			}
			
			addSpecialTier(root, IGTTierType.SPEAKER_LABEL, speaker);
		}
		// time code tier
		if (hiddenSpecialTiers == null  || !hiddenSpecialTiers.contains(IGTTierType.TIME_CODE)) {
			addSpecialTier(root, IGTTierType.TIME_CODE, TimeFormatter.toString(bt) + " - " + TimeFormatter.toString(et));
		}
		// add silence duration tier?
	}

	/**
	 * @param root
	 * @param annotationText
	 */
	private void addSpecialTier(IGTAnnotation root, IGTTierType tierType, String annotationText) {
		IGTAnnotation annNode = new IGTAnnotation(annotationText);
		root.addChild(annNode);
		IGTTier tierNode = new IGTTier(tierType.label(), tierType);
		tierNode.addAnnotation(annNode);
		
		if (tierType == IGTTierType.SPEAKER_LABEL && numInitializedSpecialTiers > 0) {
			int insertIndex = rowData.size() - numInitializedSpecialTiers;
			rowData.add(insertIndex, tierNode);
			rowHeader.addHeader(insertIndex, tierType.label());
		} else {
			rowData.add(tierNode);
			rowHeader.addHeader(tierType.label());
		}
		
		root.getIGTTier().addChildTier(tierNode);
		tierNode.setParentTier(root.getIGTTier());
		numInitializedSpecialTiers++;
	}
	
	/**
	 * Removes a special tier (time code, speaker etc.) from the model.
	 * 
	 * @param rootIA the root annotation
	 * @param tierType the special type to remove
	 */
	private void removeSpecialTier(IGTAnnotation rootIA, IGTTierType tierType) {
		IGTTier specTier = getRowDataForTier(tierType.label());
		if (specTier != null && specTier.isSpecial()) {
			rootIA.removeChild(specTier.getAnnotations().get(0));
			if (specTier.getParentTier() != null) {// shouldn't be
				specTier.getParentTier().removeChildTier(specTier);
				specTier.setParentTier(null);
			}
			rowData.remove(specTier);
			rowHeader.removeHeader(specTier.getTierName());
			numInitializedSpecialTiers--;
		}
	}
	
	/**
	 * Update the annotation in the "TC" tier with the current time interval.
	 */
	private void updateTimeCodeTier() {
		IGTTier tcTier = getRowDataForTier(IGTTierType.TIME_CODE.label());
		if (tcTier != null) {
			IGTAnnotation tcAnn = tcTier.getAnnotations().get(0);
			String text = TimeFormatter.toString(getBeginTime()) + " - " +
		              TimeFormatter.toString(getEndTime());
			tcAnn.setTextForNode(text);
		}
	}

	/**
	 * Returns the begin time of the root annotation when this block was constructed.
	 * If the Annotation gets changed later, this stays at the original time.
	 * 
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#getBeginTime()
	 */
	@Override
	public long getBeginTime() {
		return beginTime;
		
//		IGTTier root = getRootRow();
//		if (root != null) {
//			if (root.getAnnotations().size() > 0) {// should be 1
//				IGTAnnotation rootAnn = root.getAnnotations().get(0);
//				if (rootAnn.getAnnotation() != null) {
//					return rootAnn.getAnnotation().getBeginTimeBoundary();
//				}
//			}
//		}
//		
//		return -1;
	}

	/**
	 * Returns the end time of the root annotation when this block was constructed.
	 * If the Annotation gets changed later, this stays at the original time.
	 * 
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#getEndTime()
	 */
	@Override
	public long getEndTime() {
		return endTime;
		
//		IGTTier root = getRootRow();
//		if (root != null) {
//			if (root.getAnnotations().size() > 0) {// should be 1
//				IGTAnnotation rootAnn = root.getAnnotations().get(0);
//				if (rootAnn.getAnnotation() != null) {
//					return rootAnn.getAnnotation().getEndTimeBoundary();
//				}
//			}
//		}
//		
//		return -1;
	}
		
	/**
	 * Sets the visibility of the time code or the speaker tier.
	 * The specified tier will be added or removed.
	 * 
	 * @param specialTier the special tier type
	 * @param visible the visibility flag 
	 * 
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#setSpecialTierVisibility(mpi.eudico.client.annotator.interlinear.IGTTierType, boolean)
	 */
	@Override
	public void setSpecialTierVisibility(IGTTierType specialTier,
			boolean visible) {
		String annotationText = null;
		IGTAnnotation rootIA = getRootRow().getAnnotations().get(0);
		// check if it is indeed a special tier and creates the annotation text
		switch (specialTier) {
		case TIME_CODE:
			if (visible) {
				annotationText = String.format("%s - %s", 
					TimeFormatter.toString(getBeginTime()), TimeFormatter.toString(getEndTime()));
			}
			break;
		case SPEAKER_LABEL:
			if (visible) {
				annotationText = rootIA.getAnnotation().getTier().getParticipant();
				if (annotationText == null) {
					annotationText = "   ";
				}
			}
			break;
			default:
				return;// error, return
		}
		
		if (!visible) {
			removeSpecialTier(rootIA, specialTier);
		} else {
			// checks if it isn't already there
			IGTTier specTier = getRowDataForTier(specialTier.label());
			if (specTier == null) {
				addSpecialTier(rootIA, specialTier, annotationText);
			}
		}
	}

	/**
	 * Returns the visibility of the specified special tier.
	 * 
	 * @param specialTier the tier to check
	 * @return the visibility of the specified tier
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#getSpecialTierVisibility(mpi.eudico.client.annotator.interlinear.IGTTierType)
	 */
	@Override
	public boolean getSpecialTierVisibility(IGTTierType specialTier) {
		return getRowDataForTier(specialTier.label()) != null;
	}
	
	// methods for updating the model as far as annotations are concerned, reflect the type of events in ACMEditEvent

	/**
	 * Adds an IGT annotation for the specified Annotation to the model, if it can find the right position, the right
	 * annotation group to add it to.
	 * 
	 * @param annotation the annotation to add to the model
	 */
	public void annotationAdded(AbstractAnnotation annotation) {
		if (annotation != null) {
			IGTTier chTier = getRowDataForTier(annotation.getTier().getName());
			if (chTier == null) {
				return;
			}
			IGTTier parTier = chTier.getParentTier();// check null??
			if (parTier == null) {
				return;
			}
			List<IGTAnnotation> parAnns = parTier.getAnnotations();
			
			for (IGTAnnotation parAnn : parAnns) {
				// there are several ways to check which annotation is the parent annotation 
				// one is via times. Another one is via ParentListeners
				if (parAnn.getAnnotation().getBeginTimeBoundary() <= annotation.getBeginTimeBoundary() && 
						parAnn.getAnnotation().getEndTimeBoundary() >= annotation.getEndTimeBoundary()) {
					annotationAdded(chTier, annotation, parAnn);
					break;
				}
			}
		}
	}

	/**
	 * @param annotationIGTTier the IGT tier where addedAnnotation is to be added
	 * @param addedAnnotation
	 * @param parAnn the IGT parent of the addedAnnotation
	 * @throws ArrayIndexOutOfBoundsException
	 */
	private void annotationAdded(IGTTier annotationIGTTier, AbstractAnnotation addedAnnotation,
			IGTAnnotation parAnn) {
		IGTAnnotation addedIgtAnn = new IGTAnnotation(addedAnnotation);
		boolean inserted = false;
		
		// check if there are already child annotations 
		Map<IGTTier, List<IGTNode>> childMap = parAnn.getChildrenPerTier();
		
		if (childMap != null) {
			List<IGTNode> childList = childMap.get(annotationIGTTier);
			
			if (childList != null) {
				//find position
				
				final int childListSize = childList.size();
				for (int k = 0; k < childListSize; k++) {
					IGTAnnotation child = (IGTAnnotation) childList.get(k);
					
					if (child.getAnnotation().getBeginTimeBoundary() >= addedAnnotation.getEndTimeBoundary()) {
						//  insert before this child, assume that existing annotations times have been updated
						inserted = parAnn.insertChild(addedIgtAnn, k);
					} else if (k == childListSize - 1) {
						// after the last existing annotation
						inserted = parAnn.addChild(addedIgtAnn);
					}
				}
			} else {// no children for this parent annotation on this tier yet
				inserted = parAnn.addChild(addedIgtAnn);
			}
		} else {// no children on any tier yet, add
			inserted = parAnn.addChild(addedIgtAnn);
		}
		
		if (inserted) {
			//nextIgtAnn.setIGTTier(chTier); // {add,insert}Annotation(nextIgtAnn) would do this already
			
			if (annotationIGTTier.getAnnotations().isEmpty()) {
				annotationIGTTier.addAnnotation(addedIgtAnn);
			} else {
				for (int n = 0; n < annotationIGTTier.getAnnotations().size(); n++) {
					IGTAnnotation child = annotationIGTTier.getAnnotations().get(n);
					
					if (child.getAnnotation().getBeginTimeBoundary() >= addedAnnotation.getEndTimeBoundary()) {
						annotationIGTTier.insertAnnotation(addedIgtAnn, n);
						break;
					} else if (n == annotationIGTTier.getAnnotations().size() - 1) {
						annotationIGTTier.addAnnotation(addedIgtAnn);// situation of annotation after?
						break;
					}
				}
			}
			// generate and post event
			IGTDataModelEvent event = new IGTDataModelEvent(this, annotationIGTTier, addedIgtAnn, ModelEventType.ADD);
			postEvent(event);
			
			addChildrenOfNewAnnotation(addedAnnotation, addedIgtAnn);
		}
	}

	/**
	 * Add the children too. Since this annotation was new,
	 * its children are new too.
	 * 
	 * @param addedAnnotation
	 * @param addedIgtAnn
	 */
	private void addChildrenOfNewAnnotation(AbstractAnnotation addedAnnotation,
			IGTAnnotation addedIgtAnn) {
		List<Annotation> newChildren = addedAnnotation.getParentListeners();
		
		for (Annotation child : newChildren) {
			IGTTier igtTier = getRowDataForTier(child.getTier().getName());
			
			if (igtTier != null) {
				annotationAdded(igtTier, (AbstractAnnotation)child, addedIgtAnn);
			}
		}
	}

	/**
	 * Adds an annotation that has been inserted after an existing annotation.
	 * 
	 * @param addedAnnotation the annotation to add to the model
	 * @param afterThis the annotation the new annotation has been inserted after
	 */
	public void annotationAddedAfter(AbstractAnnotation addedAnnotation,
			AbstractAnnotation afterThis) {
		if (addedAnnotation != null && afterThis != null) {
			IGTTier chTier = getRowDataForTier(addedAnnotation.getTier().getName());
			if (chTier != null) {
				int numAnns = chTier.getAnnotations().size();
				
				for (int i = 0; i < numAnns; i++) {
					IGTAnnotation afterAnn = chTier.getAnnotations().get(i);
					if (afterAnn.getAnnotation() == afterThis) {
						
						IGTAnnotation addedIgtAnn = new IGTAnnotation(addedAnnotation);
						boolean inserted = false;
						
						IGTAnnotation parAnn = (IGTAnnotation) afterAnn.getParent();
						int afterIndex = parAnn.getIndex(afterAnn);
						//assert(afterIndex != -1);
						if (afterIndex > -1) {
							if (afterIndex < parAnn.getChildCount()) {
								inserted = parAnn.insertChild(addedIgtAnn, afterIndex + 1);
							} else {
								inserted = parAnn.addChild(addedIgtAnn);
							}
						}
						
						if (inserted) {
							if (i < numAnns - 2) {
								chTier.insertAnnotation(addedIgtAnn, i + 1);
							} else {
								chTier.addAnnotation(addedIgtAnn);
							}
							
							// generate and post event
							IGTDataModelEvent event = new IGTDataModelEvent(this, chTier, addedIgtAnn, ModelEventType.ADD);
							postEvent(event);
							
							addChildrenOfNewAnnotation(addedAnnotation,	addedIgtAnn);

							break;
						}
					}
				}
			}
		}		
	}

	/**
	 * Adds an annotation that has been inserted before an existing annotation
	 * 
	 * @param addedAnnotation the annotation to add to the model
	 * @param beforeThis the annotation the new annotation has been inserted before
	 */
	public void annotationAddedBefore(AbstractAnnotation addedAnnotation,
			AbstractAnnotation beforeThis) {
		if (addedAnnotation != null && beforeThis != null) {
			IGTTier chTier = getRowDataForTier(addedAnnotation.getTier().getName());
			if (chTier != null) {
				int numAnns = chTier.getAnnotations().size();
				
				for (int i = 0; i < numAnns; i++) {
					IGTAnnotation beforeAnn = chTier.getAnnotations().get(i);
					if (beforeAnn.getAnnotation() == beforeThis) {
						
						IGTAnnotation addedIgtAnn = new IGTAnnotation(addedAnnotation);
						boolean inserted = false;
						
						IGTAnnotation parAnn = (IGTAnnotation) beforeAnn.getParent();
						int beforeIndex = parAnn.getIndex(beforeAnn);
						//assert(beforeIndex != -1);
						if (beforeIndex > -1) {
							inserted = parAnn.insertChild(addedIgtAnn, beforeIndex);
						}
						
						if (inserted) {
							chTier.insertAnnotation(addedIgtAnn, i);

							// generate and post event
							IGTDataModelEvent event = new IGTDataModelEvent(this, chTier, addedIgtAnn, ModelEventType.ADD);
							postEvent(event);
							
							addChildrenOfNewAnnotation(addedAnnotation,	addedIgtAnn);

							break;
						}
					}
				}
			}
		}		
	}

	/**
	 * Changes the text value of an existing annotation.
	 * Should also be called if the time interval of an annotation changes. 
	 * 
	 * @param annotation the changed annotation
	 */
	public void annotationValueChanged(AbstractAnnotation annotation) {
		if (annotation != null) {
			IGTTier chTier = getRowDataForTier(annotation.getTier().getName());
			if (chTier != null) {
				int numAnns = chTier.getAnnotations().size();
				
				for (int i = 0; i < numAnns; i++) {
					IGTAnnotation igtAnn = chTier.getAnnotations().get(i);
					if (igtAnn.getAnnotation() == annotation) {
						// The annotation is indeed in this model, recalculate the rendering,
						// generate and post event.
						// Since the AbstractAnnotation is linked to the IGTAnnotation,
						// the updated text value is used automatically.
						
						if (numAnns == 1 && chTier == getRootRow()) {
							long newBeginTime = annotation.getBeginTimeBoundary();
							long newEndTime = annotation.getEndTimeBoundary();
							
							if (newBeginTime != beginTime || newEndTime != endTime) {
								beginTime = newBeginTime;
								endTime = newEndTime;
								// Update TIME_CODE tier
								updateTimeCodeTier();
							}
						}
						
						IGTDataModelEvent event = new IGTDataModelEvent(this, chTier, igtAnn, ModelEventType.CHANGE);
						postEvent(event);
						
						break;
					}
				}
			}
		}		
	}

	/**
	 * Removes the IGT annotation for the specified annotation.
	 * 
	 * @param annotation the annotation to remove
	 */
	public void annotationRemoved(AbstractAnnotation annotation) {
		if (annotation != null) {
			IGTTier chTier = getRowDataForTier(annotation.getTier().getName());
			if (chTier != null) {
				int numAnns = chTier.getAnnotations().size();
				
				for (int i = 0; i < numAnns; i++) {
					IGTAnnotation igtAnn = chTier.getAnnotations().get(i);
					if (igtAnn.getAnnotation() == annotation) {
						// the annotation is indeed in this model, disconnect and recalculate the rendering
						
						removeChildren(igtAnn);
						if (igtAnn.getParent() != null) {
							igtAnn.getParent().removeChild(igtAnn);
						}
						chTier.removeAnnotation(igtAnn);
						
						// generate and post event
						IGTDataModelEvent event = new IGTDataModelEvent(this, chTier, igtAnn, ModelEventType.REMOVE);
						postEvent(event);
						
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Adds an IGT tier to the model.
	 * This is basically done by removing all tiers and re-adding them.
	 * 
	 * @param tier, the tier to be added to the model
	 */
	public void tierAdded(TierImpl tier, Collection<String> hiddenTiers) {
		if (tier != null) {
			// Remove all tiers except the root tier and the special tiers
			for (int i = rowData.size()-3; i > 0; i--) {
				rowData.remove(i);
			}
			
			// Re-add all tiers, including the one that has been added
			IGTTier igtTier = getRowDataForTier(tier.getRootTier().getName());
			addDependingTiersNodes(igtTier, tier.getRootTier(), hiddenTiers);
			
			for (IGTAnnotation ann : igtTier.getAnnotations()) {
				addChildrenToNode(ann.getAnnotation().getParentListeners(), ann, false);
			}
		}
	}
	
	/**
	 * Removes the IGT Tier from the model.
	 * 
	 * @param tierName, the name of the tier to be removed from the model
	 */
	public void tierRemoved(String tierName) {
		if (tierName != null) {
			IGTTier igtTier = getRowDataForTier(tierName);
			if (igtTier != null) {
				if (igtTier.getParentTier() != null) {
					igtTier.getParentTier().removeChildTier(igtTier);
				}
				rowData.remove(igtTier);
				rowHeader.removeHeader(tierName);
				removeChildTiers(igtTier);
			}
		}
	}
	
	/**
	 * Removes all child tiers from the model. Recursively.
	 * 
	 * @param igtTier, the IGT tier of which all child tiers are to be removed from the model
	 */
	private void removeChildTiers(IGTTier igtTier) {
		if (igtTier != null) {
			int numChildren = igtTier.getChildTiers().size();
			IGTTier child;
			for (int i = numChildren - 1; i >= 0; i--) {
				child =  igtTier.getChildTiers().get(i);
				rowData.remove(child);
				rowHeader.removeHeader(child.getTierName());
				removeChildTiers(child);
			}
		}
	}
	
	/**
	 * Removes all children of the specified annotation/node. Recursively.
	 * 
	 * @param igtAnnotation the IGT annotation of which to delete all children
	 */
	private void removeChildren(IGTAnnotation igtAnnotation) {
		if (igtAnnotation != null) {

			int numChildren = igtAnnotation.getChildCount();
			IGTAnnotation child;
			for (int i = numChildren - 1; i >= 0; i--) {
				child = (IGTAnnotation) igtAnnotation.getChildren().get(i);
				igtAnnotation.removeChild(child);
				child.getIGTTier().removeAnnotation(child);
				removeChildren(child);
			}
		}
	}

	// methods to notify listeners
	@Override
	protected void notifyRowAdded() {
		IGTDataModelEvent event = new IGTDataModelEvent(this);
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).dataModelChanged(event);
		}
	}
	
	/**
	 * Mini implementation of a comparator. Compares annotations on the same tier, 
	 * assumes that they different begin times. 
	 * 
	 * @author Han Sloetjes
	 */
	private class AAComparator implements Comparator<AbstractAnnotation> {

		/**
		 * Compares only time info.
		 */
		@Override
		public int compare(AbstractAnnotation arg0, AbstractAnnotation arg1) {
			final long beginTimeBoundary0 = arg0.getBeginTimeBoundary();
			final long beginTimeBoundary1 = arg1.getBeginTimeBoundary();
			
			if (beginTimeBoundary0 < beginTimeBoundary1) {
				return -1;
			} else if (beginTimeBoundary0 > beginTimeBoundary1) {
				return 1;
			}
			// here could compare end times
			return 0;
		}
		
	}

	
}

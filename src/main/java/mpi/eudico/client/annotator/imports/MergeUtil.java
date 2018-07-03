package mpi.eudico.client.annotator.imports;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.ConstraintImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.ControlledVocabulary;


public class MergeUtil {

    /**
     * Checks whether the tiers can be added: this depends on tier dependencies and
     * compatibility of linguistic types.
     * 
     * @return a list of tiers that can be added
     */
    public List<TierImpl> getAddableTiers(TranscriptionImpl srcTrans, TranscriptionImpl destTrans,
            List<String> selTiers) {
        if (srcTrans == null || destTrans == null) {
        	ClientLogger.LOG.warning("A Transcription is null");
            return new ArrayList<TierImpl>(0); 
        }
        if (selTiers == null) {
            int size = srcTrans.getTiers().size();
            selTiers = new ArrayList<String>(size);
            Tier ti;
            for (int i = 0; i < size; i++) {
                ti = srcTrans.getTiers().get(i);
                selTiers.add(ti.getName());
            }
        }
        List<TierImpl> validTiers = new ArrayList<TierImpl>(selTiers.size());

        String name;
        TierImpl t, t2;
        for (int i = 0; i < selTiers.size(); i++) {
            name = selTiers.get(i);
            t = srcTrans.getTierWithId(name);
            if (t != null) {
                t2 = destTrans.getTierWithId(name);
                if (t2 == null) { // not yet in destination
                    if (t.getParentTier() == null) {
                        // a toplevel tier can always be added
                        validTiers.add(t);    
                    } else {
                        // check whether:
                        // 1 - the parent/ancestors are also in the list to be added
                        // 2 - the parent/ancestors are already in the destination
                        TierImpl parent = null;
                        String parentName = null;
                        TierImpl loopTier = t;
                        while (loopTier.getParentTier() != null) {
                            parent = loopTier.getParentTier();
                            parentName = parent.getName();
                            if (selTiers.contains(parentName)) {
                                if (parent.getParentTier() == null) {
                                    validTiers.add(t);
                                    break;
                                } else if (destTrans.getTierWithId(parentName) != null) {
                                    if (lingTypeCompatible(parent, destTrans.getTierWithId(parentName))) {
                                        validTiers.add(t); 
                                    }
                                    
                                    break;
                                } else {
                                    // try next ancestor
                                    loopTier = parent;
                                    continue;
                                }
                            } else {
                                // the parent is not selected
                                if (destTrans.getTierWithId(parentName) != null) {
                                    if (lingTypeCompatible(parent, destTrans.getTierWithId(parentName))) {
                                        validTiers.add(t); 
                                    }
                                    
                                    break;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                    
                } else {
                    // already in destination, check linguistic type
                    if (lingTypeCompatible(t, t2)) {
                        validTiers.add(t);
                    }
                }
            } else {
            	ClientLogger.LOG.warning("Tier " + name + " does not exist.");
            }
            if (!validTiers.contains(t)) {
            	ClientLogger.LOG.warning("Cannot add tier " + name);
            }
        }
        return validTiers;
    }

    /**
     * Check whether the LinguisticTypes of the tiers have the same stereotype.
     * This is a loose check, other attributes could also be checked; name, cv etc. 
     */       
    public boolean lingTypeCompatible(TierImpl t, TierImpl t2) {
        if (t == null || t2 == null) {
            return false;
        }
        // check linguistic type
        LinguisticType lt = t.getLinguisticType();
        LinguisticType lt2 = t2.getLinguisticType();
        // loosely check the linguistic types
        if (/*lt.getLinguisticTypeName().equals(lt2.getLinguisticTypeName()) &&*/ 
                lt.hasConstraints() == lt2.hasConstraints()) {
            if (lt.getConstraints() != null) {
                if (lt.getConstraints().getStereoType() == lt2.getConstraints().getStereoType()) {
                    return true;
                } else {
                	ClientLogger.LOG.warning("Incompatible tier types in source and destination: " + t.getName());
                    return false;
                }
            } else {
                // both toplevel tiers
                return true;
            }
        }
        return false;
    }
    
    /**
     * Compares (mainly) the stereotypes of the two types.
     * 
     * @param lType1 first type
     * @param lType2 second type
     * @return true if the the types have the same stereotype
     */
    public boolean lingTypeCompatible(LinguisticType lType1, LinguisticType lType2) {
    	if (lType1 == null) {
    		return false;// log
    	}
    	if (lType2 == null) {
    		return false;//log
    	}
    	
    	if (!constraintsCompatible(lType1.getConstraints(), lType2.getConstraints())) {
    		return false;
    	}
    	
    	// TODO check CV
    	
    	return true;
    }
    
    /**
     * Check if two Constraint objects are compatible,
     * which in this case means either both null, or both having the same stereotype.
     */
    public boolean constraintsCompatible(Constraint c1, Constraint c2) {
    	if (c1 == null && c2 == null) {
    		return true;
    	}
    	if (c1 == null || c2 == null) {
    		return false;
    	}
    	return c1.getStereoType() == c2.getStereoType();
    }
    
    /**
     * Checks whether the second type can be used for a tier that is a direct child of
     * a tier based on the first type.
     *  
     * @param parentType the type for the parent tier
     * @param childType the type for the child tier
     * 
     * @return true if the the second type can be used for a tier that is a child of a tier of the first type,
     * false otherwise
     */
    public boolean parentChildTypeCompatible(LinguisticType parentType, LinguisticType childType) {
    	if (parentType == null || childType == null) {
    		return false;// log...
    	}
    	
    	if (!parentType.isTimeAlignable() && childType.isTimeAlignable()) {
    		return false;
    	}
    	
    	if (!childType.hasConstraints()) {
    		return false;
    	}
    	
    	if (!parentType.hasConstraints()) {// top level
    		return childType.hasConstraints();// any constraints
    	}
    	
    	// now check for situations where the parent is not a top level type
    	ConstraintImpl parentCon = (ConstraintImpl) parentType.getConstraints();
    	ConstraintImpl childCon = (ConstraintImpl) childType.getConstraints();
    	// both should be guaranteed to be not null now
    	boolean compatible = false;
    	
    	switch(parentCon.getStereoType()) {
    	case Constraint.TIME_SUBDIVISION:
    		switch (childCon.getStereoType()) {// all currently used types are compatible
    		case Constraint.INCLUDED_IN:
    		case Constraint.TIME_SUBDIVISION:
    		case Constraint.SYMBOLIC_SUBDIVISION:
    		case Constraint.SYMBOLIC_ASSOCIATION:
    			compatible = true;
    			break;
    		}
    		break;
    	case Constraint.INCLUDED_IN:
    		switch(childCon.getStereoType()) {
    		// all currently used types are compatible, the first two cases mean that if parentType.isTimeAlignable() could return true
    		case Constraint.INCLUDED_IN:
    		case Constraint.TIME_SUBDIVISION:
    		case Constraint.SYMBOLIC_SUBDIVISION:
    		case Constraint.SYMBOLIC_ASSOCIATION:
    			compatible = true;
    			break;
    		}
    		break;
    	case Constraint.SYMBOLIC_SUBDIVISION:
    		switch (childCon.getStereoType()) {
    		case Constraint.SYMBOLIC_SUBDIVISION:
    		case Constraint.SYMBOLIC_ASSOCIATION:
    			compatible = true;
    			break;
    		}
    		break;
    	case Constraint.SYMBOLIC_ASSOCIATION:
    		switch (childCon.getStereoType()) {
    		case Constraint.SYMBOLIC_SUBDIVISION:
    		case Constraint.SYMBOLIC_ASSOCIATION:
    			compatible = true;
    			break;
    		}
    		break;
    	}
    	
    	return compatible;
    }

    /**
     * Sort the tiers in the list hierarchically.
     * @param tiers the tiers
     */
    public <T extends Tier> List<T> sortTiers(List<T> tiersToSort) {
        if (tiersToSort == null || tiersToSort.size() == 0) {
            return null;
        }
        
        DefaultMutableTreeNode sortedRootNode = new DefaultMutableTreeNode(
        "sortRoot");
        HashMap<Tier, DefaultMutableTreeNode> nodes = new HashMap<Tier, DefaultMutableTreeNode>();
        Tier t = null;
        for (int i = 0; i < tiersToSort.size(); i++) {
            t = tiersToSort.get(i);

            DefaultMutableTreeNode node = new DefaultMutableTreeNode(t);
            nodes.put(t, node);
        }

        for (int i = 0; i < tiersToSort.size(); i++) {
            t = tiersToSort.get(i);

            if ((t.getParentTier() == null) ||
                    !tiersToSort.contains(t.getParentTier())) {
                sortedRootNode.add(nodes.get(t));
            } else {
                nodes.get(t.getParentTier()).add(
                        nodes.get(t));
            }
        }
        
        //tiersToAdd.clear();
        List<T> sorted = new ArrayList<T>(tiersToSort.size());

        Enumeration<DefaultMutableTreeNode> en = sortedRootNode.breadthFirstEnumeration();

        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = en.nextElement();

            if (node.getUserObject() instanceof Tier) {
                sorted.add((T)(Tier) node.getUserObject());
            }
        }
        return sorted;
    }

    /**
     * Sorts the list of tier names based on the hierarchy the tiers have in the transcription.
     * First the actual tiers are retrieved from the transcription, that lists is sorted 
     * and then a list of tier names is returned.
     * 
     * @param transcription the transcription
     * @param tierNames the list of tier names to sort hierarchically
     * @return the sorted list of tier names
     */
    public List<String> sortTiers(TranscriptionImpl transcription, List<String> tierNames) {
    	if (transcription == null || tierNames == null || tierNames.size() == 0) {
    		return null;
    	}
    	
    	List<Tier> tierList = new ArrayList<Tier>(tierNames.size());
    	
    	for (String name : tierNames) {
    		TierImpl t = transcription.getTierWithId(name);
    		if (t != null) {
    			tierList.add(t);
    		}
    	}
    	
    	tierList = sortTiers(tierList);
    	
    	if (tierList != null) {
    		List<String> sortedNames = new ArrayList<String>(tierList.size());
    		
    		for (Tier t : tierList) {
    			sortedNames.add(t.getName());
    		}
    		
    		return sortedNames;
    	}
    	
     	return null;
    }
    
    /**
     * Returns a list of tier objects, sorted hierarchically.
     *  
     * @param transcription the transcription
     * @param tiersToSort the list of tier names to sort hierarchically
     * @return a list of Tier objects corresponding to the tier names, sorted
     */
    public List<TierImpl> getSortedTiers(TranscriptionImpl transcription, List<String> tiersToSort) {   	
    	if (transcription == null || tiersToSort == null || tiersToSort.size() == 0) {
    		return null;
    	}
    	
    	List<TierImpl> tierList = new ArrayList<TierImpl>(tiersToSort.size());
    	
    	for (String name : tiersToSort) {
    		TierImpl t = transcription.getTierWithId(name);
    		if (t != null) {
    			tierList.add(t);
    		}
    	}
    	
    	return sortTiers(tierList);
    }
    
    /**
     * Returns all tiers from the specified list of tiers that don't have their parent tier 
     * (or ancestor) in the same list.
     * 
     * @param transcription the transcription the tiers should be in
     * @param tiersToInspect the list of tiers to extract (sub) top level tiers from
     * 
     * @return a list of (sub) top level tiers
     */
    public List<String> getTiersWithoutParentInGroup(TranscriptionImpl transcription, List<String> tiersToInspect) {
    	if (transcription == null || tiersToInspect == null) {
    		return null;
    	}
    	List<String> rootAndSubRoots = new ArrayList<String> (tiersToInspect.size());
    	TierImpl t1;
    	Tier t2;
    	
    	for(int i = 0; i < tiersToInspect.size(); i++) {
    		t1 = transcription.getTierWithId(tiersToInspect.get(i));
    		if (t1 == null) {
    			continue;
    		}
    		
    		for (int j = 0; j < tiersToInspect.size(); j++) {
    			if (j == i && j != tiersToInspect.size() - 1) {
    				// if j == i == the last element in the list, it will be added as well
    				continue;
    			}
    			t2 = transcription.getTierWithId(tiersToInspect.get(j));
    			if (t2 != null) {
    				if (t1.hasAncestor(t2)) {
    					// don't add t1
    					break;
    				}
    			}
    			if (j == tiersToInspect.size() - 1) {
    				// last element, ancestor not found
    				rootAndSubRoots.add(tiersToInspect.get(i));
    			}
    		}
    	}
    	
    	return rootAndSubRoots;
    }
    
    /**
     * Adds the tiers that are not yet in the destination transcription,
     * after performing some checks.  If Linguistic types and/or CV's should
     * be copied/added these are copied/added first. It is assumed that it is 
     * save to add LT's and CV's to the destination Transcription without cloning.
     *
     * NOTE: this method assumes that the source transcription is loaded for the 
     * purpose of this merging. Some objects that are added to the destination transcription
     * are not cloned, e.g. tier type objects of the source transcription are added to the 
     * destination transcription (and should therefore not be edited or saved anymore
     * in source.
     * 
     * @param tiersToAdd a list of tiers to add to the destination
     */
    public void addTiersTypesAndCVs(TranscriptionImpl srcTrans, TranscriptionImpl destTrans, 
            List<TierImpl> tiersToAdd) {
        if (srcTrans == null) {
        	ClientLogger.LOG.warning("Source transcription is null.");
            return;
        }
        if (destTrans == null){
        	ClientLogger.LOG.warning("Destination transcription is null");
            return;
        }
        if (tiersToAdd == null || tiersToAdd.size() == 0) {
        	ClientLogger.LOG.warning("No tiers to add");
            return;
        }
//        System.out.println("num tiers: " + tiersToAdd.size());
        Map<String, ControlledVocabulary> renamedCVS = new HashMap<String, ControlledVocabulary>(5);
        Map<String, String> renamedTypes = new HashMap<String, String>(5);
        List<LinguisticType> typesToAdd = new ArrayList<LinguisticType>(5);
        List<ControlledVocabulary> cvsToAdd = new ArrayList<ControlledVocabulary>(5);
        TierImpl t;
        TierImpl t2;
        TierImpl newTier;
        LinguisticType lt;
        LinguisticType lt2 = null;
        String typeName;
        ControlledVocabulary cv;
        ControlledVocabulary cv2 = null;

        for (int i = 0; i < tiersToAdd.size(); i++) {
            t = tiersToAdd.get(i);
            if (t == null || destTrans.getTierWithId(t.getName()) != null) {
                // don't do further checks on ling. type and cv
                continue;
            }
            lt = t.getLinguisticType();
            if (typesToAdd.contains(lt)) {
                continue;
            }
            typeName = lt.getLinguisticTypeName();
            lt2 = destTrans.getLinguisticTypeByName(typeName);
            if (lt2 != null) {//already there
                if (lt.getConstraints() == null && lt2.getConstraints() == null) {
                    continue;
                } else if (lt.getConstraints() != null && lt2.getConstraints() != null) {
                    if (lt.getConstraints().getStereoType() == lt.getConstraints().getStereoType()) {
                        continue;
                    }
                }
                // rename and add
                String nname = typeName + "-cp";
                int c = 1;
                while (destTrans.getLinguisticTypeByName(nname + c) != null) {
                    c++;
                }
                nname = nname + c;
                if (!renamedTypes.containsKey(typeName)) {
                    renamedTypes.put(typeName, nname); 
                }
                
            }// check if they are the same?

            typesToAdd.add(lt);

            if (lt.isUsingControlledVocabulary()) {
                cv = srcTrans.getControlledVocabulary(lt.getControlledVocabularyName());

                if (!cvsToAdd.contains(cv)) {
                    cvsToAdd.add(cv);
                }
            }
        }

        // add CV's, renaming when necessary
        for (int i = 0; i < cvsToAdd.size(); i++) {
            cv = cvsToAdd.get(i);
            cv2 = destTrans.getControlledVocabulary(cv.getName());

            if (cv2 == null) {
                destTrans.addControlledVocabulary(cv);
                ClientLogger.LOG.info("Added Controlled Vocabulary: " + cv.getName());
            } else if (!cv.equals(cv2)) {
                // rename
                String newCVName = cv.getName() + "-cp";
                int c = 1;
                while (destTrans.getControlledVocabulary(newCVName + c) != null) {
                    c++;
                }
                newCVName = newCVName + c;
                ClientLogger.LOG.info("Renamed Controlled Vocabulary: " + cv.getName() +
                    " to " + newCVName);
                renamedCVS.put(cv.getName(), cv);
                cv.setName(newCVName);
                destTrans.addControlledVocabulary(cv);
                ClientLogger.LOG.info("Added Controlled Vocabulary: " + cv.getName());
            }
        } // end cv
        // add linguistic types
        for (int i = 0; i < typesToAdd.size(); i++) {
            lt = typesToAdd.get(i);

            typeName = lt.getLinguisticTypeName();

            if (lt.isUsingControlledVocabulary() &&
                    renamedCVS.containsKey(lt.getControlledVocabularyName())) {
                cv2 = renamedCVS.get(lt.getControlledVocabularyName());
                lt.setControlledVocabularyName(cv2.getName());
            }

            if (renamedTypes.containsKey(lt.getLinguisticTypeName())) {     
                String newLTName = renamedTypes.get(lt.getLinguisticTypeName());
                
                ClientLogger.LOG.info("Renamed Linguistic Type: " +
                            lt.getLinguisticTypeName() + " to " + newLTName);
                lt.setLinguisticTypeName(newLTName);                           
            } 
            destTrans.addLinguisticType(lt);
            ClientLogger.LOG.info("Added Linguistic Type: " +
                        lt.getLinguisticTypeName());

        } // end linguistic types

        // add tiers if necessary
        for (int i = 0; i < tiersToAdd.size(); i++) {
//            System.out.println("i: " + i);
            t = tiersToAdd.get(i);
            
            if (destTrans.getTierWithId(t.getName()) != null) {
                continue;
            }
            t2 = t.getParentTier();

            String parentTierName = null;

            if (t2 != null) {
                parentTierName = t2.getName();
            }

            newTier = null;
            if (parentTierName == null) {
                newTier = new TierImpl(t.getName(), t.getParticipant(),
                        destTrans, null);
            } else {
                t2 = destTrans.getTierWithId(parentTierName);

                if (t2 != null) {
                    newTier = new TierImpl(t2, t.getName(), t.getParticipant(),
                            destTrans, null);
                } else {
                	ClientLogger.LOG.warning("The parent tier: " + parentTierName +
                        " for tier: " + t.getName() +
                        " was not found in the destination transcription");
                }
            }

            if (newTier != null) {
                lt = t.getLinguisticType();
                lt2 = destTrans.getLinguisticTypeByName(lt.getLinguisticTypeName());

                if (lt2 != null) {                  
                    newTier.setLinguisticType(lt2);
                    destTrans.addTier(newTier);
                    ClientLogger.LOG.info("Created and added tier to destination: " +
                            newTier.getName());
                } else {
                	ClientLogger.LOG.warning("Could not add tier: " + newTier.getName() +
                        " because the Linguistic Type was not found in the destination transcription.");
                }
                newTier.setDefaultLocale(t.getDefaultLocale());
                newTier.setAnnotator(t.getAnnotator());
                newTier.setLangRef(t.getLangRef());
            }
        } //end tiers
    }

    
}

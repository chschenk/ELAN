package mpi.eudico.client.annotator.imports;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.ConstraintImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexicalEntryFieldIdentification;
import mpi.eudico.server.corpora.lexicon.LexiconIdentification;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.multilangcv.LangInfo;


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
        	LOG.warning("A Transcription is null");
            return new ArrayList<TierImpl>(0); 
        }
        if (selTiers == null) {
            selTiers = new ArrayList<String>(srcTrans.getTiers().size());
            for (Tier ti : srcTrans.getTiers()) {
            	selTiers.add(ti.getName());
            }
        }
        List<TierImpl> validTiers = new ArrayList<TierImpl>(selTiers.size());

        for (int i = 0; i < selTiers.size(); i++) {
            String name = selTiers.get(i);
            TierImpl t = srcTrans.getTierWithId(name);
            if (t != null) {
                TierImpl t2 = destTrans.getTierWithId(name);
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
            	LOG.warning("Tier " + name + " does not exist.");
            }
            if (!validTiers.contains(t)) {
            	LOG.warning("Cannot add tier " + name);
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
                	LOG.warning("Incompatible tier types in source and destination: " + t.getName());
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
    @SuppressWarnings("unchecked")
	public <T extends Tier> List<T> sortTiers(List<T> tiersToSort) {
        if (tiersToSort == null || tiersToSort.size() == 0) {
            return null;
        }
        
        DefaultMutableTreeNode sortedRootNode = new DefaultMutableTreeNode(
        "sortRoot");
        HashMap<Tier, DefaultMutableTreeNode> nodes = new HashMap<Tier, DefaultMutableTreeNode>();
        
        for (int i = 0; i < tiersToSort.size(); i++) {
        	Tier t = tiersToSort.get(i);

            DefaultMutableTreeNode node = new DefaultMutableTreeNode(t);
            nodes.put(t, node);
        }

        for (int i = 0; i < tiersToSort.size(); i++) {
            Tier t = tiersToSort.get(i);

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
        	LOG.warning("Source transcription is null.");
            return;
        }
        if (destTrans == null){
        	LOG.warning("Destination transcription is null");
            return;
        }
        if (tiersToAdd == null || tiersToAdd.size() == 0) {
        	LOG.warning("No tiers to add");
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
                LOG.info("Added Controlled Vocabulary: " + cv.getName());
            } else if (!cv.equals(cv2)) {
                // rename
                String newCVName = cv.getName() + "-cp";
                int c = 1;
                while (destTrans.getControlledVocabulary(newCVName + c) != null) {
                    c++;
                }
                newCVName = newCVName + c;
                LOG.info("Renamed Controlled Vocabulary: " + cv.getName() +
                    " to " + newCVName);
                renamedCVS.put(cv.getName(), cv);
                cv.setName(newCVName);
                destTrans.addControlledVocabulary(cv);
                LOG.info("Added Controlled Vocabulary: " + cv.getName());
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
                
                LOG.info("Renamed Linguistic Type: " +
                            lt.getLinguisticTypeName() + " to " + newLTName);
                lt.setLinguisticTypeName(newLTName);                           
            } 
            destTrans.addLinguisticType(lt);
            LOG.info("Added Linguistic Type: " +
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
                	LOG.warning("The parent tier: " + parentTierName +
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
                    LOG.info("Created and added tier to destination: " +
                            newTier.getName());
                } else {
                	LOG.warning("Could not add tier: " + newTier.getName() +
                        " because the Linguistic Type was not found in the destination transcription.");
                }
                newTier.setDefaultLocale(t.getDefaultLocale());
                newTier.setAnnotator(t.getAnnotator());
                newTier.setLangRef(t.getLangRef());
            }
        } //end tiers
    }

    /**
     * Checks which of the selected tiers is already present in the destination transcription
     * and renames those tiers (and dependent tiers) in the source transcription. The renaming 
     * is implemented as adding a number/renumbering.
     * 
     * Important note: the assumption is that the source transcription can be safely changed,  
     * without undo etc.
     * 
     * @param srcTrans the transcription containing the tiers to add to the other transcription
     * @param destTrans the transcription tier will be added to
     * @param selTiers the (names of) the selected tiers
     *  
     * @return a map containing mappings from original name to modified name or to the same name if
     * no renaming is required
     */
    public Map<String, String> getRenamingTierMap(TranscriptionImpl srcTrans, TranscriptionImpl destTrans,
            List<String> selTiers) {
        if (srcTrans == null || destTrans == null) {
        	LOG.warning("A Transcription is null");
            return new HashMap<String, String>(0); 
        }
        // add all tiers if no selection has been passed
        if (selTiers == null) {
            selTiers = new ArrayList<String>(srcTrans.getTiers().size());
            for (Tier ti : srcTrans.getTiers()) {
            	selTiers.add(ti.getName());
            }
        }
        
        HashMap<String, String> nameMap = new HashMap<String, String>();
        final String HYPH = "-";
        final String END_PAT = ".+" + HYPH + "[0-9]+$";
        
        for (String tName : selTiers) {
        	// check if the tier was already processed as dependent tier of another tier
        	if (nameMap.containsKey(tName)) {
        		continue;
        	}
        	if (srcTrans.getTierWithId(tName) != null) {
        		if (destTrans.getTierWithId(tName) != null) {
        			int count = 1;
        			// check pattern of tName, if it already has a -00 suffix
        			String bareName = tName;
        			if (tName.matches(END_PAT)) {
        				String curNum = tName.substring(tName.lastIndexOf(HYPH) + 1);
        				try {
        					count = Integer.parseInt(curNum);
        				} catch (NumberFormatException nfe) {}
        				
        				bareName = tName.substring(0, tName.lastIndexOf(HYPH));
        			}
        			
        			for ( ; count <= 100; count++) {
        				String nextTName = bareName + HYPH + count;
        				if (destTrans.getTierWithId(nextTName) == null && !nameMap.containsValue(nextTName)) {
        					nameMap.put(tName, nextTName);
        					
        					TierImpl t = destTrans.getTierWithId(tName);
        					List<TierImpl> depTiers = t.getDependentTiers();
        					// try to use the same suffix number for dependent tiers
        					for (TierImpl ti : depTiers) {       						
        						if (selTiers.contains(ti.getName())) {
        							String bareDepName = ti.getName();
        							if (bareDepName.matches(END_PAT)) {
        								bareDepName = bareDepName.substring(0, bareDepName.lastIndexOf(HYPH));
        							}
        							String nextDepTierName = bareDepName + HYPH + count;
        							if (destTrans.getTierWithId(nextDepTierName) == null && 
        									!nameMap.containsValue(nextDepTierName)) {
        								nameMap.put(ti.getName(), nextDepTierName);
        							} else {
        								// unexpected situation: tierA-2 doesn't exist but subtierA-2 exists
        								// increment the count with a new counter
        								int ncount = count + 1;
        								for (; ncount<= 100; ncount++) {
        									nextDepTierName = bareDepName + HYPH + ncount;
        									if (destTrans.getTierWithId(nextDepTierName) == null && 
        											!nameMap.containsValue(nextDepTierName)) {
        										nameMap.put(ti.getName(), nextDepTierName);
        										break;
        									}
        								}
        							}
        						}
        					}
        					// break the count loop
        					break;
        				}
        			}
        		} else {
        			nameMap.put(tName, tName);
        		}
        	} else {
        		nameMap.put(tName, tName);
        	}
        }
        
    	return nameMap;
    }
    
    // #### utility methods for updating items in one transcription (target) based on ####
    // #### the items in another transcription (source, could be a template)          ####
    /**
     * Update a target controlled vocabulary in a target transcription based on the source 
     * controlled vocabulary which is assumed to have the same ID (= name).
     * New languages are added, descriptions are updated, new entries are added (this 
     * concerns internal CV's), internal CV's are converted to external CV's and vice versa,
     * url's of ECV's are updated.
     * Entries that are in the target CV but not in the source are left untouched (unless 
     * the forceReplace flag is true). 
     * 
     * @param srcTrans the transcription containing the source CV
     * @param targetTrans the transcription that contains or will contain the target CV 
     * @param cvName the name (id) of the Controlled Vocabulary to update
     * @param forceReplace replace internal CV's completely and forcibly
     * @param report if not null, messages will be added to the report
     */
    public void updateControlledVocabulary(TranscriptionImpl srcTrans, TranscriptionImpl targetTrans, String cvName, 
    		boolean forceReplace, ProcessReport report) {
        if (srcTrans == null) {
        	LOG.warning("The source Transcription is null");
        	return;
        }
        if (targetTrans == null) {
        	LOG.warning("The target Transcription is null"); 
        	return;
        }

        ControlledVocabulary srcCV = srcTrans.getControlledVocabulary(cvName);
    	if (srcCV == null) {
    		LOG.info(String.format("The source transcription does not contain a CV with name: \"%s\"", cvName));
    		if (report != null) {
    			report.append(String.format("The source does not contain a CV with name: \"%s\"", cvName));
    		}
    		return;
    	}
    	
		ControlledVocabulary targetCV = targetTrans.getControlledVocabulary(cvName);
		
    	if (forceReplace) {
    		ControlledVocabulary nextCV = srcCV.clone();
    		if (targetCV != null) {
	    		targetTrans.replaceControlledVocabulary(nextCV);
	    		if (report != null) {
	    			report.append(String.format("Performed a forced replace of target CV: \"%s\"", nextCV.getName()));
	    		}
    		} else {
    			// targetCV is null, doesn't exist, add a copy
    			targetTrans.addControlledVocabulary(nextCV);
    			if (report != null) {
    				report.append(String.format("Added new CV: \"%s\"", nextCV.getName()));
    			}
    		}
    		return;
    	}
    	
    	// update url of external CV, convert internal to external and vice versa
    	if (srcCV instanceof ExternalCV) {
    		ExternalCV srcECV = (ExternalCV) srcCV;
    		
    		if (targetCV instanceof ExternalCV) {
    			ExternalCV targetECV = (ExternalCV) targetCV;
    			if (srcECV.getExternalRef() != null) {
    				if (targetECV.getExternalRef() != null) {
    					if (srcECV.getExternalRef().getReferenceType() == 
    							targetECV.getExternalRef().getReferenceType() && srcECV.getExternalRef().getValue() != null &&
    							!srcECV.getExternalRef().getValue().equals(targetECV.getExternalRef().getValue())) {
    						targetECV.getExternalRef().setValue(srcECV.getExternalRef().getValue());
    						targetTrans.setChanged();// the ExternalReference object does not notify listeners of the change
    						if (report != null) {
    							report.append(String.format("Updated external URL of target CV \"%s\" to: \"%s\"", targetCV.getName(), 
    									targetECV.getExternalRef().getValue()));
    						}
    					}
    				} else {
    					// should not happen, ExternalCV without ext.ref., but add the reference
    					try {
    						ExternalReference erf = srcECV.getExternalRef().clone();
    						targetECV.setExternalRef(erf);
    						targetTrans.setChanged();
    						if (report != null) {
    							report.append(String.format("Added external URL: \"%s\" to target CV: \"%s\"", 
    									targetECV.getExternalRef().getValue(), targetCV.getName()));
    						}
    					} catch (CloneNotSupportedException cnse) {
    						// report
    						if (report != null) {
    							report.append(String.format("Cannot clone external reference of CV: \"%s\"", 
    									targetCV.getName()));
    						}
    					}   					
    				}
    			}// else sourceECV's ext.ref. is null, error situation
    		} else if (targetCV != null) {
    			// sourceCV is external, targetCV is internal, convert to external
    			ExternalCV convertCV = new ExternalCV(srcECV); // copies external reference as well //or new ExternalCV(targetCV);
    			targetTrans.replaceControlledVocabulary(convertCV);
    			if (report != null) {
    				report.append(String.format("Replaced internal CV by external CV: \"%s\"", srcECV.getName()));
    			}
    		} else {
    			// targetCV is null, does nor exist, add a copy
    			targetTrans.addControlledVocabulary(new ExternalCV(srcECV));
    			if (report != null) {
    				report.append(String.format("Added external CV: \"%s\"", srcECV.getName()));
    			}
    		}
    	} else {
    		// sourceCV is internal
    		if (targetCV == null) {
        		targetTrans.addControlledVocabulary(srcCV.clone());
        		if (report != null) {
        			report.append(String.format("Added internal CV: \"%s\"", srcCV.getName()));
        		}
        		return;
    		} else if (targetCV instanceof ExternalCV) { // replace external by internal
    			ControlledVocabulary convertCV = new ControlledVocabulary(srcCV.getName());
    			convertCV.clone(srcCV);
    			targetTrans.replaceControlledVocabulary(convertCV);
    			targetCV = convertCV;
    			if (report != null) {
    				report.append(String.format("Replaced external CV by internal CV: \"%s\"", srcCV.getName()));
    			}
    			return;
    		}
    		
    		// source and target are internal CV's
    		int numSrcLanguages = srcCV.getNumberOfLanguages();
    		int numTargetLanguages = targetCV.getNumberOfLanguages();
    		
    		if (numSrcLanguages == numTargetLanguages) {
    			// assume that if there is a difference in languages, one or more have been updated/changed
    			// rather than one deleted and another added
    			for (int i = 0; i < numSrcLanguages; i++) {
            		String lid = srcCV.getLanguageId(i);
            		int index2 = targetCV.getIndexOfLanguage(lid);
            		if (index2 < 0) {
            			String curLid = targetCV.getLanguageId(i);
            			if (srcCV.getIndexOfLanguage(curLid) < 0) {
            				// assume replace/update of this index
            				LangInfo lInfo = srcCV.getLangInfo(i);
            				targetCV.setLanguageIds(i, lInfo.getId(), lInfo.getLongId(), lInfo.getLabel());
            				if (report != null) {
            					report.append(String.format("Changed Language of target CV: \"%s\", at index %d from \"%s\" to \"%s\"", 
            							targetCV.getName(), i, curLid, lid ));
            				}
            				// check description
                			if (srcCV.getDescription(i) != null && !srcCV.getDescription(i).equals(targetCV.getDescription(i))) {
                				targetCV.setDescription(i, srcCV.getDescription(i));
                				if (report != null) {
                					report.append(String.format("Updated Description of target CV: \"%s\" at index %d to \"%s\"",
                							targetCV.getName(), i, targetCV.getDescription(i)));
                				}
                			}
            			} else {
            				// the language currently at index i of the target is also there in the source but at different index
            			}
            		} else if (i == index2){
            			// check description
            			if (srcCV.getDescription(i) != null && !srcCV.getDescription(i).equals(targetCV.getDescription(i))) {
            				targetCV.setDescription(i, srcCV.getDescription(i));
            				if (report != null) {
            					report.append(String.format("Updated Description of target CV: \"%s\" at index %d to \"%s\"",
            							targetCV.getName(), i, targetCV.getDescription(index2)));
            				}
            			}
            		} else {
            			// the language at index i of the source is also there in the target but at different index
            			// check description
            			if (srcCV.getDescription(i) != null && !srcCV.getDescription(i).equals(targetCV.getDescription(index2))) {
            				targetCV.setDescription(index2, srcCV.getDescription(i));
            				if (report != null) {
            					report.append(String.format("Updated Description of target CV: \"%s\" at index %d to \"%s\"",
            							targetCV.getName(), index2, targetCV.getDescription(index2)));
            				}
            			}
            		}
    			}
    		} else { // different number of languages		
	        	// add languages, do not correct inconsistencies in language order, do not delete languages
	        	for (int i = 0; i < numSrcLanguages; i++) {
	        		String lid = srcCV.getLanguageId(i);
	        		int index2 = targetCV.getIndexOfLanguage(lid);
	        		if (index2 < 0) {
	        			LangInfo langInfo = srcCV.getLangInfo(i);
	        			int ni = targetCV.addLanguage(langInfo);// creates a copy
	        			if (ni < 0) {
	        				if (report != null) {
	        					report.append(String.format("Failed to add Language: \"%s\" to target CV: \"%s\"", lid, targetCV.getName()));
	        				}
	        				continue;
	        			} else {
	        				index2 = ni;
	        				if (report != null) {
	        					report.append(String.format("Added Language: \"%s\" to target CV: \"%s\"", lid, targetCV.getName()));
	        				}
	        			}
	        		}
	        		// check description
	        		if (index2 >= 0) {
	        			if (srcCV.getDescription(i) != null && !srcCV.getDescription(i).equals(targetCV.getDescription(index2))) {
	        				targetCV.setDescription(index2, srcCV.getDescription(i));
	        				if (report != null) {
	        					report.append(String.format("Updated Description of target CV: \"%s\"  at index %d to \"%s\"", 
	        							targetCV.getName(), index2, targetCV.getDescription(index2)));
	        				}
	        			}
	        		}
	        	}
    		}
        	
        	// update with new CV Entries??
        	if (!srcCV.equals(targetCV)) { // this equals ignores differences in order and ignores id's 
        		Iterator<CVEntry> entryIt = srcCV.iterator();
        		int numAdded = 0;
        		int numLangValueAdded = 0;
        		
        		while (entryIt.hasNext()) {
        			CVEntry entry = entryIt.next();
        			CVEntry tarEntry = targetCV.getEntrybyId(entry.getId());
        			if (tarEntry == null) {
        				targetCV.addEntry(new CVEntry(targetCV, entry));
        				numAdded++;
        			} else {
        				for (int i = 0; i < numSrcLanguages; i++) {
        					String sv = entry.getValue(i);
        					int index2 = targetCV.getIndexOfLanguage(srcCV.getLanguageId(i));
        					if (index2 >= 0) {
        						String tv = tarEntry.getValue(index2);
        						if (tv == null || !sv.equals(tv)) {
        							tarEntry.setValue(index2, sv);
        							targetTrans.setChanged();
        							numLangValueAdded++;
        							
        							// entry description
        							String srcDesc = entry.getDescription(i);
        							if (srcDesc != null && !srcDesc.equals(tarEntry.getDescription(index2))) {
        								tarEntry.setDescription(index2, srcDesc);
        							}
        							// entry external ref
        							if (entry.getExternalRef() != null && !entry.getExternalRef().equals(tarEntry.getExternalRef())) {
        								try {
        									tarEntry.setExternalRef(entry.getExternalRef().clone());
        								} catch (CloneNotSupportedException cnse) {
        									// log?
        								}
        							}
        						}
        					}
        				}
        			}
        		}
        		if (report != null) {
        			if (numAdded > 0) {
	        			report.append(String.format("Added %d entries to target CV: \"%s\"", 
	        					numAdded, targetCV.getName()));
        			}
        			if (numLangValueAdded > 0) {
        				// and added or updated %d entry values
        				report.append(String.format("Added or updated %d entry values to target CV: \"%s\"", 
        						numLangValueAdded, targetCV.getName()));
        			}
        		}
        	}
    	}    	
    }
    
    /**
     * Updates or adds a tier type in the target transcription based on the attributes 
     * and properties of the same type (with the same name or id) in the source transcription.
     * The Stereotype of a tier type will not be updated because that could lead to inconsistencies
     * and data loss.
     * The association with a CV, a lexicon and lexical entry field and with a data category
     * are updated (including setting to null if tha is the case in the source).
     * 
     * @param srcTrans the transcription containing the source type
     * @param targetTrans the transcription to be updated
     * @param typeName the name of the tier type
     * @param report if not null, messages will be added to this report
     */
    public void updateTierType(TranscriptionImpl srcTrans, TranscriptionImpl targetTrans, String typeName, 
    		ProcessReport report) {
        if (srcTrans == null) {
        	LOG.warning("The source Transcription is null");
        	return;
        }
        if (targetTrans == null) {
        	LOG.warning("The target Transcription is null"); 
        	return;
        }
        
        LinguisticType srcType = srcTrans.getLinguisticTypeByName(typeName);
        if (srcType == null) {
        	LOG.info("The source transcription does not contain a Tier Type with name: " + typeName);
        	return;
        }
        boolean changed = false;
        LinguisticType targetType = targetTrans.getLinguisticTypeByName(typeName);
        
        if (targetType != null) {
        	// update the type as much as possible
        	int srcStereo = srcType.getConstraints() == null ? -1 : srcType.getConstraints().getStereoType();
        	int tarStereo = targetType.getConstraints() == null ? -1 : targetType.getConstraints().getStereoType();
        	if (srcStereo != tarStereo) {
        		LOG.info(String.format("Incompatible stereotypes of Tier Type: \"%s\", cannot update the stereotype", typeName));
        		if (report != null) {
        			report.append(String.format("Incompatible stereotypes of Tier Type: \"%s\", cannot update the stereotype", typeName));
        		}
        	}
        	// check CV
        	if (srcType.isUsingControlledVocabulary()) {
        		if (!srcType.getControlledVocabularyName().equals(targetType.getControlledVocabularyName())) {
        			if (targetTrans.getControlledVocabulary(srcType.getControlledVocabularyName()) != null) {
        				targetType.setControlledVocabularyName(srcType.getControlledVocabularyName());
        				changed = true;
        				if (report != null) {
        					report.append(String.format("Changed the CV of Type \"%s\" to: \"%s\"", typeName, srcType.getControlledVocabularyName()));
        				}
        			} else {
        				if (report != null) {
        					report.append(String.format("Cannot change the CV of Type \"%s\" to: \"%s\"; the CV does not exist in target", 
        							typeName, srcType.getControlledVocabularyName()));
        				}
        			}
        		}// else already equal
        	} else {
        		if (targetType.isUsingControlledVocabulary()) {
        			String oldCV = targetType.getControlledVocabularyName();
        			targetType.setControlledVocabularyName(null);
        			changed = true;
        			if (report != null) {
        				report.append(String.format("Removed the CV from Type \"%s\" (was: \"%s\")", typeName, oldCV));
        			}
        		}
        	}
        	// update Data Category
        	if (srcType.getDataCategory() != null) {
        		if (!srcType.getDataCategory().equals(targetType.getDataCategory())) {
        			targetType.setDataCategory(srcType.getDataCategory());
        			changed = true;
        			if (report != null) {
        				report.append(String.format("Changed the Data Category of Type \"%s\" to: \"%s\"", typeName, srcType.getDataCategory()));
        			}
        		}
        	} else {
        		if (targetType.getDataCategory() != null) {
        			String oldDC = targetType.getDataCategory();
        			targetType.setDataCategory(null);
        			changed = true;
        			if (report != null) {
        				report.append(String.format("Removed the Data Category of Type \"%s\" (was: \"%s\")", typeName, oldDC));
        			}
        		}
        	}
        	// update lexicon link, lexicon query bundle
        	if (srcType.getLexiconQueryBundle() != null) {
        		LexiconQueryBundle2 srcBundle = srcType.getLexiconQueryBundle();
        		if (targetType.getLexiconQueryBundle() == null) {
        			// add a copy using the copy constructor
        			targetType.setLexiconQueryBundle(
        					new LexiconQueryBundle2(srcBundle));
        			changed = true;
        			if (report != null) {
        				report.append(String.format("Added a Lexicon Link to Type \"%s\": \"%s\"", typeName, srcBundle.getLinkName()));
        			}
        		} else {
        			// update differences, or completely replace
        			boolean bundleChange = false;
        			boolean copySrcLEFI = false;
        			boolean copySrcLL = false;
        			
        			LexiconQueryBundle2 targetBundle = targetType.getLexiconQueryBundle(); 
        			
        			LexicalEntryFieldIdentification srcLefi = srcBundle.getFldId();
        			LexicalEntryFieldIdentification tarLefi = targetBundle.getFldId();
        			
        			if (srcLefi != null) {
        				if (tarLefi != null) {
        					// don't set ID or name to null if it isn't null in the target
        					if (srcLefi.getId() != null && !srcLefi.getId().equals(tarLefi.getId())) {
        						tarLefi.setId(srcLefi.getId());
        						bundleChange = true;
        					}
        					if (srcLefi.getName() != null && !srcLefi.getName().equals(tarLefi.getName())) {
        						tarLefi.setName(srcLefi.getName());
        						bundleChange = true;
        					}
        					if ((srcLefi.getDescription() == null && tarLefi.getDescription() != null) ||
        							(srcLefi.getDescription() != null && !srcLefi.getDescription().equals(tarLefi.getDescription()))) {
        						tarLefi.setDescription(srcLefi.getDescription());
        						bundleChange = true;
        					}
        				} else {
        					// create a new bundle with a copy of the LexicalEntryFieldIdentification?
        					copySrcLEFI = true;
        				}
        			} else { // the src lex id is null, apply to the target?
        				if (tarLefi != null) {
        					copySrcLEFI = true;// set to null 
        				}
        			}
        			
        			LexiconLink srcLL = srcBundle.getLink();
        			LexiconLink tarLL = targetBundle.getLink();
        			// compare LexiconIdentification, name and URL
        			if (srcLL != null) {
        				if (tarLL != null) {
        					LexiconIdentification srcLI = srcLL.getLexId();
        					LexiconIdentification tarLI = tarLL.getLexId();
        					
        					if (srcLI != null && tarLI != null) {
        						if (srcLI.getId() != null && !srcLI.getId().equals(tarLI.getId())) {
        							tarLI.setId(srcLI.getId());
        							bundleChange = true;
        						}
        						if (srcLI.getName() != null && !srcLI.getName().equals(tarLI.getName())) {
        							tarLI.setName(srcLI.getName());
        							bundleChange = true;
        						}
        						if ((srcLI.getDescription() == null && tarLI.getDescription() != null) ||
        								(srcLI.getDescription() != null && !srcLI.getDescription().equals(
        										tarLI.getDescription()))) {
        							tarLI.setDescription(srcLI.getDescription());
        							bundleChange = true;
        						}
        					}
                			// compare name
        					if (srcLL.getName() != null && !srcLL.getName().equals(tarLL.getName())) {
        						tarLL.setName(srcLL.getName());
        						bundleChange = true;
        					}
        					// compare URL
        					if (srcLL.getUrl() != null && !srcLL.getUrl().equals(tarLL.getUrl())) {
        						tarLL.setUrl(srcLL.getUrl());
        						bundleChange = true;
        					}
        				} else {// targetLL == null
        					copySrcLL = true;
        				}
        			} else {//source lexicon link is null, apply to target? 
        				if (tarLL != null) {
        					copySrcLL = true;
        				}
        			}    			
        			
        			// update or replace bundle
        			if (copySrcLEFI || copySrcLL) {
        				LexicalEntryFieldIdentification nextLefi = null;
        				LexiconLink nextLL = null;
        				
        				if (copySrcLEFI) {
        					if (srcLefi != null) {
        						nextLefi = new LexicalEntryFieldIdentification(srcLefi.getId(), srcLefi.getName());
        						nextLefi.setDescription(srcLefi.getDescription());
        					}
        				}
        				if (copySrcLL) {
        					if (srcLL != null) {
        						nextLL = new LexiconLink(srcLL.getName(), srcLL.getLexSrvcClntType(), 
        								srcLL.getUrl(), null, 
        								srcLL.getLexId() == null ? null : new LexiconIdentification(srcLL.getLexId()));
        					}
        				}
        				
        				LexiconQueryBundle2 nextBundle = new LexiconQueryBundle2(nextLL, nextLefi);
        				targetType.setLexiconQueryBundle(nextBundle);
        				bundleChange = true;
        			}
        			
        			if (bundleChange) {
        				changed = true;
        				if (report != null) {
        					report.append(String.format("Changed the Lexicon Link of Type \"%s\"", typeName));
        				}
        			}
        		}
        	} else { // no lexicon link in source, remove from target?
        		if (targetType.getLexiconQueryBundle() != null) {
        			String oldName = targetType.getLexiconQueryBundle().getLinkName();
        			targetType.setLexiconQueryBundle(null);
    				changed = true;
    				if (report != null) {
    					report.append(String.format("Removed the Lexicon Link of Type \"%s\" (was: \"%s\")", typeName, oldName));
    				}
        		}
        	}
        } else {
        	//type not there yet, add a copy to the target transcription
        	LinguisticType nextLt = new LinguisticType(typeName, srcType);
        	targetTrans.addLinguisticType(nextLt);
        	changed = true;
        	if (report != null) {
        		report.append(String.format("Added Type \"%s\" to target" , typeName));
        	}
        }
        
		if (changed) {
			targetTrans.setChanged();
		}
    }
    
    /**
     * Updates the target tier in the target transcription based on properties 
     * of the source tier.
     * A few properties will not be updated: the parent tier of the target 
     * tier (this could potentially result in deletion of annotations) and the 
     * tier type in case the Stereotype of the source tier and target tier are
     * different. 
     * 
     * @param srcTrans the transcription containing the source tier
     * @param targetTrans the transcription containing the target tier
     * @param tierName the name of the tier to update
     * @param report if not null, messages will be added to this report
     */
    public void updateTier(TranscriptionImpl srcTrans, TranscriptionImpl targetTrans, String tierName, 
    		ProcessReport report) {
        if (srcTrans == null) {
        	LOG.warning("The source Transcription is null");
        	return;
        }
        if (targetTrans == null) {
        	LOG.warning("The target Transcription is null"); 
        	return;
        }
        
        TierImpl srcTier = srcTrans.getTierWithId(tierName);
        if (srcTier == null) {
        	LOG.info("The source transcription does not contain a Tier with name: " + tierName);
        	return;
        }
        
        TierImpl targetTier = targetTrans.getTierWithId(tierName);
        if (targetTier == null) {
        	LOG.info("The target transcription does not contain a Tier with name: " + tierName);
        	return;
        }
        
        // check parent names; if different, report that this can not be updated
        TierImpl srcParTier = srcTier.getParentTier();
        TierImpl tarParTier = targetTier.getParentTier();
        if ( (srcParTier == null && tarParTier != null) || 
        		(srcParTier != null && (tarParTier == null || !srcParTier.getName().equals(tarParTier.getName()))) ) {
        	if (report != null) {
        		report.append(String.format(
        				"Incompatible Tier hierarchy of source (parent = \"%s\") and target (parent = \"%s\"), cannot update", 
        				(srcParTier == null ? "none" : srcParTier.getName()), 
        				(tarParTier == null ? "none" : tarParTier.getName()) ));
        	}
        }
        // check tier type
        LinguisticType srcType = srcTier.getLinguisticType();
        LinguisticType tarType = targetTier.getLinguisticType();
        if (srcType.getLinguisticTypeName().equals(tarType.getLinguisticTypeName())) {
        	// same type name, ignore
        } else if (constraintsCompatible(srcType.getConstraints(), tarType.getConstraints())) {
        	LinguisticType nextType = targetTrans.getLinguisticTypeByName(srcType.getLinguisticTypeName());
        	if (nextType != null) {
        		targetTier.setLinguisticType(nextType);
        		if (report != null) {
        			report.append(String.format("Changed the Type of Tier \"%s\" to \"%s\"", tierName, 
        					nextType.getLinguisticTypeName()));
        		}
        	} else {
        		// this shouldn't happen in the scenario where first CV's, types and then tiers are added/updated.
        		// in principle the missing type could be added the transcription here first
        		if (report != null) {
        			report.append(String.format("Cannot change the Type of Tier \"%s\" to \"%s\", the Tier Type is not in the target", 
        					tierName, srcType.getLinguisticTypeName()));
        		}
        	}
        } else {
        	if (report != null) {
        		report.append(String.format("Incompatible Type of source (\"%s\") and target (\"%s\") Tier, cannot change the Type", 
        				ConstraintImpl.getStereoTypeName(
        						srcType.getConstraints() == null ? -1 : srcType.getConstraints().getStereoType()), 
        				ConstraintImpl.getStereoTypeName(
        						tarType.getConstraints() == null ? -1 : tarType.getConstraints().getStereoType()) ));
        	}
        }
        // check participant
        if (srcTier.getParticipant() != null && !srcTier.getParticipant().equals(targetTier.getParticipant())) {
        	targetTier.setParticipant(srcTier.getParticipant());
        	if (report != null) {
        		report.append(String.format("Changed the Participant of Tier \"%s\" to \"%s\"", tierName, srcTier.getParticipant()));
        	}
        }
        // annotator
        if (srcTier.getAnnotator() != null && !srcTier.getAnnotator().equals(targetTier.getAnnotator())) {
        	targetTier.setAnnotator(srcTier.getAnnotator());
        	if (report != null) {
        		report.append(String.format("Changed the Annotator of Tier \"%s\" to \"%s\"", tierName, srcTier.getAnnotator()));
        	}
        }
        // content language, uses short language id as a reference
        if (srcTier.getLangRef() != null && !srcTier.getLangRef().equals(targetTier.getLangRef())) {
        	targetTier.setLangRef(srcTier.getLangRef());
        	if (report != null) {
        		report.append(String.format("Changed the Content Language of Tier \"%s\" to \"%s\"", tierName, srcTier.getLangRef()));
        	}
        }
        // locale
        if (srcTier.getDefaultLocale() != null && !srcTier.getDefaultLocale().equals(targetTier.getDefaultLocale())) {
        	targetTier.setDefaultLocale(srcTier.getDefaultLocale()); // Locale is immutable, no need to clone
        	if (report != null) {
        		report.append(String.format("Changed the Locale (Input Method) of Tier \"%s\" to \"%s\"", 
        				tierName, srcTier.getDefaultLocale().toString()));
        	}
        } else if (srcTier.getDefaultLocale() == null && targetTier.getDefaultLocale() != null) {
        	Locale oldLoc = targetTier.getDefaultLocale();
        	targetTier.setDefaultLocale(null);
        	if (report != null) {
        		report.append(String.format("Removed the Locale (Input Method) of Tier \"%s\" (was \"%s\")", 
        				tierName, oldLoc.toString()));
        	}
        }
    }
    
    /**
     * Adds new tiers to the target transcription. It is assumed that checks have been performed
     * before to ensure that the tiers in the list are not yet present in the target (but it is
     * double checked here). It is also assumed that necessary tier types have been added 
     * beforehand, but it is double checked here, including some checks on the compatibility 
     * of tier types.  
     * 
     * @param srcTrans the transcription containing the source tiers
     * @param targetTrans the target transcription
     * @param newTiersNames a list of names of tiers to add 
     * @param report if not null, messages will be added to this report
     */
    public void updateWithNewTiers(TranscriptionImpl srcTrans, TranscriptionImpl targetTrans, 
    		List<String> newTiersNames, ProcessReport report) {
        if (srcTrans == null) {
        	LOG.warning("The source Transcription is null");
        	return;
        }
        if (targetTrans == null) {
        	LOG.warning("The target Transcription is null"); 
        	return;
        }
        if (newTiersNames == null || newTiersNames.isEmpty()) {
        	LOG.warning("The list of tiers to add is null or empty"); 
        	return;
        }
        
        List<TierImpl> tiersToAdd = new ArrayList<TierImpl>(newTiersNames.size());
        for (String s : newTiersNames) {
        	TierImpl tta = srcTrans.getTierWithId(s);
        	if (tta != null) {
        		tiersToAdd.add(tta);
        	}
        }
        // sort the tiers hierarchical, add the top level tiers first
        tiersToAdd = sortTiers(tiersToAdd);
        // add the tiers in this order
        for (TierImpl t : tiersToAdd) {
        	TierImpl tarTier = targetTrans.getTierWithId(t.getName());
        	
        	if (tarTier != null) {
        		if (report != null) {
        			report.append(String.format("Cannot add Tier \"%s\" to target, it is already there", 
        					t.getName()));
        		}
        		continue;
        	}
        	
        	if (t.hasParentTier()) {
        		TierImpl tarParent = targetTrans.getTierWithId(t.getParentTier().getName());
        		if (tarParent == null) {
            		if (report != null) {
            			report.append(String.format("Cannot add Tier \"%s\" to target, the target Parent Tier \"%s\" is not there", 
            					t.getName(), t.getParentTier().getName()));
            		}
        			continue;
        		}
        		LinguisticType type = targetTrans.getLinguisticTypeByName(
        				t.getLinguisticType().getLinguisticTypeName());
        		if (type == null) {
            		if (report != null) {
            			report.append(String.format("Cannot add Tier \"%s\" to target, the Tier Type \"%s\" is not there", 
            					t.getName(), t.getLinguisticType().getLinguisticTypeName()));
            		}
        			continue;
        		} else {
        			if (!parentChildTypeCompatible(tarParent.getLinguisticType(), type)) {
                		if (report != null) {
                			report.append(String.format("Cannot add Tier \"%s\" to target, the Tier Type \"%s\" "
                					+ "is not compatible with the Type of the Parent Tier", 
                					t.getName(), type.getLinguisticTypeName()));
                		}
            			continue;
        			}
        		}
        		
        		tarTier = new TierImpl(tarParent, t.getName(), t.getParticipant(), targetTrans, type);
        		tarTier.setAnnotator(t.getAnnotator());
        		tarTier.setDefaultLocale(t.getDefaultLocale());
        		tarTier.setLangRef(t.getLangRef());
        		tarTier.setExtRef(t.getExtRef());
        		
        		targetTrans.addTier(tarTier);
        	} else {// no parent, top level tier
        		LinguisticType type = targetTrans.getLinguisticTypeByName(
        				t.getLinguisticType().getLinguisticTypeName());
        		if (type == null) {
            		if (report != null) {
            			report.append(String.format("Cannot add Tier \"%s\" to target, the Tier Type \"%s\"is not there", 
            					t.getName(), t.getLinguisticType().getLinguisticTypeName()));
            		}
        			continue;
        		} else {
        			if (type.hasConstraints()) {
                		if (report != null) {
                			report.append(String.format("Cannot add Tier \"%s\" to target, the Tier Type \"%s\" "
                					+ "has constraints and cannot be used for a Tier without a Parent", 
                					t.getName(), type.getLinguisticTypeName()));
                		}
            			continue;
        			}
        		}
        		tarTier = new TierImpl(t.getName(), t.getParticipant(), targetTrans, type);
        		tarTier.setAnnotator(t.getAnnotator());
        		tarTier.setDefaultLocale(t.getDefaultLocale());
        		tarTier.setLangRef(t.getLangRef());
        		tarTier.setExtRef(t.getExtRef());
        		
        		targetTrans.addTier(tarTier);
        	}
        	// the target tier should be non null here
        	if (report != null) {
        		report.append(String.format("Added Tier \"%s\" to target", tarTier.getName()));
        	}
        }
    }
    
    /**
     * Method to update the preferences of the target transcription with part of the preferences
     * of the source transcription. 
     *  
     * @param srcTrans the source transcription
     * @param targetTrans the target transcription
     * @param prefKeys the keys of preferences to copy (e.g. specific fonts, 
     * font sizes and colors)
     */
	public void updatePreferences(TranscriptionImpl srcTrans, TranscriptionImpl targetTrans, 
			String... prefKeys ) {
        if (srcTrans == null) {
        	LOG.warning("The source Transcription is null");
        	return;
        }
        if (targetTrans == null) {
        	LOG.warning("The target Transcription is null"); 
        	return;
        }
        
        Map<String, Object> srcPrefs = Preferences.getPreferencesFor(srcTrans);
        if (srcPrefs == null || !srcPrefs.isEmpty()) {
        	LOG.info("There are no source preferences");
        	return;
        }
        
        updatePreferences(targetTrans, srcPrefs, prefKeys);
	}
	
	/**
     * Method to update the preferences of the target transcription with part of the 
     * preferences present in the specified map.
     * 
	 * @param targetTrans the target transcription
	 * @param srcPrefs the loaded preferences of a source transcription
	 * @param prefKeys the keys of preferences to copy (e.g. specific fonts, 
	 * font sizes and colors)
	 */
	public void updatePreferences(TranscriptionImpl targetTrans, Map<String, Object> srcPrefs,
			String... prefKeys ) {
        if (targetTrans == null) {
        	LOG.warning("The target Transcription is null"); 
        	return;
        }
        if (srcPrefs == null || srcPrefs.isEmpty()) {
        	LOG.info("There are no source preferences");
        	return;
        }
        
        Map<String, Object> targetPrefs = Preferences.getPreferencesFor(targetTrans);
        if (targetPrefs == null) {
        	targetPrefs = new HashMap<String, Object>();
        }
        
        if (prefKeys == null) {
        	// replace or import all
        	targetPrefs.putAll(srcPrefs);
        } else {
        	for (String s : prefKeys) {
        		targetPrefs.put(s, srcPrefs.get(s));
        	}
        }
        
        Preferences.importPreferences(targetTrans, targetPrefs);
	}
}

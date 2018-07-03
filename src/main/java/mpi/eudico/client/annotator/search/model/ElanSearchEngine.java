package mpi.eudico.client.annotator.search.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import mpi.eudico.client.annotator.search.result.model.ElanMatch;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.TimeRelation;
import mpi.search.SearchLocale;
import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.Constraint;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.content.query.model.QueryFormulationException;
import mpi.search.content.query.model.RestrictedAnchorConstraint;
import mpi.search.content.query.model.Utilities;
import mpi.search.model.SearchEngine;
import mpi.search.model.SearchListener;
import mpi.search.query.model.Query;


/**
 * The SearchEngine performs the actual search in ELAN
 *
 * @author Alexander Klassmann
 * @version Aug 2005 Identity removed
 */
public class ElanSearchEngine implements SearchEngine {
    private static final Logger logger = Logger.getLogger(ElanSearchEngine.class.getName());
    private Map<String, List<? extends Annotation>> annotationHash = new HashMap<String, List<? extends Annotation>>();
    private Map<Constraint, Pattern> patternHash = new HashMap<Constraint, Pattern>();
    private Map<Constraint, Tier[]> relationshipHash = new HashMap<Constraint, Tier[]>();
    private Map<Constraint, TierImpl> unitTierHash = new HashMap<Constraint, TierImpl>();
    private Transcription transcription;

    /**
     * constructor
     *
     * @param transcription
     */
    public ElanSearchEngine(SearchListener searchTool,
        Transcription transcription) {
        this.transcription = transcription;
        logger.setLevel(Level.ALL);
    }

    /**
     * Performs search
     *
     * @param query
     *
     * @throws PatternSyntaxException DOCUMENT ME!
     * @throws QueryFormulationException DOCUMENT ME!
     * @throws NullPointerException DOCUMENT ME!
     */
    public void executeThread(ContentQuery query)
        throws PatternSyntaxException, QueryFormulationException, 
            NullPointerException {
        //set unlimited size since search is done only within one transcription
        query.getResult().setPageSize(Integer.MAX_VALUE);
        initHashtables(query);

        AnchorConstraint anchorConstraint = query.getAnchorConstraint();

        String[] tierNames = anchorConstraint.getTierNames();

        if (tierNames[0].equals(Constraint.ALL_TIERS)) {
            tierNames = annotationHash.keySet().toArray(new String[0]);
        }

        for (String tierName : tierNames) {
        	List<? extends Annotation> anchorAnnotations = annotationHash.get(tierName);
            List<ElanMatch> anchorMatches;

            if (!(anchorConstraint instanceof RestrictedAnchorConstraint)) {
                int[] range = getAnnotationIndicesInScope(anchorAnnotations,
                        anchorConstraint.getLowerBoundary(),
                        anchorConstraint.getUpperBoundary(),
                        anchorConstraint.getUnit());

                anchorMatches = getMatches(null,
                        patternHash.get(anchorConstraint),
                        anchorConstraint.getId(), anchorAnnotations, range);
            } else {
                anchorMatches =
                		(List)	// FIXME TYPE This is an unsafe type conversion!
                		        // I am not even sure why it is supposed to be correct.
                		((RestrictedAnchorConstraint) anchorConstraint).getResult()
                                 .getMatches(tierName);
            }

            filterDependentConstraints(anchorMatches, anchorConstraint);

            for (int j = 0; j < anchorMatches.size(); j++) {
            	ElanMatch em = anchorMatches.get(j);
            	em.setFileName(((TranscriptionImpl) transcription).getPathName());
            	query.getResult().addMatch(em);
                //query.getResult().addMatch((ElanMatch) anchorMatches.get(j));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    @Override
	public void performSearch(Query query) throws Exception {
        executeThread((ContentQuery) query);
    }

    /**
     * same as getAnnotationIndicesInScope(...) with distance 0
     *
     * @param annotationList
     * @param intervalBegin
     * @param intervalEnd
     * @param timeComparisonMode
     *
     * @return int[]
     */
    private static int[] getAnnotationIndicesInScope(List<? extends Annotation> annotationList,
        long intervalBegin, long intervalEnd, String timeComparisonMode) {
        return getAnnotationIndicesInScope(annotationList, intervalBegin,
            intervalEnd, 0L, timeComparisonMode);
    }

    /**
     * returns indices of annotations that fulfill the time constraint the
     * parameter "distance" is used only for particular timeComparisonModes
     *
     * @param annotationList
     * @param intervalBegin
     * @param intervalEnd
     * @param distance
     * @param timeComparisonMode
     *
     * @return int[]
     */
    private static int[] getAnnotationIndicesInScope(List<? extends Annotation> annotationList,
        long intervalBegin, long intervalEnd, long distance,
        String timeComparisonMode) {
        int[] annotationsInInterval = new int[annotationList.size()];
        int index = 0;

        for (int i = 0; i < annotationList.size(); i++) {
            Annotation annotation = annotationList.get(i);
            boolean constraintFulfilled = false;

            if (Constraint.OVERLAP.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.overlaps(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.IS_INSIDE.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isInside(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.NO_OVERLAP.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.doesNotOverlap(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.NOT_INSIDE.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isNotInside(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.LEFT_OVERLAP.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.overlapsOnLeftSide(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.RIGHT_OVERLAP.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.overlapsOnRightSide(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.WITHIN_OVERALL_DISTANCE.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isWithinDistance(annotation,
                        intervalBegin, intervalEnd, distance);
            } else if (Constraint.WITHIN_DISTANCE_TO_LEFT_BOUNDARY.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isWithinLeftDistance(annotation,
                        intervalBegin, distance);
            } else if (Constraint.WITHIN_DISTANCE_TO_RIGHT_BOUNDARY.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isWithinRightDistance(annotation,
                        intervalEnd, distance);
            } else if (Constraint.BEFORE_LEFT_DISTANCE.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isBeforeLeftDistance(annotation,
                        intervalBegin, distance);
            } else if (Constraint.AFTER_RIGHT_DISTANCE.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isAfterRightDistance(annotation,
                        intervalEnd, distance);
            }

            if (constraintFulfilled) {
                annotationsInInterval[index++] = i;
            }
        }

        int[] range = new int[index];
        System.arraycopy(annotationsInInterval, 0, range, 0, index);

        return range;
    }

    /**
     * Returns list with the annotations (not their indices!) in constraint
     * tier within specified range
     *
     * @param lowerBoundary
     * @param upperBoundary
     * @param unitTier
     * @param unitAnnotations
     * @param relationship
     * @param centralAnnotation
     *
     * @return List<Annotation>
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    private static List<Annotation> getAnnotationsInScope(long lowerBoundary,
        long upperBoundary, TierImpl unitTier, List<? extends Annotation> unitAnnotations,
        Tier[] relationship, Annotation centralAnnotation)
        throws NullPointerException {
        List<Annotation> annotationsInScope = new ArrayList<Annotation>();
        Annotation centralUnitAnnotation = centralAnnotation;

        while ((centralUnitAnnotation.getTier() != unitTier) &&
                (centralUnitAnnotation != null)) {
            centralUnitAnnotation = centralUnitAnnotation.getParentAnnotation();
        }

        if (centralUnitAnnotation == null) {
            throw new NullPointerException();
        }

        int unitAnnotationIndex = unitAnnotations.indexOf(centralUnitAnnotation);

        int[] unitAnnotationIndicesInScope = getRangeForTier(unitTier,
                lowerBoundary, upperBoundary, unitAnnotationIndex);

        Annotation rootOfCentralAnnotation = centralUnitAnnotation;

        while (rootOfCentralAnnotation.hasParentAnnotation()) {
            rootOfCentralAnnotation = rootOfCentralAnnotation.getParentAnnotation();
        }

        logger.log(Level.FINE,
            "Unit annotation " + centralUnitAnnotation.getValue());

        Annotation unitAnnotation;

        for (int element : unitAnnotationIndicesInScope) {
            unitAnnotation = unitAnnotations.get(element);

            boolean haveSameRoot = true;

            if (unitAnnotation.hasParentAnnotation()) {
                Annotation rootOfUnitAnnotation = unitAnnotation;

                while (rootOfUnitAnnotation.hasParentAnnotation()) {
                    rootOfUnitAnnotation = rootOfUnitAnnotation.getParentAnnotation();
                }

                haveSameRoot = rootOfUnitAnnotation == rootOfCentralAnnotation;
            }

            if (haveSameRoot) {
                annotationsInScope.addAll(getDescAnnotations(unitAnnotation,
                        relationship));
            }
        }

        return annotationsInScope;
    }

    /**
     * gets all descendant annotations (e.g. children of children etc.)
     *
     * @param ancestorAnnotation
     * @param relationship
     *
     * @return List<Annotation>
     */
    private static List<Annotation> getDescAnnotations(Annotation ancestorAnnotation,
        Tier[] relationship) {
        List<Annotation> childAnnotations = new ArrayList<Annotation>();
        List<Annotation> parentAnnotations = new ArrayList<Annotation>();
        parentAnnotations.add(ancestorAnnotation);

        for (int r = relationship.length - 1; r >= 0; r--) {
            childAnnotations = new ArrayList<Annotation>();

            try {
                for (int i = 0; i < parentAnnotations.size(); i++) {
                    childAnnotations.addAll(parentAnnotations.get(
                            i).getChildrenOnTier(relationship[r]));
                }
            } catch (Exception re) {
                re.printStackTrace();

                return new ArrayList<Annotation>();
            }

            parentAnnotations = childAnnotations;
        }

        return parentAnnotations;
    }

    /**
     * get all (pattern) matches in a tier
     *
     * @param parentMatch DOCUMENT ME!
     * @param pattern
     * @param constraintId DOCUMENT ME!
     * @param annotationList
     * @param range subindices
     *
     * @return int[]
     */
    private static List<ElanMatch> getMatches(ElanMatch parentMatch, Pattern pattern,
        String constraintId, List<? extends Annotation> annotationList, int[] range) {
        List<ElanMatch> matchList = new ArrayList<ElanMatch>();

        for (int i = 0; i < range.length; i++) {
            Annotation annotation = annotationList.get(range[i]);
            Matcher matcher = pattern.matcher(annotation.getValue());

            if (matcher.find()) {
                List<int[]> substringIndices = new ArrayList<int[]>();

                do {
                    substringIndices.add(new int[] {
                            matcher.start(0), matcher.end(0)
                        });
                } while (matcher.find());

                ElanMatch match = new ElanMatch(parentMatch, annotation,
                        constraintId, range[i],
                        substringIndices.toArray(new int[0][0]));

                if (range[i] > 0) {
                    match.setLeftContext(annotationList.get(range[i] - 1));
                }

                if ((match.getIndex() + 1) < annotationList.size()) {
                    match.setRightContext(annotationList.get(range[i] + 1));
                }

                //add parent
                // TODO should it not be  if(annotation.hasParentAnnotation()){ //???
                // TODO because of range[i]; and in the next line too
                if(annotationList.get(i).hasParentAnnotation()){
                	match.setParentContext(annotationList.get(i).getParentAnnotation());
                }
                
                //add children
                // TODO should it not be  TierImpl tier=(TierImpl)annotation.getTier(); //???
                TierImpl tier=(TierImpl)annotationList.get(i).getTier();
                match.setChildrenContext(constructChildrenString(tier.getChildTiers(), annotationList.get(i)));
                
                matchList.add(match);
            }
        }

        return matchList;
    }

    /**
     * Method to construct a string with the children of a particular annotation mod. Coralie Villes
     * @param tiers the tier list of children
     * @param annotation the current annotation
     * @return String representation of annotation's children
     */
    public static String constructChildrenString(List<? extends Tier> tiers, Annotation annotation){
    	StringBuilder childrenBuffer = new StringBuilder();
    	if (!tiers.isEmpty()) {
    		for (int j = 0; j < tiers.size(); j++){
    			List<Annotation> children =annotation.getChildrenOnTier(tiers.get(j));
    			Collections.sort(children);
    			childrenBuffer.append('[');
    			for (Annotation child : children) {
        			childrenBuffer.append(child.getValue());
        			childrenBuffer.append(' ');
    			}
    			childrenBuffer.append(']');
    		}
    	}
    	return childrenBuffer.toString();
    }
    
    /**
     * computes intersection of range and [0..tier.size] returns array of the
     * integers in this intersection
     *
     * @param tier
     * @param lowerBoundary
     * @param upperBoundary
     * @param center
     *
     * @return int[]
     */
    private static int[] getRangeForTier(TierImpl tier, long lowerBoundary,
        long upperBoundary, int center) {
        int newLowerBoundary = (lowerBoundary == Long.MIN_VALUE) ? 0
                                                                 : (int) Math.max(0,
                center + lowerBoundary);
        int newUpperBoundary = (upperBoundary == Long.MAX_VALUE)
            ? (tier.getNumberOfAnnotations() - 1)
            : (int) Math.min(tier.getNumberOfAnnotations() - 1,
                center + upperBoundary);

        int[] range = new int[-newLowerBoundary + newUpperBoundary + 1];

        for (int i = 0; i < range.length; i++) {
            range[i] = i + newLowerBoundary;
        }

        return range;
    }

    /**
     * returns array of all Tiers between ancester and descendant tier,
     * including descendTier, excluding ancestorTier; empty, if ancestorTier
     * == descendTier
     *
     * @param ancesterTier
     * @param descendTier
     *
     * @return TierImpl[]
     */
    private static TierImpl[] getRelationship(TierImpl ancesterTier,
        TierImpl descendTier) {
        List<TierImpl> relationship = new ArrayList<TierImpl>();
        TierImpl parentTier = descendTier;

        try {
            if (descendTier.hasAncestor(ancesterTier)) {
                while (!ancesterTier.equals(parentTier)) {
                    relationship.add(parentTier);
                    parentTier = parentTier.getParentTier();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return relationship.toArray(new TierImpl[0]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param match
     * @param constraint
     *
     * @return DOCUMENT ME!
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    private List<ElanMatch> getChildMatches(ElanMatch match, Constraint constraint)
        throws NullPointerException {
        TierImpl unitTier = null;
        List<? extends Annotation> unitAnnotations = null;
        List<? extends Annotation> constraintAnnotations = null;
        Tier[] relShip = null;

        long lowerBoundary = constraint.getLowerBoundary();
        long upperBoundary = constraint.getUpperBoundary();
        Pattern pattern = patternHash.get(constraint);
        // HS Nov 2011: added support for multiple "child" tiers
        String[] tierNames = constraint.getTierNames();
        
        if (tierNames[0].equals(Constraint.ALL_TIERS)) {
            tierNames = annotationHash.keySet().toArray(new String[0]);
        }
        
        List<ElanMatch> allMatches = new ArrayList<ElanMatch>();
        
        for (String name : tierNames) {      
	        constraintAnnotations = annotationHash.get(name);
	
	        if (Constraint.STRUCTURAL.equals(constraint.getMode())) {
	            unitTier = unitTierHash.get(constraint);
	
	            unitAnnotations = annotationHash.get(unitTier.getName());
	
	            relShip = relationshipHash.get(constraint);
	        }
	
	        List<Annotation> annotationsInScope;
	        int[] annotationIndicesInScope;
	        Annotation annotation = match.getAnnotation();
	
	        if (Constraint.TEMPORAL.equals(constraint.getMode())) {
	            annotationIndicesInScope = getAnnotationIndicesInScope(constraintAnnotations,
	                    annotation.getBeginTimeBoundary(),
	                    annotation.getEndTimeBoundary(), upperBoundary,
	                    constraint.getUnit());
	        } else {
	            annotationsInScope = getAnnotationsInScope(lowerBoundary,
	                    upperBoundary, unitTier, unitAnnotations, relShip,
	                    annotation);
	
	            annotationIndicesInScope = new int[annotationsInScope.size()];
	
	            for (int j = 0; j < annotationsInScope.size(); j++) {
	                annotationIndicesInScope[j] = constraintAnnotations.indexOf(annotationsInScope.get(
	                            j));
	                logger.log(Level.FINE,
	                    "Constraint annotation: " +
	                    annotationsInScope.get(j).getValue());
	            }
	        }
	
	        List<ElanMatch> matches = getMatches(match, pattern, constraint.getId(),
	                constraintAnnotations, annotationIndicesInScope);
	
	        filterDependentConstraints(matches, constraint);
	        allMatches.addAll(matches);
        }
        
        return allMatches;
    }

    private void fillAnnotationHash(Constraint constraint)
        throws QueryFormulationException {
        String[] tierNames = constraint.getTierNames();
        TierImpl[] tiers;

        if (tierNames[0].equals(Constraint.ALL_TIERS)) {
            tiers = transcription.getTiers().toArray(new TierImpl[0]);
        } else {
            tiers = new TierImpl[tierNames.length];

            for (int i = 0; i < tierNames.length; i++) {
                tiers[i] = (TierImpl) transcription.getTierWithId(tierNames[i]);

                if (tiers[i] == null) {
                    throw new QueryFormulationException(SearchLocale.getString(
                            "Search.Exception.CannotFindTier") + " '" +
                        tierNames[i] + "'");
                }
            }
        }

        for (TierImpl tier : tiers) {
            annotationHash.put(tier.getName(), tier.getAnnotations());
        }

        //find unit tiers for dependent constraints
        if (Constraint.STRUCTURAL.equals(constraint.getMode())) {
            String tierName = constraint.getUnit().substring(0,
                    constraint.getUnit().lastIndexOf(' '));

            TierImpl unitTier = (TierImpl) transcription.getTierWithId(tierName);

            if (unitTier == null) {
                throw new QueryFormulationException(SearchLocale.getString(
                        "Search.Exception.CannotFindTier") + " '" + tierName +
                    "'");
            }

            unitTierHash.put(constraint, unitTier);
            relationshipHash.put(constraint, getRelationship(unitTier, tiers[0]));

            if (!annotationHash.containsKey(tierName)) {
                List<? extends Annotation> annotations = unitTier.getAnnotations();
                annotationHash.put(tierName, annotations);
            }
        }
    }

    /*
     * traverse whole tree
     */
    private void fillHashes(CorpusType type, Constraint constraint)
        throws QueryFormulationException {
        for (Enumeration<Constraint> e = constraint.children(); e.hasMoreElements();) {
            fillHashes(type, e.nextElement());
        }

        fillAnnotationHash(constraint);
        patternHash.put(constraint, Utilities.getPattern(constraint, type));
    }

    private void filterDependentConstraints(List<ElanMatch> startingMatches,
        Constraint constraint) throws NullPointerException {
        for (Enumeration<Constraint> e = constraint.children(); e.hasMoreElements();) {
            int j = 0;

            Constraint childConstraint = e.nextElement();

            while (j < startingMatches.size()) {
                ElanMatch match = startingMatches.get(j);

                List<ElanMatch> childMatches = getChildMatches(match, childConstraint);
                /*
                if (((childConstraint.getQuantifier() == Constraint.ANY) &&
                        (childMatches.size() > 0)) ||
                        ((childConstraint.getQuantifier() == Constraint.NONE) &&
                        (childMatches.size() == 0))) {
                        */
                // HS 03-2008 replaced the "==" equality test by equals because e.g. when a query
                // has been read from file the constants are not always used. All other equality 
                // tests in this class are also performed using equals.
                if (((Constraint.ANY.equals(childConstraint.getQuantifier())) &&
                        (childMatches.size() > 0)) ||
                        ((Constraint.NONE.equals(childConstraint.getQuantifier())) &&
                        (childMatches.size() == 0))) {
                    j++;
                    match.addChildren(childMatches);
                } else {
                    startingMatches.remove(j);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @throws QueryFormulationException
     * @throws PatternSyntaxException
     */
    private void initHashtables(ContentQuery query)
        throws QueryFormulationException, PatternSyntaxException {
        patternHash.clear();
        annotationHash.clear();
        unitTierHash.clear();
        relationshipHash.clear();

        fillHashes(query.getType(), query.getAnchorConstraint());
    }
}

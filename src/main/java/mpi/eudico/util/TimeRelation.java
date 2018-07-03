package mpi.eudico.util;

import mpi.eudico.server.corpora.clom.AnnotationCore;


/**
 * Possible time relations between an AnnotationCore(-interval) and an
 * externally given interval
 *
 * @author Alexander Klassmann
 * $Id: TimeRelation.java 46303 2017-11-27 13:13:23Z hasloe $
 */
public class TimeRelation {
    /**
     * Returns negative of isWithinRightDistance()
     *
     * @param annotationCore annotation
     * @param intervalEnd right boundary
     * @param distance distance
     *
     * @return boolean true if annotation lies completely after right interval boundary + distance
     */
    public static final boolean isAfterRightDistance(
        AnnotationCore annotationCore, long intervalEnd, long distance) {
        return annotationCore.getBeginTimeBoundary() > (intervalEnd + distance);
    }

    /**
     * Returns negative of isWithinLeftDistance()
     *
     * @param annotationCore annotation
     * @param intervalBegin left interval boundary
     * @param distance distance
     *
     * @return boolean true if annotation lies completely before left interval boundary - distance
     */
    public static final boolean isBeforeLeftDistance(
        AnnotationCore annotationCore, long intervalBegin, long distance) {
        return annotationCore.getEndTimeBoundary() < (intervalBegin - distance);
    }

    /**
     * annotation is inside (or matches exactly) interval
     *
     * @param annotationCore annotation
     * @param intervalBegin left interval boundary
     * @param intervalEnd right interval boundary
     *
     * @return boolean true if annotation lies inside or matches exactly interval
     */
    public static final boolean isInside(AnnotationCore annotationCore,
        long intervalBegin, long intervalEnd) {
        return (annotationCore.getBeginTimeBoundary() >= intervalBegin) &&
        (annotationCore.getEndTimeBoundary() <= intervalEnd);
    }

    /**
     * negative of isInside
     *
     * @param annotationCore annotation
     * @param intervalBegin left interval boundary
     * @param intervalEnd right interval boundary
     *
     * @return boolean true if annotation and interval have no overlap (not even a boundary)
     */
    public static final boolean isNotInside(AnnotationCore annotationCore,
        long intervalBegin, long intervalEnd) {
        return !isInside(annotationCore, intervalBegin, intervalEnd);
    }

    /**
     * Returns true if intervalBegin - distance &lt; annotationBegin AND
     * annotationEnd &lt; intervalEnd + distance
     *
     * @param annotationCore annotation
     * @param intervalBegin left interval boundary
     * @param intervalEnd right interval boundary
     * @param distance distance
     *
     * @return boolean true annotation contained in interval, enlarged by distance on either side
     */
    public static final boolean isWithinDistance(
        AnnotationCore annotationCore, long intervalBegin, long intervalEnd,
        long distance) {
        return (distance == Long.MAX_VALUE) ||
        ((annotationCore.getBeginTimeBoundary() > (intervalBegin - distance)) &&
        (annotationCore.getEndTimeBoundary() <= (intervalEnd + distance)));
    }

    /**
     * annotation is contained within the specified distance of left interval boundary
     *
     * @param annotationCore annotation
     * @param intervalBegin left interval boundary
     * @param distance distance
     *
     * @return boolean true if annotation is within specified distance of interval boundary
     */
    public static final boolean isWithinLeftDistance(
        AnnotationCore annotationCore, long intervalBegin, long distance) {
        return (distance == Long.MAX_VALUE) ||
        ((annotationCore.getBeginTimeBoundary() > (intervalBegin - distance)) &&
        (annotationCore.getEndTimeBoundary() <= (intervalBegin + distance)));
    }

    /**
     * annotation is contained within the specified distance of right interval boundary
     *
     * @param annotationCore annotation
     * @param intervalEnd right interval boundary
     * @param distance distance
     *
     * @return boolean true if annotation is within specified distance of interval boundary
     */
    public static final boolean isWithinRightDistance(
        AnnotationCore annotationCore, long intervalEnd, long distance) {
        return (distance == Long.MAX_VALUE) ||
        ((annotationCore.getBeginTimeBoundary() > (intervalEnd - distance)) &&
        (annotationCore.getEndTimeBoundary() <= (intervalEnd + distance)));
    }

    /**
     * annotation and interval have no overlap, yet may share a boundary
     *
     * @param annotationCore annotation
     * @param intervalBegin left interval boundary
     * @param intervalEnd right interval boundary
     *
     * @return boolean true, if no 'real' overlap
     */
    public static final boolean doesNotOverlap(AnnotationCore annotationCore,
        long intervalBegin, long intervalEnd) {
        return (annotationCore.getEndTimeBoundary() <= intervalBegin) ||
        (annotationCore.getBeginTimeBoundary() >= intervalEnd);
    }

    /**
     * annotation and interval have an overlap
     *
     * @param annotationCore annotation
     * @param intervalBegin left interval boundary
     * @param intervalEnd right interval boundary
     *
     * @return boolean true if intersection of annotation and interval is not empty
     */
    public static final boolean overlaps(AnnotationCore annotationCore,
        long intervalBegin, long intervalEnd) {
        return !doesNotOverlap(annotationCore, intervalBegin, intervalEnd);
    }

    /**
     * Annotation overlaps left side of interval
     *
     * @param annotationCore annotation
     * @param intervalBegin left interval boundary
     * @param intervalEnd right interval boundary
     *
     * @return boolean true if intervalBegin in annotation, intervalEnd after annotation
     */
    public static final boolean overlapsOnLeftSide(
        AnnotationCore annotationCore, long intervalBegin, long intervalEnd) {
        return (annotationCore.getBeginTimeBoundary() < intervalBegin) &&
        (intervalBegin < annotationCore.getEndTimeBoundary()) &&
        (annotationCore.getEndTimeBoundary() < intervalEnd);
    }

    /**
     * Annotation overlaps right side of interval
     *
     * @param annotationCore annotation
     * @param intervalBegin left interval boundary
     * @param intervalEnd right interval boundary
     *
     * @return boolean true if intervalEnd in annotation, intervalBegin before annotation
     */
    public static final boolean overlapsOnRightSide(
        AnnotationCore annotationCore, long intervalBegin, long intervalEnd) {
        return (intervalBegin < annotationCore.getBeginTimeBoundary()) &&
        (annotationCore.getBeginTimeBoundary() < intervalEnd) &&
        (intervalEnd < annotationCore.getEndTimeBoundary());
    }
    
    /**
     * Annotation completely surrounds or contains the interval
     * 
     * @param annotationCore the annotation
     * @param intervalBegin left interval boundary
     * @param intervalEnd right interval boundary
     * @return true if both interval begin and interval end are contained in or inside 
     * the annotation, excluding the boundaries
     */
    public static final boolean surrounds(
    	AnnotationCore annotationCore, long intervalBegin, long intervalEnd) {
    	return (annotationCore.getBeginTimeBoundary() < intervalBegin) && 
    			(annotationCore.getEndTimeBoundary() > intervalEnd);
    }
}

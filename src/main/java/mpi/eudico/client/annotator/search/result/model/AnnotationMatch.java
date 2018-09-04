package mpi.eudico.client.annotator.search.result.model;

import mpi.eudico.server.corpora.clom.Annotation;

/**
 * This interface is intended as a mix-in with classes that implement 
 * mpi.search.content.result.model.ContentMatch.
 * 
 * That interface provides left, right and parent context as String.
 * This interface extends that with providing those same contexts as Annotation,
 * if available.
 * <p>
 * If a get...ContextAnnotation() method returns a non-null value p, then the corresponding
 * get...Context() should return normally p.getValue() or its moral equivalent.
 * <p>
 * If a get...ContextAnnotation() method returns null, then get...Context() may
 * return any String.
 * <p>
 * This implies that the setter of a ...Match sets either the String or the Annotation,
 * but not both.
 * <p>
 * Typical use will first call get...ContextAnnotation() and if it returns null,
 * call get...Context().
 * 
 * @author olasei
 *
 */
public interface AnnotationMatch {
    public Annotation getParentContextAnnotation();
    
    public Annotation getLeftContextAnnotation();
    
    public Annotation getRightContextAnnotation();
}

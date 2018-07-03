package mpi.eudico.client.annotator.comments;

/**
 * A Predicate interface seems to be missing from the standard libraries.
 * From Java 1.8 on it exists as java.util.function.Predicate.
 * This is the functionality we need in ELAN.
 * 
 * @author olasei
 *
 * @param <T> the type of objects to test
 */
public interface Predicate<T> {
    public boolean test(T obj);
}

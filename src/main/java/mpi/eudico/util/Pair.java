package mpi.eudico.util;

/**
 * A class to combine two arbitrary objects, where it isn't worth the trouble
 * to use a specially named class.
 * 
 * @author olasei, after an example on stackoverflow.
 *
 * @param <A>
 * @param <B>
 */

public class Pair<A,B> {

    public static <P, Q> Pair<P, Q> makePair(P p, Q q) {
        return new Pair<P, Q>(p, q);
    }

    private final A a;
    private final B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getFirst() {
    	return a;
    }
    
    public B getSecond() {
    	return b;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((a == null) ? 0 : a.hashCode());
        result = prime * result + ((b == null) ? 0 : b.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        Pair other = (Pair) obj;
        if (a == null) {
            if (other.a != null) {
                return false;
            }
        } else if (!a.equals(other.a)) {
            return false;
        }
        if (b == null) {
            if (other.b != null) {
                return false;
            }
        } else if (!b.equals(other.b)) {
            return false;
        }
        return true;
    }

    public boolean isInstance(Class<?> classA, Class<?> classB) {
        return classA.isInstance(a) && classB.isInstance(b);
    }

    @SuppressWarnings("unchecked")
    public static <P, Q> Pair<P, Q> cast(Pair<?, ?> pair, Class<P> pClass, Class<Q> qClass) {

        if (pair.isInstance(pClass, qClass)) {
            return (Pair<P, Q>) pair;
        }

        throw new ClassCastException();

    }

    public String toString() {
    	return a.toString() + " " + b.toString();
    }
}
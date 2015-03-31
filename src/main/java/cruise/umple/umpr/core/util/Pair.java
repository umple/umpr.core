package cruise.umple.umpr.core.util;

/**
 * Simple class for working with tuples of arity two.
 *
 * @param <F> Type of first
 * @param <S> Type of second
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 */
public class Pair<F, S> {

    public final F first;
    public final S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }
    
    public static <U, V>  Pair<U, V> create(final U first, final V second) {
      return new Pair<>(first, second);
    }
}

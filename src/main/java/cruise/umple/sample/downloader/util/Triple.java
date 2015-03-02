package cruise.umple.sample.downloader.util;

/**
 * Simple class for working with Tuples of arity three.
 *
 * @param <F> Type of first
 * @param <S> Type of second
 * @param <T> Type of third
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 */
public class Triple<F, S, T> {

    public final F first;
    public final S second;
    public final T third;

    public Triple(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
    
    /**
     * Factory method to simplify specifying a Triple using type deduction.
     * @param first
     * @param second
     * @param third
     * @return new {@link Triple} instance
     * @since Feb 25, 2015
     */
    public static <F, S, T> Triple<F, S, T> newTriple(F first, S second, T third) {
      return new Triple<>(first, second, third);
    }
}

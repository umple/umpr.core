package cruise.umple.sample.downloader.util;

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
}

package cruise.umple.sample.downloader.util;

/**
 * Simple class for working with Tuples of arity three.
 *
 * @param <F> Type of first
 * @param <S> Type of second
 * @param <T> Type of third
 *
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 */
public class Triple<F, S, T> {

  public Triple(F first, S second, T third) {
    this.first  = first;
    this.second = second;
    this.third  = third;
  }

  public final F first;
  public final S second;
  public final T third;
}

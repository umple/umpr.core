/**
 * 
 */
package cruise.umple.umpr.core.entities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URL;

import cruise.umple.umpr.core.util.Networks;

/**
 * 
 *
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 *
 * @since Apr 9, 2015
 */
public class ImportAttrib {
  
  /**
   * The type of attribution, thus how the URL will work. 
   * 
   * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
   *
   * @since Apr 9, 2015
   */
  public enum Type {
    /**
     * The attribution directly links to the content, i.e. ready for download.
     */
    RAW, 
    
    /**
     * The attribution link refers to the content, use this if the content is not directly accessible or the page
     * has ads or similar to cater to. 
     */
    REFERENCE;
  }
  
  private final Type attribType;
  
  private final URL remoteLoc;
  
  /**
   * Instantiate new ImportAttrib
   * @since Apr 9, 2015
   */
  private ImportAttrib(final URL remoteLoc, final Type attribType) {
    this.remoteLoc = checkNotNull(remoteLoc);
    this.attribType = checkNotNull(attribType);
  }
  
  /**
   * Create a new {@link ImportAttrib} instance with the given URL, this attribution is {@link Type#RAW}. 
   * @param remoteUrl Remote location, must not be {@code null}. 
   * @return Non-{@code null} instance. 
   * 
   * @since Apr 9, 2015
   */
  public static ImportAttrib raw(final String remoteUrl) {
    return new ImportAttrib(Networks.newURL(remoteUrl), Type.RAW);
  }
  
  /**
   * Create a new {@link ImportAttrib} instance with the given URL, this attribution is {@link Type#RAW}.
   * @param remoteUrl Remote location, must not be {@code null}. 
   * @return Non-{@code null} instance. 
   * 
   * @since Apr 9, 2015
   */
  public static ImportAttrib raw(final URL remoteUrl) {
    return new ImportAttrib(remoteUrl, Type.RAW);
  }
  
  /**
   * Create a new {@link ImportAttrib} instance with the given URL, this attribution is a {@link Type#REFERENCE}.
   * @param remoteUrl Remote location, must not be {@code null}. 
   * @return Non-{@code null} instance. 
   * 
   * @since Apr 9, 2015
   */
  public static ImportAttrib ref(final String remoteUrl) {
    return new ImportAttrib(Networks.newURL(remoteUrl), Type.REFERENCE);
  }
  
  /**
   * Create a new {@link ImportAttrib} instance with the given URL, this attribution is a {@link Type#REFERENCE}.
   * @param remoteUrl Remote location, must not be {@code null}. 
   * @return Non-{@code null} instance. 
   * 
   * @since Apr 9, 2015
   */
  public static ImportAttrib ref(final URL remoteUrl) {
    return new ImportAttrib(remoteUrl, Type.REFERENCE);
  }
 
  /**
   * Return the attribution type
   * @return {@link Type} instance. 
   * 
   * @since Apr 9, 2015
   */
  public Type getType() {
    return attribType;
  }

  /**
   * @return the remoteLoc
   *
   * @since Apr 9, 2015
   */
  public URL getRemote() {
    return remoteLoc;
  }
  
}

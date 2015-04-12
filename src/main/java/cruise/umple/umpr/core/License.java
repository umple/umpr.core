/**
 * 
 */
package cruise.umple.umpr.core;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Hold information about the license of repository. This includes the License's {@link URL} for linking.
 *
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 *
 * @since Apr 9, 2015
 */
public enum License {
  
  CC_ATTRIBUTION_4("http://creativecommons.org/licenses/by/4.0/"),
  
  EPL("https://www.eclipse.org/legal/epl-v10.html"),
  
  W3C("http://www.w3.org/Consortium/Legal/2015/doc-license"),
  
  MIT("http://opensource.org/licenses/MIT"),
  
  /**
   * Signifies that the License is unknown. 
   */
  UNKNOWN("http://example.com/");
  
  private final URL url;
  
  License(final String url) {
    try {
      this.url = new URL(url);
    } catch (MalformedURLException e) {
      // propagate exception out
      throw new IllegalStateException(e);
    }
  }
  
  public URL url() {
    return url;
  }
  
}

package cruise.umple.sample.downloader.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Throwables;

/**
 * Utility methods for networking
 */
public abstract class Networks {
    private Networks() {
    }

    /**
     * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in
     * the 200-399 range.
     *
     * @param url     The HTTP URL to be pinged.
     * @param timeout The timeout in millis for both the connection timeout and the response read timeout. Note that
     *                the total timeout is effectively two times the given timeout.
     * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request within the
     * given timeout, otherwise <code>false</code>.
     * <p>
     * Source: http://stackoverflow.com/a/3584332
     */
    public static boolean ping(String url, int timeout) {
        // Otherwise an exception may be thrown on invalid SSL certificates:
        url = url.replaceFirst("^https", "http");

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException exception) {
            return false;
        }
    }
    
    /**
     * Supplies the content of a {@link URL} and uses HTTP caching to try to avoid redownloading the file as they tend
     * to be large. 
     * 
     * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
     * @since Mar 2, 2015
     */
    private static class URLSupplier implements Supplier<String> {
      
      private final Logger log = Logger.getLogger(URLSupplier.class.getName());
      
      private final URL url;
      
      private long timestamp = -1;
      private Optional<String> content = Optional.empty();
      
      /**
       * Creates a new instance of URLSupplier.
       * @param url
       * @since Mar 2, 2015
       */
      URLSupplier(URL url) {
        this.url = url;
      }
      
      @Override
      public String get() {
        try {
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setAllowUserInteraction(true);
            http.setRequestMethod("GET");
            
            // if we already have a content instance, we can set the If-Modified-Since header, this will stop a full 
            // download if not necessary since the response code will not be HTTP_OK
            content.ifPresent(c -> {
              http.setIfModifiedSince(timestamp);
            });
            
            http.connect();
            
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
              // we only set the content if the HTTP response code was OK. 
              
              try (InputStream in = http.getInputStream()) {
                content = Optional.of(IOUtils.toString(in));
                
                // this will not be set if the toString call fails
                timestamp = http.getLastModified();
              }
            }
            
            return content.get();
        }
        catch (IOException ioe) {
          log.warning("URLSupplier#get() failed with exception");
          log.warning(Throwables.getStackTraceAsString(ioe));
          
          throw new IllegalStateException(ioe);
        }
      }
      
    }
    
    /**
     * Creates a simple {@link Supplier} function that downloads a {@link URL} via {@link IOUtils#toString(URL)}.
     * @param url The {@link URL} to download
     * @return {@link Supplier} function for downloading a {@link URL}. 
     * 
     * @since Feb 25, 2015
     */
    public static Supplier<String> newURLDownloader(final URL url) {
      return new URLSupplier(url);
    }

}

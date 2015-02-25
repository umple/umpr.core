package cruise.umple.sample.downloader.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;

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
     * Creates a simple {@link Supplier} function that downloads a {@link URL} via {@link IOUtils#toString(URL)}.
     * @param url The {@link URL} to download
     * @return {@link Supplier} function for downloading a {@link URL}. 
     * 
     * @since Feb 25, 2015
     */
    public static Supplier<String> newURLDownloader(final URL url) {
      return () -> {
        try {
          return IOUtils.toString(url);
        } catch (IOException ioe) {
          throw new IllegalStateException(ioe);
        }
      };
    }

}

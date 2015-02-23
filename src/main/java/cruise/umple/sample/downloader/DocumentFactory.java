/**
 *
 */
package cruise.umple.sample.downloader;

import org.jsoup.nodes.Document;

import java.io.File;
import java.util.Optional;

/**
 * Functional interface for providing {@link Document} instances.
 *
 * @author Kevin Brightwell
 */
public interface DocumentFactory {

    /**
     * Loads a path or URL into a {@link Document}.
     *
     * @param path A URL or local path.
     * @return {@link Optional} with a valid value if the provider passed.
     */
    public Optional<Document> fromURL(String path);

    /**
     * Loads a path and reads from a file.
     *
     * @param file File on the file system to read
     * @return {@link Optional} with a valid value iff the path was valid.
     */
    public Optional<Document> fromFile(File file);

    /**
     * Loads a path and reads from a file. This overloaded method calls {@link #fromFile(java.io.File)} by creating a new
     * {@link java.io.File}.
     *
     * @param path Path to a file
     * @return {@link Optional} with a valid value iff the path was valid.
     */
    public default Optional<Document> fromFile(String path) {
        return this.fromFile(new File(path));
    }

}

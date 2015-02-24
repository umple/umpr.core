package cruise.umple.sample.downloader;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.inject.Inject;

/**
 * Implementation of {@link DocumentFactory}.
 */
public class RealDocumentFactory implements DocumentFactory {

    @Inject
    private Logger logger;

    @Override
    public Optional<Document> fromURL(String url) {
        try {
            return Optional.of(Jsoup.connect(url).timeout(30 * 1000).get());
        } catch (IllegalArgumentException | IOException ioe) {
            logger.warning("Could not load URL: " + url + "\nException: " + ioe.getMessage());

            return Optional.empty();
        }
    }

    @Override
    public Optional<Document> fromFile(File file) {
        if (!file.exists()) {
            logger.warning("File does not exist: " + file.getPath());
            return Optional.empty();
        }

        try {
            return Optional.of(Jsoup.parse(file, "utf-8"));
        } catch (IOException ioe) {
            logger.warning("Could not parse file: " + file.getPath() + "\nException: " + ioe.getMessage());

            return Optional.empty();
        }
    }
}

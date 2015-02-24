package cruise.umple.sample.downloader.repositories;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.inject.Inject;

import cruise.umple.sample.downloader.DocumentFactory;
import cruise.umple.sample.downloader.FileType;
import cruise.umple.sample.downloader.Repository;
import cruise.umple.sample.downloader.util.Networks;
import cruise.umple.sample.downloader.util.Pair;

/**
 * Created by kevin on 15-02-23.
 */
class AtlanZooRepository implements Repository {

    private final static String REPO_URL = "http://www.emn.fr/z-info/atlanmod/index.php/Ecore";

    private final Logger logger;
    private final DocumentFactory documentFactory;

    /**
     * Create new instances of AtlanZooRepository
     *
     * @param logger
     * @param documentFactory
     */
    @Inject
    AtlanZooRepository(Logger logger, DocumentFactory documentFactory) {
        this.logger = logger;
        this.documentFactory = documentFactory;
    }

    @Override
    public String getName() {
        return "AtlanZooEcore";
    }
    
    @Override
    public String getDescription() {
      return "The Metamodel Zoos are a collaborative open source research effort intended to produce experimental "
          + "material that may be used by all in the domain of Model Driven Engineering.\n"
          + "This Repository uses the eCore version located at: http://www.emn.fr/z-info/atlanmod/index.php/Ecore.";
    }

    @Override
    public FileType getFileType() {
        return FileType.ECORE;
    }

    @Override
    public List<Pair<Repository, URL>> getImportFiles() {
        Optional<Document> doc = documentFactory.fromURL(REPO_URL);

        if (!doc.isPresent()) {
            logger.severe("Could not load repository.");
            throw new IllegalStateException("Could not load repository.");
        }

        Elements top = doc.get().select("div#bodyContent p + ul");

        return top.stream()
                .map(e -> e.select("li a"))
                .flatMap(List::stream)
                .map(e -> {
                    try {
                        return new Pair<Repository, URL>(this, new URL(e.attr("href")));
                    } catch (MalformedURLException mue) {
                        throw new IllegalArgumentException(mue);
                    }
                }).collect(Collectors.toList());
    }

    @Override
    public boolean isAccessible() {
        return Networks.ping(REPO_URL, 30 * 1000);
    }
}

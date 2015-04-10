package cruise.umple.umpr.core.repositories;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cruise.umple.compiler.UmpleImportType;
import cruise.umple.umpr.core.DiagramType;
import cruise.umple.umpr.core.DocumentFactory;
import cruise.umple.umpr.core.ImportAttrib;
import cruise.umple.umpr.core.License;
import cruise.umple.umpr.core.Repository;
import cruise.umple.umpr.core.entities.ImportEntity;
import cruise.umple.umpr.core.entities.ImportEntityFactory;
import cruise.umple.umpr.core.util.Networks;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * {@link Repository} representation for the Zoo repository on 
 * {@link http://www.emn.fr/z-info/atlanmod/index.php/Ecore AtlanMod}. 
 */
@Singleton
class AtlanZooRepository extends SimpleRepository implements Repository {

    @Remote
    private final static String REPO_URL = "http://www.emn.fr/z-info/atlanmod/index.php/Ecore";
    
    @Name
    private final static String REPO_NAME = "AtlanZooEcore";
    
    @Description
    private final static String REPO_DESC = "The Metamodel Zoos are a collaborative open source research effort intended to produce experimental "
        + "material that may be used by all in the domain of Model Driven Engineering.\n"
        + "This Repository uses the eCore version located at: http://www.emn.fr/z-info/atlanmod/index.php/Ecore.";

    @DType
    private final static DiagramType REPO_DTYPE = DiagramType.CLASS;
    
    @CLicense
    private final static License REPO_LICENSE = License.CC_ATTRIBUTION_4;
    
    private final Logger logger;
    private final DocumentFactory documentFactory;
    private final ImportEntityFactory entityFactory;

    /**
     * Create new instances of AtlanZooRepository
     *
     * @param logger
     * @param documentFactory
     */
    @Inject
    AtlanZooRepository(Logger logger, DocumentFactory documentFactory, ImportEntityFactory entityFactory) {
      super(logger, AtlanZooRepository.class);
      
      this.logger = logger;
      this.documentFactory = documentFactory;
      this.entityFactory = entityFactory;
    }

    @Override
    public Stream<ImportEntity> getImports() {
      Optional<Document> doc = documentFactory.fromURL(REPO_URL);

      if (!doc.isPresent()) {
        logger.severe("Could not load repository.");
        throw new IllegalStateException("Could not load repository.");
      }

      Seq<Element> links = Seq.seq(doc.get().select("#bodyContent ul a.external.text"));
      Seq<Element> bases = Seq.seq(doc.get().select("#bodyContent a[name]"));
      
      Stream<Tuple2<Element, Element>> top = Seq.zip(bases, links).collect(Collectors.toList()).stream();
      
      return top.parallel().map(t -> {
        // (AttribURL, DownloadURL)
        final ImportAttrib attrib = ImportAttrib.ref(REPO_URL + "#" + t.v1().attr("name"));
        
        if (t.v2() == null) {
          throw new NullPointerException("v2 == null :: " + t.v1());
        }
        
        final URL url = Networks.newURL(t.v2().attr("href"));
        
        return entityFactory.createUrlEntity(this, Paths.get(url.getPath()), UmpleImportType.ECORE, url, 
            Optional.of(attrib));
      });
    }

    @Override
    public boolean isAccessible() {
        return Networks.ping(REPO_URL, 30 * 1000);
    }

    
}

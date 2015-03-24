/**
 * 
 */
package cruise.umple.sample.downloader.repositories;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import cruise.umple.compiler.UmpleImportType;
import cruise.umple.sample.downloader.DiagramType;
import cruise.umple.sample.downloader.DocumentFactory;
import cruise.umple.sample.downloader.Repository;
import cruise.umple.sample.downloader.entities.ImportEntity;
import cruise.umple.sample.downloader.entities.ImportEntityFactory;
import cruise.umple.sample.downloader.util.Networks;

/**
 * @author kevin
 *
 */
public class ScxmlStandardRepository implements Repository {
  private final static String REPO_URL = "http://www.w3.org/TR/scxml/";

  private final Logger logger;
  private final DocumentFactory documentFactory;
  private final ImportEntityFactory entityFactory;
  
  private static final Set<String> EXAMPLE_OUTER_SELECTORS = ImmutableSet.<String>builder()
      .add(".exampleOuter:has(a#N1182E)") // microwave-01.scxml
      .add(".exampleOuter:has(a#N1183A)") // microwave-02.scxml
      .add(".exampleOuter:has(a#N11845)") // 
      .add(".exampleOuter:has(a#N11851)") // traffic light
      .add(".exampleOuter:has(a#N11859)") // black jack
      .build();

  /**
   * Create new instances of AtlanZooRepository
   *
   * @param logger
   * @param documentFactory
   */
  @Inject
  ScxmlStandardRepository(Logger logger, DocumentFactory documentFactory, ImportEntityFactory entityFactory) {
      this.logger = logger;
      this.documentFactory = documentFactory;
      this.entityFactory = entityFactory;
  }

  @Override
  public String getName() {
      return "SCXML Standards Examples";
  }
  
  @Override
  public String getDescription() {
    return "State Chart XML (SCXML): State Machine Notation for Control Abstraction\n" + 
        "Available at: http://www.w3.org/TR/scxml/";
  }

  @Override
  public DiagramType getDiagramType() {
      return DiagramType.STATE;
  }
  
  private static class Example {
    public final Supplier<String> content;
    public final String name;
    
    Example(final String name, final Supplier<String> content) {
      this.name = name;
      this.content = content;
    }
  }

  @Override
  public List<ImportEntity> getImports() {
      Optional<Document> odoc = documentFactory.fromURL(REPO_URL);

      if (!odoc.isPresent()) {
          logger.severe("Could not load repository.");
          throw new IllegalStateException("Could not load repository.");
      }

      final Element examples = odoc.get().select(".div1:has(#Examples)").first();

      return EXAMPLE_OUTER_SELECTORS.stream()
              .map(selector -> {
                final Elements outerDiv = examples.select(selector);
                final String title = outerDiv.select(".exampleHeader").text();
                final int exampleIdx = title.indexOf("Example:");
//                final String content = outerDiv.select(".exampleInner pre").text();
                
                if (exampleIdx == -1) {
                  logger.severe("Failed to load " + selector + ", content: " + examples.toString());
                  throw new IllegalStateException("selector failed: " + selector);
                }
                
                return new Example(title.substring(exampleIdx + "Example:".length()).trim(), 
                    () -> outerDiv.select(".exampleInner pre").text());
              })
              .map(e -> {
                  return entityFactory.createStringEntity(this, Paths.get(e.name), UmpleImportType.SCXML, e.content);
              })
              .collect(Collectors.toList());
  }

  @Override
  public boolean isAccessible() {
      return Networks.ping(REPO_URL, 30 * 1000);
  }
}

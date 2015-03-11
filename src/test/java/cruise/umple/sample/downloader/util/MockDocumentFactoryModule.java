package cruise.umple.sample.downloader.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;

import cruise.umple.sample.downloader.DocumentFactory;
import cruise.umple.sample.downloader.RealDocumentFactory;
import cruise.umple.sample.downloader.consistent.ConsistentsModule;
import cruise.umple.sample.downloader.entities.EntityModule;
import cruise.umple.sample.downloader.repositories.RepositoryModule;
import cruise.umple.sample.downloader.repositories.TestRepositoryModule;

/**
 * Test module to load files locally instead of from web pages, allowing for
 * consistency
 */
public class MockDocumentFactoryModule extends AbstractModule {

  @Override
  protected void configure() {

    install(new EntityModule());
    install(new TestRepositoryModule());
    install(new ConsistentsModule());

    bind(DocumentFactory.class).to(MockDocumentFactory.class);
  }

  private static class MockDocumentFactory extends RealDocumentFactory {

    private static final Map<String, String> URL_TO_RESOURCE = (ImmutableMap.<String, String> builder()
                                                     .put("http://www.emn.fr/z-info/atlanmod/index.php/Ecore",
                                                          "repositories/Ecore-AtlanMod.html")
                                                     .build());

    @Override
    public Optional<Document> fromURL(String path) {
      if (URL_TO_RESOURCE.containsKey(path)) {
        try {
          final String content = Resources.toString(
              Resources.getResource(URL_TO_RESOURCE.get(path)),
              Charset.defaultCharset());

          return Optional.of(Jsoup.parse(content));
        } catch (IOException e) {
          return Optional.empty();
        }
      }

      return super.fromURL(path);
    }
  }
}

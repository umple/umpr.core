package cruise.umple.sample.downloader.util;

import com.google.inject.AbstractModule;

import cruise.umple.sample.downloader.DocumentFactory;
import cruise.umple.sample.downloader.RealDocumentFactory;
import cruise.umple.sample.downloader.consistent.ConsistentsModule;
import cruise.umple.sample.downloader.entities.EntityModule;
import cruise.umple.sample.downloader.repositories.TestRepositoryModule;

/**
 * Test module to load files locally instead of from web pages, allowing for
 * consistency
 */
public class MockModule extends AbstractModule {

  @Override
  protected void configure() {

    install(new EntityModule());
    install(new TestRepositoryModule());
    install(new ConsistentsModule());
    
    bind(DocumentFactory.class).to(RealDocumentFactory.class);
  }
}

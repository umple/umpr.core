package cruise.umple.umpr.core.util;

import com.google.inject.AbstractModule;

import cruise.umple.umpr.core.DocumentFactory;
import cruise.umple.umpr.core.RealDocumentFactory;
import cruise.umple.umpr.core.consistent.ConsistentsModule;
import cruise.umple.umpr.core.entities.EntityModule;
import cruise.umple.umpr.core.repositories.TestRepositoryModule;

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

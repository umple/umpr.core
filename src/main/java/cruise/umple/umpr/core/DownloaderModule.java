package cruise.umple.umpr.core;

import com.google.inject.AbstractModule;

import cruise.umple.umpr.core.consistent.ConsistentsModule;
import cruise.umple.umpr.core.entities.EntityModule;
import cruise.umple.umpr.core.repositories.RepositoryModule;

/**
 * Guice module for the Downloader application
 */
public class DownloaderModule extends AbstractModule {

  @Override
  public void configure() {
    
    install(new EntityModule());
    install(new RepositoryModule());
    install(new ConsistentsModule());

    bind(DocumentFactory.class).to(RealDocumentFactory.class);
  }

}

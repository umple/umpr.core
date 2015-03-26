package cruise.umple.sample.downloader;

import com.google.inject.AbstractModule;

import cruise.umple.sample.downloader.consistent.ConsistentsModule;
import cruise.umple.sample.downloader.entities.EntityModule;
import cruise.umple.sample.downloader.repositories.RepositoryModule;

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

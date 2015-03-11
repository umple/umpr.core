package cruise.umple.sample.downloader;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    Handler ch = new ConsoleHandler();
    Logger rootLogger = Logger.getLogger("");
    rootLogger.addHandler(ch);
    rootLogger.setLevel(Level.ALL);

    install(new EntityModule());
    install(new RepositoryModule());
    install(new ConsistentsModule());

    bind(DocumentFactory.class).to(RealDocumentFactory.class);
  }

}

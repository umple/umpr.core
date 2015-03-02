/**
 * 
 */
package cruise.umple.sample.downloader.entities;

import java.net.URL;
import java.nio.file.Path;
import java.util.logging.Logger;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import cruise.umple.sample.downloader.Repository;
import cruise.umple.sample.downloader.util.Networks;

/**
 * Simple wrapper around {@link StringEntity} that allows for downloading a URL to a string. 
 * @author kevin
 * @since Mar 2, 2015
 */
final class URLEntity implements ImportEntity {
  
  private final Logger log;
  
  private final ImportEntity wrappedEntity;
  
  @AssistedInject
  URLEntity(Logger log, ImportEntityFactory factory, 
      @Assisted Repository repository, @Assisted Path path, @Assisted URL url) {
    this.log = log;
    
    this.wrappedEntity = factory.createStringEntity(repository, path, Networks.newURLDownloader(url));
  }
  

  /* (non-Javadoc)
   * @see cruise.umple.sample.downloader.entities.ImportEntity#getRepository()
   * @since Mar 2, 2015
   */
  @Override
  public Repository getRepository() {
    return wrappedEntity.getRepository();
  }

  /* (non-Javadoc)
   * @see cruise.umple.sample.downloader.entities.ImportEntity#getPath()
   * @since Mar 2, 2015
   */
  @Override
  public Path getPath() {
    return wrappedEntity.getPath();
  }

  /* (non-Javadoc)
   * @see cruise.umple.sample.downloader.entities.ImportEntity#get()
   * @since Mar 2, 2015
   */
  @Override
  public String get() {
    return wrappedEntity.get();
  }

}

/**
 * 
 */
package cruise.umple.umpr.core.entities;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import cruise.umple.compiler.UmpleImportType;
import cruise.umple.umpr.core.ImportAttrib;
import cruise.umple.umpr.core.Repository;
import cruise.umple.umpr.core.util.Networks;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Simple wrapper around {@link StringEntity} that allows for downloading a URL to a string. 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 * @since Mar 2, 2015
 */
final class URLEntity implements ImportEntity {
  
  @SuppressWarnings("unused")
  private final Logger log;
  
  private final ImportEntity wrappedEntity;
  
  @AssistedInject
  URLEntity(Logger log, ImportEntityFactory factory,
      @Assisted UmpleImportType fileType, @Assisted Repository repository, 
      @Assisted Path path, @Assisted URL url, @Assisted Optional<ImportAttrib> attrib) {
    this.log = log;
    
    this.wrappedEntity = factory.createStringEntity(repository, path.subpath(path.getNameCount()-1, path.getNameCount()), 
        fileType, Networks.newURLDownloader(url), attrib);
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
   * @see cruise.umple.umpr.core.entities.ImportEntity#getAttributionLocation()
   */
  @Override
  public Optional<ImportAttrib> getAttribLoc() {
    return wrappedEntity.getAttribLoc();
  }

  /* (non-Javadoc)
   * @see cruise.umple.sample.downloader.entities.ImportEntity#get()
   * @since Mar 2, 2015
   */
  @Override
  public String get() {
    return wrappedEntity.get();
  }
  
  @Override
  public UmpleImportType getImportType() {
    return wrappedEntity.getImportType();
  }

}

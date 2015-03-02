/**
 * 
 */
package cruise.umple.sample.downloader.entities;

import java.net.URL;
import java.nio.file.Path;
import java.util.function.Supplier;

import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.name.Named;

import cruise.umple.sample.downloader.Repository;

/**
 * {@link AssistedInject} factory for creating {@link ImportEntity} instances.
 * 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 * @since Mar 2, 2015
 */
public interface ImportEntityFactory {

  /**
   * Creates an {@link ImportEntity} that returns a {@link String} every time. 
   * 
   * @param repository The repository this entity is linked to
   * @param path Relative path to store the entity after import
   * @param content Content to import
   * 
   * @return Non-{@code null} instance
   * 
   * @see StringEntity#StringEntity(java.util.logging.Logger, Repository, Path, String)
   * @since Mar 2, 2015
   */
  @Named("String")
  public ImportEntity createStringEntity(Repository repository, Path path, String content);
  
  /**
   * Creates an {@link ImportEntity} that returns the result of a {@link Supplier} every time. 
   * 
   * @param repository The repository this entity is linked to
   * @param path Relative path to store the entity after import
   * @param content Content to import
   * 
   * @return Non-{@code null} instance
   * 
   * @see StringEntity#StringEntity(java.util.logging.Logger, Repository, Path, java.util.function.Supplier)
   * @since Mar 2, 2015
   */
  @Named("String")
  public ImportEntity createStringEntity(Repository repository, Path path, Supplier<String> content);
  
  /**
   * Creates an {@link ImportEntity} that returns the result of downloading the {@link URL} instance.
   * 
   * @param repository The repository this entity is linked to
   * @param path Relative path to store the entity after import
   * @param url Resource to download
   * 
   * @return New non-{@code null} instance
   * 
   * @since Mar 2, 2015
   */
  @Named("URL")
  public ImportEntity createUrlEntity(Repository repository, Path path, URL url);
}

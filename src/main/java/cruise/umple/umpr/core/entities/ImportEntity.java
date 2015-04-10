/**
 * 
 */
package cruise.umple.umpr.core.entities;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

import cruise.umple.compiler.UmpleImportType;
import cruise.umple.umpr.core.Repository;
import cruise.umple.umpr.core.repositories.License;

/**
 * Entity to store information about importing. This includes the {@link Path} to store the output file, the 
 * {@link Repository} the Entity belongs to, and a method for getting an {@link InputStream} of the content. 
 * 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 * @since Mar 2, 2015
 */
public interface ImportEntity extends Supplier<String> {
  
  /**
   * Returns the repository this entity belongs to
   * @return Non-null {@link Repository} reference.
   * 
   * @since Mar 2, 2015
   */
  public abstract Repository getRepository();
  
  /**
   * The relative path that a file should be stored at within the {@link Repository}'s subdirectory.
   * @return Non-null {@link Path} instance for where an import should be stored.
   * @since Mar 2, 2015
   */
  public abstract Path getPath();
  
  /**
   * Return the file type. 
   * @return Non-{@code null} {@link String} of the file type. 
   */
  public abstract UmpleImportType getImportType();
  
  /**
   * Get the remote url location of the entity, if it exists. 
   * 
   * @return {@link Optional#empty()} if the location does not exist. Otherwise, if content is an {@link ImportAttrib}.
   * @since Apr 9, 2015
   */
  public Optional<ImportAttrib> getAttributionLocation();
  
  /**
   * Create and open an {@link InputStream} instance of the imported entity's content. It is the callers responsibility
   * to close the returned {@link InputStream}, i.e. {@link InputStream#close()}. 
   * @return {@link InputStream} that is open for reading.
   */
  @Override
  public abstract String get();
  
  /**
   * Get a {@link License} if this entity is under a <em>different</em> license than the parent repository. This has a
   * default implementation which returns {@link Optional#empty()}.
   * 
   * @return Present {@link License} iff. the {@link License} is different than the parent 
   *    {@link Repository#getLicense()}
   *    
   * @since Apr 9, 2015
   */
  public default Optional<License> getLicense() {
    return Optional.empty();
  }
}

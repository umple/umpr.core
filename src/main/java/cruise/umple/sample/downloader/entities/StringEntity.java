package cruise.umple.sample.downloader.entities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.logging.Logger;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import cruise.umple.sample.downloader.Repository;

/**
 * Convenience entity that stores a {@link Supplier} for {@link String} and accesses it quickly. 
 * 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 * @since Mar 2, 2015
 */
final class StringEntity implements ImportEntity {
  
  @SuppressWarnings("unused")
  private final Logger log;
  
  private final Supplier<String> content;
  private final Repository repository;
  private final Path path;
  
  /**
   * Creates a new instance of StringEntity.
   * @param log
   * @param repository
   * @param path
   * @param content
   * @since Mar 2, 2015
   */
  @AssistedInject
  StringEntity(Logger log, @Assisted Repository repository, @Assisted Path path, @Assisted Supplier<String> content) {
    this.log = log;
    
    // params
    this.content = checkNotNull(content);
    this.repository = checkNotNull(repository);
    this.path = checkNotNull(path);
  }
  
  /**
   * Creates a new instance of StringEntity, this is a simple wrapper around
   * {@link StringEntity#StringEntity(Repository, Path, Supplier)}. 
   * @param log
   * @param repository
   * @param path
   * @param content
   * @since Mar 2, 2015
   */
  @AssistedInject
  StringEntity(Logger log, @Assisted Repository repository, @Assisted Path path, @Assisted String content) {
    this(log, repository, path, () -> content);
  }

  @Override
  public Repository getRepository() {
    return repository;
  }

  @Override
  public Path getPath() {
    return path;
  }

  @Override
  public String get() {
    return content.get();
  }
}
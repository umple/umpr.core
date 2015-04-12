package cruise.umple.umpr.core.entities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

import cruise.umple.compiler.UmpleImportType;
import cruise.umple.umpr.core.ImportAttrib;
import cruise.umple.umpr.core.Repository;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

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
  private final UmpleImportType fileType;
  private final Optional<ImportAttrib> attrib;
  
  /**
   * Creates a new instance of StringEntity.
   * @param log
   * @param repository
   * @param path
   * @param content
   * @since Mar 2, 2015
   */
  @AssistedInject
  StringEntity(Logger log, 
      @Assisted UmpleImportType fileType, @Assisted Repository repository,
      @Assisted Path path, @Assisted Supplier<String> content, @Assisted Optional<ImportAttrib> attrib) {
    this.log = log;
    
    // params
    this.content = checkNotNull(content);
    this.repository = checkNotNull(repository);
    this.path = checkNotNull(path);
    this.fileType = checkNotNull(fileType);
    this.attrib = checkNotNull(attrib);
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
  StringEntity(Logger log, 
      @Assisted UmpleImportType fileType, @Assisted Repository repository, 
      @Assisted Path path, @Assisted String content, @Assisted Optional<ImportAttrib> attrib) {
    this.log = log;
    
    // params
    checkNotNull(content);
    this.content = () -> content;
    this.repository = checkNotNull(repository);
    this.path = checkNotNull(path);
    this.fileType = checkNotNull(fileType);
    this.attrib = checkNotNull(attrib);
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
  
  @Override
  public UmpleImportType getImportType() {
    return fileType;
  }

  /* (non-Javadoc)
   * @see cruise.umple.umpr.core.entities.ImportEntity#getAttributionLocation()
   */
  @Override
  public Optional<ImportAttrib> getAttribLoc() {
    return attrib;
  }
}
/**
 * 
 */
package cruise.umple.umpr.core.consistent;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Path;
import java.sql.Date;
import java.sql.Time;
import java.util.function.Function;
import java.util.logging.Logger;

import cruise.umple.umpr.core.DiagramType;
import cruise.umple.umpr.core.License;
import cruise.umple.umpr.core.Repository;

import com.google.common.base.Strings;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Builds a repository chain. This provides a simpler fluent-builder API that delegates to the umple models: 
 * {@link ImportRepositorySet}, {@link ImportRepository}, and {@link ImportFile}. 
 * 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 * 
 * @since 11 Mar 2015
 */
public class ConsistentsBuilder {
  
  private final Logger log;
  private final ConsistentsFactory factory;
  
  private final ImportRepositorySet repositorySet;
  
  private static final Function<Path, String> PATH_TRANSFORM = (path) -> {
    if (path == null) {
      return null;
    } 
    
    return path.toAbsolutePath().normalize().toString();
  };
  
  @AssistedInject
  ConsistentsBuilder(Logger log, ConsistentsFactory factory, 
      @Assisted("umple") final Path umplePath, 
      @Assisted("src") final Path srcPath) {  
    this.log = log;
    this.factory = factory;
    
    checkNotNull(umplePath);

    final long now = System.currentTimeMillis(); 
    this.repositorySet = new ImportRepositorySet(new Date(now), new Time(now), PATH_TRANSFORM.apply(umplePath));
    this.repositorySet.setSrcPath(PATH_TRANSFORM.apply(srcPath));
    
    log.finer("Created ConsistentsBuilder: " + this.repositorySet);
  }
  
  /**
   * Gets the {@link ImportRepositorySet} being built. 
   * 
   * @return The finished (or in-progress) {@link ImportRepositorySet} of this builder. 
   * 
   * @since 11 Mar 2015
   */
  public ImportRepositorySet getRepositorySet() {
    return this.repositorySet;
  }
  
  /**
   * Creates a new repository builder and adds the current repository information, this is used to build the underlying
   * types. 
   * 
   * @param repository The {@link Repository} base to use. 
   * @return Non-{@code null} instance of {@link ConsistentRepositoryBuilder} for building children. 
   * 
   * @since 11 Mar 2015
   * 
   * @see ConsistentRepositoryBuilder
   */
  public ConsistentRepositoryBuilder withRepository(final Repository repository) {
    checkNotNull(repository);
    
    log.finest("Adding repository: " + repository);
    
    return factory.createReposBuilder(this, repository.getName(), repository.getDiagramType(), 
        repository.getDescription(), repository.getLicense(), this.repositorySet);
  }
  
  /**
   * Creates a new repository builder and adds the current repository information, this is used to build the underlying
   * types. 
   * 
   * @param name Name of the repository, not null or empty
   * @param diagramType The type of diagrams stored
   * @param description A human-readable description of the repository
   *  
   * @return Non-{@code null} instance of {@link ConsistentRepositoryBuilder} for building children. 
   * 
   * @since 11 Mar 2015
   * 
   * @see ConsistentRepositoryBuilder
   */
  public ConsistentRepositoryBuilder withRepository(final String name, final DiagramType diagramType, 
      final String description, final License license) {
    checkArgument(!Strings.isNullOrEmpty(name), "name can not be empty or null");
    checkNotNull(description, "description can not be null");
    checkNotNull(diagramType);
    
    log.finest("Adding repository: " + name);
    
    return factory.createReposBuilder(this, name, diagramType, description, license, this.repositorySet);
  }
  
  
}

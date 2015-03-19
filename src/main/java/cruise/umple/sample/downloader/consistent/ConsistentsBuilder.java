/**
 * 
 */
package cruise.umple.sample.downloader.consistent;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Date;
import java.sql.Time;
import java.util.logging.Logger;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import cruise.umple.sample.downloader.Repository;

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
  
  @AssistedInject
  ConsistentsBuilder(Logger log, ConsistentsFactory factory, @Assisted final String rootPath) {  
    this.log = log;
    this.factory = factory;
    
    checkNotNull(rootPath);

    final long now = System.currentTimeMillis();
    this.repositorySet = new ImportRepositorySet(new Date(now), new Time(now), rootPath);
    
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
        repository.getDescription(), this.repositorySet);
  }
  
  
}

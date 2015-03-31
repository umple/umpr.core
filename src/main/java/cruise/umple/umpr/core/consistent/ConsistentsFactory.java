package cruise.umple.umpr.core.consistent;

import java.nio.file.Path;

import com.google.inject.assistedinject.Assisted;

import cruise.umple.umpr.core.DiagramType;


/**
 * Guice assisted factory for creating builders for Consistent groups. 
 * 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 *
 */
public interface ConsistentsFactory {
  
  /**
   * Creates a new instance of {@link ConsistentsBuilder}. 
   * @param rootPath Path that the factory lives at. 
   * @return New non-{@code null} instance
   */
  public ConsistentsBuilder create(@Assisted("umple") final Path umplePath, @Assisted("src") final Path srcPath);
  
  /**
   * 
   * @param importRepos
   * @return Non-{@code null} repository builder 
   */
  ConsistentRepositoryBuilder createReposBuilder(final ConsistentsBuilder bld, 
      @Assisted("name") final String name, final DiagramType diagramType, 
      @Assisted("description") final String description,
      final ImportRepositorySet repSet);
}
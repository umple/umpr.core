package cruise.umple.umpr.core.consistent;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

import cruise.umple.umpr.core.DiagramType;
import cruise.umple.umpr.core.License;

import com.google.inject.assistedinject.Assisted;


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
      @Assisted("description") final String description, final Optional<URL> remoteLoc, 
      final License license, final ImportRepositorySet repSet);
}
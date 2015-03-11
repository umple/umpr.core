package cruise.umple.sample.downloader.consistent;


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
  public ConsistentsBuilder create(String rootPath);
  
  /**
   * 
   * @param importRepos
   * @return Non-{@code null} repository builder 
   */
  ConsistentRepositoryBuilder createReposBuilder(final ConsistentsBuilder bld, 
      final String name, final ImportRepositorySet repSet);
}
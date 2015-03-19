package cruise.umple.sample.downloader.consistent;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.logging.Logger;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import cruise.umple.sample.downloader.DiagramType;
import cruise.umple.sample.downloader.ImportType;

/**
 * Builds an {@link ImportRepository} instance simply by removing some guess work. 
 * 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 * @since 11 Mar 2015
 */
public class ConsistentRepositoryBuilder {
  
  private final ImportRepository importRepos;
  
  private final Logger log;
  
  private final ConsistentsBuilder parent;
  
  @AssistedInject
  ConsistentRepositoryBuilder(Logger log, 
      @Assisted final ConsistentsBuilder parent, 
      @Assisted("name") final String name, 
      @Assisted("description") final String description,
      @Assisted final DiagramType diagramType,
      @Assisted final ImportRepositorySet repSet) {
    this.log = log;
    
    this.importRepos = new ImportRepository(checkNotNull(name), checkNotNull(description), checkNotNull(name), 
        checkNotNull(diagramType), checkNotNull(repSet));
    this.parent = checkNotNull(parent);
  }
  
  /**
   * Add a file that was successfully imported.
   * @param path Name of file, i.e. the path it will be stored at. 
   * @param fileType The file type
   * @return New not-{@code null} {@link ImportFile} instance.
   * 
   * @since 11 Mar 2015
   * 
   * @see #addFailedFile(String, String, String)
   */
  public ConsistentRepositoryBuilder addSuccessFile(final String path, final ImportType fileType) {
    log.finer("Adding successful file: path=" + path + ", type=" + fileType);
    
    new ImportFile(path, fileType, true, "", importRepos);
    
    return this;
  }
  
  /**
   * Add an unsuccessful file, the output path will likely not exist. 
   * @param path 
   * @param fileType
   * @param errorMessage
   * @return New not-{@code null} {@link ImportFile} instance. 
   * 
   * @since 11 Mar 2015
   * 
   * @see #addSuccessFile(String, String)
   */
  public ConsistentRepositoryBuilder addFailedFile(final String path, final ImportType fileType, final String errorMessage) {
    log.finer("Adding failed file: path=" + path + ", type=" + fileType + ", error=" + errorMessage);
    
    new ImportFile(path, fileType, false, errorMessage, importRepos);
    
    return this;
  }
  
  /**
   * Fluent API return to return to the parent builder. This call is provided as a convenience not a required call. 
   * @return the {@link ConsistentsBuilder} which created this instance. 
   */
  public ConsistentsBuilder build() {
    log.finest("Completed ImportRepository: " + this.importRepos);
    
    return parent;
  }
}
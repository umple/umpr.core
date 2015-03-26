package cruise.umple.sample.downloader.consistent;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import cruise.umple.compiler.UmpleImportType;
import cruise.umple.sample.downloader.DiagramType;

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
   * Given the files within the current import repository, calculate the rate of successes. 
   * 
   * <strong>Warning: This will set the success rate to {@code NaN} if no files are set.</strong>
   * @return {@code this}. 
   */
  public ConsistentRepositoryBuilder withCalculatedSuccessRate() {
    final long scount = importRepos.getFiles().stream().filter(ImportFile::isSuccessful).count();
    
    importRepos.setSuccessRate(Double.valueOf(scount) / importRepos.numberOfFiles());
    
    if (importRepos.numberOfFiles() == 0) {
      log.warning("withCalculatedSuccessRate: Set successRate to NaN");
    }
  
    return this;
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
  public ConsistentRepositoryBuilder addSuccessFile(final String path, final UmpleImportType fileType) {
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
  public ConsistentRepositoryBuilder addFailedFile(final String path, final UmpleImportType fileType, 
      final Throwable error) {
    log.finer("Adding failed file: path=" + path + ", type=" + fileType + ", error=" + error);
    
    String message = Throwables.getRootCause(error).getMessage();
    if (Strings.isNullOrEmpty(message)) {
      message = error.toString();
    }
     
    new ImportFile(path, fileType, false, message, importRepos);
    
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
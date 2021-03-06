package cruise.umple.umpr.core.consistent;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;

import cruise.umple.compiler.UmpleImportType;
import cruise.umple.umpr.core.DiagramType;
import cruise.umple.umpr.core.ImportAttrib;
import cruise.umple.umpr.core.ImportFSM;
import cruise.umple.umpr.core.License;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

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
      @Assisted final Optional<URL> remoteLoc,
      @Assisted final License license,
      @Assisted final ImportRepositorySet repSet) {
    this.log = log;
    
    this.importRepos = new ImportRepository(checkNotNull(name), checkNotNull(description), checkNotNull(name), 
        checkNotNull(diagramType), checkNotNull(remoteLoc), checkNotNull(license), checkNotNull(repSet));
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
   * Add the result of an {@link ImportFSM} to the builder.
   * @param fsm Non-{@code null} Finite state machine. 
   * @return {@code this} instance. 
   * 
   * @since Apr 10, 2015
   */
  public ConsistentRepositoryBuilder addFSM(final ImportFSM fsm) {
    checkNotNull(fsm);
    
    if (fsm.isSuccessful()) {
      addSuccessFile(fsm.getOutputPath().toString(), fsm.getImportType(), fsm.getAttribLoc());
    } else {
      addFailedFile(fsm.getOutputPath().toString(), fsm.getImportType(), fsm.getAttribLoc(), fsm.getState(), 
          fsm.getFailure().get());
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
  @SuppressWarnings("unused")
  public ConsistentRepositoryBuilder addSuccessFile(final String path, final UmpleImportType fileType, 
      final Optional<ImportAttrib> attrib) {
    log.finer("Adding successful file: path=" + path + ", type=" + fileType + ", attrib=" + attrib);
    
    new ImportFile(path, fileType, ImportFSM.State.Completed, "", attrib, importRepos);
    
    return this;
  }
  
  /**
   * Add an unsuccessful file, the output path will likely not exist. 
   * @param path 
   * @param fileType
   * @param ex Throwable with reason
   * @return New not-{@code null} {@link ImportFile} instance. 
   * 
   * @since 11 Mar 2015
   * 
   * @see #addSuccessFile(String, String)
   */
  public ConsistentRepositoryBuilder addFailedFile(final String path, final UmpleImportType fileType, 
      final Optional<ImportAttrib> attrib, final ImportFSM.State state, final Throwable ex) {
    String message = Throwables.getRootCause(ex).getMessage();
    if (Strings.isNullOrEmpty(message)) {
      message = Throwables.getStackTraceAsString(Throwables.getRootCause(ex));
      log.info("Error importing model: " + Throwables.getStackTraceAsString(ex));
    }
    
    return addFailedFile(path, fileType, attrib, state, message);
  }
  
  /**
   * Add an unsuccessful file, the output path will likely not exist. 
   * @param path 
   * @param fileType
   * @param ex Throwable with reason
   * @return New not-{@code null} {@link ImportFile} instance. 
   * 
   * @since 11 Mar 2015
   * 
   * @see #addSuccessFile(String, String)
   */
  @SuppressWarnings("unused")
  public ConsistentRepositoryBuilder addFailedFile(final String path, final UmpleImportType fileType,
      final Optional<ImportAttrib> attrib, final ImportFSM.State state, final String failMsg) {
    log.finer("Adding failed file: path=" + path + ", type=" + fileType + ", error=" + failMsg);

    new ImportFile(path, fileType, state, failMsg, attrib, importRepos);
    
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
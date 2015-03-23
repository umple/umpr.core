package cruise.umple.sample.downloader;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Throwables;

import cruise.umple.compiler.UmpleFile;
import cruise.umple.compiler.UmpleImportType;

/**
 * Stores data throughout the process
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 *
 */
public class ImportRuntimeData {
  private final Path outputFile;
  private final UmpleImportType importType;
  private Optional<String> umpleContent = Optional.empty();
  
  private Optional<UmpleFile> umpleFile = Optional.empty();
  
  private final Supplier<String> inputFunction;
  private Optional<String> inputContent = Optional.empty();
  
  private final Repository repository;
  
  // holds an exception if errors occur
  private Optional<Exception> failure = Optional.empty();
  
  /**
   * Create a new instance of Data, a simple struct
   * @param outputFolder
   * @param input
   * @param repository
   */
  ImportRuntimeData(Path outputFolder, Path inputName, final UmpleImportType fileType, Supplier<String> inputFunc, Repository repository) {        
    checkNotNull(inputName);
    this.outputFile = Paths.get(outputFolder.toAbsolutePath().toString(),
        repository.getName(), inputName.getFileName().toString() + ".ump");
    this.repository = checkNotNull(repository);
    this.inputFunction = checkNotNull(inputFunc);
    this.importType = fileType;
  }

  public Optional<String> getInputContent() {
    return inputContent;
  }

  public void setInputContent(String content) {
    this.inputContent = Optional.of(content);
  }

  public Path getOutputPath() {
    return outputFile;
  }

  public Supplier<String> getInputFunction() {
    return inputFunction;
  }

  public Repository getRepository() {
    return repository;
  }
  
  public boolean isSuccessful() {
    return !failure.isPresent();
  }

  public Optional<Exception> getFailure() {
    return failure;
  }

  public void setFailure(final Exception failure) {
    this.failure = Optional.of(failure);
  }
  
  public Optional<String> getUmpleContent() {
    return umpleContent;
  }
  
  public void setUmpleFile(UmpleFile file) {
    this.umpleFile = Optional.of(file);
  }
  
  public Optional<UmpleFile> getUmpleFile() {
    return umpleFile;
  }
  
  public void setUmpleContent(String umpleContent) {
    this.umpleContent = Optional.of(umpleContent);
  }
  
  /**
   * Get the associate file type. 
   * @return FileType string
   */
  public UmpleImportType getImportType() {
    return importType;
  }
  
  @Override
  public String toString() {
    ToStringHelper helper = MoreObjects.toStringHelper(getClass())
        .add("importType", importType)
        .add("repository", repository)
        .add("outputFile", outputFile.normalize())
        .add("successful", !failure.isPresent());
    if (failure.isPresent()) {
      helper.add("failureReason", Throwables.getRootCause(failure.get()));
    }
    
    return helper.toString();
  }

  
}
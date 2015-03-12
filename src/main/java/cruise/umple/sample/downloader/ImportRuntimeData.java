package cruise.umple.sample.downloader;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Stores data throughout the process
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 *
 */
public class ImportRuntimeData {
  private final Path outputFile;
  private final ImportType importType;
  private Optional<String> umpleContent = Optional.empty();
  
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
  ImportRuntimeData(Path outputFolder, Path inputName, final ImportType fileType, Supplier<String> inputFunc, Repository repository) {        
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

  public void setUmpleContent(String umpleContent) {
    this.umpleContent = Optional.of(umpleContent);
  }
  
  /**
   * Get the associate file type. 
   * @return FileType string
   */
  public ImportType getImportType() {
    return importType;
  }

  
}
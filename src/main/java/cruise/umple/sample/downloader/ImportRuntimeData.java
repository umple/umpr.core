package cruise.umple.sample.downloader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Stores data throughout the process
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 *
 */
class ImportRuntimeData {
  private final Path outputFile;
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
  ImportRuntimeData(Path outputFolder, Path inputName, Supplier<String> inputFunc, Repository repository) {        
    this.outputFile = Paths.get(outputFolder.toFile().getAbsolutePath(),
        repository.getName(), inputName.getFileName().toString() + ".ump");
    this.repository = repository;
    this.inputFunction = inputFunc;
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

  
}
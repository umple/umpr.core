package cruise.umple.sample.downloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import cruise.umple.compiler.EcoreImportHandler;
import cruise.umple.compiler.UmpleImportModel;
import cruise.umple.sample.downloader.util.Pair;

public class ConsoleMain {

    @Parameters
    static class Config {

        @Parameter(names={"-import", "-i"}, description = "Folder to save import files to")
        File importFileFolder = Files.createTempDir();

        @Parameter(names={"-o", "--output"}, description = "Output folder for generated .ump files", required = true)
        File outputFolder;

        @Parameter(description = "Repositories to download from, no entries implies download all.")
        List<String> respositories = Collections.emptyList();

        @Parameter(names={"-l", "--limit"}, description = "Number of imports to download in total, " +
                "no guarentees to which repositories are used")
        Integer limit = -1;

        Config() {

        }

        @Override
        public String toString() {
            return "Config{" +
                    "importFileFolder=" + importFileFolder +
                    ", outputFolder=" + outputFolder +
                    ", respositories=" + respositories +
                    ", limit=" + limit +
                    '}';
        }


    }

    private final Logger logger;
    private final Set<Repository> repositories;

    /**
     * Creates a new console main instance.
     *
     * @param logger
     * @param docFactory
     */
    @Inject
    ConsoleMain(Logger logger, Set<Repository> repositories) {
        this.logger = logger;
        this.repositories = ImmutableSet.copyOf(repositories);
    }

    public static void main(String[] args) throws IOException {
        Config cfg = new Config();
        new JCommander(cfg, args);

        System.out.println(cfg.toString());

        Injector in = Guice.createInjector(new DownloaderModule());

        ConsoleMain main = in.getInstance(ConsoleMain.class);
        main.run(cfg);
    }
    
    /**
     * Stores data throughout the process
     * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
     *
     */
    static class ImportRuntimeData {
      private final Path outputFile;
      private Optional<String> umpleContent = Optional.empty();
      
      private final URL input;
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
      ImportRuntimeData(Path outputFolder, URL input, Repository repository) {
        final Path inputPath = Paths.get(input.getPath());
        
        this.outputFile = Paths.get(outputFolder.toFile().getAbsolutePath(),
            repository.getName(), inputPath.getFileName().toString() + ".ump");
        this.repository = repository;
        this.input = input;
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

      public URL getInputUrl() {
        return input;
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
    
    public static class ImportedInfo {
      
      
    }

    /**
     * Run the main console function which produces two lists of {@link ImportRuntimeData} constructs, pre-filtered into successful
     * and unsuccessful. 
     * @param cfg Configuration data
     * @return Two lists, first list is successful data, second is unsucessful. Both lists are non-null, possibly empty
     *    and immutable. 
     * @since Feb 25, 2015
     */
    public Pair<List<ImportRuntimeData>, List<ImportRuntimeData>> run(final Config cfg) {

        cfg.outputFolder.mkdirs();
        cfg.importFileFolder.mkdirs();

        Stream<Repository> repos = repositories.stream();
        if (cfg.respositories.size() > 0) {
            Set<String> names = ImmutableSet.copyOf(cfg.respositories);

            repos = repos.filter(r -> names.contains(r.getName()));
        }

        Stream<Pair<Repository, URL>> urls = repos.filter(Repository::isAccessible)
                .peek(r -> this.logger.config("Loading Repository: " + r.getName()))
                .map(Repository::getImportFiles)
                .flatMap(List::stream);

        if (cfg.limit > -1) {
            urls = urls.limit(cfg.limit);
        }
        
        List<ImportRuntimeData> allData = urls.parallel()
            .map(pair -> {
                return new ImportRuntimeData(cfg.outputFolder.toPath(), pair.second, pair.first); 
            }).map(data -> {
                logger.finer("Downloading " + data.getInputUrl());
                try {
                    final String content = IOUtils.toString(data.getInputUrl());
                    data.setInputContent(content);
                } catch (IOException ioe) {
                    data.setFailure(ioe);
                }
                
                return data;
            })
            .map(data -> {
                data.getOutputPath().getParent().toFile().mkdir();

                data.getInputContent().ifPresent(content -> {
                  EcoreImportHandler handler = new EcoreImportHandler();
                  UmpleImportModel model;

                  logger.fine("Importing for " + data.getOutputPath());
                  try (InputStream in = IOUtils.toInputStream(content, Charsets.UTF_8)) {
                    model = handler.readDataFromXML(in);
                    if (handler.isSuccessful()) {
                      data.setUmpleContent(model.generateUmple());
                    } else {
                      data.setFailure(handler.getParseException().get());
                    }
                  
                  } catch (IOException ioe) {
                    data.setFailure(ioe);
                  }
                });

                return data;
            })
            .map(data -> {
                data.getUmpleContent().ifPresent(umple -> {
                    try {
                        FileUtils.write(new File(data.getOutputPath().toString()), umple);
                    } catch (IOException ioe) {
                        throw new IllegalStateException(ioe);
                    }
                });

                // Log any failures
                data.getFailure().ifPresent(e -> {
                  logger.fine(() -> {
                    // simple lambda supplier of the string for the stack trace
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                         PrintStream ps = new PrintStream(baos)) {
                      // write the messages to the print stream, then convert it to string and log it
                      ps.printf("Failed to parse %s:\n", data.getInputUrl());
                      e.printStackTrace(ps);
                      return baos.toString(Charsets.UTF_8.name());  
                    } catch (IOException ioe) {
                      // this should *never* happen, none of these operations are on the file system and the charset 
                      // chosen should be universally supported.
                      throw new IllegalStateException(ioe);
                    }
                  });
                });
                
                return data;
            }).collect(Collectors.toList());

        ImmutableList.Builder<ImportRuntimeData> successful = ImmutableList.builder(), 
            failure = ImmutableList.builder();
       
        allData.stream().filter(ImportRuntimeData::isSuccessful).forEach(successful::add);
        allData.stream().filter(d -> !d.isSuccessful()).forEach(failure::add);
        
        logger.info("Saved Umple files to: " + cfg.outputFolder.getPath());

        return new Pair<>(successful.build(), failure.build());
    }
}

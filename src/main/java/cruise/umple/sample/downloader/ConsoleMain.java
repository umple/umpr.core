package cruise.umple.sample.downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
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
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import cruise.umple.compiler.UmpleFile;
import cruise.umple.compiler.UmpleImportHandler;
import cruise.umple.compiler.UmpleImportHandlerFactory;
import cruise.umple.compiler.UmpleImportModel;
import cruise.umple.compiler.UmpleModel;
import cruise.umple.sample.downloader.consistent.Consistents;
import cruise.umple.sample.downloader.consistent.ImportRepositorySet;
import cruise.umple.sample.downloader.entities.ImportEntity;

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

        Config() { }

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
     * Run the main console function which produces a {@link Set} of {@link ImportRuntimeData} instances based on the
     * configuration. 
     * 
     * @param cfg Configuration data
     * @return Non-{@code null}, possibly empty {@link Set} of {@link ImportRuntimeData}. 
     * @since Feb 25, 2015
     */
    public Set<ImportRuntimeData> run(final Config cfg) {

        cfg.outputFolder.mkdirs();
        cfg.importFileFolder.mkdirs();

        Stream<Repository> repos = repositories.stream();
        if (cfg.respositories.size() > 0) {
            Set<String> names = ImmutableSet.copyOf(cfg.respositories);

            repos = repos.filter(r -> names.contains(r.getName()));
        }

        Stream<ImportEntity> urls = repos.filter(Repository::isAccessible)
                .peek(r -> this.logger.config("Loading Repository: " + r.getName()))
                .map(Repository::getImports)
                .flatMap(List::stream);

        if (cfg.limit > -1) {
            urls = urls.limit(cfg.limit);
        }
        
        // TODO Code-smell, this pipeline should be broken down
        final Set<ImportRuntimeData> allData = urls.parallel()
            .map(tr -> new ImportRuntimeData(cfg.outputFolder.toPath(), tr.getPath(), tr.getImportType(),
                                             tr, tr.getRepository()))  
            .map(data -> {
              try {
                data.setInputContent(data.getInputFunction().get());
              } catch (RuntimeException re) {
                // we do this because the input function could theoretically fail
                data.setFailure(re);
              }
                
              return data;
            })
            .map(data -> {
                data.getInputContent().ifPresent(content -> {
                  data.getOutputPath().getParent().toFile().mkdir();

                  UmpleImportHandler handler = UmpleImportHandlerFactory.create(data.getImportType());
                  
                  logger.fine("Importing for " + data.getOutputPath());
                  try (InputStream in = IOUtils.toInputStream(content, Charsets.UTF_8)) {
                    UmpleImportModel model = handler.readDataFromXML(in);
                    if (handler.isSuccessful()) {
                      data.setUmpleContent(model.generateUmple());
                    } else {
                      data.setFailure(handler.getParseException().get());
                    }
                  
                  } catch (Exception e) {
                    data.setFailure(e);
                  }
                });

                return data;
            })
            .map(data -> {
              data.getUmpleContent().ifPresent(umple -> {
                try {
                  final File file = new File(data.getOutputPath().toString());
                  FileUtils.write(file, umple);
                  
                  data.setUmpleFile(new UmpleFile(file));
                } catch (Exception e) {
                  data.setFailure(e);
                }
              });
              
              return data;
            })
            .map(data -> {
              
              data.getUmpleFile().ifPresent((ufile) -> {
                final UmpleModel model = new UmpleModel(ufile);
                model.setShouldGenerate(false);
                try {
                  model.run();
                } catch (Exception e) {
                  data.setFailure(e);
                }
              });
                
              return data;
            })
            .peek(data -> {
              // Log any failures if they exist
              data.getFailure().ifPresent(e -> {
                logger.fine("Failed to parse " + data.getInputFunction() + ":\n" + Throwables.getStackTraceAsString(e));
              });
            })
            .collect(Collectors.toSet());
        
        logger.info("Saved Umple files to: " + cfg.outputFolder.getPath());
        
        final ImportRepositorySet set = Consistents.buildImportRepositorySet(cfg.outputFolder.toPath(), 
            cfg.importFileFolder.toPath(), allData);

        final String json = Consistents.toJson(set);
        
        final Path jsonPath = cfg.outputFolder.toPath().resolve("meta.json");
        
        try (FileOutputStream fos = new FileOutputStream(jsonPath.toFile())) {
          IOUtils.write(json, fos);
          
          logger.info("Metadata written to " + jsonPath);
        } catch (IOException e) {
          logger.severe(() -> "Exception writing metadata to file.\n" + Throwables.getStackTraceAsString(e));
          throw Throwables.propagate(e);
        }
        
        return ImmutableSet.copyOf(allData);
    }
}

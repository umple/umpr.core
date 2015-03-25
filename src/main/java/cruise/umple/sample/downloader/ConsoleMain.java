package cruise.umple.sample.downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import cruise.umple.sample.downloader.consistent.Consistents;
import cruise.umple.sample.downloader.consistent.ImportRepositorySet;
import cruise.umple.sample.downloader.entities.ImportEntity;

public class ConsoleMain {

    @Parameters
    static class Config {

        @Parameter(names={"-import", "-i"}, description = "Folder to save import files to")
        File importFileFolder;

        @Parameter(names={"-o", "--output"}, description = "Output folder for generated .ump files", required = true)
        File outputFolder;

        @Parameter(description = "Repositories to download from, no entries implies download all.")
        List<String> respositories = Collections.emptyList();

        @Parameter(names={"-l", "--limit"}, description = "Number of imports to download in total, " +
                "no guarentees to which repositories are used")
        Integer limit = -1;

        Config() { 
          try {
            importFileFolder = Files.createTempDirectory(getClass().getName() + "-ImportFiles").toFile();
          } catch (IOException ioe) {
            throw Throwables.propagate(ioe);
          }
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
     * Run the main console function which produces a {@link Set} of {@link ImportRuntimeData} instances based on the
     * configuration. 
     * 
     * @param cfg Configuration data
     * @return Non-{@code null}, possibly empty {@link Set} of {@link ImportRuntimeData}. 
     * @since Feb 25, 2015
     */
    public Set<ImportFSM> run(final Config cfg) {
        
        Path workingDir;
        try {
          workingDir = java.nio.file.Files.createTempDirectory(getClass().getName() + "-working-dir-");
        } catch (IOException e) {
          throw Throwables.propagate(e);
        }
        
        logger.info("Working directory: " + workingDir.toString());
        
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
        
        final Set<ImportFSM> allData = urls.parallel() //Path aOutputPath, UmpleImportType aImportType, Supplier<String> aInputFunction, Repository aRepository
            .map(tr -> new ImportFSM(Paths.get(workingDir.toString(), tr.getRepository().getName(), tr.getPath().toString()),
                                     tr.getImportType(), tr, tr.getRepository()))
            .collect(Collectors.toSet());
        
        try {
          Files.deleteIfExists(cfg.outputFolder.toPath());
          Files.move(workingDir, cfg.outputFolder.toPath());
        } catch (IOException ioe) {
          throw Throwables.propagate(ioe);
        }
        logger.info("Saved Umple files to: " + cfg.outputFolder.getPath());
        
        final ImportRepositorySet set = Consistents.buildImportRepositorySet(cfg.outputFolder.toPath(), 
            cfg.importFileFolder.toPath(), allData);

        final String json = Consistents.toJson(set);
        
        final Path jsonPath = cfg.outputFolder.toPath().resolve("meta.json");
        
        try (FileOutputStream fos = new FileOutputStream(jsonPath.toFile())) {
          IOUtils.write(json, fos);
          
          logger.info("Metadata written to: " + jsonPath);
        } catch (IOException e) {
          logger.severe(() -> "Exception writing metadata to file.\n" + Throwables.getStackTraceAsString(e));
          throw Throwables.propagate(e);
        }
        
        return ImmutableSet.copyOf(allData);
    }
}

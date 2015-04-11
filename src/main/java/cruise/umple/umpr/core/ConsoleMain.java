package cruise.umple.umpr.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cruise.umple.umpr.core.consistent.Consistents;
import cruise.umple.umpr.core.consistent.ImportRepositorySet;
import cruise.umple.umpr.core.entities.ImportEntity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class ConsoleMain {

  @Parameters
  static class Config {

    @Parameter(names={"--import", "-i"}, description = "Folder to save import files to")
    File importFileFolder;

    @Parameter(names={"-o", "--output"}, description = "Output folder for generated .ump files", required = true)
    File outputFolder;

    @Parameter(description = "[Repository1] [.. [RepositoryN]]")
    List<String> respositories = Collections.emptyList();

    @Parameter(names={"-l", "--limit"}, description = "Number of imports to download in total, " +
            "there are no guarantees to which repositories are used or what order. (-1 implies no limit)")
    int limit = -1;
    
    @Parameter(names={"-O", "--override"}, description="Force overriding of the output folders, "
        + "i.e. remove output folder contents.")
    boolean override = false;
    
    @Parameter(names = {"-h", "-?", "--help"}, help = true, description="Print help message.")
    boolean help;

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
    JCommander jc = new JCommander(cfg, args);
    
    if (cfg.help) {
      jc.usage();
      
      return;
    }

    System.out.println(cfg.toString());

    Injector in = Guice.createInjector(new DownloaderModule());

    ConsoleMain main = in.getInstance(ConsoleMain.class);
    main.run(cfg);
  }
  
  private static final void removeDirectoryContents(final Path path) {
    final Set<Path> ignorePaths = getIgnorePaths(path);
    
    try {
      if (path.toFile().exists()) {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (dir.toFile().isHidden()) {
              return FileVisitResult.SKIP_SUBTREE;
            } 
            
            if (ignorePaths.contains(dir)) {
              return FileVisitResult.SKIP_SUBTREE;
            }
            
            return FileVisitResult.CONTINUE;
          }
          
          @Override
          public FileVisitResult visitFile(Path file,
              BasicFileAttributes attrs) throws IOException {
            if (!file.toFile().isHidden() && !ignorePaths.contains(file)) {
              Files.delete(file);
            }
            
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException e)
              throws IOException {
            if (e == null) {
              if (!dir.equals(path)) {
                Files.delete(dir);
              }
              
              return FileVisitResult.CONTINUE;
            } else {
              // directory iteration failed
              throw e;
            }
          }
        });
      }
    } catch (IOException ioe) {
      throw Throwables.propagate(ioe);
    }
  }
  
  private static void mergeDirs(final Config cfg, final Path src, final Path dest) {

    final Set<Path> ignorePaths = getIgnorePaths(dest);
    
    try {
      Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
          
          final Path outputPath = dest;
          
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (dir.toFile().isHidden()) { 
              // skip hidden directories
              return FileVisitResult.SKIP_SUBTREE;
            }
            
            final Path relative = src.relativize(dir);
            final Path fixed = outputPath.resolve(relative);
            
            if (ignorePaths.contains(fixed)) {
              return FileVisitResult.SKIP_SUBTREE;
            }
            
            final File fdir = fixed.toFile();
            
            fdir.mkdirs();
            
            return FileVisitResult.CONTINUE;
          }
          
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            final Path relative = src.relativize(file);
            final Path fixed = outputPath.resolve(relative);
            if (ignorePaths.contains(fixed)) {
              return FileVisitResult.SKIP_SUBTREE;
            }
            
            final File ffile = fixed.toFile();
            if (ffile.exists()) {
              ffile.delete();
            }
            
            Files.move(file, fixed);
            
            return FileVisitResult.CONTINUE;
          }

        });
    } catch (IOException ioe) {
      throw Throwables.propagate(ioe);
    }
  }
  
  public static final Path IGNORE_FILE = Paths.get(".umpr.core.ignore");
  
  /**
   * Gets the ignore paths from a folder, if the ignore file exists ({@value #IGNORE_FILE}). If the file exists, the 
   * {@link Set} returned has all the paths resolved against the {@code folder} parameter including 
   * {@value #IGNORE_FILE}.
   * 
   * @param folder Folder to read from
   * @return Non-{@code null} {@link ImmutableSet} including at least {{@value #IGNORE_FILE}. 
   */
  private static Set<Path> getIgnorePaths(final Path folder) {
    final Path ignPath = folder.resolve(IGNORE_FILE);
    final File ignFile = ignPath.toFile();
    
    final ImmutableSet.Builder<Path> setBld = ImmutableSet.builder();
    if (ignFile.exists()) {
      try {
        Files.readAllLines(ignPath).stream().map(str -> folder.resolve(Paths.get(str))).forEach(setBld::add);
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
    
    return setBld.add(ignPath).build();
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
      
    Path workingDir, importWorkingDir;
    try {
      workingDir = Files.createTempDirectory(getClass().getName() + "-WorkingDirectory");
      importWorkingDir = Files.createTempDirectory(getClass().getName() + "-ImportWorkingDirectory");
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    
    logger.info("Working directory: " + workingDir.toString());
    logger.info("Import working directory: " + importWorkingDir.toString());
    
    cfg.outputFolder.mkdirs();
    cfg.importFileFolder.mkdirs();

    Stream<Repository> repos = repositories.stream();
    if (cfg.respositories.size() > 0) {
        Set<String> names = ImmutableSet.copyOf(cfg.respositories);

        repos = repos.filter(r -> names.contains(r.getName()));
    }

    Stream<ImportEntity> urls = repos.filter(Repository::isAccessible)
            .peek(r -> this.logger.config("Loading Repository: " + r.getName()))
            .flatMap(Repository::getImports);

    if (cfg.limit > -1) {
        urls = urls.limit(cfg.limit);
    }
    
    final Set<ImportFSM> allData = urls.parallel() 
        .map(tr -> new ImportFSM(Paths.get(workingDir.toString(), tr.getRepository().getName(), tr.getPath().toString()),
                                 tr.getImportType(), tr, tr.getRepository(), tr.getAttribLoc()))
        .collect(Collectors.toSet());
    
    // write the import files to the import working directory iff the fetch data exists AND there is no attribution loc
    final EnumSet<ImportFSM.State> IMPORT_SUCCESS = EnumSet.complementOf(EnumSet.of(ImportFSM.State.Fetch));
    allData.stream()
      .filter(fsm -> !fsm.getAttribLoc().isPresent() && IMPORT_SUCCESS.contains(fsm.getState()))
      .forEach(fsm -> {     
        final Path rel = workingDir.relativize(fsm.getOutputPath());
        final Path imp = importWorkingDir.resolve(rel);
        try {
          FileUtils.write(imp.toFile(), fsm.getInputContent().get());
        } catch (IOException ioe) {
          throw Throwables.propagate(ioe);
        }
      });
    
    
    if (cfg.override) {
      if (cfg.outputFolder.exists()) {
        removeDirectoryContents(cfg.outputFolder.toPath());
      }
      
      if (cfg.importFileFolder.exists()) {
        removeDirectoryContents(cfg.importFileFolder.toPath());
      }
    }
    
    mergeDirs(cfg, workingDir, cfg.outputFolder.toPath());
    mergeDirs(cfg, importWorkingDir, cfg.importFileFolder.toPath());
    
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

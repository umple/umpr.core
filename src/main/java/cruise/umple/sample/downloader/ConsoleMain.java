package cruise.umple.sample.downloader;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import cruise.umple.compiler.EcoreImportHandler;
import cruise.umple.compiler.UmpleImportModel;
import cruise.umple.sample.downloader.util.Pair;
import cruise.umple.sample.downloader.util.Triple;
import org.apache.commons.io.FileUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final DocumentFactory documentFactory;
    private final Set<Repository> repositories;

    /**
     * Creates a new console main instance.
     *
     * @param logger
     * @param docFactory
     */
    @Inject
    ConsoleMain(Logger logger, DocumentFactory docFactory, Set<Repository> repositories) {
        this.logger = logger;
        this.documentFactory = docFactory;
        this.repositories = ImmutableSet.copyOf(repositories);
    }

    private Triple<Path, Optional<String>, Optional<Exception>> loadECore(Path xmi) {
        EcoreImportHandler handler = new EcoreImportHandler();
        UmpleImportModel model;

        logger.fine("Importing " + xmi);
        try {
            model = handler.readDataFromXML(xmi.toAbsolutePath().toString());
            String output = model.generateUmple();
            if (!"".equals(output)) {
                return new Triple<>(xmi, Optional.of(output), Optional.empty());
            } else {
                return new Triple<>(xmi, Optional.empty(), Optional.of(new IllegalStateException("Failed to import " + xmi)));
            }
        } catch (IOException | ParserConfigurationException e) {
            return new Triple<>(xmi, Optional.empty(), Optional.of(e));
        }
    }

    public static void main(String[] args) throws IOException {
        Config cfg = new Config();
        new JCommander(cfg, args);

        System.out.println(cfg.toString());

        Injector in = Guice.createInjector(new DownloaderModule());

        ConsoleMain main = in.getInstance(ConsoleMain.class);
        main.run(cfg);

        // actual page: http://www.emn.fr/z-info/atlanmod/index.php/Ecore
//		Document doc = Jsoup.parse(new File("lib/AtlanMod_ecore.html"), "utf-8");
    }

    public void run(final Config cfg) {

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

        urls.parallel()
            .map(pair -> {
                Path path = Paths.get(pair.second.getPath());
                Path xmiPath = Paths.get(cfg.importFileFolder.getAbsolutePath(),
                        pair.first.getName(),
                        path.getFileName().toString());

                logger.finer("Downloading " + pair.second + " -> " + xmiPath);
                try {
                    FileUtils.copyURLToFile(pair.second, xmiPath.toFile());
                } catch (IOException ioe) {
                    throw new IllegalStateException(ioe);
                }

                return xmiPath;
            })
            .map(this::loadECore)
            .forEach(t -> {
                Path xmi = t.first;
                final Path outputPath = Paths.get(cfg.outputFolder.getAbsolutePath(),
                        xmi.subpath(xmi.getNameCount() - 2, xmi.getNameCount()).toString());

                outputPath.getParent().toFile().mkdir();

                t.second.ifPresent(umple -> {
                    try (PrintWriter pw = new PrintWriter(new File(outputPath.toString() + ".ump"))) {
                        pw.write(t.second.get());
                    } catch (IOException ioe) {
                        throw new IllegalStateException(ioe);
                    }
                });

                t.third.ifPresent(e -> {
                    System.err.printf("Failed to parse %s, reason: %s", xmi, e.getMessage());
                });
            });

        System.out.println("Saved Ecore files to: " + cfg.outputFolder.getPath());

    }
}

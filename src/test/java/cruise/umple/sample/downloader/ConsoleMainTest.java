package cruise.umple.sample.downloader;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Provider;

import cruise.umple.sample.downloader.util.MockDocumentFactoryModule;

/**
 * Created by kevin on 15-02-23.
 */
@Guice(modules={DownloaderModule.class, MockDocumentFactoryModule.class})
public class ConsoleMainTest {

    @Inject
    private Set<Repository> repositorySet;

    @Inject
    private Provider<ConsoleMain> mainProvider;

    private ConsoleMain.Config cfg;
    private ConsoleMain main;

    /**
     * Creates a new {@link ConsoleMain.Config} for each method and a new {@link ConsoleMain} instance. The default 
     * directory for output is a temporary folder which will be deleted after the JVM exits.
     * 
     * @since Feb 24, 2015
     */
    @BeforeMethod
    public void beforeMethod() {
        cfg = new ConsoleMain.Config();
        cfg.limit = 5; // for speed
        cfg.outputFolder = Files.createTempDir();
        cfg.outputFolder.deleteOnExit();

        main = mainProvider.get();
    }

    /**
     * Gets a {@link Stream} of all of the created output files (inside their respective repository folders). 
     * @return {@link Stream} of {@link File} for each umple file created.
     * @since Feb 24, 2015
     */
    private Stream<File> getOutputFiles() {
        return Arrays.stream(cfg.outputFolder.listFiles())
                .map(File::listFiles)
                .flatMap(Arrays::stream);
    }

    /**
     * Test that the number of total output files created is only equal to the limit set via 
     * {@link ConsoleMain.Config#limit}.
     * 
     * @since Feb 24, 2015
     */
    @Test
    public void limitConfig() {
        cfg.limit = 5;

        main.run(cfg);

        Assert.assertEquals(getOutputFiles().count(),
                cfg.limit.longValue(),
                "Did not limit files to " + cfg.limit);
    }

    /**
     * Tests that all files are pulled if limit is not specified.
     * 
     * @since Feb 24, 2015
     */
    @Test
    public void testRepositories() {
        cfg.limit = -1; // get all of them
        main.run(cfg);

        Assert.assertEquals(getOutputFiles().count(),
                0,
                "Did not limit files to " + cfg.limit);
    }


}

package cruise.umple.umpr.core;

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

import cruise.umple.umpr.core.fixtures.MockModule;
import cruise.umple.umpr.core.fixtures.TestRepository;

/**
 * Created by kevin on 15-02-23.
 */
@Guice(modules={MockModule.class})
public class ConsoleMainTest {

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
        cfg.limit = -1; // for speed
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
        cfg.limit = 3;

        Set<?> data = main.run(cfg);
        int count = data.size();

        Assert.assertEquals(count, cfg.limit, "Did not limit files to " + cfg.limit);
    }

    /**
     * Tests that all files are pulled if limit is not specified.
     * 
     * @since Feb 24, 2015
     */
    @Test
    public void testRepositories() {
        Set<?> data = main.run(cfg);

        Assert.assertEquals(data.size(), TestRepository.ECORE_FILES_SET.size(),
                "Failed to import properly.");
    }


}

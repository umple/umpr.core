package cruise.umple.sample.downloader;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Provider;
import cruise.umple.sample.downloader.util.MockDocumentFactoryModule;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

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

    @BeforeMethod
    public void beforeMethod() {
        cfg = new ConsoleMain.Config();
        cfg.limit = 5; // for speed
        cfg.outputFolder = Files.createTempDir();

        main = mainProvider.get();
    }

    private Stream<File> getOutputFiles() {
        return Arrays.stream(cfg.outputFolder.listFiles())
                .map(File::listFiles)
                .flatMap(Arrays::stream);
    }

    @Test
    public void limitConfig() {
        cfg.limit = 5;

        main.run(cfg);

        Assert.assertEquals(getOutputFiles().count(),
                cfg.limit.longValue(),
                "Did not limit files to " + cfg.limit);
    }

    @Test
    public void testRepositories() {
        cfg.limit = -1; // get all of them
        main.run(cfg);

        Assert.assertEquals(getOutputFiles().count(),
                0,
                "Did not limit files to " + cfg.limit);
    }


}

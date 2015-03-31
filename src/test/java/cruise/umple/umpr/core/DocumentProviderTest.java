package cruise.umple.umpr.core;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.inject.Inject;

import cruise.umple.umpr.core.util.MockModule;

/**
 * Test {@link RealDocumentFactory}.
 */
@Guice(modules = MockModule.class)
public class DocumentProviderTest {

    @Inject
    private DocumentFactory docProv;

    private static final String ZOO_URL = "http://www.emn.fr/z-info/atlanmod/index.php/Ecore";
    private static File ZOO_FILE;

    /**
     * Creates a local copy of the {@link #ZOO_URL} and stores it into a temporary file to be deleted when the program
     * exits.
     * 
     * @since Feb 24, 2015
     */
    @BeforeClass
    public static void beforeClass() {
        try {
            ZOO_FILE = File.createTempFile("AtlanMod-Zoo", ".html");
            ZOO_FILE.deleteOnExit();

            FileUtils.copyURLToFile(new URL(ZOO_URL), ZOO_FILE);
        } catch (IOException ioe) {
            // fail if we can't create the file
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Tests {@link DocumentFactory#fromURL(String)}.
     * 
     * @since Feb 24, 2015
     */
    @Test
    public void testFromURL() {
        assertFalse(docProv.fromURL("Non-exist").isPresent(), "Loaded proper Document from non-existant URL");

        assertTrue(docProv.fromURL(ZOO_URL).isPresent(), "Failed to load proper Document from Zoo repository");
    }

    /**
     * Tests {@link DocumentFactory#fromFile(File)} and {@link DocumentFactory#fromFile(String)}.
     * 
     * @throws IOException
     * @since Feb 24, 2015
     */
    @Test
    public void testFromFile() throws IOException {
        assertFalse(docProv.fromFile("Non-exist").isPresent(), "Loaded proper Document from non-existant file");

        Optional<Document> odoc = docProv.fromFile(ZOO_FILE);
        assertTrue(odoc.isPresent(), "Failed to load proper Document from Zoo repository");
    }
}

package cruise.umple.umpr.core.repositories;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import cruise.umple.umpr.core.DownloaderModule;
import cruise.umple.umpr.core.Repository;
import cruise.umple.umpr.core.entities.ImportEntity;
import cruise.umple.umpr.core.util.Pair;

/**
 * This Test will test all loaded repositories to make sure that when inheriting from the {@link Repository} interface, 
 * they match the contracts outlined. This test may be slow due to it fetching the result of 
 * {@link Repository#getImports()} for every loaded {@link Repository}. 
 * 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 * 
 * @since 24 Feb 2015
 *
 */
// Intentionally using the NON-mock version, this tests the implemented repositories that they match their contracts
@Guice(modules={DownloaderModule.class})
@Test(groups={"long-runtime"})
public class RepositoriesContractsTest {
    
    @Inject
    private Set<Repository> allRepositories;
    
    @Inject
    private Logger logger;
    
    /**
     * Check all names to adhere to {@link Repository#getName()} specifications 
     * @since Feb 24, 2015
     */
    @Test
    public void checkNames() {
      for (Repository r : allRepositories) {
        assertFalse(Strings.isNullOrEmpty(r.getName()), "Repository had a null name.");
      }
    }
    
    /**
     * Check all descriptions to adhere to {@link Repository#getDescription()} specifications 
     * @since Feb 24, 2015
     */
    @Test
    public void checkDescriptions() {
      for (Repository r : allRepositories) {
        assertNotNull(r.getDescription(), "Repository had a null name.");
        assertFalse(r.getDescription().isEmpty(), "Repository had an empty name.");
      }
    }
    
    /**
     * Check all {@link FileType} to adhere to {@link Repository#getImportType()} specifications 
     * @since Feb 24, 2015
     */
    @Test
    public void checkFileType() {
      for (Repository r : allRepositories) {
        assertNotNull(r.getDiagramType(), "Repository, " + r.getName() + ", has a null DiagramType.");
      }
    }
    
    /**
     * Check all returned {@link Repository}, {@link URL} {@link Pair} instances to adhere to 
     * {@link Repository#getImports()} specifications.
     * 
     * @since Feb 24, 2015
     */
    @Test
    public void checkURLs() {
      logger.fine("Running Repository tests, these may take time depending on internet connection and repositories.");
      for (Repository r : allRepositories) {
        logger.finest("Repository: " + r.getName());
        
        List<ImportEntity> imports = r.getImports();
        assertNotNull(imports, "urls response was null");
        
        for (final ImportEntity ie : imports) {
          assertNotNull(ie, "ImportEntity was null");
          assertEquals(ie.getRepository(), r, "Import from Repository, " + r.getName() + ", do not have proper Repository set");
          assertNotNull(ie.getPath(), "Import path from Repository, " + r.getName() + ", was null.");
          assertNotNull(ie.get(), "Import InputStream from Repository, " + r.getName() + ", was null.");
          assertNotNull(ie.getImportType(), "ImportEntity::getImportType() from Repository, " + r.getName() + ", was null.");
        }
      }
    }
  
}

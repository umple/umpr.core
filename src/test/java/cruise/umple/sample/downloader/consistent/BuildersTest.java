package cruise.umple.sample.downloader.consistent;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Time;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.google.inject.Inject;

import cruise.umple.sample.downloader.ImportType;
import cruise.umple.sample.downloader.Repository;
import cruise.umple.sample.downloader.repositories.TestRepository;
import cruise.umple.sample.downloader.util.MockDocumentFactoryModule;

@Guice(modules=MockDocumentFactoryModule.class)
public class BuildersTest {
  
  private ConsistentsBuilder bld;
  private final Set<Repository> repos;
  
  @Inject
  public BuildersTest(ConsistentsFactory factory, Set<Repository> repos) {
    bld = factory.create(Paths.get("."), Files.createTempDir().toPath());
    this.repos = repos;
  }
  
  
  @SuppressWarnings("deprecation")
  @Test
  public void simpleNoRepositories() {
    final long time = System.currentTimeMillis();
    final Time now = new Time(time);
    final Date nowD = new Date(time);
    
    final ImportRepositorySet fromBld = bld.getRepositorySet();
    
    assertEquals(fromBld.getDate().getYear(), nowD.getYear());
    assertEquals(fromBld.getDate().getMonth(), nowD.getMonth());
    assertEquals(fromBld.getDate().getDay(), nowD.getDay());
    
    // THIS IS BAD, BUT UMPLE USES java.sql.Date INSTEAD OF java.util.Calendar or java.util.Duration
    assertEquals(fromBld.getTime().getHours(), now.getHours());
    assertEquals(fromBld.getTime().getMinutes(), now.getMinutes());
    
    assertEquals(fromBld.getRepositories().size(), 0);
  }
  
  @Test
  public void withRepositoryNoFiles() { 
    repos.forEach(r -> {
      ConsistentRepositoryBuilder rbld = bld.withRepository(r);
      r.getImports().forEach(e -> {
        try {
          e.get();
          rbld.addSuccessFile(e.getPath().toString(), e.getImportType());
        } catch (Exception ex) {
          rbld.addFailedFile(e.getPath().toString(), e.getImportType(), Throwables.getRootCause(ex).toString());
        }
      });
    });
    
    final ImportRepositorySet fromBld = bld.getRepositorySet();
    fromBld.getRepositories().forEach( repo -> {
      assertTrue(repo.getPath().contains(TestRepository.TEST_NAME), "Repository name is not found in path.");
      assertEquals(repo.getFiles().size(), TestRepository.ECORE_FILES.size());
      
      final AtomicInteger counter = new AtomicInteger(0);
      
      repo.getFiles().forEach(f -> {
        final String name = f.getPath();
        assertTrue(TestRepository.ECORE_FILES_SET.contains(name), "Unknown path found: " + name);
        assertEquals(f.getImportType(), ImportType.ECORE);
        
        if (f.isSuccessful()) {
          assertTrue(Strings.isNullOrEmpty(f.getMessage()), "Message was not empty when successful.");
        } else {
          assertFalse(Strings.isNullOrEmpty(f.getMessage()), "Message was empty when failed.");
          counter.incrementAndGet();
        }
      });
      
      assertEquals(counter.get(), 1, "Found multiple failed resource gathers, expected 1.");
    });
  }
}

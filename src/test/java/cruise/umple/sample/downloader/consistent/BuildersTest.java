package cruise.umple.sample.downloader.consistent;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Time;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.inject.Inject;

import cruise.umple.compiler.UmpleImportType;
import cruise.umple.sample.downloader.ImportFSM;
import cruise.umple.sample.downloader.Repository;
import cruise.umple.sample.downloader.repositories.TestRepository;
import cruise.umple.sample.downloader.util.MockModule;

@Guice(modules=MockModule.class)
public class BuildersTest {
  
  private ConsistentsBuilder bld;
  private final ConsistentsFactory factory;
  private final Set<Repository> repos;
  
  @Inject
  public BuildersTest(ConsistentsFactory factory, Set<Repository> repos) {
    this.repos = repos;
    this.factory = factory;
  }
  
  private long time;
  
  @BeforeMethod
  public void setup() {
    time = System.currentTimeMillis();
    bld = factory.create(Paths.get("."), Files.createTempDir().toPath());
  }
  
  
  @SuppressWarnings("deprecation")
  @Test
  public void simpleNoRepositories() {
    final Date nowD = new Date(time);
    final Time now = new Time(time);
    
    final ImportRepositorySet fromBld = bld.getRepositorySet();
    
    assertEquals(fromBld.getDate().getYear(), nowD.getYear());
    assertEquals(fromBld.getDate().getMonth(), nowD.getMonth());
    assertEquals(fromBld.getDate().getDay(), nowD.getDay());
    
    // THIS IS BAD, BUT UMPLE USES java.sql.Date INSTEAD OF java.util.Calendar or java.util.Duration
    assertEquals(fromBld.getTime().getHours(), now.getHours(), 
        "If this fails, re-run before raising issue -- hour may have rolled over");
    assertEquals(fromBld.getTime().getMinutes(), now.getMinutes(), 
        "If this fails, re-run before raising issue -- minute may have rolled over");
    
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
          rbld.addFailedFile(e.getPath().toString(), e.getImportType(), ImportFSM.State.Fetch, ex);
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
        assertEquals(f.getImportType(), UmpleImportType.ECORE);
        
        if (f.isSuccessful()) {
          assertTrue(Strings.isNullOrEmpty(f.getMessage()), "Message was not empty when successful.");
        } else {
          assertFalse(Strings.isNullOrEmpty(f.getMessage()), "Message was empty when failed.");
          counter.incrementAndGet();
        }
      });
      
      assertEquals(counter.get(), 1, "Found incorrect failed resource gathers.");
    });
  }
}

package cruise.umple.umpr.core.consistent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.inject.Inject;
import com.jayway.jsonassert.JsonAssert;
import com.jayway.jsonassert.JsonAsserter;

import cruise.umple.umpr.core.DiagramType;
import cruise.umple.umpr.core.ImportFSM;
import cruise.umple.umpr.core.Repository;
import cruise.umple.umpr.core.fixtures.MockModule;
import cruise.umple.umpr.core.fixtures.TestRepository;

@Guice(modules={MockModule.class})
@Test
public class ConsistentsTest {
  
  private final ConsistentsFactory factory;
  
  private ConsistentsBuilder bld;
  private final Set<Repository> repos;
  
  @Inject
  public ConsistentsTest(ConsistentsFactory factory, Set<Repository> repos) {
    this.factory = factory;
    
    this.repos = repos;
  }
  
  private Path TEST_UMP_DIR;
  @BeforeMethod
  public void setup() throws IOException {
    TEST_UMP_DIR = Files.createTempDirectory("TEST_UMP_");
    bld = factory.create(TEST_UMP_DIR, Files.createTempDirectory("TEST_SRC_"));
  }
 
  @Test
  public void toJsonNoRepositories() throws JsonParseException, JsonMappingException, IOException {
    
    final ImportRepositorySet fromBld = bld.getRepositorySet();
    final String json = Consistents.toJson(fromBld);
    
    JsonAssert.with(json)
      .assertEquals("$.date", fromBld.getDate().getTime()).and()
      .assertEquals("$.time", fromBld.getTime().getTime()).and()   
      .assertEquals("$.umple", fromBld.getUmplePath()).and() 
      .assertEquals("$.src", fromBld.getSrcPath()).and()
      .assertEquals("$.repositories", Collections.<ImportRepository>emptyList());
  }
  
  @Test
  public void toJsonImportRepository() {
    repos.forEach(bld::withRepository);
    
    final ImportRepositorySet fromBld = bld.getRepositorySet();
    final String json = Consistents.toJson(fromBld);
    
    JsonAssert.with(json)
      .assertEquals("$.repositories[0].path", TestRepository.TEST_NAME).and()
      .assertEquals("$.repositories[0].description", TestRepository.DESCRIPTION).and()
      .assertEquals("$.repositories[0].diagramType", DiagramType.CLASS.getType()).and()
      .assertEquals("$.repositories[0].successRate", 1.0).and()
      .assertEquals("$.repositories[0].failRate", 0.0).and()
      .assertEquals("$.repositories[0].files", Collections.<ImportFile>emptyList()); 
  }
  
  @Test
  public void toJsonImportFile() {
    repos.forEach(r -> {
      final ConsistentRepositoryBuilder rbld = bld.withRepository(r);
      
      r.getImports().forEach(e -> {
        final ImportFSM fsm = new ImportFSM(Paths.get(TEST_UMP_DIR.toString(), r.getName(), e.getPath().toString()), 
            e.getImportType(), e, e.getRepository());
        
        if (fsm.isSuccessful()) {
          rbld.addSuccessFile(fsm.getOutputPath().toString(), fsm.getImportType());
        } else {
          rbld.addFailedFile(e.getPath().toString(), e.getImportType(), fsm.getState(), fsm.getFailure().get());
        }
      });
      
      rbld.withCalculatedSuccessRate();
    }); 
    
    final ImportRepositorySet fromBld = bld.getRepositorySet();
    final String json = Consistents.toJson(fromBld);
    
    final String START = "$.repositories[0].files[%d]";
    final JsonAsserter jassert = JsonAssert.with(json);
    fromBld.getRepositories().forEach(r -> {
      
      for (int i = 0; i < r.getFiles().size(); ++i) {
        final ImportFile file = r.getFile(i);
        final String path = String.format(START, i);
        jassert.assertEquals(path + ".path", file.getPath()).and()
          .assertEquals(path + ".type", file.getImportType().getName()).and()
          .assertEquals(path + ".successful", file.isSuccessful()).and()
          .assertEquals(path + ".lastState", file.getLastState().toString());
        
        if (file.isSuccessful()) {
          jassert.assertNotDefined(path + ".message");
        } else {
          jassert.assertEquals(path + ".message", file.getMessage());
        }   
      }
    });
    
  }
  
}

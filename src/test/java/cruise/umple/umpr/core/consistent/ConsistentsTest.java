package cruise.umple.umpr.core.consistent;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

import cruise.umple.umpr.core.ImportFSM;
import cruise.umple.umpr.core.Repository;
import cruise.umple.umpr.core.fixtures.MockModule;
import cruise.umple.umpr.core.repositories.TestRepository;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.codepoetics.protonpack.StreamUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.inject.Inject;
import com.jayway.jsonassert.JsonAssert;
import com.jayway.jsonassert.JsonAsserter;

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
      .assertEquals("$.repositories[0].diagramType", TestRepository.REPO_DTYPE.getType()).and()
      .assertEquals("$.repositories[0].license", TestRepository.REPO_LICENSE.toString()).and()
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
            e.getImportType(), e, e.getRepository(), e.getAttribLoc());
        
        if (fsm.isSuccessful()) {
          rbld.addSuccessFile(fsm.getOutputPath().toString(), fsm.getImportType(), fsm.getAttribLoc());
        } else {
          rbld.addFailedFile(e.getPath().toString(), e.getImportType(), fsm.getAttribLoc(), fsm.getState(), fsm.getFailure().get());
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
        
        if (file.getAttrib().isPresent()) {
          jassert.assertEquals(path + ".attrib.url", file.getAttrib().get().getRemoteLoc().toString());
          jassert.assertEquals(path + ".attrib.type", file.getAttrib().get().getAttribType().toString());
        } else {
          jassert.assertNotDefined(path + ".attrib");
        }
      }
    });
    
  }
  
  public void deserialize() {
    repos.forEach(r -> {
      final ConsistentRepositoryBuilder rbld = bld.withRepository(r);
      
      r.getImports().map(e -> new ImportFSM(Paths.get(TEST_UMP_DIR.toString(), r.getName(), e.getPath().toString()), 
            e.getImportType(), e, e.getRepository(), e.getAttribLoc())).forEach(rbld::addFSM);
      
      rbld.withCalculatedSuccessRate();
    }); 
    
    final ImportRepositorySet fromBld = bld.getRepositorySet();
    final String json = Consistents.toJson(fromBld);
    
    final ImportRepositorySet fromJson = Consistents.fromJson(json);
    
    assertEquals(fromJson.getSrcPath(), fromBld.getSrcPath());
    assertEquals(fromJson.getUmplePath(), fromBld.getUmplePath());
    
    StreamUtils.zip(fromJson.getRepositories().stream(), fromBld.getRepositories().stream(), 
        (actual, expected) -> {
          assertEquals(actual.getPath(), expected.getPath());
          assertEquals(actual.getDescription(), expected.getDescription());
          assertEquals(actual.getDiagramType(), expected.getDiagramType());
          assertEquals(actual.getSuccessRate(), expected.getSuccessRate());
          assertEquals(actual.getFailRate(), expected.getFailRate());
          assertEquals(actual.getLicense(), expected.getLicense());
          
          StreamUtils.zip(actual.getFiles().stream(), expected.getFiles().stream(), 
              (afile, efile) -> {
                assertEquals(afile.getImportRepository().getName(), actual.getName()); // names are unique
                
                // check file internals
                assertEquals(afile.getImportType(), efile.getImportType());
                assertEquals(afile.getLastState(), efile.getLastState());
                assertEquals(afile.getMessage(), efile.getMessage());
                assertEquals(afile.isSuccessful(), efile.isSuccessful());
                assertEquals(afile.getAttrib(), efile.getAttrib());
                
                return true;
              });
          
          return true;
        });
  }
  
}

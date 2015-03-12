package cruise.umple.sample.downloader.consistent;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.SkipException;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import cruise.umple.sample.downloader.ImportType;
import cruise.umple.sample.downloader.Repository;
import cruise.umple.sample.downloader.util.MockDocumentFactoryModule;

@Guice(modules={MockDocumentFactoryModule.class}) @SuppressWarnings("unchecked")
@Test
public class ConsistentsTest {
  
  private final ConsistentsFactory factory;
  
  private ObjectMapper mapper = new ObjectMapper();
  
  private ConsistentsBuilder bld;
  private final Set<Repository> repos;
  
  @Inject
  public ConsistentsTest(ConsistentsFactory factory, Set<Repository> repos) {
    this.factory = factory;
    
    bld = factory.create(".");
    this.repos = repos;
  }
  
 
  @Test
  public void toJsonNoRepositories() throws JsonParseException, JsonMappingException, IOException {
    throw new SkipException("Skipping, working later");
    
//    final ImportRepositorySet fromBld = bld.getRepositorySet();
//    final String json = Consistents.toJson(fromBld);
//    Map<String, Object> fromJson = mapper.readValue(json, new TypeReference<Map<String, Object>>() { });
//    
//    assertEquals(fromJson.get("date"), fromBld.getDate().toString());
//    assertEquals(fromJson.get("time"), fromBld.getTime().toString());
//    assertEquals((String)fromJson.get("rootPath"), fromBld.getRootPath());
//    assertEquals(((List<Object>)fromJson.get("repositories")).size(), fromBld.numberOfRepositories());
  }
  
  @Test
  public void toJsonImportRepository() {
    throw new SkipException("Skipping, working later");    
    
  }
  
}

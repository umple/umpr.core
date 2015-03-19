/**
 * 
 */
package cruise.umple.sample.downloader.repositories;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import cruise.umple.sample.downloader.DiagramType;
import cruise.umple.sample.downloader.ImportType;
import cruise.umple.sample.downloader.Repository;
import cruise.umple.sample.downloader.entities.ImportEntity;
import cruise.umple.sample.downloader.entities.ImportEntityFactory;


/**
 * Test repository that will respond immediately without lag. 
 * 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 *
 * @since 11 Mar 2015
 */
@Singleton
public class TestRepository implements Repository {
  
  public static final String TEST_NAME = "TestRespository-ECore";
  
  public static final String DESCRIPTION = "Fast and small ECore repository.";
  
  public final List<ImportEntity> entities;
  
  public final ImportEntityFactory factory;
  
  public static final List<String> ECORE_FILES = ImmutableList.<String>builder()
      .add("bibtex.ecore")
      .add("ocl-operations.ecore")
      .add("sharengo.ecore")
      .add("intentional-failure.ecore")
      .add("adelfe.model-failure.ecore")
      .build();
  
  public static final Set<String> ECORE_FILES_SET = ImmutableSet.copyOf(ECORE_FILES);
  public static final Map<String, Supplier<String> > ECORE_MAP;
  public static final Map<String, String> ECORE_CONTENT;
  
  static {
    
    Map<String, Supplier<String>> content = Maps.asMap(Sets.newHashSet(ECORE_FILES), path -> 
      () -> {
          try {
            URL url = Resources.getResource("repositories/" + path);
            
            return Resources.toString(url, Charsets.UTF_8);
          } catch (IllegalArgumentException | IOException e) {
            throw new IllegalStateException("RESOURCE_FAILURE -- \n"
                + "If multiple failures occur it is likely due to resources not on the classpath.", e);
          }
        });
    
    ECORE_MAP = ImmutableMap.copyOf(content);
    
    ECORE_CONTENT = Maps.transformEntries(ECORE_MAP, (key, val) -> {
      try {
        return val.get();
      } catch (IllegalStateException ise) {
        return Throwables.getStackTraceAsString(ise);
      }
    });
  }
  
  @Inject
  TestRepository(final ImportEntityFactory factory) {
    this.factory = factory;
    ImmutableList.Builder<ImportEntity> bld = ImmutableList.builder();
    
    ECORE_MAP.entrySet().forEach(entry -> {
      bld.add(factory.createStringEntity(this, Paths.get(entry.getKey()), ImportType.ECORE, entry.getValue()));
    });
    
    entities = bld.build();
  }
  
  /* (non-Javadoc)
   * @see cruise.umple.sample.downloader.Repository#getName()
   */
  @Override
  public String getName() {
    return TEST_NAME;
  }

  /* (non-Javadoc)
   * @see cruise.umple.sample.downloader.Repository#getDescription()
   */
  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  /* (non-Javadoc)
   * @see cruise.umple.sample.downloader.Repository#getFileType()
   */
  @Override
  public DiagramType getDiagramType() {
    return DiagramType.CLASS;
  }

  /* (non-Javadoc)
   * @see cruise.umple.sample.downloader.Repository#getImports()
   */
  @Override
  public List<ImportEntity> getImports() {
    return entities;
  }

  /* (non-Javadoc)
   * @see cruise.umple.sample.downloader.Repository#isAccessible()
   */
  @Override
  public boolean isAccessible() {
    return true;
  }
  
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).toString();
  }

}

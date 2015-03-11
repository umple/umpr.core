/**
 * 
 */
package cruise.umple.sample.downloader.repositories;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.inject.Inject;

import cruise.umple.sample.downloader.FileType;
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
public class TestRepository implements Repository {
  
  public static final String TEST_NAME = "Test Respository - ECore";
  
  public static final String DESCRIPTION = "Fast and small ECore repository.";
  
  public final List<ImportEntity> entities;
  
  public final ImportEntityFactory factory;
  
  public static final List<String> ECORE_FILES = ImmutableList.<String>builder()
      .add("bibtex.ecore")
      .add("ocl-operations.ecore")
      .add("sharengo.ecore")
      .build();
  
  public static final Map<String, String> ECORE_MAP;
  
  static {
    
    Map<String, String> content = Maps.asMap(Sets.newHashSet(ECORE_FILES), path -> {
        URL url = Resources.getResource("repositories/" + path);
        try {
          return Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
          throw new IllegalStateException("Likely due to resources not on the classpath.", e);
        }
      });
    
    ECORE_MAP = ImmutableMap.copyOf(content);
  }
  
  @Inject
  TestRepository(final ImportEntityFactory factory) {
    this.factory = factory;
    ImmutableList.Builder<ImportEntity> bld = ImmutableList.builder();
    
    ECORE_MAP.entrySet().forEach(entry -> {
      bld.add(factory.createStringEntity(this, Paths.get(entry.getKey()), FileType.ECORE, entry.getValue()));
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
  public FileType getFileType() {
    return FileType.ECORE;
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

}

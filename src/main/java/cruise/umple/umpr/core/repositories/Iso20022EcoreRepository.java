/**
 * 
 */
package cruise.umple.umpr.core.repositories;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.base.Throwables;
import com.google.inject.Inject;

import cruise.umple.compiler.UmpleImportType;
import cruise.umple.umpr.core.DiagramType;
import cruise.umple.umpr.core.Repository;
import cruise.umple.umpr.core.entities.ImportEntity;
import cruise.umple.umpr.core.entities.ImportEntityFactory;
import cruise.umple.umpr.core.util.Networks;

/**
 * Simple, one-file repository used in the Umpr repository tutorial, the model is still useful so the code is left here.
 * 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 * @since 31 Mar 2015
 * 
 * @see https://code.google.com/p/umple/wiki/UmprCoreRepositories
 * 
 */
class Iso20022EcoreRepository implements Repository {
  
  private static final String GIST_URL = "https://gist.githubusercontent.com/Nava2/4ca3335224d51c185c0b/" + 
                                         "raw/87067f5d6889d9efa6032de25331fde6d1d0f88c/iso20022.ecore";
  
  //Creates ImportEntity instances
  private final ImportEntityFactory factory;
 
  @Inject
  Iso20022EcoreRepository(ImportEntityFactory importEntityFactory) {
    this.factory = importEntityFactory;
  }
  

  /* (non-Javadoc)
   * @see cruise.umple.umpr.core.Repository#getName()
   */
  @Override
  public String getName() {
    return "ISO20022";
  }

  /* (non-Javadoc)
   * @see cruise.umple.umpr.core.Repository#getDescription()
   */
  @Override
  public String getDescription() {
    return "ISO20022 ECore model from http://www.iso20022.org/e_dictionary.page, "
        + "stored statically at: " + GIST_URL;
  }

  /* (non-Javadoc)
   * @see cruise.umple.umpr.core.Repository#getDiagramType()
   */
  @Override
  public DiagramType getDiagramType() {
    return DiagramType.CLASS;
  }

  /* (non-Javadoc)
   * @see cruise.umple.umpr.core.Repository#getImports()
   */
  @Override
  public Stream<ImportEntity> getImports() {
    try {
      final URL url = new URL(GIST_URL);
      
      final ImportEntity entity = factory.createUrlEntity(this, Paths.get(url.getPath()), 
          UmpleImportType.ECORE, url);
      
      // create a list
      final List<ImportEntity> out = new ArrayList<>();
      // add the import entity
      out.add(entity);
      
      // return a stream against the list
      return out.stream();
    } catch (MalformedURLException mue) {
      throw Throwables.propagate(mue);
    }
  }

  /* (non-Javadoc)
   * @see cruise.umple.umpr.core.Repository#isAccessible()
   */
  @Override
  public boolean isAccessible() {
    return Networks.ping(GIST_URL, 200);
  }

}

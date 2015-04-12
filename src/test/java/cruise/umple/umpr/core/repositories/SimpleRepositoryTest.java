/**
 * 
 */
package cruise.umple.umpr.core.repositories;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.net.URL;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.stream.Stream;

import cruise.umple.umpr.core.DiagramType;
import cruise.umple.umpr.core.License;
import cruise.umple.umpr.core.Repository;
import cruise.umple.umpr.core.consistent.ConsistentsModule;
import cruise.umple.umpr.core.entities.EntityModule;
import cruise.umple.umpr.core.entities.ImportEntity;
import cruise.umple.umpr.core.repositories.SimpleRepositoryTest.SRModule;
import cruise.umple.umpr.core.util.Networks;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;

/**
 * 
 *
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 *
 * @since Apr 10, 2015
 */
@Guice(modules={SRModule.class})
public class SimpleRepositoryTest {
  
  public static class SRModule extends AbstractModule {

    /* (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
      install(new ConsistentsModule());
      install(new EntityModule());
      
    }
    
  }
  
  @SuppressWarnings("unused")
  private final Logger log;
  private final Injector injector;
  
  /**
   * Instantiate new SimpleRepositoryTest
   * @since Apr 10, 2015
   */
  @Inject
  public SimpleRepositoryTest(Logger log, Injector injector) {
    this.log = log;
    this.injector = injector;
  }
  
  @Test
  public void proper() {
    Repository r = injector.getInstance(Proper.class);
    
    assertEquals(r.getName(), Fixture.REPO_NAME);
    assertEquals(r.getRemoteLoc().get().toString(), Fixture.REPO_URL_STRING);
    assertEquals(r.getDescription(), Fixture.REPO_DESC);
    assertEquals(r.getDiagramType(), Fixture.REPO_DTYPE);
    assertEquals(r.getLicense(), Fixture.REPO_LICENSE);
  }
  
  private static void stripProv(Runnable inv) throws Throwable {
    try {
      inv.run();
    } catch (ProvisionException pe) {
      throw Throwables.getRootCause(pe);
    }
  }

  @Test(expectedExceptions=NoSuchElementException.class)
  public void noName() throws Throwable {
    stripProv(() -> injector.getInstance(NoName.class));
    
  }
  
  // Url is actually optional.
  @Test
  public void noUrl() {
    Repository r = injector.getInstance(NoUrl.class);
    assertFalse(r.getRemoteLoc().isPresent());
  }
  
  @Test(expectedExceptions=NoSuchElementException.class)
  public void noDesc() throws Throwable {
    stripProv(() -> injector.getInstance(NoDesc.class));
    
  }
  
  @Test(expectedExceptions=NoSuchElementException.class)
  public void NoDT() throws Throwable {
    stripProv(() -> injector.getInstance(NoDT.class));
    
  }
  
  @Test(expectedExceptions=NoSuchElementException.class)
  public void NoLicense() throws Throwable {
    stripProv(() -> injector.getInstance(NoLicense.class));
    
  }
  
  @Test(expectedExceptions=IllegalStateException.class)
  public void twoNames() throws Throwable {
    stripProv(() -> injector.getInstance(TwoNames.class));
    
  }
  
  @Test(expectedExceptions=IllegalStateException.class)
  public void twoNamesMethods() throws Throwable {
    stripProv(() -> injector.getInstance(TwoNamesTwoMethods.class));
    
  }
  
  @Test(expectedExceptions=IllegalStateException.class)
  public void twoNamesOneMethodOneField() throws Throwable {
    stripProv(() -> injector.getInstance(TwoNamesOneFieldOneMethod.class));
    
  }
  
  @Test
  public void urlViaUrl() throws Throwable {
    Repository r = injector.getInstance(UrlViaUrl.class);
    assertEquals(r.getRemoteLoc().get(), Fixture.REPO_URL_URL);
  }
  

  
  
  
  
  ///////////////////////////
  // Private classes required for testing:
  //////////////////////////
  
  /**
   * Class used as a fixture to remove dead method definitions.
   * 
   * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
   *
   * @since Apr 10, 2015
   */
  private static class Fixture extends SimpleRepository implements Repository {

    public static final String REPO_NAME = "TestTest";
      
    public final static String REPO_URL_STRING = "http://www.example.com/";
    public final static URL REPO_URL_URL = Networks.newURL(REPO_URL_STRING);
    
    public final static String REPO_DESC = "It's a tarp!";
  
    public final static DiagramType REPO_DTYPE = DiagramType.CLASS;
    
    public final static License REPO_LICENSE = License.MIT;
    
    Fixture(Logger logger, Class<?> subtype) {
      super(logger, subtype);
    }
    
    // The following methods are all unimplemented, they are not required to be

    /* (non-Javadoc)
     * @see cruise.umple.umpr.core.repositories.SimpleRepository#getImports()
     */
    @Override
    public final Stream<ImportEntity> getImports() {
      throw new UnsupportedOperationException("SimpleRepository::getImports is unimplemented.");
    }

    /* (non-Javadoc)
     * @see cruise.umple.umpr.core.repositories.SimpleRepository#isAccessible()
     */
    @Override
    public final boolean isAccessible() {
      throw new UnsupportedOperationException("SimpleRepository::isAccessible is unimplemented.");
    }
    
  }
  
  static class Proper extends Fixture implements Repository {

    @Name
    private static final String REPO_NAME = Fixture.REPO_NAME;
    
    @Remote
    private final static String REPO_URL = Fixture.REPO_URL_STRING;
    
    @Description
    private final static String REPO_DESC = Fixture.REPO_DESC;
  
    @DType
    private final static DiagramType REPO_DTYPE = Fixture.REPO_DTYPE;
    
    // use a static method
    @CLicense
    private static License getLicenseM() {
      return Fixture.REPO_LICENSE;
    }
    
    @Inject
    public Proper(Logger logger) {
      super(logger, Proper.class);
      
    }
    
  }
  
  static class NoName extends Fixture implements Repository {

//    @Name
//    private static final String REPO_NAME = Fixture.REPO_NAME;
    
    @Remote
    private final static String REPO_URL = Fixture.REPO_URL_STRING;
    
    @Description
    private final static String REPO_DESC = Fixture.REPO_DESC;

    @DType
    private final static DiagramType REPO_DTYPE = Fixture.REPO_DTYPE;
    
    @CLicense
    private final static License REPO_LICENSE = Fixture.REPO_LICENSE;
    
    @Inject
    public NoName(Logger logger) {
      super(logger, NoName.class);
      
    }
    
  }
  
  static class NoUrl extends Fixture implements Repository {

    @Name
    private static final String REPO_NAME = Fixture.REPO_NAME;
    
//    @Remote
//    private final static String REPO_URL = Fixture.REPO_URL;
    
    @Description
    private final static String REPO_DESC = Fixture.REPO_DESC;
  
    @DType
    private final static DiagramType REPO_DTYPE = Fixture.REPO_DTYPE;
    
    @CLicense
    private final static License REPO_LICENSE = Fixture.REPO_LICENSE;
    
    @Inject
    public NoUrl(Logger logger) {
      super(logger, NoUrl.class);
      
    }
    
  }
  
  static class NoDesc extends Fixture implements Repository {

    @Name
    private static final String REPO_NAME = Fixture.REPO_NAME;
    
    @Remote
    private final static String REPO_URL = Fixture.REPO_URL_STRING;
    
//    @Description
//    private final static String REPO_DESC = Fixture.REPO_DESC;
  
    @DType
    private final static DiagramType REPO_DTYPE = Fixture.REPO_DTYPE;
    
    @CLicense
    private final static License REPO_LICENSE = Fixture.REPO_LICENSE;
    
    @Inject
    public NoDesc(Logger logger) {
      super(logger, NoDesc.class);
      
    }
    
  }
  
  static class NoDT extends Fixture implements Repository {

    @Name
    private static final String REPO_NAME = Fixture.REPO_NAME;
    
    @Remote
    private final static String REPO_URL = Fixture.REPO_URL_STRING;
    
    @Description
    private final static String REPO_DESC = Fixture.REPO_DESC;
  
//    @DType
//    private final static DiagramType REPO_DTYPE = Fixture.REPO_DTYPE;
    
    @CLicense
    private final static License REPO_LICENSE = Fixture.REPO_LICENSE;
    
    @Inject
    public NoDT(Logger logger) {
      super(logger, NoDT.class);
      
    }
    
  }
  
  static class NoLicense extends Fixture implements Repository {

    @Name
    private static final String REPO_NAME = Fixture.REPO_NAME;
    
    @Remote
    private final static String REPO_URL = Fixture.REPO_URL_STRING;
    
    @Description
    private final static String REPO_DESC = Fixture.REPO_DESC;
  
    @DType
    private final static DiagramType REPO_DTYPE = Fixture.REPO_DTYPE;
    
//    @CLicense
//    private final static License REPO_LICENSE = Fixture.REPO_LICENSE;
    
    @Inject
    public NoLicense(Logger logger) {
      super(logger, NoLicense.class);
      
    }
    
  }
  
  static class TwoNames extends Fixture implements Repository {

    @Name
    private static final String REPO_NAME = Fixture.REPO_NAME;
    
    @Name
    private static final String REPO_NAME2 = "TestTest2";
    
    @Remote
    private final static String REPO_URL = Fixture.REPO_URL_STRING;
    
    @Description
    private final static String REPO_DESC = Fixture.REPO_DESC;
  
    @DType
    private final static DiagramType REPO_DTYPE = Fixture.REPO_DTYPE;
    
    @CLicense
    private final static License REPO_LICENSE = Fixture.REPO_LICENSE;
    
    @Inject
    public TwoNames(Logger logger) {
      super(logger, TwoNames.class);
    }
    
  }
  
  static class TwoNamesOneFieldOneMethod extends Fixture implements Repository {

    @Name
    private static final String REPO_NAME = Fixture.REPO_NAME;
    
    @Name
    private static String getNameM() {
      return Fixture.REPO_NAME;
    }
    
    @Remote
    private final static String REPO_URL = Fixture.REPO_URL_STRING;
    
    @Description
    private final static String REPO_DESC = Fixture.REPO_DESC;
  
    @DType
    private final static DiagramType REPO_DTYPE = Fixture.REPO_DTYPE;
    
    @CLicense
    private final static License REPO_LICENSE = Fixture.REPO_LICENSE;
    
    @Inject
    public TwoNamesOneFieldOneMethod(Logger logger) {
      super(logger, TwoNamesOneFieldOneMethod.class);
    }
    
  }
  
  static class TwoNamesTwoMethods extends Fixture implements Repository {
    
    @Name
    private static String getNameM() {
      return Fixture.REPO_NAME;
    }
    
    @Name
    private static String getNameM2() {
      return Fixture.REPO_NAME;
    }
    
    @Remote
    private final static String REPO_URL = Fixture.REPO_URL_STRING;
    
    @Description
    private final static String REPO_DESC = Fixture.REPO_DESC;
  
    @DType
    private final static DiagramType REPO_DTYPE = Fixture.REPO_DTYPE;
    
    @CLicense
    private final static License REPO_LICENSE = Fixture.REPO_LICENSE;
    
    @Inject
    public TwoNamesTwoMethods(Logger logger) {
      super(logger, TwoNamesTwoMethods.class);
    }
    
  }
  
  static class UrlViaUrl extends Fixture implements Repository {

    @Name
    private static final String REPO_NAME = Fixture.REPO_NAME;
    
    @Remote
    private final static URL REPO_URL = Fixture.REPO_URL_URL;
    
    @Description
    private final static String REPO_DESC = Fixture.REPO_DESC;
  
    @DType
    private final static DiagramType REPO_DTYPE = Fixture.REPO_DTYPE;
    
    @CLicense
    private final static License REPO_LICENSE = Fixture.REPO_LICENSE;
    
    @Inject
    public UrlViaUrl(Logger logger) {
      super(logger, UrlViaUrl.class);
    }
    
  }
  
}

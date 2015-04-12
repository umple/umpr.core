/**
 * 
 */
package cruise.umple.umpr.core.fixtures;

import cruise.umple.umpr.core.Repository;
import cruise.umple.umpr.core.repositories.TestRepository;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * Module for Test Repositories to give guarenteed consistencies.
 * 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 */
public class TestRepositoryModule extends AbstractModule {

  /* (non-Javadoc)
   * @see com.google.inject.AbstractModule#configure()
   */
  @Override
  protected void configure() {
    Multibinder<Repository> mbinder = Multibinder.newSetBinder(binder(), Repository.class);

    mbinder.addBinding().to(TestRepository.class);
    
  }

}

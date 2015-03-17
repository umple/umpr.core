/**
 * 
 */
package cruise.umple.sample.downloader.consistent;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Consistents Module setting up all injection 
 * 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 * 
 * @since 11 Mar 2015
 */
public class ConsistentsModule extends AbstractModule {

  /* (non-Javadoc)
   * @see com.google.inject.AbstractModule#configure()
   */
  @Override
  protected void configure() {
    install(new FactoryModuleBuilder()
      .implement(ConsistentsBuilder.class, ConsistentsBuilder.class)
      .implement(ConsistentRepositoryBuilder.class, ConsistentRepositoryBuilder.class)
      .build(ConsistentsFactory.class));
    
    requestStaticInjection(Consistents.class);

  }

}

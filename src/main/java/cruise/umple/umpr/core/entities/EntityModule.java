package cruise.umple.umpr.core.entities;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

/**
 * Guice module for {@link ImportEntity} model. 
 * 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 * @since Mar 2, 2015
 */
public class EntityModule implements Module {

  @Override
  public void configure(Binder binder) {
    binder.install(new FactoryModuleBuilder()
      .implement(ImportEntity.class, Names.named("String"), StringEntity.class)
      .implement(ImportEntity.class, Names.named("URL"), URLEntity.class)
      .build(ImportEntityFactory.class));
  }

}

package cruise.umple.umpr.core.repositories;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

import cruise.umple.umpr.core.Repository;

/**
 * Module that binds all Repository instances in for the main application to load at runtime.
 */
public class RepositoryModule implements Module {
  
    @Override
    public void configure(Binder binder) {
      Multibinder<Repository> mbinder = Multibinder.newSetBinder(binder, Repository.class);

      mbinder.addBinding().to(AtlanZooRepository.class);
      mbinder.addBinding().to(ScxmlStandardRepository.class);
      mbinder.addBinding().to(Iso20022EcoreRepository.class);
    }
}

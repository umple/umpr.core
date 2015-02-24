package cruise.umple.sample.downloader.repositories;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import cruise.umple.sample.downloader.Repository;

/**
 * Module that binds all Repository instances in for the main application to load at runtime.
 */
public class RepositoryModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<Repository> binder = Multibinder.newSetBinder(binder(), Repository.class);

        binder.addBinding().to(AtlanZooRepository.class);
    }
}

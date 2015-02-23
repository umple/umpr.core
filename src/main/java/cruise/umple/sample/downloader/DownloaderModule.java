package cruise.umple.sample.downloader;

import com.google.inject.AbstractModule;

/**
 * Created by kevin on 15-02-22.
 */
public class DownloaderModule extends AbstractModule {

    @Override
    public void configure() {
        bind(DocumentFactory.class).to(RealDocumentProvider.class);

    }

}

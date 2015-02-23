package cruise.umple.sample.downloader;

import java.net.URL;
import java.util.List;

/**
 * Created by kevin on 15-02-22.
 */
public interface Repository {

    /**
     * Get the name of the Respository, there are no requirements except not null or empty.
     * @return Human-friendly name
     */
    public String getName();

    /**
     * Get a list of URL instances where the import files may be stored.
     * @return Non-{@code null}, possibly empty list of Import Files
     */
    public List<URL> getImportFiles();

    /**
     * Checks if the repository is accessible.
     * @return True if accessible and usable
     */
    public boolean isAccessible();
}

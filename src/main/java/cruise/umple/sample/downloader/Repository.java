package cruise.umple.sample.downloader;

import cruise.umple.sample.downloader.util.Pair;

import java.net.URL;
import java.util.List;

/**
 * Defines the behaviour of a collection of importable files.
 */
public interface Repository {

    /**
     * Get the name of the Respository, there are no requirements except not null or empty.
     *
     * @return Human-friendly name
     */
    public String getName();

    /**
     * Get the type of files in the repository.
     *
     * @return The type of file
     */
    public FileType getFileType();

    /**
     * Get a list of URL instances where the import files may be stored.
     *
     * @return Non-{@code null}, possibly empty list of Import Files
     */
    public List<Pair<Repository, URL>> getImportFiles();

    /**
     * Checks if the repository is accessible.
     *
     * @return True if accessible and usable
     */
    public boolean isAccessible();
}

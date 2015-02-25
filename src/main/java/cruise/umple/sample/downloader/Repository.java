package cruise.umple.sample.downloader;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import cruise.umple.sample.downloader.util.Pair;
import cruise.umple.sample.downloader.util.Triple;

/**
 * Defines the behaviour of a collection of importable files.
 */
public interface Repository {

    /**
     * Get the name of the Repository, there are no requirements except not null or empty.
     *
     * @return Human-friendly name, non-null or empty. 
     */
    public String getName();
    
    /**
     * Returns a human-readable description of the repository. 
     * @return Non-null, nor empty String representation. 
     */
    public String getDescription();
    

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
    public List<Triple<Repository, Path, Supplier<String>>> getImports();

    /**
     * Checks if the repository is accessible.
     *
     * @return True if accessible and usable
     */
    public boolean isAccessible();
}

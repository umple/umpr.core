package cruise.umple.sample.downloader;

import java.util.List;

import cruise.umple.sample.downloader.entities.ImportEntity;

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
     * Get the diagram types in the repository.
     *
     * @return The diagrams of file
     */
    public DiagramType getDiagramType();

    /**
     * Get a list of URL instances where the import files may be stored.
     *
     * @return Non-{@code null}, possibly empty list of {@link ImportEntity} instances
     */
    public List<ImportEntity> getImports();

    /**
     * Checks if the repository is accessible.
     *
     * @return True if accessible and usable
     */
    public boolean isAccessible();
}

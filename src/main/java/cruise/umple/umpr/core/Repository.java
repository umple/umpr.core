package cruise.umple.umpr.core;

import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;

import cruise.umple.umpr.core.entities.ImportEntity;

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
     * Return a {@link URL} to a source. 
     * @return Return {@link Optional#of(Object)} {@link URL} instance if available, otherwise {@link Optional#empty()}.
     * @since Apr 9, 2015
     */
    public Optional<URL> getRemoteLoc();
    
    /**
     * Get the license that all material are covered under. Imported entities have optional {@link License}
     * @return
     * @since Apr 9, 2015
     */
    public License getLicense();

    /**
     * Get the diagram types in the repository.
     *
     * @return The diagrams of file
     */
    public DiagramType getDiagramType();

    /**
     * Get a list of URL instances where the import files may be stored.
     *
     * @return Non-{@code null}, possibly empty {@link Stream} of {@link ImportEntity} instances
     */
    public Stream<ImportEntity> getImports();

    /**
     * Checks if the repository is accessible.
     *
     * @return True if accessible and usable
     */
    public boolean isAccessible();
}

/**
 * 
 */
package cruise.umple.sample.downloader;

/**
 * Used to signify the stage of importing for a file. 
 * 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 * @since 24 Mar 2015
 *
 */
public enum ImportStage {
  
  /**
   * No failure occured.
   */
  COMPLETE(null),
  
  /**
   * The model was improperly formatted. 
   */
  MODEL(COMPLETE),
  
  /**
   * The fetched model wasn't properly produced. 
   */
  IMPORT(MODEL),
  
  /**
   * If an error happens when fetching any imports from the repository (or itself).
   */
  FETCH(IMPORT);
  
  final ImportStage next;
  
  ImportStage(final ImportStage next) {
    this.next = next;
  }
  
  /**
   * Get the next stage. 
   * @return
   */
  public final ImportStage next() {
    return this.next;
  }
  
  /**
   * Rethrows {@code cause} as a {@link StageException}. 
   * @param cause
   */
  public StageException throwAs(final Throwable cause) {
    throw new StageException(cause);
  }
  
  /**
   * Wraps a {@link Throwable} in {@link StageException}. 
   * @param cause Exception to wrap. 
   * @return wrapped instance
   */
  public StageException wrapException(final Throwable cause) {
    return new StageException(cause);
  }
  
  /**
   * Wrapped exception stating the stage it was thrown at. 
   * 
   * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
   * @since 24 Mar 2015
   *
   */
  public class StageException extends RuntimeException {
    
    private static final long serialVersionUID = -342108241335560977L;

    /**
     * Wraps a {@link Throwable} in another exception. 
     * @param stage Stage the exception was thrown from. 
     * @param cause Reason for the exception.
     */
    public StageException(final Throwable cause) {
      super(cause);
    }
    
    /**
     * Get the stage that this was thrown at. 
     * @return Stage this was thrown at. 
     */
    public ImportStage getStage() {
      return ImportStage.this;
    }
  }
}

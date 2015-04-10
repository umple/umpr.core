/**
 * 
 */
package cruise.umple.umpr.core.repositories;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cruise.umple.umpr.core.DiagramType;
import cruise.umple.umpr.core.Repository;
import cruise.umple.umpr.core.entities.ImportEntity;

import com.google.common.collect.ImmutableList;

/**
 * {@link SimpleRepository} is a means of defining repositories quickly and consistently. This class aims to use an
 * Annotation based scheme to ease the implementation of {@link Repository} types. 
 *
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 *
 * @since Apr 9, 2015
 */
abstract class SimpleRepository implements Repository {
  
  /**
   * Marks a field that will be the repository name from {@link SimpleRepository}. This must mark a {@link String} or
   * static method that returns a {@link String}.
   *
   * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
   *
   * @since Apr 9, 2015
   */
  @Target({ElementType.FIELD, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Name {
    // no content
  }
  
  /**
   * Marks a field that will be the repository Description from {@link SimpleRepository}. This must mark a 
   * {@link String} or static method that returns a {@link String}.
   *
   * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
   *
   * @since Apr 9, 2015
   */
  @Target({ElementType.FIELD, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Description {
    // no content
  }
  
  /**
   * Marks a field that will be the repository Remote URL from {@link SimpleRepository}. This must mark a {@link String}
   * or {@link URL} or static method that returns a {@link String} or {@link URL}.
   *
   * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
   *
   * @since Apr 9, 2015
   */
  @Target({ElementType.FIELD, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Remote {
    // no content
  }
  
  /**
   * Marks a field that will be the {@link DiagramType} from {@link SimpleRepository}. This must mark a 
   * {@link DiagramType} or static method that returns a {@link DiagramType}.
   *
   * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
   *
   * @since Apr 9, 2015
   */
  @Target({ElementType.FIELD, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface DType {
    // no content
  }
  
  /**
   * Marks a field that will be the {@link License} from {@link SimpleRepository}. This must mark a {@link License} or a
   * static method that returns a {@link License}.
   *
   * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
   *
   * @since Apr 9, 2015
   */
  @Target({ElementType.FIELD, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface CLicense {
    // no content
  }
  
  private final String name;
  private final String description;
  private final Optional<URL> remoteLoc;
  private final DiagramType diagramType;
  private final License license;
  
  /**
   * Uses Reflection to parse through the fields and methods of the subtype to try to find the {@code annotation} passed
   * and return the type in an {@link Optional}. 
   * @param base Type searching against
   * @param ifields A {@link List} of the static {@link Field}s in the base
   * @param imethods A {@link List} of static {@link Method}s in the base
   * @param type Expected return type
   * @param annotation Annotation to find
   * @return {@link Optional#empty()} if not found, or present with content.
   * 
   * @since Apr 9, 2015
   * @throws IllegalStateException if annotations are found with invalid values. 
   */
  private <T> Optional<T> getReflectiveValue(final Class<?> base, final List<Field> ifields, 
      final List<Method> imethods, Class<T> type, Class<? extends Annotation> annotation) {
    List<Field> fields = ifields.stream()
        .filter(f -> type.isAssignableFrom(f.getType()) && f.isAnnotationPresent(annotation))
        .collect(Collectors.toList());
    
    List<Method> methods = imethods.stream()
        .filter(m -> m.getReturnType().isAssignableFrom(type) && m.isAnnotationPresent(annotation))
        .collect(Collectors.toList());
    
    boolean foundAnn = !fields.isEmpty() || !methods.isEmpty();
    
    if (!fields.isEmpty() && !methods.isEmpty()) {
      log.severe("Class " + base.getName() + " has both method(s) and field(s) annotated for " + annotation.getName());
      throw new IllegalStateException("Class " + base.getName() + " has both method(s) and "
          + "field(s) annotated for " + annotation.getName());
    }
    
    Optional<T> out = Optional.empty();
    if (!fields.isEmpty()) {
      if (fields.size() > 1) {
        log.severe("Class " + base.getName() + " has multiple field annotations for " + annotation.getName());
        throw new IllegalStateException("Class " + base.getName() + " has multiple field annotations for " + annotation.getName());
      }
      
      final Field f = fields.get(0);
      f.setAccessible(true);
      // found a valid field?
      
      try {
        @SuppressWarnings("unchecked")
        final T value = (T)f.get(null);
        out = Optional.ofNullable(value);
      } catch (IllegalArgumentException | IllegalAccessException e) {
        // propagate exception out, however it should never happen
        throw new IllegalStateException(e);
      }
    }
    
    if (!out.isPresent() && !methods.isEmpty()) {
      if (methods.size() > 1) {
        log.warning("Class " + base.getName() + " has multiple method annotations for " + annotation.getName());
        throw new IllegalStateException("Class " + base.getName() + " has multiple method annotations for " + annotation.getName());
        
      }
      
      final Method m = methods.get(0);
      m.setAccessible(true);
      
      try {
        @SuppressWarnings("unchecked")
        final T value = (T)m.invoke(null, new Object[]{});
        out = Optional.ofNullable(value);
      } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
        // propagate exception out, however it should never happen
        throw new IllegalStateException(e);
      }
    }
    
    if (foundAnn && !out.isPresent()) {
      log.severe("Annotations were found for " + annotation.getName() + " but no non-null value was returned.");
      throw new IllegalStateException("Annotations were found for " + annotation.getName() + " but no non-null "
          + "value was returned.");
    }
    
    return out;
  }
  
  private final Logger log;
  
  /**
   * Instantiate new SimpleRepository via reflection using the annotations: {@link Name}, {@link Description}, 
   * {@link Remote}, {@link DType}, and {@link CLicense} respectively. If a method or field is misslabelled, this scheme
   * silently ignores it.
   * 
   * @param logger Injected logger instance from subtype
   * @param subtype Class for subclass.
   * 
   * @since Apr 9, 2015
   * @throws IllegalStateException if anything fails throughout except a parameter not found
   * @throws NoSuchElementException if a parameter is not found
   * @see #getReflectiveValue(Class, List, List, Class, Class)
   */
  public SimpleRepository(Logger logger, Class<?> subtype) {
    log = logger;
    checkArgument(SimpleRepository.class.isAssignableFrom(subtype));
    
    final List<Field> fields = ImmutableList.copyOf(Arrays.asList(subtype.getDeclaredFields()).stream()
        .filter(f -> Modifier.isStatic(f.getModifiers()))
        .collect(Collectors.toList()));
    
    final List<Method> methods = ImmutableList.copyOf(Arrays.asList(subtype.getDeclaredMethods()).stream()
        .filter(m -> Modifier.isStatic(m.getModifiers()))
        .collect(Collectors.toList()));
    
    // read the parameters from the list of methods and fields
    this.name = getReflectiveValue(subtype, fields, methods, String.class, Name.class).get();
    this.description = getReflectiveValue(subtype, fields, methods, String.class, Description.class).get();
    this.diagramType = getReflectiveValue(subtype, fields, methods, DiagramType.class, DType.class).get();
    this.license = getReflectiveValue(subtype, fields, methods, License.class, CLicense.class).get();
    
    // read the URL (it's optional)
    final Optional<String> sloc = getReflectiveValue(subtype, fields, methods, String.class, Remote.class);
    if (sloc.isPresent()) {
      try {
        this.remoteLoc = Optional.of(new URL(sloc.get()));
      } catch (MalformedURLException e) {
        // propagate exception out
        throw new IllegalStateException(e);
      }
    } else {
      this.remoteLoc = getReflectiveValue(subtype, fields, methods, URL.class, Remote.class);
    }
  }

  /* (non-Javadoc)
   * @see cruise.umple.umpr.core.Repository#getName()
   */
  @Override
  public String getName() {
    return name;
  }
  
  /* (non-Javadoc)
   * @see cruise.umple.umpr.core.Repository#getDescription()
   */
  @Override
  public String getDescription() {
    return description;
  }
  
  /* (non-Javadoc)
   * @see cruise.umple.umpr.core.Repository#getRemoteLocation()
   */
  @Override
  public Optional<URL> getRemoteLocation() {
    return remoteLoc;
  }
  
  /* (non-Javadoc)
   * @see cruise.umple.umpr.core.Repository#getDiagramType()
   */
  @Override
  public DiagramType getDiagramType() {
    return diagramType;
  }
  
  @Override 
  public License getLicense() {
    return this.license;
  }
  
  /* (non-Javadoc)
   * @see cruise.umple.umpr.core.Repository#getImports()
   */
  @Override
  public abstract Stream<ImportEntity> getImports();
  
  /* (non-Javadoc)
   * @see cruise.umple.umpr.core.Repository#isAccessible()
   */
  @Override
  public abstract boolean isAccessible();
  
}

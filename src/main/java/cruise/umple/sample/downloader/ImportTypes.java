/**
 * 
 */
package cruise.umple.sample.downloader;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

/**
 * @author kevin
 *
 */
public abstract class ImportTypes {

  private ImportTypes() { 
    // Do not instantiate
  }
  
  private static final Map<String, ImportType> ALL_TYPES;
  static {
    ImmutableMap.Builder<String, ImportType> allTypesBld = ImmutableMap.builder();
    
    
    List<Field> fields = Arrays.asList(ImportType.class.getFields());
    fields.stream().filter(f -> {
      final int mods = f.getModifiers();
      
      return f.getType() == ImportType.class && 
          f.isAccessible() && 
          Modifier.isStatic(mods) && Modifier.isPublic(mods);
    }).forEach(f -> {
      try {
        allTypesBld.put(f.getName(), (ImportType)f.get(null));
      } catch (IllegalArgumentException | IllegalAccessException e) {
        // Neither exception should be thrown as the field is static and public
        Throwables.propagate(e);
      }
    });
    
    ALL_TYPES = allTypesBld.build();
  }
  
  /**
   * Gets the {@link ImportType} with {@code name}. 
   * @param name The name of the {@link ImportType}, see {@link ImportType#getName()}.
   * @return Non-{@code null} {@link ImportType}. 
   * @throws IllegalArgumentException if name is {@code null} or empty
   * @throws NoSuchElementException if name is not a valid identifier
   */
  public static ImportType valueOf(final String name) {
    if (Strings.isNullOrEmpty(name)) {
      throw new IllegalArgumentException("name is null or empty.");
    }
    
    if (!ALL_TYPES.containsKey(name)) {
      throw new NoSuchElementException("ImportType with name=" + name + " does not exist.");
    }
    
    return ALL_TYPES.get(name);
  }
    
}

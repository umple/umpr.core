/**
 * 
 */
package cruise.umple.umpr.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

import cruise.umple.compiler.UmpleImportType;

/**
 * @author kevin
 *
 */
public abstract class UmpleImportTypes {

  private UmpleImportTypes() { 
    // Do not instantiate
  }
  
  private static final Map<String, UmpleImportType> ALL_TYPES;
  // reflectively build up a map of all types
  static {
    ImmutableMap.Builder<String, UmpleImportType> allTypesBld = ImmutableMap.builder();
    
    
    List<Field> fields = Arrays.asList(UmpleImportType.class.getFields());
    fields.stream().filter(f -> {
      final int mods = f.getModifiers();
      
      return f.getType() == UmpleImportType.class && 
          f.isAccessible() && 
          Modifier.isStatic(mods) && Modifier.isPublic(mods);
    }).forEach(f -> {
      try {
        allTypesBld.put(f.getName(), (UmpleImportType)f.get(null));
      } catch (IllegalArgumentException | IllegalAccessException e) {
        // Neither exception should be thrown as the field is static and public
        Throwables.propagate(e);
      }
    });
    
    ALL_TYPES = allTypesBld.build();
  }
  
  /**
   * Gets the {@link UmpleImportType} with {@code name}. 
   * @param name The name of the {@link UmpleImportType}, see {@link UmpleImportType#getName()}.
   * @return Non-{@code null} {@link UmpleImportType}. 
   * @throws IllegalArgumentException if name is {@code null} or empty
   * @throws NoSuchElementException if name is not a valid identifier
   */
  public static UmpleImportType valueOf(final String name) {
    if (Strings.isNullOrEmpty(name)) {
      throw new IllegalArgumentException("name is null or empty.");
    }
    
    if (!ALL_TYPES.containsKey(name)) {
      throw new NoSuchElementException("UmpleImportType with name=" + name + " does not exist.");
    }
    
    return ALL_TYPES.get(name);
  }
    
}

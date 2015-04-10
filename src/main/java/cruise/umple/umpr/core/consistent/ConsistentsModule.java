/**
 * 
 */
package cruise.umple.umpr.core.consistent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cruise.umple.umpr.core.ImportAttrib;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Consistents Module setting up all injection 
 * 
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 * 
 * @since 11 Mar 2015
 */
public class ConsistentsModule extends AbstractModule {
  
  /**
   * Used to mark in the {@link Consistents} module primarily, however any field may be annotated with this if it 
   * requires the {@link Module} or {@link ObjectMapper} for JSON parsing. 
   * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
   * @since 1 Apr 2015
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  @BindingAnnotation
  public @interface ConsistentsJacksonConfig {
    /* Intentionally empty */
  }

  /* (non-Javadoc)
   * @see com.google.inject.AbstractModule#configure()
   */
  @Override
  protected void configure() {
    install(new FactoryModuleBuilder()
      .implement(ConsistentsBuilder.class, ConsistentsBuilder.class)
      .implement(ConsistentRepositoryBuilder.class, ConsistentRepositoryBuilder.class)
      .build(ConsistentsFactory.class));
    
    final SimpleModule jsonModule = new SimpleModule();
    // initialize the Jackson module
    jsonModule.addSerializer(ImportRepositorySet.class, new Consistents.ImportRepositorySetSerializer());
    jsonModule.addSerializer(ImportRepository.class, new Consistents.ImportRepositorySerializer());
    jsonModule.addSerializer(ImportFile.class, new Consistents.ImportFileSerializer());
    jsonModule.addSerializer(ImportAttrib.class, new Consistents.AttribSerializer());
    jsonModule.addDeserializer(ImportRepositorySet.class, new Consistents.ImportRepositorySetDeserializer(getProvider(ConsistentsFactory.class)));
    
    final ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(jsonModule);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    
    bind(Module.class).annotatedWith(ConsistentsJacksonConfig.class).toInstance(jsonModule);
    bind(ObjectMapper.class).annotatedWith(ConsistentsJacksonConfig.class).toInstance(mapper);
    
    requestStaticInjection(Consistents.class);

  }

}

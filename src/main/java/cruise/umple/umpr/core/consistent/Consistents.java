/**
 * 
 */
package cruise.umple.umpr.core.consistent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import com.google.inject.Provider;

import cruise.umple.compiler.UmpleImportType;
import cruise.umple.umpr.core.ConsoleMain;
import cruise.umple.umpr.core.DiagramType;
import cruise.umple.umpr.core.ImportFSM;
import cruise.umple.umpr.core.Repository;
import cruise.umple.umpr.core.consistent.ConsistentsModule.ConsistentsJacksonConfig;

/**
 * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
 *
 * @since 11 Mar 2015
 */
public abstract class Consistents {
  
  @Inject
  private static ConsistentsFactory CONSISTENTS_FACTORY;
  
  @SuppressWarnings("unused")
  @Inject
  private static Logger logger;
  
  @Inject @ConsistentsJacksonConfig // get the json config from the module
  private static ObjectMapper mapper;

  private Consistents() { }
  
  /**
   * Builds an {@link ImportRepositorySet} from the runtime data produced by the {@link ConsoleMain} using 
   * {@link ConsistentsBuilder}.  
   * 
   * @param outputFolder The location the repository lives. 
   * @param allData {@link List} of {@link ImportRuntimeData} to map into new the consistent data structures. 
   * @return Non-{@code null} instance
   */
  public static ImportRepositorySet buildImportRepositorySet(final Path outputFolder,
                                                             final Path srcFolder, 
                                                             final Iterable<? extends ImportFSM> allData) {
    
    final Multimap<Repository, ? extends ImportFSM> dataByRepo = Multimaps.index(allData, ImportFSM::getRepository);
    
    final ConsistentsBuilder cbld = CONSISTENTS_FACTORY.create(outputFolder, srcFolder);
    dataByRepo.asMap().entrySet().forEach(entry -> {
        final Repository key = entry.getKey();
        final ConsistentRepositoryBuilder repoBld = cbld.withRepository(key);
        
        entry.getValue().forEach( data -> {
          final Path outpath = data.getOutputPath().getFileName();
          if (data.isSuccessful()) {
            repoBld.addSuccessFile(outpath.toString(), data.getImportType());
          } else {
            repoBld.addFailedFile(outpath.toString(), data.getImportType(), 
                data.getState(), data.getFailure().get());
          }
        });
        
        repoBld.withCalculatedSuccessRate();
      });
    
    return cbld.getRepositorySet();
  }
  
  /**
   * Converts a POJO to JSON string. 
   * @param obj Object to convert 
   * @return Non-{@code null} JSON string. 
   * 
   * @throws IllegalStateException if an {@link IOException} is thrown by 
   *    {@link ObjectMapper#writeValue(java.io.OutputStream, Object)}
   */
  public static <T> String toJson(final T obj) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      mapper.writeValue(baos, obj);
      
      return baos.toString();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }    
  }
  
  /**
   * Reads an {@link InputStream} of JSON data and returns a new {@link ImportRepositorySet} instance. 
   * @param stream input data (callers responsibility to close)
   * @return Non-{@code null} instance, errors cause an exception. 
   */
  public static ImportRepositorySet fromJson(final InputStream stream) {
    try {
      return mapper.readValue(stream, ImportRepositorySet.class);     
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }
  
  /**
   * Reads an {@link String} of JSON data and returns a new {@link ImportRepositorySet} instance. 
   * @param data input json {@link String}
   * @return Non-{@code null} instance, errors cause an exception. 
   */
  public static ImportRepositorySet fromJson(final String data) {
    try {
      return mapper.readValue(data, ImportRepositorySet.class);     
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }
  
  
  /**
   * Converts an {@link ImportRepositorySet} to JSON
   * 
   * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
   * @since 17 Mar 2015
   */
  static class ImportRepositorySetSerializer extends JsonSerializer<ImportRepositorySet> {

    @Override
    public void serialize(ImportRepositorySet value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException,
        JsonProcessingException {
      
      gen.writeStartObject();
      
      gen.writeNumberField("date", value.getDate().getTime());
      gen.writeNumberField("time", value.getTime().getTime());
      
      gen.writeStringField("umple", value.getUmplePath());
      if (!Strings.isNullOrEmpty(value.getSrcPath())) {
        gen.writeStringField("src", value.getSrcPath());
      }
           
      gen.writeArrayFieldStart("repositories");
      final JsonSerializer<Object> repoSrlzr = serializers.findValueSerializer(ImportRepository.class);
      for (ImportRepository r: value.getRepositories()) {
        repoSrlzr.serialize(r, gen, serializers);
      }
      gen.writeEndArray();
      
      gen.writeEndObject();
    }
  }
    
  /**
   * Converts an {@link ImportRepository} to JSON
   * 
   * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
   * @since 17 Mar 2015
   */
  static class ImportRepositorySerializer extends JsonSerializer<ImportRepository> {

    @Override
    public void serialize(ImportRepository value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException,
        JsonProcessingException {
      gen.writeStartObject();
      
      gen.writeStringField("path", value.getPath());
      gen.writeStringField("description", value.getDescription());
      gen.writeStringField("name", value.getName());
      gen.writeStringField("diagramType", value.getDiagramType().getType());
      gen.writeNumberField("successRate", value.getSuccessRate());
      gen.writeNumberField("failRate", value.getFailRate());
      
      gen.writeArrayFieldStart("files");
      final JsonSerializer<Object> fileSrlzr = serializers.findValueSerializer(ImportFile.class);
      
      for (ImportFile f: value.getFiles()) {
        fileSrlzr.serialize(f, gen, serializers);
      }
      gen.writeEndArray();
      
      gen.writeEndObject();
    }
  }
  
  /**
   * Converts an {@link ImportFile} to JSON
   * 
   * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
   * @since 17 Mar 2015
   */
  static class ImportFileSerializer extends JsonSerializer<ImportFile> {

    @Override
    public void serialize(ImportFile value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException,
        JsonProcessingException {
      gen.writeStartObject();
      
      gen.writeStringField("path", value.getPath());
      gen.writeStringField("type", value.getImportType().getName());
      gen.writeStringField("lastState", value.getLastState().toString());
      gen.writeBooleanField("successful", value.isSuccessful());
      
      if (!Strings.isNullOrEmpty(value.getMessage())) {
        gen.writeStringField("message", value.getMessage());
      }
      
      gen.writeEndObject();
    }
  }
  
  static class ImportRepositorySetDeserializer extends JsonDeserializer<ImportRepositorySet> {
    
    private final Provider<ConsistentsFactory> factory;
    
    protected ImportRepositorySetDeserializer(Provider<ConsistentsFactory> factory) {
      super();
      
      this.factory = factory;
    }
    
    /**
     * Read all of the public static fields and convert them to a map of NAME -> Field
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> getFakeEnumMapping(final Class<T> type) {
      final List<Field> fields = Arrays.asList(type.getFields());

      Map<String, T> outFields = fields.stream()
          .filter(f -> Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers()) && f.getType() == type)
          .collect(Collectors.toMap(f -> f.getName().toLowerCase(),
              f -> {
                try {
                  return (T)f.get(null); 
                } catch (IllegalAccessException | IllegalArgumentException e) {
                  // CAN'T happen, we've filtered out the baddies.
                  throw Throwables.propagate(e);
                }
              }));
      
      return ImmutableMap.copyOf(outFields);
    }
    
    private final static Map<String, DiagramType> diagramMapping = getFakeEnumMapping(DiagramType.class);
    private final static Map<String, UmpleImportType> importMapping = getFakeEnumMapping(UmpleImportType.class);

    @Override
    public ImportRepositorySet deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      
      ObjectMapper mapper = (ObjectMapper) jp.getCodec();
      ObjectNode root = (ObjectNode) mapper.readTree(jp);
      
      final String umplePath = root.findValue("umple").asText();
      final String srcPath = root.findValue("src").asText();
      
      final ConsistentsBuilder bld = factory.get().create(Paths.get(umplePath), Paths.get(srcPath));
      
      final List<JsonNode> repos = root.findValues("repositories");
      
      repos.forEach(node -> {
        /* {
              "path" : "AtlanZooEcore",
              "description" : "STRING",
              "name" : "AtlanZooEcore",
              "diagramType" : "class",
              "successRate" : 0.5737704918032787,
              "failRate" : 0.42622950819672134,
              "files" : [ ... ]
           } */
        // read all the data points: 
        final String description = node.findValue("description").asText();
        final String name = node.findValue("name").asText();
        final DiagramType diagramType = diagramMapping.get(node.findValue("diagramType").asText());
        
        final ConsistentRepositoryBuilder rbld = bld.withRepository(name, diagramType, description);
        final List<JsonNode> files = node.findValues("files");
        
        // work through the file nodes individually
        files.forEach(fnode -> {
          /*  {
                "path" : "Mantis.ecore",
                "type" : "ECore",
                "lastState" : "Completed",
                "successful" : true
              }
           */
          final UmpleImportType type = importMapping.get(fnode.findValue("type").asText().toLowerCase());
          final String path = fnode.findValue("path").asText();
          final ImportFSM.State lastState = ImportFSM.State.valueOf(fnode.findValue("lastState").asText());
          final boolean successful = fnode.findValue("successful").asBoolean(false);
          
          // add the file
          if (!successful) {
            final String message = fnode.findValue("message").asText();
            rbld.addFailedFile(path, type, lastState, message);
          } else {
            rbld.addSuccessFile(path, type);
          }
        });
        
        rbld.withCalculatedSuccessRate();
      });
      
      return bld.getRepositorySet();     
    }
    
  }
}

/**
 * 
 */
package cruise.umple.sample.downloader.consistent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;

import cruise.umple.sample.downloader.ConsoleMain;
import cruise.umple.sample.downloader.ImportFSM;
import cruise.umple.sample.downloader.ImportRuntimeData;
import cruise.umple.sample.downloader.Repository;

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
  
  private static final SimpleModule jsonModule;
  static {
    // initialize the Jackson module
    jsonModule = new SimpleModule();
    jsonModule.addSerializer(ImportRepositorySet.class, new ImportRepositorySetSerializer());
    jsonModule.addSerializer(ImportRepository.class, new ImportRepositorySerializer());
    jsonModule.addSerializer(ImportFile.class, new ImportFileSerializer());
  }
  
  /**
   * {@link ObjectMapper} for mapping Java objects to Json
   */
  private static final ObjectMapper mapper = new ObjectMapper();
  static {
    mapper.registerModule(jsonModule);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

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
          if (!data.isSuccessful()) {
            repoBld.addSuccessFile(outpath.toString(), data.getImportType());
          } else {
            repoBld.addFailedFile(outpath.toString(), data.getImportType(), 
                data.getAction(), data.getFailure().get());
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
   * Converts an {@link ImportRepositorySet} to JSON
   * 
   * @author Kevin Brightwell <kevin.brightwell2@gmail.com>
   * @since 17 Mar 2015
   */
  private static class ImportRepositorySetSerializer extends JsonSerializer<ImportRepositorySet> {

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
  private static class ImportRepositorySerializer extends JsonSerializer<ImportRepository> {

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
  private static class ImportFileSerializer extends JsonSerializer<ImportFile> {

    @Override
    public void serialize(ImportFile value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException,
        JsonProcessingException {
      gen.writeStartObject();
      
      gen.writeStringField("path", value.getPath());
      gen.writeStringField("type", value.getImportType().getName());
      gen.writeStringField("lastState", value.getLastAction().toString());
      
      if (!Strings.isNullOrEmpty(value.getMessage())) {
        gen.writeStringField("message", value.getMessage());
      }
      
      gen.writeEndObject();
    }
  }
}

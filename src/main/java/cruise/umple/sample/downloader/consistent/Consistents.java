/**
 * 
 */
package cruise.umple.sample.downloader.consistent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Throwables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;

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
  
  private static final SimpleModule jsonModule;
  static {
    jsonModule = new SimpleModule();
    jsonModule.addSerializer(ImportRepositorySet.class, new ImportRepositorySetSerializer());
    jsonModule.addSerializer(ImportRepository.class, new ImportRepositorySerializer());
    jsonModule.addSerializer(ImportFile.class, new ImportFileSerializer());
  }
  
  
  private static final ObjectMapper mapper = new ObjectMapper();
  
  static {
    mapper.registerModule(jsonModule);
  }

  private Consistents() { }
  
  public static ImportRepositorySet buildImportRepositorySet(final Path outputFolder, 
                                                             final List<ImportRuntimeData> allData) {
    final Multimap<Repository, ImportRuntimeData> dataByRepo = Multimaps.index(allData, 
                                                                               ImportRuntimeData::getRepository);
    
    final ConsistentsBuilder cbld = CONSISTENTS_FACTORY.create(outputFolder.toAbsolutePath().toString());
    dataByRepo.asMap().entrySet().forEach(entry -> {
        final Repository key = entry.getKey();
        final ConsistentRepositoryBuilder repoBld = cbld.withRepository(key);
        entry.getValue().forEach(data -> {
              if (data.isSuccessful()) {
                repoBld.addSuccessFile(data.getOutputPath().toString(), data.getImportType());
              } else {
                repoBld.addFailedFile(data.getOutputPath().toString(),
                    data.getImportType(), Throwables.getStackTraceAsString(data.getFailure().get()));
              }
            });
      });
    
    return cbld.getRepositorySet();
  }
  
  public static <T> String toJson(T repository) {
    
    
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      mapper.writeValue(baos, repository);
      
      return baos.toString();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }    
  }
  
  
  private static class ImportRepositorySetSerializer extends JsonSerializer<ImportRepositorySet> {

    @Override
    public void serialize(ImportRepositorySet value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException,
        JsonProcessingException {
      
      gen.writeStartObject();
      
      gen.writeNumberField("date", value.getDate().getTime());
      gen.writeNumberField("time", value.getTime().getTime());
      
      gen.writeStringField("rootPath", value.getRootPath());
      
      gen.writeArrayFieldStart("repositories");
      final JsonSerializer<Object> repoSrlzr = serializers.findValueSerializer(ImportRepository.class);
      for (ImportRepository r: value.getRepositories()) {
        repoSrlzr.serialize(r, gen, serializers);
      }
      gen.writeEndArray();
      
      gen.writeEndObject();
    }
  }
    
  private static class ImportRepositorySerializer extends JsonSerializer<ImportRepository> {

    @Override
    public void serialize(ImportRepository value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException,
        JsonProcessingException {
      gen.writeStartObject();
      
      gen.writeStringField("path", value.getPath());
      gen.writeStringField("description", value.getDescription());
      gen.writeStringField("name", value.getName());
      
      gen.writeArrayFieldStart("files");
      final JsonSerializer<Object> fileSrlzr = serializers.findValueSerializer(ImportFile.class);
      
      for (ImportFile f: value.getFiles()) {
        fileSrlzr.serialize(f, gen, serializers);
      }
      gen.writeEndArray();
      
      gen.writeEndObject();
    }
  }
  
  private static class ImportFileSerializer extends JsonSerializer<ImportFile> {

    @Override
    public void serialize(ImportFile value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException,
        JsonProcessingException {
      gen.writeStartObject();
      
      gen.writeStringField("path", value.getPath());
      gen.writeStringField("type", value.getImportType().getName());
      gen.writeBooleanField("successful", value.getSuccessful());
      gen.writeStringField("message", value.getMessage());
      
      gen.writeEndObject();
    }
  }
}

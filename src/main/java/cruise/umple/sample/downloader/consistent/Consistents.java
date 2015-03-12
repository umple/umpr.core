/**
 * 
 */
package cruise.umple.sample.downloader.consistent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
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
  
  public static String toJson(ImportRepositorySet repository) {
    ObjectMapper mapper = new ObjectMapper();
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      mapper.writeValue(baos, repository);
      
      return baos.toString();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }    
  }
}

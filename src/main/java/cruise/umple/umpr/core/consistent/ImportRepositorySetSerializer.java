package cruise.umple.umpr.core.consistent;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

public class ImportRepositorySetSerializer implements JsonSerializable {

  @Override
  public void serialize(JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void serializeWithType(JsonGenerator gen,
      SerializerProvider serializers, TypeSerializer typeSer)
      throws IOException {
    // TODO Auto-generated method stub

  }

}

package org.codehaus.enunciate.modules.amf;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Input;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * An AMF data reader that assumes Enunciate-generated JAXB beans.
 *
 * @author Ryan Heaton
 */
public class EnunciateAMFDataReader implements AMFDataReader {

  public boolean isReadable(Class realType, Type genericType) {
    AMFMapper mapper = AMFMapperIntrospector.getAMFMapper(realType, genericType);
    if (mapper instanceof CustomAMFMapper) {
      return realType.isAssignableFrom(((CustomAMFMapper) mapper).getJaxbClass());
    }
    else {
      return ((mapper instanceof CollectionAMFMapper) || (mapper instanceof MapAMFMapper));
    }    
  }

  public Object readFrom(Class realType, Type genericType, InputStream stream) throws IOException {
    AMFMapper mapper = AMFMapperIntrospector.getAMFMapper(realType, genericType);
    SerializationContext context = new SerializationContext();
    Amf3Input input = new Amf3Input(context);
    input.setInputStream(stream);
    try {
      return mapper.toJAXB(input.readObject(), new AMFMappingContext());
    }
    catch (ClassNotFoundException e) {
      throw new IOException("Invalid request: " + e.getMessage());
    }
  }
}

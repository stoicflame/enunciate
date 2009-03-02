package org.codehaus.enunciate.modules.amf;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Output;

import java.lang.reflect.Type;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An AMF data reader that assumes Enunciate-generated JAXB beans.
 *
 * @author Ryan Heaton
 */
public class EnunciateAMFDataWriter implements AMFDataWriter {

  public boolean isWriteable(Class realType, Type genericType) {
    AMFMapper mapper = AMFMapperIntrospector.getAMFMapper(realType, genericType);
    if (mapper instanceof CustomAMFMapper) {
      return ((CustomAMFMapper) mapper).getJaxbClass().isAssignableFrom(realType);
    }
    else {
      return ((mapper instanceof CollectionAMFMapper) || (mapper instanceof MapAMFMapper));
    }
  }

  public void writeTo(Object obj, Class realType, Type genericType, OutputStream stream) throws IOException {
    AMFMapper mapper = AMFMapperIntrospector.getAMFMapper(realType, genericType);
    SerializationContext context = new SerializationContext();
    Amf3Output output = new Amf3Output(context);
    output.setOutputStream(stream);
    output.writeObject(mapper.toAMF(obj, new AMFMappingContext()));
  }
}

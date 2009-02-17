package org.codehaus.enunciate.modules.amf;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Input;
import flex.messaging.io.amf.Amf3Output;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * A JAX-RS provider for data that is serialized/deserialized according to the AMF specification for serialization of objects.
 * E.g. mime type "application/x-amf". 
 *
 * @author Ryan Heaton
 */
@Provider
@Produces ("application/x-amf")
public class JAXRSProvider implements MessageBodyReader, MessageBodyWriter {

  public boolean isReadable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return true;
  }

  public Object readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, InputStream entityStream)
    throws IOException, WebApplicationException {
    AMFMapper mapper = AMFMapperIntrospector.getAMFMapper(type, genericType);
    SerializationContext context = new SerializationContext();
    Amf3Input input = new Amf3Input(context);
    input.setInputStream(entityStream);
    try {
      return mapper.toJAXB(input.readObject(), new AMFMappingContext());
    }
    catch (ClassNotFoundException e) {
      throw new WebApplicationException(Response.status(400).entity("Invalid request: " + e.getMessage()).build());
    }
  }

  public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return true;
  }

  public void writeTo(Object o, Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    AMFMapper mapper = AMFMapperIntrospector.getAMFMapper(type, genericType);
    SerializationContext context = new SerializationContext();
    Amf3Output output = new Amf3Output(context);
    output.setOutputStream(entityStream);
    output.writeObject(mapper.toAMF(o, new AMFMappingContext()));
  }

  public long getSize(Object o, Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

}

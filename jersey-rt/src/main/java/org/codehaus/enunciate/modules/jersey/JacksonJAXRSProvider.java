package org.codehaus.enunciate.modules.jersey;

import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.ext.*;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Context;
import java.lang.reflect.Type;
import java.lang.annotation.Annotation;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
@Provider
@Produces ( "application/json" )
@Consumes ( "application/json" )
public class JacksonJAXRSProvider implements MessageBodyReader, MessageBodyWriter {

  private final ContextResolver<ObjectMapper> objectMapperResolver;

  public JacksonJAXRSProvider(@Context Providers providers) {
    ContextResolver<ObjectMapper> contextResolver = providers.getContextResolver(ObjectMapper.class, null);
    if (contextResolver == null) {
      contextResolver = new SingleContextResolver();
    }
    this.objectMapperResolver = contextResolver;
  }

  public boolean isReadable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return true; //todo: is anything NOT readable?
  }

  public Object readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
    ObjectMapper mapper = this.objectMapperResolver.getContext(type);
    return mapper.readValue(entityStream, type);
  }

  public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return true; //todo: is anything NOT writeable?
  }

  public void writeTo(Object o, Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    ObjectMapper mapper = this.objectMapperResolver.getContext(type);
    mapper.writeValue(entityStream, o);
  }

  public long getSize(Object o, Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    //size is unknown (?)
    return -1;
  }

  /**
   * Default context resolver.  Just returns a default singleton instance.
   */
  public static class SingleContextResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper singleton = new ObjectMapper();

    public ObjectMapper getContext(Class<?> type) {
      return singleton;
    }

  }

}

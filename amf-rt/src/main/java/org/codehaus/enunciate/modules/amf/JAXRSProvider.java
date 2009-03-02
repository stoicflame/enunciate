package org.codehaus.enunciate.modules.amf;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
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
@Consumes ("application/x-amf")
public class JAXRSProvider implements MessageBodyReader, MessageBodyWriter {

  private AMFDataReader reader = new EnunciateAMFDataReader();
  private AMFDataWriter writer = new EnunciateAMFDataWriter();

  public boolean isReadable(Class realType, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return this.reader.isReadable(realType, genericType);
  }

  public Object readFrom(Class realType, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, InputStream stream)
    throws IOException, WebApplicationException {
    return this.reader.readFrom(realType, genericType, stream);
  }

  public boolean isWriteable(Class realType, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return this.writer.isWriteable(realType, genericType);
  }

  public void writeTo(Object o, Class realType, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, OutputStream stream) throws IOException, WebApplicationException {
    this.writer.writeTo(o, realType, genericType, stream);
  }

  public long getSize(Object o, Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  public AMFDataReader getReader() {
    return reader;
  }

  public void setReader(AMFDataReader reader) {
    this.reader = reader;
  }

  public AMFDataWriter getWriter() {
    return writer;
  }

  public void setWriter(AMFDataWriter writer) {
    this.writer = writer;
  }
}

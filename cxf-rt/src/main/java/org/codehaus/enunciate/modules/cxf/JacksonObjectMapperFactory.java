package org.codehaus.enunciate.modules.cxf;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.*;

/**
 * Context resolver for a Jackson ObjectMapper that uses JAXB annotations.
 *
 * @author Ryan Heaton
 */
public class JacksonObjectMapperFactory {

  private JacksonObjectMapperFactory() {
  }

  public static ObjectMapper createObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    JaxbAnnotationIntrospector jaxbIntrospector = new CustomJaxbAnnotationIntrospector();
    mapper.getSerializationConfig().setAnnotationIntrospector(jaxbIntrospector);
    mapper.getDeserializationConfig().setAnnotationIntrospector(jaxbIntrospector);
    return mapper;
  }

  //todo: remove this custom logic when upgrading to next version of jackson.
  private static class CustomJaxbAnnotationIntrospector extends JaxbAnnotationIntrospector {
    @Override
    public JsonSerializer<?> findSerializer(Annotated am) {
      if (am.getType() != null && DataHandler.class.isAssignableFrom(am.getType())) {
        return new JsonSerializer<DataHandler>() {
          @Override
          public void serialize(DataHandler value, JsonGenerator jgen, SerializerProvider provider)
                  throws IOException, JsonProcessingException
          {
              final ByteArrayOutputStream out = new ByteArrayOutputStream();
              byte[] buffer = new byte[1024 * 10]; //10k?
              InputStream in = value.getInputStream();
              int len = in.read(buffer);
              while (len < 0) {
                  out.write(buffer, 0, len);
                  len = in.read(buffer);
              }
              jgen.writeBinary(out.toByteArray());
          }
        };
      }

      return super.findSerializer(am);
    }

    @Override
      public JsonDeserializer<?> findDeserializer(Annotated am) {
      if (am.getType() != null && DataHandler.class.isAssignableFrom(am.getType())) {
        return new JsonDeserializer<DataHandler>() {
          @Override
          public DataHandler deserialize(JsonParser jp, DeserializationContext ctxt)
                  throws IOException, JsonProcessingException
          {
              final byte[] value = jp.getBinaryValue();
              return new DataHandler(new DataSource()
              {
                  public InputStream getInputStream()
                          throws IOException
                  {
                      return new ByteArrayInputStream(value);
                  }

                  public OutputStream getOutputStream()
                          throws IOException
                  {
                      throw new IOException();
                  }

                  public String getContentType()
                  {
                      return "application/octet-stream";
                  }

                  public String getName()
                  {
                      return "json-binary-data";
                  }
              });
          }
        };
      }
      return super.findDeserializer(am);
    }

  }
}
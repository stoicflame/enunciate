package org.codehaus.enunciate.modules.jersey;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Context resolver for a Jackson ObjectMapper that uses JAXB annotations.
 * 
 * @author Ryan Heaton
 */
@Provider
public class JacksonObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

  private final ObjectMapper commonMapper;

  public JacksonObjectMapperContextResolver() {
    this.commonMapper = new ObjectMapper();
    JaxbAnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector();
    this.commonMapper.getSerializationConfig().setAnnotationIntrospector(jaxbIntrospector);
    this.commonMapper.getDeserializationConfig().setAnnotationIntrospector(jaxbIntrospector);
  }

  public ObjectMapper getContext(Class<?> type) {
    return this.commonMapper;
  }

}

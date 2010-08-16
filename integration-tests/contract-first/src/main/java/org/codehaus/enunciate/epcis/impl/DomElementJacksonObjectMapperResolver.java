package org.codehaus.enunciate.epcis.impl;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.CustomDeserializerFactory;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.w3c.dom.Element;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * @author Ryan Heaton
 */
@Provider
public class DomElementJacksonObjectMapperResolver implements ContextResolver<ObjectMapper> {
  private final ObjectMapper commonMapper;

  public DomElementJacksonObjectMapperResolver() {
    this.commonMapper = new ObjectMapper();
    CustomSerializerFactory sf = new CustomSerializerFactory();
    sf.addGenericMapping(Element.class, new DomElementJsonSerializer());
    CustomDeserializerFactory df = new CustomDeserializerFactory();
    df.addSpecificMapping(Element.class, new DomElementJsonDeserializer());
    this.commonMapper.setSerializerFactory(sf);
    this.commonMapper.setDeserializerProvider(new StdDeserializerProvider(df));
  }

  public ObjectMapper getContext(Class<?> type) {
    return this.commonMapper;
  }

}

package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;

/**
 * @author Ryan Heaton
 */
public class GenealogyAPI extends ResourceConfig {

  public GenealogyAPI() {
    packages(GenealogyAPI.class.getPackage().getName(), "com.webcohesion.enunciate.rt");
    register(MultiPartFeature.class);
    register(JacksonJsonProvider.class);
    property(ServletProperties.FILTER_FORWARD_ON_404, true);
  }
}

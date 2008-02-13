package org.codehaus.enunciate.modules.rest;

import org.springframework.web.multipart.MultipartResolver;
import org.codehaus.enunciate.rest.annotations.VerbType;

/**
 * A factory for a multipart resolver.
 * 
 * @author Ryan Heaton
 */
public interface MultipartResolverFactory {

  /**
   * Get the multipart resolver for the given REST resource.
   *
   * @param nounContext The noun context of the rest resource.
   * @param noun The noun of the rest resource.
   * @param verb The verb for the operation for which the multipart resolver will be applied.
   * @return The multipart resolver.
   */
  MultipartResolver getMultipartResolver(String nounContext, String noun, VerbType verb);
}

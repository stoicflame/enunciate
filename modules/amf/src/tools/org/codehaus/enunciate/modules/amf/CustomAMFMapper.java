package org.codehaus.enunciate.modules.amf;

/**
 * AMF mapper that maps a custom type, e.g. one that AMF doesn't inherently support.
 * 
 * @author Ryan Heaton
 */
public interface CustomAMFMapper<J, G> extends AMFMapper<J, G> {

  /**
   * The JAXB class supported by this mapper.
   *
   * @return The JAXB class supported by this mapper.
   */
  Class<? extends J> getJaxbClass();

  /**
   * The AMF class supported by this mapper.
   *
   * @return The AMF class supported by this mapper.
   */
  Class<? extends G> getAmfClass();
}

package org.codehaus.enunciate.modules.gwt;

/**
 * GWT mapper that maps a custom type, e.g. one that GWT doesn't inherently support.
 *
 * @author Ryan Heaton
 */
public interface CustomGWTMapper<J, G> extends GWTMapper<J, G> {

  /**
   * The JAXB class supported by this mapper.
   *
   * @return The JAXB class supported by this mapper.
   */
  Class<? extends J> getJaxbClass();

  /**
   * The GWT class supported by this mapper.
   *
   * @return The GWT class supported by this mapper.
   */
  Class<? extends G> getGwtClass();
}
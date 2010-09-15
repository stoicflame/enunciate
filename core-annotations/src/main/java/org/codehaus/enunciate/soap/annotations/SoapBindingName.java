package org.codehaus.enunciate.soap.annotations;

/**
 * Annotation for configuring the soap binding name. This isn't really necessary; it's more for aesthetic purposes.
 *
 * @author Ryan Heaton
 */
public @interface SoapBindingName {

  /**
   * The soap binding name.
   *
   * @return The soap binding name.
   */
  String value();
}

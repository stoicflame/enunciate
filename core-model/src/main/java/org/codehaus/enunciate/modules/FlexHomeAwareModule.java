package org.codehaus.enunciate.modules;

/**
 * @author Ryan Heaton
 */
public interface FlexHomeAwareModule {

  /**
   * Set the Flex home directory.
   *
   * @param flexHome The Flex home directory.
   */
  void setFlexHome(String flexHome);
}
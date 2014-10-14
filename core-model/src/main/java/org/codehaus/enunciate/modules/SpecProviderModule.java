package org.codehaus.enunciate.modules;

/**
 * Interface for deployment modules that are providers for a spec.
 *
 * @author Ryan Heaton
 */
public interface SpecProviderModule {

  /**
   * Whether this module is a JAX-WS provider module.
   *
   * @return Whether this module is a JAX-WS provider module.
   */
  boolean isJaxwsProvider();

  /**
   * Whether this module is a JAX-RS provider module.
   *
   * @return Whether this module is a JAX-RS provider module.
   */
  boolean isJaxrsProvider();
}

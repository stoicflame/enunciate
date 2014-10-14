package org.codehaus.enunciate.modules;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface FacetAware extends DeploymentModule {

  /**
   * The set of facets to include.
   *
   * @return The set of facets to include.
   */
  Set<String> getFacetIncludes();

  /**
   * The set of facets to exclude.
   *
   * @return The set of facets to exclude.
   */
  Set<String> getFacetExcludes();

}

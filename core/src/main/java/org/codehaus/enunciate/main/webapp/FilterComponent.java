package org.codehaus.enunciate.main.webapp;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class FilterComponent extends WebAppComponent {

  private Set<String> dispatchers;
  private Set<String> servletNames;

  /**
   * The list of dispatchers to apply the filter component.
   *
   * @return The list of dispatchers to apply the filter component.
   */
  public Set<String> getDispatchers() {
    return dispatchers;
  }

  /**
   * The list of dispatchers to apply the filter component.
   *
   * @param dispatchers The list of dispatchers to apply the filter component.
   */
  public void setDispatchers(Set<String> dispatchers) {
    this.dispatchers = dispatchers;
  }

  /**
   * The servlet names to which the filter is applicable.
   *
   * @return The servlet names to which the filter is applicable.
   */
  public Set<String> getServletNames() {
    return servletNames;
  }

  /**
   * The servlet names to which the filter is applicable.
   *
   * @param servletNames The servlet names to which the filter is applicable.
   */
  public void setServletNames(Set<String> servletNames) {
    this.servletNames = servletNames;
  }
}

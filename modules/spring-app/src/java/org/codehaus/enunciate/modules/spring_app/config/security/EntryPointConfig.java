package org.codehaus.enunciate.modules.spring_app.config.security;

/**
 * Configuration for a security entry point.
 *
 * @author Ryan Heaton
 */
public class EntryPointConfig {

  private String redirectTo;
  private BeanReference useEntryPoint;

  /**
   * The entry point should redirect to the specified URL.
   * 
   * @return The entry point should redirect to the specified URL.
   */
  public String getRedirectTo() {
    return redirectTo;
  }

  /**
   * The entry point should redirect to the specified URL.
   *
   * @param redirectTo The entry point should redirect to the specified URL.
   */
  public void setRedirectTo(String redirectTo) {
    this.redirectTo = redirectTo;
  }

  /**
   * The bean to use as the entry point.
   *
   * @return The bean to use as the entry point.
   */
  public BeanReference getUseEntryPoint() {
    return useEntryPoint;
  }

  /**
   * The bean to use as the entry point.
   *
   * @param useEntryPoint The bean to use as the entry point.
   */
  public void setUseEntryPoint(BeanReference useEntryPoint) {
    this.useEntryPoint = useEntryPoint;
  }
}

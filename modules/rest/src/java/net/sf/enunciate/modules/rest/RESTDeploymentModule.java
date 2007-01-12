package net.sf.enunciate.modules.rest;

import net.sf.enunciate.modules.BasicDeploymentModule;
import net.sf.enunciate.contract.validation.Validator;

/**
 * The deployment module for the REST API.
 *
 * @author Ryan Heaton
 */
public class RESTDeploymentModule extends BasicDeploymentModule {

  /**
   * @return "rest"
   */
  @Override
  public String getName() {
    return "rest";
  }

  /**
   * @return A new {@link net.sf.enunciate.modules.rest.RESTValidator}.
   */
  @Override
  public Validator getValidator() {
    return new RESTValidator();
  }

}

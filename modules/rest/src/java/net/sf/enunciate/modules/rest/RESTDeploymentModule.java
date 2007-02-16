package net.sf.enunciate.modules.rest;

import net.sf.enunciate.modules.BasicDeploymentModule;
import net.sf.enunciate.contract.validation.Validator;

/**
 * <h1>Introduction</h1>
 *
 * <p>The REST deployment module exists only as a set of tools used to deploy the REST API.  For more information, see the
 * Javadoc API for the REST tools.</p>.
 *
 * <p>The order of the REST deployment module is 0, as it doesn't depend on any artifacts exported
 * by any other module.</p>
 *
 * <ul>
 *   <li><a href="#steps">steps</a></li>
 *   <li><a href="#config">configuration</a></li>
 *   <li><a href="#artifacts">artifacts</a></li>
 * </ul>
 *
 * <h1><a name="steps">Steps</a></h1>
 *
 * <p>There are no significant steps in the REST module.  </p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>There are no configuration options for the REST deployment module.</p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The REST deployment module exports no artifacts.</p>
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

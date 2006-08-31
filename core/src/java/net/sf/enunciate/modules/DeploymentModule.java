package net.sf.enunciate.modules;

import net.sf.enunciate.main.Enunciate;
import org.apache.commons.digester.RuleSet;

import java.io.IOException;

/**
 * Interface for a deployment module.  A deployment module for a specific platform implements logic for each
 * enunciate step.
 *
 * @author Ryan Heaton
 */
public interface DeploymentModule extends Comparable<DeploymentModule> {

  /**
   * The name of the deployment module.  Along with the {@link #getNamespace(), namespace} identifies
   * its section in the enunciate configuration.
   *
   * @return The name of the deployment module.
   */
  String getName();

  /**
   * The namespace for this deployment module.  Along with the {@link #getName(), name} identifies
   * its section in the enunciate configuration.
   *
   * @return The namespace for this module.
   */
  String getNamespace();

  /**
   * Initialize this deployment module with the specified enunciate mechanism.
   *
   * @param enunciate The enunciate mechanism.
   */
  void init(Enunciate enunciate);

  /**
   * Step to the next enunciate target.
   *
   * @param target The enunciate target to step to.
   */
  void step(Enunciate.Target target) throws IOException;

  /**
   * Close this enunciate module.
   */
  void close();

  /**
   * The configuration rules for this deployment module, or null if none.
   *
   * @return The configuration rules for this deployment module.
   */
  RuleSet getConfigurationRules();

  /**
   * The comparison of deployment modules determines their invocation order.
   *
   * @param module The module to compare to.
   * @return a negative integer, zero, or a positive integer as this object
   *         comes before, is equal to, or comes after another deployment module.
   */
  int compareTo(DeploymentModule module);

}

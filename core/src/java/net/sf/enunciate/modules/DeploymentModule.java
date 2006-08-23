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
public interface DeploymentModule {

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

}

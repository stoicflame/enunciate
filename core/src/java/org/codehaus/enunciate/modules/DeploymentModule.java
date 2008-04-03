/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules;

import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.Enunciate;
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
   * The name of the deployment module.  Identifies its section in the enunciate configuration.
   *
   * @return The name of the deployment module.
   */
  String getName();

  /**
   * Get the validator for this module, or null if none.
   *
   * @return The validator.
   */
  Validator getValidator();

  /**
   * Initialize this deployment module with the specified enunciate mechanism.
   *
   * @param enunciate The enunciate mechanism.
   */
  void init(Enunciate enunciate) throws EnunciateException;

  /**
   * Step to the next enunciate target.
   *
   * @param target The enunciate target to step to.
   */
  void step(Enunciate.Target target) throws EnunciateException, IOException;

  /**
   * Close this enunciate module.
   */
  void close() throws EnunciateException;

  /**
   * The configuration rules for this deployment module, or null if none.
   *
   * @return The configuration rules for this deployment module.
   */
  RuleSet getConfigurationRules();

  /**
   * The order of execution for the deployment module.
   *
   * @return The order of execution for the deployment module.
   */
  int getOrder();

  /**
   * Whether this deployment module has been disabled, e.g. in the config file.  Since the
   * discovery mechanism is used to discover the modules on the classpath, it may be necessary
   * to be able to disable a module.
   *
   * @return Whether this deployment module has been disabled.
   */
  boolean isDisabled();
}

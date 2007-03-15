/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.config;

import org.codehaus.enunciate.modules.DeploymentModule;

import java.util.Comparator;

/**
 * Compares deployment modules for the order in which they should be executed.
 *
 * @author Ryan Heaton
 */
public class DeploymentModuleComparator implements Comparator<DeploymentModule> {

  /**
   * Compares modules by order, then by name.
   *
   * @param module1 The first module.
   * @param module2 The second module.
   * @return The comparison.
   */
  public int compare(DeploymentModule module1, DeploymentModule module2) {
    int comparison = module1.getOrder() - module2.getOrder();

    if (comparison == 0) {
      String name1 = module1.getName() == null ? "" : module1.getName();
      String name2 = module2.getName() == null ? "" : module2.getName();
      comparison = name1.compareTo(name2);
    }

    return comparison;
  }
}

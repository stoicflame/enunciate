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

package org.codehaus.enunciate.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.digester.RuleSetBase;
import org.codehaus.enunciate.modules.BasicDeploymentModule;

import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
public class DeploymentModuleOne extends BasicDeploymentModule {

  String attribute;
  final HashMap<String, String> elementMap = new HashMap<String, String>();

  @Override
  public String getName() {
    return "module1";
  }

  public String getAttribute() {
    return attribute;
  }

  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }

  public void putElement(String elementName, String elementValue) {
    this.elementMap.put(elementName, elementValue);
  }

  @Override
  public RuleSet getConfigurationRules() {
    return new RuleSetBase() {
      public void addRuleInstances(Digester digester) {
        digester.addCallMethod("enunciate/modules/module1/element", "putElement", 2);
        digester.addCallParam("enunciate/modules/module1/element", 0, "name");
        digester.addCallParam("enunciate/modules/module1/element", 1);
      }
    };
  }

}

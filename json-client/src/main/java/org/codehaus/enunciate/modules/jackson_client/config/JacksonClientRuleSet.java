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

package org.codehaus.enunciate.modules.jackson_client.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

/**
 * Rules for the configuration of the JAX-WS client module.
 *
 * @author Ryan Heaton
 */
public class JacksonClientRuleSet extends RuleSetBase {

  public void addRuleInstances(Digester digester) {
    //allow client package conversions to be configured.
    digester.addObjectCreate("enunciate/modules/jackson-client/package-conversions/convert", org.codehaus.enunciate.modules.jackson_client.config.ClientPackageConversion.class);
    digester.addSetProperties("enunciate/modules/jackson-client/package-conversions/convert");
    digester.addSetNext("enunciate/modules/jackson-client/package-conversions/convert", "addClientPackageConversion");

    digester.addCallMethod("enunciate/modules/jackson-client/server-side-type", "addServerSideTypeToUse", 1);
    digester.addCallParam("enunciate/modules/jackson-client/server-side-type", 0, "pattern");
  }
}
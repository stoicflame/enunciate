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

package org.codehaus.enunciate.modules.xfire.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

/**
 * Rules for the configuration of the XFire client module.
 *
 * @author Ryan Heaton
 */
public class XFireRuleSet extends RuleSetBase {

  public void addRuleInstances(Digester digester) {
    //allow the war file to be created.
    digester.addObjectCreate("enunciate/modules/xfire/war", WarConfig.class);
    digester.addSetProperties("enunciate/modules/xfire/war");
    digester.addSetNext("enunciate/modules/xfire/war", "setWarConfig");

    digester.addObjectCreate("enunciate/modules/xfire/war/lib", WarLib.class);
    digester.addSetProperties("enunciate/modules/xfire/war/lib");
    digester.addSetNext("enunciate/modules/xfire/war/lib", "addWarLib");

    digester.addObjectCreate("enunciate/modules/xfire/war/excludeJar", ExcludeJars.class);
    digester.addSetProperties("enunciate/modules/xfire/war/excludeJar");
    digester.addSetNext("enunciate/modules/xfire/war/excludeJar", "addExcludeJars");

    digester.addObjectCreate("enunciate/modules/xfire/springImport", SpringImport.class);
    digester.addSetProperties("enunciate/modules/xfire/springImport");
    digester.addSetNext("enunciate/modules/xfire/springImport", "addSpringImport");

    digester.addObjectCreate("enunciate/modules/xfire/copyResources", CopyResources.class);
    digester.addSetProperties("enunciate/modules/xfire/copyResources");
    digester.addSetNext("enunciate/modules/xfire/copyResources", "addCopyResources");
  }

}

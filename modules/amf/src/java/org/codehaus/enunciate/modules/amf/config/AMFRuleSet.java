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

package org.codehaus.enunciate.modules.amf.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

/**
 * Rules for the configuration of the gwt module.
 *
 * @author Ryan Heaton
 */
public class AMFRuleSet extends RuleSetBase {

  public void addRuleInstances(Digester digester) {
    digester.addCallMethod("enunciate/modules/amf/gwtCompileJVMArg", "addGwtCompileJVMArg", 1);
    digester.addCallParam("enunciate/modules/amf/gwtCompileJVMArg", 0, "value");

    digester.addObjectCreate("enunciate/modules/amf/app", AMFApp.class);
    digester.addSetProperties("enunciate/modules/amf/app",
                              new String[] {"name", "javascriptStyle", "srcDir"},
                              new String[] {"name", "javascriptStyleValue", "srcDir"} );
    digester.addSetNext("enunciate/modules/amf/app", "addGWTApp");
  }
}

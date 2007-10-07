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

package org.codehaus.enunciate.modules.spring_app.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

/**
 * Rules for the configuration of the XFire client module.
 *
 * @author Ryan Heaton
 */
public class SpringAppRuleSet extends RuleSetBase {

  public void addRuleInstances(Digester digester) {
    //allow the war file to be created.
    digester.addObjectCreate("enunciate/modules/spring-app/war", WarConfig.class);
    digester.addSetProperties("enunciate/modules/spring-app/war");
    digester.addSetNext("enunciate/modules/spring-app/war", "setWarConfig");

    digester.addObjectCreate("enunciate/modules/spring-app/war/lib", WarLib.class);
    digester.addSetProperties("enunciate/modules/spring-app/war/lib");
    digester.addSetNext("enunciate/modules/spring-app/war/lib", "addWarLib");

    digester.addObjectCreate("enunciate/modules/spring-app/war/excludeJar", IncludeExcludeLibs.class);
    digester.addSetProperties("enunciate/modules/spring-app/war/excludeJar");
    digester.addSetNext("enunciate/modules/spring-app/war/excludeJar", "addExcludeLibs");

    digester.addObjectCreate("enunciate/modules/spring-app/war/excludeLibs", IncludeExcludeLibs.class);
    digester.addSetProperties("enunciate/modules/spring-app/war/excludeLibs");
    digester.addSetNext("enunciate/modules/spring-app/war/excludeLibs", "addExcludeLibs");

    digester.addObjectCreate("enunciate/modules/spring-app/war/includeLibs", IncludeExcludeLibs.class);
    digester.addSetProperties("enunciate/modules/spring-app/war/includeLibs");
    digester.addSetNext("enunciate/modules/spring-app/war/includeLibs", "addIncludeLibs");

    digester.addCallMethod("enunciate/modules/spring-app/war/manifest/attribute", "addManifestAttribute", 3);
    digester.addCallParam("enunciate/modules/spring-app/war/manifest/attribute", 0, "section");
    digester.addCallParam("enunciate/modules/spring-app/war/manifest/attribute", 1, "name");
    digester.addCallParam("enunciate/modules/spring-app/war/manifest/attribute", 2, "value");

    digester.addObjectCreate("enunciate/modules/spring-app/springImport", SpringImport.class);
    digester.addSetProperties("enunciate/modules/spring-app/springImport");
    digester.addSetNext("enunciate/modules/spring-app/springImport", "addSpringImport");

    digester.addObjectCreate("enunciate/modules/spring-app/copyResources", CopyResources.class);
    digester.addSetProperties("enunciate/modules/spring-app/copyResources");
    digester.addSetNext("enunciate/modules/spring-app/copyResources", "addCopyResources");

    digester.addObjectCreate("enunciate/modules/spring-app/globalServiceInterceptor", GlobalServiceInterceptor.class);
    digester.addSetProperties("enunciate/modules/spring-app/globalServiceInterceptor");
    digester.addSetNext("enunciate/modules/spring-app/globalServiceInterceptor", "addGlobalServiceInterceptor");

    digester.addObjectCreate("enunciate/modules/spring-app/handlerInterceptor", HandlerInterceptor.class);
    digester.addSetProperties("enunciate/modules/spring-app/handlerInterceptor");
    digester.addSetNext("enunciate/modules/spring-app/handlerInterceptor", "addHandlerInterceptor");
  }

}

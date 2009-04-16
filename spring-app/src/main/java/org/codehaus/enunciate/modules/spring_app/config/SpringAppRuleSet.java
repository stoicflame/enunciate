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

package org.codehaus.enunciate.modules.spring_app.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;
import org.codehaus.enunciate.modules.spring_app.config.security.*;
import org.codehaus.enunciate.main.webapp.WebAppComponent;

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

    digester.addObjectCreate("enunciate/modules/spring-app/war/resource-env-ref", WebAppResource.class);
    digester.addSetProperties("enunciate/modules/spring-app/war/resource-env-ref");
    digester.addSetNext("enunciate/modules/spring-app/war/resource-env-ref", "addResourceEnvRef");

    digester.addObjectCreate("enunciate/modules/spring-app/war/resource-ref", WebAppResource.class);
    digester.addSetProperties("enunciate/modules/spring-app/war/resource-ref");
    digester.addSetNext("enunciate/modules/spring-app/war/resource-ref", "addResourceRef");

    digester.addObjectCreate("enunciate/modules/spring-app/war/env", WebAppResource.class);
    digester.addSetProperties("enunciate/modules/spring-app/war/env");
    digester.addSetNext("enunciate/modules/spring-app/war/env", "addEnvEntry");

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

    digester.addObjectCreate("enunciate/modules/spring-app/globalServletFilter", WebAppComponent.class);
    digester.addSetProperties("enunciate/modules/spring-app/globalServletFilter");
    digester.addSetNext("enunciate/modules/spring-app/globalServletFilter", "addGlobalServletFilter");

    digester.addCallMethod("enunciate/modules/spring-app/globalServletFilter/init-param", "addInitParam", 2);
    digester.addCallParam("enunciate/modules/spring-app/globalServletFilter/init-param", 0, "name");
    digester.addCallParam("enunciate/modules/spring-app/globalServletFiltery/init-param", 1, "value");

    digester.addObjectCreate("enunciate/modules/spring-app/handlerInterceptor", HandlerInterceptor.class);
    digester.addSetProperties("enunciate/modules/spring-app/handlerInterceptor");
    digester.addSetNext("enunciate/modules/spring-app/handlerInterceptor", "addHandlerInterceptor");

    //security configuration.
    digester.addObjectCreate("enunciate/modules/spring-app/security", SecurityConfig.class);
    digester.addSetProperties("enunciate/modules/spring-app/security");
    digester.addSetNext("enunciate/modules/spring-app/security", "setSecurityConfig");

    digester.addCallMethod("enunciate/modules/spring-app/security/url", "addSecureUrl", 2);
    digester.addCallParam("enunciate/modules/spring-app/security/url", 0, "pattern");
    digester.addCallParam("enunciate/modules/spring-app/security/url", 1, "rolesAllowed");

    digester.addObjectCreate("enunciate/modules/spring-app/security/anonymous", AnonymousConfig.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/anonymous");
    digester.addSetNext("enunciate/modules/spring-app/security/anonymous", "setAnonymousConfig");

    digester.addObjectCreate("enunciate/modules/spring-app/security/basicAuth", BasicAuthConfig.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/basicAuth");
    digester.addSetNext("enunciate/modules/spring-app/security/basicAuth", "setBasicAuthConfig");

    digester.addObjectCreate("enunciate/modules/spring-app/security/oauth", OAuthConfig.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/oauth");
    digester.addSetNext("enunciate/modules/spring-app/security/oauth", "setOAuthConfig");

    digester.addObjectCreate("enunciate/modules/spring-app/security/oauth/confirmAccessPageController", BeanReference.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/oauth/confirmAccessPageController");
    digester.addSetNext("enunciate/modules/spring-app/security/oauth/confirmAccessPageController", "setConfirmAccessPageController");

    digester.addObjectCreate("enunciate/modules/spring-app/security/oauth/accessConfirmedPageController", BeanReference.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/oauth/accessConfirmedPageController");
    digester.addSetNext("enunciate/modules/spring-app/security/oauth/accessConfirmedPageController", "setAccessConfirmedPageController");

    digester.addObjectCreate("enunciate/modules/spring-app/security/oauth/tokenServices", BeanReference.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/oauth/tokenServices");
    digester.addSetNext("enunciate/modules/spring-app/security/oauth/tokenServices", "setTokenServices");

    digester.addObjectCreate("enunciate/modules/spring-app/security/oauth/consumerDetailsService", BeanReference.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/oauth/consumerDetailsService");
    digester.addSetNext("enunciate/modules/spring-app/security/oauth/consumerDetailsService", "setConsumerDetailsService");

    digester.addObjectCreate("enunciate/modules/spring-app/security/digestAuth", DigestAuthConfig.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/digestAuth");
    digester.addSetNext("enunciate/modules/spring-app/security/digestAuth", "setDigestAuthConfig");

    digester.addObjectCreate("enunciate/modules/spring-app/security/formBasedLogin", FormBasedLoginConfig.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/formBasedLogin");
    digester.addSetNext("enunciate/modules/spring-app/security/formBasedLogin", "setFormBasedLoginConfig");

    digester.addObjectCreate("enunciate/modules/spring-app/security/formBasedLogin/loginPageController", BeanReference.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/formBasedLogin/loginPageController");
    digester.addSetNext("enunciate/modules/spring-app/security/formBasedLogin/loginPageController", "setLoginPageController");

    digester.addObjectCreate("enunciate/modules/spring-app/security/formBasedLogout", FormBasedLogoutConfig.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/formBasedLogout");
    digester.addSetNext("enunciate/modules/spring-app/security/formBasedLogout", "setFormBasedLogoutConfig");

    digester.addObjectCreate("enunciate/modules/spring-app/security/rememberMe", RememberMeConfig.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/rememberMe");
    digester.addSetNext("enunciate/modules/spring-app/security/rememberMe", "setRememberMeConfig");

    digester.addObjectCreate("enunciate/modules/spring-app/security/onAuthenticationFailed", EntryPointConfig.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/onAuthenticationFailed");
    digester.addSetNext("enunciate/modules/spring-app/security/onAuthenticationFailed", "setOnAuthenticationFailed");

    digester.addObjectCreate("enunciate/modules/spring-app/security/onAuthenticationFailed/entryPoint", BeanReference.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/onAuthenticationFailed/entryPoint");
    digester.addSetNext("enunciate/modules/spring-app/security/onAuthenticationFailed/entryPoint", "setEntryPoint");

    digester.addObjectCreate("enunciate/modules/spring-app/security/onAccessDenied", EntryPointConfig.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/onAccessDenied");
    digester.addSetNext("enunciate/modules/spring-app/security/onAccessDenied", "setOnAccessDenied");

    digester.addObjectCreate("enunciate/modules/spring-app/security/filter", BeanReference.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/filter");
    digester.addSetNext("enunciate/modules/spring-app/security/filter", "addAdditionalAuthenticationFilter");

    digester.addObjectCreate("enunciate/modules/spring-app/security/userDetailsService", BeanReference.class);
    digester.addSetProperties("enunciate/modules/spring-app/security/userDetailsService");
    digester.addSetNext("enunciate/modules/spring-app/security/userDetailsService", "setUserDetailsService");

  }

}

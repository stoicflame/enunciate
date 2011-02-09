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

package org.codehaus.enunciate.modules.jboss;

import com.sun.mirror.declaration.TypeDeclaration;
import freemarker.template.TemplateException;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateClasspathListener;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.config.war.WebAppConfig;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.webapp.BaseWebAppFragment;
import org.codehaus.enunciate.main.webapp.WebAppComponent;
import org.codehaus.enunciate.modules.BasicAppModule;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.SpecProviderModule;
import org.codehaus.enunciate.modules.jboss.config.JBossRuleSet;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <h1>JBoss Module</h1>
 *
 * <p>The JBoss module assembles a JBoss-based server-side application for hosting the WS endpoints.</p>
 *
 * <ul>
 *   <li><a href="#steps">steps</a></li>
 *   <li><a href="#config">configuration</a></li>
 *   <li><a href="#artifacts">artifacts</a></li>
 * </ul>
 *
 * <h1><a name="steps">Steps</a></h1>
 *
 * <h3>generate</h3>
 * 
 * <p>The "generate" step generates the configuration files.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The JBoss module supports the following configuration attributes:</p>
 *
 * <ul>
 *   <li>The "useSubcontext" attribute is used to enable/disable mounting the JAX-RS resources at the rest subcontext. Default: "true".</li>
 * </ul>
 *
 * <p>The JBoss module also supports a list of <tt>option</tt> child elements that each support a 'name' and 'value' attribute. This can be used to configure the RESTEasy
 * mechanism, and the properties will be passed along as context parameters.
 * <a href="http://docs.jboss.org/resteasy/docs/2.0.0.GA/userguide/html/Installation_Configuration.html#d0e72">See the RESTEasy docs for details</a>.</p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The JBoss deployment module exports no artifacts.</p>
 *
 * @author Ryan Heaton
 * @docFileName module_jboss.html
 */
public class JBossDeploymentModule extends FreemarkerDeploymentModule implements EnunciateClasspathListener, SpecProviderModule {

  private boolean useSubcontext = true;
  private boolean jacksonAvailable = false;
  private boolean servletFound = false;
  private final Map<String, String> options = new TreeMap<String, String>();

  /**
   * @return "jboss"
   */
  @Override
  public String getName() {
    return "jboss";
  }

  // Inherited.
  public void onClassesFound(Set<String> classes) {
    jacksonAvailable |= classes.contains("org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider");
    servletFound |= classes.contains(org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher.class.getName());
  }

  // Inherited.
  @Override
  public void initModel(EnunciateFreemarkerModel model) {
    super.initModel(model);

    if (!isDisabled()) {
      for (RootResource resource : model.getRootResources()) {
        for (ResourceMethod resourceMethod : resource.getResourceMethods(true)) {
          Map<String, Set<String>> subcontextsByContentType = new HashMap<String, Set<String>>();
          String subcontext = this.useSubcontext ? getRestSubcontext() : "";
          debug("Resource method %s of resource %s to be made accessible at subcontext \"%s\".",
                resourceMethod.getSimpleName(), resourceMethod.getParent().getQualifiedName(), subcontext);
          subcontextsByContentType.put(null, new TreeSet<String>(Arrays.asList(subcontext)));
          resourceMethod.putMetaData("defaultSubcontext", subcontext);
          resourceMethod.putMetaData("subcontexts", subcontextsByContentType);
        }
      }

      EnunciateConfiguration config = model.getEnunciateConfig();
      for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
        for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
          String path = "/soap/" + ei.getServiceName();
          if (config != null) {
            path = config.getDefaultSoapSubcontext() + '/' + ei.getServiceName();
            if (config.getSoapServices2Paths().containsKey(ei.getServiceName())) {
              path = config.getSoapServices2Paths().get(ei.getServiceName());
            }
          }

          ei.putMetaData("soapPath", path);
        }
      }
      if (!servletFound) {
        warn("The JBoss runtime wasn't found on the Enunciate classpath. This could be fatal to the runtime application.");
      }
    }
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);
    enunciate.getConfig().setForceJAXWSSpecCompliance(true); //make sure the WSDL and client code are JAX-WS-compliant.
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    WebAppConfig webAppConfig = enunciate.getConfig().getWebAppConfig();
    if (webAppConfig == null) {
      webAppConfig = new WebAppConfig();
      enunciate.getConfig().setWebAppConfig(webAppConfig);
    }
    webAppConfig.addWebXmlAttribute("version", "3.0");
    webAppConfig.addWebXmlAttribute("xmlns", "http://java.sun.com/xml/ns/javaee");
    webAppConfig.addWebXmlAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    webAppConfig.addWebXmlAttribute("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd");
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    super.doBuild();

    Enunciate enunciate = getEnunciate();

    File webappDir = getBuildDir();
    webappDir.mkdirs();

    BaseWebAppFragment webappFragment = new BaseWebAppFragment(getName());
    webappFragment.setBaseDir(webappDir);

    ArrayList<WebAppComponent> servlets = new ArrayList<WebAppComponent>();

    for (WsdlInfo wsdlInfo : getModelInternal().getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        String path = (String) ei.getMetaData().get("soapPath");
        WebAppComponent wsComponent = new WebAppComponent();
        wsComponent.setName(ei.getServiceName());
        wsComponent.setClassname(ei.getEndpointImplementations().iterator().next().getQualifiedName());
        wsComponent.setUrlMappings(new TreeSet<String>(Arrays.asList(path)));
        servlets.add(wsComponent);
      }
    }


    WebAppComponent jaxrsServletComponent = new WebAppComponent();
    jaxrsServletComponent.setName("resteasy-jaxrs");
    jaxrsServletComponent.setClassname(org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher.class.getName());
    TreeSet<String> jaxrsUrlMappings = new TreeSet<String>();
    StringBuilder resources = new StringBuilder();
    for (RootResource rootResource : getModel().getRootResources()) {
      if (resources.length() > 0) {
        resources.append(',');
      }
      resources.append(rootResource.getQualifiedName());

      for (ResourceMethod resourceMethod : rootResource.getResourceMethods(true)) {
        String resourceMethodPattern = resourceMethod.getServletPattern();
        for (Set<String> subcontextList : ((Map<String, Set<String>>) resourceMethod.getMetaData().get("subcontexts")).values()) {
          for (String subcontext : subcontextList) {
            String servletPattern;
            if ("".equals(subcontext)) {
              servletPattern = resourceMethodPattern;
            }
            else {
              servletPattern = subcontext + resourceMethodPattern;
            }

            if (jaxrsUrlMappings.add(servletPattern)) {
              debug("Resource method %s of resource %s to be made accessible by servlet pattern %s.",
                    resourceMethod.getSimpleName(), resourceMethod.getParent().getQualifiedName(), servletPattern);
            }
          }
        }
      }
    }

    StringBuilder providers = new StringBuilder();
    for (TypeDeclaration provider : getModel().getJAXRSProviders()) {
      if (providers.length() > 0) {
        providers.append(',');
      }

      providers.append(provider.getQualifiedName());
    }

    if (jacksonAvailable) {
      if (providers.length() > 0) {
        providers.append(',');
      }

      providers.append("org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider");
    }

    if (getEnunciate().isModuleEnabled("amf")) {
      if (providers.length() > 0) {
        providers.append(',');
      }

      providers.append("org.codehaus.enunciate.modules.amf.JAXRSProvider");
    }

    jaxrsServletComponent.setUrlMappings(jaxrsUrlMappings);
    jaxrsServletComponent.addInitParam("resteasy.resources", resources.toString());
    jaxrsServletComponent.addInitParam("resteasy.providers", providers.toString());
    String mappingPrefix = this.useSubcontext ? getRestSubcontext() : "";
    if (!"".equals(mappingPrefix)) {
      jaxrsServletComponent.addInitParam("resteasy.servlet.mapping.prefix", mappingPrefix);
    }
    jaxrsServletComponent.addInitParam("resteasy.scan", "false"); //turn off scanning because we've already done it.
    servlets.add(jaxrsServletComponent);
    webappFragment.setServlets(servlets);

    if (!this.options.isEmpty()) {
      webappFragment.setContextParameters(this.options);
    }

    enunciate.addWebAppFragment(webappFragment);
  }

  @Override
  public Validator getValidator() {
    return new JBossValidator();
  }

  // Inherited.
  public boolean isJaxwsProvider() {
    return true;
  }

  // Inherited.
  public boolean isJaxrsProvider() {
    return true;
  }

  /**
   * Whether to use the REST subcontext.
   *
   * @param useSubcontext Whether to use the REST subcontext.
   */
  public void setUseSubcontext(boolean useSubcontext) {
    this.useSubcontext = useSubcontext;
  }

  protected String getRestSubcontext() {
    String restSubcontext = getEnunciate().getConfig().getDefaultRestSubcontext();
    //todo: override default rest subcontext?
    return restSubcontext;
  }

  public void addOption(String name, String value) {
    this.options.put(name, value);
  }

  @Override
  public RuleSet getConfigurationRules() {
    return new JBossRuleSet();
  }

  // Inherited.
  @Override
  public boolean isDisabled() {
    if (super.isDisabled()) {
      return true;
    }
    else if (getModelInternal() != null) {
      if (getModelInternal().getRootResources().isEmpty()) {
        debug("CXF module is disabled because there are no root resources to process.");
        return true;
      }
    }

    return false;
  }
}

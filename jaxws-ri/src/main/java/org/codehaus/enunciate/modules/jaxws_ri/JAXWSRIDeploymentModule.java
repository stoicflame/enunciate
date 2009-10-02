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

package org.codehaus.enunciate.modules.jaxws_ri;

import freemarker.template.TemplateException;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateClasspathListener;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.webapp.BaseWebAppFragment;
import org.codehaus.enunciate.main.webapp.WebAppComponent;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.SpecProviderModule;
import org.codehaus.enunciate.template.freemarker.ClientClassnameForMethod;
import org.codehaus.enunciate.template.freemarker.ClientPackageForMethod;
import org.codehaus.enunciate.template.freemarker.SimpleNameWithParamsMethod;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * <h1>JAX-WS RI Module</h1>
 *
 * <p>The JAX-WS RI module assembles a JAX-WS RI-based server-side application for hosting the SOAP endpoints.</p>
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
 * <p>The "generate" step generates the necessary configuration files.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The JAX-WS RI module accepts a single parameter, "forceSpringEnabled" that forces spring integration. By default, spring integration will only be enabled
 * if the spring-app module is enabled and the JAX-WS spring components are found on the classpath.</p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The JAX-WS RI deployment module exports no artifacts.</p>
 *
 * @author Ryan Heaton
 * @docFileName module_jaxws_ri.html
 */
public class JAXWSRIDeploymentModule extends FreemarkerDeploymentModule implements SpecProviderModule {

  private boolean springModuleEnabled = false;
  private boolean forceSpringEnabled = false;

  /**
   * @return "cxf"
   */
  @Override
  public String getName() {
    return "jaxws-ri";
  }

  /**
   * @return The URL to "jaxws-servlet.xml.fmt"
   */
  protected URL getJAXWSSpringTemplateURL() {
    return JAXWSRIDeploymentModule.class.getResource("jaxws-servlet.xml.fmt");
  }

  /**
   * @return The URL to "jaxws-servlet.xml.fmt"
   */
  protected URL getSunJAXWSTemplateURL() {
    return JAXWSRIDeploymentModule.class.getResource("sun-jaxws.xml.fmt");
  }

  /**
   * @return The URL to "jaxws-ri-endpoint.fmt"
   */
  protected URL getInstrumentedEndpointTemplateURL() {
    return JAXWSRIDeploymentModule.class.getResource("jaxws-ri-endpoint.fmt");
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    if (!isDisabled()) {
      if (!enunciate.isModuleEnabled("jaxws-support")) {
        throw new EnunciateException("The JAX-WS RI module requires an enabled JAX-WS Support module.");
      }

      this.springModuleEnabled = enunciate.isModuleEnabled("spring-app");
    }
  }

  // Inherited.
  @Override
  public void initModel(EnunciateFreemarkerModel model) {
    super.initModel(model);

    if (!isDisabled()) {
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
    }
  }

  /**
   * Spring is enabled if the spring-app module is enabled AND the spring runtime servlet is on the Enunciate classpath.
   *
   * @return Whether spring is enabled.
   */
  public boolean isSpringEnabled() {
    return forceSpringEnabled || springModuleEnabled;
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    if (!isUpToDate()) {
      EnunciateFreemarkerModel model = getModel();
      Map<String, String> conversions = Collections.<String, String>emptyMap();
      ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(conversions);
      classnameFor.setJdk15(true);
      model.put("packageFor", new ClientPackageForMethod(conversions));
      model.put("classnameFor", classnameFor);
      model.put("simpleNameFor", new SimpleNameWithParamsMethod(classnameFor));
      model.put("docsDir", enunciate.getProperty("docs.webapp.dir"));
      URL configTemplate = isSpringEnabled() ? getJAXWSSpringTemplateURL() : getSunJAXWSTemplateURL();
      processTemplate(configTemplate, model);

      URL eiTemplate = getInstrumentedEndpointTemplateURL();
      for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
        for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
          model.put("endpointInterface", ei);
          processTemplate(eiTemplate, model);
        }
      }

      getEnunciate().addAdditionalSourceRoot(getGenerateDir());
    }
    else {
      info("Skipping generation of JAX-WS RI support as everything appears up-to-date....");
    }

  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    super.doBuild();

    Enunciate enunciate = getEnunciate();

    File webappDir = getBuildDir();
    webappDir.mkdirs();
    File webinf = new File(webappDir, "WEB-INF");
    getEnunciate().copyDir(getGenerateDir(), webinf);

    BaseWebAppFragment webappFragment = new BaseWebAppFragment(getName());
    webappFragment.setBaseDir(webappDir);
    WebAppComponent servletComponent = new WebAppComponent();
    servletComponent.setName("jaxws");
    String servletClass = isSpringEnabled() ? "org.codehaus.enunciate.modules.jaxws_ri.WSSpringServlet" : "com.sun.xml.ws.transport.http.servlet.WSServlet";
    servletComponent.setClassname(servletClass);
    TreeSet<String> urlMappings = new TreeSet<String>();
    for (WsdlInfo wsdlInfo : getModel().getNamespacesToWSDLs().values()) {
      for (EndpointInterface endpointInterface : wsdlInfo.getEndpointInterfaces()) {
        urlMappings.add(String.valueOf(endpointInterface.getMetaData().get("soapPath")));
      }
    }
    servletComponent.setUrlMappings(urlMappings);
    webappFragment.setServlets(Arrays.asList(servletComponent));
    if (!isSpringEnabled()) {
      webappFragment.setListeners(Arrays.asList("com.sun.xml.ws.transport.http.servlet.WSServletContextListener"));
    }
    enunciate.addWebAppFragment(webappFragment);
  }

  /**
   * Whether the generated sources are up-to-date.
   *
   * @return Whether the generated sources are up-to-date.
   */
  protected boolean isUpToDate() {
    return enunciate.isUpToDateWithSources(getGenerateDir());
  }

  // Inherited.
  public boolean isJaxwsProvider() {
    return true;
  }

  // Inherited.
  public boolean isJaxrsProvider() {
    return false;
  }

  @Override
  public Validator getValidator() {
    return new JAXWSRIValidator();
  }

  public void setForceSpringEnabled(boolean forceSpringEnabled) {
    this.forceSpringEnabled = forceSpringEnabled;
  }

  // Inherited.
  @Override
  public boolean isDisabled() {
    if (super.isDisabled()) {
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getNamespacesToWSDLs().isEmpty()) {
      debug("JAXWS-RI module is disabled because there are no endpoint interfaces.");
      return true;
    }

    return false;
  }
}

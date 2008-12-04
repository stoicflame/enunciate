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

import com.sun.xml.ws.transport.http.servlet.WSSpringServlet;
import freemarker.template.TemplateException;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.webapp.BaseWebAppFragment;
import org.codehaus.enunciate.main.webapp.WebAppComponent;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.codehaus.enunciate.modules.spring_app.ServiceEndpointBeanIdMethod;
import org.codehaus.enunciate.modules.spring_app.SpringAppDeploymentModule;
import org.codehaus.enunciate.modules.spring_app.config.SpringImport;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.List;

/**
 * <h1>JAX-WS RI Module</h1>
 *
 * <p>The JAX-WS RI module assembles a JAX-WS RI-based server-side application for hosting the SOAP endpoints.</i>
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
 * <p>The "generate" step generates the spring configuration file.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>There are no additional configuration elements for the JAX-WS RI module.</p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The JAX-WS RI deployment module exports no artifacts.</p>
 *
 * @author Ryan Heaton
 * @docFileName module_jaxws_ri.html
 */
public class JAXWSRIDeploymentModule extends FreemarkerDeploymentModule {

  public JAXWSRIDeploymentModule() {
    setDisabled(true); //disabled by default; still using XFire.
  }

  /**
   * @return "cxf"
   */
  @Override
  public String getName() {
    return "jaxws-ri";
  }

  /**
   * @return The URL to "cxf-servlet.xml.fmt"
   */
  protected URL getJAXWSServletTemplateURL() {
    return JAXWSRIDeploymentModule.class.getResource("jaxws-servlet.xml.fmt");
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    if (!isDisabled()) {
      if (enunciate.isModuleEnabled("xfire")) {
        throw new EnunciateException("The JAX-WS RI module requires you to disable the XFire module.");
      }

      if (!enunciate.isModuleEnabled("jaxws")) {
        throw new EnunciateException("The JAX-WS RI module requires an enabled JAX-WS module.");
      }

      if (!enunciate.isModuleEnabled("spring-app")) {
        throw new EnunciateException("The CXF module requires the spring-app module to be enabled.");
      }

//      enunciate.getConfig().setForceJAXWSSpecCompliance(true); //make sure the WSDL and client code are JAX-WS-compliant.
    }

  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();
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

    if (!isUpToDate()) {
      model.put("endpointBeanId", new ServiceEndpointBeanIdMethod());
      model.put("docsDir", enunciate.getProperty("docs.webapp.dir"));
      processTemplate(getJAXWSServletTemplateURL(), model);
    }
    else {
      info("Skipping generation of CXF config as everything appears up-to-date....");
    }

    //add the spring import...
    List<DeploymentModule> enabledModules = enunciate.getConfig().getEnabledModules();
    for (DeploymentModule enabledModule : enabledModules) {
      if (enabledModule instanceof SpringAppDeploymentModule) {
        SpringImport beanImport = new SpringImport();
        beanImport.setFile(new File(getGenerateDir(), "jaxws-servlet.xml").getAbsolutePath());
        ((SpringAppDeploymentModule) enabledModule).getSpringImports().add(beanImport);
      }
    }
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    super.doBuild();

    Enunciate enunciate = getEnunciate();

    File webappDir = getBuildDir();
    webappDir.mkdirs();

    BaseWebAppFragment webappFragment = new BaseWebAppFragment(getName());
    webappFragment.setBaseDir(webappDir);
    WebAppComponent servletComponent = new WebAppComponent();
    servletComponent.setName("jaxws");
    servletComponent.setClassname(WSSpringServlet.class.getName());
//    WebAppComponent filterComponent = new WebAppComponent();
//    filterComponent.setName("cxf-filter");
//    filterComponent.setClassname(WSServlet.class.getName());
    TreeSet<String> urlMappings = new TreeSet<String>();
    for (WsdlInfo wsdlInfo : getModel().getNamespacesToWSDLs().values()) {
      for (EndpointInterface endpointInterface : wsdlInfo.getEndpointInterfaces()) {
        urlMappings.add(String.valueOf(endpointInterface.getMetaData().get("soapPath")));
      }
    }
    servletComponent.setUrlMappings(urlMappings);
//    filterComponent.setUrlMappings(urlMappings);
    webappFragment.setServlets(Arrays.asList(servletComponent));
//    webappFragment.setFilters(Arrays.asList(filterComponent));
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

  @Override
  public Validator getValidator() {
    return new JAXWSRIValidator();
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

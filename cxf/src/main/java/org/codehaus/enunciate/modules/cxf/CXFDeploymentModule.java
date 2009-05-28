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

package org.codehaus.enunciate.modules.cxf;

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
import org.apache.cxf.transport.servlet.CXFServlet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.List;

/**
 * <h1>CXF Module</h1>
 *
 * <p>The CXF module assembles a CXF-based server-side application for hosting the SOAP endpoints.</p>
 *
 * <p>Note that the CXF module is disabled by default, so you must enable it in the enunciate configuration file, e.g.:</p>
 *
 * <code class="console">
 * &lt;enunciate&gt;
 * &nbsp;&nbsp;&lt;modules&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;xfire disabled="true"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;cxf disabled="false"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;...
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/cxf&gt;
 * &nbsp;&nbsp;&lt;/modules&gt;
 * &lt;/enunciate&gt;
 *
 * <p>You should also be aware that the CXF module is not, by default, on the classpath when invoking Enunciate. For more information, 
 * see <a href="executables.html">invoking Enunciate</a></p>
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
 * <p>There are no additional configuration elements for the CXF module.</p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The CXF deployment module exports no artifacts.</p>
 *
 * @author Ryan Heaton
 * @docFileName module_cxf.html
 */
public class CXFDeploymentModule extends FreemarkerDeploymentModule {

  public CXFDeploymentModule() {
    setDisabled(true); //disabled by default; still using XFire.
  }

  /**
   * @return "cxf"
   */
  @Override
  public String getName() {
    return "cxf";
  }

  /**
   * @return The URL to "cxf-servlet.xml.fmt"
   */
  protected URL getCXFServletTemplateURL() {
    return CXFDeploymentModule.class.getResource("cxf-servlet.xml.fmt");
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    if (!isDisabled()) {
      if (enunciate.isModuleEnabled("xfire")) {
        throw new EnunciateException("The CXF module requires you to disable the XFire module.");
      }

      if (enunciate.isModuleEnabled("jaxws-ri")) {
        throw new EnunciateException("The CXF module requires you to disable the JAX-WS RI module.");
      }

      if (!enunciate.isModuleEnabled("spring-app")) {
        throw new EnunciateException("The CXF module requires the spring-app module to be enabled.");
      }
      else {
        List<DeploymentModule> enabledModules = enunciate.getConfig().getEnabledModules();
        for (DeploymentModule enabledModule : enabledModules) {
          if (enabledModule instanceof SpringAppDeploymentModule) {
            SpringImport cxfImport = new SpringImport();
            cxfImport.setUri("classpath:cxf-boilerplate.xml");
            ((SpringAppDeploymentModule) enabledModule).getSpringImports().add(cxfImport);
          }
        }
      }

      enunciate.getConfig().setForceJAXWSSpecCompliance(true); //make sure the WSDL and client code are JAX-WS-compliant.
    }

  }

  // Inherited.
  @Override
  public void initModel(EnunciateFreemarkerModel model) {
    super.initModel(model);

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

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    if (!isUpToDate()) {
      EnunciateFreemarkerModel model = getModel();
      model.put("endpointBeanId", new ServiceEndpointBeanIdMethod());
      model.put("docsDir", enunciate.getProperty("docs.webapp.dir"));
      processTemplate(getCXFServletTemplateURL(), model);
    }
    else {
      info("Skipping generation of CXF config as everything appears up-to-date....");
    }
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    super.doBuild();

    Enunciate enunciate = getEnunciate();

    File webappDir = getBuildDir();
    webappDir.mkdirs();
    File webinf = new File(webappDir, "WEB-INF");
    getEnunciate().copyFile(new File(getGenerateDir(), "cxf-servlet.xml"), new File(webinf, "cxf-servlet.xml"));

    BaseWebAppFragment webappFragment = new BaseWebAppFragment(getName());
    webappFragment.setBaseDir(webappDir);
    WebAppComponent servletComponent = new WebAppComponent();
    servletComponent.setName("cxf");
    servletComponent.setClassname(CXFServlet.class.getName());
    WebAppComponent filterComponent = new WebAppComponent();
    filterComponent.setName("cxf-filter");
    filterComponent.setClassname(CXFAdaptedServletFilter.class.getName());
    TreeSet<String> urlMappings = new TreeSet<String>();
    for (WsdlInfo wsdlInfo : getModel().getNamespacesToWSDLs().values()) {
      for (EndpointInterface endpointInterface : wsdlInfo.getEndpointInterfaces()) {
        urlMappings.add(String.valueOf(endpointInterface.getMetaData().get("soapPath")));
      }
    }
    servletComponent.setUrlMappings(urlMappings);
    filterComponent.setUrlMappings(urlMappings);
    webappFragment.setServlets(Arrays.asList(servletComponent));
    webappFragment.setFilters(Arrays.asList(filterComponent));
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
    return new CXFValidator();
  }

  // Inherited.
  @Override
  public boolean isDisabled() {
    if (super.isDisabled()) {
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getNamespacesToWSDLs().isEmpty()) {
      debug("CXF module is disabled because there are no endpoint interfaces.");
      return true;
    }

    return false;
  }
}

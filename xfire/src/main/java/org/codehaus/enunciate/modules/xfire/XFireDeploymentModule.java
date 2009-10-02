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

package org.codehaus.enunciate.modules.xfire;

import freemarker.template.TemplateException;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateClasspathListener;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.FileArtifact;
import org.codehaus.enunciate.main.webapp.BaseWebAppFragment;
import org.codehaus.enunciate.main.webapp.WebAppComponent;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.SpecProviderModule;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Set;

/**
 * <h1>XFire Module</h1>
 *
 * <p>The XFire deployment module that deploys the SOAP endpoints using XFire as a provider.</p>
 *
 * <p>Note that the XFire module is disabled by default, so you must enable it in the enunciate configuration file, e.g.:</p>
 *
 * <code class="console">
 * &lt;enunciate&gt;
 * &nbsp;&nbsp;&lt;modules&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;jaxws-ri disabled="true"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;xfire disabled="false"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;...
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/xfire&gt;
 * &nbsp;&nbsp;&lt;/modules&gt;
 * &lt;/enunciate&gt;
 *
 * <p>You should also be aware that the XFire module is not, by default, on the classpath when invoking Enunciate. For more information,
 * see <a href="http://docs.codehaus.org/display/ENUNCIATE/Using+CXF+or+XFire">using CXF or XFire</a>.</p>
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
 * <p>The "generate" step generates the source beans and the spring configuration file.  And the spring
 * servlet file for the XFire soap servlet.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The XFire modules supports one attribute, "xfireBeansImport", which defines the location of the XFire spring beans declaration file. The default value is
 * "classpath:org/codehaus/xfire/spring/xfire.xml".</p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The XFire deployment module exports the following artifacts:</p>
 *
 * <ul>
 *   <li>The "xfire-server.src.dir" artifact is the directory where the beans are generated.</li>
 * </ul>
 *
 * @author Ryan Heaton
 * @docFileName module_xfire.html
 */
public class XFireDeploymentModule extends FreemarkerDeploymentModule implements SpecProviderModule, EnunciateClasspathListener {

  private String xfireBeansImport = "classpath:org/codehaus/xfire/spring/xfire.xml";
  private boolean exporterFound = false;

  public XFireDeploymentModule() {
    setDisabled(true); //disabled by default; using JAXWS RI by default.
  }

  /**
   * @return "xfire"
   */
  @Override
  public String getName() {
    return "xfire";
  }

  /**
   * @return The URL to "rpc-request-bean.fmt"
   */
  protected URL getRPCRequestBeanTemplateURL() {
    return XFireDeploymentModule.class.getResource("rpc-request-bean.fmt");
  }

  /**
   * @return The URL to "rpc-response-bean.fmt"
   */
  protected URL getRPCResponseBeanTemplateURL() {
    return XFireDeploymentModule.class.getResource("rpc-response-bean.fmt");
  }

  /**
   * @return The URL to "xfire-servlet.xml.fmt"
   */
  protected URL getXfireServletTemplateURL() {
    return XFireDeploymentModule.class.getResource("xfire-servlet.xml.fmt");
  }

  /**
   * @return The URL to "xfire-servlet.xml.fmt"
   */
  protected URL getParameterNamesTemplateURL() {
    return XFireDeploymentModule.class.getResource("enunciate-soap-parameter-names.properties.fmt");
  }

  public void onClassesFound(Set<String> classes) {
    exporterFound |= classes.contains(EnunciatedXFireExporter.class.getName());
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    if (!isDisabled()) {
      if (!enunciate.isModuleEnabled("jaxws-support")) {
        throw new EnunciateException("The XFire module requires an enabled JAXWS Support module.");
      }

      if (!exporterFound) {
        warn("The Enunciate XFire runtime wasn't found on the Enunciate classpath. This could be fatal to the runtime application.");
      }
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

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    if (!isUpToDate()) {
      EnunciateFreemarkerModel model = getModel();
      //generate the rpc request/response beans.
      for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
        for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
          for (WebMethod webMethod : ei.getWebMethods()) {
            for (WebMessage webMessage : webMethod.getMessages()) {
              if (webMessage instanceof RPCInputMessage) {
                model.put("message", webMessage);
                processTemplate(getRPCRequestBeanTemplateURL(), model);
              }
              else if (webMessage instanceof RPCOutputMessage) {
                model.put("message", webMessage);
                processTemplate(getRPCResponseBeanTemplateURL(), model);
              }
            }
          }
        }
      }

      model.put("xfireBeansImport", getXfireBeansImport());
      model.put("docsDir", enunciate.getProperty("docs.webapp.dir"));
      processTemplate(getXfireServletTemplateURL(), model);
      processTemplate(getParameterNamesTemplateURL(), model);
    }
    else {
      info("Skipping generation of XFire support classes as everything appears up-to-date....");
    }

    getEnunciate().addArtifact(new FileArtifact(getName(), "xfire-server.src.dir", getGenerateDir()));
    getEnunciate().addAdditionalSourceRoot(getGenerateDir());
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    super.doBuild();

    File webappDir = getBuildDir();
    webappDir.mkdirs();
    File webinf = new File(webappDir, "WEB-INF");
    getEnunciate().copyFile(new File(getGenerateDir(), "xfire-servlet.xml"), new File(webinf, "xfire-servlet.xml"));
    getEnunciate().copyFile(new File(getGenerateDir(), "enunciate-soap-parameter-names.properties"),
                            new File(new File(webinf, "classes"), "enunciate-soap-parameter-names.properties"));

    BaseWebAppFragment webappFragment = new BaseWebAppFragment(getName());
    webappFragment.setBaseDir(webappDir);
    WebAppComponent servletComponent = new WebAppComponent();
    servletComponent.setName("xfire");
    servletComponent.setClassname(DispatcherServlet.class.getName());
    TreeSet<String> urlMappings = new TreeSet<String>();
    for (WsdlInfo wsdlInfo : getModel().getNamespacesToWSDLs().values()) {
      for (EndpointInterface endpointInterface : wsdlInfo.getEndpointInterfaces()) {
        urlMappings.add(String.valueOf(endpointInterface.getMetaData().get("soapPath")));
      }
    }
    servletComponent.setUrlMappings(urlMappings);
    webappFragment.setServlets(Arrays.asList(servletComponent));
    getEnunciate().addWebAppFragment(webappFragment);
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
    return new XFireValidator();
  }

  public String getXfireBeansImport() {
    return xfireBeansImport;
  }

  public void setXfireBeansImport(String xfireBeansImport) {
    this.xfireBeansImport = xfireBeansImport;
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
  public boolean isDisabled() {
    if (super.isDisabled()) {
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getNamespacesToWSDLs().isEmpty()) {
      debug("XFire module is disabled because there are no endpoint interfaces.");
      return true;
    }

    return false;
  }
}

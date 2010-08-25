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
import org.apache.cxf.transport.servlet.CXFServlet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.webapp.WSDLRedirectFilter;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateClasspathListener;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.webapp.BaseWebAppFragment;
import org.codehaus.enunciate.main.webapp.WebAppComponent;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.SpecProviderModule;
import org.codehaus.enunciate.modules.spring_app.SpringAppDeploymentModule;
import org.codehaus.enunciate.modules.spring_app.config.SpringImport;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

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
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;jaxws-ri disabled="true"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;cxf disabled="false"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;...
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/cxf&gt;
 * &nbsp;&nbsp;&lt;/modules&gt;
 * &lt;/enunciate&gt;
 *
 * <p>You should also be aware that the CXF module is not, by default, on the classpath when invoking Enunciate. For more information, 
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
 * <p>The "generate" step generates the spring configuration file.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The CXF module supports the following configuration attributes:</p>
 *
 * <ul>
 * <li>The "validate" attribute (boolean) can be used to turn off CXF validation errors. Default: true</li>
 * <li>The "enableJaxws" attribute (boolean) can be used to disable the JAX-WS support, leaving the JAX-WS support to another module if necessary. Default: true</li>
 * <li>The "enableJaxrs" attribute (boolean) can be used to disable the JAX-RS support, leaving the JAX-RS support to another module if necessary. Default: true</li>
 * <li>The "useSubcontext" attribute is used to enable/disable mounting the JAX-RS resources at the rest subcontext. Default: "true".</li>
 * <li>The "useWsdlRedirectFilter" attribute is used to disable the use of the Enunciate-provided wsdl redirect filter that
 *     handles the requests to ?wsdl</li>
 * </ul>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The CXF deployment module exports no artifacts.</p>
 *
 * @author Ryan Heaton
 * @docFileName module_cxf.html
 */
public class CXFDeploymentModule extends FreemarkerDeploymentModule implements EnunciateClasspathListener, SpecProviderModule {

  private boolean enableJaxrs = true;
  private boolean enableJaxws = true;
  private boolean validate = false;
  private boolean useSubcontext = true;
  private boolean jacksonAvailable = false;
  private boolean filterFound = false;
  private boolean useWsdlRedirectFilter = true;

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

  // Inherited.
  public void onClassesFound(Set<String> classes) {
    jacksonAvailable |= classes.contains("org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider");
    filterFound |= classes.contains(CXFAdaptedServletFilter.class.getName());
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    if (!isDisabled()) {
      if (!enunciate.isModuleEnabled("spring-app")) {
        throw new EnunciateException("The CXF module requires the spring-app module to be enabled.");
      }
      else {
        List<DeploymentModule> enabledModules = enunciate.getConfig().getEnabledModules();
        for (DeploymentModule enabledModule : enabledModules) {
          if (enabledModule instanceof SpringAppDeploymentModule) {
            List<SpringImport> springImports = ((SpringAppDeploymentModule) enabledModule).getSpringImports();

            //standard cxf import.
            SpringImport cxfImport = new SpringImport();
            cxfImport.setUri("classpath:META-INF/cxf/cxf.xml");
            springImports.add(cxfImport);

            //standard cxf import.
            cxfImport = new SpringImport();
            cxfImport.setUri("classpath:META-INF/cxf/cxf-servlet.xml");
            springImports.add(cxfImport);

            if (enableJaxws) {
              cxfImport = new SpringImport();
              cxfImport.setUri("classpath:META-INF/cxf/cxf-extension-soap.xml");
              springImports.add(cxfImport);
            }

            if (enableJaxrs) {
              cxfImport = new SpringImport();
              cxfImport.setUri("classpath:META-INF/cxf/cxf-extension-jaxrs-binding.xml");
              springImports.add(cxfImport);
            }
          }
        }
      }

      if (this.enableJaxws) {
        enunciate.getConfig().setForceJAXWSSpecCompliance(true); //make sure the WSDL and client code are JAX-WS-compliant.
      }

    }

  }

  // Inherited.
  @Override
  public void initModel(EnunciateFreemarkerModel model) {
    super.initModel(model);

    if (!isDisabled()) {
      EnunciateConfiguration config = model.getEnunciateConfig();
      if (enableJaxws) {
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

      if (enableJaxrs) {
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
      }

      if (!filterFound) {
        warn("The Enunciate CXF runtime wasn't found on the Enunciate classpath. This could be fatal to the runtime application.");
      }
    }
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    if (!isUpToDate()) {
      EnunciateFreemarkerModel model = getModel();
      model.put("provideJaxws", enableJaxws);
      model.put("provideJaxrs", enableJaxrs);
      model.put("jacksonAvailable", jacksonAvailable);
      model.put("amfEnabled", getEnunciate().isModuleEnabled("amf"));
      model.put("restSubcontext", this.useSubcontext ? getRestSubcontext() : "/");
      model.put("docsDir", enunciate.getProperty("docs.webapp.dir"));
      model.put("loggingFeatureEnabled", (enableJaxrs && isLoggingFeatureEnabled()));
      processTemplate(getCXFServletTemplateURL(), model);
    }
    else {
      info("Skipping generation of CXF config as everything appears up-to-date....");
    }
  }

  /**
   * Whether the logging features is enabled via annotations.
   *
   * @return Whether the logging features is enabled via annotations.
   */
  protected boolean isLoggingFeatureEnabled() {
	  org.apache.cxf.feature.Features featuresAnnotation;
	  
	  for (RootResource resource : getModel().getRootResources()) {
	 	  featuresAnnotation = resource.getAnnotation(org.apache.cxf.feature.Features.class);
	 	  if (featuresAnnotation != null) {
	 		  for (String feature : featuresAnnotation.features()) {
	 			  if (feature.compareTo("org.apache.cxf.feature.LoggingFeature") != 0) {
	 				  return true;
	 			  }
	 		  }
	 	  }
	  }

    return false;
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    super.doBuild();

    Enunciate enunciate = getEnunciate();

    File webappDir = getBuildDir();
    webappDir.mkdirs();
    File webinf = new File(webappDir, "WEB-INF");

    BaseWebAppFragment webappFragment = new BaseWebAppFragment(getName());
    webappFragment.setBaseDir(webappDir);

    Set<String> urlMappings = new TreeSet<String>();
    ArrayList<WebAppComponent> servlets = new ArrayList<WebAppComponent>();
    ArrayList<WebAppComponent> filters = new ArrayList<WebAppComponent>();
    if (enableJaxws) {
      //jax-ws servlet config.
      WebAppComponent jaxwsServletComponent = new WebAppComponent();
      jaxwsServletComponent.setName("cxf-jaxws");
      jaxwsServletComponent.setClassname(CXFServlet.class.getName());
      TreeSet<String> jaxwsUrlMappings = new TreeSet<String>();
      for (WsdlInfo wsdlInfo : getModel().getNamespacesToWSDLs().values()) {
        TreeSet<String> urlMappingsForNs = new TreeSet<String>();
        for (EndpointInterface endpointInterface : wsdlInfo.getEndpointInterfaces()) {
          urlMappingsForNs.add(String.valueOf(endpointInterface.getMetaData().get("soapPath")));
        }
        jaxwsUrlMappings.addAll(urlMappingsForNs);

        String redirectLocation = (String) wsdlInfo.getProperty("redirectLocation");
        if (redirectLocation != null && isUseWsdlRedirectFilter()) {
          WebAppComponent wsdlFilter = new WebAppComponent();
          wsdlFilter.setName("wsdl-redirect-filter-" + wsdlInfo.getId());
          wsdlFilter.setClassname(WSDLRedirectFilter.class.getName());
          wsdlFilter.addInitParam(WSDLRedirectFilter.WSDL_LOCATION_PARAM, redirectLocation);
          wsdlFilter.setUrlMappings(urlMappingsForNs);
          filters.add(wsdlFilter);
        }
      }
      jaxwsServletComponent.setUrlMappings(jaxwsUrlMappings);
      getEnunciate().copyFile(new File(getGenerateDir(), "cxf-jaxws-servlet.xml"), new File(webinf, "cxf-jaxws-servlet.xml"));
      jaxwsServletComponent.addInitParam("config-location", "/WEB-INF/cxf-jaxws-servlet.xml");
      servlets.add(jaxwsServletComponent);
      urlMappings.addAll(jaxwsUrlMappings);
    }

    if (enableJaxrs) {
      WebAppComponent jaxrsServletComponent = new WebAppComponent();
      jaxrsServletComponent.setName("cxf-jaxrs");
      jaxrsServletComponent.setClassname(CXFServlet.class.getName());
      TreeSet<String> jaxrsUrlMappings = new TreeSet<String>();
      for (RootResource rootResource : getModel().getRootResources()) {
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

      jaxrsServletComponent.setUrlMappings(jaxrsUrlMappings);
      getEnunciate().copyFile(new File(getGenerateDir(), "cxf-jaxrs-servlet.xml"), new File(webinf, "cxf-jaxrs-servlet.xml"));
      jaxrsServletComponent.addInitParam("config-location", "/WEB-INF/cxf-jaxrs-servlet.xml");
      servlets.add(jaxrsServletComponent);
      urlMappings.addAll(jaxrsUrlMappings);
    }
    webappFragment.setServlets(servlets);
    webappFragment.setFilters(filters);

    WebAppComponent filterComponent = new WebAppComponent();
    filterComponent.setName("cxf-filter");
    filterComponent.setClassname(CXFAdaptedServletFilter.class.getName());
    filterComponent.setUrlMappings(urlMappings);
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

  // Inherited.
  public boolean isJaxwsProvider() {
    return this.enableJaxws;
  }

  // Inherited.
  public boolean isJaxrsProvider() {
    return this.enableJaxrs;
  }

  /**
   * Whether to use the REST subcontext.
   *
   * @param useSubcontext Whether to use the REST subcontext.
   */
  public void setUseSubcontext(boolean useSubcontext) {
    this.useSubcontext = useSubcontext;
  }

  /**
   * Whether or not to apply the CXF validation rules.
   *
   * @param validate Whether or not to apply the CXF validation rules.
   */
  public void setValidate(boolean validate) {
    this.validate = validate;
  }

  /**
   * Whether to enable the JAX-RS capabilities of CXF.
   *
   * @param enableJaxrs Whether to enable the JAX-RS capabilities of CXF.
   */
  public void setEnableJaxrs(boolean enableJaxrs) {
    this.enableJaxrs = enableJaxrs;
  }

  /**
   * Whether to enable the JAX-WS capabilities of CXF.
   *
   * @param enableJaxws Whether to enable the JAX-WS capabilities of CXF.
   */
  public void setEnableJaxws(boolean enableJaxws) {
    this.enableJaxws = enableJaxws;
  }

  public boolean isUseWsdlRedirectFilter() {
    return useWsdlRedirectFilter;
  }

  public void setUseWsdlRedirectFilter(boolean useWsdlRedirectFilter) {
    this.useWsdlRedirectFilter = useWsdlRedirectFilter;
  }

  @Override
  public Validator getValidator() {
    return this.validate ? new CXFValidator(enableJaxws, enableJaxrs) : null;
  }

  protected String getRestSubcontext() {
    String restSubcontext = getEnunciate().getConfig().getDefaultRestSubcontext();
    //todo: override default rest subcontext?
    return restSubcontext;
  }

  // Inherited.
  @Override
  public boolean isDisabled() {
    if (super.isDisabled()) {
      return true;
    }
    else if (!enableJaxws && !enableJaxrs) {
      debug("CXF module is disabled because both JAX-WS and JAX-RS support is disabled.");
      return true;
    }
    else if (getModelInternal() != null) {
      boolean noJaxrsWorkToDo = !enableJaxrs || getModelInternal().getRootResources().isEmpty();
      boolean noJaxwsWorkToDo = !enableJaxws || getModelInternal().getNamespacesToWSDLs().isEmpty();
      if (noJaxrsWorkToDo && noJaxwsWorkToDo) {
        debug("CXF module is disabled because there are no endpoint interfaces, nor any root resources to process.");
        return true;
      }
    }

    return false;
  }
}

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

package org.codehaus.enunciate.modules.jersey;

import freemarker.template.TemplateException;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.main.webapp.BaseWebAppFragment;
import org.codehaus.enunciate.main.webapp.WebAppComponent;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.jersey.config.JerseyRuleSet;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Arrays;
import java.util.Map;

/**
 * <h1>Jersey Module</h1>
 *
 * <p>The Jersey module generates and compiles the support files and classes necessary to support a REST application according to
 * <a href="https://jsr311.dev.java.net/">JSR-311</a>, using <a href="https://jersey.dev.java.net/">Jersey</a>.</p>
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
 * <p>The generate step of the Jersey module generates the configuration files for a servlet-based Jersey application.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The Jersey module supports the following attributes:</p>
 *
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The Jersey deployment module exports no artifacts.</p>
 *
 * @author Ryan Heaton
 * @docFileName module_jersey.html
 */
public class JerseyDeploymentModule extends FreemarkerDeploymentModule {

  private boolean useSubcontext = true;
  private boolean usePathBasedConneg = true;
  private boolean disableWildcardServletError = false;

  /**
   * @return "jersey"
   */
  @Override
  public String getName() {
    return "jersey";
  }

  /**
   * The root resources template URL.
   *
   * @return The root resources template URL.
   */
  public URL getRootResourceListTemplateURL() {
    return JerseyDeploymentModule.class.getResource("jaxrs-root-resources.list.fmt");
  }

  /**
   * The providers template URL.
   *
   * @return The providers template URL.
   */
  public URL getProvidersListTemplateURL() {
    return JerseyDeploymentModule.class.getResource("jaxrs-providers.list.fmt");
  }

  /**
   * @return A new {@link JerseyValidator}.
   */
  @Override
  public Validator getValidator() {
    return new JerseyValidator(!isUseSubcontext() && !isDisableWildcardServletError());
  }

  public void doFreemarkerGenerate() throws EnunciateException, IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();
    processTemplate(getRootResourceListTemplateURL(), model);
    processTemplate(getProvidersListTemplateURL(), model);
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    super.doBuild();

    File webappDir = getBuildDir();
    webappDir.mkdirs();
    File webinf = new File(webappDir, "WEB-INF");
    File webinfClasses = new File(webinf, "classes");
    getEnunciate().copyFile(new File(getGenerateDir(), "jaxrs-providers.list"), new File(webinfClasses, "jaxrs-providers.list"));
    getEnunciate().copyFile(new File(getGenerateDir(), "jaxrs-root-resources.list"), new File(webinfClasses, "jaxrs-root-resources.list"));

    BaseWebAppFragment webappFragment = new BaseWebAppFragment(getName());
    webappFragment.setBaseDir(webappDir);
    WebAppComponent servletComponent = new WebAppComponent();
    servletComponent.setName("jersey");
    servletComponent.setClassname(EnunciateSpringServlet.class.getName());
    TreeMap<String, String> initParams = new TreeMap<String, String>();
    if (!isUsePathBasedConneg()) {
      initParams.put(JerseyAdaptedHttpServletRequest.FEATURE_PATH_BASED_CONNEG, Boolean.FALSE.toString());
    }
    if (isUseSubcontext()) {
      initParams.put(JerseyAdaptedHttpServletRequest.PROPERTY_SERVLET_PATH, getRestSubcontext());
    }
    servletComponent.setInitParams(initParams);

    Map<String, String> contentTypes2Ids = getModel().getContentTypesToIds();
    TreeSet<String> urlMappings = new TreeSet<String>();
    for (ResourceMethod resourceMethod : getModel().getResourceMethods()) {
      String resourceMethodPattern = resourceMethod.getServletPattern();
      String servletPattern = isUseSubcontext() ? getRestSubcontext() + resourceMethodPattern : resourceMethodPattern;
      debug("Resource method %s of resource %s to be made accessible by servlet pattern %s.",
            resourceMethod.getSimpleName(), resourceMethod.getParent().getQualifiedName(), servletPattern);
      urlMappings.add(servletPattern);
      if (isUsePathBasedConneg()) {
        for (String produces : resourceMethod.getProducesMime()) {
          MediaType mediaType = MediaType.valueOf(produces);
          for (Map.Entry<String, String> entry : contentTypes2Ids.entrySet()) {
            MediaType supportedType = MediaType.valueOf(entry.getKey());
            if (mediaType.isCompatible(supportedType)) {
              String connegServletPattern = '/' + entry.getKey() + resourceMethodPattern;
              debug("Resource method %s of resource %s to be made accessible by servlet pattern %s because it produces %s/%s.",
                    resourceMethod.getSimpleName(), resourceMethod.getParent().getQualifiedName(), connegServletPattern,
                    mediaType.getType(), mediaType.getSubtype());
              urlMappings.add(connegServletPattern);
            }
          }
        }
      }
    }
    servletComponent.setUrlMappings(urlMappings);
    webappFragment.setServlets(Arrays.asList(servletComponent));
    getEnunciate().addWebAppFragment(webappFragment);
  }

  protected String getRestSubcontext() {
    String restSubcontext = getEnunciate().getConfig().getDefaultRestSubcontext();
    //todo: override default rest subcontext?
    return restSubcontext;
  }

  @Override
  public RuleSet getConfigurationRules() {
    return new JerseyRuleSet();
  }

  /**
   * Whether to use the REST subcontext.
   *
   * @return Whether to use the REST subcontext.
   */
  public boolean isUseSubcontext() {
    return useSubcontext;
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
   * Whether to use path-based conneg.
   *
   * @return Whether to use path-based conneg.
   */
  public boolean isUsePathBasedConneg() {
    return usePathBasedConneg;
  }

  /**
   * Whether to use path-based conneg.
   *
   * @param usePathBasedConneg Whether to use path-based conneg.
   */
  public void setUsePathBasedConneg(boolean usePathBasedConneg) {
    this.usePathBasedConneg = usePathBasedConneg;
  }

  /**
   * Whether to disable the greedy servlet pattern error.
   *
   * @return Whether to disable the greedy servlet pattern error.
   */
  public boolean isDisableWildcardServletError() {
    return disableWildcardServletError;
  }

  /**
   * Whether to disable the wildcard servlet pattern error.
   *
   * @param disableWildcardServletError Whether to disable the wildcard servlet pattern error.
   */
  public void setDisableWildcardServletError(boolean disableWildcardServletError) {
    this.disableWildcardServletError = disableWildcardServletError;
  }
}
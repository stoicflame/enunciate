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

package org.codehaus.enunciate.modules.spring_app;

import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import freemarker.template.TemplateException;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.template.freemarker.SoapAddressPathMethod;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.Artifact;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.FileArtifact;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.spring_app.config.*;
import org.codehaus.enunciate.modules.spring_app.config.security.SecurityConfig;
import org.codehaus.enunciate.modules.spring_app.config.security.FormBasedLoginConfig;
import org.codehaus.enunciate.modules.spring_app.config.security.OAuthConfig;
import org.springframework.util.AntPathMatcher;
import sun.misc.Service;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Manifest;

/**
 * <h1>Spring App Module</h1>
 *
 * <p>The spring app deployment module produces the web app for hosting the API endpoints and documentation.</p>
 *
 * <p>The order of the spring app deployment module is 200, putting it after any of the other modules, including
 * the documentation deployment module.  The spring app deployment module maintains soft dependencies on the other
 * Enunciate modules.  If those modules are active, the spring app deployment modules will assemble their artifacts
 * into a <a href="http://www.springframework.org/">spring</a>-supported web application.</p>
 *
 * <ul>
 * <li><a href="#steps">steps</a></li>
 * <li><a href="#config">application configuration</a></li>
 * <li><a href="module_spring_app_security.html">security configuration</a></li>
 * <li><a href="#artifacts">artifacts</a></li>
 * </ul>
 *
 * <h1><a name="steps">Steps</a></h1>
 *
 * <h3>generate</h3>
 *
 * <p>The "generate" step generates the deployment descriptors, and the <a href="http://www.springframework.org/">Spring</a>
 * configuration file.  Refer to <a href="#config">configuration</a> to learn how to customize the deployment
 * descriptors and the spring config file.</p>
 *
 * <h3>compile</h3>
 *
 * <p>The "compile" step compiles all API source files, including the source files that were generated from other modules
 * (e.g. JAX-WS module, XFire module, GWT module, AMF module, etc.).</p>
 *
 * <h3>build</h3>
 *
 * <p>The "build" step assembles all the generated artifacts, compiled classes, and deployment descriptors into an (expanded)
 * war directory.</p>
 *
 * <p>All classes compiled in the compile step are copied to the WEB-INF/classes directory.</p>
 *
 * <p>A set of libraries are copied to the WEB-INF/lib directory.  This set of libraries can be specified in the
 * <a href="#config">configuration file</a>.  Unless specified otherwise in the configuration file, the
 * libraries copied will be filtered from the classpath specified to Enunciate at compile-time.  The filtered libraries
 * are those libraries that are determined to be specific to running the Enunciate compile-time engine.  All other
 * libraries on the classpath are assumed to be dependencies for the API and are therefore copied to WEB-INF/lib.
 * (If a directory is found on the classpath, its contents are copied to WEB-INF/classes.)</p>
 *
 * <p>The web.xml file is copied to the WEB-INF directory.  A tranformation can be applied to the web.xml file before the copy,
 * if specified in the config, allowing you to apply your own servlet filters, etc.  <i>Take care to preserve the existing elements
 * when applying a transformation to the web.xml file, as losing data will result in missing or malfunctioning endpoints.</i></p>
 *
 * <p>The spring-servlet.xml file is generated and copied to the WEB-INF directory.  You can specify other spring config files that
 * will be copied (and imported by the spring-servlet.xml file) in the configuration.  This option allows you to specify spring AOP
 * interceptors and XFire in/out handlers to wrap your endpoints, if desired. Additional spring configuration for security is also
 * copied.</p>
 *
 * <p>The documentation (if found) is copied to the configured location.</p>
 *
 * <p>The other modules application are copied to the configured location.  This includes GWT apps and Flex apps.</p>
 *
 * <h3>package</h3>
 *
 * <p>The "package" step packages the expanded war and exports it.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <ul>
 * <li><a href="#config_structure">structure</a></li>
 * <li><a href="#config_attributes">attributes</a></li>
 * <li>elements<br/>
 * <ul>
 * <li><a href="#config_war_element">The "war" element</a></li>
 * <li><a href="#config_springImport">The "springImport" element</a></li>
 * <li><a href="#config_globalServiceInterceptor">The "globalServiceInterceptor" element</a></li>
 * <li><a href="#config_handlerInterceptor">The "handlerInterceptor" element</a></li>
 * <li><a href="#config_handlerMapping">The "handlerMapping" element</a></li>
 * <li><a href="#config_copyResources">The "copyResources" element</a></li>
 * </ul>
 * </li>
 * <li><a href="module_spring_app_security.html">Spring Application Security</a><br/>
 * <ul>
 * <li><a href="module_spring_app_security.html#security_annotations">Security Annotations</a></li>
 * <li><a href="module_spring_app_security.html#security_user_details">User Details Service</a></li>
 * <li><a href="module_spring_app_security.html#security_config">The "security" configuration element</a></li>
 * <li><a href="module_spring_app_security.html#security_login_logout">Login and Logout API Methods</a></li>
 * </ul>
 * </li>
 * </ul>
 *
 * <p>The configuration for the Spring App deployment module is specified by the "spring-app" child element under the "modules" element
 * of the enunciate configuration file.</p>
 *
 * <h3><a name="config_structure">Structure</a></h3>
 *
 * <p>The following example shows the structure of the configuration elements for this module.  Note that this shows only the structure.
 * Some configuration elements don't make sense when used together. For more information about the security configuration, see
 * <a href="module_spring_app_security.html">Spring Application Security</a>.</p>
 *
 * <code class="console">
 * &lt;enunciate&gt;
 * &nbsp;&nbsp;&lt;modules&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;spring-app compileDebugInfo="[true | false]"
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contextLoaderListenerClass="..."
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dispatcherServletClass="..."
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;defaultDependencyCheck="[none | objects | simple | all]"
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;defaultAutowire="[no | byName | byType | constructor | autodetect]"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;war name="..." webXMLTransform="..." webXMLTransformURL="..."
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;preBase="..." postBase="..."
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;includeClasspathLibs="[true|false]" excludeDefaultLibs="[true|false]"
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;docsDir="..." gwtAppDir="..." flexAppDir="..."&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;includeLibs pattern="..." file="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;includeLibs pattern="..." file="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;excludeLibs pattern="..." file="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;excludeLibs pattern="..." file="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;manifest&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;attribute name="..." value="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;attribute section="..." name="..." value="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/manifest&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/war&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;springImport file="..." uri="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;springImport file="..." uri="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;globalServiceInterceptor interceptorClass="..." beanName="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;globalServiceInterceptor interceptorClass="..." beanName="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;handlerInterceptor interceptorClass="..." beanName="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;handlerInterceptor interceptorClass="..." beanName="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;handlerMapping pattern="..." beanName="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;handlerMapping pattern="..." beanName="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;copyResources dir="..." pattern="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;copyResources dir="..." pattern="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;security ...&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/security&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/spring-app&gt;
 * &nbsp;&nbsp;&lt;/modules&gt;
 * &lt;/enunciate&gt;
 * </code>
 *
 * <h3><a name="config_attributes">attributes</a></h3>
 *
 * <ul>
 * <li>The "<b>enableSecurity</b>" attribute specifies that <a href="module_spring_app_security.html">security</a> should be enabled.  The default is "false."</li>
 * <li>The "<b>compileDebugInfo</b>" attribute specifies that the compiled classes should be compiled with debug info.  The default is "true."</li>
 * <li>The "<b>dispatcherServletClass</b>" attribute specifies the FQN of the class to use as the Spring dispatcher servlet.  The default is "org.springframework.web.servlet.DispatcherServlet".</li>
 * <li>The "<b>contextLoaderListenerClass</b>" attribute specifies that FQN of the class to use as the Spring context loader listener.  The default is "org.springframework.web.context.ContextLoaderListener".</li>
 * <li>The "<b>defaultDependencyCheck</b>" attribute specifies that value of the "default-dependency-check" for the generated spring file.</li>
 * <li>The "<b>defaultAutowire</b>" attribute specifies that value of the "default-autowire" for the generated spring file.</li>
 * <li>The "<b>doCompile</b>" attribute specifies whether this module should take on the responsibility of compiling the server-side classes.  This may not be
 * desired if the module is being used only for generating the war structure and configuration files.  Default: "true".</li>
 * <li>The "<b>doLibCopy</b>" attribute specifies whether this module should take on the responsibility of copying libraries to WEB-INF/lib.  This may not be
 * desired if the module is being used only for generating the war structure and configuration files.  Default: "true".</li>
 * <li>The "<b>doPackage</b>" attribute specifies whether this module should take on the responsibility of packaging (zipping) up the war.  This may not be
 * desired if the module is being used only for generating the war structure and configuration files.  Default: "true".</li>
 * </ul>
 *
 * <h3><a name="config_war_element">The "war" element</a></h3>
 *
 * <p>The "war" element is used to specify configuration for the assembly of the war.  It supports the following attributes:</p>
 *
 * <ul>
 * <li>The "<b>name</b>" attribute specifies the name of the war.  The default is the enunciate configuration label.</li>
 * <li>The "<b>docsDir</b>" attribute specifies a different directory in the war for the documentation (including WSDL and schemas).  The default is the
 * root directory of the war.</li>
 * <li>The "<b>gwtAppDir</b>" attribute specifies a different directory in the war for the GWT appliction(s).  The default is the
 * root directory of the war.</li>
 * <li>The "<b>flexAppDir</b>" attribute specifies a different directory in the war for the flex appliction(s).  The default is the
 * root directory of the war.</li>
 * <li>The "<b>webXMLTransform</b>" attribute specifies the XSLT tranform file that the web.xml file will pass through before being copied to the WEB-INF
 * directory.  No tranformation will be applied if none is specified.</li>
 * <li>The "<b>webXMLTransformURL</b>" attribute specifies the URL to an XSLT tranform that the web.xml file will pass through before being copied to the WEB-INF
 * directory.  No tranformation will be applied if none is specified.</li>
 * <li>The "<b>preBase</b>" attribute specifies a directory (could be gzipped) that supplies a "base" for the war.  The directory contents will be copied to
 * the building war directory <i>before</i> it is provided with any Enunciate-specific files and directories.</li>
 * <li>The "<b>postBase</b>" attribute specifies a directory (could be gzipped) that supplies a "base" for the war.  The directory contents will be copied to
 * the building war directory <i>after</i> it is provided with any Enunciate-specific files and directories.</li>
 * <li>The "<b>includeClasspathLibs</b>" attribute specifies whether Enunciate will use the libraries from the classpath for applying the include/exclude
 * filters.  If "false" only the libs explicitly included by file (see below) will be filtered.</li>
 * <li>The "<b>excludeDefaultLibs</b>" attribute specifies whether Enunciate should perform its default filtering of known compile-time-only jars.</li>
 * </ul>
 *
 * <p><u>Including or excluding jars from the war</u></p>
 *
 * <p>By default, the war is constructed by copying jars that are on the classpath to its "lib" directory (the contents of <i>directories</i> on the classpath
 * will be copied to the "classes" directory).  You add a specific file to this list with the "file" attribute of the "includeLibs" element of the "war" element.</p>
 *
 * <p>Once the initial list of jars to be potentially copied is created, it is passed through an "include" filter that you may specify with nested "includeLibs"
 * elements. For each of these elements, you can specify a set of files to include with the "pattern" attribute.  This is an
 * ant-style pattern matcher against the absolute path of the file (or directory).  By default, all files are included.
 *
 * <p>Once the initial list is passed through the "include" filter, it will be passed through an "exclude" filter. There is a set of known jars that by default
 * will not be copied to the "lib" directory.  These include the jars that ship by default with the JDK and the jars that are known to be build-time-only jars
 * for Enunciate.  You can disable the default filter with the "excludeDefaultLibs" attribute of the "war" element. You can also specify additional jars that
 * are to be excluded with an arbitrary number of "excludeLibs" child elements under the "war" element in the configuration file.  The "excludeLibs" element
 * supports either a "pattern" attribute or a "file" attribute.  The "pattern" attribute is an ant-style pattern matcher against the absolute path of the
 * file (or directory) on the classpath that should not be copied to the destination war.  The "file" attribute refers to a specific file on the filesystem
 * (relative paths are resolved relative to the configuration file). Furthermore, the "excludeLibs" element supports a "includeInManifest" attribute specifying
 * whether the exclude should be listed in the "Class-Path" attribute of the manifest, even though they are excluded in the war.  The is useful if, for example,
 * you're assembling an "ear" with multiple war files. By default, excluded jars are not included in the manifest.</p>
 *
 * <p>You can customize the manifest for the war by the "manifest" element of the "war" element.  Underneath the "manifest" element can be an arbitrary number
 * of "attribute" elements that can be used to specify the manifest attributes.  Each "attribute" element supports a "name" attribute, a "value" attribute, and
 * a "section" attribute.  If no section is specified, the default section is assumed.  If there is no "Class-Path" attribute in the main section, one will be
 * provided listing the jars on the classpath.</p>
 *
 * <h3><a name="config_springImport">The "springImport" element</a></h3>
 *
 * <p>The "springImport" element is used to specify a spring configuration file that will be imported by the main
 * spring servlet config. It supports the following attributes:</p>
 *
 * <ul>
 * <li>The "file" attribute specifies the spring import file on the filesystem.  It will be copied to the WEB-INF directory.</li>
 * <li>The "uri" attribute specifies the URI to the spring import file.  The URI will not be resolved at compile-time, nor will anything be copied to the
 * WEB-INF directory. The value of this attribute will be used to reference the spring import file in the main config file.  This attribute is useful
 * to specify an import file on the classpath, e.g. "classpath:com/myco/spring/config.xml".</li>
 * </ul>
 *
 * <p>One use of specifying spring a import file is to wrap your endpoints with spring interceptors and/or XFire in/out/fault handlers.  This can be done
 * by simply declaring a bean that is an instance of your endpoint class.  This bean can be advised as needed, and if it implements
 * org.codehaus.xfire.handler.HandlerSupport (perhaps <a href="http://static.springframework.org/spring/docs/1.2.x/reference/aop.html#d0e4128">through the use
 * of a mixin</a>?), the in/out/fault handlers will be used for the XFire invocation of that endpoint.</p>
 *
 * <p>It's important to note that the type on which the bean context will be searched is the type of the endpoint <i>interface</i>, and then only if it exists.
 * If there are more than one beans that are assignable to the endpoint interface, the bean that is named the name of the service will be used.  Otherwise,
 * the deployment of your endpoint will fail.</p>
 *
 * <p>The same procedure can be used to specify the beans to use as REST endpoints, although the XFire in/out/fault handlers will be ignored.  In this case,
 * the bean context will be searched for each <i>REST interface</i> that the endpoint implements.  If there is a bean that implements that interface, it will
 * used instead of the default implementation.  If there is more than one, the bean that is named the same as the REST endpoint will be used.</p>
 *
 * <p>There also exists a mechanism to add certain AOP interceptors to all service endpoint beans.  Such interceptors are referred to as "global service
 * interceptors." This can be done by using the "globalServiceInterceptor" element (see below), or by simply creating an interceptor that implements
 * org.codehaus.enunciate.modules.spring_app.EnunciateServiceAdvice or org.codehaus.enunciate.modules.spring_app.EnunciateServiceAdvisor and declaring it in your
 * imported spring beans file.</p>
 *
 * <p>Each global interceptor has an order.  The default order is 0 (zero).  If a global service interceptor implements org.springframework.core.Ordered, the
 * order will be respected. As global service interceptors are added, it will be assigned a position in the chain according to it's order.  Interceptors
 * of the same order will be ordered together according to their position in the config file, with priority to those declared by the "globalServiceInterceptor"
 * element, then to instances of org.codehaus.enunciate.modules.spring_app.EnunciateServiceAdvice, then to instances of
 * org.codehaus.enunciate.modules.spring_app.EnunciateServiceAdvisor.</p>
 *
 * <p>For more information on spring bean configuration and interceptor advice, see
 * <a href="http://static.springframework.org/spring/docs/1.2.x/reference/index.html">the spring reference documentation</a>.</p>
 *
 * <h3><a name="config_globalServiceInterceptor">The "globalServiceInterceptor" element</a></h3>
 *
 * <p>The "globalServiceInterceptor" element is used to specify a Spring interceptor (instance of org.aopalliance.aop.Advice or
 * org.springframework.aop.Advisor) that is to be injected on all service endpoint beans.</p>
 *
 * <ul>
 * <li>The "interceptorClass" attribute specified the class of the interceptor.</p>
 * <li>The "beanName" attribute specifies the bean name of the interceptor.</p>
 * </ul>
 *
 * <h3><a name="config_handlerInterceptor">The "handlerInterceptor" element</a></h3>
 *
 * <p>The "handlerInterceptor" element is used to specify a Spring interceptor (instance of org.springframework.web.servlet.HandlerInterceptor)
 * that is to be injected on the handler mapping.</p>
 *
 * <ul>
 * <li>The "interceptorClass" attribute specifies the class of the interceptor.</p>
 * <li>The "beanName" attribute specifies the bean name of the interceptor.</p>
 * </ul>
 *
 * <p>For more information on spring bean configuration and interceptor advice, see
 * <a href="http://static.springframework.org/spring/docs/1.2.x/reference/index.html">the spring reference documentation</a>.</p>
 *
 * <h3><a name="config_handlerMapping">The "handlerMapping" element</a></h3>
 *
 * <p>The "handlerMapping" element is used to specify a custom Spring handler mapping.</p>
 *
 * <ul>
 * <li>The "pattern" attribute specifies the pattern that maps to the handler.</p>
 * <li>The "beanName" attribute specifies the bean name of the handler.</p>
 * </ul>
 *
 * <p>For more information on spring handler mappings, see
 * <a href="http://static.springframework.org/spring/docs/1.2.x/reference/index.html">the spring reference documentation</a>.</p>
 *
 * <h3><a name="config_copyResources">The "copyResources" element</a></h3>
 *
 * <p>The "copyResources" element is used to specify a pattern of resources to copy to the compile directory.  It supports the following attributes:</p>
 *
 * <ul>
 * <li>The "<b>dir</b>" attribute specifies the base directory of the resources to copy.</li>
 * <li>The "<b>pattern</b>" attribute specifies an <a href="http://ant.apache.org/">Ant</a>-style
 * pattern used to find the resources to copy.  For more information, see the documentation for the
 * <a href="http://static.springframework.org/spring/docs/1.2.x/api/org/springframework/util/AntPathMatcher.html">ant path matcher</a> in the Spring
 * JavaDocs.</li>
 * </ul>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The spring app deployment module exports the following artifacts:</p>
 *
 * <ul>
 * <li>The "spring.app.dir" artifact is the (expanded) web app directory, exported during the build step.</li>
 * <li>The "spring.war.file" artifact is the packaged war, exported during the package step.</li>
 * </ul>
 *
 * @author Ryan Heaton
 * @docFileName module_spring_app.html
 */
public class SpringAppDeploymentModule extends FreemarkerDeploymentModule {

  private WarConfig warConfig;
  private final List<SpringImport> springImports = new ArrayList<SpringImport>();
  private final List<CopyResources> copyResources = new ArrayList<CopyResources>();
  private final List<GlobalServiceInterceptor> globalServiceInterceptors = new ArrayList<GlobalServiceInterceptor>();
  private final List<HandlerInterceptor> handlerInterceptors = new ArrayList<HandlerInterceptor>();
  private final Map<String, String> customHandlerMappings = new HashMap<String, String>();
  private boolean compileDebugInfo = true;
  private String defaultAutowire = null;
  private String defaultDependencyCheck = null;
  private String contextLoaderListenerClass = "org.springframework.web.context.ContextLoaderListener";
  private String dispatcherServletClass = "org.springframework.web.servlet.DispatcherServlet";
  private boolean doCompile = true;
  private boolean doLibCopy = true;
  private boolean doPackage = true;
  private boolean enableSecurity = false;
  private SecurityConfig securityConfig = new SecurityConfig();

  /**
   * @return "spring-app"
   */
  @Override
  public String getName() {
    return "spring-app";
  }

  /**
   * @return The URL to "spring-servlet.fmt"
   */
  protected URL getSpringServletTemplateURL() {
    return SpringAppDeploymentModule.class.getResource("spring-servlet.fmt");
  }

  /**
   * @return The URL to "spring-servlet.fmt"
   */
  protected URL getApplicationContextTemplateURL() {
    return SpringAppDeploymentModule.class.getResource("applicationContext.xml.fmt");
  }

  /**
   * @return The URL to "spring-security-context.xml.fmt"
   */
  protected URL getSecurityTemplateURL() {
    return SpringAppDeploymentModule.class.getResource("spring-security-context.xml.fmt");
  }

  /**
   * @return The URL to "spring-security-oauth-context.xml.fmt"
   */
  protected URL getOAuthTemplateURL() {
    return SpringAppDeploymentModule.class.getResource("spring-security-oauth-context.xml.fmt");
  }

  /**
   * @return The URL to "web.xml.fmt"
   */
  protected URL getWebXmlTemplateURL() {
    return SpringAppDeploymentModule.class.getResource("web.xml.fmt");
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    if (!enunciate.isUpToDateWithSources(getConfigGenerateDir())) {
      EnunciateFreemarkerModel model = getModel();

      //generate the spring-servlet.xml
      model.setFileOutputDirectory(getConfigGenerateDir());
      model.put("springImports", getSpringImportURIs());
      model.put("defaultDependencyCheck", getDefaultDependencyCheck());
      model.put("defaultAutowire", getDefaultAutowire());
      model.put("springContextLoaderListenerClass", getContextLoaderListenerClass());
      model.put("springDispatcherServletClass", getDispatcherServletClass());
      model.put("soapAddressPath", new SoapAddressPathMethod());
      model.put("restSubcontext", model.getEnunciateConfig().getDefaultRestSubcontext());
      model.put("displayName", model.getEnunciateConfig().getLabel());
      if (!globalServiceInterceptors.isEmpty()) {
        for (GlobalServiceInterceptor interceptor : this.globalServiceInterceptors) {
          if ((interceptor.getBeanName() == null) && (interceptor.getInterceptorClass() == null)) {
            throw new IllegalStateException("A global interceptor must have either a bean name or a class set.");
          }
        }
        model.put("globalServiceInterceptors", this.globalServiceInterceptors);
      }
      if (!handlerInterceptors.isEmpty()) {
        for (HandlerInterceptor interceptor : this.handlerInterceptors) {
          if ((interceptor.getBeanName() == null) && (interceptor.getInterceptorClass() == null)) {
            throw new IllegalStateException("A handler interceptor must have either a bean name or a class set.");
          }
        }
        model.put("handlerInterceptors", this.handlerInterceptors);
      }

      model.put("customHandlerMappings", this.customHandlerMappings);
      model.put("xfireEnabled", getEnunciate().isModuleEnabled("xfire"));
      model.put("restEnabled", getEnunciate().isModuleEnabled("rest"));
      model.put("gwtEnabled", getEnunciate().isModuleEnabled("gwt"));
      model.put("amfEnabled", getEnunciate().isModuleEnabled("amf"));

      String docsDir = "";
      if ((this.warConfig != null) && (this.warConfig.getDocsDir() != null)) {
        docsDir = this.warConfig.getDocsDir().trim();
        if ((!"".equals(docsDir)) && (!docsDir.endsWith("/"))) {
          docsDir = docsDir + "/";
        }
      }
      model.put("docsDir", docsDir);

      //spring security configuration:
      model.put("securityEnabled", isEnableSecurity());
      model.put("hasSecureMethod", new HasSecureMethod());
      SecurityConfig securityConfig = getSecurityConfig();
      if (securityConfig.getRealmName() == null) {
        String realmName = "Generic Enunciate Application Realm";
        EnunciateConfiguration enunciateConfig = enunciate.getConfig();
        if (enunciateConfig.getDescription() != null) {
          realmName = enunciateConfig.getDescription();
        }
        securityConfig.setRealmName(realmName);
      }

      if (securityConfig.getKey() == null) {
        securityConfig.setKey(String.valueOf(System.currentTimeMillis()));
      }
      model.put("securityConfig", securityConfig);

      processTemplate(getApplicationContextTemplateURL(), model);
      processTemplate(getSpringServletTemplateURL(), model);
      processTemplate(getWebXmlTemplateURL(), model);
      if (isEnableSecurity()) {
        processTemplate(getSecurityTemplateURL(), model);

        if (getSecurityConfig().isEnableOAuth()) {
          processTemplate(getOAuthTemplateURL(), model);
        }
      }
    }
    else {
      info("Skipping generation of spring config files as everything appears up-to-date...");
    }
  }

  @Override
  protected void doCompile() throws EnunciateException, IOException {
    if (!isDoCompile()) {
      info("Compilation has been disabled.  No server-side classes will be compiled, nor will any resources be copied.");
      return;
    }

    ArrayList<String> javacAdditionalArgs = new ArrayList<String>();
    if (compileDebugInfo) {
      javacAdditionalArgs.add("-g");
    }

    Enunciate enunciate = getEnunciate();
    final File compileDir = getCompileDir();
    if (!enunciate.isUpToDateWithSources(compileDir)) {
      enunciate.invokeJavac(enunciate.getEnunciateClasspath(), "1.5", compileDir, javacAdditionalArgs, enunciate.getSourceFiles());

      File jaxwsSources = (File) enunciate.getProperty("jaxws.src.dir");
      if (jaxwsSources != null) {
        info("Compiling the JAX-WS support classes found in %s...", jaxwsSources);
        Collection<String> jaxwsSourceFiles = new ArrayList<String>(enunciate.getJavaFiles(jaxwsSources));

        File xfireSources = (File) enunciate.getProperty("xfire-server.src.dir");
        if (xfireSources != null) {
          //make sure we include all the wrappers generated for the rpc methods, too...
          jaxwsSourceFiles.addAll(enunciate.getJavaFiles(xfireSources));
        }

        if (!jaxwsSourceFiles.isEmpty()) {
          StringBuilder jaxwsClasspath = new StringBuilder(enunciate.getEnunciateClasspath());
          jaxwsClasspath.append(File.pathSeparator).append(compileDir.getAbsolutePath());
          enunciate.invokeJavac(jaxwsClasspath.toString(), "1.5", compileDir, javacAdditionalArgs, jaxwsSourceFiles.toArray(new String[jaxwsSourceFiles.size()]));
        }
        else {
          info("No JAX-WS source files have been found to compile.");
        }
      }
      else {
        info("No JAX-WS source directory has been found.  SOAP services disabled.");
      }

      File gwtSources = (File) enunciate.getProperty("gwt.server.src.dir");
      if (gwtSources != null) {
        info("Copying the GWT client classes to %s...", compileDir);
        File gwtClientCompileDir = (File) enunciate.getProperty("gwt.client.compile.dir");
        if (gwtClientCompileDir == null) {
          throw new EnunciateException("Required dependency on the GWT client classes not found.");
        }
        enunciate.copyDir(gwtClientCompileDir, compileDir);

        info("Compiling the GWT support classes found in %s...", gwtSources);
        Collection<String> gwtSourceFiles = new ArrayList<String>(enunciate.getJavaFiles(gwtSources));
        StringBuilder gwtClasspath = new StringBuilder(enunciate.getEnunciateClasspath());
        gwtClasspath.append(File.pathSeparator).append(compileDir.getAbsolutePath());
        enunciate.invokeJavac(gwtClasspath.toString(), "1.5", compileDir, javacAdditionalArgs, gwtSourceFiles.toArray(new String[gwtSourceFiles.size()]));
      }

      File amfSources = (File) enunciate.getProperty("amf.server.src.dir");
      if (amfSources != null) {
        info("Compiling the AMF support classes found in %s...", amfSources);
        Collection<String> amfSourceFiles = new ArrayList<String>(enunciate.getJavaFiles(amfSources));
        StringBuilder amfClasspath = new StringBuilder(enunciate.getEnunciateClasspath());
        amfClasspath.append(File.pathSeparator).append(compileDir.getAbsolutePath());
        enunciate.invokeJavac(amfClasspath.toString(), "1.5", compileDir, javacAdditionalArgs, amfSourceFiles.toArray(new String[amfSourceFiles.size()]));
      }

      File restParamterNames = (File) enunciate.getProperty("rest.parameter.names");
      if (restParamterNames != null) {
        enunciate.copyFile(restParamterNames, new File(compileDir, "enunciate-rest-parameter-names.properties"));
      }

      if (!this.copyResources.isEmpty()) {
        AntPathMatcher matcher = new AntPathMatcher();
        for (CopyResources copyResource : this.copyResources) {
          String pattern = copyResource.getPattern();
          if (pattern == null) {
            throw new EnunciateException("A pattern must be specified for copying resources.");
          }

          if (!matcher.isPattern(pattern)) {
            warn("'%s' is not a valid pattern.  Resources NOT copied!", pattern);
            continue;
          }

          File basedir;
          if (copyResource.getDir() == null) {
            File configFile = enunciate.getConfigFile();
            if (configFile != null) {
              basedir = configFile.getAbsoluteFile().getParentFile();
            }
            else {
              basedir = new File(System.getProperty("user.dir"));
            }
          }
          else {
            basedir = enunciate.resolvePath(copyResource.getDir());
          }

          for (String file : enunciate.getFiles(basedir, new PatternFileFilter(basedir, pattern, matcher))) {
            enunciate.copyFile(new File(file), basedir, compileDir);
          }
        }
      }
    }
    else {
      info("Skipping compilation as everything appears up-to-date...");
    }
  }

  @Override
  protected void doBuild() throws IOException, EnunciateException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();

    if (!enunciate.isUpToDateWithSources(buildDir)) {
      copyPreBase();

      info("Building the expanded WAR in %s", buildDir);

      if (isDoCompile()) {
        //copy the compiled classes to WEB-INF/classes.
        File webinf = new File(buildDir, "WEB-INF");
        File webinfClasses = new File(webinf, "classes");
        enunciate.copyDir(getCompileDir(), webinfClasses);
      }

      if (isDoLibCopy()) {
        doLibCopy();
      }
      else {
        info("Lib copy has been disabled.  No libs will be copied, nor any manifest written.");
      }

      copyWebXml();

      copySpringConfig();

      copyDocs();

      copyGwtApps();

      copyAmfConfig();

      copyFlexApps();

      if (isEnableSecurity()) {
        createSecurityUI();
      }

      copyPostBase();
    }
    else {
      info("Skipping the build of the expanded war as everything appears up-to-date...");
    }

    //export the expanded application directory.
    enunciate.addArtifact(new FileArtifact(getName(), "spring.app.dir", buildDir));
  }

  /**
   * Copy the post base.
   */
  protected void copyPostBase() throws IOException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    //extract a post base if specified.
    if ((this.warConfig != null) && (this.warConfig.getPostBase() != null)) {
      File postBase = enunciate.resolvePath(this.warConfig.getPostBase());
      if (postBase.isDirectory()) {
        info("Copying postBase directory %s to %s...", postBase, buildDir);
        enunciate.copyDir(postBase, buildDir);
      }
      else {
        info("Extracting postBase zip file %s to %s...", postBase, buildDir);
        enunciate.extractBase(new FileInputStream(postBase), buildDir);
      }
    }
  }

  /**
   * Copy the pre base.
   */
  protected void copyPreBase() throws IOException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    if ((this.warConfig != null) && (this.warConfig.getPreBase() != null)) {
      File preBase = enunciate.resolvePath(this.warConfig.getPreBase());
      if (preBase.isDirectory()) {
        info("Copying preBase directory %s to %s...", preBase, buildDir);
        enunciate.copyDir(preBase, buildDir);
      }
      else {
        info("Extracting preBase zip file %s to %s...", preBase, buildDir);
        enunciate.extractBase(new FileInputStream(preBase), buildDir);
      }
    }
  }

  /**
   * Create the UI pages for security as needed (e.g. login page).
   */
  protected void createSecurityUI() throws IOException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    File webinf = new File(buildDir, "WEB-INF");
    File jspDir = new File(webinf, "jsp");
    jspDir.mkdirs();
    if (getSecurityConfig().isEnableFormBasedLogin()) {
      //form-based login is enabled; we'll use the login page.
      File loginPageFile = null;
      FormBasedLoginConfig formBasedLoginConfig = getSecurityConfig().getFormBasedLoginConfig();
      if (formBasedLoginConfig != null) {
        if (formBasedLoginConfig.getLoginPageFile() != null) {
          loginPageFile = enunciate.resolvePath(formBasedLoginConfig.getLoginPageFile());
        }
      }

      if (loginPageFile != null) {
        enunciate.copyFile(loginPageFile, new File(jspDir, "login.jsp"));
      }
      else {
        enunciate.copyResource("/org/codehaus/enunciate/modules/spring_app/jsp/login.jsp", new File(jspDir, "login.jsp"));
      }
    }

    if (getSecurityConfig().isEnableOAuth()) {
      OAuthConfig oauthConfig = getSecurityConfig().getOAuthConfig();

      //copy the OAuth information page.
      File infoPageFile = null;
      if (oauthConfig != null) {
        if (oauthConfig.getInfoPageFile() != null) {
          infoPageFile = enunciate.resolvePath(oauthConfig.getInfoPageFile());
        }
      }

      if (infoPageFile != null) {
        enunciate.copyFile(infoPageFile, new File(jspDir, "oauth_info.jsp"));
      }
      else {
        enunciate.copyResource("/org/codehaus/enunciate/modules/spring_app/jsp/oauth.jsp", new File(jspDir, "oauth_info.jsp"));
      }

      //copy the OAuth access confirmation page.
      File confirmAccessPageFile = null;
      if (oauthConfig != null) {
        if (oauthConfig.getConfirmAccessPageFile() != null) {
          confirmAccessPageFile = enunciate.resolvePath(oauthConfig.getConfirmAccessPageFile());
        }
      }

      if (confirmAccessPageFile != null) {
        enunciate.copyFile(confirmAccessPageFile, new File(jspDir, "confirm_access.jsp"));
      }
      else {
        enunciate.copyResource("/org/codehaus/enunciate/modules/spring_app/jsp/confirm_access.jsp", new File(jspDir, "confirm_access.jsp"));
      }

      //copy the OAuth access confirmed page.
      File accessConfirmedPageFile = null;
      if (oauthConfig != null) {
        if (oauthConfig.getAccessConfirmedPageFile() != null) {
          accessConfirmedPageFile = enunciate.resolvePath(oauthConfig.getAccessConfirmedPageFile());
        }
      }

      if (accessConfirmedPageFile != null) {
        enunciate.copyFile(accessConfirmedPageFile, new File(jspDir, "access_confirmed.jsp"));
      }
      else {
        enunciate.copyResource("/org/codehaus/enunciate/modules/spring_app/jsp/access_confirmed.jsp", new File(jspDir, "access_confirmed.jsp"));
      }
    }
  }

  /**
   * Copy any flex apps.
   */
  protected void copyFlexApps() throws IOException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    File flexAppDir = (File) enunciate.getProperty("flex.app.dir");
    if (flexAppDir != null) {
      File flexAppDest = buildDir;
      if ((this.warConfig != null) && (this.warConfig.getFlexAppDir() != null)) {
        flexAppDest = new File(buildDir, this.warConfig.getFlexAppDir());
      }
      enunciate.copyDir(flexAppDir, flexAppDest);
    }
    else {
      info("No FLEX application directory was found.  Skipping the copy...");
    }
  }

  /**
   * Copy the AMF configuration.
   */
  protected void copyAmfConfig() throws IOException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    File webinf = new File(buildDir, "WEB-INF");
    File amfXmlDir = (File) enunciate.getProperty("amf.xml.dir");
    if (amfXmlDir != null) {
      File servicesConfigFile = new File(amfXmlDir, "services-config.xml");
      if (servicesConfigFile.exists()) {
        enunciate.copyFile(servicesConfigFile, new File(new File(webinf, "flex"), "services-config.xml"));
      }
      else {
        warn("No services configuration file found.  Skipping the copy...");
      }
    }
    else {
      info("No AMF configuration directory was found.  Skipping the copy...");
    }
  }

  /**
   * Copy the GWT apps.
   */
  protected void copyGwtApps() throws IOException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    File gwtAppDir = (File) enunciate.getProperty("gwt.app.dir");
    if (gwtAppDir != null) {
      File gwtAppDest = buildDir;
      if ((this.warConfig != null) && (this.warConfig.getGwtAppDir() != null)) {
        gwtAppDest = new File(buildDir, this.warConfig.getGwtAppDir());
      }
      enunciate.copyDir(gwtAppDir, gwtAppDest);
    }
    else {
      info("No GWT application directory was found.  Skipping the copy...");
    }
  }

  /**
   * Copy the documentation.
   *
   * @throws IOException
   */
  protected void copyDocs() throws IOException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    Artifact artifact = enunciate.findArtifact("docs");
    if (artifact != null) {
      File docsDir = buildDir;
      if ((this.warConfig != null) && (this.warConfig.getDocsDir() != null)) {
        docsDir = new File(buildDir, this.warConfig.getDocsDir());
        docsDir.mkdirs();
      }

      artifact.exportTo(docsDir, enunciate);
    }
    else {
      warn("WARNING: No documentation artifact found!");
    }
  }

  /**
   * Copy the spring application context and servlet config from the build dir to the WEB-INF directory.
   */
  protected void copySpringConfig() throws IOException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    File webinf = new File(buildDir, "WEB-INF");
    File configDir = getConfigGenerateDir();
    enunciate.copyFile(new File(configDir, "applicationContext.xml"), new File(webinf, "applicationContext.xml"));
    enunciate.copyFile(new File(configDir, "spring-servlet.xml"), new File(webinf, "spring-servlet.xml"));
    if (isEnableSecurity()) {
      enunciate.copyFile(new File(configDir, "spring-security-context.xml"), new File(webinf, "spring-security-context.xml"));

      if (getSecurityConfig().isEnableOAuth()) {
        enunciate.copyFile(new File(configDir, "spring-security-oauth-context.xml"), new File(webinf, "spring-security-oauth-context.xml"));
      }
    }

    for (SpringImport springImport : springImports) {
      //copy the extra spring import files to the WEB-INF directory to be imported.
      if (springImport.getFile() != null) {
        File importFile = enunciate.resolvePath(springImport.getFile());
        enunciate.copyFile(importFile, new File(webinf, importFile.getName()));
      }
    }
  }

  /**
   * Copies web.xml to WEB-INF. Pass it through a stylesheet, if specified.
   */
  protected void copyWebXml() throws IOException, EnunciateException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    File webinf = new File(buildDir, "WEB-INF");
    File configDir = getConfigGenerateDir();
    File webXML = new File(configDir, "web.xml");
    File destWebXML = new File(webinf, "web.xml");

    if ((this.warConfig != null) && (this.warConfig.getWebXMLTransformURL() != null)) {
      URL transformURL = this.warConfig.getWebXMLTransformURL();
      info("web.xml transform has been specified as %s.", transformURL);
      try {
        StreamSource source = new StreamSource(transformURL.openStream());
        Transformer transformer = new TransformerFactoryImpl().newTransformer(source);
        info("Transforming %s to %s.", webXML, destWebXML);
        transformer.transform(new StreamSource(new FileReader(webXML)), new StreamResult(destWebXML));
      }
      catch (TransformerException e) {
        throw new EnunciateException("Error during transformation of the web.xml (stylesheet " + transformURL + ", file " + webXML + ")", e);
      }
    }
    else {
      enunciate.copyFile(webXML, destWebXML);
    }
  }

  /**
   * Copies the classpath elements to WEB-INF.
   *
   * @throws IOException
   */
  protected void doLibCopy() throws IOException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    File webinf = new File(buildDir, "WEB-INF");
    File webinfClasses = new File(webinf, "classes");
    File webinfLib = new File(webinf, "lib");

    //initialize the include filters.
    AntPathMatcher pathMatcher = new AntPathMatcher();
    pathMatcher.setPathSeparator(File.separator);
    List<File> explicitIncludes = new ArrayList<File>();
    List<String> includePatterns = new ArrayList<String>();
    if (this.warConfig != null) {
      for (IncludeExcludeLibs el : this.warConfig.getIncludeLibs()) {
        if (el.getFile() != null) {
          //add explicit files to the include files list.
          explicitIncludes.add(el.getFile());
        }

        String pattern = el.getPattern();
        if (pattern != null) {
          //normalize the pattern to the platform.
          pattern = pattern.replace('/', File.separatorChar);
          if (pathMatcher.isPattern(pattern)) {
            //make sure that the includes pattern list only has patterns.
            includePatterns.add(pattern);
          }
          else {
            info("Pattern '%s' is not a valid pattern, so it will not be applied.", pattern);
          }
        }
      }
    }

    if (includePatterns.isEmpty()) {
      //if no include patterns are specified, the implicit pattern is "**/*".
      String starPattern = "**" + File.separatorChar + "*";
      debug("No include patterns have been specified.  Using the implicit '%s' pattern.", starPattern);
      includePatterns.add(starPattern);
    }

    List<String> warLibs = new ArrayList<String>();
    if (this.warConfig == null || this.warConfig.isIncludeClasspathLibs()) {
      debug("Using the Enunciate classpath as the initial list of libraries to be passed through the include/exclude filter.");
      //prime the list of libs to include in the war with what's on the enunciate classpath.
      warLibs.addAll(Arrays.asList(enunciate.getEnunciateClasspath().split(File.pathSeparator)));
    }

    // Apply the "in filter" (i.e. the filter that specifies the files to be included).
    List<File> includedLibs = new ArrayList<File>();
    for (String warLib : warLibs) {
      File libFile = new File(warLib);
      if (libFile.exists()) {
        for (String includePattern : includePatterns) {
          String absolutePath = libFile.getAbsolutePath();
          if (absolutePath.startsWith(File.separator)) {
            //lob off the beginning "/" for Linux boxes.
            absolutePath = absolutePath.substring(1);
          }
          if (pathMatcher.match(includePattern, absolutePath)) {
            debug("Library '%s' passed the include filter. It matches pattern '%s'.", libFile.getAbsolutePath(), includePattern);
            includedLibs.add(libFile);
            break;
          }
          else if (enunciate.isDebug()) {
            debug("Library '%s' did NOT match include pattern '%s'.", includePattern);
          }
        }
      }
    }

    //Now, with what's left, apply the "exclude filter".
    boolean excludeDefaults = this.warConfig == null || this.warConfig.isExcludeDefaultLibs();
    List<String> manifestClasspath = new ArrayList<String>();
    Iterator<File> toBeIncludedIt = includedLibs.iterator();
    while (toBeIncludedIt.hasNext()) {
      File toBeIncluded = toBeIncludedIt.next();
      if (excludeDefaults && knownExclude(toBeIncluded)) {
        toBeIncludedIt.remove();
      }
      else if (this.warConfig != null) {
        for (IncludeExcludeLibs excludeLibs : this.warConfig.getExcludeLibs()) {
          boolean exclude = false;
          if ((excludeLibs.getFile() != null) && (excludeLibs.getFile().equals(toBeIncluded))) {
            exclude = true;
            debug("%s was explicitly excluded.", toBeIncluded);
          }
          else {
            String pattern = excludeLibs.getPattern();
            if (pattern != null) {
              pattern = pattern.replace('/', File.separatorChar);
              if (pathMatcher.isPattern(pattern)) {
                String absolutePath = toBeIncluded.getAbsolutePath();
                if (absolutePath.startsWith(File.separator)) {
                  //lob off the beginning "/" for Linux boxes.
                  absolutePath = absolutePath.substring(1);
                }

                if (pathMatcher.match(pattern, absolutePath)) {
                  exclude = true;
                  debug("%s was excluded because it matches pattern '%s'", toBeIncluded, pattern);
                }
              }
            }
          }

          if (exclude) {
            toBeIncludedIt.remove();
            if ((excludeLibs.isIncludeInManifest()) && (!toBeIncluded.isDirectory())) {
              //include it in the manifest anyway.
              manifestClasspath.add(toBeIncluded.getName());
              debug("'%s' will be included in the manifest classpath.", toBeIncluded.getName());
            }
            break;
          }
        }
      }
    }

    //now add the lib files that are explicitly included.
    includedLibs.addAll(explicitIncludes);

    //now we've got the final list, copy the libs.
    for (File includedLib : includedLibs) {
      if (includedLib.isDirectory()) {
        info("Adding the contents of %s to WEB-INF/classes.", includedLib);
        enunciate.copyDir(includedLib, webinfClasses);
      }
      else {
        info("Including %s in WEB-INF/lib.", includedLib);
        enunciate.copyFile(includedLib, includedLib.getParentFile(), webinfLib);
      }
    }

    // write the manifest file.
    Manifest manifest = this.warConfig == null ? WarConfig.getDefaultManifest() : this.warConfig.getManifest();
    if ((manifestClasspath.size() > 0) && (manifest.getMainAttributes().getValue("Class-Path") == null)) {
      StringBuilder manifestClasspathValue = new StringBuilder();
      Iterator<String> manifestClasspathIt = manifestClasspath.iterator();
      while (manifestClasspathIt.hasNext()) {
        String entry = manifestClasspathIt.next();
        manifestClasspathValue.append(entry);
        if (manifestClasspathIt.hasNext()) {
          manifestClasspathValue.append(" ");
        }
      }
      manifest.getMainAttributes().putValue("Class-Path", manifestClasspathValue.toString());
    }
    File metaInf = new File(buildDir, "META-INF");
    metaInf.mkdirs();
    FileOutputStream manifestFileOut = new FileOutputStream(new File(metaInf, "MANIFEST.MF"));
    manifest.write(manifestFileOut);
    manifestFileOut.flush();
    manifestFileOut.close();
  }

  @Override
  protected void doPackage() throws EnunciateException, IOException {
    if (isDoPackage()) {
      File buildDir = getBuildDir();
      File warFile = getWarFile();
      Enunciate enunciate = getEnunciate();

      if (!enunciate.isUpToDate(buildDir, warFile)) {
        if (!warFile.getParentFile().exists()) {
          warFile.getParentFile().mkdirs();
        }

        info("Creating %s", warFile.getAbsolutePath());

        enunciate.zip(warFile, buildDir);
      }
      else {
        info("Skipping war file creation as everything appears up-to-date...");
      }

      enunciate.addArtifact(new FileArtifact(getName(), "spring.war.file", warFile));
    }
    else {
      info("Packaging has been disabled.  No packaging will be performed.");
    }
  }

  /**
   * The configuration for the war.
   *
   * @return The configuration for the war.
   */
  public WarConfig getWarConfig() {
    return warConfig;
  }

  /**
   * The war file to create.
   *
   * @return The war file to create.
   */
  public File getWarFile() {
    String filename = "enunciate.war";
    if (getEnunciate().getConfig().getLabel() != null) {
      filename = getEnunciate().getConfig().getLabel() + ".war";
    }

    if ((this.warConfig != null) && (this.warConfig.getName() != null)) {
      filename = this.warConfig.getName();
    }

    return new File(getPackageDir(), filename);
  }

  /**
   * Set the configuration for the war.
   *
   * @param warConfig The configuration for the war.
   */
  public void setWarConfig(WarConfig warConfig) {
    this.warConfig = warConfig;
  }

  /**
   * Get the string form of the spring imports that have been configured.
   *
   * @return The string form of the spring imports that have been configured.
   */
  protected ArrayList<String> getSpringImportURIs() {
    ArrayList<String> springImportURIs = new ArrayList<String>(this.springImports.size());
    for (SpringImport springImport : springImports) {
      if (springImport.getFile() != null) {
        if (springImport.getUri() != null) {
          throw new IllegalStateException("A spring import configuration must specify a file or a URI, but not both.");
        }

        springImportURIs.add(new File(springImport.getFile()).getName());
      }
      else if (springImport.getUri() != null) {
        springImportURIs.add(springImport.getUri());
      }
      else {
        throw new IllegalStateException("A spring import configuration must specify either a file or a URI.");
      }
    }
    return springImportURIs;
  }

  /**
   * Add a spring import.
   *
   * @param springImports The spring import to add.
   */
  public void addSpringImport(SpringImport springImports) {
    this.springImports.add(springImports);
  }

  /**
   * Add a copy resources.
   *
   * @param copyResources The copy resources to add.
   */
  public void addCopyResources(CopyResources copyResources) {
    this.copyResources.add(copyResources);
  }

  /**
   * Add a global service interceptor to the spring configuration.
   *
   * @param interceptorConfig The interceptor configuration.
   */
  public void addGlobalServiceInterceptor(GlobalServiceInterceptor interceptorConfig) {
    this.globalServiceInterceptors.add(interceptorConfig);
  }

  /**
   * Add a handler interceptor to the spring configuration.
   *
   * @param interceptorConfig The interceptor configuration.
   */
  public void addHandlerInterceptor(HandlerInterceptor interceptorConfig) {
    this.handlerInterceptors.add(interceptorConfig);
  }

  /**
   * Add a custom handler mapping to the Spring handler.
   *
   * @param pattern The pattern.
   * @param beanName The bean name.
   */
  public void addCustomHandlerMapping(String pattern, String beanName) {
    if (pattern == null) {
      throw new IllegalArgumentException("A pattern must be supplied to a custom handler mapping.");
    }

    if (beanName == null) {
      throw new IllegalArgumentException("The bean name for a custom handler must be supplied to a custom handler mapping.");
    }

    this.customHandlerMappings.put(pattern, beanName);
  }

  /**
   * The value for the spring default autowiring.
   *
   * @return The value for the spring default autowiring.
   */
  public String getDefaultAutowire() {
    return defaultAutowire;
  }

  /**
   * The value for the spring default autowiring.
   *
   * @param defaultAutowire The value for the spring default autowiring.
   */
  public void setDefaultAutowire(String defaultAutowire) {
    this.defaultAutowire = defaultAutowire;
  }

  /**
   * The value for the spring default dependency checking.
   *
   * @return The value for the spring default dependency checking.
   */
  public String getDefaultDependencyCheck() {
    return defaultDependencyCheck;
  }

  /**
   * The value for the spring default dependency checking.
   *
   * @param defaultDependencyCheck The value for the spring default dependency checking.
   */
  public void setDefaultDependencyCheck(String defaultDependencyCheck) {
    this.defaultDependencyCheck = defaultDependencyCheck;
  }

  /**
   * The class to use as the context loader listener.
   *
   * @return The class to use as the context loader listener.
   */
  public String getContextLoaderListenerClass() {
    return contextLoaderListenerClass;
  }

  /**
   * The class to use as the context loader listener.
   *
   * @param contextLoaderListenerClass The class to use as the context loader listener.
   */
  public void setContextLoaderListenerClass(String contextLoaderListenerClass) {
    this.contextLoaderListenerClass = contextLoaderListenerClass;
  }

  /**
   * The class to use as the dispatcher servlet.
   *
   * @return The class to use as the dispatcher servlet.
   */
  public String getDispatcherServletClass() {
    return dispatcherServletClass;
  }

  /**
   * The class to use as the dispatcher servlet.
   *
   * @param dispatcherServletClass The class to use as the dispatcher servlet.
   */
  public void setDispatcherServletClass(String dispatcherServletClass) {
    this.dispatcherServletClass = dispatcherServletClass;
  }

  /**
   * whether this module should take on the responsibility of compiling the server-side classes.
   *
   * @return whether this module should take on the responsibility of compiling the server-side classes
   */
  public boolean isDoCompile() {
    return doCompile;
  }

  /**
   * whether this module should take on the responsibility of compiling the server-side classes
   *
   * @param doCompile whether this module should take on the responsibility of compiling the server-side classes
   */
  public void setDoCompile(boolean doCompile) {
    this.doCompile = doCompile;
  }

  /**
   * whether this module should take on the responsibility of copying libraries to WEB-INF/lib.
   *
   * @return whether this module should take on the responsibility of copying libraries to WEB-INF/lib
   */
  public boolean isDoLibCopy() {
    return doLibCopy;
  }

  /**
   * whether this module should take on the responsibility of copying libraries to WEB-INF/lib
   *
   * @param doLibCopy whether this module should take on the responsibility of copying libraries to WEB-INF/lib
   */
  public void setDoLibCopy(boolean doLibCopy) {
    this.doLibCopy = doLibCopy;
  }

  /**
   * whether this module should take on the responsibility of packaging (zipping) up the war
   *
   * @return whether this module should take on the responsibility of packaging (zipping) up the war
   */
  public boolean isDoPackage() {
    return doPackage;
  }

  /**
   * whether this module should take on the responsibility of packaging (zipping) up the war
   *
   * @param doPackage whether this module should take on the responsibility of packaging (zipping) up the war
   */
  public void setDoPackage(boolean doPackage) {
    this.doPackage = doPackage;
  }

  /**
   * Whether to enable security.
   *
   * @return Whether to enable security.
   */
  public boolean isEnableSecurity() {
    return enableSecurity;
  }

  /**
   * Whether to enable security.
   *
   * @param enableSecurity Whether to enable security.
   */
  public void setEnableSecurity(boolean enableSecurity) {
    this.enableSecurity = enableSecurity;
  }

  /**
   * The spring security configuration.
   *
   * @return The spring security configuration.
   */
  public SecurityConfig getSecurityConfig() {
    return securityConfig;
  }

  /**
   * The spring security configuration.
   *
   * @param securityConfig The spring security configuration.
   */
  public void setSecurityConfig(SecurityConfig securityConfig) {
    this.securityConfig = securityConfig;
  }

  /**
   * Whether to exclude a file from copying to the WEB-INF/lib directory.
   *
   * @param file The file to exclude.
   * @return Whether to exclude a file from copying to the lib directory.
   */
  protected boolean knownExclude(File file) throws IOException {
    //instantiate a loader with this library only in its path...
    URLClassLoader loader = new URLClassLoader(new URL[]{file.toURL()}, null);
    if (loader.findResource("META-INF/enunciate/preserve-in-war") != null) {
      debug("%s is a known include because it contains the entry META-INF/enunciate/preserve-in-war.", file);
      //if a jar happens to have the enunciate "preserve-in-war" file, it is NOT excluded.
      return false;
    }
    else if (loader.findResource(com.sun.tools.apt.Main.class.getName().replace('.', '/').concat(".class")) != null) {
      debug("%s is a known exclude because it appears to be tools.jar.", file);
      //exclude tools.jar.
      return true;
    }
    else if (loader.findResource(net.sf.jelly.apt.Context.class.getName().replace('.', '/').concat(".class")) != null) {
      debug("%s is a known exclude because it appears to be apt-jelly.", file);
      //exclude apt-jelly-core.jar
      return true;
    }
    else if (loader.findResource(net.sf.jelly.apt.freemarker.FreemarkerModel.class.getName().replace('.', '/').concat(".class")) != null) {
      debug("%s is a known exclude because it appears to be the apt-jelly-freemarker libs.", file);
      //exclude apt-jelly-freemarker.jar
      return true;
    }
    else if (loader.findResource(freemarker.template.Configuration.class.getName().replace('.', '/').concat(".class")) != null) {
      debug("%s is a known exclude because it appears to be the freemarker libs.", file);
      //exclude freemarker.jar
      return true;
    }
    else if (loader.findResource(Enunciate.class.getName().replace('.', '/').concat(".class")) != null) {
      debug("%s is a known exclude because it appears to be the enunciate core jar.", file);
      //exclude enunciate-core.jar
      return true;
    }
    else if (loader.findResource("javax/servlet/ServletContext.class") != null) {
      debug("%s is a known exclude because it appears to be the servlet api.", file);
      //exclude the servlet api.
      return true;
    }
    else if (loader.findResource("org/codehaus/enunciate/modules/xfire_client/EnunciatedClientSoapSerializerHandler.class") != null) {
      debug("%s is a known exclude because it appears to be the enunciated xfire client tools jar.", file);
      //exclude xfire-client-tools
      return true;
    }
    else if (loader.findResource("javax/swing/SwingBeanInfoBase.class") != null) {
      debug("%s is a known exclude because it appears to be dt.jar.", file);
      //exclude dt.jar
      return true;
    }
    else if (loader.findResource("HTMLConverter.class") != null) {
      debug("%s is a known exclude because it appears to be htmlconverter.jar.", file);
      return true;
    }
    else if (loader.findResource("sun/tools/jconsole/JConsole.class") != null) {
      debug("%s is a known exclude because it appears to be jconsole.jar.", file);
      return true;
    }
    else if (loader.findResource("sun/jvm/hotspot/debugger/Debugger.class") != null) {
      debug("%s is a known exclude because it appears to be sa-jdi.jar.", file);
      return true;
    }
    else if (loader.findResource("sun/io/ByteToCharDoubleByte.class") != null) {
      debug("%s is a known exclude because it appears to be charsets.jar.", file);
      return true;
    }
    else if (loader.findResource("com/sun/deploy/ClientContainer.class") != null) {
      debug("%s is a known exclude because it appears to be deploy.jar.", file);
      return true;
    }
    else if (loader.findResource("com/sun/javaws/Globals.class") != null) {
      debug("%s is a known exclude because it appears to be javaws.jar.", file);
      return true;
    }
    else if (loader.findResource("javax/crypto/SecretKey.class") != null) {
      debug("%s is a known exclude because it appears to be jce.jar.", file);
      return true;
    }
    else if (loader.findResource("sun/net/www/protocol/https/HttpsClient.class") != null) {
      debug("%s is a known exclude because it appears to be jsse.jar.", file);
      return true;
    }
    else if (loader.findResource("sun/plugin/JavaRunTime.class") != null) {
      debug("%s is a known exclude because it appears to be plugin.jar.", file);
      return true;
    }
    else if (loader.findResource("com/sun/corba/se/impl/activation/ServerMain.class") != null) {
      debug("%s is a known exclude because it appears to be rt.jar.", file);
      return true;
    }
    else if (Service.providers(DeploymentModule.class, loader).hasNext()) {
      debug("%s is a known exclude because it appears to be an enunciate module.", file);
      //exclude by default any deployment module libraries.
      return true;
    }

    return false;
  }

  /**
   * Configure whether to compile with debug info (default: true).
   *
   * @param compileDebugInfo Whether to compile with debug info (default: true).
   */
  public void setCompileDebugInfo(boolean compileDebugInfo) {
    this.compileDebugInfo = compileDebugInfo;
  }

  /**
   * @return 200
   */
  @Override
  public int getOrder() {
    return 200;
  }

  @Override
  public RuleSet getConfigurationRules() {
    return new SpringAppRuleSet();
  }

  @Override
  public Validator getValidator() {
    return new SpringAppValidator();
  }

  /**
   * The directory where the config files are generated.
   *
   * @return The directory where the config files are generated.
   */
  protected File getConfigGenerateDir() {
    return new File(getGenerateDir(), "config");
  }

}

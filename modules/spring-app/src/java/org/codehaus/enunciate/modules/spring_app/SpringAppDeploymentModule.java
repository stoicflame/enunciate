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

package org.codehaus.enunciate.modules.spring_app;

import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import freemarker.template.TemplateException;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.Artifact;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.FileArtifact;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.spring_app.config.*;
import org.springframework.util.AntPathMatcher;
import sun.misc.Service;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * <h1>Spring App Module</h1>
 * <p/>
 * <p>The spring app deployment module produces the web app for hosting the API endpoints and documentation.</p>
 * <p/>
 * <p>The order of the spring app deployment module is 200, putting it after any of the other modules, including
 * the documentation deployment module.  The spring app deployment module maintains soft dependencies on the other
 * Enunciate modules.  If those modules are active, the spring app deployment modules will assemble their artifacts
 * into a <a href="http://www.springframework.org/">spring</a>-supported web application.</p>
 * <p/>
 * <ul>
 * <li><a href="#steps">steps</a></li>
 * <li><a href="#config">configuration</a></li>
 * <li><a href="#artifacts">artifacts</a></li>
 * </ul>
 * <p/>
 * <h1><a name="steps">Steps</a></h1>
 * <p/>
 * <h3>generate</h3>
 * <p/>
 * <p>The "generate" step generates the deployment descriptors, and the <a href="http://www.springframework.org/">Spring</a>
 * configuration file.  Refer to <a href="#config">configuration</a> to learn how to customize the deployment
 * descriptors and the spring config file.</p>
 * <p/>
 * <h3>compile</h3>
 * <p/>
 * <p>The "compile" step compiles all API source files, including the source files that were generated from other modules
 * (e.g. JAX-WS module and XFire module).</p>
 * <p/>
 * <h3>build</h3>
 * <p/>
 * <p>The "build" step assembles all the generated artifacts, compiled classes, and deployment descriptors into an (expanded)
 * war directory.</p>
 * <p/>
 * <p>All classes compiled in the compile step are copied to the WEB-INF/classes directory.</p>
 * <p/>
 * <p>A set of libraries are copied to the WEB-INF/lib directory.  This set of libraries can be specified in the
 * <a href="#config">configuration file</a>.  Unless specified otherwise in the configuration file, the
 * libraries copied will be filtered from the classpath specified to Enunciate at compile-time.  The filtered libraries
 * are those libraries that are determined to be specific to running the Enunciate compile-time engine.  All other
 * libraries on the classpath are assumed to be dependencies for the API and are therefore copied to WEB-INF/lib.
 * (If a directory is found on the classpath, its contents are copied to WEB-INF/classes.)</p>
 * <p/>
 * <p>The web.xml file is copied to the WEB-INF directory.  A tranformation can be applied to the web.xml file before the copy,
 * if specified in the config, allowing you to apply your own servlet filters, etc.  <i>Take care to preserve the existing elements
 * when applying a transformation to the web.xml file, as losing data will result in missing or malfunctioning endpoints.</i></p>
 * <p/>
 * <p>The spring-servlet.xml file is generated and copied to the WEB-INF directory.  You can specify other spring config files that
 * will be copied (and imported by the spring-servlet.xml file) in the configuration.  This option allows you to specify spring AOP
 * interceptors and XFire in/out handlers to wrap your endpoints, if desired.</p>
 * <p/>
 * <p>Finally, the documentation (if found) is copied to the base of the web app directory.</p>
 * <p/>
 * <h3>package</h3>
 * <p/>
 * <p>The "package" step packages the expanded war and exports it.</p>
 * <p/>
 * <h1><a name="config">Configuration</a></h1>
 * <p/>
 * <p>The configuration for the XFire deployment module is specified by the "xfire" child element under the "modules" element
 * of the enunciate configuration file.</p>
 * <p/>
 * <h3>Structure</h3>
 * <p/>
 * <p>The following example shows the structure of the configuration elements for this module.  Note that this shows only the structure.
 * Some configuration elements don't make sense when used together.</p>
 * <p/>
 * <code class="console">
 * &lt;enunciate&gt;
 * &nbsp;&nbsp;&lt;modules&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;spring-app compileDebugInfo="[true | false]" defaultDependencyCheck="[none | objects | simple | all]" defaultAutowire="[no | byName | byType | constructor | autodetect]"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;war name="..." webXMLTransform="..." webXMLTransformURL="..." preBase="..." postBase="..." includeDefaultLibs="[true|false]" excludeDefaultLibs="[true|false]"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;includeLibs pattern="..." file="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;includeLibs pattern="..." file="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;excludeLibs pattern="..." file="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;excludeLibs pattern="..." file="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
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
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;copyResources dir="..." pattern="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;copyResources dir="..." pattern="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/spring-app&gt;
 * &nbsp;&nbsp;&lt;/modules&gt;
 * &lt;/enunciate&gt;
 * </code>
 * <p/>
 * <h3>attributes</h3>
 * <p/>
 * <ul>
 * <li>The "<b>compileDebugInfo</b>" attribute specifies that the compiled classes should be compiled with debug info.  The default is "true."</li>
 * <li>The "<b>defaultDependencyCheck</b>" attribute specifies that value of the "default-dependency-check" for the generated spring file.</li>
 * <li>The "<b>defaultAutowire</b>" attribute specifies that value of the "default-autowire" for the generated spring file.</li>
 * </ul>
 * <p/>
 * <h3>The "war" element</h3>
 * <p/>
 * <p>The "war" element is used to specify configuration for the assembly of the war.  It supports the following attributes:</p>
 * <p/>
 * <ul>
 * <li>The "<b>name</b>" attribute specifies the name of the war.  The default is the enunciate configuration label.</li>
 * <li>The "<b>webXMLTransform</b>" attribute specifies the XSLT tranform file that the web.xml file will pass through before being copied to the WEB-INF
 * directory.  No tranformation will be applied if none is specified.</li>
 * <li>The "<b>webXMLTransformURL</b>" attribute specifies the URL to an XSLT tranform that the web.xml file will pass through before being copied to the WEB-INF
 * directory.  No tranformation will be applied if none is specified.</li>
 * <li>The "<b>preBase</b>" attribute specifies a directory (could be gzipped) that supplies a "base" for the war.  The directory contents will be copied to
 * the building war directory <i>before</i> it is provided with any Enunciate-specific files and directories.</li>
 * <li>The "<b>postBase</b>" attribute specifies a directory (could be gzipped) that supplies a "base" for the war.  The directory contents will be copied to
 * the building war directory <i>after</i> it is provided with any Enunciate-specific files and directories.</li>
 * <li>The "<b>includeDefaultLibs</b>" attribute specifies whether the detault set of libs (pulled from the classpath) should be used.  If "false" only the
 * libs explicitly included by file (see below) will be included.</li>
 * <li>The "<b>excludeDefaultLibs</b>" attribute specifies whether Enunciate should perform its default filtering of known compile-time-only jars.</li>
 * </ul>
 * <p/>
 * <p>By default, the war is constructed by copying jars that are on the classpath to its "lib" directory (the contents of directories on the classpath
 * will be copied to the "classes" directory).  You add a specific file to this list with the "file" attribute "includeLibs" element of the "war" element.
 * From this list, you can specify a set of files to include with the "pattern" attribute of the "includeLibs" element.  This is an ant-style pattern matcher
 * against the absolute path of the file (or directory).  By default all files are included.</p>
 * <p/>
 * <p>There is a set of known jars that by default will not be copied to the "lib" directory.  These include the jars that
 * ship by default with the JDK and the jars that are known to be build-time-only jars for Enunciate.  You can specify additional jars that are to be
 * excluded with an arbitrary number of "excludeLibs" child elements under the "war" element in the configuration file.  The "excludeLibs" element supports either a
 * "pattern" attribute or a "file" attribute.  The "pattern" attribute is an ant-style pattern matcher against the absolute path of the file (or directory)
 * on the classpath that should not be copied to the destination war.  The "file" attribute refers to a specific file on the filesystem.</p>
 * <p/>
 * <h3>The "springImport" element</h3>
 * <p/>
 * <p>The "springImport" element is used to specify a spring configuration file that will be imported by the main
 * spring servlet config. It supports the following attributes:</p>
 * <p/>
 * <ul>
 * <li>The "file" attribute specifies the spring import file on the filesystem.  It will be copied to the WEB-INF directory.</li>
 * <li>The "uri" attribute specifies the URI to the spring import file.  The URI will not be resolved at compile-time, nor will anything be copied to the
 * WEB-INF directory. The value of this attribute will be used to reference the spring import file in the main config file.  This attribute is useful
 * to specify an import file on the classpath, e.g. "classpath:com/myco/spring/config.xml".</li>
 * </ul>
 * <p/>
 * <p>One use of specifying spring a import file is to wrap your endpoints with spring interceptors and/or XFire in/out/fault handlers.  This can be done
 * by simply declaring a bean that is an instance of your endpoint class.  This bean can be advised as needed, and if it implements
 * org.codehaus.xfire.handler.HandlerSupport (perhaps <a href="http://static.springframework.org/spring/docs/1.2.x/reference/aop.html#d0e4128">through the use
 * of a mixin</a>?), the in/out/fault handlers will be used for the XFire invocation of that endpoint.</p>
 * <p/>
 * <p>It's important to note that the type on which the bean context will be searched is the type of the endpoint <i>interface</i>, and then only if it exists.
 * If there are more than one beans that are assignable to the endpoint interface, the bean that is named the name of the service will be used.  Otherwise,
 * the deployment of your endpoint will fail.</p>
 * <p/>
 * <p>The same procedure can be used to specify the beans to use as REST endpoints, although the XFire in/out/fault handlers will be ignored.  In this case,
 * the bean context will be searched for each <i>REST interface</i> that the endpoint implements.  If there is a bean that implements that interface, it will
 * used instead of the default implementation.  If there is more than one, the bean that is named the same as the REST endpoint will be used.</p>
 * <p/>
 * <p>There also exists a mechanism to add certain AOP interceptors to all service endpoint beans.  Such interceptors are referred to as "global service
 * interceptors." This can be done by using the "globalServiceInterceptor" element (see below), or by simply creating an interceptor that implements
 * org.codehaus.enunciate.modules.spring_app.EnunciateServiceAdvice or org.codehaus.enunciate.modules.spring_app.EnunciateServiceAdvisor and declaring it in your
 * imported spring beans file.</p>
 * <p/>
 * <p>Each global interceptor has an order.  The default order is 0 (zero).  If a global service interceptor implements org.springframework.core.Ordered, the
 * order will be respected. As global service interceptors are added, it will be assigned a position in the chain according to it's order.  Interceptors
 * of the same order will be ordered together according to their position in the config file, with priority to those declared by the "globalServiceInterceptor"
 * element, then to instances of org.codehaus.enunciate.modules.spring_app.EnunciateServiceAdvice, then to instances of
 * org.codehaus.enunciate.modules.spring_app.EnunciateServiceAdvisor.</p>
 * <p/>
 * <p>For more information on spring bean configuration and interceptor advice, see
 * <a href="http://static.springframework.org/spring/docs/1.2.x/reference/index.html">the spring reference documentation</a>.</p>
 * <p/>
 * <h3>The "globalServiceInterceptor" element</h3>
 * <p/>
 * <p>The "globalServiceInterceptor" element is used to specify a Spring interceptor (instance of org.aopalliance.aop.Advice or
 * org.springframework.aop.Advisor) that is to be injected on all service endpoint beans.</p>
 * <p/>
 * <ul>
 * <li>The "interceptorClass" attribute specified the class of the interceptor.</p>
 * <li>The "beanName" attribute specifies the bean name of the interceptor.</p>
 * </ul>
 * <p/>
 * <h3>The "handlerInterceptor" element</h3>
 * <p/>
 * <p>The "handlerInterceptor" element is used to specify a Spring interceptor (instance of org.springframework.web.servlet.HandlerInterceptor)
 * that is to be injected on the handler mapping.</p>
 * <p/>
 * <ul>
 * <li>The "interceptorClass" attribute specifies the class of the interceptor.</p>
 * <li>The "beanName" attribute specifies the bean name of the interceptor.</p>
 * </ul>
 * <p/>
 * <p>For more information on spring bean configuration and interceptor advice, see
 * <a href="http://static.springframework.org/spring/docs/1.2.x/reference/index.html">the spring reference documentation</a>.</p>
 * <p/>
 * <h3>The "copyResources" element</h3>
 * <p/>
 * <p>The "copyResources" element is used to specify a pattern of resources to copy to the compile directory.  It supports the following attributes:</p>
 * <p/>
 * <ul>
 * <li>The "<b>dir</b>" attribute specifies the base directory of the resources to copy.</li>
 * <li>The "<b>pattern</b>" attribute specifies an <a href="http://ant.apache.org/">Ant</a>-style
 * pattern used to find the resources to copy.  For more information, see the documentation for the
 * <a href="http://static.springframework.org/spring/docs/1.2.x/api/org/springframework/util/AntPathMatcher.html">ant path matcher</a> in the Spring
 * JavaDocs.</li>
 * </ul>
 * <p/>
 * <h1><a name="artifacts">Artifacts</a></h1>
 * <p/>
 * <p>The spring app deployment module exports the following artifacts:</p>
 * <p/>
 * <ul>
 * <li>The "spring.app.dir" artifact is the (expanded) web app directory, exported during the build step.</li>
 * <li>The "spring.war.file" artifact is the packaged war, exported during the package step.</li>
 * </ul>
 *
 * @author Ryan Heaton
 */
public class SpringAppDeploymentModule extends FreemarkerDeploymentModule {

  private WarConfig warConfig;
  private final List<SpringImport> springImports = new ArrayList<SpringImport>();
  private final List<CopyResources> copyResources = new ArrayList<CopyResources>();
  private final List<GlobalServiceInterceptor> globalServiceInterceptors = new ArrayList<GlobalServiceInterceptor>();
  private final List<HandlerInterceptor> handlerInterceptors = new ArrayList<HandlerInterceptor>();
  private boolean compileDebugInfo = true;
  private String defaultAutowire = null;
  private String defaultDependencyCheck = null;

  /**
   * @return "xfire"
   */
  @Override
  public String getName() {
    return "spring-app";
  }

  /**
   * @return The URL to "xfire-servlet.fmt"
   */
  protected URL getSpringServletTemplateURL() {
    return SpringAppDeploymentModule.class.getResource("spring-servlet.fmt");
  }

  /**
   * @return The URL to "web.xml.fmt"
   */
  protected URL getWebXmlTemplateURL() {
    return SpringAppDeploymentModule.class.getResource("web.xml.fmt");
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();

    //generate the xfire-servlet.xml
    model.setFileOutputDirectory(getConfigGenerateDir());
    model.put("springImports", getSpringImportURIs());
    model.put("defaultDependencyCheck", getDefaultDependencyCheck());
    model.put("defaultAutowire", getDefaultAutowire());
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

    model.put("xfireEnabled", getEnunciate().isModuleEnabled("xfire"));
    model.put("restEnabled", getEnunciate().isModuleEnabled("rest"));
    model.put("gwtEnabled", getEnunciate().isModuleEnabled("gwt"));

    processTemplate(getSpringServletTemplateURL(), model);
    processTemplate(getWebXmlTemplateURL(), model);
  }

  @Override
  protected void doCompile() throws EnunciateException, IOException {
    ArrayList<String> javacAdditionalArgs = new ArrayList<String>();
    if (compileDebugInfo) {
      javacAdditionalArgs.add("-g");
    }

    Enunciate enunciate = getEnunciate();
    File compileDir = getCompileDir();
    enunciate.invokeJavac(enunciate.getEnunciateClasspath(), compileDir, javacAdditionalArgs, enunciate.getSourceFiles());

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
        enunciate.invokeJavac(jaxwsClasspath.toString(), compileDir, javacAdditionalArgs, jaxwsSourceFiles.toArray(new String[jaxwsSourceFiles.size()]));
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
      enunciate.invokeJavac(gwtClasspath.toString(), compileDir, javacAdditionalArgs, gwtSourceFiles.toArray(new String[gwtSourceFiles.size()]));
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

  @Override
  protected void doBuild() throws IOException, EnunciateException {
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

    info("Building the expanded WAR in %s", buildDir);
    File webinf = new File(buildDir, "WEB-INF");
    File webinfClasses = new File(webinf, "classes");
    File webinfLib = new File(webinf, "lib");

    //copy the compiled classes to WEB-INF/classes.
    enunciate.copyDir(getCompileDir(), webinfClasses);

    //prime the list of libs to include in the war with what's on the enunciate classpath.
    List<String> warLibs = new ArrayList<String>();
    if (this.warConfig == null || this.warConfig.isIncludeDefaultLibs()) {
      warLibs.addAll(Arrays.asList(enunciate.getEnunciateClasspath().split(File.pathSeparator)));
    }
    List<IncludeExcludeLibs> includeLibs = this.warConfig != null ? new ArrayList<IncludeExcludeLibs>(this.warConfig.getIncludeLibs()) : new ArrayList<IncludeExcludeLibs>();
    List<IncludeExcludeLibs> excludeLibs = this.warConfig != null ? new ArrayList<IncludeExcludeLibs>(this.warConfig.getExcludeLibs()) : new ArrayList<IncludeExcludeLibs>();

    //now add the lib files that are explicitly included.
    Iterator<IncludeExcludeLibs> includeIt = includeLibs.iterator();
    while (includeIt.hasNext()) {
      IncludeExcludeLibs includeJar = includeIt.next();
      if (includeJar.getFile() != null) {
        warLibs.add(includeJar.getFile().getAbsolutePath());
        includeIt.remove();
      }
    }

    AntPathMatcher pathMatcher = new AntPathMatcher();
    List<File> includedLibs = new ArrayList<File>();
    // Now get the files that are to be explicitly included.
    // If none are explicitly included, include all of them.
    INCLUDE_LOOP: for (String warLib : warLibs) {
      File libFile = new File(warLib);
      if (libFile.exists()) {
        if (includeLibs.isEmpty()) {
          includedLibs.add(libFile);
        }
        else {
          for (IncludeExcludeLibs includeJar : includeLibs) {
            String pattern = includeJar.getPattern();
            String absolutePath = libFile.getAbsolutePath();
            if (absolutePath.startsWith(File.separator)) {
              //lob off the beginning "/" for Linux boxes.
              absolutePath = absolutePath.substring(1);
            }
            if ((pattern != null) && (pathMatcher.isPattern(pattern) && (pathMatcher.match(pattern, absolutePath)))) {
              includedLibs.add(libFile);
              break INCLUDE_LOOP;
            }
          }
        }
      }
    }

    //if there are any excludes, filter them out here.
    boolean excludeDefaults = this.warConfig == null || this.warConfig.isExludeDefaultLibs();
    Iterator<File> includeLibIt = includedLibs.iterator();
    while (includeLibIt.hasNext()) {
      File includedLib = includeLibIt.next();
      if ((!excludeDefaults) || (!knownExclude(includedLib)) || (excludeLibs.size() > 0)) {
        for (IncludeExcludeLibs excludeJar : excludeLibs) {
          String pattern = excludeJar.getPattern();
          String absolutePath = includedLib.getAbsolutePath();
          if (absolutePath.startsWith(File.separator)) {
            //lob off the beginning "/" for Linux boxes.
            absolutePath = absolutePath.substring(1);
          }
          if ((pattern != null) && (pathMatcher.isPattern(pattern)) && (pathMatcher.match(pattern, absolutePath))) {
            includeLibIt.remove();
          }
          else if ((excludeJar.getFile() != null) && (excludeJar.getFile().equals(includedLib))) {
            includeLibIt.remove();
          }
        }
      }
    }

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

    //todo: assert that the necessary jars (spring, xfire, commons-whatever, etc.) are there?

    //put the web.xml in WEB-INF.  Pass it through a stylesheet, if specified.
    File xfireConfigDir = getConfigGenerateDir();
    File webXML = new File(xfireConfigDir, "web.xml");
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

    //copy the spring servlet config from the build dir to the WEB-INF directory.
    enunciate.copyFile(new File(xfireConfigDir, "spring-servlet.xml"), new File(webinf, "spring-servlet.xml"));
    for (SpringImport springImport : springImports) {
      //copy the extra spring import files to the WEB-INF directory to be imported.
      if (springImport.getFile() != null) {
        File importFile = enunciate.resolvePath(springImport.getFile());
        enunciate.copyFile(importFile, new File(webinf, importFile.getName()));
      }
    }

    //now try to find the documentation and export it to the build directory...
    Artifact artifact = enunciate.findArtifact("docs");
    if (artifact != null) {
      artifact.exportTo(buildDir, enunciate);
    }
    else {
      warn("WARNING: No documentation artifact found!");
    }

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

    //export the expanded application directory.
    enunciate.addArtifact(new FileArtifact(getName(), "spring.app.dir", buildDir));
  }

  @Override
  protected void doPackage() throws EnunciateException, IOException {
    File buildDir = getBuildDir();
    File warFile = getWarFile();

    if (!warFile.getParentFile().exists()) {
      warFile.getParentFile().mkdirs();
    }

    Enunciate enunciate = getEnunciate();
    info("Creating %s", warFile.getAbsolutePath());

    enunciate.zip(warFile, buildDir);
    enunciate.addArtifact(new FileArtifact(getName(), "spring.war.file", warFile));
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
   * Whether to exclude a file from copying to the WEB-INF/lib directory.
   *
   * @param file The file to exclude.
   * @return Whether to exclude a file from copying to the lib directory.
   */
  protected boolean knownExclude(File file) throws IOException {
    //instantiate a loader with this library only in its path...
    URLClassLoader loader = new URLClassLoader(new URL[]{file.toURL()}, null);
    if (loader.findResource("META-INF/enunciate/preserve-in-war") != null) {
      debug("%s will be included in the war because it contains the entry META-INF/enunciate/preserve-in-war.", file);
      //if a jar happens to have the enunciate "preserve-in-war" file, it is NOT excluded.
      return false;
    }
    else if (loader.findResource(com.sun.tools.apt.Main.class.getName().replace('.', '/').concat(".class")) != null) {
      debug("%s will be excluded from the war because it appears to be tools.jar.", file);
      //exclude tools.jar.
      return true;
    }
    else if (loader.findResource(net.sf.jelly.apt.Context.class.getName().replace('.', '/').concat(".class")) != null) {
      debug("%s will be excluded from the war because it appears to be apt-jelly.", file);
      //exclude apt-jelly-core.jar
      return true;
    }
    else if (loader.findResource(net.sf.jelly.apt.freemarker.FreemarkerModel.class.getName().replace('.', '/').concat(".class")) != null) {
      debug("%s will be excluded from the war because it appears to be the apt-jelly-freemarker libs.", file);
      //exclude apt-jelly-freemarker.jar
      return true;
    }
    else if (loader.findResource(freemarker.template.Configuration.class.getName().replace('.', '/').concat(".class")) != null) {
      debug("%s will be excluded from the war because it appears to be the freemarker libs.", file);
      //exclude freemarker.jar
      return true;
    }
    else if (loader.findResource(Enunciate.class.getName().replace('.', '/').concat(".class")) != null) {
      debug("%s will be excluded from the war because it appears to be the enunciate core jar.", file);
      //exclude enunciate-core.jar
      return true;
    }
    else if (loader.findResource("javax/servlet/ServletContext.class") != null) {
      debug("%s will be excluded from the war because it appears to be the servlet api.", file);
      //exclude the servlet api.
      return true;
    }
    else if (loader.findResource("org/codehaus/enunciate/modules/xfire_client/EnunciatedClientSoapSerializerHandler.class") != null) {
      debug("%s will be excluded from the war because it appears to be the enunciated xfire client tools jar.", file);
      //exclude xfire-client-tools
      return true;
    }
    else if (loader.findResource("javax/swing/SwingBeanInfoBase.class") != null) {
      debug("%s will be excluded from the war because it appears to be dt.jar.", file);
      //exclude dt.jar
      return true;
    }
    else if (loader.findResource("HTMLConverter.class") != null) {
      debug("%s will be excluded from the war because it appears to be htmlconverter.jar.", file);
      return true;
    }
    else if (loader.findResource("sun/tools/jconsole/JConsole.class") != null) {
      debug("%s will be excluded from the war because it appears to be jconsole.jar.", file);
      return true;
    }
    else if (loader.findResource("sun/jvm/hotspot/debugger/Debugger.class") != null) {
      debug("%s will be excluded from the war because it appears to be sa-jdi.jar.", file);
      return true;
    }
    else if (loader.findResource("sun/io/ByteToCharDoubleByte.class") != null) {
      debug("%s will be excluded from the war because it appears to be charsets.jar.", file);
      return true;
    }
    else if (loader.findResource("com/sun/deploy/ClientContainer.class") != null) {
      debug("%s will be excluded from the war because it appears to be deploy.jar.", file);
      return true;
    }
    else if (loader.findResource("com/sun/javaws/Globals.class") != null) {
      debug("%s will be excluded from the war because it appears to be javaws.jar.", file);
      return true;
    }
    else if (loader.findResource("javax/crypto/SecretKey.class") != null) {
      debug("%s will be excluded from the war because it appears to be jce.jar.", file);
      return true;
    }
    else if (loader.findResource("sun/net/www/protocol/https/HttpsClient.class") != null) {
      debug("%s will be excluded from the war because it appears to be jsse.jar.", file);
      return true;
    }
    else if (loader.findResource("sun/plugin/JavaRunTime.class") != null) {
      debug("%s will be excluded from the war because it appears to be plugin.jar.", file);
      return true;
    }
    else if (loader.findResource("com/sun/corba/se/impl/activation/ServerMain.class") != null) {
      debug("%s will be excluded from the war because it appears to be rt.jar.", file);
      return true;
    }
    else if (Service.providers(DeploymentModule.class, loader).hasNext()) {
      debug("%s will be excluded from the war because it appears to be an enunciate module.", file);
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

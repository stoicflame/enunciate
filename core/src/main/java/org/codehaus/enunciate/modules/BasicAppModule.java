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

package org.codehaus.enunciate.modules;

import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import freemarker.ext.dom.NodeModel;
import freemarker.template.TemplateException;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.util.AntPatternMatcher;
import org.codehaus.enunciate.util.PatternFileFilter;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.war.CopyResources;
import org.codehaus.enunciate.config.war.WebAppConfig;
import org.codehaus.enunciate.config.war.WebAppResource;
import org.codehaus.enunciate.config.war.IncludeExcludeLibs;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.FileArtifact;
import org.codehaus.enunciate.main.webapp.BaseWebAppFragment;
import org.codehaus.enunciate.main.webapp.WebAppComponent;
import org.codehaus.enunciate.main.webapp.WebAppFragment;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import sun.misc.Service;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
 * <h1>Basic App Module</h1>
 *
 * <p>The basic app deployment module produces the web app for hosting the API endpoints and documentation.</p>
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
 * <p>The "generate" step generates the deployment descriptors and config files. Refer to <a href="http://enunciate.codehaus.org/user_guide.html#config_webapp">user guide</a> to learn
 * how to customize the deployment descriptors and config files.</p>
 *
 * <h3>compile</h3>
 *
 * <p>The "compile" step compiles all API source files, including the source files that were generated from other modules
 * (e.g. JAX-WS Support module, GWT module, AMF module, etc.).</p>
 *
 * <h3>build</h3>
 *
 * <p>The "build" step assembles all the generated artifacts, compiled classes, and deployment descriptors into a
 * directory.</p>
 *
 * <p>Refer to <a href="http://enunciate.codehaus.org/user_guide.html#config_webapp">user guide</a> to learn
 * how to customize the deployment descriptors and config files.</p>
 *
 * <h3>package</h3>
 *
 * <p>The "package" step packages the expanded war and exports it.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The configuration for the basic app deployment module is defined entirely in the core Enunciate configuration. Refer to <a href="http://enunciate.codehaus.org/user_guide.html#config_webapp">user guide</a> to learn
 * how to customize the deployment descriptors and config files.</p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The spring app deployment module exports the following artifacts:</p>
 *
 * <ul>
 * <li>The "app.dir" artifact is the (expanded) web app directory, exported during the build step.</li>
 * <li>The "war.file" artifact is the packaged war, exported during the package step.</li>
 * </ul>
 *
 * @author Ryan Heaton
 * @docFileName module_basic_app.html
 */
public class BasicAppModule extends FreemarkerDeploymentModule {

  /**
   * @return "basic-app"
   */
  @Override
  public String getName() {
    return "basic-app";
  }

  /**
   * @return The URL to "web.xml.fmt"
   */
  protected URL getWebXmlTemplateURL() {
    return BasicAppModule.class.getResource("web.xml.fmt");
  }

  /**
   * @return The URL to "web.xml.fmt"
   */
  protected URL getMergeWebXmlTemplateURL() {
    return BasicAppModule.class.getResource("merge-web-xml.fmt");
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    if (!isDisabled()) {
      if (getWebAppConfig() != null && !getWebAppConfig().getGlobalServletFilters().isEmpty()) {
        for (WebAppComponent globalServletFilter : getWebAppConfig().getGlobalServletFilters()) {
          if (globalServletFilter.getName() == null) {
            throw new EnunciateException("A global servlet filter (as specified in the enunciate config) requires a name.");
          }
          if (globalServletFilter.getClassname() == null) {
            throw new EnunciateException("A global servlet filter (as specified in the enunciate config) requires a classname.");
          }
        }
      }
    }
  }

  public void doFreemarkerGenerate() throws EnunciateException, IOException, TemplateException {
    //no-op; we do our generation at the build phase...
  }

  @Override
  protected void doCompile() throws EnunciateException, IOException {
    Enunciate enunciate = getEnunciate();
    if (getWebAppConfig() != null && !getWebAppConfig().isDoCompile()) {
      debug("Compilation has been disabled.  No server-side classes will be compiled, nor will any resources be copied.");
      return;
    }

    final File compileDir = getCompileDir();
    if (!enunciate.isUpToDateWithSources(compileDir)) {
      enunciate.compileSources(compileDir);

      if (getWebAppConfig() != null && !getWebAppConfig().getCopyResources().isEmpty()) {
        AntPatternMatcher matcher = new AntPatternMatcher();
        for (CopyResources copyResource : getWebAppConfig().getCopyResources()) {
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

      debug("Building the expanded WAR in %s", buildDir);

      if (getWebAppConfig() != null && !getWebAppConfig().getGlobalServletFilters().isEmpty()) {
        Set<String> allServletUrls = new TreeSet<String>();
        for (WebAppFragment fragment : enunciate.getWebAppFragments()) {
          if (fragment.getServlets() != null) {
            for (WebAppComponent servletComponent : fragment.getServlets()) {
              if (servletComponent.getUrlMappings() != null) {
                allServletUrls.addAll(servletComponent.getUrlMappings());
              }
            }
          }
        }
        for (WebAppComponent filter : getWebAppConfig().getGlobalServletFilters()) {
          Set<String> urlMappings = filter.getUrlMappings();
          if (urlMappings == null) {
            urlMappings = new TreeSet<String>();
            filter.setUrlMappings(urlMappings);
          }
          urlMappings.addAll(allServletUrls);
        }
        BaseWebAppFragment fragment = new BaseWebAppFragment("global-servlet-filters");
        fragment.setFilters(getWebAppConfig().getGlobalServletFilters());
        enunciate.addWebAppFragment(fragment);
      }

      for (WebAppFragment fragment : enunciate.getWebAppFragments()) {
        if (fragment.getBaseDir() != null) {
          enunciate.copyDir(fragment.getBaseDir(), buildDir);
        }
      }

      if (getWebAppConfig() == null || getWebAppConfig().isDoCompile()) {
        //copy the compiled classes to WEB-INF/classes.
        File webinf = new File(buildDir, "WEB-INF");
        File webinfClasses = new File(webinf, "classes");
        enunciate.copyDir(getCompileDir(), webinfClasses);
      }

      if (getWebAppConfig() == null || getWebAppConfig().isDoLibCopy()) {
        doLibCopy();
      }
      else {
        debug("Lib copy has been disabled.  No libs will be copied, nor any manifest written.");
      }

      generateWebXml();

      copyPostBase();
    }
    else {
      info("Skipping the build of the expanded war as everything appears up-to-date...");
    }

    //export the expanded application directory.
    enunciate.addArtifact(new FileArtifact(getName(), "app.dir", buildDir));
  }

  /**
   * Copy the post base.
   */
  protected void copyPostBase() throws IOException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    //extract a post base if specified.
    WebAppConfig webAppConfig = getWebAppConfig();
    if ((webAppConfig != null) && (webAppConfig.getPostBase() != null)) {
      File postBase = enunciate.resolvePath(webAppConfig.getPostBase());
      if (postBase.isDirectory()) {
        debug("Copying postBase directory %s to %s...", postBase, buildDir);
        enunciate.copyDir(postBase, buildDir);
      }
      else {
        debug("Extracting postBase zip file %s to %s...", postBase, buildDir);
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
    WebAppConfig webAppConfig = getWebAppConfig();
    if ((webAppConfig != null) && (webAppConfig.getPreBase() != null)) {
      File preBase = enunciate.resolvePath(webAppConfig.getPreBase());
      if (preBase.isDirectory()) {
        debug("Copying preBase directory %s to %s...", preBase, buildDir);
        enunciate.copyDir(preBase, buildDir);
      }
      else {
        debug("Extracting preBase zip file %s to %s...", preBase, buildDir);
        enunciate.extractBase(new FileInputStream(preBase), buildDir);
      }
    }
  }

  /**
   * generates web.xml to WEB-INF. Pass it through a stylesheet, if specified.
   */
  protected void generateWebXml() throws IOException, EnunciateException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    File webinf = new File(buildDir, "WEB-INF");
    webinf.mkdirs();
    File destWebXML = new File(webinf, "web.xml");

    File configDir = getGenerateDir();
    File webXML = new File(configDir, "web.xml");
    EnunciateFreemarkerModel model = getModel();
    model.setFileOutputDirectory(configDir);
    try {
      //delayed to the "build" phase to enable modules to supply their web app fragments.
      model.put("displayName", model.getEnunciateConfig().getLabel());
      model.put("webAppFragments", enunciate.getWebAppFragments());
      List<WebAppResource> envEntries = Collections.<WebAppResource>emptyList();
      List<WebAppResource> resourceEnvRefs = Collections.<WebAppResource>emptyList();
      List<WebAppResource> resourceRefs = Collections.<WebAppResource>emptyList();
      WebAppConfig webAppConfig = getWebAppConfig();
      if (webAppConfig != null) {
        envEntries = webAppConfig.getEnvEntries();
        resourceEnvRefs = webAppConfig.getResourceEnvRefs();
        resourceRefs = webAppConfig.getResourceRefs();
      }
      model.put("envEntries", envEntries);
      model.put("resourceEnvRefs", resourceEnvRefs);
      model.put("resourceRefs", resourceRefs);
      if (webAppConfig != null) {
        model.put("webappAttributes", webAppConfig.getWebXmlAttributes());
      }
      processTemplate(getWebXmlTemplateURL(), model);
    }
    catch (TemplateException e) {
      throw new EnunciateException("Error processing web.xml template file.", e);
    }

    File mergedWebXml = webXML;
    WebAppConfig webAppConfig = getWebAppConfig();
    if ((webAppConfig != null) && (webAppConfig.getMergeWebXMLURL() != null || webAppConfig.getMergeWebXML() != null)) {
      URL webXmlToMerge = webAppConfig.getMergeWebXMLURL();
      if (webXmlToMerge == null) {
        webXmlToMerge = enunciate.resolvePath(webAppConfig.getMergeWebXML()).toURL();
      }

      try {
        Document source1Doc = loadMergeXml(webXmlToMerge.openStream());
        NodeModel.simplify(source1Doc);
        Document source2Doc = loadMergeXml(new FileInputStream(webXML));
        NodeModel.simplify(source2Doc);

        Map<String, String> mergedAttributes = new HashMap<String, String>();
        NamedNodeMap source2Attributes = source2Doc.getDocumentElement().getAttributes();
        for (int i = 0; i < source2Attributes.getLength(); i++) {
          mergedAttributes.put(source2Attributes.item(i).getNodeName(), source2Attributes.item(i).getNodeValue());
        }
        NamedNodeMap source1Attributes = source1Doc.getDocumentElement().getAttributes();
        for (int i = 0; i < source1Attributes.getLength(); i++) {
          mergedAttributes.put(source1Attributes.item(i).getNodeName(), source1Attributes.item(i).getNodeValue());
        }

        model.put("source1", NodeModel.wrap(source1Doc.getDocumentElement()));
        model.put("source2", NodeModel.wrap(source2Doc.getDocumentElement()));
        model.put("mergedAttributes", mergedAttributes);
        processTemplate(getMergeWebXmlTemplateURL(), model);
      }
      catch (TemplateException e) {
        throw new EnunciateException("Error while merging web xml files.", e);
      }

      File mergeTarget = new File(getGenerateDir(), "merged-web.xml");
      if (!mergeTarget.exists()) {
        throw new EnunciateException("Error: " + mergeTarget + " doesn't exist.");
      }

      debug("Merged %s and %s into %s...", webXmlToMerge, webXML, mergeTarget);
      mergedWebXml = mergeTarget;
    }

    if ((webAppConfig != null) && (webAppConfig.getWebXMLTransformURL() != null || webAppConfig.getWebXMLTransform() != null)) {
      URL transformURL = webAppConfig.getWebXMLTransformURL();
      if (transformURL == null) {
        transformURL = enunciate.resolvePath(webAppConfig.getWebXMLTransform()).toURL();
      }

      debug("web.xml transform has been specified as %s.", transformURL);
      try {
        StreamSource source = new StreamSource(transformURL.openStream());
        Transformer transformer = new TransformerFactoryImpl().newTransformer(source);
        debug("Transforming %s to %s.", mergedWebXml, destWebXML);
        transformer.transform(new StreamSource(new FileReader(mergedWebXml)), new StreamResult(destWebXML));
      }
      catch (TransformerException e) {
        throw new EnunciateException("Error during transformation of the web.xml (stylesheet " + transformURL + ", file " + mergedWebXml + ")", e);
      }
    }
    else {
      enunciate.copyFile(mergedWebXml, destWebXML);
    }
  }

  /**
   * Loads the node model for merging xml.
   *
   * @param inputStream The input stream of the xml.
   * @return The node model.
   */
  protected Document loadMergeXml(InputStream inputStream) throws EnunciateException {
    Document doc;
    try {
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(false); //no namespace for the merging...
      builderFactory.setValidating(false);
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      builder.setEntityResolver(new EntityResolver() {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
          //we don't want to validate or parse external dtds...
          return new InputSource(new StringReader(""));
        }
      });
      doc = builder.parse(inputStream);
    }
    catch (Exception e) {
      throw new EnunciateException("Error parsing web.xml file for merging", e);
    }
    return doc;
  }

  /**
   * Copies the classpath elements to WEB-INF.
   *
   * @throws java.io.IOException
   */
  protected void doLibCopy() throws IOException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    File webinf = new File(buildDir, "WEB-INF");
    File webinfClasses = new File(webinf, "classes");
    File webinfLib = new File(webinf, "lib");

    //initialize the include filters.
    AntPatternMatcher pathMatcher = new AntPatternMatcher();
    pathMatcher.setPathSeparator(File.separator);
    List<File> explicitIncludes = new ArrayList<File>();
    List<String> includePatterns = new ArrayList<String>();
    WebAppConfig webAppConfig = getWebAppConfig();
    if (webAppConfig != null) {
      for (IncludeExcludeLibs el : webAppConfig.getIncludeLibs()) {
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
            warn("Pattern '%s' is not a valid pattern, so it will not be applied.", pattern);
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
    if (webAppConfig == null || webAppConfig.isIncludeClasspathLibs()) {
      debug("Using the Enunciate classpath as the initial list of libraries to be passed through the include/exclude filter.");
      //prime the list of libs to include in the war with what's on the enunciate classpath.
      warLibs.addAll(Arrays.asList(enunciate.getEnunciateRuntimeClasspath().split(File.pathSeparator)));
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
    boolean excludeDefaults = webAppConfig == null || webAppConfig.isExcludeDefaultLibs();
    List<String> manifestClasspath = new ArrayList<String>();
    Iterator<File> toBeIncludedIt = includedLibs.iterator();
    while (toBeIncludedIt.hasNext()) {
      File toBeIncluded = toBeIncludedIt.next();
      if (excludeDefaults && knownExclude(toBeIncluded)) {
        toBeIncludedIt.remove();
      }
      else if (webAppConfig != null) {
        for (IncludeExcludeLibs excludeLibs : webAppConfig.getExcludeLibs()) {
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
        debug("Adding the contents of %s to WEB-INF/classes.", includedLib);
        enunciate.copyDir(includedLib, webinfClasses);
      }
      else {
        debug("Including %s in WEB-INF/lib.", includedLib);
        enunciate.copyFile(includedLib, includedLib.getParentFile(), webinfLib);
      }
    }

    // write the manifest file.
    Manifest manifest = webAppConfig == null ? WebAppConfig.getDefaultManifest() : webAppConfig.getManifest();
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
    if (getWebAppConfig() == null || getWebAppConfig().isDoPackage()) {
      File buildDir = getBuildDir();
      File warFile = getWarFile();
      Enunciate enunciate = getEnunciate();

      if (!enunciate.isUpToDate(buildDir, warFile)) {
        if (!warFile.getParentFile().exists()) {
          warFile.getParentFile().mkdirs();
        }

        debug("Creating %s", warFile.getAbsolutePath());

        enunciate.zip(warFile, buildDir);
      }
      else {
        info("Skipping war file creation as everything appears up-to-date...");
      }

      enunciate.addArtifact(new FileArtifact(getName(), "war.file", warFile));
    }
    else {
      debug("Packaging has been disabled.  No packaging will be performed.");
    }
  }

  /**
   * The war file to create.
   *
   * @return The war file to create.
   */
  public File getWarFile() {
    WebAppConfig config = getWebAppConfig();
    if (config != null && config.getWar() != null) {
      return getEnunciate().resolvePath(config.getWar());
    }
    else {
      String filename = "enunciate.war";
      if (getEnunciate().getConfig().getLabel() != null) {
        filename = getEnunciate().getConfig().getLabel() + ".war";
      }

      return new File(getPackageDir(), filename);
    }
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
   * @return 300
   */
  @Override
  public int getOrder() {
    return 300;
  }

  @Override
  public RuleSet getConfigurationRules() {
    //no configuration rules for this module; all configuration is directly set in the main Enunciate configuration.
    return null;
  }

  @Override
  public Validator getValidator() {
    return null;
  }

  public WebAppConfig getWebAppConfig() {
    return (getEnunciate() != null && getEnunciate().getConfig() != null) ? getEnunciate().getConfig().getWebAppConfig() : null;
  }

  @Override
  public boolean isDisabled() {
    return getWebAppConfig() != null && getWebAppConfig().isDisabled();
  }

  @Override
  public File getBuildDir() {
    File buildDir = null;
    WebAppConfig webAppConfig = getWebAppConfig();
    if (webAppConfig != null && webAppConfig.getDir() != null) {
      buildDir = getEnunciate().resolvePath(webAppConfig.getDir());
      buildDir.mkdirs();
    }
    return buildDir == null ? super.getBuildDir() : buildDir;
  }
}
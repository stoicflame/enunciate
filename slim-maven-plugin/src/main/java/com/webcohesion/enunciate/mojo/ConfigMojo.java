package com.webcohesion.enunciate.mojo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.EnunciateConfiguration;
import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.module.EnunciateModule;
import com.webcohesion.enunciate.module.ProjectExtensionModule;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.License;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Goal which initializes an Enunciate build process.
 */
@SuppressWarnings ( "unchecked" )
@Mojo ( name = "config", defaultPhase = LifecyclePhase.VALIDATE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class ConfigMojo extends AbstractMojo {

  public static final String ENUNCIATE_PROPERTY = "com.webcohesion.enunciate.mojo.ConfigMojo#ENUNCIATE_PROPERTY";

  @Component
  protected MavenProjectHelper projectHelper;

  @Component
  protected MavenFileFilter configFilter;

  @Component
  protected ArtifactFactory artifactFactory;

  @Component
  protected ArtifactResolver artifactResolver;

  @Parameter ( defaultValue = "${project}", required = true, readonly = true )
  protected MavenProject project;

  @Parameter ( defaultValue = "${plugin.artifacts}", required = true, readonly = true )
  protected Collection<org.apache.maven.artifact.Artifact> pluginDependencies;

  @Parameter ( defaultValue = "${session}", required = true, readonly = true )
  protected MavenSession session;

  @Parameter ( defaultValue = "${localRepository}", required = true, readonly = true )
  protected ArtifactRepository localRepository;

  @Parameter ( defaultValue = "${project.build.directory}", required = true )
  protected File exportsDir = null;

  /**
   * The enunciate artifacts.
   */
  @Parameter
  protected Artifact[] artifacts;

  /**
   * The enunciate configuration file.
   */
  @Parameter
  protected File configFile = null;

  /**
   * The output directory for Enunciate.
   */
  @Parameter ( defaultValue = "${project.build.directory}/enunciate", property = "enunciate.build.directory" )
  protected File buildDir = null;

  /**
   * The Enunciate exports.
   */
  @Parameter
  protected Map<String, String> exports = new HashMap<String, String>();

  /**
   * The include patterns.
   */
  @Parameter
  protected String[] includes;

  /**
   * The exclude patterns.
   */
  @Parameter
  protected String[] excludes;

  /**
   * The modules to include as project extensions.
   */
  @Parameter ( name = "project-extensions" )
  protected String[] projectExtensions;

  /**
   * List of compiler arguments.
   */
  @Parameter
  protected String[] compilerArgs;

  /**
   * Compiler -source version parameter
   */
  @Parameter ( property = "maven.compiler.source" )
  private String source = null;

  /**
   * Compiler -target version parameter
   */
  @Parameter ( property = "maven.compiler.target" )
  private String target = null;

  /**
   * The -encoding argument for the Java compiler
   */
  @Parameter ( property = "encoding", defaultValue = "${project.build.sourceEncoding}" )
  private String encoding = null;

  /**
   * A flag used to disable enunciate. This is primarily intended for usage from the command line to occasionally adjust the build.
   */
  @Parameter ( defaultValue = "false", property = "enunciate.skip" )
  protected boolean skipEnunciate;

  /**
   * The list of dependencies on which Enunciate should attempt to lookup their sources for inclusion in the source path.
   * By default, dependencies with the same groupId as the current project will be included.
   */
  @Parameter ( name = "sourcepath-includes" )
  protected DependencySourceSpec[] sourcepathIncludes;

  /**
   * The list of dependencies on which Enunciate should NOT attempt to lookup their sources for inclusion in the source path.
   * By default, dependencies that do _not_ have the same groupId as the current project will be excluded.
   */
  @Parameter ( name = "sourcepath-excludes" )
  protected DependencySourceSpec[] sourcepathExcludes;

  public void execute() throws MojoExecutionException {
    if (skipEnunciate) {
      getLog().info("[ENUNCIATE] Skipping enunciate per configuration.");
      return;
    }

    Enunciate enunciate = new Enunciate();

    //set up the logger.
    enunciate.setLogger(new MavenEnunciateLogger());

    //set the build dir.
    enunciate.setBuildDir(this.buildDir);

    //load the config.
    EnunciateConfiguration config = enunciate.getConfiguration();
    File configFile = this.configFile;
    if (configFile == null) {
      configFile = new File(project.getBasedir(), "enunciate.xml");
    }

    if (configFile.exists()) {
      getLog().info("[ENUNCIATE] Using enunciate configuration at " + configFile.getAbsolutePath());

      try {
        loadConfig(enunciate, configFile);
        config.setBase(configFile.getParentFile());
      }
      catch (Exception e) {
        throw new MojoExecutionException("Problem with enunciate config file " + configFile, e);
      }
    }

    //set the default configured label.
    config.setDefaultSlug(project.getArtifactId());

    if (project.getName() != null && !"".equals(project.getName().trim())) {
      StringBuilder description = new StringBuilder("<h1>").append(project.getName()).append("</h1>");
      config.setDefaultTitle(project.getName());
      if (project.getDescription() != null && !"".equals(project.getDescription().trim())) {
        description.append("<p>").append(project.getDescription()).append("</p>");
      }
      config.setDefaultDescription(description.toString());
    }

    if (project.getVersion() != null && !"".equals(project.getVersion().trim())) {
      config.setDefaultVersion(project.getVersion());
    }

    List contributors = project.getContributors();
    if (contributors != null && !contributors.isEmpty()) {
      List<EnunciateConfiguration.Contact> contacts = new ArrayList<EnunciateConfiguration.Contact>(contributors.size());
      for (Object c : contributors) {
        Contributor contributor = (Contributor) c;
        contacts.add(new EnunciateConfiguration.Contact(contributor.getName(), contributor.getUrl(), contributor.getEmail()));
      }
      config.setDefaultContacts(contacts);
    }

    List licenses = project.getLicenses();
    if (licenses != null && !licenses.isEmpty()) {
      License license = (License) licenses.get(0);
      config.setDefaultApiLicense(new EnunciateConfiguration.License(license.getName(), license.getUrl(), null, null));
    }

    //set the class paths.
    setClasspathAndSourcepath(enunciate);

    //load any modules on the classpath.
    List<URL> pluginClasspath = buildPluginClasspath();
    ServiceLoader<EnunciateModule> moduleLoader = ServiceLoader.load(EnunciateModule.class, new URLClassLoader(pluginClasspath.toArray(new URL[pluginClasspath.size()]), Thread.currentThread().getContextClassLoader()));
    for (EnunciateModule module : moduleLoader) {
      enunciate.addModule(module);
    }

    //set the compiler arguments.
    List<String> compilerArgs = new ArrayList<String>();
    String sourceVersion = findSourceVersion();
    if (sourceVersion != null) {
      compilerArgs.add("-source");
      compilerArgs.add(sourceVersion);
    }

    String targetVersion = findTargetVersion();
    if (targetVersion != null) {
      compilerArgs.add("-target");
      compilerArgs.add(targetVersion);
    }

    String sourceEncoding = this.encoding;
    if (sourceEncoding != null) {
      compilerArgs.add("-encoding");
      compilerArgs.add(sourceEncoding);
    }
    enunciate.getCompilerArgs().addAll(compilerArgs);

    //includes.
    if (this.includes != null) {
      enunciate.getIncludePatterns().addAll(Arrays.asList(this.includes));
    }

    //excludes.
    if (this.excludes != null) {
      enunciate.getExcludePatterns().addAll(Arrays.asList(this.excludes));
    }

    //exports.
    if (this.exports != null) {
      for (String exportId : this.exports.keySet()) {
        String filename = this.exports.get(exportId);
        if (filename == null || "".equals(filename)) {
          throw new MojoExecutionException("Invalid (empty or null) filename for export " + exportId + ".");
        }
        File exportFile = new File(filename);
        if (!exportFile.isAbsolute()) {
          exportFile = new File(this.exportsDir, filename);
        }

        enunciate.addExport(exportId, exportFile);
      }
    }

    Set<String> enunciateAddedSourceDirs = new TreeSet<String>();
    List<EnunciateModule> modules = enunciate.getModules();
    if (modules != null) {
      Set<String> projectExtensions = new TreeSet<String>(this.projectExtensions == null ? Collections.<String>emptyList() : Arrays.asList(this.projectExtensions));
      for (EnunciateModule module : modules) {

        //configure the project with the module project extensions.
        if (projectExtensions.contains(module.getName()) && module instanceof ProjectExtensionModule) {
          ProjectExtensionModule extensions = (ProjectExtensionModule) module;
          for (File projectSource : extensions.getProjectSources()) {
            String sourceDir = projectSource.getAbsolutePath();
            enunciateAddedSourceDirs.add(sourceDir);
            if (!project.getCompileSourceRoots().contains(sourceDir)) {
              getLog().debug("[ENUNCIATE] Adding '" + sourceDir + "' to the compile source roots.");
              project.addCompileSourceRoot(sourceDir);
            }
          }

          for (File testSource : extensions.getProjectTestSources()) {
            project.addTestCompileSourceRoot(testSource.getAbsolutePath());
          }

          for (File resourceDir : extensions.getProjectResourceDirectories()) {
            Resource restResource = new Resource();
            restResource.setDirectory(resourceDir.getAbsolutePath());
            project.addResource(restResource);
          }

          for (File resourceDir : extensions.getProjectTestResourceDirectories()) {
            Resource resource = new Resource();
            resource.setDirectory(resourceDir.getAbsolutePath());
            project.addTestResource(resource);
          }
        }

        applyAdditionalConfiguration(module);
      }
    }

    //add any new source directories to the project.
    Set<File> sourceDirs = new HashSet<File>();
    Collection<String> sourcePaths = (Collection<String>) project.getCompileSourceRoots();
    for (String sourcePath : sourcePaths) {
      File sourceDir = new File(sourcePath);
      if (!enunciateAddedSourceDirs.contains(sourceDir.getAbsolutePath())) {
        sourceDirs.add(sourceDir);
      }
      else {
        getLog().info("[ENUNCIATE] " + sourceDir + " appears to be added to the source roots by Enunciate.  Excluding from original source roots....");
      }
    }

    for (File sourceDir : sourceDirs) {
      enunciate.addSourceDir(sourceDir);
    }

    postProcessConfig(enunciate);

    try {
      enunciate.run();
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new MojoExecutionException("Error invoking Enunciate.", e);
    }

    if (this.artifacts != null) {
      for (Artifact projectArtifact : artifacts) {
        if (projectArtifact.getEnunciateArtifactId() == null) {
          getLog().warn("[ENUNCIATE] No enunciate export id specified.  Skipping project artifact...");
          continue;
        }

        com.webcohesion.enunciate.artifacts.Artifact artifact = null;
        for (com.webcohesion.enunciate.artifacts.Artifact enunciateArtifact : enunciate.getArtifacts()) {
          if (projectArtifact.getEnunciateArtifactId().equals(enunciateArtifact.getId())
            || enunciateArtifact.getAliases().contains(projectArtifact.getEnunciateArtifactId())) {
            artifact = enunciateArtifact;
            break;
          }
        }

        if (artifact != null) {
          try {
            File tempExportFile = enunciate.createTempFile(project.getArtifactId() + "-" + projectArtifact.getClassifier(), projectArtifact.getArtifactType());
            artifact.exportTo(tempExportFile, enunciate);
            projectHelper.attachArtifact(project, projectArtifact.getArtifactType(), projectArtifact.getClassifier(), tempExportFile);
          }
          catch (IOException e) {
            throw new MojoExecutionException("Error exporting Enunciate artifact.", e);
          }
        }
        else {
          getLog().warn("[ENUNCIATE] Enunciate artifact '" + projectArtifact.getEnunciateArtifactId() + "' not found in the project...");
        }
      }
    }

    postProcess(enunciate);

    getPluginContext().put(ConfigMojo.ENUNCIATE_PROPERTY, enunciate);
  }

  protected void postProcess(Enunciate enunciate) {

  }

  protected void applyAdditionalConfiguration(EnunciateModule module) {

  }

  protected String findSourceVersion() {
    String source = this.source;
    if (source == null) {
      List plugins = this.project.getBuildPlugins();
      for (Object plugin : plugins) {
        if (plugin instanceof Plugin && "org.apache.maven.plugins".equals(((Plugin) plugin).getGroupId()) && "maven-compiler-plugin".equals(((Plugin) plugin).getArtifactId()) && ((Plugin) plugin).getConfiguration() instanceof Xpp3Dom) {
          Xpp3Dom configuration = (Xpp3Dom) ((Plugin) plugin).getConfiguration();
          Xpp3Dom sourceConfig = configuration.getChild("source");
          if (sourceConfig != null) {
            source = sourceConfig.getValue();
          }
        }
      }
    }
    return source;
  }

  protected String findTargetVersion() {
    String target = this.target;
    if (target == null) {
      List plugins = this.project.getBuildPlugins();
      for (Object plugin : plugins) {
        if (plugin instanceof Plugin && "org.apache.maven.plugins".equals(((Plugin) plugin).getGroupId()) && "maven-compiler-plugin".equals(((Plugin) plugin).getArtifactId()) && ((Plugin) plugin).getConfiguration() instanceof Xpp3Dom) {
          Xpp3Dom configuration = (Xpp3Dom) ((Plugin) plugin).getConfiguration();
          Xpp3Dom targetConfig = configuration.getChild("target");
          if (targetConfig != null) {
            target = targetConfig.getValue();
          }
        }
      }
    }
    return target;
  }

  protected List<URL> buildPluginClasspath() throws MojoExecutionException {
    List<URL> classpath = new ArrayList<URL>();
    for (org.apache.maven.artifact.Artifact next : this.pluginDependencies) {
      try {
        classpath.add(next.getFile().toURI().toURL());
      }
      catch (MalformedURLException e) {
        throw new MojoExecutionException("Unable to add artifact " + next + " to the classpath.", e);
      }
    }
    return classpath;
  }

  protected void setClasspathAndSourcepath(Enunciate enunciate) throws MojoExecutionException {
    List<File> classpath = new ArrayList<File>();
    List<File> sourcepath = new ArrayList<File>();

    Set<org.apache.maven.artifact.Artifact> dependencies = new LinkedHashSet<org.apache.maven.artifact.Artifact>();
    dependencies.addAll(((Set<org.apache.maven.artifact.Artifact>) this.project.getArtifacts()));
    Iterator<org.apache.maven.artifact.Artifact> it = dependencies.iterator();
    while (it.hasNext()) {
      org.apache.maven.artifact.Artifact artifact = it.next();
      String artifactScope = artifact.getScope();
      String type = artifact.getType() == null ? "jar" : artifact.getType();
      if (!"jar".equals(type)) {
        //remove the non-jars from the classpath.
        it.remove();
      }
      else if (org.apache.maven.artifact.Artifact.SCOPE_TEST.equals(artifactScope)) {
        //remove just the test-scope artifacts from the classpath.
        it.remove();
      }
      else {
        classpath.add(artifact.getFile());
      }
    }

    List<org.apache.maven.artifact.Artifact> sourcepathDependencies = new ArrayList<org.apache.maven.artifact.Artifact>();
    for (org.apache.maven.artifact.Artifact projectDependency : dependencies) {
      if (projectDependency.getGroupId().equals(this.project.getGroupId())) {
        if (getLog().isDebugEnabled()) {
          getLog().debug("[ENUNCIATE] Attempt will be made to lookup the sources for " + projectDependency + " because it has the same groupId as the current project.");
        }
        sourcepathDependencies.add(projectDependency);
      }
      else if (this.sourcepathIncludes != null) {
        for (DependencySourceSpec include : this.sourcepathIncludes) {
          if (include.specifies(projectDependency)) {
            if (getLog().isDebugEnabled()) {
              getLog().debug("[ENUNCIATE] Attempt will be made to lookup the sources for " + projectDependency + " because it was explicitly included in the plugin configuration.");
            }

            sourcepathDependencies.add(projectDependency);
            break;
          }
        }
      }
    }

    //now go through the excludes.
    if (this.sourcepathExcludes != null && sourcepathExcludes.length > 0) {
      Iterator<org.apache.maven.artifact.Artifact> sourcepathIt = sourcepathDependencies.iterator();
      while (sourcepathIt.hasNext()) {
        org.apache.maven.artifact.Artifact sourcepathDependency = sourcepathIt.next();
        for (DependencySourceSpec exclude : this.sourcepathExcludes) {
          if (exclude.specifies(sourcepathDependency)) {
            if (getLog().isDebugEnabled()) {
              getLog().debug("[ENUNCIATE] Attempt will NOT be made to lookup the sources for " + sourcepathDependency + " because it was explicitly excluded in the plugin configuration.");
            }

            sourcepathIt.remove();
          }
        }
      }
    }

    //now attempt the source path lookup for the needed dependencies
    for (org.apache.maven.artifact.Artifact sourcepathDependency : sourcepathDependencies) {
      try {
        org.apache.maven.artifact.Artifact sourceArtifact = this.artifactFactory.createArtifactWithClassifier(sourcepathDependency.getGroupId(), sourcepathDependency.getArtifactId(), sourcepathDependency.getVersion(), sourcepathDependency.getType(), "sources");
        this.artifactResolver.resolve(sourceArtifact, this.project.getRemoteArtifactRepositories(), this.localRepository);

        if (getLog().isDebugEnabled()) {
          getLog().debug("[ENUNCIATE] Source artifact found at " + sourceArtifact + ".");
        }

        sourcepath.add(sourceArtifact.getFile());
      }
      catch (Exception e) {
        if (getLog().isDebugEnabled()) {
          getLog().debug("[ENUNCIATE] Attempt to find source artifact for " + sourcepathDependency + " failed.");
        }
      }
    }

    enunciate.setClasspath(classpath);
    enunciate.setSourcepath(sourcepath);
  }

  protected void postProcessConfig(Enunciate enunciate) {
    //no-op in this implementation.
  }

  /**
   * Load the config, do filtering as needed.
   *
   * @param config     The config to load into.
   * @param configFile The config file.
   */
  protected void loadConfig(Enunciate config, File configFile) throws IOException, SAXException, MavenFilteringException {
    if (this.configFilter == null) {
      getLog().debug("[ENUNCIATE] No maven file filter was provided, so no filtering of the config file will be done.");
      config.loadConfiguration(configFile);
    }
    else {
      this.buildDir.mkdirs();
      File filteredConfig = File.createTempFile("enunciateConfig", ".xml", this.buildDir);
      getLog().debug("[ENUNCIATE] Filtering " + configFile + " to " + filteredConfig + "...");
      this.configFilter.copyFile(configFile, filteredConfig, true, this.project, new ArrayList(), true, "utf-8", this.session);
      config.loadConfiguration(filteredConfig);
    }
  }

  protected class MavenEnunciateLogger implements EnunciateLogger {
    @Override
    public void debug(String message, Object... formatArgs) {
      if (getLog().isDebugEnabled()) {
        getLog().debug("[ENUNCIATE] " + String.format(message, formatArgs));
      }
    }

    @Override
    public void info(String message, Object... formatArgs) {
      if (getLog().isInfoEnabled()) {
        getLog().info("[ENUNCIATE] " + String.format(message, formatArgs));
      }
    }

    @Override
    public void warn(String message, Object... formatArgs) {
      if (getLog().isWarnEnabled()) {
        getLog().warn("[ENUNCIATE] " + String.format(message, formatArgs));
      }
    }

    @Override
    public void error(String message, Object... formatArgs) {
      if (getLog().isErrorEnabled()) {
        getLog().error("[ENUNCIATE] " + String.format(message, formatArgs));
      }
    }

  }

}

  package org.codehaus.enunciate;

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

import com.webcohesion.enunciate.EnunciateConfiguration;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Goal which initializes an Enunciate build process.
 */
@SuppressWarnings ( "unchecked" )
@Mojo( name = "config", defaultPhase = LifecyclePhase.VALIDATE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class ConfigMojo extends AbstractMojo {

  public static final String ENUNCIATE_PROPERTY = "urn:" + ConfigMojo.class.getName() + "#enunciate";
  public static final String ENUNCIATE_STEPPER_PROPERTY = "urn:" + ConfigMojo.class.getName() + "#stepper";
  public static final String SOURCE_JAR_MAP_PROPERTY = "urn:" + ConfigMojo.class.getName() + "#source_jars";

  @Component
  protected MavenProjectHelper projectHelper;

  @Component
  protected MavenFileFilter configFilter;

  @Component
  protected ArtifactFactory artifactFactory;

  @Component
  protected ArtifactResolver artifactResolver;

  @Parameter( defaultValue = "${project}", required = true, readonly = true )
  protected MavenProject project;

  @Parameter( defaultValue = "${plugin.artifacts}", required = true, readonly = true)
  protected Collection<org.apache.maven.artifact.Artifact> pluginDepdendencies;

  @Parameter( defaultValue = "${session}", required = true, readonly = true )
  protected MavenSession session;

  @Parameter( defaultValue = "${localRepository}", required = true, readonly = true)
  protected ArtifactRepository localRepository;

  /**
   * The enunciate artifacts.
   */
  @Parameter
  protected org.codehaus.enunciate.Artifact[] artifacts;

  /**
   * The enunciate configuration file.
   */
  @Parameter
  protected File configFile = null;

  /**
   * The -encoding argument for the Java compiler Enunciate will use when compiling generated Java sources.
   */
  @Parameter( defaultValue = "${project.build.sourceEncoding}", property = "compilationEncoding" )
  protected String compilationEncoding = null;

  /**
   * The output directory for Enunciate.
   */
  @Parameter( defaultValue = "${project.build.directory}/enunciate", property = "enunciate.build.directory")
  protected File buildDir = null;

  /**
   * List of extra arguments to Enunciate's javac.
   */
  @Parameter
  protected String[] javacArguments;

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
   * Whether Enunciate should first compile the project with "javac" so compile errors will surface before Enunciate errors.
   */
  @Parameter( defaultValue = "false", property = "enunciate.javac.check" )
  protected boolean javacCheck = false;
  
  /**
   * javac -source version parameter
   */
  @Parameter( property = "enunciate.javac.sourceVersion" )
  private String javacSourceVersion = null;
  
  /**
   * javac -target version parameter
   */
  @Parameter( property = "enunciate.javac.targetVersion" )
  private String javacTargetVersion = null;

  /**
   * A flag used to disable enunciate. This is primarily intended for usage from the command line to occasionally adjust the build.
   */
  @Parameter(defaultValue = "false", property = "enunciate.skip")
  protected boolean skipEnunciate;

  public void execute() throws MojoExecutionException {
    if (skipEnunciate) {
      getLog().info("Skipping enunciate per configuration.");
      return;
    }

    Set<File> sourceDirs = new HashSet<File>();
    Collection<String> sourcePaths = (Collection<String>) project.getCompileSourceRoots();
    for (String sourcePath : sourcePaths) {
      File sourceDir = new File(sourcePath);
      if (!isEnunciateAdded(sourceDir)) {
        sourceDirs.add(sourceDir);
      }
      else {
        getLog().info(sourceDir + " appears to be added to the source roots by Enunciate.  Excluding from original source roots....");
      }
    }

    MavenSpecificEnunciate enunciate = loadMavenSpecificEnunciate(sourceDirs);
    enunciate.setJavacCheck(this.javacCheck);
    EnunciateConfiguration config = createEnunciateConfiguration();
    config.setLabel(project.getArtifactId());
    if (this.configFile != null) {
      try {
        loadConfig(config, this.configFile);
      }
      catch (Exception e) {
        throw new MojoExecutionException("Problem with enunciate config file " + this.configFile, e);
      }
      enunciate.setConfigFile(this.configFile);
    }
    else {
      File defaultConfig = new File(project.getBasedir(), "enunciate.xml");
      if (defaultConfig.exists()) {
        getLog().info(defaultConfig.getAbsolutePath() + " exists, so it will be used.");
        try {
          loadConfig(config, defaultConfig);
        }
        catch (Exception e) {
          throw new MojoExecutionException("Problem with enunciate config file " + defaultConfig, e);
        }
        enunciate.setConfigFile(defaultConfig);
      }
    }

    postProcessConfig(config);
    enunciate.setConfig(config);
    Set<org.apache.maven.artifact.Artifact> classpathEntries = new LinkedHashSet<org.apache.maven.artifact.Artifact>();
    classpathEntries.addAll(((Set<org.apache.maven.artifact.Artifact>) this.project.getArtifacts()));
    Iterator<org.apache.maven.artifact.Artifact> it = classpathEntries.iterator();
    while (it.hasNext()) {
      org.apache.maven.artifact.Artifact artifact = it.next();
      String artifactScope = artifact.getScope();
      if (org.apache.maven.artifact.Artifact.SCOPE_TEST.equals(artifactScope)) {
        //remove just the test-scope artifacts from the classpath.
        it.remove();
      }
    }

    StringBuffer classpath = new StringBuffer();
    Iterator<org.apache.maven.artifact.Artifact> classpathIt = classpathEntries.iterator();
    while (classpathIt.hasNext()) {
      classpath.append(classpathIt.next().getFile().getAbsolutePath());
      if (classpathIt.hasNext()) {
        classpath.append(File.pathSeparatorChar);
      }
    }
    if (additionalClasspathEntries != null) {
      for (String additionalClasspathEntry : additionalClasspathEntries) {
        classpath.append(File.pathSeparatorChar).append(additionalClasspathEntry);
      }
    }
    enunciate.setRuntimeClasspath(classpath.toString());

    classpathEntries.clear();
    classpathEntries.addAll(this.pluginDepdendencies);
    classpath = new StringBuffer();
    classpathIt = classpathEntries.iterator();
    while (classpathIt.hasNext()) {
      classpath.append(classpathIt.next().getFile().getAbsolutePath());
      if (classpathIt.hasNext()) {
        classpath.append(File.pathSeparatorChar);
      }
    }
    enunciate.setBuildClasspath(classpath.toString());


    if (this.generateDir != null) {
      enunciate.setGenerateDir(this.generateDir);
    }

    if (this.compileDir != null) {
      enunciate.setCompileDir(this.compileDir);
    }

    if (this.buildDir != null) {
      enunciate.setBuildDir(this.buildDir);
    }

    if (this.packageDir != null) {
      enunciate.setPackageDir(this.packageDir);
    }

    if (this.scratchDir != null) {
      enunciate.setScratchDir(this.scratchDir);
    }
    
    if (this.javacSourceVersion != null)
    {
    	enunciate.setJavacSourceVersion(this.javacSourceVersion);
    }
    
    if (this.javacTargetVersion != null)
    {
    	enunciate.setJavacTargetVersion(this.javacTargetVersion);
    }

    if (this.exports != null) {
      for (String exportId : this.exports.keySet()) {
        String filename = this.exports.get(exportId);
        if (filename == null || "".equals(filename)) {
          throw new MojoExecutionException("Invalid (empty or null) filename for export " + exportId + ".");
        }
        File exportFile = new File(filename);
        if (!exportFile.isAbsolute()) {
          exportFile = new File(this.outputDir, filename);
        }

        enunciate.addExport(exportId, exportFile);
      }
    }

    enunciate.setCompileDebugInfo(this.compileDebug);

    try {
      enunciate.loadMavenConfiguration();
      Enunciate.Stepper stepper = enunciate.getStepper();
      getPluginContext().put(ENUNCIATE_STEPPER_PROPERTY, stepper);
      getPluginContext().put(ENUNCIATE_PROPERTY, enunciate);
    }
    catch (Exception e) {
      throw new MojoExecutionException("Error initializing Enunciate mechanism.", e);
    }
  }

  /**
   * Does any post-processing of the enunciate configuration.
   *
   * @param config The config.
   */
  protected void postProcessConfig(EnunciateConfiguration config) {
    if (this.includes != null) {
      for (String include : this.includes) {
        config.addApiIncludePattern(include);
      }
    }

    if (this.excludes != null) {
      for (String exclude : this.excludes) {
        config.addApiExcludePattern(exclude);
      }
    }

    config.setIncludeReferenceTrailInErrors(this.includeReferenceTrailInErrors);
  }

  /**
   * Load the config, do filtering as needed.
   *
   * @param config     The config to load into.
   * @param configFile The config file.
   */
  protected void loadConfig(EnunciateConfiguration config, File configFile) throws IOException, SAXException, MavenFilteringException {
    if (this.configFilter == null) {
      getLog().debug("No maven file filter was provided, so no filtering of the config file will be done.");
      config.load(configFile);
    }
    else {
      this.scratchDir.mkdirs();
      File filteredConfig = File.createTempFile("enunciateConfig", ".xml", this.scratchDir);
      getLog().debug("Filtering " + configFile + " to " + filteredConfig + "...");
      this.configFilter.copyFile(configFile, filteredConfig, true, this.project, new ArrayList(), true, "utf-8", this.session);
      config.load(filteredConfig);
    }
  }

  /**
   * Whether the given source directory is Enunciate-generated.
   *
   * @param sourceDir The source directory.
   * @return Whether the given source directory is Enunciate-generated.Whether the given source directory is Enunciate-generated.
   */
  protected boolean isEnunciateAdded(File sourceDir) {
    return ENUNCIATE_ADDED.contains(sourceDir.getAbsolutePath());
  }

  /**
   * Adds the specified source directory to the Maven project.
   *
   * @param dir The directory to add to the project.
   */
  protected void addSourceDirToProject(File dir) {
    String sourceDir = dir.getAbsolutePath();
    ENUNCIATE_ADDED.add(sourceDir);
    if (!project.getCompileSourceRoots().contains(sourceDir)) {
      getLog().debug("Adding '" + sourceDir + "' to the compile source roots.");
      project.addCompileSourceRoot(sourceDir);
    }
  }

  /**
   * Create an Enunciate configuration.
   *
   * @return The enunciate configuration.
   */
  protected EnunciateConfiguration createEnunciateConfiguration() {
    return new EnunciateConfiguration();
  }

  /**
   * Loads a correct instance of the Maven-specific Enunciate mechanism.
   *
   * @param sourceDirs The directories where the source files exist.
   * @return The maven-specific Enunciate mechanism.
   */
  protected MavenSpecificEnunciate loadMavenSpecificEnunciate(Set<File> sourceDirs) {
    return new MavenSpecificEnunciate(sourceDirs);
  }

  protected Set<String> getExcludedProjectExtensions() {
    TreeSet<String> excluded = new TreeSet<String>();
    if (excludeProjectExtensions != null) {
      excluded.addAll(Arrays.asList(excludeProjectExtensions));
    }

    if (!addActionscriptSources) {
      excluded.add("amf");
    }

    if (!addGWTSources) {
      excluded.add("gwt");
    }

    if (!addJavaClientSourcesToTestClasspath) {
      excluded.add("java-client");
    }

    if (!addXFireClientSourcesToTestClasspath) {
      excluded.add("xfire-client");
    }

    return excluded;
  }

  protected String lookupSourceJar(File pathEntry) {
    Map<File, String> sourceJars = (Map<File, String>) getPluginContext().get(SOURCE_JAR_MAP_PROPERTY);
    if (sourceJars == null) {
      sourceJars = new TreeMap<File, String>();
      getPluginContext().put(SOURCE_JAR_MAP_PROPERTY, sourceJars);
    }

    if (sourceJars.containsKey(pathEntry)) {
      return sourceJars.get(pathEntry);
    }

    String sourceJar = null;
    for (org.apache.maven.artifact.Artifact projectDependency : ((Set<org.apache.maven.artifact.Artifact>) this.project.getArtifacts())) {
      if (pathEntry.equals(projectDependency.getFile())) {
        if (skipSourceJarLookup(projectDependency)) {
          getLog().debug("Skipping the source lookup for " + projectDependency.toString() + "...");
        }

        getLog().debug("Attemping to lookup source artifact for " + projectDependency.toString() + "...");
        try {
          org.apache.maven.artifact.Artifact sourceArtifact = this.artifactFactory.createArtifactWithClassifier(projectDependency.getGroupId(), projectDependency.getArtifactId(),
                                                                                      projectDependency.getVersion(), projectDependency.getType(), "sources");
          this.artifactResolver.resolve(sourceArtifact, this.project.getRemoteArtifactRepositories(), this.localRepository);
          String path = sourceArtifact.getFile().getAbsolutePath();
          getLog().debug("Source artifact found at " + path + ".");
          sourceJar = path;
          break;
        }
        catch (Exception e) {
          getLog().debug("Unable to lookup source artifact for path entry " + pathEntry, e);
          break;
        }
      }
    }

    sourceJars.put(pathEntry, sourceJar);

    return sourceJar;
  }

  /**
   * Whether to skip the source-jar lookup for the given dependency.
   *
   * @param projectDependency The dependency.
   * @return Whether to skip the source-jar lookup for the given dependency.
   */
  protected boolean skipSourceJarLookup(org.apache.maven.artifact.Artifact projectDependency) {
    String groupId = String.valueOf(projectDependency.getGroupId());
    return groupId.startsWith("com.sun.") //skip com.sun.*
      || "com.sun".equals(groupId) //skip com.sun
      || groupId.startsWith("javax."); //skip "javax.*"
  }

  /**
   * Enunciate mechanism that logs via the Maven logging mechanism.
   */
  protected class MavenSpecificEnunciate extends Enunciate {

    public MavenSpecificEnunciate(Collection<File> rootDirs) {
      super();
      ArrayList<String> sources = new ArrayList<String>();
      for (File rootDir : rootDirs) {
        sources.addAll(getJavaFiles(rootDir));
      }

      setSourceFiles(sources.toArray(new String[sources.size()]));
      setEncoding(compilationEncoding);
      if (javacArguments != null) {
        getConfiguredJavacArguments().addAll(Arrays.asList(javacArguments));
      }
    }

    public void loadMavenConfiguration() throws IOException {
      for (DeploymentModule module : getConfig().getAllModules()) {
        if (!module.isDisabled()) {
          if (gwtHome != null && (module instanceof GWTHomeAwareModule)) {
            ((GWTHomeAwareModule) module).setGwtHome(gwtHome);
          }
          else if (flexHome != null && (module instanceof FlexHomeAwareModule)) {
            ((FlexHomeAwareModule) module).setFlexHome(flexHome);
          }
        }
      }
    }

    @Override
    protected void initModules(Collection<DeploymentModule> modules) throws EnunciateException, IOException {
      super.initModules(modules);

      if (compileDir == null) {
        //set an explicit compile dir if one doesn't exist because we're going to need to reference it to set the output directory for Maven.
        setCompileDir(createTempDir());
      }

      for (DeploymentModule module : modules) {
        if (!module.isDisabled()) {
          if (project.getName() != null && !"".equals(project.getName().trim()) && module instanceof ProjectTitleAware) {
            ((ProjectTitleAware) module).setTitleConditionally(project.getName());
          }
          if (project.getVersion() != null && !"".equals(project.getVersion().trim()) && module instanceof ProjectVersionAware) {
            ((ProjectVersionAware) module).setProjectVersion(project.getVersion());
          }
        }
      }
    }

    @Override
    protected void doGenerate() throws IOException, EnunciateException {
      super.doGenerate();

      Set<String> excludedExtensions = getExcludedProjectExtensions();
      for (DeploymentModule module : getConfig().getAllModules()) {
        if (!module.isDisabled() && (module instanceof ProjectExtensionModule) && !excludedExtensions.contains(module.getName())) {
          ProjectExtensionModule extensions = (ProjectExtensionModule) module;
          for (File projectSource : extensions.getProjectSources()) {
            addSourceDirToProject(projectSource);
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
      }
    }

    @Override
    protected String lookupSourceEntry(File pathEntry) {
      return lookupSourceJar(pathEntry);
    }

    @Override
    public void info(String message, Object... formatArgs) {
      getLog().info(String.format(message, formatArgs));
    }

    @Override
    public void debug(String message, Object... formatArgs) {
      getLog().debug(String.format(message, formatArgs));
    }

    @Override
    public void warn(String message, Object... formatArgs) {
      getLog().warn(String.format(message, formatArgs));
    }

    @Override
    public void error(String message, Object... formatArgs) {
      getLog().error(String.format(message, formatArgs));
    }

    @Override
    public boolean isDebug() {
      return getLog().isDebugEnabled();
    }

    @Override
    public boolean isVerbose() {
      return getLog().isInfoEnabled();
    }

    @Override
    protected void doClose() throws EnunciateException, IOException {
      super.doClose();

      if (artifacts != null) {
        for (org.codehaus.enunciate.Artifact projectArtifact : artifacts) {
          if (projectArtifact.getEnunciateArtifactId() == null) {
            getLog().warn("No enunciate export id specified.  Skipping project artifact...");
            continue;
          }

          org.codehaus.enunciate.main.Artifact artifact = null;
          for (org.codehaus.enunciate.main.Artifact enunciateArtifact : getArtifacts()) {
            if (projectArtifact.getEnunciateArtifactId().equals(enunciateArtifact.getId())
              || enunciateArtifact.getAliases().contains(projectArtifact.getEnunciateArtifactId())) {
              artifact = enunciateArtifact;
              break;
            }
          }

          if (artifact != null) {
            File tempExportFile = createTempFile(project.getArtifactId() + "-" + projectArtifact.getClassifier(), projectArtifact.getArtifactType());
            artifact.exportTo(tempExportFile, this);
            projectHelper.attachArtifact(project, projectArtifact.getArtifactType(), projectArtifact.getClassifier(), tempExportFile);
          }
          else {
            getLog().warn("Enunciate artifact '" + projectArtifact.getEnunciateArtifactId() + "' not found in the project...");
          }
        }
      }
    }
  }
}

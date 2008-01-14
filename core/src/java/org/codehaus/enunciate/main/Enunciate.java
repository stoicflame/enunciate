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

package org.codehaus.enunciate.main;

import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateAnnotationProcessorFactory;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.config.APIImport;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;

/**
 * Main enunciate entry point.
 *
 * @author Ryan Heaton
 */
public class Enunciate {

  /**
   * The targets for the project.
   */
  public enum Target {

    /**
     * Generate the XML APIs and source code.
     */
    GENERATE,

    /**
     * Generate and compile.
     */
    COMPILE,

    /**
     * Compile and build the structure.
     */
    BUILD,

    /**
     * Build and package.
     */
    PACKAGE
  }

  private boolean verbose = false;
  private boolean debug = false;
  private boolean javacCheck = false;

  private File configFile;
  private File generateDir;
  private File compileDir;
  private File buildDir;
  private File packageDir;
  private String classpath;
  private EnunciateConfiguration config;
  private Target target = Target.PACKAGE;
  private final HashMap<String, Object> properties = new HashMap<String, Object>();
  private final Set<Artifact> artifacts = new TreeSet<Artifact>();
  private final HashMap<String, File> exports = new HashMap<String, File>();
  private final List<String> sourceFiles = new ArrayList<String>();

  public static void main(String[] args) throws Exception {
    Main.main(args);
  }

  /**
   * Protected to ensure the source files are set.
   */
  protected Enunciate() {
  }

  /**
   * Protected to allow the source files to be set after construction.
   *
   * @param sourceFiles The source files to enunciate.
   */
  protected void setSourceFiles(String[] sourceFiles) {
    for (String sourceFile : sourceFiles) {
      if (!sourceFile.endsWith(".java")) {
        throw new IllegalArgumentException("Illegal name of java source file: " + sourceFile + ".  (Must end with \".java\")");
      }
    }

    this.sourceFiles.addAll(Arrays.asList(sourceFiles));
  }

  /**
   * Construct an enunciate mechanism on the specified source files.
   *
   * @param sourceFiles The source files.
   */
  public Enunciate(String[] sourceFiles) {
    setSourceFiles(sourceFiles);
  }

  /**
   * Construct an enunciate mechanism on the specified source files with the specified config.
   *
   * @param sourceFiles The source files.
   * @param config      The config
   */
  public Enunciate(String[] sourceFiles, EnunciateConfiguration config) {
    setSourceFiles(sourceFiles);
    this.config = config;
  }

  /**
   * Construct an enunciate mechanism on the specified source files.
   *
   * @param sourceFiles The source files.
   */
  public Enunciate(List<String> sourceFiles) {
    setSourceFiles(sourceFiles.toArray(new String[sourceFiles.size()]));
  }

  /**
   * Get a stepper that can be used to step through the Enunciate mechanism, which will be initialized.
   *
   * @return A stepper.
   * @throws java.util.ConcurrentModificationException
   *                            If a stepper has already been retrieved.
   * @throws EnunciateException If there was an error initializing the Enunciate mechanism.
   * @throws IOException        If there was an error initializing the Enunciate mechanism.
   */
  public Stepper getStepper() throws EnunciateException, IOException {
    return new Stepper();
  }

  /**
   * Execute the mechanism.
   */
  public void execute() throws EnunciateException, IOException {
    Enunciate.Stepper stepper = getStepper();
    stepper.stepTo(this.target);
    stepper.close();
  }

  /**
   * Logic for handling the closing of the Enunciate mechanism.  Closes the modules and exports
   * the artifacts.
   *
   * @param deploymentModules The deployment modules to close.
   */
  protected void doClose(List<DeploymentModule> deploymentModules) throws EnunciateException, IOException {
    info("\n\nClosing Enunciate mechanism.");
    for (DeploymentModule deploymentModule : deploymentModules) {
      debug("Closing module %s.", deploymentModule.getName());
      deploymentModule.close();
    }

    HashSet<String> exportedArtifacts = new HashSet<String>();
    for (Artifact artifact : artifacts) {
      String artifactId = artifact.getId();
      if (this.exports.containsKey(artifactId)) {
        File dest = this.exports.get(artifactId);
        info("\n\nExporting artifact %s to %s.", artifactId, dest);
        artifact.exportTo(dest, this);
        exportedArtifacts.add(artifactId);
      }
    }

    for (String export : this.exports.keySet()) {
      if (!exportedArtifacts.remove(export)) {
        warn("WARNING: Unknown artifact '%s'.  Artifact will not be exported.", export);
      }
    }
  }

  /**
   * Do the package logic.
   *
   * @param deploymentModules The deployment modules to use.
   */
  protected void doPackage(List<DeploymentModule> deploymentModules) throws IOException, EnunciateException {
    File packageDir = getPackageDir();
    if (packageDir == null) {
      packageDir = createTempDir();
      debug("No package directory specified, assigned %s.", packageDir);
      setPackageDir(packageDir);
    }

    if (packageDir.equals(getBuildDir())) {
      throw new EnunciateException("The package output directory cannot be the same as the build directory. " +
        "(BTW, if you don't specify a package output directory, a suitable temp directory wil be created for you.)");
    }

    for (DeploymentModule deploymentModule : deploymentModules) {
      debug("Invoking %s step for module %s", Target.PACKAGE, deploymentModule.getName());
      deploymentModule.step(Target.PACKAGE);
    }
  }

  /**
   * Do the build logic.
   *
   * @param deploymentModules The deployment modules to use.
   */
  protected void doBuild(List<DeploymentModule> deploymentModules) throws IOException, EnunciateException {
    File buildDir = getBuildDir();
    if (buildDir == null) {
      buildDir = createTempDir();
      debug("No build directory specified, assigned %s.", buildDir);
      setBuildDir(buildDir);
    }

    if (buildDir.equals(getCompileDir())) {
      throw new EnunciateException("The build output directory cannot be the same as the compile directory. " +
        "(BTW, if you don't specify a build output directory, a suitable temp directory wil be created for you.)");
    }

    for (DeploymentModule deploymentModule : deploymentModules) {
      debug("Invoking %s step for module %s", Target.BUILD, deploymentModule.getName());
      deploymentModule.step(Target.BUILD);
    }
  }

  /**
   * Do the compile logic.
   *
   * @param deploymentModules The deployment modules to use.
   */
  protected void doCompile(List<DeploymentModule> deploymentModules) throws IOException, EnunciateException {
    File destdir = getCompileDir();
    if (destdir == null) {
      destdir = createTempDir();
      debug("No compile directory specified, assigned %s.", destdir);
      setCompileDir(destdir);
    }

    if (compileDir.equals(getBuildDir())) {
      throw new EnunciateException("The compile output directory cannot be the same as the generate directory. " +
        "(BTW, if you don't specify a compile output directory, a suitable temp directory wil be created for you.)");
    }

    for (DeploymentModule deploymentModule : deploymentModules) {
      debug("Invoking %s step for module %s", Target.COMPILE, deploymentModule.getName());
      deploymentModule.step(Target.COMPILE);
    }
  }

  /**
   * Do the generate logic.
   *
   * @param deploymentModules The deployment modules to use.
   */
  protected void doGenerate(List<DeploymentModule> deploymentModules) throws IOException, EnunciateException {
    File genDir = getGenerateDir();
    if (genDir == null) {
      genDir = createTempDir();
      debug("No generate directory specified, assigned %s.", genDir);
      setGenerateDir(genDir);
    }

    //handle the API imports/exports.
    List<String> classpath = new ArrayList<String>(Arrays.asList(getEnunciateClasspath().split(File.pathSeparator)));
    List<URL> classpathURLs = new ArrayList<URL>(classpath.size());
    for (String pathItem : classpath) {
      File pathFile = new File(pathItem);
      if (pathFile.exists()) {
        classpathURLs.add(pathFile.toURL());
      }
    }

    final URLClassLoader loader = new URLClassLoader(classpathURLs.toArray(new URL[classpathURLs.size()]));

    Map<String, Boolean> imports2seekSource = new HashMap<String, Boolean>();
    Enumeration<URL> exportResources = loader.getResources("META-INF/enunciate/api-exports");
    while (exportResources.hasMoreElements()) {
      URL url = exportResources.nextElement();
      BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
      String line = reader.readLine();
      info("Importing class %s, which was automatically imported from %s...", line, url);
      while (line != null) {
        imports2seekSource.put(line, true);
      }
    }

    if ((this.config != null) && (this.config.getAPIImports().size() > 0)) {
      for (APIImport apiImport : this.config.getAPIImports()) {
        if (apiImport.getClassname() != null) {
          info("Importing explicitly-imported class %s...", apiImport.getClassname());
          imports2seekSource.put(apiImport.getClassname(), apiImport.isSeekSource());
        }
      }
    }

    ArrayList<String> sourceFiles = new ArrayList<String>(Arrays.asList(getSourceFiles()));
    ArrayList<String> additionalApiClasses = new ArrayList<String>();
    if (!imports2seekSource.isEmpty()) {
      File tempSourcesDir = createTempDir();
      for (String classname : imports2seekSource.keySet()) {
        URL source = null;
        if (imports2seekSource.get(classname)) {
          source = loader.findResource(classname.replace('.', '/').concat(".java"));
        }
        if (source != null) {
          //we have the source for the class.  Add it to the source list.
          File srcFile = new File(tempSourcesDir, classname.replace('.', File.separatorChar).concat(".java"));
          if (!srcFile.getParentFile().exists()) {
            srcFile.getParentFile().mkdirs();
          }
          copyResource(source, srcFile);
          sourceFiles.add(srcFile.getAbsolutePath());
        }
        else if (loader.findResource(classname.replace('.', '/').concat(".class")) != null) {
          //no source found, just use the class.
          additionalApiClasses.add(classname);
        }
        else {
          warn("API Import class '%s' not found in the Enunciate classpath.  It will not be imported.", classname);
        }
      }
    }

    invokeApt(sourceFiles.toArray(new String[sourceFiles.size()]), additionalApiClasses.toArray(new String[additionalApiClasses.size()]));
  }

  /**
   * Do the initialization logic.  Loads and initializes the deployment modules.
   *
   * @return The deployment modules that were loaded and initialized.
   */
  protected List<DeploymentModule> doInit() throws EnunciateException, IOException {
    if ((this.sourceFiles == null) || (this.sourceFiles.isEmpty())) {
      throw new EnunciateException("No source files have been specified!");
    }

    if (isJavacCheck()) {
      invokeJavac(createTempDir(), getSourceFiles());
    }

    if (this.config == null) {
      this.config = loadConfig();
    }

    List<DeploymentModule> deploymentModules = this.config.getEnabledModules();
    initModules(deploymentModules);
    return deploymentModules;
  }

  /**
   * Initialize each module.
   *
   * @param deploymentModules The deployment modules.
   */
  protected void initModules(List<DeploymentModule> deploymentModules) throws EnunciateException, IOException {
    info("\n\nInitializing Enunciate mechanism.");
    for (DeploymentModule deploymentModule : deploymentModules) {
      debug("Initializing module %s.", deploymentModule.getName());
      deploymentModule.init(this);
    }
  }

  /**
   * Handle an info-level message.
   *
   * @param message    The info message.
   * @param formatArgs The format args of the message.
   */
  public void info(String message, Object... formatArgs) {
    if (isVerbose()) {
      System.out.println(String.format(message, formatArgs));
    }
  }

  /**
   * Handle a debug-level message.
   *
   * @param message    The debug message.
   * @param formatArgs The format args of the message.
   */
  public void debug(String message, Object... formatArgs) {
    if (isDebug()) {
      System.out.println(String.format(message, formatArgs));
    }
  }

  /**
   * Handle a warn-level message.
   *
   * @param message    The warn message.
   * @param formatArgs The format args of the message.
   */
  public void warn(String message, Object... formatArgs) {
    System.out.println(String.format(message, formatArgs));
  }

  /**
   * Creates a temporary directory.
   *
   * @return A temporary directory.
   */
  public File createTempDir() throws IOException {
    File tempDir = File.createTempFile("enunciate", "");
    tempDir.delete();
    tempDir.mkdirs();

    debug("Created directory %s", tempDir);

    return tempDir;
  }

  /**
   * Get the source files for this enunciate mechanism.
   *
   * @return The source files.
   */
  public String[] getSourceFiles() {
    return this.sourceFiles.toArray(new String[this.sourceFiles.size()]);
  }

  /**
   * Modifiable list of the Enunciate source files.
   *
   * @return The modifiable list of enunciate source files.
   */
  public List<String> getSourceFileList() {
    return this.sourceFiles;
  }

  /**
   * Reads the enunciate configuration from the specified file, if any.
   *
   * @return The configuration, or null if none is specified.
   */
  protected EnunciateConfiguration loadConfig() throws IOException {
    EnunciateConfiguration config = new EnunciateConfiguration();
    File configFile = getConfigFile();
    if (configFile == null) {
      info("No config file specified, using defaults....");
    }
    else if (!configFile.exists()) {
      warn("Config file %s doesn't exist, using defaults....", configFile);
    }
    else {
      try {
        config.load(configFile);
      }
      catch (SAXException e) {
        throw new IOException("Error parsing enunciate configuration file " + configFile + ": " + e.getMessage());
      }
    }

    return config;
  }

  /**
   * Finds all java files in the specified base directory.
   *
   * @param basedir The base directory.
   * @return The collection of java files.
   */
  public Collection<String> getJavaFiles(File basedir) {
    ArrayList<String> files = new ArrayList<String>();
    findFiles(basedir, JAVA_FILTER, files);
    return files;
  }

  /**
   * Finds all files in the specified base directory using the specified filter.
   *
   * @param basedir The base directory.
   * @param filter  The filter to use.
   * @return The collection of files.
   */
  public Collection<String> getFiles(File basedir, FileFilter filter) {
    ArrayList<String> files = new ArrayList<String>();
    findFiles(basedir, filter, files);
    return files;
  }

  /**
   * Finds all files in the specified directory (recursively) using the specified filter.
   *
   * @param dir       The directory.
   * @param filter    The filter.
   * @param filenames A container for the files.
   */
  private void findFiles(File dir, FileFilter filter, Collection<String> filenames) {
    File[] files = dir.listFiles(filter);
    if (files != null) {
      for (File file : files) {
        filenames.add(file.getAbsolutePath());
      }
    }

    File[] dirs = dir.listFiles(DIR_FILTER);
    if (dirs != null) {
      for (File subdir : dirs) {
        findFiles(subdir, filter, filenames);
      }
    }
  }

  /**
   * The enunciate classpath to use for javac and apt.
   *
   * @return The enunciate classpath to use for javac and apt.
   */
  public String getEnunciateClasspath() {
    String classpath = getClasspath();
    if (classpath == null) {
      classpath = System.getProperty("java.class.path");
    }
    return classpath;
  }

  /**
   * Invokes APT on the specified source files.
   *
   * @param sourceFiles The source files.
   * @param additionalApiClasses The FQNs of additional classes (should be found on the Enunciate classpath) that comprise the API.
   */
  protected void invokeApt(String[] sourceFiles, String... additionalApiClasses) throws IOException, EnunciateException {
    ArrayList<String> args = new ArrayList<String>();
    String classpath = getEnunciateClasspath();

    args.add("-cp");
    args.add(classpath);

    if (isVerbose()) {
      args.add(EnunciateAnnotationProcessorFactory.VERBOSE_OPTION);
    }

    args.add("-nocompile");

    if (getGenerateDir() != null) {
      args.add("-s");
      args.add(getGenerateDir().getAbsolutePath());
    }

    args.addAll(Arrays.asList(sourceFiles));

    if (isDebug()) {
      StringBuilder message = new StringBuilder("Invoking APT with arguments:");
      for (String arg : args) {
        message.append(' ');
        message.append(arg);
      }
      debug(message.toString());
    }

    EnunciateAnnotationProcessorFactory apf = new EnunciateAnnotationProcessorFactory(this, additionalApiClasses);
    com.sun.tools.apt.Main.process(apf, args.toArray(new String[args.size()]));
    apf.throwAnyErrors();
  }

  /**
   * Invokes javac on the specified source files. The classpath will be the classpath for this enunciate mechanism,
   * if specified, otherwise the system classpath.
   *
   * @param compileDir  The compile directory.
   * @param sourceFiles The source files.
   * @throws EnunciateException if the compile fails.
   */
  public void invokeJavac(File compileDir, String[] sourceFiles) throws EnunciateException {
    String classpath = getEnunciateClasspath();

    invokeJavac(classpath, compileDir, sourceFiles);
  }

  /**
   * Invokes javac on the specified source files.
   *
   * @param classpath   The classpath.
   * @param compileDir  The compile directory.
   * @param sourceFiles The source files.
   * @throws EnunciateException if the compile fails.
   */
  public void invokeJavac(String classpath, File compileDir, String[] sourceFiles) throws EnunciateException {
    invokeJavac(classpath, compileDir, new ArrayList<String>(), sourceFiles);
  }

  /**
   * Invokes javac on the specified source files.
   *
   * @param classpath      The classpath.
   * @param compileDir     The compile directory.
   * @param additionalArgs Any additional arguments to the compiler.
   * @param sourceFiles    The source files.
   * @throws EnunciateException if the compile fails.
   */
  public void invokeJavac(String classpath, File compileDir, List<String> additionalArgs, String[] sourceFiles) throws EnunciateException {
    List<String> args = new ArrayList<String>();

    args.add("-cp");
    args.add(classpath);

    if (isDebug()) {
      args.add("-verbose");
    }

    args.add("-d");
    args.add(compileDir.getAbsolutePath());
    args.addAll(additionalArgs);
    args.addAll(Arrays.asList(sourceFiles));

    if (isDebug()) {
      StringBuilder message = new StringBuilder("Invoking Javac with arguments:");
      for (String arg : args) {
        message.append(' ');
        message.append(arg);
      }
      debug(message.toString());
    }

    compileDir.mkdirs();
    int procCode = com.sun.tools.javac.Main.compile(args.toArray(new String[args.size()]));
    if (procCode != 0) {
      throw new EnunciateException("compile failed.");
    }
  }

  /**
   * Copy an entire directory from one place to another.
   *
   * @param from The source directory.
   * @param to   The destination directory.
   */
  public void copyDir(File from, File to) throws IOException {
    File[] files = from.listFiles();

    if (!to.exists()) {
      to.mkdirs();
    }

    for (File file : files) {
      if (file.isDirectory()) {
        copyDir(file, new File(to, file.getName()));
      }
      else {
        copyFile(file, new File(to, file.getName()));
      }
    }
  }

  /**
   * Extracts the (zipped up) base to the specified directory.
   *
   * @param baseIn The stream to the base.
   * @param toDir  The directory to extract to.
   */
  public void extractBase(InputStream baseIn, File toDir) throws IOException {
    ZipInputStream in = new ZipInputStream(baseIn);
    ZipEntry entry = in.getNextEntry();
    while (entry != null) {
      File file = new File(toDir, entry.getName());
      debug("Extracting %s to %s.", entry.getName(), file);
      if (entry.isDirectory()) {
        file.mkdirs();
      }
      else {
        FileOutputStream out = new FileOutputStream(file);
        byte[] buffer = new byte[1024 * 2]; //2 kb buffer should suffice.
        int len;
        while ((len = in.read(buffer)) > 0) {
          out.write(buffer, 0, len);
        }
        out.close();
      }

      in.closeEntry();
      entry = in.getNextEntry();
    }
  }

  /**
   * Copy a file from one directory to another, preserving directory structure.
   *
   * @param src     The source file.
   * @param fromDir The from directory.
   * @param toDir   The to directory.
   */
  public void copyFile(File src, File fromDir, File toDir) throws IOException {
    URI fromURI = fromDir.toURI();
    URI srcURI = src.toURI();
    URI relativeURI = fromURI.relativize(srcURI);
    File toFile = new File(toDir, relativeURI.getPath());
    copyFile(src, toFile);
  }

  /**
   * Copy a file from one location to another.
   *
   * @param from The source file.
   * @param to   The destination file.
   */
  public void copyFile(File from, File to) throws IOException {
    FileChannel srcChannel = new FileInputStream(from).getChannel();
    to = to.getAbsoluteFile();
    if ((!to.exists()) && (to.getParentFile() != null)) {
      to.getParentFile().mkdirs();
    }

    debug("Copying %s to %s ", from, to);
    FileChannel dstChannel = new FileOutputStream(to, false).getChannel();
    dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
    srcChannel.close();
    dstChannel.close();
  }

  /**
   * Copies a resource on the classpath to a file.
   *
   * @param resource The resource to copy.
   * @param to       The file to copy to.
   */
  public void copyResource(String resource, File to) throws IOException {
    URL url = getClass().getResource(resource);
    if (url == null) {
      throw new IOException("Request to copy a resource that was not found: " + resource);
    }
    copyResource(url, to);
  }

  /**
   * Copies a resource to a file.
   *
   * @param url The url of the resource.
   * @param to  The file to copy to.
   */
  public void copyResource(URL url, File to) throws IOException {
    InputStream stream = url.openStream();

    debug("Copying resource %s to %s...", url, to);
    FileOutputStream out = new FileOutputStream(to);
    byte[] buffer = new byte[1024 * 2]; //2 kb buffer should suffice.
    int len;
    while ((len = stream.read(buffer)) > 0) {
      out.write(buffer, 0, len);
    }
  }

  /**
   * zip up directories to a specified zip file.
   *
   * @param toFile The file to zip to.
   * @param dirs   The directories to zip up.
   */
  public void zip(File toFile, File... dirs) throws IOException {
    if (!toFile.getParentFile().exists()) {
      debug("Creating directory %s...", toFile.getParentFile());
      toFile.getParentFile().mkdirs();
    }

    byte[] buffer = new byte[2 * 1024]; //buffer of 2K should be fine.
    ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(toFile));
    for (File dir : dirs) {

      URI baseURI = dir.toURI();
      debug("Adding contents of directory %s to zip file %s...", dir, toFile);
      ArrayList<File> files = new ArrayList<File>();
      buildFileList(files, dir);
      for (File file : files) {
        ZipEntry entry = new ZipEntry(baseURI.relativize(file.toURI()).getPath());
        debug("Adding entry %s...", entry.getName());
        zipout.putNextEntry(entry);

        if (!file.isDirectory()) {
          FileInputStream in = new FileInputStream(file);
          int len;
          while ((len = in.read(buffer)) > 0) {
            zipout.write(buffer, 0, len);
          }
          in.close();
        }

        // Complete the entry
        zipout.closeEntry();
      }
    }

    zipout.close();
  }

  /**
   * Resolves a path relative to the Enunciate config file, if present, otherwise the file will be a relative file
   * to the current user directory.
   *
   * @param filePath The file path to resolve.
   * @return The resolved file.
   */
  public File resolvePath(String filePath) {
    File downloadFile = new File(filePath);

    if (!downloadFile.isAbsolute()) {
      //try to relativize this download file to the directory of the config file.
      File configFile = getConfigFile();
      if (configFile != null) {
        downloadFile = new File(configFile.getAbsoluteFile().getParentFile(), filePath);
        debug("%s relatived to %s.", filePath, downloadFile.getAbsolutePath());
      }
    }
    return downloadFile;
  }

  /**
   * Adds all files in specified directories to a list.
   *
   * @param list The list.
   * @param dirs The directories.
   */
  protected void buildFileList(List<File> list, File... dirs) {
    for (File dir : dirs) {
      for (File file : dir.listFiles()) {
        list.add(file);

        if (file.isDirectory()) {
          buildFileList(list, file);
        }
      }
    }
  }

  /**
   * Whether to be verbose.
   *
   * @return Whether to be verbose.
   */
  public boolean isVerbose() {
    return verbose || isDebug();
  }

  /**
   * Whether to be verbose.
   *
   * @param verbose Whether to be verbose.
   */
  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  /**
   * Whether to print debugging information.
   *
   * @return Whether to print debugging information.
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * Whether to print debugging information.
   *
   * @param debug Whether to print debugging information.
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  /**
   * The version of the Enunciate mechanism.
   *
   * @return The version of the Enunciate mechanism.
   */
  public String getVersion() {
    String version = "1.0";
    URL resource = Enunciate.class.getResource("/enunciate.properties");
    if (resource != null) {
      Properties properties = new Properties();
      try {
        properties.load(resource.openStream());
        version = (String) properties.get("version");
      }
      catch (IOException e) {
        //fall through...
      }
    }
    return version;
  }

  /**
   * Whether to do a javac check before invoking the Enunciate mechanism.
   *
   * @return Whether to do a javac check before invoking the Enunciate mechanism.
   */
  public boolean isJavacCheck() {
    return javacCheck;
  }

  /**
   * Whether to do a javac check before invoking the Enunciate mechanism.
   *
   * @param javacCheck Whether to do a javac check before invoking the Enunciate mechanism.
   */
  public void setJavacCheck(boolean javacCheck) {
    this.javacCheck = javacCheck;
  }

  /**
   * The enunciate config file.
   *
   * @return The enunciate config file.
   */
  public File getConfigFile() {
    return configFile;
  }

  /**
   * The enunciate config file.
   *
   * @param configFile The enunciate config file.
   */
  public void setConfigFile(File configFile) {
    this.configFile = configFile;
  }

  /**
   * The destination directory for the compiled classes.
   *
   * @return The destination directory for the compiled classes.
   */
  public File getCompileDir() {
    return compileDir;
  }

  /**
   * The destination directory for the compiled classes.
   *
   * @param compileDir The destination directory for the compiled classes.
   */
  public void setCompileDir(File compileDir) {
    this.compileDir = compileDir;
  }

  /**
   * The directory to use to build the war.
   *
   * @return The directory to use to build the war.
   */
  public File getBuildDir() {
    return buildDir;
  }

  /**
   * The directory to use to build the war.
   *
   * @param buildDir The directory to use to build the war.
   */
  public void setBuildDir(File buildDir) {
    this.buildDir = buildDir;
  }

  /**
   * The package directory.
   *
   * @return The package directory.
   */
  public File getPackageDir() {
    return packageDir;
  }

  /**
   * The package directory.
   *
   * @param packageDir The package directory.
   */
  public void setPackageDir(File packageDir) {
    this.packageDir = packageDir;
  }

  /**
   * The preprocessor directory (-s).
   *
   * @return The preprocessor directory (-s).
   */
  public File getGenerateDir() {
    return generateDir;
  }

  /**
   * The preprocessor directory (-s).
   *
   * @param generateDir The preprocessor directory (-s).
   */
  public void setGenerateDir(File generateDir) {
    this.generateDir = generateDir;
  }

  /**
   * The classpath.
   *
   * @return The classpath.
   */
  public String getClasspath() {
    return classpath;
  }

  /**
   * The classpath.
   *
   * @param classpath The classpath.
   */
  public void setClasspath(String classpath) {
    this.classpath = classpath;
  }

  /**
   * The target.
   *
   * @return The target.
   */
  public Target getTarget() {
    return target;
  }

  /**
   * The target.
   *
   * @param target The target.
   */
  public void setTarget(Target target) {
    this.target = target;
  }

  /**
   * Set a property value.
   *
   * @param property The property.
   * @param value    The value.
   */
  public void setProperty(String property, Object value) {
    this.properties.put(property, value);
  }

  /**
   * The artifacts exportable by enunciate.
   *
   * @return The artifacts exportable by enunciate.
   */
  public Set<Artifact> getArtifacts() {
    return Collections.unmodifiableSet(artifacts);
  }

  /**
   * Finds the artifact of the given id.
   *
   * @param artifactId The id of the artifact.
   * @return The artifact, or null if the artifact wasn't found.
   */
  public Artifact findArtifact(String artifactId) {
    if (artifactId != null) {
      for (Artifact artifact : artifacts) {
        if (artifactId.equals(artifact.getId())) {
          return artifact;
        }
      }
    }

    return null;
  }

  /**
   * Adds the specified artifact.
   *
   * @param artifact The artifact to add.
   * @return Whether the artifact was successfully added.
   */
  public boolean addArtifact(Artifact artifact) {
    info("Artifact %s added for module %s.", artifact.getId(), artifact.getModule());
    return this.artifacts.add(artifact);
  }

  /**
   * Adds an export.
   *
   * @param artifactId  The id of the artifact to export.
   * @param destination The file or directory to export the artifact to.
   */
  public void addExport(String artifactId, File destination) {
    this.exports.put(artifactId, destination);
  }

  /**
   * Get a property value.
   *
   * @param property The property whose value to retrieve.
   * @return The property value.
   */
  public Object getProperty(String property) {
    return this.properties.get(property);
  }

  /**
   * @return The configuration.
   */
  public EnunciateConfiguration getConfig() {
    return config;
  }

  /**
   * Whether the specified module is enabled.
   *
   * @param moduleName The name of the module.
   * @return Whether the module is enabled.
   */
  public boolean isModuleEnabled(String moduleName) {
    if (this.config != null) {
      for (DeploymentModule module : this.config.getEnabledModules()) {
        if (module.getName().equals(moduleName)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Set the configuration for the mechanism.
   *
   * @param config The configuration.
   */
  public void setConfig(EnunciateConfiguration config) {
    this.config = config;
  }

  /**
   * A file filter for java files.
   */
  private static FileFilter JAVA_FILTER = new FileFilter() {
    public boolean accept(File file) {
      return file.getName().endsWith(".java");
    }
  };

  /**
   * A file filter for directories.
   */
  private static FileFilter DIR_FILTER = new FileFilter() {
    public boolean accept(File file) {
      return file.isDirectory();
    }
  };

  /**
   * Mechansim for stepping through the Enunciate build process.
   */
  public final class Stepper {

    private final List<DeploymentModule> deploymentModules;
    private Target nextTarget;

    private Stepper() throws EnunciateException, IOException {
      deploymentModules = doInit();
      this.nextTarget = Target.GENERATE;
    }

    /**
     * The next target that is to be executed.
     *
     * @return The next target that is to be executed.
     */
    public Target getNextTarget() {
      return nextTarget;
    }

    /**
     * Steps to the next target in the process.
     */
    public synchronized void step() throws EnunciateException, IOException {
      if (this.nextTarget == null) {
        throw new EnunciateExecutionException("All steps completed.");
      }

      info("\n\nEntering %s step....", this.nextTarget);

      switch (this.nextTarget) {
        case GENERATE:
          doGenerate(this.deploymentModules);
          this.nextTarget = Target.COMPILE;
          break;
        case COMPILE:
          doCompile(this.deploymentModules);
          this.nextTarget = Target.BUILD;
          break;
        case BUILD:
          doBuild(this.deploymentModules);
          this.nextTarget = Target.PACKAGE;
          break;
        case PACKAGE:
          doPackage(this.deploymentModules);
          this.nextTarget = null;
          break;
        default:
          throw new IllegalStateException("Unknown next step: " + this.nextTarget);
      }

    }

    /**
     * Steps to the specified target.
     *
     * @param target The target to step to.
     */
    public synchronized void stepTo(Target target) throws EnunciateException, IOException {
      if (this.nextTarget == null) {
        throw new EnunciateExecutionException("Cannot step: stepper must be initialized.");
      }

      while ((this.nextTarget != null) && (this.nextTarget.ordinal() <= target.ordinal())) {
        step();
      }
    }

    /**
     * Closes the stepper and the underlying enunciate mechanism.
     */
    public synchronized void close() throws EnunciateException, IOException {
      doClose(this.deploymentModules);
    }

  }

}

package net.sf.enunciate.main;

import net.sf.enunciate.EnunciateException;
import net.sf.enunciate.apt.EnunciateAnnotationProcessorFactory;
import net.sf.enunciate.config.EnunciateConfiguration;
import net.sf.enunciate.modules.DeploymentModule;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

  private File configFile;
  private File generateDir;
  private File compileDir;
  private File buildDir;
  private File packageDir;
  private String classpath;
  private EnunciateConfiguration config;
  private Target target = Target.PACKAGE;
  private final HashMap<String, Object> properties = new HashMap<String, Object>();
  private final String[] sourceFiles;

  public static void main(String[] args) {
    //todo: compile the args, set the variables, then:
    //Enunciate enunciate = new Enunciate();
    //enunciate.execute();
  }

  /**
   * Construct an enunciate mechanism on the specified source files.
   *
   * @param sourceFiles The source files.
   */
  public Enunciate(String[] sourceFiles) {
    this.sourceFiles = sourceFiles;
  }

  /**
   * Execute the mechanism.
   */
  public void execute() throws EnunciateException, IOException {
    if (this.config == null) {
      this.config = loadConfig();
    }

    final List<DeploymentModule> deploymentModules = this.config.getEnabledModules();
    int target = getTarget().ordinal();

    for (DeploymentModule deploymentModule : deploymentModules) {
      deploymentModule.init(this);
    }

    if (target >= Target.GENERATE.ordinal()) {
      File genDir = getGenerateDir();
      if (genDir == null) {
        genDir = createTempDir();
        setGenerateDir(genDir);
      }

      invokeApt(getSourceFiles());
    }

    if (target >= Target.COMPILE.ordinal()) {
      File destdir = getCompileDir();
      if (destdir == null) {
        destdir = createTempDir();
        setCompileDir(destdir);
      }

      for (DeploymentModule deploymentModule : deploymentModules) {
        deploymentModule.step(Target.COMPILE);
      }
    }

    if (target >= Target.BUILD.ordinal()) {
      File buildDir = getBuildDir();
      if (buildDir == null) {
        buildDir = createTempDir();
        setBuildDir(buildDir);
      }

      for (DeploymentModule deploymentModule : deploymentModules) {
        deploymentModule.step(Target.BUILD);
      }
    }

    if (target >= Target.PACKAGE.ordinal()) {
      File packageDir = getPackageDir();
      if (packageDir == null) {
        packageDir = createTempDir();
        setPackageDir(packageDir);
      }

      for (DeploymentModule deploymentModule : deploymentModules) {
        deploymentModule.step(Target.PACKAGE);
      }
    }

    for (DeploymentModule deploymentModule : deploymentModules) {
      deploymentModule.close();
    }

    //todo: now export the artifacts as specified on the command line. 
  }

  protected File createTempDir() throws IOException {
    File genDir;
    genDir = File.createTempFile("enunciate", "");
    genDir.delete();
    genDir.mkdirs();
    return genDir;
  }

  /**
   * Get the source files for this enunciate mechanism.
   *
   * @return The source files.
   */
  public String[] getSourceFiles() {
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
    if ((configFile != null) && (configFile.exists())) {
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
    findJavaFiles(basedir, files);
    return files;
  }

  /**
   * Recursively finds all the java files in the specified directory and adds them all to the given collection.
   *
   * @param dir       The directory.
   * @param filenames The collection.
   */
  private void findJavaFiles(File dir, Collection<String> filenames) {
    File[] javaFiles = dir.listFiles(JAVA_FILTER);
    if (javaFiles != null) {
      for (File javaFile : javaFiles) {
        filenames.add(javaFile.getAbsolutePath());
      }
    }

    File[] dirs = dir.listFiles(DIR_FILTER);
    if (dirs != null) {
      for (File subdir : dirs) {
        findJavaFiles(subdir, filenames);
      }
    }
  }

  /**
   * The default classpath to use for javac and apt.
   *
   * @return The default classpath to use for javac and apt.
   */
  public String getDefaultClasspath() {
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
   */
  protected void invokeApt(String[] sourceFiles) throws IOException, EnunciateException {
    ArrayList<String> args = new ArrayList<String>();
    String classpath = getDefaultClasspath();

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
      System.out.println("Invoking APT with arguments: ");
      for (String arg : args) {
        System.out.println(arg);
      }
    }

    EnunciateAnnotationProcessorFactory apf = new EnunciateAnnotationProcessorFactory(this.config);
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
    String classpath = getDefaultClasspath();

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
      System.out.println("Invoking Javac with arguments: ");
      for (String arg : args) {
        System.out.println(arg);
      }
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
    if (!to.exists()) {
      to.mkdirs();
    }

    File[] files = from.listFiles();
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
    if (!to.exists()) {
      to.getParentFile().mkdirs();
    }

    if (isDebug()) {
      System.out.println("Copying " + from + " to " + to);
    }

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
    InputStream stream = getClass().getResourceAsStream(resource);
    if (stream == null) {
      throw new IOException("Request to copy a resource that was not found: " + resource);
    }

    if (isDebug()) {
      System.out.println("Copying resource " + resource + " to " + to);
    }

    FileOutputStream out = new FileOutputStream(to);
    byte[] buffer = new byte[1024 * 2]; //2 kb buffer should suffice.
    int len;
    while ((len = stream.read(buffer)) > 0) {
      out.write(buffer, 0, len);
    }
  }

  /**
   * zip up a directory to a specified zip file.
   *
   * @param dir    The directory to zip up.
   * @param toFile The file to zip to.
   */
  public void zip(File dir, File toFile) throws IOException {
    if (!toFile.getParentFile().exists()) {
      toFile.getParentFile().mkdirs();
    }

    ArrayList<File> files = new ArrayList<File>();
    buildFileList(dir, files);

    byte[] buffer = new byte[2 * 1024]; //buffer of 2K should be fine.
    URI baseURI = dir.toURI();
    ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(toFile));
    for (File file : files) {
      ZipEntry entry = new ZipEntry(baseURI.relativize(file.toURI()).getPath());

      if (isDebug()) {
        System.out.println("Adding entry " + entry.getName());
      }

      zipout.putNextEntry(entry);

      FileInputStream in = new FileInputStream(file);
      int len;
      while ((len = in.read(buffer)) > 0) {
        zipout.write(buffer, 0, len);
      }

      // Complete the entry
      zipout.closeEntry();
      in.close();
    }

    zipout.close();
  }

  /**
   * Adds all files in a specified directory to a list.
   *
   * @param dir  The directory.
   * @param list The list.
   */
  protected void buildFileList(File dir, List<File> list) {
    for (File file : dir.listFiles()) {
      if (file.isDirectory()) {
        buildFileList(file, list);
      }
      else {
        list.add(file);
      }
    }
  }

  /**
   * Whether to be verbose.
   *
   * @return Whether to be verbose.
   */
  public boolean isVerbose() {
    return verbose;
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

}

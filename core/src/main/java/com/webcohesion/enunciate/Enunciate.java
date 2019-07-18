/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

import com.sun.tools.javac.api.JavacTool;
import com.webcohesion.enunciate.api.AggregatedApiRegistry;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.artifacts.Artifact;
import com.webcohesion.enunciate.module.ApiRegistryAwareModule;
import com.webcohesion.enunciate.module.DependencySpec;
import com.webcohesion.enunciate.module.DependingModuleAwareModule;
import com.webcohesion.enunciate.module.EnunciateModule;
import org.apache.commons.configuration.ConfigurationException;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;


/**
 * @author Ryan Heaton
 */
public class Enunciate implements Runnable {

  private static final String DUPLICATE_CLASS_ERROR_MESSAGE_ENGLISH = "file does not contain class";

  private Set<File> sourceFiles = new TreeSet<File>();
  private List<EnunciateModule> modules;
  private final Set<String> includePatterns = new TreeSet<String>();
  private final Set<String> excludePatterns = new TreeSet<String>();
  private List<File> classpath = null;
  private List<File> sourcepath = null;
  private EnunciateLogger logger = new EnunciateConsoleLogger();
  private final EnunciateConfiguration configuration = new EnunciateConfiguration();
  private File buildDir;
  private final List<String> compilerArgs = new ArrayList<String>();
  private final Set<Artifact> artifacts = new TreeSet<Artifact>();
  private final Map<String, File> exports = new HashMap<String, File>();
  private final ApiRegistry apiRegistry = new AggregatedApiRegistry(this);

  public List<EnunciateModule> getModules() {
    return modules;
  }

  public Enunciate setModules(List<EnunciateModule> modules) {
    this.modules = null;
    if (modules != null) {
      for (EnunciateModule module : modules) {
        addModule(module);
      }
    }
    return this;
  }

  public Enunciate addModule(EnunciateModule module) {
    if (this.modules == null) {
      this.modules = new ArrayList<EnunciateModule>();
    }

    module.init(this);
    this.modules.add(module);
    return this;
  }

  public Enunciate loadDiscoveredModules() {
    ServiceLoader<EnunciateModule> moduleLoader = ServiceLoader.load(EnunciateModule.class);
    for (EnunciateModule module : moduleLoader) {
      addModule(module);
    }
    return this;
  }

  public Enunciate setSourceFiles(Set<File> sourceFiles) {
    this.sourceFiles = sourceFiles;
    return this;
  }

  public Enunciate addSourceFile(File source) {
    if (this.sourceFiles == null) {
      this.sourceFiles = new HashSet<File>();
    }
    this.sourceFiles.add(source);
    return this;
  }

  public Enunciate addSourceDir(File dir) {
    visitFiles(dir, JAVA_FILTER, new FileVisitor() {
      @Override
      public void visit(File file) {
        addSourceFile(file);
      }
    });

    return this;
  }

  public Set<String> getIncludePatterns() {
    TreeSet<String> includeClasses = new TreeSet<String>(this.includePatterns);
    includeClasses.addAll(this.configuration.getApiIncludeClasses());
    return includeClasses;
  }

  public Enunciate addInclude(String include) {
    this.includePatterns.add(include);
    return this;
  }

  public Set<String> getExcludePatterns() {
    TreeSet<String> excludeClasses = new TreeSet<String>(this.excludePatterns);
    excludeClasses.addAll(this.configuration.getApiExcludeClasses());
    return excludeClasses;
  }

  public Enunciate addExclude(String exclude) {
    this.excludePatterns.add(exclude);
    return this;
  }

  public List<File> getClasspath() {
    return classpath;
  }

  public Enunciate setClasspath(List<File> classpath) {
    this.classpath = classpath;
    return this;
  }

  public List<File> getSourcepath() {
    return sourcepath;
  }

  public Enunciate setSourcepath(List<File> sourcepath) {
    this.sourcepath = sourcepath;
    return this;
  }

  public EnunciateLogger getLogger() {
    return logger;
  }

  public Enunciate setLogger(EnunciateLogger logger) {
    this.logger = logger;
    return this;
  }

  public EnunciateConfiguration getConfiguration() {
    return configuration;
  }

  public Enunciate loadConfiguration(InputStream xml) {
    InputStreamReader reader;
    try {
      reader = new InputStreamReader(xml, "utf-8");
    } catch (UnsupportedEncodingException e) {
      throw new EnunciateException(e);
    }

    return loadConfiguration(reader);
  }

  public Enunciate loadConfiguration(Reader reader) {
    try {
      this.configuration.getSource().load(reader);
    } catch (ConfigurationException e) {
      throw new EnunciateException(e);
    }

    return this;
  }

  public Enunciate loadConfiguration(URL url) {
    try {
      return loadConfiguration(url.openStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Enunciate loadConfiguration(File xml) {
    try {
      this.configuration.setConfigFile(xml);
      loadConfiguration(xml.toURI().toURL());
      return this;
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public File getBuildDir() {
    return buildDir;
  }

  public Enunciate setBuildDir(File buildDir) {
    this.buildDir = buildDir;
    return this;
  }

  public List<String> getCompilerArgs() {
    return compilerArgs;
  }

  public Map<String, File> getExports() {
    return exports;
  }

  public Enunciate addExport(String id, File target) {
    this.exports.put(id, target);
    return this;
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
        if (artifactId.equals(artifact.getId()) || artifact.getAliases().contains(artifactId)) {
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
    return this.artifacts.add(artifact);
  }

  /**
   * The API registry for the engine.
   *
   * @return The API registry for the engine.
   */
  public ApiRegistry getApiRegistry() {
    return apiRegistry;
  }

  /**
   * Creates a temporary directory.
   *
   * @return A temporary directory.
   */
  public File createTempDir() throws IOException {
    final Double random = Math.random() * 10000; //this random name is applied to avoid an "access denied" error on windows.
    File scratchDir = this.buildDir;
    if (scratchDir != null && !scratchDir.exists()) {
      scratchDir.mkdirs();
    }

    final File tempDir = File.createTempFile("enunciate" + random.intValue(), "", scratchDir);
    tempDir.delete();
    tempDir.mkdirs();

    getLogger().debug("Created directory %s", tempDir);

    return tempDir;
  }

  /**
   * Creates a temporary file. Same as {@link File#createTempFile(String, String)} but in the Enunciate scratch directory.
   *
   * @param baseName The base name of the file.
   * @param suffix   The suffix.
   * @return The temp file.
   */
  public File createTempFile(String baseName, String suffix) throws IOException {
    final Double random = Math.random() * 10000; //this random name is applied to avoid an "access denied" error on windows.
    File scratchDir = this.buildDir;
    if (scratchDir != null && !scratchDir.exists()) {
      scratchDir.mkdirs();
    }

    return File.createTempFile(baseName + random.intValue(), suffix, scratchDir);
  }

  /**
   * Copy an entire directory from one place to another.
   *
   * @param from     The source directory.
   * @param to       The destination directory.
   * @param excludes The files to exclude from the copy
   */
  public void copyDir(File from, File to, File... excludes) throws IOException {
    if (from != null && from.exists()) {
      File[] files = from.listFiles();

      if (!to.exists()) {
        to.mkdirs();
      }

      COPY_LOOP:
      for (File file : files) {
        if (excludes != null) {
          for (File exclude : excludes) {
            if (file.equals(exclude)) {
              continue COPY_LOOP;
            }
          }
        }

        if (file.isDirectory()) {
          copyDir(file, new File(to, file.getName()));
        } else {
          copyFile(file, new File(to, file.getName()));
        }
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
    to = to.getAbsoluteFile();
    if ((!to.exists()) && (to.getParentFile() != null)) {
      to.getParentFile().mkdirs();
    }

    getLogger().debug("Copying %s to %s ", from, to);
    FileChannel dstChannel = new FileOutputStream(to, false).getChannel();
    dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
    srcChannel.close();
    dstChannel.close();
  }

  /**
   * zip up directories to a specified zip file.
   *
   * @param toFile The file to zip to.
   * @param dirs   The directories to zip up.
   */
  public boolean zip(File toFile, File... dirs) throws IOException {
    if (!toFile.getParentFile().exists()) {
      getLogger().debug("Creating directory %s...", toFile.getParentFile());
      toFile.getParentFile().mkdirs();
    }

    boolean anyFiles = false;

    byte[] buffer = new byte[2 * 1024]; //buffer of 2K should be fine.
    ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(toFile));
    for (File dir : dirs) {

      URI baseURI = dir.toURI();
      getLogger().debug("Adding contents of directory %s to zip file %s...", dir, toFile);
      ArrayList<File> files = new ArrayList<File>();
      buildFileList(files, dir);
      for (File file : files) {
        ZipEntry entry = new ZipEntry(baseURI.relativize(file.toURI()).getPath());
        getLogger().debug("Adding entry %s...", entry.getName());
        zipout.putNextEntry(entry);

        if (!file.isDirectory()) {
          anyFiles = true;
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

    if (!anyFiles) {
      ZipEntry entry = new ZipEntry("README.txt");
      zipout.putNextEntry(entry);
      zipout.write("Empty zip file".getBytes());
      zipout.closeEntry();
    }

    zipout.close();

    return anyFiles;
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
        if (file.isDirectory()) {
          buildFileList(list, file);
        } else {
          list.add(file);
        }
      }
    }
  }

  /**
   * Extracts the (zipped up) base to the specified directory.
   *
   * @param stream The stream to the zip.
   * @param toDir  The directory to extract to.
   */
  public void unzip(InputStream stream, File toDir) throws IOException {
    ZipInputStream in = new ZipInputStream(stream);
    ZipEntry entry = in.getNextEntry();
    while (entry != null) {
      File file = new File(toDir, entry.getName());
      getLogger().debug("Extracting %s to %s.", entry.getName(), file);
      if (entry.isDirectory()) {
        file.mkdirs();
      } else {
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
   * Copies a resource to a file.
   *
   * @param url The url of the resource.
   * @param to  The file to copy to.
   */
  public void copyResource(URL url, File to) throws IOException {
    InputStream stream = url.openStream();

    getLogger().debug("Copying resource %s to %s...", url, to);
    FileOutputStream out = new FileOutputStream(to);
    byte[] buffer = new byte[1024 * 2]; //2 kb buffer should suffice.
    int len;
    while ((len = stream.read(buffer)) > 0) {
      out.write(buffer, 0, len);
    }
  }

  @Override
  public void run() {
    if (this.modules != null && !this.modules.isEmpty()) {
      //scan for any included types.
      List<File> classpath = this.classpath == null ? new ArrayList<File>() : this.classpath;
      List<File> sourcepath = this.sourcepath == null ? new ArrayList<File>() : this.sourcepath;

      List<URL> scanpath = new ArrayList<URL>(classpath.size() + sourcepath.size());
      for (File entry : classpath) {
        try {
          scanpath.add(entry.toURI().toURL());
        } catch (MalformedURLException e) {
          throw new EnunciateException(e);
        }
      }

      for (File entry : sourcepath) {
        try {
          scanpath.add(entry.toURI().toURL());
        } catch (MalformedURLException e) {
          throw new EnunciateException(e);
        }
      }

      Reflections reflections = loadApiReflections(scanpath);
      Set<String> scannedEntries = reflections.getStore().get(EnunciateReflectionsScanner.class.getSimpleName()).keySet();
      Set<String> includedTypes = new HashSet<String>();
      Set<String> scannedSourceFiles = new HashSet<String>();
      for (String entry : scannedEntries) {
        int innerClassSeparatorIndex = entry.lastIndexOf('$');
        if (innerClassSeparatorIndex > 0) { //inner class; convert the name to its "canonical" name.
          String simpleName = entry.substring(innerClassSeparatorIndex + 1);
          if (!Character.isDigit(simpleName.charAt(0))) {
            //if the inner class isn't an anonymous inner class, add it to the included types, too.
            String innerClass = entry.replace('$', '.');
            includedTypes.add(innerClass);
          }

          String outerClass = entry.substring(0, entry.indexOf('$'));
          includedTypes.add(outerClass);
        } else if (entry.endsWith(".java")) { //java source file; add it to the scanned source files.
          scannedSourceFiles.add(entry);
        } else if (!entry.endsWith("package-info")) { //if it's not a package-info file, it should be a standard java class.
          includedTypes.add(entry);
        }
      }

      //only include the source files of the types that are on the classpath.
      Iterator<String> sourceFilesIt = scannedSourceFiles.iterator();
      while (sourceFilesIt.hasNext()) {
        String sourceFile = sourceFilesIt.next();
        String typeName = sourceFile.substring(0, sourceFile.length() - 5).replace('/', '.');
        if (!includedTypes.contains(typeName)) {
          sourceFilesIt.remove();
        }
      }

      getLogger().debug("Detected API Types: %s", new EnunciateLogger.ListWriter(includedTypes));

      //gather all the java source files.
      List<URL> sourceFiles = getSourceFileURLs();
      URLClassLoader apiClassLoader = new URLClassLoader(scanpath.toArray(new URL[scanpath.size()]));
      for (String javaFile : scannedSourceFiles) {

        Enumeration<URL> resources;
        try {
          resources = apiClassLoader.findResources(javaFile);
        } catch (IOException e) {
          getLogger().debug("Unable to load java source file %s: %s", javaFile, e.getMessage());
          continue;
        }

        if (!resources.hasMoreElements()) {
          getLogger().debug("Unable to find java source file %s on the classpath.", javaFile);
        } else {
          URL resource = resources.nextElement();
          if (!resources.hasMoreElements()) {
            sourceFiles.add(resource);
          } else {
            StringBuilder locations = new StringBuilder("[").append(resource.toString());
            while (resources.hasMoreElements()) {
              resource = resources.nextElement();
              locations.append(", ").append(resource);
            }
            getLogger().warn("Java source file %s will not be included on the classpath because it is found in multiple locations: %s", javaFile, locations);
          }
        }
      }

      if (sourceFiles.isEmpty()) {
        //Java compiler needs _something_ to compile, so we'll provide an dummy class.
        sourceFiles.add(Enunciate.class.getResource("/com/webcohesion/enunciate/Nothing.java"));
      }

      //invoke the processor.
      List<String> options = new ArrayList<String>();

      options.add("-proc:only"); // don't compile the classes; only run the annotation processing engine.

      options.add("-implicit:none"); // don't generate class files for implicit classes

      options.addAll(Arrays.asList("-processorpath", "")); // set the processor path to empty so the engine won't automatically find annotation processors

      String cp = writeClasspath(classpath);
      getLogger().debug("Compiler classpath: %s", new EnunciateLogger.ListWriter(classpath));
      options.addAll(Arrays.asList("-classpath", cp));

      String sp = writeClasspath(sourcepath);
      getLogger().debug("Compiler sourcepath: %s", new EnunciateLogger.ListWriter(sourcepath));
      options.addAll(Arrays.asList("-sourcepath", sp));

      List<String> compilerArgs = getCompilerArgs();
      getLogger().debug("Compiler args: %s", compilerArgs);
      options.addAll(compilerArgs);

      getLogger().debug("Compiler sources: %s", new EnunciateLogger.ListWriter(sourceFiles));
      List<JavaFileObject> sources = new ArrayList<JavaFileObject>(sourceFiles.size());
      String encoding = findEncoding(compilerArgs);
      for (URL sourceFile : sourceFiles) {
        sources.add(new URLFileObject(sourceFile, encoding));
      }

      JavaCompiler compiler = JavacTool.create();
      StringWriter compilerOutput = new StringWriter();
      DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
      JavaCompiler.CompilationTask task = compiler.getTask(compilerOutput, null, diagnostics, options, null, sources);
      EnunciateAnnotationProcessor processor = new EnunciateAnnotationProcessor(this, includedTypes);
      task.setProcessors(Collections.singletonList(processor));
      Boolean javacSuccess = task.call();
      if (!javacSuccess || !processor.processed) {
        String outputText = compilerOutput.toString();
        try {
          if (!outputText.isEmpty()) {
            BufferedReader reader = new BufferedReader(new StringReader(outputText));
            String line = reader.readLine();
            while (line != null) {
              getLogger().warn("[javac] %s", line);
              line = reader.readLine();
            }
          }
        } catch (IOException e) {
          //fall through...
        }

        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
          BufferedReader message = new BufferedReader(new StringReader(diagnostic.toString()));
          try {
            String line = message.readLine();
            boolean duplicateClassErrorDetected = line != null && line.contains(DUPLICATE_CLASS_ERROR_MESSAGE_ENGLISH);
            getLogger().warn("[javac] [%s] %s:%s:%s %s", diagnostic.getKind(), diagnostic.getSource(), diagnostic.getLineNumber(), diagnostic.getColumnNumber(), line == null ? "" : line);
            while (line != null) {
              getLogger().warn("[javac] %s", line);
              line = message.readLine();
            }
            if (duplicateClassErrorDetected) {
              getLogger().warn("");
              getLogger().warn("It appears you ran into the infamous \"duplicate class\" bug in the Java compiler. Bummer.");
              getLogger().warn("This usually happens when you've got two source files on your source path that declare types of the same name, even if they're in different packages.");
              getLogger().warn("This a bug in the Java compiler, and is being tracked at https://github.com/stoicflame/enunciate/issues/117.");
              getLogger().warn("The only known workaround is to exclude the offending source jar(s) from your source path.");
              getLogger().warn("If you're using Maven, you can do so using the 'sourcepathExcludes' configuration element as described at https://github.com/stoicflame/enunciate/wiki/Multi-Module-Projects.");
              getLogger().warn("");
            }
          } catch (IOException e) {
            getLogger().warn("[javac] %s", diagnostic);
          }
        }


        if (javacSuccess && !processor.processed) {
          getLogger().error("");
          getLogger().error("The Java compiler has crashed! This is likely due to some anomalies in your classpath or your source path (e.g. duplicate source files for the same class). The fact that there isn't more information available is a bug in the Java compiler.");
          getLogger().error("");
          getLogger().error("Please see https://github.com/stoicflame/enunciate/wiki/Java-Compiler-Crash for some tips on what to do about this.");
          getLogger().error("");

        }

        throw new EnunciateException("Enunciate compile failed.");
      }

      getLogger().debug("[javac] %s", compilerOutput);
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
        getLogger().debug("[javac] [%s] %s:%s:%s %s", diagnostic.getKind(), diagnostic.getSource(), diagnostic.getLineNumber(), diagnostic.getColumnNumber(), diagnostic);
      }

      HashSet<String> exportedArtifacts = new HashSet<String>();
      for (Artifact artifact : artifacts) {
        String artifactId = artifact.getId();
        Map.Entry<String, File> export = null;
        for (Map.Entry<String, File> entry : this.exports.entrySet()) {
          if (artifactId.equals(entry.getKey()) || artifact.getAliases().contains(entry.getKey())) {
            export = entry;
          }
        }

        if (export != null) {
          File dest = export.getValue();
          getLogger().debug("Exporting artifact %s to %s.", export.getKey(), dest);
          try {
            artifact.exportTo(dest, this);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          exportedArtifacts.add(export.getKey());
        }
      }

      for (String export : this.exports.keySet()) {
        if (!exportedArtifacts.remove(export)) {
          getLogger().warn("Unknown artifact '%s'.  Artifact will not be exported.", export);
        }
      }
    } else {
      this.logger.warn("No Enunciate modules have been loaded. No work was done.");
    }
  }

  private String findEncoding(List<String> compilerArgs) {
    for (int i = 0; i < compilerArgs.size(); i++) {
      String arg = compilerArgs.get(i);
      if ("-encoding".equals(arg) && i + 1 < compilerArgs.size()) {
        return compilerArgs.get(i + 1);
      }
    }

    return "utf-8"; //default encoding
  }

  public String writeClasspath(List<File> cp) {
    StringBuilder builder = new StringBuilder();
    Iterator<File> it = cp.iterator();
    while (it.hasNext()) {
      File next = it.next();
      builder.append(next.getAbsolutePath());
      if (it.hasNext()) {
        builder.append(File.pathSeparatorChar);
      }
    }
    return builder.toString();
  }

  protected List<URL> getSourceFileURLs() {
    List<URL> sourceFiles = new ArrayList<URL>(this.sourceFiles.size());
    for (File sourceFile : this.sourceFiles) {
      try {
        sourceFiles.add(sourceFile.toURI().toURL());
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return sourceFiles;
  }

  protected Reflections loadApiReflections(List<URL> classpath) {
    ConfigurationBuilder reflectionSpec = new ConfigurationBuilder()
        .setUrls(classpath)
        .setScanners(new EnunciateReflectionsScanner(this, getModules()));

    return new Reflections(reflectionSpec);
  }

  public void visitFiles(File dir, FileFilter filter, FileVisitor visitor) {
    File[] files = dir.listFiles(filter);
    if (files != null) {
      for (File file : files) {
        visitor.visit(file);
      }
    }

    File[] dirs = dir.listFiles(DIR_FILTER);
    if (dirs != null) {
      for (File subdir : dirs) {
        visitFiles(subdir, filter, visitor);
      }
    }
  }

  protected Map<String, ? extends EnunciateModule> findEnabledModules() {
    TreeMap<String, EnunciateModule> enabledModules = new TreeMap<String, EnunciateModule>();
    for (EnunciateModule module : this.modules) {
      if (module.isEnabled()) {
        enabledModules.put(module.getName(), module);
      }
    }
    return enabledModules;
  }

  protected Graph<String, DefaultEdge> buildModuleGraph(Map<String, ? extends EnunciateModule> modules) {
    Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
    for (String moduleName : modules.keySet()) {
      graph.addVertex(moduleName);
    }

    for (EnunciateModule module : modules.values()) {
      List<DependencySpec> dependencies = module.getDependencySpecifications();
      if (dependencies != null && !dependencies.isEmpty()) {
        for (DependencySpec dependency : dependencies) {
          for (EnunciateModule other : modules.values()) {
            if (dependency.accept(other)) {
              graph.addEdge(other.getName(), module.getName());
            }
          }

          if (!dependency.isFulfilled()) {
            throw new EnunciateException(String.format("Unfulfilled dependency %s of module %s.", dependency.toString(), module.getName()));
          }
        }
      }
    }

    for (EnunciateModule module : modules.values()) {
      if (module instanceof DependingModuleAwareModule) {
        Set<DefaultEdge> edges = graph.outgoingEdgesOf(module.getName());
        Set<String> dependingModules = new TreeSet<String>();
        for (DefaultEdge edge : edges) {
          dependingModules.add(graph.getEdgeTarget(edge));
        }
        ((DependingModuleAwareModule) module).acknowledgeDependingModules(dependingModules);
      }

      if (module instanceof ApiRegistryAwareModule) {
        ((ApiRegistryAwareModule) module).setApiRegistry(this.apiRegistry);
      }
    }

    CycleDetector<String, DefaultEdge> cycleDetector = new CycleDetector<String, DefaultEdge>(graph);
    Set<String> modulesInACycle = cycleDetector.findCycles();
    if (!modulesInACycle.isEmpty()) {
      StringBuilder errorMessage = new StringBuilder("Module cycle detected: ");
      java.util.Iterator<String> subcycle = cycleDetector.findCyclesContainingVertex(modulesInACycle.iterator().next()).iterator();
      while (subcycle.hasNext()) {
        String next = subcycle.next();
        errorMessage.append(next);
        if (subcycle.hasNext()) {
          errorMessage.append(" --> ");
        }
      }

      throw new EnunciateException(errorMessage.toString());
    }

    return graph;
  }

  protected void invokeModules(EnunciateContext context, Map<String, ? extends EnunciateModule> modules, Graph<String, DefaultEdge> graph) {
    Set<String> invoked = new TreeSet<>();
    TopologicalOrderIterator<String, DefaultEdge> graphIt = new TopologicalOrderIterator<>(graph);
    while (graphIt.hasNext()) {
      String module = graphIt.next();
      if (!invoked.contains(module)) {
        modules.get(module).call(context);
        invoked.add(module);
      }
    }
  }

  /**
   * A file filter for java files.
   */
  public static FileFilter JAVA_FILTER = new FileFilter() {
    public boolean accept(File file) {
      return file.getName().endsWith(".java");
    }
  };

  /**
   * A file filter for directories.
   */
  public static FileFilter DIR_FILTER = new FileFilter() {
    public boolean accept(File file) {
      return file.isDirectory();
    }
  };

  /**
   * File visitor interface used to visit files.
   */
  public interface FileVisitor {

    void visit(File file);
  }

  public static class URLFileObject extends SimpleJavaFileObject {

    private final URL source;
    private final String encoding;

    public URLFileObject(URL source, String encoding) {
      super(toURI(source), Kind.SOURCE);
      this.source = source;
      this.encoding = encoding;
    }

    static URI toURI(URL source) {
      try {
        if ("jar".equals(source.getProtocol())) {
          return new URI(source.toString().replace("jar:file:", "file:"));
        } else {
          return source.toURI();
        }
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public InputStream openInputStream() throws IOException {
      return this.source.openStream();
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
      StringBuilder content = new StringBuilder();
      InputStream in = openInputStream();
      try {
        byte[] bytes = new byte[2 * 1024];
        int len;
        while ((len = in.read(bytes)) >= 0) {
          content.append(new String(bytes, 0, len, this.encoding));
        }
        return content;
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          //fall through...
        }
      }
    }
  }

}

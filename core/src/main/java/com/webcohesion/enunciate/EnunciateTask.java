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

package com.webcohesion.enunciate;

import com.webcohesion.enunciate.module.EnunciateModule;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.filters.ExpandProperties;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Ant task for enunciate.
 *
 * @author Ryan Heaton
 */
public class EnunciateTask extends MatchingTask {

  private boolean compileDebugInfo = true;
  private String encoding;
  private File configFile;
  private File basedir;
  private Path classpath;
  private File buildDir;
  private String javacSourceVersion = null;
  private String javacTargetVersion = null;
  private final ArrayList<Export> exports = new ArrayList<Export>();
  private final ArrayList<JavacArgument> javacArguments = new ArrayList<JavacArgument>();

  /**
   * Executes the enunciate task.
   */
  @Override
  public void execute() throws BuildException {
    if (basedir == null) {
      throw new BuildException("A base directory must be specified.");
    }

    try {
      Enunciate enunciate = new Enunciate();

      //set up the logger.
      enunciate.setLogger(new AntEnunciateLogger());

      //set the build dir.
      enunciate.setBuildDir(this.buildDir);

      //add the source files.
      DirectoryScanner scanner = getDirectoryScanner(basedir);
      scanner.scan();
      Set<File> sourceFiles = new TreeSet<File>();
      for (String file : scanner.getIncludedFiles()) {
        sourceFiles.add(new File(basedir, file));
      }
      enunciate.setSourceFiles(sourceFiles);

      //load the config.
      if (this.configFile != null && this.configFile.exists()) {
        getProject().log("[ENUNCIATE] Using enunciate configuration at " + this.configFile.getAbsolutePath());
        ExpandProperties reader = new ExpandProperties(new FileReader(this.configFile));
        reader.setProject(getProject());
        enunciate.loadConfiguration(reader);
        enunciate.getConfiguration().setBase(this.configFile.getParentFile());
      }

      if (classpath != null) {
        String[] filenames = this.classpath.list();
        List<File> cp = new ArrayList<File>(filenames.length);
        for (String filename : filenames) {
          File file = new File(filename);
          if (file.exists()) {
            cp.add(file);
          }
        }
        enunciate.setClasspath(cp);

        //set up the classloader for the Enunciate invocation.
        AntClassLoader loader = new AntClassLoader(Enunciate.class.getClassLoader(), getProject(), this.classpath, true);
        Thread.currentThread().setContextClassLoader(loader);

        ServiceLoader<EnunciateModule> moduleLoader = ServiceLoader.load(EnunciateModule.class, loader);
        for (EnunciateModule module : moduleLoader) {
          enunciate.addModule(module);
        }
      }

      if (this.buildDir != null) {
        enunciate.setBuildDir(this.buildDir);
      }

      List<String> compilerArgs = new ArrayList<String>();
      String sourceVersion = this.javacSourceVersion;
      if (sourceVersion != null) {
        compilerArgs.add("-source");
        compilerArgs.add(sourceVersion);
      }

      String targetVersion = this.javacTargetVersion;
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

      for (JavacArgument javacArgument : this.javacArguments) {
        enunciate.getCompilerArgs().add(javacArgument.getArgument());
      }

      for (Export export : exports) {
        enunciate.addExport(export.getArtifactId(), export.getDestination());
      }

      enunciate.run();
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
    catch (EnunciateException e) {
      throw new BuildException(e);
    }
  }
  
  public void setEncoding(String encoding) {
	  this.encoding = encoding;
  }

  /**
   * The base directory for the source files.
   *
   * @param basedir The base directory for the source files.
   */
  public void setBasedir(File basedir) {
    this.basedir = basedir;
  }

  /**
   * The base directory for the source files.
   *
   * @param basedir The base directory for the source files.
   */
  public void setDir(File basedir) {
    setBasedir(basedir);
  }

  /**
   * The build directory.
   *
   * @param buildDir The build directory.
   */
  public void setBuildDir(File buildDir) {
    this.buildDir = buildDir;
  }

  /**
   * The enunciate config file.
   *
   * @param config The enunciate config file.
   */
  public void setConfigFile(File config) {
    this.configFile = config;
  }

  /**
   * Whether to compile with debug info.
   *
   * @return Whether to compile with debug info.
   */
  public boolean isCompileDebugInfo() {
    return compileDebugInfo;
  }

  /**
   * Whether to compile with debug info.
   *
   * @param compileDebugInfo Whether to compile with debug info.
   */
  public void setCompileDebugInfo(boolean compileDebugInfo) {
    this.compileDebugInfo = compileDebugInfo;
  }

  /**
   * javac -source version parameter
   * 
   * @param javacSourceVersion javac -source version parameter
   */
  public void setJavacSourceVersion(String javacSourceVersion) {
    this.javacSourceVersion = javacSourceVersion;
  }

  /**
   * javac -target version parameter
   * 
   * @param javacTargetVersion javac -target version parameter
   */
  public void setJavacTargetVersion(String javacTargetVersion) {
    this.javacTargetVersion = javacTargetVersion;
  }

  /**
   * The classpath to use to enunciate.
   *
   * @param classpath The classpath to use to enunciate.
   */
  public void setClasspath(Path classpath) {
    if (this.classpath == null) {
      this.classpath = classpath;
    }
    else {
      this.classpath.append(classpath);
    }
  }

  /**
   * The classpath to use to enunciate.
   *
   * @return The classpath to use to enunciate.
   */
  public Path getClasspath() {
    return classpath;
  }

  /**
   * Adds a path to the classpath.
   *
   * @return The path.
   */
  public Path createClasspath() {
    if (classpath == null) {
      classpath = new Path(getProject());
    }
    return classpath.createPath();
  }

  /**
   * Adds a reference to a classpath defined elsewhere.
   *
   * @param ref a reference to a classpath.
   */
  public void setClasspathRef(Reference ref) {
    createClasspath().setRefid(ref);
  }

  /**
   * Creates a nested export task.
   *
   * @return the nested export task.
   */
  public Export createExport() {
    Export export = new Export();
    this.exports.add(export);
    return export;
  }

  /**
   * Creates a nested javac argument.
   *
   * @return the nested javac argument.
   */
  public JavacArgument createJavacArgument() {
    JavacArgument export = new JavacArgument();
    this.javacArguments.add(export);
    return export;
  }

  /**
   * A nested export task.
   */
  public static class Export {

    private String artifactId;
    private File destination;

    /**
     * The id of the artifact to export.
     *
     * @return The id of the artifact to export.
     */
    public String getArtifactId() {
      return artifactId;
    }

    /**
     * The id of the artifact to export.
     *
     * @param artifactId The id of the artifact to export.
     */
    public void setArtifactId(String artifactId) {
      this.artifactId = artifactId;
    }

    /**
     * The destination (file or directory) of the export.
     *
     * @return The destination (file or directory) of the export.
     */
    public File getDestination() {
      return destination;
    }

    /**
     * The destination (file or directory) of the export.
     *
     * @param destination The destination (file or directory) of the export.
     */
    public void setDestination(File destination) {
      this.destination = destination;
    }
  }

  /**
   * A nested javac argument task.
   */
  public static class JavacArgument {

    private String argument;

    /**
     * The argument.
     *
     * @return The argument
     */
    public String getArgument() {
      return argument;
    }

    /**
     * The argument.
     *
     * @param argument The javac argument.
     */
    public void setArgument(String argument) {
      this.argument = argument;
    }
  }

  private class AntEnunciateLogger implements EnunciateLogger {

    @Override
    public void debug(String message, Object... formatArgs) {
      getProject().log(String.format(message, formatArgs), Project.MSG_VERBOSE);
    }

    @Override
    public void info(String message, Object... formatArgs) {
      getProject().log(String.format(message, formatArgs), Project.MSG_INFO);
    }

    @Override
    public void warn(String message, Object... formatArgs) {
      getProject().log(String.format(message, formatArgs), Project.MSG_WARN);
    }
  }
}

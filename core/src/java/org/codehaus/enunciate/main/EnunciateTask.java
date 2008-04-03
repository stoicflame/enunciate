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

package org.codehaus.enunciate.main;

import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.xml.sax.SAXException;
import sun.misc.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Ant task for enunciate.
 *
 * @author Ryan Heaton
 */
public class EnunciateTask extends MatchingTask {

  private boolean verbose = false;
  private boolean debug = false;
  private File configFile;
  private File basedir;
  private Path classpath;
  private File generateDir;
  private File compileDir;
  private File buildDir;
  private File packageDir;
  private File gwtHome;
  private File flexHome;
  private Enunciate.Target target;
  private final ArrayList<Export> exports = new ArrayList<Export>();

  /**
   * Executes the enunciate task.
   */
  @Override
  public void execute() throws BuildException {
    if (basedir == null) {
      throw new BuildException("A base directory must be specified.");
    }

    if (gwtHome != null) {
      System.setProperty("gwt.home", this.gwtHome.getAbsolutePath());
    }

    if (flexHome != null) {
      System.setProperty("flex.home", this.flexHome.getAbsolutePath());
    }

    DirectoryScanner scanner = getDirectoryScanner(basedir);
    scanner.scan();
    String[] files = scanner.getIncludedFiles();
    for (int i = 0; i < files.length; i++) {
      File file = new File(basedir, files[i]);
      files[i] = file.getAbsolutePath();
    }

    try {
      Enunciate proxy = new AntLoggingEnunciate(files);
      EnunciateConfiguration config;

      if (classpath != null) {
        proxy.setClasspath(classpath.toString());

        //if the classpath is set, we need to load the modules using Ant's classloader, or it won't work.  Not totally sure why, though....
        AntClassLoader loader = new AntClassLoader(Enunciate.class.getClassLoader(), getProject(), this.classpath, true);
        ArrayList<DeploymentModule> modules = new ArrayList<DeploymentModule>();
        Iterator discoveredModules = Service.providers(DeploymentModule.class, loader);
        getProject().log("Loading modules from the specified classpath....");
        while (discoveredModules.hasNext()) {
          DeploymentModule discoveredModule = (DeploymentModule) discoveredModules.next();
          getProject().log("Discovered module " + discoveredModule.getName());
          modules.add(discoveredModule);
        }
        config = new EnunciateConfiguration(modules);
      }
      else {
        config = new EnunciateConfiguration();
      }

      proxy.setConfig(config);

      if (this.configFile != null) {
        getProject().log("Loading config " + this.configFile);
        config.load(this.configFile);
        proxy.setConfigFile(this.configFile);
      }

      if (this.generateDir != null) {
        proxy.setGenerateDir(this.generateDir);
      }

      if (this.compileDir != null) {
        proxy.setCompileDir(this.compileDir);
      }

      if (this.buildDir != null) {
        proxy.setBuildDir(this.buildDir);
      }

      if (this.packageDir != null) {
        proxy.setPackageDir(this.packageDir);
      }

      if (this.target != null) {
        proxy.setTarget(this.target);
      }

      for (Export export : exports) {
        proxy.addExport(export.getArtifactId(), export.getDestination());
      }

      proxy.setVerbose(verbose);
      proxy.setDebug(debug);
      proxy.execute();
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
    catch (EnunciateException e) {
      throw new BuildException(e);
    }
    catch (SAXException e) {
      throw new BuildException(e);
    }
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
   * The generate directory.
   *
   * @param generateDir The generate directory.
   */
  public void setGenerateDir(File generateDir) {
    this.generateDir = generateDir;
  }

  /**
   * The compile directory.
   *
   * @param compileDir The compile directory.
   */
  public void setCompileDir(File compileDir) {
    this.compileDir = compileDir;
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
   * The package directory.
   *
   * @param packageDir The package directory.
   */
  public void setPackageDir(File packageDir) {
    this.packageDir = packageDir;
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
   * @param debug Whether to print debugging information.
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
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
   * The target.
   *
   * @param target The target.
   */
  public void setTarget(String target) {
    this.target = Enunciate.Target.valueOf(target.toUpperCase());
  }

  /**
   * The path to gwt home.
   *
   * @param gwtHome The path to gwt home.
   */
  public void setGwtHome(File gwtHome) {
    this.gwtHome = gwtHome;
  }

  /**
   * The path to flex home.
   *
   * @param flexHome The path to flex home.
   */
  public void setFlexHome(File flexHome) {
    this.flexHome = flexHome;
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
   * An Enunciate mechanism that leverages Ant's logging capabilities, too.
   */
  private class AntLoggingEnunciate extends Enunciate {

    public AntLoggingEnunciate(String[] sourceFiles) {
      super(sourceFiles);
    }

    @Override
    public void debug(String message, Object... formatArgs) {
      super.debug(message, formatArgs);
      getProject().log(String.format(message, formatArgs), Project.MSG_DEBUG);
    }

    @Override
    public void info(String message, Object... formatArgs) {
      super.info(message, formatArgs);
      getProject().log(String.format(message, formatArgs), Project.MSG_INFO);
    }

    @Override
    public void warn(String message, Object... formatArgs) {
      getProject().log(String.format(message, formatArgs), Project.MSG_WARN);
    }
  }

}

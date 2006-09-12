package net.sf.enunciate.main;

import net.sf.enunciate.EnunciateException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.File;
import java.io.IOException;

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
  private Enunciate.Target target;

  /**
   * Executes the enunciate task.
   */
  @Override
  public void execute() throws BuildException {
    if (basedir == null) {
      throw new BuildException("A base directory must be specified.");
    }

    DirectoryScanner scanner = getDirectoryScanner(basedir);
    scanner.scan();
    String[] files = scanner.getIncludedFiles();
    for (int i = 0; i < files.length; i++) {
      File file = new File(basedir, files[i]);
      files[i] = file.getAbsolutePath();
    }

    try {
      Enunciate proxy = new Enunciate(files);

      if (classpath != null) {
        proxy.setClasspath(classpath.toString());
      }

      if (this.configFile != null) {
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

}

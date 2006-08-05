package net.sf.enunciate.main;

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

  private final Enunciate proxy = new Enunciate();
  private File basedir;
  private Path classpath;
  private Path warlib;

  /**
   * Executes the enunciate task.
   */
  @Override
  public void execute() throws BuildException {
    if (basedir == null) {
      throw new BuildException("A base directory must be specified.");
    }

    if (classpath != null) {
      proxy.setClasspath(classpath.toString());
    }

    if (warlib != null) {
      proxy.setWarLibs(warlib.toString());
    }

    DirectoryScanner scanner = getDirectoryScanner(basedir);
    scanner.scan();
    String[] files = scanner.getIncludedFiles();
    for (int i = 0; i < files.length; i++) {
      File file = new File(basedir, files[i]);
      files[i] = file.getAbsolutePath();
    }

    try {
      proxy.execute(files);
    }
    catch (IOException e) {
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
   * Whether to be verbose.
   *
   * @param verbose Whether to be verbose.
   */
  public void setVerbose(boolean verbose) {
    proxy.setVerbose(verbose);
  }

  /**
   * Whether to print debugging information.
   *
   * @param debug Whether to print debugging information.
   */
  public void setDebug(boolean debug) {
    proxy.setDebug(debug);
  }

  /**
   * The enunciate config file.
   *
   * @param config The enunciate config file.
   */
  public void setConfigFile(File config) {
    proxy.setConfigFile(config);
  }

  /**
   * The directory for the compiled classes.
   *
   * @param destDir The directory for the compiled classes.
   */
  public void setDestDir(File destDir) {
    proxy.setDestDir(destDir);
  }

  /**
   * The directory to use to build the war.
   *
   * @param warBuildDir The directory to use to build the war.
   */
  public void setWarBuildDir(File warBuildDir) {
    proxy.setWarBuildDir(warBuildDir);
  }

  /**
   * The war file to create.
   *
   * @param warFile The war file to create.
   */
  public void setWarFile(File warFile) {
    proxy.setWarFile(warFile);
  }

  /**
   * The directory for the preprocessed files.
   *
   * @param preprocessDir The directory for the preprocessed files.
   */
  public void setPreprocessDir(File preprocessDir) {
    proxy.setPreprocessDir(preprocessDir);
  }

  /**
   * The target.
   *
   * @param target The target.
   */
  public void setTarget(String target) {
    proxy.setTarget(Enunciate.Target.valueOf(target.toUpperCase()));
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
   * The war libraries to use to enunciate.
   *
   * @param warlib The war libraries to use to enunciate.
   */
  public void setWarlib(Path warlib) {
    if (this.warlib == null) {
      this.warlib = warlib;
    }
    else {
      this.warlib.append(warlib);
    }
  }

  /**
   * The war libraries to use to enunciate.
   *
   * @return The war libraries to use to enunciate.
   */
  public Path getWarlib() {
    return warlib;
  }

  /**
   * Adds a path to the warlib.
   *
   * @return The path.
   */
  public Path createWarlib() {
    if (warlib == null) {
      warlib = new Path(getProject());
    }
    return warlib.createPath();
  }

  /**
   * Adds a reference to a warlib defined elsewhere.
   *
   * @param ref a reference to a warlib.
   */
  public void setWarlibRef(Reference ref) {
    createWarlib().setRefid(ref);
  }


}

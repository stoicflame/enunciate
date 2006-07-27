package net.sf.enunciate.main;

import net.sf.enunciate.apt.EnunciateAnnotationProcessorFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
     * Generate, compile, and package it up.
     */
    PACKAGE
  }

  private boolean verbose = false;

  private File config;
  private File destDir;
  private File preprocessDir;
  private String classpath;
  private Target target = Target.PACKAGE;

  public static void main(String[] args) {
    Enunciate enunciate = new Enunciate();
    //todo: compile the args, set the variables, then:
    //enunciate.execute();
  }

  /**
   * Enunciate the specified source files.
   *
   * @param sourceFiles The source files to enunciate.
   */
  public void execute(String[] sourceFiles) throws IOException {
    boolean success = invokeApt(sourceFiles);

    if (success && (getTarget().ordinal() >= Target.COMPILE.ordinal())) {
      //todo: HERE put the code to compile, package up the war.

    }
  }

  /**
   * Invokes APT on the specified source files.
   *
   * @param sourceFiles The source files.
   * @return Whether the invocation was successful.
   */
  protected boolean invokeApt(String[] sourceFiles) throws IOException {
    ArrayList<String> args = new ArrayList<String>();
    String classpath = getClasspath();
    if (classpath == null) {
      classpath = System.getProperty("java.class.path");
    }

    args.add("-cp");
    args.add(classpath);

    if (isVerbose()) {
      args.add(EnunciateAnnotationProcessorFactory.VERBOSE_OPTION);
    }

    if (getTarget().ordinal() < Target.COMPILE.ordinal()) {
      args.add("-nocompile");
    }
    else {
      File destdir = getDestDir();
      if (destdir == null) {
        destdir = File.createTempFile("enunciate", "");
        destdir.delete();
        destdir.mkdirs();
        setDestDir(destdir);
      }

      args.add("-d");
      args.add(destdir.getAbsolutePath());
    }

    if (getPreprocessDir() != null) {
      args.add("-s");
      args.add(getPreprocessDir().getAbsolutePath());
    }

    args.addAll(Arrays.asList(sourceFiles));

    if (isVerbose()) {
      System.out.println("Invoking APT with arguments: ");
      for (String arg : args) {
        System.out.println(arg);
      }
    }

    int procCode = com.sun.tools.apt.Main.process(new EnunciateAnnotationProcessorFactory(), args.toArray(new String[args.size()]));
    return (procCode == 0);
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
   * The enunciate config file.
   *
   * @return The enunciate config file.
   */
  public File getConfig() {
    return config;
  }

  /**
   * The enunciate config file.
   *
   * @param config The enunciate config file.
   */
  public void setConfig(File config) {
    this.config = config;
  }

  /**
   * The destination directory for the compiled classes.
   *
   * @return The destination directory for the compiled classes.
   */
  public File getDestDir() {
    return destDir;
  }

  /**
   * The destination directory for the compiled classes.
   *
   * @param destDir The destination directory for the compiled classes.
   */
  public void setDestDir(File destDir) {
    this.destDir = destDir;
  }

  /**
   * The preprocessor directory (-s).
   *
   * @return The preprocessor directory (-s).
   */
  public File getPreprocessDir() {
    return preprocessDir;
  }

  /**
   * The preprocessor directory (-s).
   *
   * @param preprocessDir The preprocessor directory (-s).
   */
  public void setPreprocessDir(File preprocessDir) {
    this.preprocessDir = preprocessDir;
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

}

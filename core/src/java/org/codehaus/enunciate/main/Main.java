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

import java.io.File;

/**
 * Class for invoking Enunciate from the command line.
 *
 * @author Ryan Heaton
 */
public class Main {

  private Main() {
  }

  public static void main(String[] args) throws Exception {
    Enunciate enunciate = new Enunciate();
    if ((args == null) || (args.length < 1)) {
      printUsage();
      System.exit(1);
    }

    for (int argIndex = 0; argIndex < args.length; argIndex++) {
      String option = args[argIndex];

      //if it doesn't start with a '-', consider it (and the rest of the args) a source file...
      if (!option.startsWith("-")) {
        String[] sourceFiles = new String[args.length - argIndex];
        System.arraycopy(args, argIndex, sourceFiles, 0, sourceFiles.length);
        enunciate.setSourceFiles(sourceFiles);
        break;
      }
      else {
        option = option.startsWith("--") ? option.substring(2) : option.substring(1);

        //first see if the option can be handled without a value.
        boolean handled = false;
        for (Option opt : Option.values()) {
          if (handled = opt.handle(option, enunciate)) {
            break;
          }
        }

        if (!handled) {
          argIndex++;
          if (args.length > argIndex) {
            String value = args[argIndex];
            for (Option opt : Option.values()) {
              if (handled = opt.handle(option, value, enunciate)) {
                break;
              }
            }
          }
          else {
            System.out.println("Unable to parse option: " + option + " (try specifying a value).");
            printUsage();
            System.exit(1);
          }
        }

        if (!handled) {
          System.out.println("Unknown option: " + option);
          printUsage();
          System.exit(1);
        }
      }
    }

    enunciate.execute();
  }

  public static void printUsage() {
    System.out.println("Usage: <enunciate command> [options] [source files]");
    System.out.println("Where possible options include:");
    for (Option option : Option.values()) {
      System.out.println(option.getHelpInfo());
    }
  }

  public enum Option {

    verbose("v", "Print verbose output to the console."),
    debug("vv", "Print debug-level output to the console."),
    javacCheck("Xc", "Do a javac check before invoking Enunciate."),
    configFile("f", "file", "The enunciate xml config file."),
    generateDir("g", "dir", "The output directory for the \"generate\" step."),
    compileDir("c", "dir", "The output directory for the \"compile\" step."),
    buildDir("b", "dir", "The output directory for the \"build\" step."),
    packageDir("p", "dir", "The output directory for the \"package\" step."),
    classpath("cp", "path", "The classpath to use (defaults to the system classpath)."),
    target("t", "target", "The target step (defaults to \"package\"). Possible values: \"generate\", \"compile\", \"build\", \"package\"."),
    export("E[artifactId]", "file or dir", "The file (or directory) to which to export the artifact identified by [artifactId]");

    private final String id;
    private final String valueName;
    private final String description;


    Option(String id, String description) {
      this(id, null, description);
    }

    Option(String id, String valueName, String description) {
      this.id = id;
      this.valueName = valueName;
      this.description = description;
    }

    /**
     * The id of the option.
     *
     * @return The id of the option.
     */
    public String getId() {
      return id;
    }

    /**
     * The name of the option.
     *
     * @return The name of the option.
     */
    public String getName() {
      return toString();
    }

    /**
     * The name of the value.
     *
     * @return The name of the value.
     */
    public String getValueName() {
      return valueName;
    }

    /**
     * The description of the option.
     *
     * @return The description of the option.
     */
    public String getDescription() {
      return description;
    }

    /**
     * Print out the help information for this option.
     *
     * @return The help information.
     */
    public String getHelpInfo() {
      String optionFormat;
      if (this == export) {
        optionFormat = String.format("-%s %s", getId(), getValueName()); 
      }
      else {
        optionFormat = this.valueName == null ? String.format("[%-3s| --%s]", "-" + getId(), getName())
                                              : String.format("[%-3s| --%s] <%s>", "-" + getId(), getName(), getValueName());
      }
      return String.format("  %-30s %s", optionFormat, getDescription());
    }

    /**
     * Handle the specified option (with no value).
     *
     * @param option The option to handle.
     * @param enunciate The mechanism to manipulate.
     * @return Whether the option was successfully handled without a value.
     */
    public boolean handle(String option, Enunciate enunciate) {
      if ((!getId().equals(option)) && (!getName().equals(option))) {
        return false;
      }

      switch (this) {
        case verbose:
          enunciate.setVerbose(true);
          return true;
        case debug:
          enunciate.setDebug(true);
          return true;
        case javacCheck:
          enunciate.setJavacCheck(true);
          return true;
        default:
          return false;
      }
    }

    /**
     * Handle the specified option with the specified value for the given
     * enunciate mechanism.
     *
     * @param option The option to handle.
     * @param value The value to handle.
     * @param enunciate The mechansim to use.
     * @return Whether the option was successfully handled with the specified value.
     */
    public boolean handle(String option, String value, Enunciate enunciate) {
      if ((this != export) && (!getId().equals(option)) && (!getName().equals(option))) {
        return false;
      }

      switch (this) {
        case configFile:
          File file = new File(value);
          if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + file.getAbsolutePath());
          }
          enunciate.setConfigFile(file);
          return true;

        case generateDir:
          File dir = new File(value);
          if (!dir.exists()) {
            throw new IllegalArgumentException("Generate directory not found: " + dir.getAbsolutePath());
          }
          if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Generate directory " + dir.getAbsolutePath() + " is not a directory.");
          }
          enunciate.setGenerateDir(dir);
          return true;

        case compileDir:
          dir = new File(value);
          if (!dir.exists()) {
            throw new IllegalArgumentException("Compile directory not found: " + dir.getAbsolutePath());
          }
          if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Compile directory " + dir.getAbsolutePath() + " is not a directory.");
          }
          enunciate.setCompileDir(dir);
          return true;

        case buildDir:
          dir = new File(value);
          if (!dir.exists()) {
            throw new IllegalArgumentException("Build directory not found: " + dir.getAbsolutePath());
          }
          if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Build directory " + dir.getAbsolutePath() + " is not a directory.");
          }
          enunciate.setBuildDir(dir);
          return true;

        case packageDir:
          dir = new File(value);
          if (!dir.exists()) {
            throw new IllegalArgumentException("Package directory not found: " + dir.getAbsolutePath());
          }
          if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Package directory " + dir.getAbsolutePath() + " is not a directory.");
          }
          enunciate.setPackageDir(dir);
          return true;

        case classpath:
          enunciate.setClasspath(value);
          return true;

        case target:
          try {
            enunciate.setTarget(Enunciate.Target.valueOf(value.toUpperCase()));
          }
          catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown target: " + value);
          }
          return true;

        case export:
          if (!option.startsWith("E")) {
            return false;
          }

          enunciate.addExport(option.substring(1), new File(value));
          return true;

        default:
          return false;
      }
    }

  }
}

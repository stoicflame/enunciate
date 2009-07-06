package org.codehaus.enunciate.modules;

import java.io.File;

/**
 * A module that assembles the Enunciate project.
 *
 * @author Ryan Heaton
 */
public interface ProjectAssemblyModule extends OutputDirectoryAware {

  /**
   * whether this module should take on the responsibility of compiling the server-side classes
   *
   * @param doCompile whether this module should take on the responsibility of compiling the server-side classes
   */
  void setDoCompile(boolean doCompile);

  /**
   * whether this module should take on the responsibility of copying libraries to WEB-INF/lib
   *
   * @param doLibCopy whether this module should take on the responsibility of copying libraries to WEB-INF/lib
   */
  void setDoLibCopy(boolean doLibCopy);

  /**
   * whether this module should take on the responsibility of packaging (zipping) up the war
   *
   * @param doPackage whether this module should take on the responsibility of packaging (zipping) up the war
   */
  void setDoPackage(boolean doPackage);

  /**
   * The directory into which to build the (expanded) war.
   *
   * @param buildDir The directory into which to build the (expanded) war.
   */
  void setBuildDir(File buildDir);
}

package com.webcohesion.enunciate;

import java.io.File;
import java.util.Set;

/**
 * Encapsulation of the output of the Enunciate engine.
 *
 * @author Ryan Heaton
 */
public class EnunciateContext {

  private final EnunciateLogger logger;
  private final Set<File> sourceFiles;
  private final Set<String> includedTypes;

  public EnunciateContext(EnunciateLogger logger, Set<File> sourceFiles, Set<String> includedTypes) {
    this.logger = logger;
    this.sourceFiles = sourceFiles;
    this.includedTypes = includedTypes;
  }

  public EnunciateLogger getLogger() {
    return logger;
  }

  public Set<File> getSourceFiles() {
    return sourceFiles;
  }

  public Set<String> getIncludedTypes() {
    return includedTypes;
  }

}

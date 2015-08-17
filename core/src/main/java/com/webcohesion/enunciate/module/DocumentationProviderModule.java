package com.webcohesion.enunciate.module;

import java.io.File;

/**
 * @author Ryan Heaton
 */
public interface DocumentationProviderModule extends EnunciateModule {

  void setDefaultDocsDir(File docsDir);

  void setDefaultDocsSubdir(String defaultDocsSubdir);
}

package com.webcohesion.enunciate.module;

import java.io.File;

/**
 * @author Ryan Heaton
 */
public interface WebInfAwareModule {

  void setWebInfDir(File webInfDir);

}

package com.webcohesion.enunciate.module;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.EnunciateContext;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface EnunciateModule {

  String getName();

  List<DependencySpec> getDependencySpecifications();

  boolean isEnabled();

  void init(Enunciate engine);

  void init(EnunciateContext context);

  void call(EnunciateContext context);

}

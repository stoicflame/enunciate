package com.webcohesion.enunciate.module;

import java.util.Set;

/**
 * Marker interface for modules that are aware of the modules that depend on them.
 *
 * @author Ryan Heaton
 */
public interface DependingModuleAwareModule extends EnunciateModule {

  void acknowledgeDependingModules(Set<String> dependingModules);

}

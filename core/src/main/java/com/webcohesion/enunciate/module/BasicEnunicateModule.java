package com.webcohesion.enunciate.module;

import com.webcohesion.enunciate.EnunciateContext;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public abstract class BasicEnunicateModule implements EnunciateModule, DependingModuleAware {

  protected Set<String> dependingModules = null;
  protected EnunciateContext context;

  @Override
  public void init(EnunciateContext context) {
    this.context = context;
  }

  @Override
  public void acknowledgeDependingModules(Set<String> dependingModules) {
    this.dependingModules = dependingModules;
  }

  protected void debug(String message, Object... formatArgs) {
    this.context.getLogger().debug(message, formatArgs);
  }

  protected void info(String message, Object... formatArgs) {
    this.context.getLogger().info(message, formatArgs);
  }

  protected void warn(String message, Object... formatArgs) {
    this.context.getLogger().warn(message, formatArgs);
  }

}

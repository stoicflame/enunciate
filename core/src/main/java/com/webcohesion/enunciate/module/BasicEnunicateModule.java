package com.webcohesion.enunciate.module;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.EnunciateContext;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public abstract class BasicEnunicateModule implements EnunciateModule, DependingModuleAware {

  protected Set<String> dependingModules = null;
  protected Enunciate enunciate;
  protected EnunciateContext context;

  @Override
  public void init(Enunciate engine) {
    this.enunciate = engine;
  }

  @Override
  public void init(EnunciateContext context) {
    this.context = context;
  }

  @Override
  public void acknowledgeDependingModules(Set<String> dependingModules) {
    this.dependingModules = dependingModules;
  }

  protected void debug(String message, Object... formatArgs) {
    this.enunciate.getLogger().debug(message, formatArgs);
  }

  protected void info(String message, Object... formatArgs) {
    this.enunciate.getLogger().info(message, formatArgs);
  }

  protected void warn(String message, Object... formatArgs) {
    this.enunciate.getLogger().warn(message, formatArgs);
  }

}
